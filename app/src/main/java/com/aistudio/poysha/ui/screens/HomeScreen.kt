package com.aistudio.poysha.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.poysha.data.PosRepository
import com.aistudio.poysha.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit
) {
    val activeShop by PosRepository.activeShop.collectAsState()
    val sales by PosRepository.sales.collectAsState()
    val purchases by PosRepository.purchases.collectAsState()
    val expenses by PosRepository.expenses.collectAsState()
    val cashTx by PosRepository.cashTransactions.collectAsState()
    val products by PosRepository.products.collectAsState()
    val contacts by PosRepository.contacts.collectAsState()

    val currentBal = remember(cashTx) { PosRepository.calculateCurrentBalance() }
    val todaySales = remember(sales) { PosRepository.calculateTodaySales() }
    val todayExpenses = remember(expenses) { PosRepository.calculateTodayExpenses() }
    val totalStock = remember(products) { PosRepository.calculateTotalStockQty() }
    val (recvDue, payDue) = remember(contacts) { PosRepository.calculateTotalDues() }

    val cal = Calendar.getInstance()
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Good Morning"
        hour < 17 -> "Good Afternoon"
        else -> "Good Evening"
    }

    val dayStr = SimpleDateFormat("dd", Locale.getDefault()).format(Date())
    val monthStr = SimpleDateFormat("MMM", Locale.getDefault()).format(Date()).uppercase()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PoyshaBg)
    ) {
        // Dynamic Curved Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 38.dp, bottomEnd = 38.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(PoyshaPrimaryDark, PoyshaPrimary, PoyshaPrimaryLight)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 26.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            greeting.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.85f),
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✨ ", fontSize = 18.sp, color = PoyshaGoldLight)
                            Text(
                                activeShop?.name ?: "Poysha POS",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = Color.White.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                "Your Awesome Business Space",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.95f),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Date Badge
                    Surface(
                        color = Color.White.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(dayStr, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text(monthStr, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.9f))
                        }
                    }
                }
            }
        }

        // Live Dashboard Stats Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-20.dp)),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Row 1
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(
                            title = "CURRENT BALANCE",
                            value = "৳${"%,.0f".format(currentBal)}",
                            valueColor = PoyshaGreen,
                            icon = Icons.Default.AccountBalanceWallet,
                            badgeColor = PoyshaGreenLight,
                            iconTint = PoyshaGreen,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigate("CASHBOX") }
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(50.dp)
                                .background(Color(0xFFEEF0FA))
                        )
                        StatItem(
                            title = "TODAY'S SALES",
                            value = "৳${"%,.0f".format(todaySales)}",
                            valueColor = PoyshaBlue,
                            icon = Icons.Default.TrendingUp,
                            badgeColor = PoyshaBlueLight,
                            iconTint = PoyshaBlue,
                            modifier = Modifier
                                .weight(1.3f)
                                .clickable { onNavigate("SALES_BOOK") }
                        )
                    }

                    HorizontalDivider(color = Color(0xFFEEF0FA), thickness = 1.dp)

                    // Row 2
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(
                            title = "TODAY'S EXP.",
                            value = "৳${"%,.0f".format(todayExpenses)}",
                            valueColor = PoyshaRed,
                            icon = Icons.Default.ReceiptLong,
                            badgeColor = PoyshaRedLight,
                            iconTint = PoyshaRed,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigate("EXPENSES_BOOK") }
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(50.dp)
                                .background(Color(0xFFEEF0FA))
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigate("DUE_BOOK") }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PoyshaPurpleLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = PoyshaPurple, modifier = Modifier.size(15.dp))
                            }
                            Text("TOTAL DUES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PoyshaTextLight)
                            Text("Recv: ৳${"%,.0f".format(recvDue)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PoyshaGreen)
                            Text("Pay: ৳${"%,.0f".format(payDue)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PoyshaRed)
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(50.dp)
                                .background(Color(0xFFEEF0FA))
                        )
                        StatItem(
                            title = "STOCK QTY",
                            value = "$totalStock items",
                            valueColor = PoyshaTeal,
                            icon = Icons.Default.Inventory2,
                            badgeColor = PoyshaTealLight,
                            iconTint = PoyshaTeal,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigate("STOCK_BOOK") }
                        )
                    }
                }
            }
        }

        // Action Grid: Large Purchase & Sale Buttons
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                BigActionButton(
                    title = "Purchase",
                    icon = Icons.Default.ShoppingCart,
                    cardBg = Brush.linearGradient(listOf(Color(0xFFEFF6FF), Color(0xFFDBEAFE))),
                    iconBg = Brush.linearGradient(listOf(Color(0xFF60A5FA), PoyshaBlue)),
                    borderColor = Color(0xFFCFE4FD),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate("PURCHASE") }
                )
                BigActionButton(
                    title = "Sale",
                    icon = Icons.Default.ShoppingBag,
                    cardBg = Brush.linearGradient(listOf(Color(0xFFFFF7ED), Color(0xFFFFEDD5))),
                    iconBg = Brush.linearGradient(listOf(Color(0xFFFBBF24), PoyshaOrange)),
                    borderColor = Color(0xFFFDE8C8),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate("SELL") }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Section 1: 📚 Ledger Books
        item {
            MenuSection(title = "📚 Ledger Books") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    GridIconItem("Purchase\nBook", Icons.Default.Assignment, PoyshaOrange, Color(0xFFFFF7ED)) { onNavigate("PURCHASE_BOOK") }
                    GridIconItem("Sales\nBook", Icons.Default.RequestQuote, PoyshaBlue, Color(0xFFEFF6FF)) { onNavigate("SALES_BOOK") }
                    GridIconItem("Due\nBook", Icons.Default.MenuBook, PoyshaPurple, Color(0xFFF5F3FF)) { onNavigate("DUE_BOOK") }
                    GridIconItem("Expense\nBook", Icons.Default.AccountBalanceWallet, PoyshaRed, Color(0xFFFEF2F2)) { onNavigate("EXPENSES_BOOK") }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Section 2: 💼 Business Management
        item {
            MenuSection(title = "💼 Business Management") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        GridIconItem("Contacts", Icons.Default.People, PoyshaTeal, Color(0xFFF0FDFA)) { onNavigate("CONTACTS") }
                        GridIconItem("Product\nList", Icons.Default.AllInbox, PoyshaOrange, Color(0xFFFFF7ED)) { onNavigate("PRODUCTS") }
                        GridIconItem("Estimate /\nQuotation", Icons.Default.Calculate, PoyshaOrange, Color(0xFFFFF7ED)) { onNavigate("ESTIMATE") }
                        GridIconItem("Sales\nOrder", Icons.Default.AddShoppingCart, PoyshaTeal, Color(0xFFF0FDFA)) { onNavigate("SALES_BOOK") }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        GridIconItem("Stock\nReport", Icons.Default.Inventory2, PoyshaGreen, Color(0xFFECFDF5)) { onNavigate("STOCK_BOOK") }
                        GridIconItem("Business\nReport", Icons.Default.TrendingUp, PoyshaBlue, Color(0xFFEFF6FF)) { onNavigate("REPORTS") }
                        GridIconItem("Monthly\nReport", Icons.Default.TableChart, PoyshaPurple, Color(0xFFF5F3FF)) { onNavigate("REPORTS") }
                        Spacer(modifier = Modifier.width(60.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Section 3: ⚙️ Utilities & Security
        item {
            MenuSection(title = "⚙️ Utilities & Security") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        GridIconItem("Cash Box", Icons.Default.PointOfSale, PoyshaGreen, Color(0xFFECFDF5)) { onNavigate("CASHBOX") }
                        GridIconItem("Sales\nReturn", Icons.Default.Undo, PoyshaRed, Color(0xFFFEF2F2)) { onNavigate("SALES_BOOK") }
                        GridIconItem("Purchase\nReturn", Icons.Default.Redo, PoyshaOrange, Color(0xFFFFF7ED)) { onNavigate("PURCHASE_BOOK") }
                        GridIconItem("Expire\nItems", Icons.Default.EventBusy, PoyshaRed, Color(0xFFFEF2F2)) { onNavigate("PRODUCTS") }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        GridIconItem("App Access", Icons.Default.Security, PoyshaPurple, Color(0xFFF5F3FF)) { onNavigate("SETTINGS") }
                        GridIconItem("My Notes", Icons.Default.StickyNote2, PoyshaBlue, Color(0xFFEFF6FF)) { onNavigate("NOTES") }
                        GridIconItem("Barcode\nGen", Icons.Default.QrCode, PoyshaPurple, Color(0xFFF5F3FF)) { onNavigate("PRODUCTS") }
                        GridIconItem("Printer", Icons.Default.Print, Color(0xFF374151), Color(0xFFF8FAFC)) { onNavigate("SETTINGS") }
                    }
                }
            }
            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String,
    valueColor: Color,
    icon: ImageVector,
    badgeColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(badgeColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(15.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PoyshaTextLight)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = valueColor)
    }
}

@Composable
private fun BigActionButton(
    title: String,
    icon: ImageVector,
    cardBg: Brush,
    iconBg: Brush,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBg)
                .padding(vertical = 18.dp, horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = PoyshaTextDark)
            }
        }
    }
}

@Composable
private fun MenuSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = PoyshaTextDark)
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun GridIconItem(
    label: String,
    icon: ImageVector,
    tint: Color,
    bgColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(68.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = PoyshaTextLight,
            textAlign = TextAlign.Center,
            lineHeight = 13.sp
        )
    }
}
