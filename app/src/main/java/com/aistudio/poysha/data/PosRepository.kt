package com.aistudio.poysha.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object PosRepository {
    private const val TAG = "PosRepository"

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    private val rtdb by lazy {
        try {
            FirebaseDatabase.getInstance().reference
        } catch (e: Exception) {
            Log.e(TAG, "RTDB init exception: ${e.message}")
            null
        }
    }

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

    // Active state holders
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _shops = MutableStateFlow<List<Shop>>(emptyList())
    val shops: StateFlow<List<Shop>> = _shops.asStateFlow()

    private val _activeShop = MutableStateFlow<Shop?>(null)
    val activeShop: StateFlow<Shop?> = _activeShop.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    private val _sales = MutableStateFlow<List<Sale>>(emptyList())
    val sales: StateFlow<List<Sale>> = _sales.asStateFlow()

    private val _purchases = MutableStateFlow<List<Purchase>>(emptyList())
    val purchases: StateFlow<List<Purchase>> = _purchases.asStateFlow()

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _cashTransactions = MutableStateFlow<List<CashTransaction>>(emptyList())
    val cashTransactions: StateFlow<List<CashTransaction>> = _cashTransactions.asStateFlow()

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    // Status and diagnostic messages
    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private val _syncStatus = MutableStateFlow<String>("Ready")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Firestore listener registrations
    private val activeListeners = mutableListOf<ListenerRegistration>()

    init {
        checkExistingAuth()
    }

    fun clearError() {
        _lastError.value = null
    }

    // Check if user is already signed in via Firebase Auth
    private fun checkExistingAuth() {
        val currentFirebaseUser = auth.currentUser
        if (currentFirebaseUser != null) {
            val uid = currentFirebaseUser.uid
            val email = currentFirebaseUser.email ?: ""
            val displayName = currentFirebaseUser.displayName ?: email.substringBefore("@").ifBlank { "User" }
            val phone = currentFirebaseUser.phoneNumber ?: ""
            
            val user = User(
                uid = uid,
                email = email,
                name = displayName,
                phone = phone
            )
            _currentUser.value = user
            loadUserData(uid)
        }
    }

    // ----------------------------------------------------
    // Real Firebase Authentication
    // ----------------------------------------------------

    fun signInWithEmailPassword(
        emailInput: String,
        passwordInput: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val email = emailInput.trim()
        val password = passwordInput.trim()

        if (email.isBlank() || password.isBlank()) {
            val msg = "Please enter both Email/Phone and Password!"
            _lastError.value = msg
            onResult(false, msg)
            return
        }

        _isLoading.value = true
        _syncStatus.value = "Authenticating with Firebase..."
        _lastError.value = null

        // If user entered a plain phone number without '@', check if they have a mapped email or format
        if (!email.contains("@")) {
            // Check Firestore users collection by phone
            firestore.collection("users")
                .whereEqualTo("phone", email)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val matchedDoc = querySnapshot.documents.firstOrNull()
                    val targetEmail = matchedDoc?.getString("email")
                    if (!targetEmail.isNullOrBlank()) {
                        executeFirebaseAuthSignIn(targetEmail, password, onResult)
                    } else {
                        // Attempt fallback email format (e.g., 017xxx@poysha.com) or inform user
                        executeFirebaseAuthSignIn("$email@poysha.pos", password) { success, err ->
                            if (!success) {
                                _isLoading.value = false
                                val detailedErr = "Phone number not registered. Please log in with your Firebase Email or Register a new account."
                                _lastError.value = detailedErr
                                onResult(false, detailedErr)
                            } else {
                                onResult(true, null)
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    // Directly try standard sign in with formatted email
                    executeFirebaseAuthSignIn("$email@poysha.pos", password, onResult)
                }
        } else {
            executeFirebaseAuthSignIn(email, password, onResult)
        }
    }

    private fun executeFirebaseAuthSignIn(
        email: String,
        pass: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    val fbUser = auth.currentUser
                    if (fbUser != null) {
                        val uid = fbUser.uid
                        val uEmail = fbUser.email ?: email
                        val uName = fbUser.displayName ?: uEmail.substringBefore("@")
                        val uPhone = fbUser.phoneNumber ?: ""

                        val user = User(
                            uid = uid,
                            email = uEmail,
                            name = uName,
                            phone = uPhone
                        )
                        _currentUser.value = user
                        _syncStatus.value = "Connected to Firebase ($uEmail)"
                        loadUserData(uid)
                        onResult(true, null)
                    } else {
                        val err = "Authentication succeeded but user profile was null."
                        _lastError.value = err
                        onResult(false, err)
                    }
                } else {
                    val ex = task.exception
                    val friendlyError = parseFirebaseError(ex)
                    _lastError.value = friendlyError
                    _syncStatus.value = "Login Failed"
                    Log.w(TAG, "Firebase login unsuccessful: ${ex?.message}")
                    onResult(false, friendlyError)
                }
            }
    }

    fun registerWithEmailPassword(
        name: String,
        email: String,
        phone: String,
        pass: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val trimmedEmail = email.trim()
        val trimmedPass = pass.trim()
        val trimmedName = name.trim()
        val trimmedPhone = phone.trim()

        if (trimmedEmail.isBlank() || trimmedPass.isBlank()) {
            val msg = "Email and Password cannot be empty!"
            _lastError.value = msg
            onResult(false, msg)
            return
        }

        if (trimmedPass.length < 6) {
            val msg = "Password must be at least 6 characters long!"
            _lastError.value = msg
            onResult(false, msg)
            return
        }

        _isLoading.value = true
        _syncStatus.value = "Creating Firebase Account..."
        _lastError.value = null

        auth.createUserWithEmailAndPassword(trimmedEmail, trimmedPass)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    val fbUser = auth.currentUser
                    if (fbUser != null) {
                        val uid = fbUser.uid
                        // Update display name
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(trimmedName.ifBlank { trimmedEmail.substringBefore("@") })
                            .build()
                        fbUser.updateProfile(profileUpdates)

                        val user = User(
                            uid = uid,
                            email = trimmedEmail,
                            name = trimmedName.ifBlank { "User" },
                            phone = trimmedPhone
                        )
                        _currentUser.value = user

                        // Save profile to Firestore and RTDB
                        val userMap = mapOf(
                            "uid" to uid,
                            "email" to trimmedEmail,
                            "name" to user.name,
                            "phone" to trimmedPhone,
                            "createdAt" to currentDateStr()
                        )
                        firestore.collection("users").document(uid).set(userMap)
                        rtdb?.child("users")?.child(uid)?.setValue(userMap)

                        _syncStatus.value = "Account created successfully"
                        loadUserData(uid)
                        onResult(true, null)
                    } else {
                        onResult(true, null)
                    }
                } else {
                    val ex = task.exception
                    val friendlyError = parseFirebaseError(ex)
                    _lastError.value = friendlyError
                    _syncStatus.value = "Registration Failed"
                    Log.w(TAG, "Firebase register unsuccessful: ${ex?.message}")
                    onResult(false, friendlyError)
                }
            }
    }

    fun sendPasswordReset(email: String, onResult: (Boolean, String?) -> Unit) {
        val trimmed = email.trim()
        if (trimmed.isBlank() || !trimmed.contains("@")) {
            val msg = "Please enter a valid Email address to reset password."
            _lastError.value = msg
            onResult(false, msg)
            return
        }

        _isLoading.value = true
        auth.sendPasswordResetEmail(trimmed)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    onResult(true, "Password reset email sent to $trimmed!")
                } else {
                    val err = parseFirebaseError(task.exception)
                    _lastError.value = err
                    onResult(false, err)
                }
            }
    }

    private fun parseFirebaseError(exception: Exception?): String {
        if (exception == null) return "Unknown error occurred"
        val msg = exception.localizedMessage ?: exception.message ?: ""
        return when {
            exception is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ||
            msg.contains("supplied auth credential is incorrect", true) ||
            msg.contains("malformed or has expired", true) ||
            msg.contains("password is invalid", true) ||
            msg.contains("auth/wrong-password", true) ||
            msg.contains("INVALID_LOGIN_CREDENTIALS", true) ||
            msg.contains("INVALID_CREDENTIAL", true) ->
                "ভুল ইমেইল বা পাসওয়ার্ড (Incorrect email or password)। আপনার Firebase একাউন্ট না থাকলে 'Create New Account' এ ট্যাপ করে একাউন্ট তৈরি করুন, অথবা পাসওয়ার্ড রিসেট করুন।"

            exception is com.google.firebase.auth.FirebaseAuthInvalidUserException ||
            msg.contains("There is no user record", true) ||
            msg.contains("auth/user-not-found", true) ||
            msg.contains("USER_NOT_FOUND", true) ->
                "এই ইমেইলে কোনো Firebase একাউন্ট পাওয়া যায়নি (No Firebase account found)। দয়া করে 'Create New Account' এ ক্লিক করে নতুন একাউন্ট তৈরি করুন।"

            exception is com.google.firebase.auth.FirebaseAuthUserCollisionException ||
            msg.contains("email address is already in use", true) ||
            msg.contains("auth/email-already-in-use", true) ||
            msg.contains("EMAIL_EXISTS", true) ->
                "এই ইমেইল দিয়ে ইতোমধ্যে একাউন্ট খোলা আছে (Email already registered)। সঠিক পাসওয়ার্ড দিয়ে লগইন করুন অথবা পাসওয়ার্ড রিসেট করুন।"

            exception is com.google.firebase.auth.FirebaseAuthWeakPasswordException ||
            msg.contains("Password should be at least", true) ||
            msg.contains("auth/weak-password", true) ||
            msg.contains("WEAK_PASSWORD", true) ->
                "পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে (Password must be 6+ characters)।"

            msg.contains("The email address is badly formatted", true) ||
            msg.contains("auth/invalid-email", true) ||
            msg.contains("INVALID_EMAIL", true) ->
                "ইমেইল ফরম্যাট সঠিক নয় (Invalid email address format)।"

            msg.contains("network error", true) ||
            msg.contains("network-request-failed", true) ->
                "ইন্টারনেট সংযোগ সমস্যা (Network connection failed)। ইন্টারনেট চেক করুন।"

            msg.contains("blocked all requests from this device", true) ||
            msg.contains("too-many-requests", true) ||
            msg.contains("TOO_MANY_ATTEMPTS", true) ->
                "অতিরিক্ত ভুল প্রচেষ্টার কারণে সাময়িক বন্ধ। কিছুক্ষণ পর চেষ্টা করুন (Too many attempts. Please try again later)।"

            else -> "Firebase Error: $msg"
        }
    }

    // ----------------------------------------------------
    // Real Cloud Data Syncing (Firestore & RTDB)
    // ----------------------------------------------------

    private fun detachListeners() {
        for (listener in activeListeners) {
            listener.remove()
        }
        activeListeners.clear()
    }

    fun loadUserData(uid: String) {
        detachListeners()
        _isLoading.value = true
        _syncStatus.value = "Loading cloud data..."

        // Listen for user's shops
        val shopsListener = firestore.collection("users").document(uid).collection("shops")
            .addSnapshotListener { snapshot, error ->
                _isLoading.value = false
                if (error != null) {
                    Log.e(TAG, "Error fetching shops: ${error.message}")
                    _lastError.value = "Failed to sync shops: ${error.localizedMessage}"
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val fetchedShops = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Shop::class.java)?.copy(id = doc.id)
                    }
                    _shops.value = fetchedShops
                    if (fetchedShops.isNotEmpty()) {
                        if (_activeShop.value == null || !fetchedShops.any { it.id == _activeShop.value?.id }) {
                            selectShop(fetchedShops.first())
                        }
                    } else {
                        checkAndInitializeUserShop(uid)
                    }
                }
            }
        activeListeners.add(shopsListener)

        // Also check if there's user profile in Firestore
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val name = doc.getString("name") ?: _currentUser.value?.name ?: "User"
                    val phone = doc.getString("phone") ?: _currentUser.value?.phone ?: ""
                    val email = doc.getString("email") ?: _currentUser.value?.email ?: ""
                    _currentUser.value = User(phone = phone, name = name, email = email, uid = uid)
                }
            }
    }

    private fun checkAndInitializeUserShop(uid: String) {
        firestore.collection("shops").whereEqualTo("userId", uid).get()
            .addOnSuccessListener { rootSnap ->
                val rootShops = rootSnap.documents.mapNotNull { it.toObject(Shop::class.java)?.copy(id = it.id) }
                if (rootShops.isNotEmpty()) {
                    _shops.value = rootShops
                    selectShop(rootShops.first())
                } else {
                    // Check RTDB
                    rtdb?.child("users")?.child(uid)?.child("shops")?.get()
                        ?.addOnSuccessListener { rtdbSnap ->
                            val rtdbShops = mutableListOf<Shop>()
                            for (c in rtdbSnap.children) {
                                c.getValue(Shop::class.java)?.let { rtdbShops.add(it.copy(id = c.key ?: it.id)) }
                            }
                            if (rtdbShops.isNotEmpty()) {
                                _shops.value = rtdbShops
                                selectShop(rtdbShops.first())
                            } else {
                                val ownerName = _currentUser.value?.name ?: "My"
                                addShop("$ownerName POS", "General Store", "Dhaka, Bangladesh", _currentUser.value?.phone ?: "")
                            }
                        }
                        ?.addOnFailureListener {
                            val ownerName = _currentUser.value?.name ?: "My"
                            addShop("$ownerName POS", "General Store", "Dhaka, Bangladesh", _currentUser.value?.phone ?: "")
                        } ?: run {
                            val ownerName = _currentUser.value?.name ?: "My"
                            addShop("$ownerName POS", "General Store", "Dhaka, Bangladesh", _currentUser.value?.phone ?: "")
                        }
                }
            }
            .addOnFailureListener {
                val ownerName = _currentUser.value?.name ?: "My"
                addShop("$ownerName POS", "General Store", "Dhaka, Bangladesh", _currentUser.value?.phone ?: "")
            }
    }

    fun selectShop(shop: Shop) {
        _activeShop.value = shop
        val uid = _currentUser.value?.uid ?: auth.currentUser?.uid ?: return
        attachShopDataListeners(uid, shop.id)
    }

    private fun attachShopDataListeners(uid: String, shopId: String) {
        // Detach existing data listeners (keep shop listener)
        val shopRef = firestore.collection("users").document(uid)
            .collection("shops").document(shopId)

        // 1. Products Listener
        val pListener = shopRef.collection("products")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Products sync error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { it.toObject(Product::class.java)?.copy(id = it.id, shopId = shopId) }
                    _products.value = items
                }
            }
        activeListeners.add(pListener)

        // 2. Sales Listener
        val sListener = shopRef.collection("sales")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Sales sync error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { it.toObject(Sale::class.java)?.copy(id = it.id, shopId = shopId) }
                    _sales.value = items
                }
            }
        activeListeners.add(sListener)

        // 3. Purchases Listener
        val puListener = shopRef.collection("purchases")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Purchases sync error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { it.toObject(Purchase::class.java)?.copy(id = it.id, shopId = shopId) }
                    _purchases.value = items
                }
            }
        activeListeners.add(puListener)

        // 4. Contacts Listener
        val cListener = shopRef.collection("contacts")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Contacts sync error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { it.toObject(Contact::class.java)?.copy(id = it.id, shopId = shopId) }
                    _contacts.value = items
                }
            }
        activeListeners.add(cListener)

        // 5. Expenses Listener
        val expListener = shopRef.collection("expenses")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Expenses sync error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { it.toObject(Expense::class.java)?.copy(id = it.id, shopId = shopId) }
                    _expenses.value = items
                }
            }
        activeListeners.add(expListener)

        // 6. Cash Transactions Listener
        val txListener = shopRef.collection("cashTransactions")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Transactions sync error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { it.toObject(CashTransaction::class.java)?.copy(id = it.id, shopId = shopId) }
                    _cashTransactions.value = items
                }
            }
        activeListeners.add(txListener)

        // 7. Notes Listener
        val nListener = shopRef.collection("notes")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Notes sync error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { it.toObject(Note::class.java)?.copy(id = it.id, shopId = shopId) }
                    _notes.value = items
                }
            }
        activeListeners.add(nListener)

        _syncStatus.value = "Synced with Firebase Cloud"
    }

    // ----------------------------------------------------
    // Real Data Writing Methods (Firestore & Realtime DB)
    // ----------------------------------------------------

    fun addShop(name: String, type: String, address: String, contact: String): Shop {
        val uid = _currentUser.value?.uid ?: auth.currentUser?.uid ?: "user_default"
        val shopId = "shop_" + UUID.randomUUID().toString().take(8)
        val newShop = Shop(
            id = shopId,
            name = name.ifBlank { "My Shop" },
            type = type.ifBlank { "General Store" },
            address = address.ifBlank { "Dhaka, Bangladesh" },
            contact = contact.ifBlank { _currentUser.value?.phone ?: "" },
            userId = uid
        )

        // Update local state immediately for responsive UI
        _shops.value = _shops.value + newShop
        _activeShop.value = newShop

        // Persist to Firestore
        firestore.collection("users").document(uid)
            .collection("shops").document(shopId)
            .set(newShop)
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to persist shop: ${e.message}")
                _lastError.value = "Shop save error: ${e.localizedMessage}"
            }

        // Persist to RTDB
        rtdb?.child("users")?.child(uid)?.child("shops")?.child(shopId)?.setValue(newShop)

        attachShopDataListeners(uid, shopId)
        return newShop
    }

    fun deleteShop(shopId: String) {
        val uid = _currentUser.value?.uid ?: auth.currentUser?.uid ?: return
        _shops.value = _shops.value.filterNot { it.id == shopId }
        if (_activeShop.value?.id == shopId) {
            _activeShop.value = _shops.value.firstOrNull()
        }

        firestore.collection("users").document(uid)
            .collection("shops").document(shopId)
            .delete()
        rtdb?.child("users")?.child(uid)?.child("shops")?.child(shopId)?.removeValue()
    }

    fun logout() {
        detachListeners()
        auth.signOut()
        _currentUser.value = null
        _activeShop.value = null
        _shops.value = emptyList()
        _products.value = emptyList()
        _sales.value = emptyList()
        _purchases.value = emptyList()
        _contacts.value = emptyList()
        _expenses.value = emptyList()
        _cashTransactions.value = emptyList()
        _notes.value = emptyList()
        _syncStatus.value = "Logged Out"
    }

    fun addProduct(name: String, brand: String, purchasePrice: Double, sellPrice: Double, stock: Int, unit: String, barcode: String) {
        val uid = _currentUser.value?.uid ?: auth.currentUser?.uid ?: return
        val shopId = _activeShop.value?.id ?: return
        val productId = "p_" + UUID.randomUUID().toString().take(8)

        val product = Product(
            id = productId,
            name = name,
            brand = brand,
            purchasePrice = purchasePrice,
            sellPrice = sellPrice,
            stockQty = stock,
            unit = unit.ifBlank { "pcs" },
            barcode = barcode,
            shopId = shopId,
            userId = uid
        )

        _products.value = _products.value + product

        firestore.collection("users").document(uid)
            .collection("shops").document(shopId)
            .collection("products").document(productId)
            .set(product)
            .addOnFailureListener { e ->
                _lastError.value = "Failed to save product: ${e.localizedMessage}"
            }
        rtdb?.child("users")?.child(uid)?.child("shops")?.child(shopId)?.child("products")?.child(productId)?.setValue(product)
    }

    fun updateProduct(product: Product) {
        val uid = _currentUser.value?.uid ?: auth.currentUser?.uid ?: return
        val shopId = _activeShop.value?.id ?: return

        _products.value = _products.value.map { if (it.id == product.id) product else it }

        firestore.collection("users").document(uid)
            .collection("shops").document(shopId)
            .collection("products").document(product.id)
            .set(product)
            .addOnFailureListener { e ->
                _lastError.value = "Failed to update product: ${e.localizedMessage}"
            }
        rtdb?.child("users")?.child(uid)?.child("shops")?.child(shopId)?.child("products")?.child(product.id)?.setValue(product)
    }

    fun deleteProduct(productId: String) {
        val uid = _currentUser.value?.uid ?: auth.currentUser?.uid ?: return
        val shopId = _activeShop.value?.id ?: return

        _products.value = _products.value.filterNot { it.id == productId }

        firestore.collection("users").document(uid)
            .collection("shops").document(shopId)
            .collection("products").document(productId)
            .delete()
        rtdb?.child("users")?.child(uid)?.child("shops")?.child(shopId)?.child("products")?.child(productId)?.removeValue()
    }

    fun recordSale(
        customerName: String,
        customerPhone: String,
        cartItems: List<CartItem>,
        discount: Double,
        paidAmount: Double,
        paymentMethod: String
    ): Sale {
        val uid = _currentUser.value?.uid ?: auth.currentUser?.uid ?: ""
        val shopId = _activeShop.value?.id ?: ""

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

        val saleId = "s_" + UUID.randomUUID().toString().take(8)
        val sale = Sale(
            id = saleId,
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
            paymentMethod = paymentMethod,
            shopId = shopId,
            userId = uid
        )

        // Decrement product stock locally & in Firestore
        val currentProducts = _products.value.toMutableList()
        for (item in cartItems) {
            val idx = currentProducts.indexOfFirst { it.id == item.product.id }
            if (idx >= 0) {
                val p = currentProducts[idx]
                val updated = p.copy(stockQty = (p.stockQty - item.qty).coerceAtLeast(0))
                currentProducts[idx] = updated
                updateProduct(updated)
            }
        }
        _products.value = currentProducts

        _sales.value = listOf(sale) + _sales.value

        if (uid.isNotBlank() && shopId.isNotBlank()) {
            firestore.collection("users").document(uid)
                .collection("shops").document(shopId)
                .collection("sales").document(saleId)
                .set(sale)
            rtdb?.child("users")?.child(uid)?.child("shops")?.child(shopId)?.child("sales")?.child(saleId)?.setValue(sale)
        }

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
                val updatedC = c.copy(
                    dueAmount = c.dueAmount + due,
                    totalTransactions = c.totalTransactions + grandTotal
                )
                contactsList[cIdx] = updatedC
                _contacts.value = contactsList
                if (uid.isNotBlank() && shopId.isNotBlank()) {
                    firestore.collection("users").document(uid)
                        .collection("shops").document(shopId)
                        .collection("contacts").document(updatedC.id).set(updatedC)
                }
            } else {
                val newContactId = "c_" + UUID.randomUUID().toString().take(8)
                val newContact = Contact(
                    id = newContactId,
                    name = customerName,
                    phone = customerPhone,
                    type = "Customer",
                    dueAmount = due,
                    totalTransactions = grandTotal,
                    shopId = shopId,
                    userId = uid
                )
                contactsList.add(newContact)
                _contacts.value = contactsList
                if (uid.isNotBlank() && shopId.isNotBlank()) {
                    firestore.collection("users").document(uid)
                        .collection("shops").document(shopId)
                        .collection("contacts").document(newContactId).set(newContact)
                }
            }
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
        val uid = _currentUser.value?.uid ?: auth.currentUser?.uid ?: ""
        val shopId = _activeShop.value?.id ?: ""

        val billNo = "BILL-" + (1000 + (1..8999).random())
        val grandTotal = qty * rate
        val paid = paidAmount.coerceAtMost(grandTotal)
        val due = (grandTotal - paid).coerceAtLeast(0.0)

        val purchaseItem = PurchaseItem(productId, productName, "", qty, "pcs", rate, grandTotal)
        val purchaseId = "pu_" + UUID.randomUUID().toString().take(8)
        val purchase = Purchase(
            id = purchaseId,
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
            paymentMethod = paymentMethod,
            shopId = shopId,
            userId = uid
        )

        // Increment stock
        val currentProducts = _products.value.toMutableList()
        val idx = currentProducts.indexOfFirst { it.id == productId }
        if (idx >= 0) {
            val p = currentProducts[idx]
            val updated = p.copy(stockQty = p.stockQty + qty)
            currentProducts[idx] = updated
            _products.value = currentProducts
            updateProduct(updated)
        }

        _purchases.value = listOf(purchase) + _purchases.value

        if (uid.isNotBlank() && shopId.isNotBlank()) {
            firestore.collection("users").document(uid)
                .collection("shops").document(shopId)
                .collection("purchases").document(purchaseId)
                .set(purchase)
            rtdb?.child("users")?.child(uid)?.child("shops")?.child(shopId)?.child("purchases")?.child(purchaseId)?.setValue(purchase)
        }

        if (paid > 0) {
            recordCashTransaction("OUT", paid, "Purchase: $billNo")
        }

        if (due > 0 && supplierName.isNotBlank()) {
            val contactsList = _contacts.value.toMutableList()
            val cIdx = contactsList.indexOfFirst { it.name.equals(supplierName, ignoreCase = true) }
            if (cIdx >= 0) {
                val c = contactsList[cIdx]
                val updatedC = c.copy(
                    dueAmount = c.dueAmount + due,
                    totalTransactions = c.totalTransactions + grandTotal
                )
                contactsList[cIdx] = updatedC
                _contacts.value = contactsList
                if (uid.isNotBlank() && shopId.isNotBlank()) {
                    firestore.collection("users").document(uid)
                        .collection("shops").document(shopId)
                        .collection("contacts").document(updatedC.id).set(updatedC)
                }
            } else {
                val newCId = "c_" + UUID.randomUUID().toString().take(8)
                val newC = Contact(
                    id = newCId,
                    name = supplierName,
                    phone = supplierPhone,
                    type = "Supplier",
                    dueAmount = due,
                    totalTransactions = grandTotal,
                    shopId = shopId,
                    userId = uid
                )
                contactsList.add(newC)
                _contacts.value = contactsList
                if (uid.isNotBlank() && shopId.isNotBlank()) {
                    firestore.collection("users").document(uid)
                        .collection("shops").document(shopId)
                        .collection("contacts").document(newCId).set(newC)
                }
            }
        }

        return purchase
    }

    fun addExpense(title: String, amount: Double, category: String, note: String) {
        val uid = _currentUser.value?.uid ?: auth.currentUser?.uid ?: ""
        val shopId = _activeShop.value?.id ?: ""
        val expId = "e_" + UUID.randomUUID().toString().take(8)

        val exp = Expense(
            id = expId,
            title = title,
            amount = amount,
            category = category.ifBlank { "General" },
            date = currentDateStr(),
            note = note,
            shopId = shopId,
            userId = uid
        )
        _expenses.value = listOf(exp) + _expenses.value

        if (uid.isNotBlank() && shopId.isNotBlank()) {
            firestore.collection("users").document(uid)
                .collection("shops").document(shopId)
                .collection("expenses").document(expId)
                .set(exp)
            rtdb?.child("users")?.child(uid)?.child("shops")?.child(shopId)?.child("expenses")?.child(expId)?.setValue(exp)
        }

        recordCashTransaction("OUT", amount, "Expense: $title")
    }

    fun recordCashTransaction(type: String, amount: Double, reason: String) {
        val uid = _currentUser.value?.uid ?: auth.currentUser?.uid ?: ""
        val shopId = _activeShop.value?.id ?: ""
        val txId = "tx_" + UUID.randomUUID().toString().take(8)

        val tx = CashTransaction(
            id = txId,
            type = type,
            amount = amount,
            reason = reason,
            date = currentDateStr(),
            shopId = shopId,
            userId = uid
        )
        _cashTransactions.value = listOf(tx) + _cashTransactions.value

        if (uid.isNotBlank() && shopId.isNotBlank()) {
            firestore.collection("users").document(uid)
                .collection("shops").document(shopId)
                .collection("cashTransactions").document(txId)
                .set(tx)
            rtdb?.child("users")?.child(uid)?.child("shops")?.child(shopId)?.child("cashTransactions")?.child(txId)?.setValue(tx)
        }
    }

    fun addContact(name: String, phone: String, email: String, address: String, type: String) {
        val uid = _currentUser.value?.uid ?: auth.currentUser?.uid ?: ""
        val shopId = _activeShop.value?.id ?: ""
        val contactId = "c_" + UUID.randomUUID().toString().take(8)

        val contact = Contact(
            id = contactId,
            name = name,
            phone = phone,
            email = email,
            address = address,
            type = type,
            shopId = shopId,
            userId = uid
        )
        _contacts.value = _contacts.value + contact

        if (uid.isNotBlank() && shopId.isNotBlank()) {
            firestore.collection("users").document(uid)
                .collection("shops").document(shopId)
                .collection("contacts").document(contactId)
                .set(contact)
            rtdb?.child("users")?.child(uid)?.child("shops")?.child(shopId)?.child("contacts")?.child(contactId)?.setValue(contact)
        }
    }

    fun settleDue(contactId: String, amountPaid: Double) {
        val uid = _currentUser.value?.uid ?: auth.currentUser?.uid ?: ""
        val shopId = _activeShop.value?.id ?: ""

        val list = _contacts.value.toMutableList()
        val idx = list.indexOfFirst { it.id == contactId }
        if (idx >= 0) {
            val contact = list[idx]
            val newDue = (contact.dueAmount - amountPaid).coerceAtLeast(0.0)
            val updated = contact.copy(dueAmount = newDue)
            list[idx] = updated
            _contacts.value = list

            if (uid.isNotBlank() && shopId.isNotBlank()) {
                firestore.collection("users").document(uid)
                    .collection("shops").document(shopId)
                    .collection("contacts").document(contactId)
                    .set(updated)
                rtdb?.child("users")?.child(uid)?.child("shops")?.child(shopId)?.child("contacts")?.child(contactId)?.setValue(updated)
            }

            val isCustomer = contact.type.equals("Customer", ignoreCase = true)
            if (isCustomer) {
                recordCashTransaction("IN", amountPaid, "Due Collection: ${contact.name}")
            } else {
                recordCashTransaction("OUT", amountPaid, "Due Payment to: ${contact.name}")
            }
        }
    }

    fun addNote(title: String, content: String) {
        val uid = _currentUser.value?.uid ?: auth.currentUser?.uid ?: ""
        val shopId = _activeShop.value?.id ?: ""
        val noteId = "n_" + UUID.randomUUID().toString().take(8)

        val note = Note(
            id = noteId,
            title = title,
            content = content,
            date = formatDisplayDate(),
            shopId = shopId,
            userId = uid
        )
        _notes.value = listOf(note) + _notes.value

        if (uid.isNotBlank() && shopId.isNotBlank()) {
            firestore.collection("users").document(uid)
                .collection("shops").document(shopId)
                .collection("notes").document(noteId)
                .set(note)
            rtdb?.child("users")?.child(uid)?.child("shops")?.child(shopId)?.child("notes")?.child(noteId)?.setValue(note)
        }
    }

    fun deleteNote(id: String) {
        val uid = _currentUser.value?.uid ?: auth.currentUser?.uid ?: return
        val shopId = _activeShop.value?.id ?: return

        _notes.value = _notes.value.filterNot { it.id == id }

        firestore.collection("users").document(uid)
            .collection("shops").document(shopId)
            .collection("notes").document(id)
            .delete()
        rtdb?.child("users")?.child(uid)?.child("shops")?.child(shopId)?.child("notes")?.child(id)?.removeValue()
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
