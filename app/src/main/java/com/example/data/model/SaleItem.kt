package com.example.data.model

import org.json.JSONObject

data class SaleItem(
    val plantId: Long = 0L,
    val plantName: String = "",
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val discountPercent: Double = 0.0,
    val discount: Double = 0.0,
    val lineTotal: Double = 0.0
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("plantId", plantId)
            put("plantName", plantName)
            put("quantity", quantity)
            put("unitPrice", unitPrice)
            put("discountPercent", discountPercent)
            put("discount", discount)
            put("lineTotal", lineTotal)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): SaleItem {
            val q = json.optInt("quantity", 1)
            val up = json.optDouble("unitPrice", 0.0)
            val dp = json.optDouble("discountPercent", 0.0)
            val d = if (json.has("discount")) {
                json.optDouble("discount", 0.0)
            } else if (dp > 0) {
                (q * up * (dp / 100.0))
            } else {
                0.0
            }
            val lt = if (json.has("lineTotal")) {
                json.optDouble("lineTotal", (q * up - d).coerceAtLeast(0.0))
            } else {
                (q * up - d).coerceAtLeast(0.0)
            }
            return SaleItem(
                plantId = json.optLong("plantId", 0L),
                plantName = json.optString("plantName", ""),
                quantity = q,
                unitPrice = up,
                discountPercent = dp,
                discount = d,
                lineTotal = lt
            )
        }
    }
}
