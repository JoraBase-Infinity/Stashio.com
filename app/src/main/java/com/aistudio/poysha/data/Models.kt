package com.aistudio.poysha.data

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Shop(
    val id: String = "",
    val name: String = "",
    val type: String = "General Store",
    val address: String = "Dhaka, Bangladesh",
    val contact: String = "",
    val image: String = "",
    val footerNote: String = "Thank you for your business!",
    val userId: String = ""
)

@IgnoreExtraProperties
data class User(
    val phone: String = "",
    val name: String = "",
    val email: String = "",
    val pinHash: String = "",
    val uid: String = ""
)

@IgnoreExtraProperties
data class Product(
    val id: String = "",
    val name: String = "",
    val brand: String = "",
    val purchasePrice: Double = 0.0,
    val sellPrice: Double = 0.0,
    val stockQty: Int = 0,
    val unit: String = "pcs",
    val barcode: String = "",
    val minStockAlert: Int = 5,
    val shopId: String = "",
    val userId: String = ""
)

data class CartItem(
    val product: Product = Product(),
    var qty: Int = 1,
    var customRate: Double = product.sellPrice
) {
    val totalAmount: Double get() = qty * customRate
}

@IgnoreExtraProperties
data class SaleItem(
    val productId: String = "",
    val name: String = "",
    val brand: String = "",
    val qty: Int = 0,
    val unit: String = "pcs",
    val rate: Double = 0.0,
    val amount: Double = 0.0
)

@IgnoreExtraProperties
data class Sale(
    val id: String = "",
    val invoiceNo: String = "",
    val date: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val items: List<SaleItem> = emptyList(),
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val grandTotal: Double = 0.0,
    val paid: Double = 0.0,
    val due: Double = 0.0,
    val paymentMethod: String = "Cash",
    val shopId: String = "",
    val userId: String = ""
)

@IgnoreExtraProperties
data class PurchaseItem(
    val productId: String = "",
    val name: String = "",
    val brand: String = "",
    val qty: Int = 0,
    val unit: String = "pcs",
    val rate: Double = 0.0,
    val amount: Double = 0.0
)

@IgnoreExtraProperties
data class Purchase(
    val id: String = "",
    val billNo: String = "",
    val date: String = "",
    val supplierName: String = "",
    val supplierPhone: String = "",
    val items: List<PurchaseItem> = emptyList(),
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val grandTotal: Double = 0.0,
    val paid: Double = 0.0,
    val due: Double = 0.0,
    val paymentMethod: String = "Cash",
    val shopId: String = "",
    val userId: String = ""
)

@IgnoreExtraProperties
data class Contact(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val type: String = "Customer", // "Customer" or "Supplier"
    val dueAmount: Double = 0.0,
    val totalTransactions: Double = 0.0,
    val shopId: String = "",
    val userId: String = ""
)

@IgnoreExtraProperties
data class Expense(
    val id: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val category: String = "General",
    val date: String = "",
    val note: String = "",
    val paymentMethod: String = "Cash",
    val shopId: String = "",
    val userId: String = ""
)

@IgnoreExtraProperties
data class CashTransaction(
    val id: String = "",
    val type: String = "IN", // "IN" or "OUT"
    val amount: Double = 0.0,
    val reason: String = "",
    val date: String = "",
    val sourceOrTarget: String = "Counter",
    val shopId: String = "",
    val userId: String = ""
)

@IgnoreExtraProperties
data class Note(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val date: String = "",
    val shopId: String = "",
    val userId: String = ""
)
