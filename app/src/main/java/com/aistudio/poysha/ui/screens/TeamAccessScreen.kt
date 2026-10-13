package com.aistudio.poysha.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.poysha.data.PosRepository
import com.aistudio.poysha.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamAccessScreen(
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var memberName by remember { mutableStateOf("") }
    var memberPhone by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Staff") }

    val mockMembers = remember {
        mutableStateListOf(
            mapOf("name" to "Fahim Maya", "phone" to "01712345678", "role" to "Owner"),
            mapOf("name" to "Anisur Rahman", "phone" to "01811223344", "role" to "Manager"),
            mapOf("name" to "Salam Mia", "phone" to "01922334455", "role" to "Staff")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Team & Access", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Member", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PoyshaPrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PoyshaSecondary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Member")
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
            item {
                Text("Shop Staff & Permissions", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
            }

            items(mockMembers) { member ->
                val role = member["role"] ?: "Staff"
                val badgeColor = when (role) {
                    "Owner" -> PoyshaGreen
                    "Manager" -> PoyshaBlue
                    else -> PoyshaOrange
                }

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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(PoyshaPrimaryLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    member["name"]?.take(1)?.uppercase() ?: "M",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Column {
                                Text(member["name"] ?: "", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
                                Text(member["phone"] ?: "", fontSize = 12.sp, color = PoyshaTextLight)
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    color = badgeColor.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        role.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = badgeColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        IconButton(onClick = {
                            mockMembers.remove(member)
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = PoyshaRed)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Team Member", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = memberName,
                        onValueChange = { memberName = it },
                        label = { Text("Full Name *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = memberPhone,
                        onValueChange = { memberPhone = it },
                        label = { Text("Mobile Number / Login ID *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (memberName.isNotBlank() && memberPhone.isNotBlank()) {
                            mockMembers.add(mapOf("name" to memberName, "phone" to memberPhone, "role" to selectedRole))
                            memberName = ""
                            memberPhone = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PoyshaPrimary)
                ) {
                    Text("ADD MEMBER")
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
