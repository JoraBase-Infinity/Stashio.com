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
import com.aistudio.poysha.data.Purchase
import com.aistudio.poysha.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseReturnScreen(
    onBack: () -> Unit
) {
    val purchases by PosRepository.purchases.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedPurchase by remember { mutableStateOf<Purchase?>(purchases.firstOrNull()) }
    val returnQtys = remember { mutableStateMapOf<String, Int>() }

    val filteredPurchases = remember(purchases, searchQuery) {
        if (searchQuery.isBlank()) purchases
        else purchases.filter {
            it.billNo.contains(searchQuery, ignoreCase = true) ||
            it.supplierName.contains(searchQuery, ignoreCase = true)
        }
    }

    LaunchedEffect(selectedPurchase) {
        returnQtys.clear()
        selectedPurchase?.items?.forEach { item ->
            returnQtys[item.productId] = 0
        }
    }

    val totalReceivable = remember(selectedPurchase, returnQtys.toMap()) {
        selectedPurchase?.items?.sumOf { item ->
            val qty = returnQtys[item.productId] ?: 0
            qty * item.rate
        } ?: 0.0
    }

    var showSuccessDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Purchase Return (ক্রয় ফেরত)", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PoyshaOrange)
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
                        Text("RECEIVABLE AMOUNT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PoyshaTextLight)
                        Text("৳${"%,.0f".format(totalReceivable)}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = PoyshaOrange)
                    }
                    Button(
                        onClick = {
                            if (totalReceivable > 0 && selectedPurchase != null) {
                                PosRepository.recordCashTransaction("IN", totalReceivable, "Purchase Return Refund: ${selectedPurchase?.billNo}")
                                showSuccessDialog = true
                            }
                        },
                        enabled = totalReceivable > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = PoyshaOrange),
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
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("SEARCH CHALLAN OR SUPPLIER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PoyshaTextLight)
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search supplier or challan no...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PoyshaTextLight) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Text("Recent Challans:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PoyshaTextLight)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(filteredPurchases.take(10)) { purchase ->
                                val isSelected = selectedPurchase?.id == purchase.id
                                Surface(
                                    color = if (isSelected) Color(0xFFFFEDD5) else Color(0xFFF8FAFC),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) PoyshaOrange else Color(0xFFE2E8F0)
                                    ),
                                    modifier = Modifier.clickable { selectedPurchase = purchase }
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                                        Text(purchase.billNo, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) PoyshaOrange else PoyshaTextDark, fontFamily = FontFamily.Monospace)
                                        Text(purchase.supplierName, fontSize = 10.sp, color = PoyshaTextLight)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            selectedPurchase?.let { purchase ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFED7AA))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Supplier:", fontSize = 12.sp, color = PoyshaTextLight)
                                Text(purchase.supplierName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Challan / Bill No:", fontSize = 12.sp, color = PoyshaTextLight)
                                Text(purchase.billNo, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PoyshaOrange, fontFamily = FontFamily.Monospace)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Purchase Date:", fontSize = 12.sp, color = PoyshaTextLight)
                                Text(purchase.date, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Original Bill:", fontSize = 12.sp, color = PoyshaTextLight)
                                Text("৳${"%,.0f".format(purchase.grandTotal)}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item {
                    Text("Select Return Quantities", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
                }

                items(purchase.items) { item ->
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
                                    Text("Bought: ${item.qty} ${item.unit} • Rate: ৳${"%,.0f".format(item.rate)}", fontSize = 11.sp, color = PoyshaTextLight)
                                }
                                Button(
                                    onClick = { returnQtys[item.productId] = item.qty },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEDD5), contentColor = PoyshaOrange),
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
                                    .background(Color(0xFFFFF7ED), RoundedCornerShape(10.dp))
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
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = PoyshaOrange, modifier = Modifier.size(16.dp))
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
                                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = PoyshaOrange, modifier = Modifier.size(16.dp))
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
            title = { Text("ক্রয় ফেরত সফল হয়েছে!", fontWeight = FontWeight.Bold, color = PoyshaGreen) },
            text = { Text("পণ্য স্টক থেকে কমানো হয়েছে এবং ক্রয় খাতা সমন্বয় করা হয়েছে।") },
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
