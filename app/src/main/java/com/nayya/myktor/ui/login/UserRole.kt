package com.nayya.myktor.ui.login

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    @SerialName("SYSTEM_ADMIN")
    SYSTEM_ADMIN,

    @SerialName("WAREHOUSE_ADMIN")
    WAREHOUSE_ADMIN,

    @SerialName("SHOP_ADMIN")
    SHOP_ADMIN,

    @SerialName("SUPPLIER")
    SUPPLIER,

    @SerialName("CUSTOMER")
    CUSTOMER
}
