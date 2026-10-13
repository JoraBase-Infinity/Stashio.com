package com.aistudio.poysha.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
fun ReportsScreen(
    onBack: () -> Unit
) {
    val sales by PosRepository.sales.collectAsState()
    val purchases by PosRepository.purchases.collectAsState()
    val expenses by PosRepository.expenses.collectAsState()
    val products by PosRepository.products.collectAsState()

    val totalSales = remember(sales) { sales.sumOf { it.grandTotal } }
    val totalPurchases = remember(purchases) { purchases.sumOf { it.grandTotal } }
    val totalExpenses = remember(expenses) { expenses.sumOf { it.amount } }
    val netProfit = totalSales - totalPurchases - totalExpenses

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Business Reports", fontWeight = FontWeight.Bold, color = Color.White) },
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
            // Net Profit Hero Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "ESTIMATED NET PROFIT / MARGIN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PoyshaTextLight,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "${if (netProfit >= 0) "+" else ""}৳${"%,.0f".format(netProfit)}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (netProfit >= 0) PoyshaGreen else PoyshaRed
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Sales (৳${"%,.0f".format(totalSales)}) - Purchases (৳${"%,.0f".format(totalPurchases)}) - Exp. (৳${"%,.0f".format(totalExpenses)})",
                            fontSize = 11.sp,
                            color = PoyshaTextLight,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // Key Metrics
            item {
                Text("Financial Breakdown", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetricRow("Total Sales Revenue", "৳${"%,.0f".format(totalSales)}", PoyshaBlue)
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        MetricRow("Inventory Stock Purchases", "৳${"%,.0f".format(totalPurchases)}", PoyshaOrange)
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        MetricRow("Shop Operational Expenses", "৳${"%,.0f".format(totalExpenses)}", PoyshaRed)
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        MetricRow("Total Inventory Stock Value", "৳${"%,.0f".format(products.sumOf { it.stockQty * it.sellPrice })}", PoyshaTeal)
                    }
                }
            }

            // Overview Counts
            item {
                Text("Activity Counts", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CountBox("Sales Bills", "${sales.size}", PoyshaBlue, Modifier.weight(1f))
                    CountBox("Purchase Bills", "${purchases.size}", PoyshaOrange, Modifier.weight(1f))
                    CountBox("Expenses", "${expenses.size}", PoyshaRed, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = PoyshaTextDark, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun CountBox(label: String, count: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = PoyshaTextLight)
            Spacer(modifier = Modifier.height(4.dp))
            Text(count, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}
