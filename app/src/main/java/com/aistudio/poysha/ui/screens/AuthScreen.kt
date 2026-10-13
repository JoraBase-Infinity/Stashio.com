package com.aistudio.poysha.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.poysha.data.PosRepository
import com.aistudio.poysha.ui.theme.*

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit
) {
    var authMode by remember { mutableStateOf("LOGIN") } // LOGIN, REGISTER, OTP, PIN, FORGOT
    var phone by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF667EEA), PoyshaPrimaryLight, PoyshaPrimary)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 440.dp)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Poysha POS Logo Symbol
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(PoyshaPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFBF7EF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("৳", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = PoyshaPrimary)
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(PoyshaSecondary)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (authMode) {
                    "LOGIN" -> {
                        Text("Welcome Back", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
                        Text("Login to manage your shop", fontSize = 13.sp, color = PoyshaTextLight)
                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Mobile Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = PoyshaPrimary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = pin,
                            onValueChange = { pin = it },
                            label = { Text("6-Digit PIN") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PoyshaPrimary) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (phone.isBlank() || pin.isBlank()) {
                                    errorMessage = "Please enter Phone & PIN!"
                                } else {
                                    PosRepository.login(phone, pin)
                                    onLoginSuccess()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PoyshaPrimary)
                        ) {
                            Text("LOGIN", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                                auth.signInAnonymously().addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val firebaseUser = auth.currentUser
                                        PosRepository.loginWithFirebase(firebaseUser?.uid ?: "user_cloud", firebaseUser?.email ?: "Cloud Owner")
                                        successMessage = "Connected to Firebase Cloud! Logged in."
                                        onLoginSuccess()
                                    } else {
                                        errorMessage = "Firebase Cloud Login failed: ${task.exception?.localizedMessage}"
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PoyshaPrimary)
                        ) {
                            Icon(Icons.Default.Cloud, contentDescription = null, tint = PoyshaPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sign in with Firebase Cloud", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Create Account",
                                color = PoyshaPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                modifier = Modifier.clickable { authMode = "REGISTER" }
                            )
                            Text(
                                "Forgot PIN?",
                                color = PoyshaOrange,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                modifier = Modifier.clickable { authMode = "FORGOT" }
                            )
                        }
                    }

                    "REGISTER" -> {
                        Text("New Account", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
                        Text("Join Poysha POS today", fontSize = 13.sp, color = PoyshaTextLight)
                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PoyshaPrimary) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Mobile Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = PoyshaPrimary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email (For OTP)") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = PoyshaPrimary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (name.isBlank() || phone.isBlank() || email.isBlank()) {
                                    errorMessage = "Please fill all fields!"
                                } else {
                                    authMode = "OTP"
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PoyshaPrimary)
                        ) {
                            Text("SEND OTP CODE", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "Already have an account? Login",
                            color = PoyshaPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            modifier = Modifier.clickable { authMode = "LOGIN" }
                        )
                    }

                    "OTP" -> {
                        Text("Verify Code", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
                        Text("Demo OTP is 1234", fontSize = 13.sp, color = PoyshaPrimary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = otpInput,
                            onValueChange = { otpInput = it },
                            label = { Text("Enter 4-Digit OTP") },
                            leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = PoyshaPrimary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (otpInput == "1234" || otpInput.length >= 4) {
                                    authMode = "PIN"
                                } else {
                                    errorMessage = "Enter valid 4-digit code!"
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PoyshaPrimary)
                        ) {
                            Text("VERIFY & PROCEED", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    "PIN" -> {
                        Text("Secure Account", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
                        Text("Set a 6-digit PIN for login", fontSize = 13.sp, color = PoyshaTextLight)
                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = newPin,
                            onValueChange = { newPin = it },
                            label = { Text("New PIN (6 Digits)") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = confirmPin,
                            onValueChange = { confirmPin = it },
                            label = { Text("Confirm PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (newPin.length < 4 || newPin != confirmPin) {
                                    errorMessage = "PINs must match and be valid!"
                                } else {
                                    PosRepository.register(
                                        name = name.ifBlank { "Shop Owner" },
                                        phone = phone,
                                        email = email,
                                        pin = newPin
                                    )
                                    onLoginSuccess()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PoyshaPrimary)
                        ) {
                            Text("SAVE & LOGIN", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    "FORGOT" -> {
                        Text("Reset PIN", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PoyshaTextDark)
                        Text("Enter your registered mobile number", fontSize = 13.sp, color = PoyshaTextLight)
                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Mobile Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = PoyshaPrimary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { authMode = "OTP" },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PoyshaPrimary)
                        ) {
                            Text("SEND RESET CODE", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "Back to Login",
                            color = PoyshaPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            modifier = Modifier.clickable { authMode = "LOGIN" }
                        )
                    }
                }

                AnimatedVisibility(visible = errorMessage != null) {
                    errorMessage?.let {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(it, color = PoyshaRed, fontSize = 12.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}
