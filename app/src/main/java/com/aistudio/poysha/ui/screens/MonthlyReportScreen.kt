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
import com.aistudio.poysha.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyReportScreen(
    onBack: () -> Unit
) {
    val sales by PosRepository.sales.collectAsState()
    val purchases by PosRepository.purchases.collectAsState()
    val products by PosRepository.products.collectAsState()

    val totalSales = remember(sales) { sales.sumOf { it.grandTotal } }
    val totalPurchases = remember(purchases) { purchases.sumOf { it.grandTotal } }
    val totalStockVal = remember(products) { products.sumOf { it.stockQty * it.sellPrice } }
    val estimatedProfit = totalSales * 0.20

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monthly Report Hub", fontWeight = FontWeight.Bold, color = Color.White) },
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
            // Metrics Summary Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("TOTAL PURCHASE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PoyshaTextLight)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("৳${"%,.0f".format(totalPurchases)}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = PoyshaBlue)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("TOTAL SALE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PoyshaTextLight)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("৳${"%,.0f".format(totalSales)}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = PoyshaGreen)
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("STOCK ASSET VALUE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PoyshaTextLight)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("৳${"%,.0f".format(totalStockVal)}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = PoyshaSecondary)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("ESTIMATED PROFIT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PoyshaTextLight)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("৳${"%,.0f".format(estimatedProfit)}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = PoyshaPurple)
                        }
                    }
                }
            }

            item {
                Text("Product Wise Monthly Breakdown", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
            }

            items(products) { product ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
                            if (product.brand.isNotBlank()) {
                                Text(product.brand, fontSize = 11.sp, color = PoyshaPrimary, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Stock: ${product.stockQty} ${product.unit} • Buy: ৳${product.purchasePrice} • Sell: ৳${product.sellPrice}", fontSize = 11.sp, color = PoyshaTextLight)
                        }
                        Text("৳${"%,.0f".format(product.stockQty * product.sellPrice)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PoyshaPrimary)
                    }
                }
            }
        }
    }
}
