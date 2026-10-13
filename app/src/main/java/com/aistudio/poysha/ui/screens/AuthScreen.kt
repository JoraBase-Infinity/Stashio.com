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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.poysha.data.PosRepository
import com.aistudio.poysha.ui.theme.*

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit
) {
    var authMode by remember { mutableStateOf("LOGIN") } // LOGIN, REGISTER, FORGOT
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Register fields
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var regPasswordVisible by remember { mutableStateOf(false) }

    // Reset password field
    var resetEmail by remember { mutableStateOf("") }

    val isLoading by PosRepository.isLoading.collectAsState()
    val repoError by PosRepository.lastError.collectAsState()
    val syncStatus by PosRepository.syncStatus.collectAsState()
    val currentUser by PosRepository.currentUser.collectAsState()

    var localMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessMessage by remember { mutableStateOf(false) }

    // If user is already logged in, they can continue directly
    LaunchedEffect(currentUser) {
        if (currentUser != null && authMode != "REGISTER") {
            // Already logged in
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF4A00E0), Color(0xFF8E2DE2), PoyshaPrimary)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 460.dp)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Poysha POS Logo
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(PoyshaPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
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

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Poysha Cloud POS",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = PoyshaTextDark
                )

                // Real Firebase Cloud Status Tag
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2E7D32))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Firebase Cloud Realtime (stashio-6b332)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }

                // If currently logged in user exists, give quick continue option
                if (currentUser != null && authMode == "LOGIN") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                "Logged in as:",
                                fontSize = 12.sp,
                                color = Color(0xFF6B21A8)
                            )
                            Text(
                                currentUser?.email?.ifBlank { currentUser?.name } ?: "Firebase User",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF581C87)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onLoginSuccess,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Enter Dashboard", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { PosRepository.logout() },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Log out", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                when (authMode) {
                    "LOGIN" -> {
                        Text(
                            "Log in with Firebase Account",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PoyshaTextDark
                        )
                        Text(
                            "Enter your registered Firebase email & password",
                            fontSize = 12.sp,
                            color = PoyshaTextLight,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick email suggestion chip
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFF3E8FF),
                                modifier = Modifier.clickable {
                                    emailOrPhone = "fahimmaya722@gmail.com"
                                    PosRepository.clearError()
                                    localMessage = null
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = PoyshaPrimary, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("fahimmaya722@gmail.com", fontSize = 11.sp, color = PoyshaPrimary, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = emailOrPhone,
                            onValueChange = {
                                emailOrPhone = it
                                PosRepository.clearError()
                                localMessage = null
                            },
                            label = { Text("Email or Registered Mobile") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = PoyshaPrimary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_email_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                PosRepository.clearError()
                                localMessage = null
                            },
                            label = { Text("Password / PIN") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PoyshaPrimary) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                localMessage = null
                                isSuccessMessage = false
                                if (emailOrPhone.isBlank() || password.isBlank()) {
                                    localMessage = "দয়া করে ইমেইল এবং পাসওয়ার্ড দিন (Please enter Email & Password)."
                                } else {
                                    PosRepository.signInWithEmailPassword(emailOrPhone, password) { success, err ->
                                        if (success) {
                                            onLoginSuccess()
                                        } else {
                                            localMessage = err
                                        }
                                    }
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("firebase_login_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PoyshaPrimary)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color.White,
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("লগইন হচ্ছে...", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            } else {
                                Text("FIREBASE LOGIN", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Create New Account",
                                color = PoyshaPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .clickable {
                                        authMode = "REGISTER"
                                        if (emailOrPhone.isNotBlank()) {
                                            if (emailOrPhone.contains("@")) {
                                                regEmail = emailOrPhone
                                            } else {
                                                regPhone = emailOrPhone
                                            }
                                        }
                                        localMessage = null
                                        PosRepository.clearError()
                                    }
                                    .padding(vertical = 4.dp)
                            )
                            Text(
                                "Forgot Password?",
                                color = PoyshaOrange,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .clickable {
                                        authMode = "FORGOT"
                                        if (emailOrPhone.contains("@")) {
                                            resetEmail = emailOrPhone
                                        }
                                        localMessage = null
                                        PosRepository.clearError()
                                    }
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }

                    "REGISTER" -> {
                        Text(
                            "Create Real Firebase Account",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = PoyshaTextDark
                        )
                        Text(
                            "This account will be permanently registered in Firebase",
                            fontSize = 12.sp,
                            color = PoyshaTextLight,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = regName,
                            onValueChange = { regName = it; localMessage = null },
                            label = { Text("Full Name / শপের মালিকের নাম") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PoyshaPrimary) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it; localMessage = null },
                            label = { Text("Firebase Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = PoyshaPrimary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regPhone,
                            onValueChange = { regPhone = it; localMessage = null },
                            label = { Text("Mobile Number (ঐচ্ছিক)") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = PoyshaPrimary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it; localMessage = null },
                            label = { Text("Password (কমপক্ষে ৬ অক্ষর)") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PoyshaPrimary) },
                            trailingIcon = {
                                IconButton(onClick = { regPasswordVisible = !regPasswordVisible }) {
                                    Icon(
                                        imageVector = if (regPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regConfirmPassword,
                            onValueChange = { regConfirmPassword = it; localMessage = null },
                            label = { Text("Confirm Password") },
                            leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = PoyshaPrimary) },
                            visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                localMessage = null
                                isSuccessMessage = false
                                if (regName.isBlank() || regEmail.isBlank() || regPassword.isBlank()) {
                                    localMessage = "নাম, ইমেইল এবং পাসওয়ার্ড পূরণ করুন।"
                                } else if (regPassword.length < 6) {
                                    localMessage = "পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে।"
                                } else if (regPassword != regConfirmPassword) {
                                    localMessage = "উভয় পাসওয়ার্ড একই হতে হবে।"
                                } else {
                                    PosRepository.registerWithEmailPassword(
                                        name = regName,
                                        email = regEmail,
                                        phone = regPhone,
                                        pass = regPassword
                                    ) { success, err ->
                                        if (success) {
                                            onLoginSuccess()
                                        } else {
                                            localMessage = err
                                        }
                                    }
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("register_submit_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PoyshaPrimary)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("তৈরি হচ্ছে...", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            } else {
                                Text("REGISTER REAL ACCOUNT", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            "Already have an account? Log in",
                            color = PoyshaPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            modifier = Modifier.clickable {
                                authMode = "LOGIN"
                                localMessage = null
                                PosRepository.clearError()
                            }
                        )
                    }

                    "FORGOT" -> {
                        Text(
                            "Reset Firebase Password",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = PoyshaTextDark
                        )
                        Text(
                            "A password reset link will be sent to your email",
                            fontSize = 12.sp,
                            color = PoyshaTextLight,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))

                        OutlinedTextField(
                            value = resetEmail,
                            onValueChange = { resetEmail = it; localMessage = null },
                            label = { Text("Your Registered Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = PoyshaPrimary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                localMessage = null
                                if (resetEmail.isBlank() || !resetEmail.contains("@")) {
                                    localMessage = "দয়া করে সঠিক ইমেইল দিন।"
                                } else {
                                    PosRepository.sendPasswordReset(resetEmail) { success, msg ->
                                        isSuccessMessage = success
                                        localMessage = msg
                                    }
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PoyshaPrimary)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("পাঠানো হচ্ছে...", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            } else {
                                Text("SEND RESET LINK", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            "Back to Login",
                            color = PoyshaPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            modifier = Modifier.clickable {
                                authMode = "LOGIN"
                                localMessage = null
                                PosRepository.clearError()
                            }
                        )
                    }
                }

                // Error or Message display
                val displayedError = localMessage ?: repoError
                AnimatedVisibility(visible = displayedError != null) {
                    displayedError?.let { msg ->
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSuccessMessage) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isSuccessMessage) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = if (isSuccessMessage) Color(0xFF2E7D32) else PoyshaRed,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = msg,
                                        color = if (isSuccessMessage) Color(0xFF2E7D32) else PoyshaRed,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                if (!isSuccessMessage && authMode == "LOGIN") {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                authMode = "REGISTER"
                                                if (emailOrPhone.isNotBlank()) {
                                                    if (emailOrPhone.contains("@")) {
                                                        regEmail = emailOrPhone
                                                    } else {
                                                        regPhone = emailOrPhone
                                                    }
                                                }
                                                localMessage = null
                                                PosRepository.clearError()
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = PoyshaPrimary),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("রেজিস্ট্রেশন করুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                authMode = "FORGOT"
                                                if (emailOrPhone.contains("@")) {
                                                    resetEmail = emailOrPhone
                                                }
                                                localMessage = null
                                                PosRepository.clearError()
                                            },
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("পাসওয়ার্ড রিসেট", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
