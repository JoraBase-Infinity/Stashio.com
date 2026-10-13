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
    private val _currentUser = MutableStateFlow<User?>(
        User(phone = "01712345678", name = "Fahim Maya", email = "fahim@poysha.com", pinHash = hashPin("123456"))
    )
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _shops = MutableStateFlow<List<Shop>>(
        listOf(
            Shop(id = "shop_1", name = "Poysha General Store", type = "Retail & Grocery", address = "Gulshan-1, Dhaka", contact = "01712345678"),
            Shop(id = "shop_2", name = "Poysha Fashion Hub", type = "Clothing & Apparel", address = "Dhanmondi, Dhaka", contact = "01812345678")
        )
    )
    val shops: StateFlow<List<Shop>> = _shops.asStateFlow()

    private val _activeShop = MutableStateFlow<Shop?>(_shops.value.first())
    val activeShop: StateFlow<Shop?> = _activeShop.asStateFlow()

    // Products
    private val _products = MutableStateFlow<List<Product>>(
        listOf(
            Product(id = "p1", name = "Miniket Premium Rice", brand = "Fresh", purchasePrice = 68.0, sellPrice = 76.0, stockQty = 45, unit = "kg", barcode = "89411001"),
            Product(id = "p2", name = "Soyabean Oil 5L", brand = "Teer", purchasePrice = 780.0, sellPrice = 850.0, stockQty = 18, unit = "can", barcode = "89411002"),
            Product(id = "p3", name = "Sugar White Pure", brand = "Deshi", purchasePrice = 125.0, sellPrice = 138.0, stockQty = 80, unit = "kg", barcode = "89411003"),
            Product(id = "p4", name = "Red Lentils (Mosur Dal)", brand = "Fresh", purchasePrice = 130.0, sellPrice = 145.0, stockQty = 35, unit = "kg", barcode = "89411004"),
            Product(id = "p5", name = "Aromatic Chinigura Rice", brand = "Pran", purchasePrice = 140.0, sellPrice = 160.0, stockQty = 25, unit = "kg", barcode = "89411005"),
            Product(id = "p6", name = "Tea Leaf Jar 400g", brand = "Ispahani", purchasePrice = 210.0, sellPrice = 240.0, stockQty = 3, unit = "jar", barcode = "89411006"), // low stock
            Product(id = "p7", name = "Biscuits Toast 300g", brand = "Olympic", purchasePrice = 45.0, sellPrice = 55.0, stockQty = 60, unit = "pkt", barcode = "89411007"),
            Product(id = "p8", name = "Milk Powder 500g", brand = "Dano", purchasePrice = 440.0, sellPrice = 490.0, stockQty = 12, unit = "pkt", barcode = "89411008")
        )
    )
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    // Contacts
    private val _contacts = MutableStateFlow<List<Contact>>(
        listOf(
            Contact(id = "c1", name = "Rahim Ahmed", phone = "01811223344", address = "Mirpur-10, Dhaka", type = "Customer", dueAmount = 450.0, totalTransactions = 5400.0),
            Contact(id = "c2", name = "Kamal Traders", phone = "01922334455", address = "Kaptan Bazar, Dhaka", type = "Supplier", dueAmount = 2200.0, totalTransactions = 38000.0),
            Contact(id = "c3", name = "Jamal Uddin", phone = "01733445566", address = "Badda, Dhaka", type = "Customer", dueAmount = 0.0, totalTransactions = 12000.0),
            Contact(id = "c4", name = "City Wholesale Agency", phone = "01644556677", address = "Moulvibazar, Dhaka", type = "Supplier", dueAmount = 1500.0, totalTransactions = 45000.0)
        )
    )
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    // Sales
    private val _sales = MutableStateFlow<List<Sale>>(
        listOf(
            Sale(
                id = "s1",
                invoiceNo = "INV-10023",
                date = currentDateStr(),
                customerName = "Rahim Ahmed",
                customerPhone = "01811223344",
                items = listOf(
                    SaleItem("p1", "Miniket Premium Rice", "Fresh", 5, "kg", 76.0, 380.0),
                    SaleItem("p2", "Soyabean Oil 5L", "Teer", 1, "can", 850.0, 850.0)
                ),
                subtotal = 1230.0,
                discount = 30.0,
                grandTotal = 1200.0,
                paid = 750.0,
                due = 450.0,
                paymentMethod = "Cash"
            )
        )
    )
    val sales: StateFlow<List<Sale>> = _sales.asStateFlow()

    // Purchases
    private val _purchases = MutableStateFlow<List<Purchase>>(
        listOf(
            Purchase(
                id = "pu1",
                billNo = "BILL-8041",
                date = currentDateStr(),
                supplierName = "Kamal Traders",
                supplierPhone = "01922334455",
                items = listOf(
                    PurchaseItem("p1", "Miniket Premium Rice", "Fresh", 50, "kg", 68.0, 3400.0)
                ),
                subtotal = 3400.0,
                discount = 0.0,
                grandTotal = 3400.0,
                paid = 1200.0,
                due = 2200.0,
                paymentMethod = "Bank"
            )
        )
    )
    val purchases: StateFlow<List<Purchase>> = _purchases.asStateFlow()

    // Expenses
    private val _expenses = MutableStateFlow<List<Expense>>(
        listOf(
            Expense(id = "e1", title = "Shop Electric Bill", amount = 1450.0, category = "Utility", date = currentDateStr(), note = "DESCO March"),
            Expense(id = "e2", title = "Tea & Staff Lunch", amount = 220.0, category = "Refreshment", date = currentDateStr(), note = "Daily counter expenses")
        )
    )
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    // Cash Transactions & Balance
    private val _cashTransactions = MutableStateFlow<List<CashTransaction>>(
        listOf(
            CashTransaction(id = "tx1", type = "IN", amount = 5000.0, reason = "Opening Counter Balance", date = currentDateStr()),
            CashTransaction(id = "tx2", type = "IN", amount = 750.0, reason = "Sale: INV-10023", date = currentDateStr()),
            CashTransaction(id = "tx3", type = "OUT", amount = 220.0, reason = "Expense: Tea & Staff Lunch", date = currentDateStr())
        )
    )
    val cashTransactions: StateFlow<List<CashTransaction>> = _cashTransactions.asStateFlow()

    // Notes
    private val _notes = MutableStateFlow<List<Note>>(
        listOf(
            Note("n1", "Supplier Order", "Call Kamal Traders for 10 bags of Miniket rice on Monday.", formatDisplayDate()),
            Note("n2", "Due Collection", "Follow up with Rahim Ahmed for remaining 450 Taka due.", formatDisplayDate())
        )
    )
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    // ----------------------------------------------------
    // Operations
    // ----------------------------------------------------

    fun login(phone: String, pin: String): Boolean {
        val user = _currentUser.value
        return if (user != null && user.phone == phone && user.pinHash == hashPin(pin)) {
            true
        } else {
            // Auto register/allow if test credential or set current
            val newUser = User(phone, "Shop Owner", "owner@poysha.com", hashPin(pin))
            _currentUser.value = newUser
            true
        }
    }

    fun register(name: String, phone: String, email: String, pin: String) {
        _currentUser.value = User(phone, name, email, hashPin(pin))
    }

    fun selectShop(shop: Shop) {
        _activeShop.value = shop
    }

    fun addShop(name: String, type: String, address: String, contact: String): Shop {
        val newShop = Shop(
            id = "shop_" + UUID.randomUUID().toString().take(6),
            name = name,
            type = type.ifBlank { "General Store" },
            address = address.ifBlank { "Dhaka, Bangladesh" },
            contact = contact.ifBlank { _currentUser.value?.phone ?: "01700000000" }
        )
        _shops.value = _shops.value + newShop
        _activeShop.value = newShop
        return newShop
    }

    fun logout() {
        // Just resets active shop selection, keeps data
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
