package com.nayya.myktor.ui.root

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.nayya.myktor.R
import com.nayya.myktor.data.RetrofitInstance
import com.nayya.myktor.data.prefs.TokenStorage
import com.nayya.myktor.databinding.ActivityMainBinding
import com.nayya.myktor.domain.counterpartyentity.CounterpartyAddresse
import com.nayya.myktor.domain.counterpartyentity.CounterpartyEntity
import com.nayya.myktor.domain.counterpartyentity.OrderEntity
import com.nayya.myktor.domain.productentity.CategoryEntity
import com.nayya.myktor.domain.productentity.Product
import com.nayya.myktor.ui.login.LoginFragment
import com.nayya.myktor.ui.product.AccountingProductsFragment
import com.nayya.myktor.ui.product.category.CategoriesFragment
import com.nayya.myktor.ui.product.category.subcategory.SubcategoryFragment
import com.nayya.myktor.ui.product.editorder.EditOrderFragment
import com.nayya.myktor.ui.product.editproduct.EditProductFragment
import com.nayya.myktor.ui.product.orders.OrdersFragment
import com.nayya.myktor.ui.product.products.ProductsFragment
import com.nayya.myktor.ui.product.productview.ViewProductFragment
import com.nayya.myktor.ui.product.counterparties.CounterpartiesFragment
import com.nayya.myktor.ui.profile.ProfileFragment
import com.nayya.myktor.ui.profile.ProfileMenuType
import com.nayya.myktor.ui.profile.address.AddressListFragment
import com.nayya.myktor.ui.profile.address.addressedit.AddressEditFragment
import com.nayya.myktor.ui.profile.detailscounterparty.CounterpartyDetailsFragment
import com.nayya.myktor.utils.ViewBindingActivity

private const val TAG_ROOT_CONTAINER_FRAGMENT = "NAG_ROOT_CONTAINER_FRAGMENT"

class MainActivity : ViewBindingActivity<ActivityMainBinding>(ActivityMainBinding::inflate),
    AccountingProductsFragment.Controller,
    OrdersFragment.Controller,
    ProductsFragment.Controller,
    CounterpartiesFragment.Controller,
    EditProductFragment.Controller,
    CategoriesFragment.Controller,
    SubcategoryFragment.Controller,
    ViewProductFragment.Controller,
    ProfileFragment.Controller,
    LoginFragment.LoginController,
    CounterpartyDetailsFragment.Controller,
    AddressListFragment.Controller {

    private lateinit var tokenStorage: TokenStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TokenStorage.init(applicationContext) // ✅ один раз при старте
        RetrofitInstance.init()

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
            openRootFragment(CategoriesFragment.newInstance())
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
                    openRootFragment(ProductsFragment.newInstance())
                    true
                }

                R.id.nav_categories -> {
                    openRootFragment(CategoriesFragment.newInstance())
                    true
                }

                R.id.nav_wallet -> {
                    openRootFragment(AccountingProductsFragment.newInstance())
                    // открыть кошелек
                    true
                }

                R.id.nav_cart -> {
                    // открыть корзину
                    openChildFragment(LoginFragment.newInstance())
                    true
                }

                R.id.nav_profile -> {
                    openRootFragment(ProfileFragment.newInstance())
                    // Профиль
                    true
                }

                else -> false
            }
        }
    }

    private fun openRootFragment(fragment: Fragment) {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(binding.container.id, fragment)
            .commit()
    }

    private fun openChildFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .add(binding.container.id, fragment, TAG_ROOT_CONTAINER_FRAGMENT)
            .addToBackStack(null)
            .commit()
    }

    private fun swapFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .add(binding.container.id, fragment, TAG_ROOT_CONTAINER_FRAGMENT)
//            .replace(binding.container.id, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        setLightStatusBarIcons(this.window)
    }

    private fun setLightStatusBarIcons(window: Window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    override fun openOrders() {
        openChildFragment(OrdersFragment.newInstance())
    }

    // TODO переделать. удалить
    override fun openProduct() {
        openChildFragment(ProductsFragment.newInstance())
    }

    // TODO переделать. удалить
    override fun openCategory() {
        openChildFragment(CategoriesFragment.newInstance())
    }

    override fun openSuppliers() {
        openChildFragment(CounterpartiesFragment.newInstance())
    }

    override fun openEditOrderFragment(orderEntity: OrderEntity?) {
        openChildFragment(EditOrderFragment.newInstance(orderEntity?.id))
    }

    override fun openSubcategoryFragment(category: CategoryEntity, products: List<Product>) {
        openChildFragment(SubcategoryFragment.newInstance(category, products))
    }

    override fun openProductsBySubcategory(subcategoryId: Long, products: List<Product>) {
        openChildFragment(ProductsFragment.newInstance(products, subcategoryId))
    }

    override fun openProductFragment(product: Product) {
        openChildFragment(ViewProductFragment.newInstance(product))
    }

    override fun openEditProductFragment(productId: Long) {
        openChildFragment(EditProductFragment.newInstance(productId))
    }

    override fun openCounterpartyDetails(counterpartyId: Long) {
        openChildFragment(CounterpartyDetailsFragment.newInstance(counterpartyId))
    }

    override fun onProfileMenuItemClicked(item: ProfileMenuType) {
        Toast.makeText(this, "Нажато: ${item.title}", Toast.LENGTH_SHORT).show()
    }

    override fun openLoginScreen() {
        openChildFragment(LoginFragment.newInstance())
    }

    override fun openEditSupplierFragment(supplier: CounterpartyEntity?) {
        TODO("Not yet implemented")
    }

    override fun onLoginSuccess(token: String) {
        // Закрываем LoginFragment
        supportFragmentManager.popBackStack() // если был addToBackStack
    }

    override fun onPrivacyPolicyClicked() {
        Toast.makeText(this, "Политика конфеденциальности", Toast.LENGTH_SHORT).show()
    }

    override fun onClientInfoClicked() {
        Toast.makeText(this, "Информация для клиентов", Toast.LENGTH_SHORT).show()
    }

    override fun openAddressEdit(address: CounterpartyAddresse?) {
        openChildFragment(AddressEditFragment.newInstance(address))
    }

    override fun openAddressList(counterpartyId: Long) {
        openChildFragment(AddressListFragment.newInstance(counterpartyId))
    }
}