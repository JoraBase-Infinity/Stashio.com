package com.aistudio.poysha.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.poysha.data.*
import com.aistudio.poysha.ui.theme.*

@Composable
fun SellScreen(
    onBack: () -> Unit
) {
    val products by PosRepository.products.collectAsState()
    val contacts by PosRepository.contacts.collectAsState()
    val activeShop by PosRepository.activeShop.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    val cart = remember { mutableStateMapOf<String, CartItem>() } // productId -> CartItem
    var isCheckingOut by remember { mutableStateOf(false) }

    // Checkout form state
    var selectedCustomer by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var discountText by remember { mutableStateOf("0") }
    var selectedPaymentMethod by remember { mutableStateOf("Cash") }
    var paidAmountText by remember { mutableStateOf("") }

    var completedSale by remember { mutableStateOf<Sale?>(null) }

    val filteredProducts = remember(products, searchQuery) {
        if (searchQuery.isBlank()) products
        else products.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.brand.contains(searchQuery, ignoreCase = true) ||
            it.barcode.contains(searchQuery, ignoreCase = true)
        }
    }

    val totalItemsInCart = cart.values.sumOf { it.qty }
    val subtotal = cart.values.sumOf { it.totalAmount }
    val discount = discountText.toDoubleOrNull() ?: 0.0
    val grandTotal = (subtotal - discount).coerceAtLeast(0.0)

    LaunchedEffect(grandTotal) {
        if (paidAmountText.isBlank() || paidAmountText == "0") {
            paidAmountText = grandTotal.toInt().toString()
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PoyshaPrimary)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(onClick = {
                        if (isCheckingOut) isCheckingOut = false
                        else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text(
                        if (isCheckingOut) "Checkout Details" else "Point of Sale (Sell)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (!isCheckingOut) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by name, brand or barcode...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PoyshaTextLight) },
                        trailingIcon = {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan", tint = PoyshaPrimary)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = PoyshaSecondary,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        bottomBar = {
            if (!isCheckingOut && totalItemsInCart > 0) {
                Surface(
                    color = Color.White,
                    shadowElevation = 12.dp,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Items in Cart", fontSize = 11.sp, color = PoyshaTextLight, fontWeight = FontWeight.SemiBold)
                            Text("$totalItemsInCart Items • ৳${"%,.0f".format(subtotal)}", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = PoyshaPrimary)
                        }
                        Button(
                            onClick = {
                                paidAmountText = grandTotal.toInt().toString()
                                isCheckingOut = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PoyshaPrimary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("Proceed", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (!isCheckingOut) {
            // Product selection list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(PoyshaBg)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredProducts) { product ->
                    val cartItem = cart[product.id]
                    val inCart = cartItem != null && cartItem.qty > 0

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (inCart) 2.dp else 0.dp,
                                color = if (inCart) PoyshaPrimary else Color.Transparent,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = if (inCart) Color(0xFFF0FDF4) else Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF1F5F9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = PoyshaPrimary, modifier = Modifier.size(24.dp))
                                }
                                Column {
                                    Text(product.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
                                    if (product.brand.isNotBlank()) {
                                        Text(product.brand, fontSize = 11.sp, color = PoyshaPrimary, fontWeight = FontWeight.SemiBold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Surface(
                                            color = if (product.stockQty <= product.minStockAlert) Color(0xFFFEE2E2) else Color(0xFFF1F5F9),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                "Stock: ${product.stockQty} ${product.unit}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (product.stockQty <= product.minStockAlert) PoyshaRed else PoyshaTextLight,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Surface(
                                            color = Color(0xFFE0E7FF),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                "৳${"%,.0f".format(product.sellPrice)}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PoyshaPrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Qty controls
                            if (inCart) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilledIconButton(
                                        onClick = {
                                            val current = cartItem!!.qty
                                            if (current <= 1) cart.remove(product.id)
                                            else cart[product.id] = cartItem.copy(qty = current - 1)
                                        },
                                        modifier = Modifier.size(32.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFFE2E8F0))
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = PoyshaTextDark, modifier = Modifier.size(16.dp))
                                    }

                                    Text("${cartItem?.qty}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)

                                    FilledIconButton(
                                        onClick = {
                                            cart[product.id] = cartItem!!.copy(qty = cartItem.qty + 1)
                                        },
                                        modifier = Modifier.size(32.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = PoyshaPrimary)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            } else {
                                IconButton(
                                    onClick = {
                                        cart[product.id] = CartItem(product = product, qty = 1, customRate = product.sellPrice)
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFF1F5F9))
                                ) {
                                    Icon(Icons.Default.AddShoppingCart, contentDescription = "Add to Cart", tint = PoyshaPrimary)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Checkout Form View
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(PoyshaBg)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Customer Details Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Customer Information", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = selectedCustomer,
                                onValueChange = { selectedCustomer = it },
                                label = { Text("Customer Name") },
                                placeholder = { Text("e.g. Rahim Ahmed or Walk-in") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = customerPhone,
                                onValueChange = { customerPhone = it },
                                label = { Text("Customer Mobile Number") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                // Cart Items List
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("ITEM & QTY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PoyshaPrimary)
                                Text("RATE & AMOUNT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PoyshaPrimary)
                            }
                            HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 8.dp))

                            cart.values.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1.5f)) {
                                        Text(item.product.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = PoyshaTextDark)
                                        Text("${item.qty} ${item.product.unit} × ৳${item.customRate}", fontSize = 11.sp, color = PoyshaTextLight)
                                    }
                                    Text("৳${"%,.0f".format(item.totalAmount)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
                                }
                            }
                        }
                    }
                }

                // Billing Summary & Payment
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Subtotal", fontSize = 13.sp, color = PoyshaTextLight)
                                Text("৳${"%,.0f".format(subtotal)}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Discount (৳)", fontSize = 13.sp, color = PoyshaTextLight)
                                OutlinedTextField(
                                    value = discountText,
                                    onValueChange = { discountText = it },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.width(110.dp),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Grand Total", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
                                Text("৳${"%,.0f".format(grandTotal)}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = PoyshaPrimary)
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Text("Payment Method", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PoyshaTextLight)
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Cash", "bKash", "Nagad", "Due").forEach { method ->
                                    val isSelected = selectedPaymentMethod == method
                                    Surface(
                                        color = if (isSelected) PoyshaPrimary else Color(0xFFF1F5F9),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                selectedPaymentMethod = method
                                                if (method == "Due") paidAmountText = "0"
                                                else paidAmountText = grandTotal.toInt().toString()
                                            }
                                    ) {
                                        Text(
                                            method,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else PoyshaTextDark,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 10.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Paid Amount (৳)", fontSize = 13.sp, color = PoyshaTextLight)
                                OutlinedTextField(
                                    value = paidAmountText,
                                    onValueChange = { paidAmountText = it },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.width(130.dp),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            val paid = paidAmountText.toDoubleOrNull() ?: 0.0
                            val due = (grandTotal - paid).coerceAtLeast(0.0)
                            if (due > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Due Amount", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PoyshaRed)
                                    Text("৳${"%,.0f".format(due)}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = PoyshaRed)
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    val sale = PosRepository.recordSale(
                                        customerName = selectedCustomer,
                                        customerPhone = customerPhone,
                                        cartItems = cart.values.toList(),
                                        discount = discount,
                                        paidAmount = paid,
                                        paymentMethod = selectedPaymentMethod
                                    )
                                    completedSale = sale
                                    cart.clear()
                                    isCheckingOut = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PoyshaGreen)
                            ) {
                                Text("CONFIRM & PRINT SALE", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    completedSale?.let { sale ->
        activeShop?.let { shop ->
            InvoicePreviewDialog(
                sale = sale,
                shop = shop,
                onDismiss = {
                    completedSale = null
                    onBack()
                }
            )
        }
    }
}
