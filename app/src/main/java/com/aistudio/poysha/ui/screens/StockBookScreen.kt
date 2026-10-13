package com.aistudio.poysha.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.poysha.data.PosRepository
import com.aistudio.poysha.data.Product
import com.aistudio.poysha.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockBookScreen(
    onBack: () -> Unit
) {
    val products by PosRepository.products.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") } // all, low, out, value

    val totalAssetValue = remember(products) { products.sumOf { it.stockQty * it.purchasePrice } }
    val lowStockCount = remember(products) { products.count { it.stockQty <= it.minStockAlert } }

    val filteredProducts = remember(products, searchQuery, selectedFilter) {
        var list = if (searchQuery.isBlank()) products
        else products.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.brand.contains(searchQuery, ignoreCase = true) ||
            it.barcode.contains(searchQuery, ignoreCase = true)
        }

        when (selectedFilter) {
            "low" -> list.filter { it.stockQty <= it.minStockAlert }
            "out" -> list.filter { it.stockQty <= 0 }
            "value" -> list.sortedByDescending { it.stockQty * it.purchasePrice }
            else -> list.sortedBy { it.name }
        }
    }

    var viewingHistoryProduct by remember { mutableStateOf<Product?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stock Analytics & Ledger", fontWeight = FontWeight.Bold, color = Color.White) },
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
            // Asset Value Hero Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PoyshaGreen),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("TOTAL STOCK ASSET VALUE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.85f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("৳${"%,.0f".format(totalAssetValue)}", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }
                        Icon(Icons.Default.PieChart, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(44.dp))
                    }
                }
            }

            // Stat Cards Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("TOTAL ITEMS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PoyshaTextLight)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${products.size}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = PoyshaTextDark)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("LOW STOCK ALERT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PoyshaRed)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$lowStockCount", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = PoyshaRed)
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search product name, brand, barcode...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PoyshaTextLight) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = PoyshaPrimary
                    )
                )
            }

            // Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("all" to "All", "low" to "Low Stock", "out" to "Stock 0", "value" to "Highest Value").forEach { (key, label) ->
                        val isSelected = selectedFilter == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = key },
                            label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PoyshaPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Product List
            items(filteredProducts) { product ->
                val isLowStock = product.stockQty <= product.minStockAlert
                val assetVal = product.stockQty * product.purchasePrice

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isLowStock) Color(0xFFFEF2F2) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(product.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
                                if (product.brand.isNotBlank()) {
                                    Text(product.brand, fontSize = 11.sp, color = PoyshaPrimary, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Button(
                                onClick = { viewingHistoryProduct = product },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = PoyshaTextDark),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("History", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text("CURRENT STOCK", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PoyshaTextLight)
                                Text("${product.stockQty} ${product.unit}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = if (isLowStock) PoyshaRed else PoyshaTextDark)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("ASSET VALUE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PoyshaTextLight)
                                Text("৳${"%,.0f".format(assetVal)}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = PoyshaPrimary)
                            }
                        }
                    }
                }
            }
        }
    }

    viewingHistoryProduct?.let { product ->
        AlertDialog(
            onDismissRequest = { viewingHistoryProduct = null },
            title = { Text("Stock History: ${product.name}", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Current In-Stock: ${product.stockQty} ${product.unit}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PoyshaGreen)
                    Text("• Buy Rate: ৳${product.purchasePrice} | Sell Rate: ৳${product.sellPrice}", fontSize = 12.sp, color = PoyshaTextLight)
                    Text("• Minimum Alert Level: ${product.minStockAlert} ${product.unit}", fontSize = 12.sp, color = PoyshaTextLight)
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Text("Recent sales and purchases automatically adjust this product's inventory levels in real-time.", fontSize = 11.sp, color = PoyshaTextLight)
                }
            },
            confirmButton = {
                TextButton(onClick = { viewingHistoryProduct = null }) {
                    Text("Close")
                }
            }
        )
    }
}
