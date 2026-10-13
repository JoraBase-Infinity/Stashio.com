package com.aistudio.poysha.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
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
import com.aistudio.poysha.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashboxScreen(
    onBack: () -> Unit
) {
    val txList by PosRepository.cashTransactions.collectAsState()
    val balance = remember(txList) { PosRepository.calculateCurrentBalance() }

    var showDialogType by remember { mutableStateOf<String?>(null) } // "IN" or "OUT"
    var txAmount by remember { mutableStateOf("") }
    var txReason by remember { mutableStateOf("") }

    val totalIn = remember(txList) { txList.filter { it.type == "IN" }.sumOf { it.amount } }
    val totalOut = remember(txList) { txList.filter { it.type == "OUT" }.sumOf { it.amount } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cash Box (Counter Cash)", fontWeight = FontWeight.Bold, color = Color.White) },
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
            // Balance Card
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
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "CURRENT CASH IN HAND",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PoyshaTextLight,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "৳${"%,.0f".format(balance)}",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (balance >= 0) PoyshaGreen else PoyshaRed
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    txAmount = ""
                                    txReason = ""
                                    showDialogType = "IN"
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDCFCE3), contentColor = PoyshaGreen)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Cash (In)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Button(
                                onClick = {
                                    txAmount = ""
                                    txReason = ""
                                    showDialogType = "OUT"
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2), contentColor = PoyshaRed)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Withdraw (Out)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Summary Bar
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
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("TOTAL CASH IN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PoyshaTextLight)
                            Text("৳${"%,.0f".format(totalIn)}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = PoyshaGreen)
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
                            Text("TOTAL CASH OUT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PoyshaTextLight)
                            Text("৳${"%,.0f".format(totalOut)}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = PoyshaRed)
                        }
                    }
                }
            }

            // Section Title
            item {
                Text(
                    "Cash Transactions",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = PoyshaTextDark,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            items(txList) { tx ->
                val isIn = tx.type == "IN"
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                color = if (isIn) Color(0xFFDCFCE3) else Color(0xFFFEE2E2),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    if (isIn) "IN" else "OUT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isIn) PoyshaGreen else PoyshaRed,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Column {
                                Text(tx.reason, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = PoyshaTextDark)
                                Text(tx.date, fontSize = 11.sp, color = PoyshaTextLight)
                            }
                        }

                        Text(
                            "${if (isIn) "+" else "-"}৳${"%,.0f".format(tx.amount)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isIn) PoyshaGreen else PoyshaRed
                        )
                    }
                }
            }
        }
    }

    showDialogType?.let { type ->
        val isIn = type == "IN"
        AlertDialog(
            onDismissRequest = { showDialogType = null },
            title = {
                Text(
                    if (isIn) "Add Cash To Counter" else "Withdraw Cash From Counter",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = txAmount,
                        onValueChange = { txAmount = it },
                        label = { Text("Amount (৳) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = txReason,
                        onValueChange = { txReason = it },
                        label = { Text("Reason / Notes *") },
                        placeholder = { Text(if (isIn) "e.g. Opening Balance or Cash Deposit" else "e.g. Owner Drawing or Petty Cash") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = txAmount.toDoubleOrNull() ?: 0.0
                        if (amt > 0 && txReason.isNotBlank()) {
                            PosRepository.recordCashTransaction(type, amt, txReason)
                            showDialogType = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isIn) PoyshaGreen else PoyshaRed)
                ) {
                    Text(if (isIn) "ADD CASH" else "WITHDRAW")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialogType = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
