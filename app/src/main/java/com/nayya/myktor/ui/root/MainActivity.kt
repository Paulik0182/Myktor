package com.nayya.myktor.ui.root

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.nayya.myktor.databinding.ActivityMainBinding
import com.nayya.myktor.domain.CounterpartyEntity
import com.nayya.myktor.domain.OrderEntity
import com.nayya.myktor.domain.ProductEntity
import com.nayya.myktor.ui.product.AccountingProductsFragment
import com.nayya.myktor.ui.product.editorder.EditOrderFragment
import com.nayya.myktor.ui.product.editproduct.EditProductFragment
import com.nayya.myktor.ui.product.editsupplier.EditSupplierFragment
import com.nayya.myktor.ui.product.orders.OrdersFragment
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
    SuppliersFragment.Controller,
    EditSupplierFragment.Controller,
    EditProductFragment.Controller {

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

    override fun openEditSupplierFragment(supplier: CounterpartyEntity?) {
        swapFragment(EditSupplierFragment.newInstance(supplier))
    }

    override fun openEditProductFragment(product: ProductEntity?) {
        swapFragment(EditProductFragment.newInstance(product))
    }

    override fun openEditOrderFragment(orderEntity: OrderEntity?) {
        swapFragment(EditOrderFragment.newInstance(orderEntity?.id))
    }
}