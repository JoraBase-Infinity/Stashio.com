package com.aistudio.poysha.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object PosRepository {

    private fun currentDateStr(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun formatDisplayDate(): String {
        return SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date())
    }

    fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // Auth & Shop State
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _shops = MutableStateFlow<List<Shop>>(emptyList())
    val shops: StateFlow<List<Shop>> = _shops.asStateFlow()

    private val _activeShop = MutableStateFlow<Shop?>(null)
    val activeShop: StateFlow<Shop?> = _activeShop.asStateFlow()

    // Products
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    // Contacts
    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    // Sales
    private val _sales = MutableStateFlow<List<Sale>>(emptyList())
    val sales: StateFlow<List<Sale>> = _sales.asStateFlow()

    // Purchases
    private val _purchases = MutableStateFlow<List<Purchase>>(emptyList())
    val purchases: StateFlow<List<Purchase>> = _purchases.asStateFlow()

    // Expenses
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    // Cash Transactions & Balance
    private val _cashTransactions = MutableStateFlow<List<CashTransaction>>(emptyList())
    val cashTransactions: StateFlow<List<CashTransaction>> = _cashTransactions.asStateFlow()

    // Notes
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    // Registered users list in memory
    private val _registeredUsers = mutableListOf<User>()

    // ----------------------------------------------------
    // Operations
    // ----------------------------------------------------

    fun login(phone: String, pin: String): Boolean {
        val existing = _registeredUsers.find { _registeredUsers -> _registeredUsers.phone == phone && _registeredUsers.pinHash == hashPin(pin) }
        return if (existing != null) {
            _currentUser.value = existing
            true
        } else {
            // First time login with this phone number auto-initializes the session
            val newUser = User(phone = phone, name = "Shop Owner", email = "", pinHash = hashPin(pin))
            _registeredUsers.add(newUser)
            _currentUser.value = newUser
            true
        }
    }

    fun loginWithFirebase(uid: String, emailOrName: String) {
        val user = User(phone = uid, name = emailOrName.ifBlank { "Cloud Owner" }, email = emailOrName, pinHash = "")
        _currentUser.value = user
        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("users").document(uid).collection("shops").get()
                .addOnSuccessListener { result ->
                    val cloudShops = result.documents.mapNotNull { doc ->
                        doc.toObject(Shop::class.java)
                    }
                    if (cloudShops.isNotEmpty()) {
                        _shops.value = cloudShops
                        if (_activeShop.value == null) {
                            _activeShop.value = cloudShops.first()
                        }
                    } else if (_shops.value.isEmpty()) {
                        val defaultShop = Shop(
                            id = "shop_" + UUID.randomUUID().toString().take(6),
                            name = "My Cloud Shop",
                            type = "General Store",
                            address = "Dhaka, Bangladesh",
                            contact = uid
                        )
                        _shops.value = listOf(defaultShop)
                        _activeShop.value = defaultShop
                        db.collection("users").document(uid).collection("shops").document(defaultShop.id).set(defaultShop)
                    }
                }
        } catch (e: Exception) {
            if (_shops.value.isEmpty()) {
                val defaultShop = Shop(
                    id = "shop_" + UUID.randomUUID().toString().take(6),
                    name = "My Shop",
                    type = "General Store",
                    address = "Dhaka, Bangladesh",
                    contact = uid
                )
                _shops.value = listOf(defaultShop)
                _activeShop.value = defaultShop
            }
        }
    }

    fun register(name: String, phone: String, email: String, pin: String) {
        val newUser = User(phone = phone, name = name, email = email, pinHash = hashPin(pin))
        _registeredUsers.removeAll { it.phone == phone }
        _registeredUsers.add(newUser)
        _currentUser.value = newUser
    }

    fun selectShop(shop: Shop) {
        _activeShop.value = shop
    }

    fun addShop(name: String, type: String, address: String, contact: String): Shop {
        val newShop = Shop(
            id = "shop_" + UUID.randomUUID().toString().take(6),
            name = name.ifBlank { "My Shop" },
            type = type.ifBlank { "General Store" },
            address = address.ifBlank { "Dhaka, Bangladesh" },
            contact = contact.ifBlank { _currentUser.value?.phone ?: "" }
        )
        _shops.value = _shops.value + newShop
        _activeShop.value = newShop
        return newShop
    }

    fun deleteShop(shopId: String) {
        _shops.value = _shops.value.filterNot { it.id == shopId }
        if (_activeShop.value?.id == shopId) {
            _activeShop.value = _shops.value.firstOrNull()
        }
    }

    fun logout() {
        _currentUser.value = null
        _activeShop.value = null
    }

    fun addProduct(name: String, brand: String, purchasePrice: Double, sellPrice: Double, stock: Int, unit: String, barcode: String) {
        val product = Product(
            id = "p_" + UUID.randomUUID().toString().take(6),
            name = name,
            brand = brand,
            purchasePrice = purchasePrice,
            sellPrice = sellPrice,
            stockQty = stock,
            unit = unit.ifBlank { "pcs" },
            barcode = barcode
        )
        _products.value = _products.value + product
    }

    fun updateProduct(product: Product) {
        _products.value = _products.value.map { if (it.id == product.id) product else it }
    }

    fun deleteProduct(productId: String) {
        _products.value = _products.value.filterNot { it.id == productId }
    }

    fun recordSale(
        customerName: String,
        customerPhone: String,
        cartItems: List<CartItem>,
        discount: Double,
        paidAmount: Double,
        paymentMethod: String
    ): Sale {
        val invNo = "INV-" + (10000 + (1..89999).random())
        val subtotal = cartItems.sumOf { it.totalAmount }
        val grandTotal = (subtotal - discount).coerceAtLeast(0.0)
        val paid = paidAmount.coerceAtMost(grandTotal)
        val due = (grandTotal - paid).coerceAtLeast(0.0)

        val saleItems = cartItems.map {
            SaleItem(
                productId = it.product.id,
                name = it.product.name,
                brand = it.product.brand,
                qty = it.qty,
                unit = it.product.unit,
                rate = it.customRate,
                amount = it.totalAmount
            )
        }

        val sale = Sale(
            id = "s_" + UUID.randomUUID().toString().take(6),
            invoiceNo = invNo,
            date = currentDateStr(),
            customerName = customerName.ifBlank { "Walk-in Customer" },
            customerPhone = customerPhone,
            items = saleItems,
            subtotal = subtotal,
            discount = discount,
            grandTotal = grandTotal,
            paid = paid,
            due = due,
            paymentMethod = paymentMethod
        )

        // Decrement product stock
        val currentProducts = _products.value.toMutableList()
        for (item in cartItems) {
            val idx = currentProducts.indexOfFirst { it.id == item.product.id }
            if (idx >= 0) {
                val p = currentProducts[idx]
                currentProducts[idx] = p.copy(stockQty = (p.stockQty - item.qty).coerceAtLeast(0))
            }
        }
        _products.value = currentProducts

        // Append to sales
        _sales.value = listOf(sale) + _sales.value

        // If paid, register cash in
        if (paid > 0) {
            recordCashTransaction("IN", paid, "Sale: $invNo ($paymentMethod)")
        }

        // If due, update customer contact due
        if (due > 0 && customerName.isNotBlank()) {
            val contactsList = _contacts.value.toMutableList()
            val cIdx = contactsList.indexOfFirst { it.name.equals(customerName, ignoreCase = true) }
            if (cIdx >= 0) {
                val c = contactsList[cIdx]
                contactsList[cIdx] = c.copy(
                    dueAmount = c.dueAmount + due,
                    totalTransactions = c.totalTransactions + grandTotal
                )
            } else {
                contactsList.add(
                    Contact(
                        id = "c_" + UUID.randomUUID().toString().take(6),
                        name = customerName,
                        phone = customerPhone,
                        type = "Customer",
                        dueAmount = due,
                        totalTransactions = grandTotal
                    )
                )
            }
            _contacts.value = contactsList
        }

        return sale
    }

    fun recordPurchase(
        supplierName: String,
        supplierPhone: String,
        productId: String,
        productName: String,
        qty: Int,
        rate: Double,
        paidAmount: Double,
        paymentMethod: String
    ): Purchase {
        val billNo = "BILL-" + (1000 + (1..8999).random())
        val grandTotal = qty * rate
        val paid = paidAmount.coerceAtMost(grandTotal)
        val due = (grandTotal - paid).coerceAtLeast(0.0)

        val purchaseItem = PurchaseItem(productId, productName, "", qty, "pcs", rate, grandTotal)
        val purchase = Purchase(
            id = "pu_" + UUID.randomUUID().toString().take(6),
            billNo = billNo,
            date = currentDateStr(),
            supplierName = supplierName.ifBlank { "Direct Supplier" },
            supplierPhone = supplierPhone,
            items = listOf(purchaseItem),
            subtotal = grandTotal,
            discount = 0.0,
            grandTotal = grandTotal,
            paid = paid,
            due = due,
            paymentMethod = paymentMethod
        )

        // Increment stock
        val currentProducts = _products.value.toMutableList()
        val idx = currentProducts.indexOfFirst { it.id == productId }
        if (idx >= 0) {
            val p = currentProducts[idx]
            currentProducts[idx] = p.copy(stockQty = p.stockQty + qty)
            _products.value = currentProducts
        }

        _purchases.value = listOf(purchase) + _purchases.value

        if (paid > 0) {
            recordCashTransaction("OUT", paid, "Purchase: $billNo")
        }

        if (due > 0 && supplierName.isNotBlank()) {
            val contactsList = _contacts.value.toMutableList()
            val cIdx = contactsList.indexOfFirst { it.name.equals(supplierName, ignoreCase = true) }
            if (cIdx >= 0) {
                val c = contactsList[cIdx]
                contactsList[cIdx] = c.copy(
                    dueAmount = c.dueAmount + due,
                    totalTransactions = c.totalTransactions + grandTotal
                )
            } else {
                contactsList.add(
                    Contact(
                        id = "c_" + UUID.randomUUID().toString().take(6),
                        name = supplierName,
                        phone = supplierPhone,
                        type = "Supplier",
                        dueAmount = due,
                        totalTransactions = grandTotal
                    )
                )
            }
            _contacts.value = contactsList
        }

        return purchase
    }

    fun addExpense(title: String, amount: Double, category: String, note: String) {
        val exp = Expense(
            id = "e_" + UUID.randomUUID().toString().take(6),
            title = title,
            amount = amount,
            category = category.ifBlank { "General" },
            date = currentDateStr(),
            note = note
        )
        _expenses.value = listOf(exp) + _expenses.value
        recordCashTransaction("OUT", amount, "Expense: $title")
    }

    fun recordCashTransaction(type: String, amount: Double, reason: String) {
        val tx = CashTransaction(
            id = "tx_" + UUID.randomUUID().toString().take(6),
            type = type,
            amount = amount,
            reason = reason,
            date = currentDateStr()
        )
        _cashTransactions.value = listOf(tx) + _cashTransactions.value
    }

    fun addContact(name: String, phone: String, email: String, address: String, type: String) {
        val contact = Contact(
            id = "c_" + UUID.randomUUID().toString().take(6),
            name = name,
            phone = phone,
            email = email,
            address = address,
            type = type
        )
        _contacts.value = _contacts.value + contact
    }

    fun settleDue(contactId: String, amountPaid: Double) {
        val list = _contacts.value.toMutableList()
        val idx = list.indexOfFirst { it.id == contactId }
        if (idx >= 0) {
            val contact = list[idx]
            val newDue = (contact.dueAmount - amountPaid).coerceAtLeast(0.0)
            list[idx] = contact.copy(dueAmount = newDue)
            _contacts.value = list

            val isCustomer = contact.type.equals("Customer", ignoreCase = true)
            if (isCustomer) {
                recordCashTransaction("IN", amountPaid, "Due Collection: ${contact.name}")
            } else {
                recordCashTransaction("OUT", amountPaid, "Due Payment to: ${contact.name}")
            }
        }
    }

    fun addNote(title: String, content: String) {
        val note = Note(
            id = "n_" + UUID.randomUUID().toString().take(6),
            title = title,
            content = content,
            date = formatDisplayDate()
        )
        _notes.value = listOf(note) + _notes.value
    }

    fun deleteNote(id: String) {
        _notes.value = _notes.value.filterNot { it.id == id }
    }

    // Calculations for Home Dashboard
    fun calculateCurrentBalance(): Double {
        var bal = 0.0
        for (tx in _cashTransactions.value) {
            if (tx.type == "IN") bal += tx.amount
            else bal -= tx.amount
        }
        return bal
    }

    fun calculateTodaySales(): Double {
        val today = currentDateStr()
        return _sales.value.filter { it.date == today }.sumOf { it.grandTotal }
    }

    fun calculateTodayExpenses(): Double {
        val today = currentDateStr()
        return _expenses.value.filter { it.date == today }.sumOf { it.amount }
    }

    fun calculateTotalDues(): Pair<Double, Double> {
        val receivable = _contacts.value.filter { it.type.equals("Customer", true) }.sumOf { it.dueAmount }
        val payable = _contacts.value.filter { it.type.equals("Supplier", true) }.sumOf { it.dueAmount }
        return Pair(receivable, payable)
    }

    fun calculateTotalStockQty(): Int {
        return _products.value.sumOf { it.stockQty }
    }

    // Number to words in Bangladeshi Taka (from invoice_kit.js)
    fun numberToWordsBDT(number: Double): String {
        var num = number.toLong()
        if (num <= 0) return "Zero Taka Only"

        val a = listOf(
            "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
            "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
            "Seventeen", "Eighteen", "Nineteen"
        )
        val b = listOf("", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety")

        fun two(n: Long): String {
            return if (n < 20) a[n.toInt()] else b[(n / 10).toInt()] + (if (n % 10 != 0L) " " + a[(n % 10).toInt()] else "")
        }

        fun three(n: Long): String {
            return (if (n >= 100) a[(n / 100).toInt()] + " Hundred " else "") + two(n % 100)
        }

        val crore = num / 10000000
        num %= 10000000
        val lakh = num / 100000
        num %= 100000
        val thousand = num / 1000
        num %= 1000
        val rest = num

        val sb = StringBuilder()
        if (crore > 0) sb.append(three(crore)).append(" Crore ")
        if (lakh > 0) sb.append(three(lakh)).append(" Lakh ")
        if (thousand > 0) sb.append(three(thousand)).append(" Thousand ")
        if (rest > 0) sb.append(three(rest))

        return sb.toString().trim() + " Taka Only"
    }
}
