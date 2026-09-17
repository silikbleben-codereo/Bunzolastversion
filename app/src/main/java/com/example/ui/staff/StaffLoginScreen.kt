package com.example.ui.staff

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.repository.BunzoRepository
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.StatusCancelled
import com.example.util.BunzoSoundManager
import kotlinx.coroutines.launch

@Composable
fun StaffLoginScreen(
    isArabic: Boolean,
    onLoginSuccess: (User) -> Unit,
    onBackToCustomer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 460.dp)
                .testTag("staff_login_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Surface(
                    shape = CircleShape,
                    color = FlameOrange,
                    modifier = Modifier.size(68.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.LockPerson,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = if (isArabic) "بوابة موظفي وإدارة بنـزو" else "Bunzo Staff & Admin Portal",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isArabic) "تسجيل الدخول الموحد (شاشة المطبخ KDS / الإدارة العامة)" else "Unified Access (Kitchen KDS & Administrator)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Input Identifier (Email or Phone)
                OutlinedTextField(
                    value = identifier,
                    onValueChange = {
                        identifier = it.replace(" ", "").trim()
                        errorMessage = null
                    },
                    label = { Text(if (isArabic) "البريد الإلكتروني المعتمد" else "Authorized Email") },
                    leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null, tint = FlameOrange) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        autoCorrectEnabled = false
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("staff_identifier_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Helpful quick-fill button for the admin email
                if (identifier.isBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = {
                            identifier = "betulelhamed380@gmail.com"
                            errorMessage = null
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp), tint = FlameOrange)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isArabic) "استخدام بريد الأدمن: betulelhamed380@gmail.com" else "Use Admin: betulelhamed380@gmail.com",
                            fontSize = 11.sp,
                            color = FlameOrange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Input Password (supports long passwords 40+ characters)
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = { Text(if (isArabic) "كلمة المرور (يدعم 40+ خانة)" else "Password (supports 40+ chars)") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = FlameOrange) },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        autoCorrectEnabled = false
                    ),
                    singleLine = !passwordVisible,
                    maxLines = if (passwordVisible) 4 else 1,
                    supportingText = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isArabic) "يدعم كلمات سر طويلة (40 خانة وأكثر بلا حد)" else "Supports 40+ chars passwords (no limit)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (password.isNotEmpty()) {
                                Text(
                                    text = if (isArabic) "${password.length} خانة" else "${password.length} chars",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (password.length >= 40) FlameOrange else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("staff_password_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = StatusCancelled.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = errorMessage!!,
                                color = StatusCancelled,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isArabic)
                                    "ملاحظة: يمكنك تغيير أو إعادة تعيين كلمة المرور لأي حساب في ثوانٍ من لوحة Firebase -> Authentication -> Users عبر الضغط على النقاط الثلاث ⋮ بجانب الإيميل."
                                else
                                    "Note: You can reset/change the password anytime from Firebase Console -> Authentication -> Users via the 3-dots ⋮ menu.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Sign In Button
                Button(
                    onClick = {
                        if (identifier.isBlank() || password.isBlank()) {
                            errorMessage = if (isArabic) "يرجى كتابة البريد الإلكتروني وكلمة المرور" else "Please enter email and password"
                            return@Button
                        }
                        isLoading = true
                        errorMessage = null
                        coroutineScope.launch {
                            val result = BunzoRepository.loginStaff(identifier, password)
                            isLoading = false
                            result.onSuccess { user ->
                                BunzoSoundManager.playSuccess()
                                onLoginSuccess(user)
                            }.onFailure {
                                BunzoSoundManager.playRemove()
                                errorMessage = it.message ?: (if (isArabic) "فشل التحقق من بيانات الموظف" else "Staff authentication failed")
                            }
                        }
                    },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FlameOrange),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("staff_submit_login_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Login, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "تسجيل الدخول لمنظومة العمل" else "Sign In",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Firebase Auth production security note
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = FlameOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic)
                                "الأمان الفعلي مفعل: يتم التحقق من الصلاحيات وأدوار الموظفين عبر Firebase Custom Claims وCloud Firestore."
                            else
                                "Enterprise Security: Access is validated via Firebase Custom Claims & Cloud Firestore roles.",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Back to Customer App
                TextButton(
                    onClick = onBackToCustomer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isArabic) "العودة لتطبيق الزبائن" else "Back to Customer App")
                }
            }
        }
    }
}
