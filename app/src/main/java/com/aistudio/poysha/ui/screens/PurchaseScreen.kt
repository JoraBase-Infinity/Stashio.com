package com.aistudio.poysha.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.poysha.data.PosRepository
import com.aistudio.poysha.data.Product
import com.aistudio.poysha.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseScreen(
    onBack: () -> Unit
) {
    val products by PosRepository.products.collectAsState()
    val contacts by PosRepository.contacts.collectAsState()

    var supplierName by remember { mutableStateOf("") }
    var supplierPhone by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<Product?>(products.firstOrNull()) }
    var customProductName by remember { mutableStateOf("") }
    var buyQtyText by remember { mutableStateOf("10") }
    var buyRateText by remember { mutableStateOf(selectedProduct?.purchasePrice?.toString() ?: "100") }
    var paidAmountText by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Cash") }

    var expandedProductDropdown by remember { mutableStateOf(false) }
    var showSuccessSnackbar by remember { mutableStateOf(false) }

    val qty = buyQtyText.toIntOrNull() ?: 1
    val rate = buyRateText.toDoubleOrNull() ?: 0.0
    val totalBill = qty * rate

    LaunchedEffect(totalBill) {
        if (paidAmountText.isBlank() || paidAmountText == "0") {
            paidAmountText = totalBill.toInt().toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Purchase Stock", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PoyshaPrimary)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(PoyshaBg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Supplier Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Supplier Details", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = supplierName,
                            onValueChange = { supplierName = it },
                            label = { Text("Supplier Name") },
                            placeholder = { Text("e.g. Kamal Traders / City Wholesale") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = supplierPhone,
                            onValueChange = { supplierPhone = it },
                            label = { Text("Supplier Mobile Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Product Selection Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Select Product To Restock", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
                        Spacer(modifier = Modifier.height(10.dp))

                        ExposedDropdownMenuBox(
                            expanded = expandedProductDropdown,
                            onExpandedChange = { expandedProductDropdown = !expandedProductDropdown }
                        ) {
                            OutlinedTextField(
                                value = selectedProduct?.name ?: "Select from existing products",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProductDropdown) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = expandedProductDropdown,
                                onDismissRequest = { expandedProductDropdown = false }
                            ) {
                                products.forEach { p ->
                                    DropdownMenuItem(
                                        text = { Text("${p.name} (Current Stock: ${p.stockQty})") },
                                        onClick = {
                                            selectedProduct = p
                                            buyRateText = p.purchasePrice.toString()
                                            expandedProductDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = buyQtyText,
                                onValueChange = { buyQtyText = it },
                                label = { Text("Quantity") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = buyRateText,
                                onValueChange = { buyRateText = it },
                                label = { Text("Buy Rate (৳)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }

            // Payment & Confirm
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
                            Text("Total Purchase Cost", fontSize = 14.sp, color = PoyshaTextLight)
                            Text("৳${"%,.0f".format(totalBill)}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = PoyshaBlue)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Payment Method", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PoyshaTextLight)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Cash", "Bank", "bKash", "Due").forEach { method ->
                                val isSelected = paymentMethod == method
                                Surface(
                                    color = if (isSelected) PoyshaBlue else Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            paymentMethod = method
                                            if (method == "Due") paidAmountText = "0"
                                            else paidAmountText = totalBill.toInt().toString()
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

                        OutlinedTextField(
                            value = paidAmountText,
                            onValueChange = { paidAmountText = it },
                            label = { Text("Paid Amount (৳)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        val paid = paidAmountText.toDoubleOrNull() ?: 0.0
                        val due = (totalBill - paid).coerceAtLeast(0.0)
                        if (due > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Payable Due", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PoyshaRed)
                                Text("৳${"%,.0f".format(due)}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = PoyshaRed)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val p = selectedProduct
                                if (p != null) {
                                    PosRepository.recordPurchase(
                                        supplierName = supplierName.ifBlank { "Supplier" },
                                        supplierPhone = supplierPhone,
                                        productId = p.id,
                                        productName = p.name,
                                        qty = qty,
                                        rate = rate,
                                        paidAmount = paid,
                                        paymentMethod = paymentMethod
                                    )
                                    showSuccessSnackbar = true
                                    onBack()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PoyshaBlue)
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CONFIRM PURCHASE & ADD STOCK", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}
