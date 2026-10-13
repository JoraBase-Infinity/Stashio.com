package com.aistudio.poysha.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.poysha.data.PosRepository
import com.aistudio.poysha.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onSwitchShop: () -> Unit,
    onLogout: () -> Unit
) {
    val activeShop by PosRepository.activeShop.collectAsState()
    val currentUser by PosRepository.currentUser.collectAsState()

    var bluetoothPrinterEnabled by remember { mutableStateOf(false) }
    var autoPrintInvoice by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Profile", fontWeight = FontWeight.Bold, color = Color.White) },
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
            // Profile Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(PoyshaPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                currentUser?.name?.take(1)?.uppercase() ?: "P",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Column {
                            Text(
                                currentUser?.name ?: "Shop Owner",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PoyshaTextDark
                            )
                            Text(
                                currentUser?.phone ?: "01700000000",
                                fontSize = 12.sp,
                                color = PoyshaTextLight
                            )
                            Surface(
                                color = Color(0xFFDCFCE3),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    "ACTIVE ACCOUNT",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PoyshaGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Current Shop Settings
            item {
                Text("Shop Management", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(activeShop?.name ?: "No shop active", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PoyshaPrimary)
                                Text(activeShop?.address ?: "", fontSize = 11.sp, color = PoyshaTextLight)
                            }
                            Button(
                                onClick = onSwitchShop,
                                colors = ButtonDefaults.buttonColors(containerColor = PoyshaSecondary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Switch Shop", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Printer & Hardware Configuration
            item {
                Text("Hardware & Utilities", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Bluetooth Thermal Printer (58mm/80mm)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Connect portable POS receipt printer", fontSize = 11.sp, color = PoyshaTextLight)
                            }
                            Switch(
                                checked = bluetoothPrinterEnabled,
                                onCheckedChange = { bluetoothPrinterEnabled = it }
                            )
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Auto Prompt Print Dialog", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Display receipt right after sale confirmation", fontSize = 11.sp, color = PoyshaTextLight)
                            }
                            Switch(
                                checked = autoPrintInvoice,
                                onCheckedChange = { autoPrintInvoice = it }
                            )
                        }
                    }
                }
            }

            // Account & Logout
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            PosRepository.logout()
                            onLogout()
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, tint = PoyshaRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log Out from Poysha POS", fontWeight = FontWeight.Bold, color = PoyshaRed, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
