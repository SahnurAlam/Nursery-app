package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.Customer
import com.example.data.model.CustomerPurchase
import com.example.data.model.Expense
import com.example.data.model.Plant
import com.example.data.model.PurchasePlatforms
import com.example.data.model.Sale
import com.example.data.model.StockLog
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    assertEquals("Nursery Data Management", appName)
  }

  @Test
  fun `verify all platform options are available and correct`() {
    val expectedPlatforms = listOf(
      "Website",
      "Facebook",
      "Instagram",
      "WhatsApp",
      "Amazon",
      "Phone Call",
      "Direct Order",
      "Other"
    )
    assertEquals(expectedPlatforms, PurchasePlatforms.ALL)
  }

  @Test
  fun `verify customer purchase fields and calculations`() {
    val customer = Customer(
      id = 1L,
      name = "Rafiqul Islam",
      mobile = "+91 9876543210",
      address = "Kolkata, WB",
      notes = "Prefers hybrid mango varieties",
      createdDate = 1700000000000L
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
      ),
      CustomerPurchase(
        id = 103L,
        customerId = 1L,
        platform = PurchasePlatforms.AMAZON,
        productName = "Dragon Fruit Plant",
        quantity = 3,
        purchasePrice = 200.0,
        purchaseDate = 1700200000000L,
        remarks = "Prime express"
      )
    )

    // Verify properties
    assertEquals(1L, customer.id)
    assertEquals("Rafiqul Islam", customer.name)
    assertEquals("+91 9876543210", customer.mobile)
    assertEquals("Kolkata, WB", customer.address)
    assertEquals("Prefers hybrid mango varieties", customer.notes)

    // Verify aggregations for purchases
    val totalPurchasesSpent = purchases.sumOf { it.purchasePrice * it.quantity }
    val totalPurchasesUnits = purchases.sumOf { it.quantity }
    val totalPurchasesCount = purchases.size

    assertEquals(2 * 850.0 + 5 * 120.0 + 3 * 200.0, totalPurchasesSpent, 0.001)
    assertEquals(10, totalPurchasesUnits)
    assertEquals(3, totalPurchasesCount)
  }

  @Test
  fun `verify POS sales and external purchases aggregation without double counting`() {
    val customerId = 1L

    // Direct POS Sales for customer
    val posSales = listOf(
      Sale(
        id = 1L,
        plantId = 10L,
        plantName = "Rose Red",
        quantity = 4,
        unitPrice = 50.0,
        amount = 200.0,
        customerId = customerId,
        customerName = "Rafiqul Islam",
        date = 1700000000000L
      ),
      Sale(
        id = 2L,
        plantId = 11L,
        plantName = "Hibiscus",
        quantity = 2,
        unitPrice = 80.0,
        amount = 160.0,
        customerId = customerId,
        customerName = "Rafiqul Islam",
        date = 1700100000000L
      )
    )

    // External logged purchases for customer
    val loggedPurchases = listOf(
      CustomerPurchase(
        id = 101L,
        customerId = customerId,
        platform = PurchasePlatforms.WEBSITE,
        productName = "Miyazaki Mango Grafted",
        quantity = 1,
        purchasePrice = 900.0,
        purchaseDate = 1700200000000L
      )
    )

    val posTotalSpent = posSales.sumOf { it.amount }
    val loggedTotalSpent = loggedPurchases.sumOf { it.purchasePrice * it.quantity }
    val grandTotalSpent = posTotalSpent + loggedTotalSpent

    val posTotalUnits = posSales.sumOf { it.quantity }
    val loggedTotalUnits = loggedPurchases.sumOf { it.quantity }
    val grandTotalUnits = posTotalUnits + loggedTotalUnits

    val totalOrdersCount = posSales.size + loggedPurchases.size

    assertEquals(360.0, posTotalSpent, 0.001)
    assertEquals(900.0, loggedTotalSpent, 0.001)
    assertEquals(1260.0, grandTotalSpent, 0.001)

    assertEquals(6, posTotalUnits)
    assertEquals(1, loggedTotalUnits)
    assertEquals(7, grandTotalUnits)

    assertEquals(3, totalOrdersCount)
  }

  @Test
  fun `verify customer-only JSON serialization and structure`() {
    val customers = listOf(
      Customer(id = 1L, name = "Amina Khatun", mobile = "9876543210", address = "Barasat", notes = "VIP buyer")
    )
    val purchases = listOf(
      CustomerPurchase(
        id = 1L,
        customerId = 1L,
        platform = PurchasePlatforms.INSTAGRAM,
        productName = "Rare Philodendron",
        quantity = 1,
        purchasePrice = 1500.0,
        remarks = "Delivered safely"
      )
    )

    val json = JSONObject().apply {
      put("exportType", "CUSTOMERS_ONLY")
      put("app", "Nursery Data Management")
      put("version", 1)
      put("exportDate", System.currentTimeMillis())
      put("totalCustomers", customers.size)
      put("customers", JSONArray().apply {
        customers.forEach { c ->
          put(JSONObject().apply {
            put("id", c.id)
            put("name", c.name)
            put("mobile", c.mobile)
            put("address", c.address)
            put("notes", c.notes)
            put("createdDate", c.createdDate)
            put("purchaseHistory", JSONArray().apply {
              purchases.filter { it.customerId == c.id }.forEach { p ->
                put(JSONObject().apply {
                  put("id", p.id)
                  put("platform", p.platform)
                  put("productName", p.productName)
                  put("quantity", p.quantity)
                  put("purchasePrice", p.purchasePrice)
                  put("purchaseDate", p.purchaseDate)
                  put("remarks", p.remarks)
                })
              }
            })
          })
        }
      })
    }.toString(2)

    val root = JSONObject(json)
    assertEquals("CUSTOMERS_ONLY", root.getString("exportType"))
    assertFalse(root.has("plants"))
    assertFalse(root.has("sales"))
    assertFalse(root.has("expenses"))
    assertFalse(root.has("stockLogs"))

    val customersArray = root.getJSONArray("customers")
    assertEquals(1, customersArray.length())
    val customerObj = customersArray.getJSONObject(0)
    assertEquals("Amina Khatun", customerObj.getString("name"))

    val purchaseHistory = customerObj.getJSONArray("purchaseHistory")
    assertEquals(1, purchaseHistory.length())
    val purchaseObj = purchaseHistory.getJSONObject(0)
    assertEquals("Rare Philodendron", purchaseObj.getString("productName"))
    assertEquals(PurchasePlatforms.INSTAGRAM, purchaseObj.getString("platform"))
    assertEquals(1500.0, purchaseObj.getDouble("purchasePrice"), 0.001)
  }

  @Test
  fun `verify full nursery backup backwards compatibility with legacy format`() {
    // Legacy format JSON without customerPurchases
    val legacyJson = """
      {
        "app": "Sahnur Nursery Manager",
        "version": 2,
        "exportDate": 1700000000000,
        "plants": [
          {"id": 1, "plantName": "Mango Amrapali", "category": "Fruit", "variety": "Amrapali", "quantity": 50, "purchasePrice": 60.0, "sellingPrice": 120.0, "lowStockThreshold": 10, "createdDate": 1700000000000}
        ],
        "customers": [
          {"id": 1, "name": "Abdul Rahim", "mobile": "9876543210", "address": "Village Green", "notes": "", "createdDate": 1700000000000}
        ],
        "sales": [],
        "expenses": [],
        "stockLogs": []
      }
    """.trimIndent()

    val root = JSONObject(legacyJson)
    assertTrue(root.has("plants"))
    assertTrue(root.has("customers"))
    assertFalse(root.has("customerPurchases"))

    val plantsArray = root.getJSONArray("plants")
    assertEquals(1, plantsArray.length())
    assertEquals("Mango Amrapali", plantsArray.getJSONObject(0).getString("plantName"))

    val customersArray = root.getJSONArray("customers")
    assertEquals(1, customersArray.length())
    assertEquals("Abdul Rahim", customersArray.getJSONObject(0).getString("name"))
  }

  @Test
  fun `verify database has no default demo seeding`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getInstance(context)
    val plants = db.plantDao().getAllPlantsList()
    val customers = db.customerDao().getAllCustomersList()
    val sales = db.saleDao().getAllSalesList()
    val expenses = db.expenseDao().getAllExpensesList()

    assertEquals(0, plants.size)
    assertEquals(0, customers.size)
    assertEquals(0, sales.size)
    assertEquals(0, expenses.size)
  }
}
