package com.aistudio.poysha.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.poysha.data.*
import com.aistudio.poysha.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerBooksScreen(
    initialTab: String = "SALES", // SALES, PURCHASES, DUES, EXPENSES
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(initialTab) }

    val sales by PosRepository.sales.collectAsState()
    val purchases by PosRepository.purchases.collectAsState()
    val contacts by PosRepository.contacts.collectAsState()
    val expenses by PosRepository.expenses.collectAsState()

    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var expTitle by remember { mutableStateOf("") }
    var expAmount by remember { mutableStateOf("") }
    var expCategory by remember { mutableStateOf("Shop Maintenance") }
    var expNote by remember { mutableStateOf("") }

    var settlingContact by remember { mutableStateOf<Contact?>(null) }
    var settleAmountText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PoyshaPrimary)
            ) {
                TopAppBar(
                    title = {
                        Text(
                            when (selectedTab) {
                                "SALES" -> "Sales Book (Ledger)"
                                "PURCHASES" -> "Purchase Book (Ledger)"
                                "DUES" -> "Due Book (Customers & Suppliers)"
                                else -> "Expense Book"
                            },
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        if (selectedTab == "EXPENSES") {
                            IconButton(onClick = { showAddExpenseDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = "Add Expense", tint = Color.White)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = PoyshaPrimary)
                )

                // Tab Bar
                ScrollableTabRow(
                    selectedTabIndex = when (selectedTab) {
                        "SALES" -> 0
                        "PURCHASES" -> 1
                        "DUES" -> 2
                        else -> 3
                    },
                    containerColor = PoyshaPrimary,
                    contentColor = Color.White,
                    edgePadding = 12.dp,
                    indicator = {}
                ) {
                    val tabs = listOf("SALES" to "Sales Book", "PURCHASES" to "Purchase Book", "DUES" to "Due Book", "EXPENSES" to "Expenses")
                    tabs.forEach { (key, label) ->
                        val isSelected = selectedTab == key
                        Tab(
                            selected = isSelected,
                            onClick = { selectedTab = key },
                            text = {
                                Surface(
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) PoyshaPrimary else Color.White,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        )
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (selectedTab) {
                "SALES" -> {
                    // Summary Bar
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("TOTAL SALES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PoyshaTextLight)
                                    Text("৳${"%,.0f".format(sales.sumOf { it.grandTotal })}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = PoyshaBlue)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("TOTAL RECEIVED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PoyshaTextLight)
                                    Text("৳${"%,.0f".format(sales.sumOf { it.paid })}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = PoyshaGreen)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("TOTAL DUE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PoyshaTextLight)
                                    Text("৳${"%,.0f".format(sales.sumOf { it.due })}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = PoyshaRed)
                                }
                            }
                        }
                    }

                    items(sales) { sale ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column {
                                        Text(sale.customerName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
                                        Surface(
                                            color = Color(0xFFF1F5F9),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Text(
                                                sale.invoiceNo,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                color = PoyshaTextDark,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(sale.date, fontSize = 11.sp, color = PoyshaTextLight, fontWeight = FontWeight.Medium)
                                }

                                HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column {
                                        Text("${sale.items.size} item(s) • ${sale.paymentMethod}", fontSize = 11.sp, color = PoyshaTextLight)
                                        Text("Paid: ৳${"%,.0f".format(sale.paid)}", fontSize = 12.sp, color = PoyshaGreen, fontWeight = FontWeight.Bold)
                                        if (sale.due > 0) {
                                            Text("Due: ৳${"%,.0f".format(sale.due)}", fontSize = 12.sp, color = PoyshaRed, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text("৳${"%,.0f".format(sale.grandTotal)}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = PoyshaTextDark)
                                }
                            }
                        }
                    }
                }

                "PURCHASES" -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("TOTAL PURCHASE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PoyshaTextLight)
                                    Text("৳${"%,.0f".format(purchases.sumOf { it.grandTotal })}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = PoyshaBlue)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("TOTAL PAID", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PoyshaTextLight)
                                    Text("৳${"%,.0f".format(purchases.sumOf { it.paid })}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = PoyshaGreen)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("PAYABLE DUE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PoyshaTextLight)
                                    Text("৳${"%,.0f".format(purchases.sumOf { it.due })}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = PoyshaRed)
                                }
                            }
                        }
                    }

                    items(purchases) { purchase ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column {
                                        Text(purchase.supplierName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
                                        Text(purchase.billNo, fontSize = 11.sp, color = PoyshaPrimary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                    }
                                    Text(purchase.date, fontSize = 11.sp, color = PoyshaTextLight)
                                }

                                HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column {
                                        Text("${purchase.items.firstOrNull()?.name ?: "Stock"} (${purchase.paymentMethod})", fontSize = 11.sp, color = PoyshaTextLight)
                                        Text("Paid: ৳${"%,.0f".format(purchase.paid)}", fontSize = 12.sp, color = PoyshaGreen, fontWeight = FontWeight.Bold)
                                        if (purchase.due > 0) {
                                            Text("Due: ৳${"%,.0f".format(purchase.due)}", fontSize = 12.sp, color = PoyshaRed, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text("৳${"%,.0f".format(purchase.grandTotal)}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = PoyshaTextDark)
                                }
                            }
                        }
                    }
                }

                "DUES" -> {
                    val dueContacts = contacts.filter { it.dueAmount > 0 }

                    items(dueContacts) { contact ->
                        val isCustomer = contact.type.equals("Customer", true)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(contact.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
                                        Text("Phone: ${contact.phone}", fontSize = 11.sp, color = PoyshaTextLight)
                                    }
                                    Surface(
                                        color = if (isCustomer) Color(0xFFDCFCE3) else Color(0xFFFEE2E2),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            if (isCustomer) "TO RECEIVE" else "TO PAY",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isCustomer) PoyshaGreen else PoyshaRed,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Pending Due", fontSize = 11.sp, color = PoyshaTextLight)
                                        Text("৳${"%,.0f".format(contact.dueAmount)}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = if (isCustomer) PoyshaGreen else PoyshaRed)
                                    }

                                    Button(
                                        onClick = {
                                            settlingContact = contact
                                            settleAmountText = contact.dueAmount.toInt().toString()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = if (isCustomer) PoyshaGreen else PoyshaPrimary),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(if (isCustomer) "Collect Due" else "Pay Due", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                "EXPENSES" -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("TOTAL EXPENSES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PoyshaTextLight)
                                    Text("৳${"%,.0f".format(expenses.sumOf { it.amount })}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = PoyshaRed)
                                }
                                Button(
                                    onClick = { showAddExpenseDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = PoyshaRed),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Expense")
                                }
                            }
                        }
                    }

                    items(expenses) { exp ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(exp.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
                                    Text("${exp.category} • ${exp.date}", fontSize = 11.sp, color = PoyshaTextLight)
                                    if (exp.note.isNotBlank()) {
                                        Text(exp.note, fontSize = 10.sp, color = PoyshaTextLight)
                                    }
                                }
                                Text("৳${"%,.0f".format(exp.amount)}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = PoyshaRed)
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Expense Dialog
    if (showAddExpenseDialog) {
        AlertDialog(
            onDismissRequest = { showAddExpenseDialog = false },
            title = { Text("Record Expense", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = expTitle,
                        onValueChange = { expTitle = it },
                        label = { Text("Expense Title *") },
                        placeholder = { Text("e.g. Shop Rent / Electricity / Staff Tea") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = expAmount,
                        onValueChange = { expAmount = it },
                        label = { Text("Amount (৳) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = expCategory,
                        onValueChange = { expCategory = it },
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = expNote,
                        onValueChange = { expNote = it },
                        label = { Text("Note (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = expAmount.toDoubleOrNull() ?: 0.0
                        if (expTitle.isNotBlank() && amt > 0) {
                            PosRepository.addExpense(expTitle, amt, expCategory, expNote)
                            expTitle = ""
                            expAmount = ""
                            expNote = ""
                            showAddExpenseDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PoyshaRed)
                ) {
                    Text("SAVE EXPENSE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddExpenseDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Due Settle Dialog
    settlingContact?.let { contact ->
        AlertDialog(
            onDismissRequest = { settlingContact = null },
            title = { Text("Settle Due: ${contact.name}", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Current Due: ৳${"%,.0f".format(contact.dueAmount)}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = settleAmountText,
                        onValueChange = { settleAmountText = it },
                        label = { Text("Amount Paid/Collected (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = settleAmountText.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            PosRepository.settleDue(contact.id, amt)
                            settlingContact = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PoyshaGreen)
                ) {
                    Text("CONFIRM PAYMENT")
                }
            },
            dismissButton = {
                TextButton(onClick = { settlingContact = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
