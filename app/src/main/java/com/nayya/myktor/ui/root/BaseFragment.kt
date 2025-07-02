package com.nayya.myktor.ui.root

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.nayya.myktor.R
import com.nayya.myktor.utils.LocaleUtils.goBack
import java.lang.Math.hypot

abstract class BaseFragment(layoutId: Int) : Fragment(layoutId) {

    enum class RevealOrigin {
        NONE,
        CENTER,
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        LEFT_CENTER,
        RIGHT_CENTER,
        TOP_CENTER,
        BOTTOM_CENTER
    }

    /**
     * Для отключения анимации у слабых дивайсов
     */
    private val isLowEndDevice: Boolean
        get() {
            val am = context?.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            return am?.isLowRamDevice == true
        }

    protected open val hideBottomNavigation: Boolean = false
    protected open val enableRevealAnimation: Boolean = false
    protected open val revealAnimationOrigin: RevealOrigin = RevealOrigin.NONE

    private var backStackListener: FragmentManager.OnBackStackChangedListener? = null

    override fun onStart() {
        super.onStart()
        observeBackStack()
        adjustBottomNavigation()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (revealAnimationOrigin != RevealOrigin.NONE) {
            enterWithRevealAnimation()

            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        exitWithRevealAnimation {
                            goBack()
                        }
                    }
                }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeBackStackObserver()
    }

    private fun observeBackStack() {
        if (backStackListener == null) { // ← защита от двойной подписки
            backStackListener = FragmentManager.OnBackStackChangedListener {
                adjustBottomNavigation()
            }
            requireActivity().supportFragmentManager.addOnBackStackChangedListener(backStackListener!!)
        }
    }

    private fun removeBackStackObserver() {
        backStackListener?.let {
            requireActivity().supportFragmentManager.removeOnBackStackChangedListener(it)
            backStackListener = null // ← обязательно обнуляем!
        }
    }

    private fun adjustBottomNavigation() {
        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_navigation)
        val container = requireActivity().findViewById<View>(R.id.container)

        if (hideBottomNavigation) {
            // Только если навигация ещё видна
            if (bottomNav.visibility != View.GONE) {
                bottomNav.animate()
                    .translationY(bottomNav.height.toFloat())
                    .setDuration(200)
                    .withEndAction {
                        container.setPadding(0, 0, 0, 0)
                        bottomNav.visibility = View.GONE
                    }
                    .start()
            } else {
                // Уже скрыта, но перестрахуемся
                container.setPadding(0, 0, 0, 0)
            }

        } else {
            // Только если навигация ещё не видна
            if (bottomNav.visibility != View.VISIBLE) {
                bottomNav.visibility = View.VISIBLE
                bottomNav.translationY = bottomNav.height.toFloat()
            }

            bottomNav.animate()
                .translationY(0f)
                .setDuration(200)
                .withEndAction {
                    // Устанавливаем паддинг после полной отрисовки
                    container.setPadding(0, 0, 0, bottomNav.height)
                }
                .start()
        }
    }

    private fun calculateRevealCenter(origin: RevealOrigin, view: View): Pair<Int, Int> =
        when (origin) {
            RevealOrigin.TOP_LEFT -> 0 to 0
            RevealOrigin.TOP_RIGHT -> view.width to 0
            RevealOrigin.BOTTOM_LEFT -> 0 to view.height
            RevealOrigin.BOTTOM_RIGHT -> view.width to view.height
            RevealOrigin.LEFT_CENTER -> 0 to view.height / 2
            RevealOrigin.RIGHT_CENTER -> view.width to view.height / 2
            RevealOrigin.TOP_CENTER -> view.width / 2 to 0
            RevealOrigin.BOTTOM_CENTER -> view.width / 2 to view.height
            RevealOrigin.NONE -> view.width / 2 to view.height / 2
            RevealOrigin.CENTER -> view.width / 2 to view.height / 2
        }

    protected fun enterWithRevealAnimation() {
        val v = view ?: return

        // 💡 Если слабое устройство — показываем сразу без анимации
        if (isLowEndDevice) {
            v.visibility = View.VISIBLE
            v.alpha = 1f
            return
        }

        v.visibility = View.INVISIBLE
        v.alpha = 0f

        v.post {
            val (cx, cy) = calculateRevealCenter(revealAnimationOrigin, v)
            val radius = hypot(v.width.toDouble(), v.height.toDouble()).toFloat()

            val anim = ViewAnimationUtils.createCircularReveal(v, cx, cy, 0f, radius).apply {
                duration = 700
                interpolator = AccelerateDecelerateInterpolator()
            }

            v.visibility = View.VISIBLE
            v.alpha = 1f
            anim.start()
        }
    }

    protected fun exitWithRevealAnimation(onEnd: () -> Unit) {
        val v = view ?: return onEnd()

        // 💡 Проверяем, можно ли анимировать
        if (!isAdded || !isResumed || !v.isShown) {
            Log.w("ExitReveal", "Can't animate: Fragment not in valid state")
            onEnd()
            return
        }

        // 💡 Если слабое устройство — просто скрываем и завершаем
        if (isLowEndDevice) {
            v.visibility = View.INVISIBLE
            onEnd()
            return
        }

        val (cx, cy) = calculateRevealCenter(revealAnimationOrigin, v)
        val radius = hypot(v.width.toDouble(), v.height.toDouble()).toFloat()

        val anim = ViewAnimationUtils.createCircularReveal(v, cx, cy, radius, 0f).apply {
            duration = 700
            interpolator = AccelerateDecelerateInterpolator()
        }

        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                v.visibility = View.INVISIBLE
                onEnd()
            }
        })

        anim.start()
    }

    protected inline fun <reified T : Controller> requireController(): T {
        val controller = activity as? T
        return controller
            ?: error("${activity} must implement ${T::class.java.simpleName}")
    }

    interface Controller
}
