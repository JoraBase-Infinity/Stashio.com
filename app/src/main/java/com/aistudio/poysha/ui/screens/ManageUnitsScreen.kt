package com.aistudio.poysha.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.poysha.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageUnitsScreen(
    onBack: () -> Unit
) {
    val units = remember { mutableStateListOf("pcs", "kg", "ডজন", "can", "box", "jar", "packet", "litre") }
    var newUnitText by remember { mutableStateOf("") }
    var editingIndex by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Units (ইউনিট)", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PoyshaPrimary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(PoyshaBg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Add / Edit Box
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = newUnitText,
                    onValueChange = { newUnitText = it },
                    placeholder = { Text("Enter unit name (e.g. kg, pcs, ডজন)") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Button(
                    onClick = {
                        val text = newUnitText.trim()
                        if (text.isNotBlank()) {
                            val idx = editingIndex
                            if (idx != null) {
                                units[idx] = text
                                editingIndex = null
                            } else {
                                units.add(text)
                            }
                            newUnitText = ""
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (editingIndex != null) PoyshaOrange else PoyshaPrimary),
                    modifier = Modifier.height(52.dp)
                ) {
                    Icon(if (editingIndex != null) Icons.Default.Check else Icons.Default.Add, contentDescription = null)
                }
            }

            Text("Configured Units (${units.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(units.toList()) { unit ->
                    val idx = units.indexOf(unit)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(unit, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = PoyshaTextDark)

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconButton(
                                    onClick = {
                                        editingIndex = idx
                                        newUnitText = unit
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PoyshaOrange, modifier = Modifier.size(18.dp))
                                }
                                IconButton(
                                    onClick = { units.removeAt(idx) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = PoyshaRed, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
