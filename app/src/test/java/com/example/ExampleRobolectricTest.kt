package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.NurseryBackup
import com.example.data.model.Customer
import com.example.data.model.CustomerExportData
import com.example.data.model.CustomerPurchase
import com.example.data.model.CustomerWithPurchases
import com.example.data.model.PurchasePlatforms
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Sahnur Nursery Manager", appName)
  }

  @Test
  fun `verify customer purchase serialization and calculation`() {
    val customer = Customer(
      id = 1L,
      name = "Rafiqul Islam",
      mobile = "+91 9876543210",
      address = "Kolkata, WB",
      notes = "Prefers hybrid mango varieties"
    )

    val purchases = listOf(
      CustomerPurchase(
        id = 101L,
        customerId = 1L,
        platform = PurchasePlatforms.WEBSITE,
        productName = "Miyazaki Mango Grafted",
        quantity = 2,
        purchasePrice = 850.0,
        purchaseDate = 1700000000000L,
        remarks = "Prepaid online"
      ),
      CustomerPurchase(
        id = 102L,
        customerId = 1L,
        platform = PurchasePlatforms.WHATSAPP,
        productName = "Thai 7 Guava",
        quantity = 5,
        purchasePrice = 120.0,
        purchaseDate = 1700100000000L,
        remarks = "COD via courier"
      )
    )

    val customerWithPurchases = CustomerWithPurchases(
      customer = customer,
      purchases = purchases
    )

    val exportData = CustomerExportData(
      exportType = "CUSTOMERS_ONLY",
      version = 1,
      timestamp = System.currentTimeMillis(),
      customers = listOf(customerWithPurchases)
    )

    val gson = Gson()
    val json = gson.toJson(exportData)
    assertNotNull(json)
    assertTrue(json.contains("CUSTOMERS_ONLY"))
    assertTrue(json.contains("Miyazaki Mango Grafted"))
    assertTrue(json.contains("Thai 7 Guava"))
    assertTrue(json.contains(PurchasePlatforms.WEBSITE))
    assertTrue(json.contains(PurchasePlatforms.WHATSAPP))

    // Verify deserialization
    val parsed = gson.fromJson(json, CustomerExportData::class.java)
    assertEquals("CUSTOMERS_ONLY", parsed.exportType)
    assertEquals(1, parsed.customers.size)
    assertEquals(2, parsed.customers[0].purchases.size)
    assertEquals(850.0, parsed.customers[0].purchases[0].purchasePrice, 0.001)

    val totalSpent = parsed.customers[0].purchases.sumOf { it.purchasePrice * it.quantity }
    assertEquals(2 * 850.0 + 5 * 120.0, totalSpent, 0.001)
  }

  @Test
  fun `verify full nursery backup backwards compatibility`() {
    val backup = NurseryBackup(
      version = 3,
      timestamp = System.currentTimeMillis(),
      plants = emptyList(),
      sales = emptyList(),
      expenses = emptyList(),
      customers = listOf(
        Customer(id = 1L, name = "Abdul Rahim", mobile = "9876543210", address = "Village Green", notes = "")
      ),
      customerPurchases = listOf(
        CustomerPurchase(
          id = 1L,
          customerId = 1L,
          platform = PurchasePlatforms.AMAZON,
          productName = "Bonsai Ficus",
          quantity = 1,
          purchasePrice = 650.0
        )
      )
    )

    val gson = Gson()
    val json = gson.toJson(backup)
    val restored = gson.fromJson(json, NurseryBackup::class.java)
    assertEquals(1, restored.customers.size)
    assertEquals(1, restored.customerPurchases.size)
    assertEquals("Amazon", restored.customerPurchases[0].platform)
    assertEquals(650.0, restored.customerPurchases[0].purchasePrice, 0.001)
  }
}
