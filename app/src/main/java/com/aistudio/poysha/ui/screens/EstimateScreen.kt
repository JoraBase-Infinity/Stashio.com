package com.aistudio.poysha.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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

data class EstimateItem(
    val product: Product,
    var qty: Int = 1,
    var buyPrice: Double = product.purchasePrice,
    var sellPrice: Double = product.sellPrice
) {
    val totalBuy: Double get() = qty * buyPrice
    val totalSell: Double get() = qty * sellPrice
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstimateScreen(
    onBack: () -> Unit
) {
    val products by PosRepository.products.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val cart = remember { mutableStateMapOf<String, EstimateItem>() }

    val filteredProducts = remember(products, searchQuery) {
        if (searchQuery.isBlank()) products
        else products.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.brand.contains(searchQuery, ignoreCase = true)
        }
    }

    val totalBuy = cart.values.sumOf { it.totalBuy }
    val totalSell = cart.values.sumOf { it.totalSell }
    val estimatedProfit = totalSell - totalBuy

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estimate & Quotation", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PoyshaSecondary)
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
                        focusedBorderColor = PoyshaSecondary
                    )
                )
            }

            // Product selection quick list
            item {
                Text("Select Products", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PoyshaTextLight)
            }

            items(filteredProducts) { product ->
                val inCart = cart[product.id] != null
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = if (inCart) Color(0xFFFEF3C7) else Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
                            Text("Stock: ${product.stockQty} • Rate: ৳${product.sellPrice}", fontSize = 11.sp, color = PoyshaTextLight)
                        }
                        IconButton(
                            onClick = {
                                if (inCart) cart.remove(product.id)
                                else cart[product.id] = EstimateItem(product)
                            }
                        ) {
                            Icon(
                                if (inCart) Icons.Default.Check else Icons.Default.Add,
                                contentDescription = null,
                                tint = if (inCart) PoyshaSecondary else PoyshaPrimary
                            )
                        }
                    }
                }
            }

            // Estimate Cart Items
            if (cart.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Quotation Items", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
                }

                items(cart.values.toList()) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.product.name, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { cart.remove(item.product.id) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = PoyshaRed, modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = item.qty.toString(),
                                    onValueChange = { val q = it.toIntOrNull() ?: 1; cart[item.product.id] = item.copy(qty = q) },
                                    label = { Text("Qty") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = item.buyPrice.toString(),
                                    onValueChange = { val b = it.toDoubleOrNull() ?: 0.0; cart[item.product.id] = item.copy(buyPrice = b) },
                                    label = { Text("Buy Rate") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = item.sellPrice.toString(),
                                    onValueChange = { val s = it.toDoubleOrNull() ?: 0.0; cart[item.product.id] = item.copy(sellPrice = s) },
                                    label = { Text("Sell Rate") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Summary Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Market Cost (Buy):", fontSize = 13.sp, color = PoyshaRed)
                                Text("৳${"%,.0f".format(totalBuy)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PoyshaRed)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Client Bill (Sell):", fontSize = 13.sp, color = PoyshaTextDark)
                                Text("৳${"%,.0f".format(totalSell)}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Estimated Profit:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PoyshaGreen)
                                Text("৳${"%,.0f".format(estimatedProfit)}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = PoyshaGreen)
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            cart.clear()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PoyshaSecondary)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Print / Save Quotation PDF", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
