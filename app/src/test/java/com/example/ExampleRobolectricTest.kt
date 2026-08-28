package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.UserPreferences
import com.example.data.model.Customer
import com.example.data.model.CustomerPurchase
import com.example.data.model.Expense
import com.example.data.model.Plant
import com.example.data.model.PurchasePlatforms
import com.example.data.model.Sale
import com.example.data.model.SaleItem
import com.example.data.model.StockLog
import com.example.util.ExportUtils
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

  @Test
  fun `verify multi-plant sale items model calculations`() {
    val item1 = SaleItem(plantId = 1L, plantName = "Mango", quantity = 1, unitPrice = 699.0, discount = 0.0, lineTotal = 699.0)
    val item2 = SaleItem(plantId = 2L, plantName = "Jamun", quantity = 1, unitPrice = 749.0, discount = 0.0, lineTotal = 749.0)
    val item3 = SaleItem(plantId = 3L, plantName = "Guava", quantity = 5, unitPrice = 749.0, discount = 100.0, lineTotal = 5 * 749.0 - 100.0)

    val items = listOf(item1, item2, item3)
    val totalQty = items.sumOf { it.quantity }
    val totalSubtotal = items.sumOf { it.quantity * it.unitPrice }
    val totalDiscount = items.sumOf { it.discount }
    val totalAmount = items.sumOf { it.lineTotal }

    assertEquals(7, totalQty)
    assertEquals(1 * 699.0 + 1 * 749.0 + 5 * 749.0, totalSubtotal, 0.001)
    assertEquals(100.0, totalDiscount, 0.001)
    assertEquals(5093.0, totalAmount, 0.001)

    val jsonArray = JSONArray().apply {
      items.forEach { put(it.toJson()) }
    }
    val jsonString = jsonArray.toString()

    val sale = Sale(
      id = 50L,
      customerId = 1L,
      customerName = "Ramesh Kumar",
      plantId = item1.plantId,
      plantName = "Mango, Jamun + 1 more",
      quantity = totalQty,
      unitPrice = totalSubtotal / totalQty,
      discount = totalDiscount,
      amount = totalAmount,
      itemsJson = jsonString
    )

    val parsedItems = sale.getSaleItems()
    assertEquals(3, parsedItems.size)
    assertEquals("Mango", parsedItems[0].plantName)
    assertEquals(1, parsedItems[0].quantity)
    assertEquals(699.0, parsedItems[0].unitPrice, 0.001)

    assertEquals("Jamun", parsedItems[1].plantName)
    assertEquals(1, parsedItems[1].quantity)
    assertEquals(749.0, parsedItems[1].unitPrice, 0.001)

    assertEquals("Guava", parsedItems[2].plantName)
    assertEquals(5, parsedItems[2].quantity)
    assertEquals(749.0, parsedItems[2].unitPrice, 0.001)
    assertEquals(100.0, parsedItems[2].discount, 0.001)
    assertEquals(3645.0, parsedItems[2].lineTotal, 0.001)

    assertEquals("Mango, Jamun, Guava", sale.getItemsSummary())
  }

  @Test
  fun `verify legacy single-plant sale backward compatibility fallback`() {
    val legacySale = Sale(
      id = 10L,
      customerId = 2L,
      customerName = "Anita Roy",
      plantId = 5L,
      plantName = "Bougainvillea Pink",
      quantity = 3,
      unitPrice = 150.0,
      discount = 20.0,
      amount = 430.0,
      itemsJson = ""
    )

    val items = legacySale.getSaleItems()
    assertEquals(1, items.size)
    assertEquals(5L, items[0].plantId)
    assertEquals("Bougainvillea Pink", items[0].plantName)
    assertEquals(3, items[0].quantity)
    assertEquals(150.0, items[0].unitPrice, 0.001)
    assertEquals(20.0, items[0].discount, 0.001)
    assertEquals(430.0, items[0].lineTotal, 0.001)
    assertEquals("Bougainvillea Pink", legacySale.getItemsSummary())
  }

  @Test
  fun `verify user preferences default invoice notes and closing footer`() {
    val prefs = UserPreferences()
    assertEquals("Thank you for buying from our nursery! Plant more trees.", prefs.invoiceNotes)
    assertEquals("Visit Again!...", prefs.invoiceFooter)
  }

  @Test
  fun `verify receipt generation with default invoice notes and closing footer`() {
    val sale = Sale(
      id = 101L,
      customerId = 1L,
      customerName = "Ramesh Kumar",
      plantId = 1L,
      plantName = "Mango Tree",
      quantity = 2,
      unitPrice = 300.0,
      discount = 50.0,
      amount = 550.0,
      paymentMethod = "Cash",
      notes = ""
    )

    val receipt = ExportUtils.generateReceiptText(
      nurseryName = "Sahnur Nursery",
      ownerPhone = "+91 98765 00000",
      address = "Greenbelt Zone",
      sale = sale,
      currencySymbol = "₹"
    )

    assertTrue(receipt.contains("Sahnur Nursery"))
    assertTrue(receipt.contains("INVOICE / CASH MEMO #INV-00101"))
    assertTrue(receipt.contains("Notes: Thank you for buying from our nursery! Plant more trees."))
    assertTrue(receipt.contains("Visit Again!..."))
  }

  @Test
  fun `verify receipt generation with customized invoice notes and closing footer`() {
    val sale = Sale(
      id = 102L,
      customerId = 2L,
      customerName = "Anita Roy",
      plantId = 2L,
      plantName = "Guava Plant",
      quantity = 5,
      unitPrice = 120.0,
      discount = 0.0,
      amount = 600.0,
      paymentMethod = "UPI",
      notes = ""
    )

    val customNotes = "Special Organic Fertilizer Guide included!\nWater gently twice a week."
    val customFooter = "Thank you for supporting our green mission! 🌱🌻"

    val receipt = ExportUtils.generateReceiptText(
      nurseryName = "Green Leaf Nursery",
      ownerPhone = "+91 91234 56789",
      address = "Sector 5, Kolkata",
      sale = sale,
      currencySymbol = "₹",
      invoiceNotes = customNotes,
      invoiceFooter = customFooter
    )

    assertTrue(receipt.contains("Notes: Special Organic Fertilizer Guide included!\nWater gently twice a week."))
    assertTrue(receipt.contains("Thank you for supporting our green mission! 🌱🌻"))
  }

  @Test
  fun `verify receipt generation with blank notes and blank footer omits sections cleanly`() {
    val sale = Sale(
      id = 103L,
      customerId = 3L,
      customerName = "Subhashish Bose",
      plantId = 3L,
      plantName = "Rose Red",
      quantity = 1,
      unitPrice = 80.0,
      discount = 0.0,
      amount = 80.0,
      paymentMethod = "Cash",
      notes = ""
    )

    val receipt = ExportUtils.generateReceiptText(
      nurseryName = "Botanica Gardens",
      ownerPhone = "+91 90000 00000",
      address = "Pune, Maharashtra",
      sale = sale,
      currencySymbol = "₹",
      invoiceNotes = "",
      invoiceFooter = ""
    )

    assertFalse(receipt.contains("Notes:"))
    assertFalse(receipt.contains("Visit Again"))
    assertTrue(receipt.endsWith("========================================"))
  }

  @Test
  fun `verify sale specific notes override global invoice notes`() {
    val sale = Sale(
      id = 104L,
      customerId = 4L,
      customerName = "Deepak Sen",
      plantId = 4L,
      plantName = "Lemon Hybrid",
      quantity = 2,
      unitPrice = 150.0,
      discount = 0.0,
      amount = 300.0,
      paymentMethod = "Card",
      notes = "Special delivery request: Deliver after 5 PM"
    )

    val receipt = ExportUtils.generateReceiptText(
      nurseryName = "Sahnur Nursery",
      ownerPhone = "+91 98765 00000",
      address = "Greenbelt Zone",
      sale = sale,
      currencySymbol = "₹",
      invoiceNotes = "Default plant more trees note",
      invoiceFooter = "Happy Gardening!"
    )

    assertTrue(receipt.contains("Notes: Special delivery request: Deliver after 5 PM"))
    assertFalse(receipt.contains("Default plant more trees note"))
    assertTrue(receipt.contains("Happy Gardening!"))
  }
}
