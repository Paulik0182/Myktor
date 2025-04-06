package com.nayya.myktor.ui.root

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.nayya.myktor.databinding.ActivityMainBinding
import com.nayya.myktor.domain.CounterpartyEntity
import com.nayya.myktor.domain.OrderEntity
import com.nayya.myktor.domain.ProductEntity
import com.nayya.myktor.domain.productentity.CategoryEntity
import com.nayya.myktor.domain.productentity.Product
import com.nayya.myktor.ui.product.AccountingProductsFragment
import com.nayya.myktor.ui.product.category.CategoriesFragment
import com.nayya.myktor.ui.product.category.subcategory.SubcategoryFragment
import com.nayya.myktor.ui.product.editorder.EditOrderFragment
import com.nayya.myktor.ui.product.editproduct.EditProductFragment
import com.nayya.myktor.ui.product.editsupplier.EditSupplierFragment
import com.nayya.myktor.ui.product.orders.OrdersFragment
import com.nayya.myktor.ui.product.products.ProductsFragment
import com.nayya.myktor.ui.product.productview.ViewProductFragment
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
    EditProductFragment.Controller,
    CategoriesFragment.Controller,
    SubcategoryFragment.Controller,
    ViewProductFragment.Controller {

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

    override fun openCategory() {
        swapFragment(CategoriesFragment.newInstance())
    }

    override fun openSuppliers() {
        swapFragment(SuppliersFragment.newInstance())
    }

    override fun openEditSupplierFragment(supplier: CounterpartyEntity?) {
        swapFragment(EditSupplierFragment.newInstance(supplier))
    }

    override fun openEditOrderFragment(orderEntity: OrderEntity?) {
        swapFragment(EditOrderFragment.newInstance(orderEntity?.id))
    }

    override fun openSubcategoryFragment(category: CategoryEntity, products: List<Product>) {
        swapFragment(SubcategoryFragment.newInstance(category, products))
    }

    override fun openProductsBySubcategory(subcategoryId: Long, products: List<Product>) {
        swapFragment(ProductsFragment.newInstance(products, subcategoryId))
    }
    override fun openProductFragment(product: Product) {
        swapFragment(ViewProductFragment.newInstance(product))
    }

    override fun openEditProductFragment(productId: Long) {
        swapFragment(EditProductFragment.newInstance(productId))
    }
}