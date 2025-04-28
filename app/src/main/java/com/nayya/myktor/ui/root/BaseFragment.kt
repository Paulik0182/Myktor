package com.nayya.myktor.ui.root

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.nayya.myktor.R

abstract class BaseFragment(layoutId: Int) : Fragment(layoutId) {

    protected open val hideBottomNavigation: Boolean = false

    private var backStackListener: FragmentManager.OnBackStackChangedListener? = null

    override fun onStart() {
        super.onStart()
        observeBackStack()
        adjustBottomNavigation()
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
        val coordinator =
            requireActivity().findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
        val container = requireActivity().findViewById<View>(R.id.container)

        if (hideBottomNavigation) {
            bottomNav.animate()
                .translationY(bottomNav.height.toFloat())
                .setDuration(200)
                .withEndAction {
                    // Убираем отступ снизу у КОНТЕЙНЕРА фрагментов
                    container.setPadding(0, 0, 0, 0)
                    bottomNav.visibility = View.GONE

                }
                .start()

        } else {
            bottomNav.visibility = View.VISIBLE
            bottomNav.animate()
                .translationY(0f)
                .setDuration(200)
                .start()

            // Ставим отступ снизу у КОНТЕЙНЕРА фрагментов
            bottomNav.post {
                container.setPadding(
                    0, 0, 0, bottomNav.height
                )
            }
        }
    }

    protected inline fun <reified T : Controller> requireController(): T {
        val controller = activity as? T
        return controller
            ?: error("${activity} must implement ${T::class.java.simpleName}")
    }

    interface Controller
}
