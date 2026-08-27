package com.example

import com.example.data.model.Sale
import com.example.data.model.SaleItem
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.roundToInt

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SaleCalculationUnitTest {

    private fun round2(value: Double): Double {
        return (value * 100.0).roundToInt() / 100.0
    }

    @Test
    fun testSingleItem10PercentDiscount() {
        val unitPrice = 699.0
        val quantity = 1
        val discountPercent = 10.0

        val itemSubtotal = unitPrice * quantity
        val discountAmount = round2(itemSubtotal * (discountPercent / 100.0))
        val finalAmount = round2(itemSubtotal - discountAmount)

        assertEquals(699.00, itemSubtotal, 0.001)
        assertEquals(69.90, discountAmount, 0.001)
        assertEquals(629.10, finalAmount, 0.001)
    }

    @Test
    fun testMultiItem10PercentDiscount() {
        // Mango: 1 x 699 = 699
        // Jamun: 1 x 749 = 749
        // Guava: 5 x 749 = 3745
        val items = listOf(
            Triple("Mango", 1, 699.0),
            Triple("Jamun", 1, 749.0),
            Triple("Guava", 5, 749.0)
        )
        val discountPercent = 10.0

        val subtotal = items.sumOf { it.second * it.third }
        val discountAmount = round2(subtotal * (discountPercent / 100.0))
        val finalAmount = round2(subtotal - discountAmount)

        assertEquals(5193.00, subtotal, 0.001)
        assertEquals(519.30, discountAmount, 0.001)
        assertEquals(4673.70, finalAmount, 0.001)

        // Line-by-line distribution
        val saleItems = items.map { (name, qty, price) ->
            val lineSubtotal = qty * price
            val lineDiscount = round2(lineSubtotal * (discountPercent / 100.0))
            val lineTotal = round2(lineSubtotal - lineDiscount)
            SaleItem(
                plantId = 1L,
                plantName = name,
                quantity = qty,
                unitPrice = price,
                discountPercent = discountPercent,
                discount = lineDiscount,
                lineTotal = lineTotal
            )
        }

        assertEquals(69.90, saleItems[0].discount, 0.001)
        assertEquals(629.10, saleItems[0].lineTotal, 0.001)

        assertEquals(74.90, saleItems[1].discount, 0.001)
        assertEquals(674.10, saleItems[1].lineTotal, 0.001)

        assertEquals(374.50, saleItems[2].discount, 0.001)
        assertEquals(3370.50, saleItems[2].lineTotal, 0.001)

        val sumLineTotals = saleItems.sumOf { it.lineTotal }
        assertEquals(4673.70, sumLineTotals, 0.001)
    }

    @Test
    fun testAllPresetDiscountPercentages() {
        val basePrice = 699.0

        val percentages = listOf(0.0, 1.0, 2.5, 5.0, 10.0, 25.0, 50.0, 100.0)
        val expectedDiscounts = listOf(0.0, 6.99, 17.48, 34.95, 69.90, 174.75, 349.50, 699.00)

        for (i in percentages.indices) {
            val pct = percentages[i]
            val expectedDisc = expectedDiscounts[i]
            val calculatedDisc = round2(basePrice * (pct / 100.0))
            val finalPrice = round2(basePrice - calculatedDisc)

            assertEquals("Discount for $pct%", expectedDisc, calculatedDisc, 0.01)
            assertEquals("Final for $pct%", round2(basePrice - expectedDisc), finalPrice, 0.01)
        }
    }

    @Test
    fun testSaleItemJsonSerializationWithDiscountPercent() {
        val item = SaleItem(
            plantId = 42L,
            plantName = "Alphonso Mango",
            quantity = 3,
            unitPrice = 450.0,
            discountPercent = 15.0,
            discount = 202.50,
            lineTotal = 1147.50
        )

        val json = item.toJson()
        val restored = SaleItem.fromJson(json)

        assertEquals("Alphonso Mango", restored.plantName)
        assertEquals(3, restored.quantity)
        assertEquals(450.0, restored.unitPrice, 0.001)
        assertEquals(15.0, restored.discountPercent, 0.001)
        assertEquals(202.50, restored.discount, 0.001)
        assertEquals(1147.50, restored.lineTotal, 0.001)
    }

    @Test
    fun testSaleWithItemsJsonRoundTrip() {
        val saleItems = listOf(
            SaleItem(1L, "Mango", 1, 699.0, 10.0, 69.90, 629.10),
            SaleItem(2L, "Jamun", 1, 749.0, 10.0, 74.90, 674.10),
            SaleItem(3L, "Guava", 5, 749.0, 10.0, 374.50, 3370.50)
        )

        val array = JSONArray()
        saleItems.forEach { array.put(it.toJson()) }

        val sale = Sale(
            id = 10,
            customerName = "Anita Roy",
            quantity = 7,
            unitPrice = 699.0,
            discountPercent = 10.0,
            discount = 519.30,
            amount = 4673.70,
            itemsJson = array.toString()
        )

        val restoredItems = sale.getSaleItems()
        assertEquals(3, restoredItems.size)
        assertEquals("Mango", restoredItems[0].plantName)
        assertEquals(629.10, restoredItems[0].lineTotal, 0.001)
        assertEquals("Jamun", restoredItems[1].plantName)
        assertEquals(674.10, restoredItems[1].lineTotal, 0.001)
        assertEquals("Guava", restoredItems[2].plantName)
        assertEquals(5, restoredItems[2].quantity)
        assertEquals(3370.50, restoredItems[2].lineTotal, 0.001)
        assertEquals("Mango, Jamun, Guava", sale.getItemsSummary())
    }

    @Test
    fun testLegacySinglePlantSaleFallback() {
        val legacySale = Sale(
            id = 5,
            customerId = 1,
            customerName = "Ramesh Kumar",
            plantId = 10,
            plantName = "Guava",
            quantity = 4,
            unitPrice = 150.0,
            discount = 60.0,
            discountPercent = 10.0,
            amount = 540.0,
            paymentMethod = "Cash",
            notes = "",
            date = 1000L,
            itemsJson = ""
        )

        val items = legacySale.getSaleItems()
        assertEquals(1, items.size)
        assertEquals("Guava", items[0].plantName)
        assertEquals(4, items[0].quantity)
        assertEquals(150.0, items[0].unitPrice, 0.001)
        assertEquals(60.0, items[0].discount, 0.001)
        assertEquals(540.0, items[0].lineTotal, 0.001)
    }
}
