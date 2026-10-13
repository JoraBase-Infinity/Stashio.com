package com.aistudio.poysha.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.poysha.data.Contact
import com.aistudio.poysha.data.PosRepository
import com.aistudio.poysha.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    onBack: () -> Unit
) {
    val contacts by PosRepository.contacts.collectAsState()
    var selectedType by remember { mutableStateOf("Customer") } // "Customer" or "Supplier"
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    var cName by remember { mutableStateOf("") }
    var cPhone by remember { mutableStateOf("") }
    var cEmail by remember { mutableStateOf("") }
    var cAddress by remember { mutableStateOf("") }

    val context = LocalContext.current

    val filteredContacts = remember(contacts, selectedType, searchQuery) {
        contacts.filter { it.type.equals(selectedType, ignoreCase = true) }
            .filter {
                searchQuery.isBlank() ||
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery, ignoreCase = true)
            }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PoyshaPrimary)
            ) {
                TopAppBar(
                    title = { Text("Contacts & Parties", fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Default.PersonAdd, contentDescription = "Add Contact", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = PoyshaPrimary)
                )

                // Tab Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("Customer", "Supplier").forEach { type ->
                        val isSelected = selectedType == type
                        Surface(
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedType = type }
                        ) {
                            Text(
                                if (type == "Customer") "Customers (${contacts.count { it.type.equals("Customer", true) }})"
                                else "Suppliers (${contacts.count { it.type.equals("Supplier", true) }})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) PoyshaPrimary else Color.White,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PoyshaSecondary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Contact")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(PoyshaBg)
        ) {
            Surface(color = Color.White, shadowElevation = 2.dp) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name or mobile number...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PoyshaTextLight) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC),
                        focusedBorderColor = PoyshaPrimary,
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredContacts) { contact ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE0E7FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        contact.name.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = PoyshaPrimary,
                                        fontSize = 18.sp
                                    )
                                }
                                Column {
                                    Text(contact.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
                                    Text(contact.phone, fontSize = 12.sp, color = PoyshaTextLight)
                                    if (contact.address.isNotBlank()) {
                                        Text(contact.address, fontSize = 10.sp, color = PoyshaTextLight)
                                    }
                                    if (contact.dueAmount > 0) {
                                        Surface(
                                            color = Color(0xFFFEE2E2),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Text(
                                                "Due: ৳${"%,.0f".format(contact.dueAmount)}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PoyshaRed,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}"))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFDCFCE3))
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = "Call", tint = PoyshaGreen, modifier = Modifier.size(18.dp))
                                }
                                IconButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${contact.phone}"))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFFEF3C7))
                                ) {
                                    Icon(Icons.Default.Message, contentDescription = "SMS", tint = PoyshaOrange, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New $selectedType", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = cName,
                        onValueChange = { cName = it },
                        label = { Text("Name *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = cPhone,
                        onValueChange = { cPhone = it },
                        label = { Text("Mobile Number *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = cEmail,
                        onValueChange = { cEmail = it },
                        label = { Text("Email (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = cAddress,
                        onValueChange = { cAddress = it },
                        label = { Text("Address / Location") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (cName.isNotBlank() && cPhone.isNotBlank()) {
                            PosRepository.addContact(cName, cPhone, cEmail, cAddress, selectedType)
                            cName = ""
                            cPhone = ""
                            cEmail = ""
                            cAddress = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PoyshaPrimary)
                ) {
                    Text("SAVE CONTACT")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
