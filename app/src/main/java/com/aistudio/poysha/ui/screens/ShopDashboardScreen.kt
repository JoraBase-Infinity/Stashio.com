package com.aistudio.poysha.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.poysha.data.PosRepository
import com.aistudio.poysha.data.Shop
import com.aistudio.poysha.ui.theme.*

@Composable
fun ShopDashboardScreen(
    onShopSelected: (Shop) -> Unit,
    onLogout: () -> Unit
) {
    val shops by PosRepository.shops.collectAsState()
    val user by PosRepository.currentUser.collectAsState()
    var showAddShopDialog by remember { mutableStateOf(false) }

    var newShopName by remember { mutableStateOf("") }
    var newBusinessType by remember { mutableStateOf("Retail & Grocery") }
    var newAddress by remember { mutableStateOf("") }
    var newContact by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "POYSHA POS",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = PoyshaPrimary
                        )
                        Text(
                            user?.name ?: "Shop Owner",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = PoyshaTextLight
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = PoyshaRed)
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddShopDialog = true },
                containerColor = PoyshaSecondary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Shop")
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
                Text(
                    "Select Your Shop",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = PoyshaTextDark,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }

            items(shops) { shop ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            PosRepository.selectShop(shop)
                            onShopSelected(shop)
                        },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(PoyshaPrimaryLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Column {
                                Text(
                                    shop.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PoyshaTextDark
                                )
                                Text(
                                    shop.type,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PoyshaPrimary
                                )
                                Text(
                                    shop.address,
                                    fontSize = 11.sp,
                                    color = PoyshaTextLight
                                )
                            }
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open Shop",
                            tint = PoyshaPrimary
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAddShopDialog = true }
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = PoyshaGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "+ Register Another Shop",
                            fontWeight = FontWeight.Bold,
                            color = PoyshaGreen,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    if (showAddShopDialog) {
        AlertDialog(
            onDismissRequest = { showAddShopDialog = false },
            title = { Text("Register New Shop", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = newShopName,
                        onValueChange = { newShopName = it },
                        label = { Text("Shop Name *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newBusinessType,
                        onValueChange = { newBusinessType = it },
                        label = { Text("Business Type") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newAddress,
                        onValueChange = { newAddress = it },
                        label = { Text("Address / Location") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newContact,
                        onValueChange = { newContact = it },
                        label = { Text("Shop Mobile") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newShopName.isNotBlank()) {
                            val shop = PosRepository.addShop(newShopName, newBusinessType, newAddress, newContact)
                            showAddShopDialog = false
                            onShopSelected(shop)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PoyshaPrimary)
                ) {
                    Text("CREATE SHOP")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddShopDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
