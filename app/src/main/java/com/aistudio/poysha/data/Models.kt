package com.aistudio.poysha.data

data class Shop(
    val id: String,
    val name: String,
    val type: String = "General Store",
    val address: String = "Dhaka, Bangladesh",
    val contact: String = "01700000000",
    val image: String = "",
    val footerNote: String = "Thank you for your business!"
)

data class User(
    val phone: String,
    val name: String,
    val email: String,
    val pinHash: String
)

data class Product(
    val id: String,
    val name: String,
    val brand: String = "",
    val purchasePrice: Double,
    val sellPrice: Double,
    val stockQty: Int,
    val unit: String = "pcs",
    val barcode: String = "",
    val minStockAlert: Int = 5
)

data class CartItem(
    val product: Product,
    var qty: Int = 1,
    var customRate: Double = product.sellPrice
) {
    val totalAmount: Double get() = qty * customRate
}

data class SaleItem(
    val productId: String,
    val name: String,
    val brand: String = "",
    val qty: Int,
    val unit: String = "pcs",
    val rate: Double,
    val amount: Double
)

data class Sale(
    val id: String,
    val invoiceNo: String,
    val date: String,
    val customerName: String,
    val customerPhone: String = "",
    val items: List<SaleItem>,
    val subtotal: Double,
    val discount: Double,
    val grandTotal: Double,
    val paid: Double,
    val due: Double,
    val paymentMethod: String = "Cash"
)

data class PurchaseItem(
    val productId: String,
    val name: String,
    val brand: String = "",
    val qty: Int,
    val unit: String = "pcs",
    val rate: Double,
    val amount: Double
)

data class Purchase(
    val id: String,
    val billNo: String,
    val date: String,
    val supplierName: String,
    val supplierPhone: String = "",
    val items: List<PurchaseItem>,
    val subtotal: Double,
    val discount: Double,
    val grandTotal: Double,
    val paid: Double,
    val due: Double,
    val paymentMethod: String = "Cash"
)

data class Contact(
    val id: String,
    val name: String,
    val phone: String,
    val email: String = "",
    val address: String = "",
    val type: String, // "Customer" or "Supplier"
    val dueAmount: Double = 0.0,
    val totalTransactions: Double = 0.0
)

data class Expense(
    val id: String,
    val title: String,
    val amount: Double,
    val category: String = "General",
    val date: String,
    val note: String = "",
    val paymentMethod: String = "Cash"
)

data class CashTransaction(
    val id: String,
    val type: String, // "IN" or "OUT"
    val amount: Double,
    val reason: String,
    val date: String,
    val sourceOrTarget: String = "Counter"
)

data class Note(
    val id: String,
    val title: String,
    val content: String,
    val date: String
)
