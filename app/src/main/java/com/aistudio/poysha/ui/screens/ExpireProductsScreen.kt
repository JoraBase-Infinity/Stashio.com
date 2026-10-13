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
fun ExpireProductsScreen(
    onBack: () -> Unit
) {
    val products by PosRepository.products.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filtered = remember(products, searchQuery) {
        if (searchQuery.isBlank()) products
        else products.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.brand.contains(searchQuery, ignoreCase = true)
        }
    }

    val expiredCount = remember(products) { products.count { it.stockQty <= 0 } }
    val expiringSoonCount = remember(products) { products.count { it.stockQty in 1..it.minStockAlert } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expire Products Alert", fontWeight = FontWeight.Bold, color = Color.White) },
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
            // Summary Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("EXPIRED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PoyshaRed)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$expiredCount", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = PoyshaRed)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("EXPIRING SOON", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PoyshaOrange)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$expiringSoonCount", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = PoyshaOrange)
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name or brand...") },
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

            item {
                Text("Inventory Expiry List", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
            }

            items(filtered) { product ->
                val isLowStock = product.stockQty <= product.minStockAlert

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isLowStock) Color(0xFFFEF2F2) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
                            if (product.brand.isNotBlank()) {
                                Text(product.brand, fontSize = 11.sp, color = PoyshaPrimary, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    color = Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        "Stock: ${product.stockQty} ${product.unit}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PoyshaTextLight,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Surface(
                                    color = if (isLowStock) Color(0xFFFEE2E2) else Color(0xFFDCFCE3),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        if (isLowStock) "⚠️ Low Stock / Alert" else "✓ Fresh Stock",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLowStock) PoyshaRed else PoyshaGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        IconButton(onClick = { /* Quick view / edit */ }) {
                            Icon(Icons.Default.MoreVert, contentDescription = null, tint = PoyshaTextLight)
                        }
                    }
                }
            }
        }
    }
}
