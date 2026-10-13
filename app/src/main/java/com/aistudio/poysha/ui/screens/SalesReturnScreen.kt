package com.aistudio.poysha.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.poysha.data.PosRepository
import com.aistudio.poysha.data.Sale
import com.aistudio.poysha.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesReturnScreen(
    onBack: () -> Unit
) {
    val sales by PosRepository.sales.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedSale by remember { mutableStateOf<Sale?>(sales.firstOrNull()) }
    val returnQtys = remember { mutableStateMapOf<String, Int>() } // productId -> returnQty

    val filteredSales = remember(sales, searchQuery) {
        if (searchQuery.isBlank()) sales
        else sales.filter {
            it.invoiceNo.contains(searchQuery, ignoreCase = true) ||
            it.customerName.contains(searchQuery, ignoreCase = true)
        }
    }

    LaunchedEffect(selectedSale) {
        returnQtys.clear()
        selectedSale?.items?.forEach { item ->
            returnQtys[item.productId] = 0
        }
    }

    val totalRefund = remember(selectedSale, returnQtys.toMap()) {
        selectedSale?.items?.sumOf { item ->
            val qty = returnQtys[item.productId] ?: 0
            qty * item.rate
        } ?: 0.0
    }

    var showSuccessDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sales Return (বিক্রয় ফেরত)", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PoyshaRed)
            )
        },
        bottomBar = {
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
                        Text("REFUND AMOUNT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PoyshaTextLight)
                        Text("৳${"%,.0f".format(totalRefund)}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = PoyshaRed)
                    }
                    Button(
                        onClick = {
                            if (totalRefund > 0 && selectedSale != null) {
                                PosRepository.recordCashTransaction("OUT", totalRefund, "Sales Return: ${selectedSale?.invoiceNo}")
                                showSuccessDialog = true
                            }
                        },
                        enabled = totalRefund > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = PoyshaRed),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Confirm Return", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
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
            // Search Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("SEARCH INVOICE OR CUSTOMER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PoyshaTextLight)
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search customer, invoice no...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PoyshaTextLight) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Text("Recent Invoices:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PoyshaTextLight)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(filteredSales.take(10)) { sale ->
                                val isSelected = selectedSale?.id == sale.id
                                Surface(
                                    color = if (isSelected) Color(0xFFFEE2E2) else Color(0xFFF8FAFC),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) PoyshaRed else Color(0xFFE2E8F0)
                                    ),
                                    modifier = Modifier.clickable { selectedSale = sale }
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                                        Text(sale.invoiceNo, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) PoyshaRed else PoyshaTextDark, fontFamily = FontFamily.Monospace)
                                        Text(sale.customerName, fontSize = 10.sp, color = PoyshaTextLight)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Selected Invoice Summary
            selectedSale?.let { sale ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Customer Name:", fontSize = 12.sp, color = PoyshaTextLight)
                                Text(sale.customerName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Invoice No:", fontSize = 12.sp, color = PoyshaTextLight)
                                Text(sale.invoiceNo, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PoyshaRed, fontFamily = FontFamily.Monospace)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Sale Date:", fontSize = 12.sp, color = PoyshaTextLight)
                                Text(sale.date, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Original Bill:", fontSize = 12.sp, color = PoyshaTextLight)
                                Text("৳${"%,.0f".format(sale.grandTotal)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Paid / Due:", fontSize = 12.sp, color = PoyshaTextLight)
                                Text("Paid: ৳${"%,.0f".format(sale.paid)} | Due: ৳${"%,.0f".format(sale.due)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PoyshaGreen)
                            }
                        }
                    }
                }

                item {
                    Text("Select Return Quantities", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
                }

                items(sale.items) { item ->
                    val returnQty = returnQtys[item.productId] ?: 0

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
                                    Text("Sold: ${item.qty} ${item.unit} • Rate: ৳${"%,.0f".format(item.rate)}", fontSize = 11.sp, color = PoyshaTextLight)
                                }
                                Button(
                                    onClick = { returnQtys[item.productId] = item.qty },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2), contentColor = PoyshaRed),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Return All", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("ফেরতের পরিমাণ:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilledIconButton(
                                        onClick = {
                                            if (returnQty > 0) returnQtys[item.productId] = returnQty - 1
                                        },
                                        modifier = Modifier.size(32.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = PoyshaRed, modifier = Modifier.size(16.dp))
                                    }

                                    Text("$returnQty", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = PoyshaTextDark)

                                    FilledIconButton(
                                        onClick = {
                                            if (returnQty < item.qty) returnQtys[item.productId] = returnQty + 1
                                        },
                                        modifier = Modifier.size(32.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = PoyshaRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onBack()
            },
            title = { Text("বিক্রয় ফেরত সম্পন্ন হয়েছে!", fontWeight = FontWeight.Bold, color = PoyshaGreen) },
            text = { Text("পণ্য স্টকে পুনরায় যুক্ত হয়েছে এবং ক্যাশবক্স থেকে রিফান্ড সমন্বয় করা হয়েছে।") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PoyshaGreen)
                ) {
                    Text("ঠিক আছে")
                }
            }
        )
    }
}
