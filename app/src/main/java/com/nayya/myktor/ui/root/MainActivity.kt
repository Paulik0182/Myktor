package com.nayya.myktor.ui.root

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.nayya.myktor.databinding.ActivityMainBinding
import com.nayya.myktor.ui.product.orders.OrdersFragment
import com.nayya.myktor.ui.product.AccountingProductsFragment
import com.nayya.myktor.ui.product.products.ProductsFragment
import com.nayya.myktor.ui.product.suppliers.SuppliersFragment
import com.nayya.myktor.ui.test.RequestFragment
import com.nayya.myktor.utils.ViewBindingActivity

private const val TAG_ROOT_CONTAINER_FRAGMENT = "NAG_ROOT_CONTAINER_FRAGMENT"

class MainActivity : ViewBindingActivity<ActivityMainBinding>(ActivityMainBinding::inflate),
    RootFragment.Controller,
    RequestFragment.Controller,
    AccountingProductsFragment.Controller,
    OrdersFragment.Controller,
    ProductsFragment.Controller,
    SuppliersFragment.Controller {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            swapFragment(RootFragment.newInstance())
        }
    }

    private fun swapFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .add(binding.container.id, fragment, TAG_ROOT_CONTAINER_FRAGMENT)
            .addToBackStack(null)
            .commit()
    }

    override fun openTestCollServer() {
        swapFragment(RequestFragment.newInstance())
    }

    override fun openProductAccounting() {
        swapFragment(AccountingProductsFragment.newInstance())
    }

    override fun openOrders() {
        swapFragment(OrdersFragment.newInstance())
    }

    override fun openProduct() {
        swapFragment(ProductsFragment.newInstance())
    }

    override fun openSuppliers() {
        swapFragment(SuppliersFragment.newInstance())
    }
}