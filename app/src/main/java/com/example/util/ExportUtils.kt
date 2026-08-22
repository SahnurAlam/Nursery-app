package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.Customer
import com.example.data.model.Expense
import com.example.data.model.Plant
import com.example.data.model.Sale
import java.io.File
import java.io.FileOutputStream

object ExportUtils {

    fun generateSalesCsv(sales: List<Sale>, currencySymbol: String): String {
        val sb = StringBuilder()
        sb.append("Sale ID,Date,Customer Name,Plant Name,Quantity,Unit Price ($currencySymbol),Discount ($currencySymbol),Total Amount ($currencySymbol),Payment Method,Notes\n")
        sales.forEach { s ->
            sb.append("\"${s.id}\",")
            sb.append("\"${FormatUtils.formatDateTime(s.date)}\",")
            sb.append("\"${s.customerName.replace("\"", "\"\"")}\",")
            sb.append("\"${s.plantName.replace("\"", "\"\"")}\",")
            sb.append("${s.quantity},")
            sb.append("${s.unitPrice},")
            sb.append("${s.discount},")
            sb.append("${s.amount},")
            sb.append("\"${s.paymentMethod}\",")
            sb.append("\"${s.notes.replace("\"", "\"\"")}\"\n")
        }
        return sb.toString()
    }

    fun generateExpensesCsv(expenses: List<Expense>, currencySymbol: String): String {
        val sb = StringBuilder()
        sb.append("Expense ID,Date,Category,Description,Amount ($currencySymbol),Payment Method\n")
        expenses.forEach { e ->
            sb.append("\"${e.id}\",")
            sb.append("\"${FormatUtils.formatDateTime(e.date)}\",")
            sb.append("\"${e.category}\",")
            sb.append("\"${e.description.replace("\"", "\"\"")}\",")
            sb.append("${e.amount},")
            sb.append("\"${e.paymentMethod}\"\n")
        }
        return sb.toString()
    }

    fun generateInventoryCsv(plants: List<Plant>, currencySymbol: String): String {
        val sb = StringBuilder()
        sb.append("Plant ID,Plant Name,Category,Variety,Quantity,Purchase Price ($currencySymbol),Selling Price ($currencySymbol),Total Cost ($currencySymbol),Total Retail ($currencySymbol),Status,Notes\n")
        plants.forEach { p ->
            val status = when {
                p.isOutOfStock -> "OUT OF STOCK"
                p.isLowStock -> "LOW STOCK"
                else -> "IN STOCK"
            }
            val totalCost = p.quantity * p.purchasePrice
            val totalRetail = p.quantity * p.sellingPrice

            sb.append("\"${p.id}\",")
            sb.append("\"${p.plantName.replace("\"", "\"\"")}\",")
            sb.append("\"${p.category}\",")
            sb.append("\"${p.variety.replace("\"", "\"\"")}\",")
            sb.append("${p.quantity},")
            sb.append("${p.purchasePrice},")
            sb.append("${p.sellingPrice},")
            sb.append("$totalCost,")
            sb.append("$totalRetail,")
            sb.append("\"$status\",")
            sb.append("\"${p.notes.replace("\"", "\"\"")}\"\n")
        }
        return sb.toString()
    }

    fun generatePnLText(
        nurseryName: String,
        period: String,
        totalSales: Double,
        totalExpenses: Double,
        netProfit: Double,
        currencySymbol: String,
        salesCount: Int,
        expenseCount: Int
    ): String = generateProfitSummaryText(
        nurseryName, period, totalSales, totalExpenses, netProfit, currencySymbol, salesCount, expenseCount
    )

    fun shareFile(
        context: Context,
        content: String,
        fileName: String,
        mimeType: String,
        title: String
    ) = shareContentAsFile(context, content, fileName, mimeType, title)

    fun generateProfitSummaryText(
        nurseryName: String,
        period: String,
        totalSales: Double,
        totalExpenses: Double,
        netProfit: Double,
        currencySymbol: String,
        salesCount: Int,
        expenseCount: Int
    ): String {
        val margin = if (totalSales > 0) (netProfit / totalSales) * 100 else 0.0
        return """
========================================
       $nurseryName
   FINANCIAL & PROFIT REPORT
========================================
Report Period: $period
Generated on: ${FormatUtils.formatDateTime(System.currentTimeMillis())}
----------------------------------------
TOTAL REVENUE (Sales):    $currencySymbol ${"%.2f".format(totalSales)} ($salesCount transactions)
TOTAL EXPENSES:           $currencySymbol ${"%.2f".format(totalExpenses)} ($expenseCount records)
----------------------------------------
NET PROFIT:               $currencySymbol ${"%.2f".format(netProfit)}
PROFIT MARGIN:            ${"%.1f".format(margin)}%
----------------------------------------
Status: ${if (netProfit >= 0) "PROFITABLE BUSINESS" else "NET LOSS"}
========================================
        Thank You - Powered by 
       Sahnur Nursery Manager
========================================
        """.trimIndent()
    }

    fun generateReceiptText(
        nurseryName: String,
        ownerPhone: String,
        address: String,
        sale: Sale,
        currencySymbol: String
    ): String {
        return """
========================================
       $nurseryName
========================================
$address
Phone: $ownerPhone
----------------------------------------
INVOICE / CASH MEMO #INV-${sale.id.toString().padStart(5, '0')}
Date: ${FormatUtils.formatDateTime(sale.date)}
Customer: ${sale.customerName}
----------------------------------------
ITEM: ${sale.plantName}
Qty: ${sale.quantity} x $currencySymbol ${"%.2f".format(sale.unitPrice)}
Subtotal: $currencySymbol ${"%.2f".format(sale.quantity * sale.unitPrice)}
Discount: $currencySymbol ${"%.2f".format(sale.discount)}
----------------------------------------
TOTAL AMOUNT: $currencySymbol ${"%.2f".format(sale.amount)}
Payment Method: ${sale.paymentMethod}
----------------------------------------
Notes: ${sale.notes.ifBlank { "Thank you for buying from our nursery! Plant more trees." }}
========================================
    Visit Again! Happy Gardening!
========================================
        """.trimIndent()
    }

    fun shareContentAsFile(
        context: Context,
        content: String,
        fileName: String,
        mimeType: String,
        title: String
    ) {
        try {
            val cacheDir = File(context.cacheDir, "reports")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            val file = File(cacheDir, fileName)
            FileOutputStream(file).use { fos ->
                fos.write(content.toByteArray(Charsets.UTF_8))
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "$title - Exported from Sahnur Nursery Manager")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shareText(context: Context, text: String, title: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, text)
            }
            val chooser = Intent.createChooser(intent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
