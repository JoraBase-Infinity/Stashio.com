package com.aistudio.poysha.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aistudio.poysha.data.PosRepository
import com.aistudio.poysha.data.Sale
import com.aistudio.poysha.data.Shop
import com.aistudio.poysha.ui.theme.*

@Composable
fun InvoicePreviewDialog(
    sale: Sale,
    shop: Shop,
    onDismiss: () -> Unit
) {
    val wordsBDT = PosRepository.numberToWordsBDT(sale.grandTotal)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PoyshaGreen)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sale Completed!", fontWeight = FontWeight.Bold, color = PoyshaGreen, fontSize = 16.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = PoyshaTextLight)
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 10.dp))

                // Printable Memo Header
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        shop.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PoyshaPrimary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        shop.address,
                        fontSize = 11.sp,
                        color = PoyshaTextLight,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Mobile: ${shop.contact}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = PoyshaTextLight
                    )

                    Surface(
                        color = PoyshaPrimary,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            "INVOICE / MEMO",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Invoice metadata
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Customer: ${sale.customerName}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PoyshaTextDark)
                        if (sale.customerPhone.isNotBlank()) {
                            Text("Phone: ${sale.customerPhone}", fontSize = 11.sp, color = PoyshaTextLight)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Inv: ${sale.invoiceNo}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PoyshaPrimary, fontFamily = FontFamily.Monospace)
                        Text("Date: ${sale.date}", fontSize = 11.sp, color = PoyshaTextLight)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Table Header
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Item", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = PoyshaTextLight, modifier = Modifier.weight(2f))
                        Text("Qty", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = PoyshaTextLight, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                        Text("Rate", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = PoyshaTextLight, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        Text("Total", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = PoyshaTextLight, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                    }
                }

                // Item rows
                sale.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(2f)) {
                            Text(item.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PoyshaTextDark)
                            if (item.brand.isNotBlank()) {
                                Text(item.brand, fontSize = 10.sp, color = PoyshaTextLight)
                            }
                        }
                        Text("${item.qty} ${item.unit}", fontSize = 11.sp, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                        Text("৳${"%,.0f".format(item.rate)}", fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        Text("৳${"%,.0f".format(item.amount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.5.dp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Calculation Breakdown
                Column(modifier = Modifier.fillMaxWidth()) {
                    SummaryRow("Subtotal", "৳${"%,.0f".format(sale.subtotal)}")
                    if (sale.discount > 0) {
                        SummaryRow("Discount", "-৳${"%,.0f".format(sale.discount)}", PoyshaRed)
                    }
                    SummaryRow("Grand Total", "৳${"%,.0f".format(sale.grandTotal)}", PoyshaPrimary, isBold = true)
                    SummaryRow("Paid (${sale.paymentMethod})", "৳${"%,.0f".format(sale.paid)}", PoyshaGreen, isBold = true)
                    if (sale.due > 0) {
                        SummaryRow("Due Amount", "৳${"%,.0f".format(sale.due)}", PoyshaRed, isBold = true)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // In words BDT
                Surface(
                    color = Color(0xFFF0FDF4),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "In Words: $wordsBDT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = PoyshaPrimary,
                        modifier = Modifier.padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Print / PDF", fontSize = 12.sp)
                    }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PoyshaPrimary)
                    ) {
                        Text("Done", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, color: Color = PoyshaTextDark, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal, color = PoyshaTextLight)
        Text(value, fontSize = 12.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold, color = color)
    }
}
