package com.nayya.myktor.ui.profile

import com.nayya.myktor.R
import com.nayya.myktor.domain.counterpartyentity.CounterpartyEntity

// 1. Enum для типов пунктов меню профиля
enum class ProfileMenuGroup {
    SYSTEM,
    PURCHASES,
    INFO,
    SETTINGS,
    OTHER
}

sealed class ProfileMenuType(
    val title: String, // Можно заменить на @StringRes
    val iconResId: Int, // Иконка (позже)
    val group: ProfileMenuGroup,
) {
    object AdminSystem : ProfileMenuType(
        "Администратор системы",
        R.drawable.ic_admin_system,
        ProfileMenuGroup.SYSTEM
    )

    object AdminStore : ProfileMenuType(
        "Администратор магазина",
        R.drawable.ic_admin_store,
        ProfileMenuGroup.SYSTEM
    )

    object AdminWarehouse : ProfileMenuType(
        "Администратор склада",
        R.drawable.ic_admin_warehouse,
        ProfileMenuGroup.SYSTEM
    )

    object Orders : ProfileMenuType("Заказы", R.drawable.ic_orders, ProfileMenuGroup.PURCHASES)
    object Deliveries :
        ProfileMenuType("Доставки", R.drawable.ic_deliveries, ProfileMenuGroup.PURCHASES)

    object Favorites : ProfileMenuType(
        "Избранное",
        R.drawable.ic_favorites_border_menu,
        ProfileMenuGroup.PURCHASES
    )

    object PromoCodes :
        ProfileMenuType("Промокоды", R.drawable.ic_promo, ProfileMenuGroup.PURCHASES)

    object DeliveryConditions : ProfileMenuType(
        "Условия доставки",
        R.drawable.ic_delivery_conditions,
        ProfileMenuGroup.INFO
    )

    object Feedback :
        ProfileMenuType("Отзывы и вопросы", R.drawable.ic_feedback, ProfileMenuGroup.INFO)

    object InfoForUser : ProfileMenuType(
        "Информация для пользователя",
        R.drawable.ic_info_user,
        ProfileMenuGroup.INFO
    )

    object AboutApp :
        ProfileMenuType("О приложении", R.drawable.ic_about_app, ProfileMenuGroup.SETTINGS)

    object PaymentMethods :
        ProfileMenuType("Способы оплаты", R.drawable.ic_payment, ProfileMenuGroup.SETTINGS)

    object Settings :
        ProfileMenuType("Настройки", R.drawable.ic_settings, ProfileMenuGroup.SETTINGS)

    object PrivacyPolicy : ProfileMenuType(
        "Политика конфиденциальности",
        R.drawable.ic_privacy,
        ProfileMenuGroup.SETTINGS
    )


    companion object {
        fun getVisibleItems(counterparty: CounterpartyEntity): List<ProfileMenuType> = buildList {
            // Системные роли
            if (counterparty.isSupplier || counterparty.isWarehouse) {
                add(AdminSystem)
                add(AdminStore)
                add(AdminWarehouse)
            }

            // Заказчики — могут видеть заказы и избранное
            if (counterparty.isCustomer) {
                add(Orders)
                add(Deliveries)
                add(Favorites)
                add(PromoCodes)
                add(PaymentMethods)
            } else {
                add(Orders)
                add(Deliveries)
            }

            // Общая информация
            add(DeliveryConditions)
            add(Feedback)
            add(InfoForUser)

            // Настройки
            add(Settings)
            add(PrivacyPolicy)
            add(AboutApp)
        }
    }
}
