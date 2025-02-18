package com.nayya.myktor.utils

import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


/** Делегат для ViewBinding
 * Безопасное использование ViewBinding. Он автоматически создает и привязывает binding к жизненному
 * циклу фрагмента, а также позаботится о том, чтобы binding был null после onDestroyView.
 *
 * binding автоматически становится null, так как Fragment больше не является владельцем свойства
 * binding. Это происходит благодаря использованию ReadOnlyProperty интерфейс, который связывает
 * lifecycle (жизненный цикл) фрагмента с lifecycle binding.
 */
class ViewBindingDelegate<T : Any>(
    private val viewBindingFactory: (View) -> T,
) : ReadOnlyProperty<Fragment, T> {
    private var binding: T? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        if (binding == null) {
            val view = thisRef.requireView()
            binding = viewBindingFactory(view)
        }
        return binding!!
    }
}

// Функция-расширение для создания делегата
inline fun <reified T : ViewBinding> Fragment.viewBinding(): ReadOnlyProperty<Fragment, T> {
    return ViewBindingDelegate { view ->
        T::class.java.getMethod("bind", View::class.java).invoke(null, view) as T
    }
}