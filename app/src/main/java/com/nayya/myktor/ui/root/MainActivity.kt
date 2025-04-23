package com.nayya.myktor.ui.root

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.nayya.myktor.R
import com.nayya.myktor.databinding.ActivityMainBinding
import com.nayya.myktor.domain.counterpartyentity.CounterpartyEntity
import com.nayya.myktor.domain.counterpartyentity.OrderEntity
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
import com.nayya.myktor.ui.product.counterparties.CounterpartiesFragment
import com.nayya.myktor.ui.profile.ProfileFragment
import com.nayya.myktor.ui.profile.ProfileMenuType
import com.nayya.myktor.ui.test.RequestFragment
import com.nayya.myktor.utils.ViewBindingActivity

private const val TAG_ROOT_CONTAINER_FRAGMENT = "NAG_ROOT_CONTAINER_FRAGMENT"

class MainActivity : ViewBindingActivity<ActivityMainBinding>(ActivityMainBinding::inflate),
    RootFragment.Controller,
    RequestFragment.Controller,
    AccountingProductsFragment.Controller,
    OrdersFragment.Controller,
    ProductsFragment.Controller,
    CounterpartiesFragment.Controller,
    EditSupplierFragment.Controller,
    EditProductFragment.Controller,
    CategoriesFragment.Controller,
    SubcategoryFragment.Controller,
    ViewProductFragment.Controller,
    ProfileFragment.Controller {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onRootBottomNavBar()

        val bottomNav = binding.bottomNavigation

        // Пример: badge на корзину
        val cartBadge = bottomNav.getOrCreateBadge(R.id.nav_cart)
        cartBadge.isVisible = true
        cartBadge.number = 5 // любое число

        // Пример: badge на кошелек
        val walletBadge = bottomNav.getOrCreateBadge(R.id.nav_wallet)
        walletBadge.isVisible = true
        walletBadge.number = 2

        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.nav_categories
            swapFragment(CategoriesFragment.newInstance())
        }
    }

    private fun onRootBottomNavBar() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            binding.bottomNavigation.post {
                val view = binding.bottomNavigation.findViewById<View>(item.itemId)
                view?.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_up))
            }
            when (item.itemId) {
                R.id.nav_search -> {
                    swapFragment(ProductsFragment.newInstance())
                    true
                }

                R.id.nav_categories -> {
                    swapFragment(CategoriesFragment.newInstance())
                    true
                }

                R.id.nav_wallet -> {
                    swapFragment(AccountingProductsFragment.newInstance())
                    // открыть кошелек
                    true
                }

                R.id.nav_cart -> {
                    // открыть корзину
                    true
                }

                R.id.nav_profile -> {
                    swapFragment(ProfileFragment.newInstance())
                    // Профиль
                    true
                }

                else -> false
            }
        }
    }

    private fun swapFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .add(binding.container.id, fragment, TAG_ROOT_CONTAINER_FRAGMENT)
//            .replace(binding.container.id, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun openTestCollServer() {
        swapFragment(RequestFragment.newInstance())
    }

    // TODO переделать. удалить
    override fun openProductAccounting() {
        swapFragment(AccountingProductsFragment.newInstance())
    }

    override fun openOrders() {
        swapFragment(OrdersFragment.newInstance())
    }

    // TODO переделать. удалить
    override fun openProduct() {
        swapFragment(ProductsFragment.newInstance())
    }

    // TODO переделать. удалить
    override fun openCategory() {
        swapFragment(CategoriesFragment.newInstance())
    }

    override fun openSuppliers() {
        swapFragment(CounterpartiesFragment.newInstance())
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

    override fun onProfileMenuItemClicked(item: ProfileMenuType) {
        Toast.makeText(this, "Нажато: ${item.title}", Toast.LENGTH_SHORT).show()
    }
}