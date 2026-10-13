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
fun BusinessReportScreen(
    onBack: () -> Unit
) {
    val sales by PosRepository.sales.collectAsState()
    val purchases by PosRepository.purchases.collectAsState()
    val expenses by PosRepository.expenses.collectAsState()
    val contacts by PosRepository.contacts.collectAsState()

    val totalSales = remember(sales) { sales.sumOf { it.grandTotal } }
    val totalPurchases = remember(purchases) { purchases.sumOf { it.grandTotal } }
    val totalExpenses = remember(expenses) { expenses.sumOf { it.amount } }

    // Gross profit estimated at 20% of sales or subtotal
    val grossProfit = totalSales * 0.20
    val netProfit = grossProfit - totalExpenses

    val totalRecv = remember(contacts) { contacts.filter { it.type.equals("Customer", true) }.sumOf { it.dueAmount } }
    val totalPay = remember(contacts) { contacts.filter { it.type.equals("Supplier", true) }.sumOf { it.dueAmount } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Business Report (P&L)", fontWeight = FontWeight.Bold, color = Color.White) },
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
            // Net Profit Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (netProfit >= 0) PoyshaGreen else PoyshaRed
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            if (netProfit >= 0) "NET PROFIT (আসল লাভ)" else "NET LOSS (লোকসান)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "৳${"%,.0f".format(netProfit)}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "(Gross Profit - Expenses)",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Grid Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ReportMetricCard(
                        title = "Total Sales",
                        amount = "৳${"%,.0f".format(totalSales)}",
                        subtitle = "মোট পণ্য বিক্রি",
                        tint = PoyshaPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    ReportMetricCard(
                        title = "Gross Profit",
                        amount = "৳${"%,.0f".format(grossProfit)}",
                        subtitle = "বিক্রিত পণ্যের লাভ",
                        tint = PoyshaGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ReportMetricCard(
                        title = "Total Expenses",
                        amount = "৳${"%,.0f".format(totalExpenses)}",
                        subtitle = "দোকানের খরচ",
                        tint = PoyshaRed,
                        modifier = Modifier.weight(1f)
                    )
                    ReportMetricCard(
                        title = "Total Purchase",
                        amount = "৳${"%,.0f".format(totalPurchases)}",
                        subtitle = "নতুন মাল কেনা",
                        tint = PoyshaBlue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Text("Due & Credit Status", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ReportMetricCard(
                        title = "Receivable (পাব)",
                        amount = "৳${"%,.0f".format(totalRecv)}",
                        subtitle = "কাস্টমারের বকেয়া",
                        tint = PoyshaGreen,
                        modifier = Modifier.weight(1f)
                    )
                    ReportMetricCard(
                        title = "Payable (দিব)",
                        amount = "৳${"%,.0f".format(totalPay)}",
                        subtitle = "সাপ্লায়ারের বকেয়া",
                        tint = PoyshaRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportMetricCard(
    title: String,
    amount: String,
    subtitle: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(title.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PoyshaTextLight)
            Spacer(modifier = Modifier.height(4.dp))
            Text(amount, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = tint)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 10.sp, color = PoyshaTextLight, fontWeight = FontWeight.Medium)
        }
    }
}
