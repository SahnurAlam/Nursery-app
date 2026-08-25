package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

object PurchasePlatforms {
    const val WEBSITE = "Website"
    const val FACEBOOK = "Facebook"
    const val INSTAGRAM = "Instagram"
    const val WHATSAPP = "WhatsApp"
    const val AMAZON = "Amazon"
    const val PHONE_CALL = "Phone Call"
    const val DIRECT_ORDER = "Direct Order"
    const val OTHER = "Other"

    val ALL = listOf(
        WEBSITE,
        FACEBOOK,
        INSTAGRAM,
        WHATSAPP,
        AMAZON,
        PHONE_CALL,
        DIRECT_ORDER,
        OTHER
    )
}

@Entity(
    tableName = "customer_purchases",
    indices = [
        Index("customerId"),
        Index("purchaseDate")
    ]
)
data class CustomerPurchase(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long = 0,
    val platform: String = PurchasePlatforms.DIRECT_ORDER,
    val productName: String = "",
    val quantity: Int = 1,
    val purchasePrice: Double = 0.0,
    val purchaseDate: Long = System.currentTimeMillis(),
    val remarks: String = ""
)
