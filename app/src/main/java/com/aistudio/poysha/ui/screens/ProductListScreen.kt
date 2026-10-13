package com.aistudio.poysha.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.aistudio.poysha.data.PosRepository
import com.aistudio.poysha.data.Product
import com.aistudio.poysha.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    onBack: () -> Unit
) {
    val products by PosRepository.products.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }

    // Form fields
    var pName by remember { mutableStateOf("") }
    var pBrand by remember { mutableStateOf("") }
    var pBuyPrice by remember { mutableStateOf("") }
    var pSellPrice by remember { mutableStateOf("") }
    var pStock by remember { mutableStateOf("") }
    var pUnit by remember { mutableStateOf("pcs") }
    var pBarcode by remember { mutableStateOf("") }

    val filtered = remember(products, searchQuery) {
        if (searchQuery.isBlank()) products
        else products.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.brand.contains(searchQuery, ignoreCase = true) ||
            it.barcode.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Inventory", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        pName = ""
                        pBrand = ""
                        pBuyPrice = ""
                        pSellPrice = ""
                        pStock = ""
                        pUnit = "pcs"
                        pBarcode = ""
                        editingProduct = null
                        showAddDialog = true
                    }) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Add Product", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PoyshaPrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    pName = ""
                    pBrand = ""
                    pBuyPrice = ""
                    pSellPrice = ""
                    pStock = ""
                    pUnit = "pcs"
                    pBarcode = ""
                    editingProduct = null
                    showAddDialog = true
                },
                containerColor = PoyshaSecondary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(PoyshaBg)
        ) {
            // Search Bar
            Surface(
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search product by name, brand, barcode...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PoyshaTextLight) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC),
                        focusedBorderColor = PoyshaPrimary,
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filtered.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Inventory2, contentDescription = null, tint = PoyshaPrimaryLight, modifier = Modifier.size(54.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No Products Found", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    if (searchQuery.isNotEmpty()) "No matching products found for \"$searchQuery\"."
                                    else "Your product inventory is empty. Tap below to add your first product.",
                                    fontSize = 12.sp,
                                    color = PoyshaTextLight,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        pName = ""; pBrand = ""; pBuyPrice = ""; pSellPrice = ""; pStock = ""; pUnit = "pcs"; pBarcode = ""
                                        editingProduct = null
                                        showAddDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PoyshaPrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add Product", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                items(filtered) { product ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
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
                                        .background(Color(0xFFF8FAFC)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = PoyshaPrimary)
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
                                                "Sell: ৳${"%,.0f".format(product.sellPrice)}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PoyshaPrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Surface(
                                            color = Color(0xFFDCFCE3),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                "Buy: ৳${"%,.0f".format(product.purchasePrice)}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PoyshaGreen,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Row {
                                IconButton(onClick = {
                                    editingProduct = product
                                    pName = product.name
                                    pBrand = product.brand
                                    pBuyPrice = product.purchasePrice.toString()
                                    pSellPrice = product.sellPrice.toString()
                                    pStock = product.stockQty.toString()
                                    pUnit = product.unit
                                    pBarcode = product.barcode
                                    showAddDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PoyshaPrimary, modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = {
                                    PosRepository.deleteProduct(product.id)
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = PoyshaRed, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    if (editingProduct == null) "Add New Product" else "Edit Product",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = pName,
                        onValueChange = { pName = it },
                        label = { Text("Product Name *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pBrand,
                        onValueChange = { pBrand = it },
                        label = { Text("Brand / Category") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = pBuyPrice,
                            onValueChange = { pBuyPrice = it },
                            label = { Text("Buy Price (৳)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = pSellPrice,
                            onValueChange = { pSellPrice = it },
                            label = { Text("Sell Price (৳)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = pStock,
                            onValueChange = { pStock = it },
                            label = { Text("Stock Qty") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = pUnit,
                            onValueChange = { pUnit = it },
                            label = { Text("Unit (pcs/kg)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = pBarcode,
                        onValueChange = { pBarcode = it },
                        label = { Text("Barcode (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val buy = pBuyPrice.toDoubleOrNull() ?: 0.0
                        val sell = pSellPrice.toDoubleOrNull() ?: 0.0
                        val stock = pStock.toIntOrNull() ?: 0

                        if (pName.isNotBlank()) {
                            if (editingProduct != null) {
                                PosRepository.updateProduct(
                                    editingProduct!!.copy(
                                        name = pName,
                                        brand = pBrand,
                                        purchasePrice = buy,
                                        sellPrice = sell,
                                        stockQty = stock,
                                        unit = pUnit,
                                        barcode = pBarcode
                                    )
                                )
                            } else {
                                PosRepository.addProduct(
                                    name = pName,
                                    brand = pBrand,
                                    purchasePrice = buy,
                                    sellPrice = sell,
                                    stock = stock,
                                    unit = pUnit,
                                    barcode = pBarcode
                                )
                            }
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PoyshaPrimary)
                ) {
                    Text(if (editingProduct == null) "SAVE PRODUCT" else "UPDATE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
