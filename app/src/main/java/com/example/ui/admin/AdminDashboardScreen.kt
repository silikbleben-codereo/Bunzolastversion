package com.example.ui.admin

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.*
import com.example.data.repository.BunzoRepository
import com.example.ui.components.BunzoProductImage
import kotlinx.coroutines.launch
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.*

// -------------------------------------------------------------
// Admin Screen Wrapper (Manages Admin Auth vs Admin Dashboard)
// -------------------------------------------------------------
@Composable
fun AdminPortalScreen(
    isArabic: Boolean,
    onBackToCustomer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentAdminUser by BunzoRepository.currentAdminUser.collectAsState()

    if (currentAdminUser == null) {
        AdminLoginScreen(
            isArabic = isArabic,
            onBackToCustomer = onBackToCustomer
        )
    } else {
        AdminDashboardContent(
            isArabic = isArabic,
            onBackToCustomer = onBackToCustomer
        )
    }
}

// -------------------------------------------------------------
// Admin Login Screen
// -------------------------------------------------------------
@Composable
fun AdminLoginScreen(
    isArabic: Boolean,
    onBackToCustomer: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 450.dp)
                .testTag("admin_login_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = FlameOrange,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = if (isArabic) "لوحة تحكم مطعم بنـزو" else "Bunzo Restaurant Admin",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isArabic) "تسجيل الدخول للموظفين والإدارة" else "Staff & Management Portal",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quick credential helper chip
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = FlameOrange.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, FlameOrange.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            identifier = "betulelhamed380@gmail.com"
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.VpnKey, contentDescription = null, tint = FlameOrange, modifier = Modifier.size(16.dp))
                        Text(
                            text = if (isArabic) "حساب الإدارة: betulelhamed380@gmail.com (انقر للملء)" else "Admin account: betulelhamed380@gmail.com (click to fill)",
                            fontSize = 11.sp,
                            color = FlameOrange,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = identifier,
                    onValueChange = { identifier = it },
                    label = { Text(if (isArabic) "البريد الإلكتروني المعتمد" else "Authorized Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_identifier_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(if (isArabic) "كلمة المرور" else "Password") },
                    visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_password_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = StatusCancelled.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = StatusCancelled,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                            result.onFailure {
                                errorMessage = it.message
                            }
                        }
                    },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FlameOrange),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("admin_submit_login_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Login, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isArabic) "دخول لوحة التحكم" else "Sign In to Admin", fontWeight = FontWeight.Black)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = onBackToCustomer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isArabic) "العودة لتطبيق الزبون" else "Back to Customer App")
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Admin Dashboard Main Content with Tabs
// -------------------------------------------------------------
@Composable
fun AdminDashboardContent(
    isArabic: Boolean,
    onBackToCustomer: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableIntStateOf(0) }
    val orders by BunzoRepository.orders.collectAsState()
    val products by BunzoRepository.products.collectAsState()
    val users by BunzoRepository.users.collectAsState()
    val currentAdminUser by BunzoRepository.currentAdminUser.collectAsState()

    val newOrdersCount = remember(orders) {
        orders.count { it.status == "received" }
    }
    val customerUsersCount = remember(users, orders) {
        val registered = users.filter { it.role.isBlank() || it.role.equals("customer", ignoreCase = true) }
            .map { if (it.phone.isNotBlank()) it.phone else it.uid }
        val orderPhones = orders.mapNotNull { if (it.customerPhone.isNotBlank()) it.customerPhone else null }
        (registered + orderPhones).distinct().size
    }

    Scaffold(
        topBar = {
            Surface(
                color = CharcoalDark,
                shadowElevation = 4.dp
            ) {
                Column {
                    // Header Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Brand Logo & Live status
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = FlameOrange,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Dashboard,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = if (isArabic) "إدارة بنـزو" else "Bunzo Admin",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                    Surface(
                                        color = StatusDelivered.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(StatusDelivered)
                                            )
                                            Text(
                                                text = if (isArabic) "مباشر" else "Live",
                                                color = StatusDelivered,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                val adminName = currentAdminUser?.fullName ?: (if (isArabic) "لوحة التحكم السحابية" else "Cloud Admin Panel")
                                Text(
                                    text = adminName,
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.7f),
                                    maxLines = 1
                                )
                            }
                        }

                        // Actions: Cloud Sync, Customer App & Logout
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Cloud Sync Refresh Button
                            Surface(
                                color = Color.White.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .size(34.dp)
                                    .clickable(enabled = !isRefreshing) {
                                        isRefreshing = true
                                        coroutineScope.launch {
                                            val res = BunzoRepository.refreshFromFirestore(context)
                                            isRefreshing = false
                                            res.onSuccess { count ->
                                                Toast.makeText(
                                                    context,
                                                    if (isArabic) "تمت مزامنة البيانات ($count طلب)" else "Synced ($count orders)",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }.onFailure {
                                                Toast.makeText(
                                                    context,
                                                    if (isArabic) "تعذر المزامنة السحابية" else "Sync failed",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (isRefreshing) {
                                        CircularProgressIndicator(
                                            color = FlameOrange,
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = "Sync Cloud",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Surface(
                                color = Color.White.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable { onBackToCustomer() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Storefront,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = if (isArabic) "تطبيق الزبائن" else "Customer App",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }

                            Surface(
                                color = StatusCancelled.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .size(34.dp)
                                    .clickable {
                                        BunzoRepository.logoutStaff()
                                        onBackToCustomer()
                                    }
                                    .testTag("admin_logout_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Logout,
                                        contentDescription = "Logout",
                                        tint = StatusCancelled,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Navigation Tabs with badges
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        edgePadding = 10.dp,
                        containerColor = CharcoalSurface,
                        contentColor = Color.White,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = FlameOrange,
                                height = 3.dp
                            )
                        },
                        divider = {}
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text(
                                        if (isArabic) "الطلبات الحية" else "Live Orders",
                                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.5.sp
                                    )
                                    if (newOrdersCount > 0) {
                                        Badge(containerColor = FlameOrange, contentColor = Color.White) {
                                            Text(newOrdersCount.toString(), fontSize = 10.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            },
                            selectedContentColor = FlameOrange,
                            unselectedContentColor = Color.White.copy(alpha = 0.65f),
                            modifier = Modifier.testTag("admin_tab_orders")
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.RestaurantMenu, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text(
                                        if (isArabic) "قائمة الطعام" else "Menu",
                                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.5.sp
                                    )
                                    Badge(containerColor = Color.White.copy(alpha = 0.15f), contentColor = Color.White) {
                                        Text(products.size.toString(), fontSize = 10.sp)
                                    }
                                }
                            },
                            selectedContentColor = FlameOrange,
                            unselectedContentColor = Color.White.copy(alpha = 0.65f),
                            modifier = Modifier.testTag("admin_tab_menu")
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text(
                                        if (isArabic) "العملاء" else "Customers",
                                        fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.5.sp
                                    )
                                    Badge(containerColor = Color.White.copy(alpha = 0.15f), contentColor = Color.White) {
                                        Text(customerUsersCount.toString(), fontSize = 10.sp)
                                    }
                                }
                            },
                            selectedContentColor = FlameOrange,
                            unselectedContentColor = Color.White.copy(alpha = 0.65f),
                            modifier = Modifier.testTag("admin_tab_customers")
                        )
                        Tab(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text(
                                        if (isArabic) "الإعدادات والقواعد" else "Settings",
                                        fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.5.sp
                                    )
                                }
                            },
                            selectedContentColor = FlameOrange,
                            unselectedContentColor = Color.White.copy(alpha = 0.65f),
                            modifier = Modifier.testTag("admin_tab_settings")
                        )
                        Tab(
                            selected = selectedTab == 4,
                            onClick = { selectedTab = 4 },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text(
                                        if (isArabic) "سجل الأمان" else "Audit Log",
                                        fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.5.sp
                                    )
                                }
                            },
                            selectedContentColor = FlameOrange,
                            unselectedContentColor = Color.White.copy(alpha = 0.65f),
                            modifier = Modifier.testTag("admin_tab_audit")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> AdminOrdersTab(isArabic = isArabic)
                1 -> AdminMenuTab(isArabic = isArabic)
                2 -> AdminCustomersTab(isArabic = isArabic)
                3 -> AdminSettingsTab(isArabic = isArabic)
                4 -> AdminAuditLogTab(isArabic = isArabic)
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 1: Real-time Live Orders
// -------------------------------------------------------------
@Composable
fun AdminOrdersTab(isArabic: Boolean) {
    val orders by BunzoRepository.orders.collectAsState()
    var selectedStatusFilter by remember { mutableStateOf<String?>("all") }
    var searchQuery by remember { mutableStateOf("") }
    var orderToCancel by remember { mutableStateOf<Order?>(null) }
    val context = LocalContext.current

    val filteredOrders = remember(orders, selectedStatusFilter, searchQuery) {
        orders.filter { ord ->
            val matchStatus = when (selectedStatusFilter) {
                "all" -> true
                "active" -> ord.status in listOf("received", "preparing", "on_the_way", "ready_for_pickup", "served")
                else -> ord.status == selectedStatusFilter
            }
            val matchQuery = searchQuery.isBlank() ||
                    ord.id.contains(searchQuery, ignoreCase = true) ||
                    ord.customerName.contains(searchQuery, ignoreCase = true) ||
                    ord.customerPhone.contains(searchQuery, ignoreCase = true)
            matchStatus && matchQuery
        }.sortedByDescending { it.createdAt }
    }

    // KPI Metrics
    val totalRevenue = remember(orders) {
        orders.filter { it.status != "cancelled" }.sumOf { it.total }
    }
    val inProgressCount = remember(orders) {
        orders.count { it.status in listOf("received", "preparing", "on_the_way", "ready_for_pickup", "served") }
    }
    val deliveredCount = remember(orders) {
        orders.count { it.status == "delivered" }
    }
    val newCount = remember(orders) {
        orders.count { it.status == "received" }
    }
    val preparingCount = remember(orders) {
        orders.count { it.status == "preparing" }
    }
    val onTheWayCount = remember(orders) {
        orders.count { it.status in listOf("on_the_way", "ready_for_pickup", "served") }
    }
    val cancelledCount = remember(orders) {
        orders.count { it.status == "cancelled" }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_orders_list"),
        contentPadding = PaddingValues(14.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // KPI Ribbon (2 rows of 2 for optimal readability and balance)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminStatCard(
                        title = if (isArabic) "إجمالي الطلبات" else "Total Orders",
                        value = orders.size.toString(),
                        subtitle = if (isArabic) "سجل كامل الطلبات" else "All orders",
                        icon = Icons.Default.ReceiptLong,
                        color = FlameOrange,
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        title = if (isArabic) "قيد التحضير والتوصيل" else "In Kitchen/Road",
                        value = inProgressCount.toString(),
                        subtitle = if (isArabic) "طلبات نشطة حالياً" else "Active now",
                        icon = Icons.Default.PendingActions,
                        color = AmberGoldDark,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminStatCard(
                        title = if (isArabic) "المكتملة بنجاح" else "Delivered",
                        value = deliveredCount.toString(),
                        subtitle = if (isArabic) "تم تسليمها للعميل" else "Completed",
                        icon = Icons.Default.CheckCircle,
                        color = StatusDelivered,
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        title = if (isArabic) "إجمالي المبيعات" else "Total Sales",
                        value = formatCurrency(totalRevenue, isArabic),
                        subtitle = if (isArabic) "صافي الإيرادات" else "Net revenue",
                        icon = Icons.Default.Paid,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Search Field with Clear Button
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (isArabic) "ابحث برقم الطلب، اسم العميل، أو رقم الهاتف..." else "Search order #, customer, phone...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = FlameOrange) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FlameOrange,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        }

        // Status Filter Chips with exact counts
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                val filters = listOf(
                    "all" to (if (isArabic) "الكل (${orders.size})" else "All (${orders.size})"),
                    "received" to (if (isArabic) "جديد ($newCount) ⚡" else "New ($newCount)"),
                    "preparing" to (if (isArabic) "قيد التحضير ($preparingCount) 🍳" else "Preparing ($preparingCount)"),
                    "on_the_way" to (if (isArabic) "في الطريق ($onTheWayCount) 🛵" else "On Way ($onTheWayCount)"),
                    "delivered" to (if (isArabic) "تم التسليم ($deliveredCount) ✓" else "Delivered ($deliveredCount)"),
                    "cancelled" to (if (isArabic) "ملغى ($cancelledCount) ✕" else "Cancelled ($cancelledCount)")
                )
                items(filters) { (key, label) ->
                    val isSelected = selectedStatusFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedStatusFilter = key },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FlameOrange,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        if (filteredOrders.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (isArabic) "لا توجد طلبات تطابق الفلتر المحدد" else "No orders matching current filter",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            items(filteredOrders, key = { it.id }) { order ->
                AdminOrderCard(
                    order = order,
                    isArabic = isArabic,
                    onAcceptOrder = {
                        BunzoRepository.updateOrderStatus(order.id, "preparing")
                    },
                    onMarkOnTheWay = {
                        val nextStatus = when (order.parsedOrderType) {
                            OrderType.DELIVERY -> "on_the_way"
                            OrderType.PICKUP -> "ready_for_pickup"
                            OrderType.TABLE -> "served"
                        }
                        BunzoRepository.updateOrderStatus(order.id, nextStatus)
                    },
                    onMarkDelivered = {
                        BunzoRepository.updateOrderStatus(order.id, "delivered")
                    },
                    onCancelClick = {
                        orderToCancel = order
                    }
                )
            }
        }
    }

    // Cancellation Reason Dialog
    if (orderToCancel != null) {
        var cancelReason by remember { mutableStateOf("") }
        val cancelTarget = orderToCancel!!

        AlertDialog(
            onDismissRequest = { orderToCancel = null },
            title = {
                Text(
                    text = if (isArabic) "إلغاء الطلب #${cancelTarget.id}" else "Cancel Order #${cancelTarget.id}",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isArabic) "يرجى تحديد سبب الإلغاء للعميل:" else "Please enter reason for cancellation:",
                        fontSize = 12.5.sp
                    )
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        placeholder = { Text(if (isArabic) "مثال: نفاذ الكمية أو عدم الرد..." else "e.g. Out of stock...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalReason = cancelReason.ifBlank { if (isArabic) "تم الإلغاء من قبل إدارة المطعم" else "Cancelled by admin" }
                        BunzoRepository.updateOrderStatus(cancelTarget.id, "cancelled", finalReason)
                        Toast.makeText(
                            context,
                            if (isArabic) "تم إلغاء الطلب #${cancelTarget.id}" else "Order #${cancelTarget.id} cancelled",
                            Toast.LENGTH_SHORT
                        ).show()
                        orderToCancel = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusCancelled),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (isArabic) "تأكيد الإلغاء" else "Confirm Cancel", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { orderToCancel = null },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (isArabic) "تراجع" else "Dismiss")
                }
            }
        )
    }
}

@Composable
fun AdminStatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.12f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = color,
                    maxLines = 1
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        fontSize = 9.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun AdminOrderCard(
    order: Order,
    isArabic: Boolean,
    onAcceptOrder: () -> Unit,
    onMarkOnTheWay: () -> Unit,
    onMarkDelivered: () -> Unit,
    onCancelClick: () -> Unit
) {
    val context = LocalContext.current
    val isNew = order.status == "received"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = if (isNew) BorderStroke(1.5.dp, FlameOrange) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isNew) 3.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_order_card_${order.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: ID + Order Type Badge + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "#${order.id}",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Order Type Pill
                    val (typeIcon, typeLabel, typeColor) = when (order.parsedOrderType) {
                        OrderType.DELIVERY -> Triple(Icons.Default.TwoWheeler, if (isArabic) "توصيل" else "Delivery", FlameOrange)
                        OrderType.PICKUP -> Triple(Icons.Default.ShoppingBag, if (isArabic) "استلام" else "Pickup", AmberGoldDark)
                        OrderType.TABLE -> Triple(Icons.Default.Restaurant, if (isArabic) "صالة" else "Dine-in", StatusReceived)
                    }
                    Surface(
                        color = typeColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(typeIcon, contentDescription = null, tint = typeColor, modifier = Modifier.size(12.dp))
                            Text(text = typeLabel, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = typeColor)
                        }
                    }

                    if (isNew) {
                        Surface(
                            color = FlameOrange,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (isArabic) "جديد! ⚡" else "NEW!",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                StatusBadge(status = order.orderStatus, isArabic = isArabic)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatTimestamp(order.createdAt, isArabic),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Customer Contact Box (Direct Call / WhatsApp button for delivery riders)
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = FlameOrange.copy(alpha = 0.12f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = FlameOrange, modifier = Modifier.size(18.dp))
                            }
                        }
                        Column {
                            Text(
                                text = "${order.customerName} (${order.customerPhone})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                Text(
                                    text = "${order.customerRegion} - ${order.address}",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (order.tableNumber != null) {
                                Text(
                                    text = "طاولة رقم: ${order.tableNumber}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FlameOrange
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${order.customerPhone}"))
                                    context.startActivity(intent)
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Phone, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = StatusDelivered.copy(alpha = 0.15f),
                            modifier = Modifier
                                .size(32.dp)
                                .clickable {
                                    val uri = Uri.parse("https://api.whatsapp.com/send?phone=+963${order.customerPhone.removePrefix("0")}&text=${Uri.encode("مرحباً ${order.customerName}، معكم مطعم بنزو بخصوص طلبك #${order.id}")}")
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    context.startActivity(intent)
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = StatusDelivered, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Items List
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    order.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    color = FlameOrange.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "${item.qty}x",
                                        color = FlameOrange,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                                Text(text = item.name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                            Text(text = formatCurrency(item.price * item.qty, isArabic), fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                        }
                        if (item.notes.isNotBlank()) {
                            Surface(
                                color = AmberGold.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(start = 28.dp, bottom = 2.dp)
                            ) {
                                Text(
                                    text = "ملاحظة: ${item.notes}",
                                    fontSize = 10.5.sp,
                                    color = AmberGoldDark,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (order.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = AmberGold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.LocalOffer, contentDescription = null, tint = AmberGoldDark, modifier = Modifier.size(14.dp))
                        Text(
                            text = "ملاحظات الزبون: ${order.notes}",
                            fontSize = 11.5.sp,
                            color = AmberGoldDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (order.status == "cancelled" && !order.cancelReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = StatusCancelled.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "سبب الإلغاء: ${order.cancelReason}",
                        color = StatusCancelled,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            // Total and Sequential Workflow Actions
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isArabic) "الإجمالي: ${formatCurrency(order.total, isArabic)}" else "Total: ${formatCurrency(order.total, isArabic)}",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.5.sp,
                        color = FlameOrange
                    )
                    Text(
                        text = if (order.paymentMethod == "cash") (if (isArabic) "نقدًا عند الاستلام" else "Cash on Delivery") else (if (isArabic) "دفع إلكتروني" else "Electronic"),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Sequential Action Buttons per state
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    when (order.status) {
                        "received" -> {
                            Button(
                                onClick = onAcceptOrder,
                                colors = ButtonDefaults.buttonColors(containerColor = StatusDelivered),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("accept_order_${order.id}")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isArabic) "قبول وبدء التحضير" else "Accept", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = onCancelClick,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusCancelled),
                                border = BorderStroke(1.dp, StatusCancelled),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(if (isArabic) "رفض" else "Reject", fontSize = 11.5.sp)
                            }
                        }

                        "preparing" -> {
                            val nextLabel = when (order.parsedOrderType) {
                                OrderType.DELIVERY -> if (isArabic) "خرج للتوصيل 🛵" else "Dispatch"
                                OrderType.PICKUP -> if (isArabic) "جاهز للاستلام 🛍️" else "Ready"
                                OrderType.TABLE -> if (isArabic) "تقديم للطاولة 🍽️" else "Serve"
                            }
                            Button(
                                onClick = onMarkOnTheWay,
                                colors = ButtonDefaults.buttonColors(containerColor = StatusOnTheWay),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("dispatch_order_${order.id}")
                            ) {
                                Icon(Icons.Default.TwoWheeler, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(nextLabel, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }

                            IconButton(onClick = onCancelClick, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.Cancel, contentDescription = "Cancel", tint = StatusCancelled, modifier = Modifier.size(20.dp))
                            }
                        }

                        "on_the_way", "ready_for_pickup", "served" -> {
                            Button(
                                onClick = onMarkDelivered,
                                colors = ButtonDefaults.buttonColors(containerColor = StatusDelivered),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("delivered_order_${order.id}")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isArabic) "تم التسليم بنجاح ✓" else "Mark Delivered", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        "delivered" -> {
                            Surface(
                                color = StatusDelivered.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusDelivered, modifier = Modifier.size(14.dp))
                                    Text(
                                        text = if (isArabic) "مكتمل ومسلّم ✓" else "Completed ✓",
                                        color = StatusDelivered,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        "cancelled" -> {
                            Surface(
                                color = StatusCancelled.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = StatusCancelled, modifier = Modifier.size(14.dp))
                                    Text(
                                        text = if (isArabic) "ملغى ✕" else "Cancelled ✕",
                                        color = StatusCancelled,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 2: Menu Management (Categories & Products)
// -------------------------------------------------------------
@Composable
fun AdminMenuTab(isArabic: Boolean) {
    var subTab by remember { mutableIntStateOf(0) } // 0 = Products, 1 = Categories
    val categories by BunzoRepository.categories.collectAsState()
    val products by BunzoRepository.products.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<String?>("all") }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }
    var categoryToDeleteError by remember { mutableStateOf<String?>(null) }

    var showAddProductDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    val filteredProducts = remember(products, searchQuery, selectedCategoryFilter) {
        products.filter { p ->
            val matchCategory = selectedCategoryFilter == "all" || p.categoryId == selectedCategoryFilter
            val matchQuery = searchQuery.isBlank() ||
                    p.nameAr.contains(searchQuery, ignoreCase = true) ||
                    p.nameEn.contains(searchQuery, ignoreCase = true) ||
                    p.descriptionAr.contains(searchQuery, ignoreCase = true)
            matchCategory && matchQuery
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_menu_tab")
    ) {
        // Sub-tabs & Add Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                Surface(
                    color = if (subTab == 0) FlameOrange else Color.Transparent,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.clickable { subTab = 0 }
                ) {
                    Text(
                        text = if (isArabic) "الوجبات (${products.size})" else "Products (${products.size})",
                        color = if (subTab == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }

                Surface(
                    color = if (subTab == 1) FlameOrange else Color.Transparent,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.clickable { subTab = 1 }
                ) {
                    Text(
                        text = if (isArabic) "الأصناف (${categories.size})" else "Categories (${categories.size})",
                        color = if (subTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }

            Button(
                onClick = {
                    if (subTab == 0) showAddProductDialog = true else showAddCategoryDialog = true
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FlameOrange),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.testTag("admin_add_item_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (subTab == 0) (if (isArabic) "إضافة وجبة" else "Add Meal") else (if (isArabic) "إضافة صنف" else "Add Category"),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (categoryToDeleteError != null) {
            Surface(
                color = StatusCancelled.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = categoryToDeleteError!!,
                        color = StatusCancelled,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { categoryToDeleteError = null },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = StatusCancelled, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // SubTab 0: Products list
        if (subTab == 0) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (isArabic) "ابحث باسم الوجبة أو المكونات..." else "Search meal name...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.outline) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                )

                // Category Filter Chips
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == "all",
                            onClick = { selectedCategoryFilter = "all" },
                            label = { Text(if (isArabic) "الكل (${products.size})" else "All (${products.size})", fontSize = 11.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FlameOrange,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    items(categories.sortedBy { it.order }, key = { it.id }) { cat ->
                        val count = products.count { it.categoryId == cat.id }
                        FilterChip(
                            selected = selectedCategoryFilter == cat.id,
                            onClick = { selectedCategoryFilter = cat.id },
                            label = { Text("${if (isArabic) cat.nameAr else cat.nameEn} ($count)", fontSize = 11.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FlameOrange,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                if (filteredProducts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.RestaurantMenu,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isArabic) "لا توجد وجبات تطابق البحث" else "No meals matching search",
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 4.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredProducts, key = { it.id }) { product ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (product.isAvailable) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (product.isAvailable) 2.dp else 0.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Image Thumbnail
                                    Box(
                                        modifier = Modifier
                                            .size(62.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        BunzoProductImage(
                                            imageUrl = product.image,
                                            imageRes = product.imageRes,
                                            contentDescription = if (isArabic) product.nameAr else product.nameEn,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        if (!product.isAvailable) {
                                            Surface(
                                                color = Color.Black.copy(alpha = 0.6f),
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = if (isArabic) "نفد" else "Sold",
                                                        color = Color.White,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Meal Information
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = if (isArabic) product.nameAr else product.nameEn,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.5.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (product.isFeatured) {
                                                Surface(
                                                    color = AmberGoldDark.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = if (isArabic) "مميز" else "Featured",
                                                        color = AmberGoldDark,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }

                                        // Pricing row
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = formatCurrency(product.effectivePrice, isArabic),
                                                color = FlameOrange,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            if (product.discountPrice != null && product.discountPrice > 0) {
                                                Text(
                                                    text = formatCurrency(product.price, isArabic),
                                                    color = MaterialTheme.colorScheme.outline,
                                                    fontSize = 11.sp,
                                                    style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                                                )
                                            }
                                        }

                                        val catName = categories.find { it.id == product.categoryId }?.let { if (isArabic) it.nameAr else it.nameEn } ?: (if (isArabic) "عام" else "General")
                                        Text(
                                            text = "${if (isArabic) "الصنف:" else "Category:"} $catName",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Availability Toggle
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = if (product.isAvailable) (if (isArabic) "متاح" else "Active") else (if (isArabic) "موقوف" else "Paused"),
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (product.isAvailable) StatusDelivered else MaterialTheme.colorScheme.outline
                                        )
                                        Switch(
                                            checked = product.isAvailable,
                                            onCheckedChange = { BunzoRepository.toggleProductAvailability(product.id) },
                                            modifier = Modifier.testTag("toggle_avail_${product.id}")
                                        )
                                    }

                                    // Edit & Delete Actions
                                    IconButton(
                                        onClick = { productToEdit = product },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    }

                                    IconButton(
                                        onClick = { productToDelete = product },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = StatusCancelled, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // SubTab 1: Categories list
            LazyColumn(
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(categories.sortedBy { it.order }, key = { it.id }) { cat ->
                    val linkedProductsCount = products.count { it.categoryId == cat.id }
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = FlameOrange.copy(alpha = 0.12f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "#${cat.order}",
                                            fontWeight = FontWeight.Bold,
                                            color = FlameOrange,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = "${cat.nameAr} - ${cat.nameEn}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp
                                    )
                                    Text(
                                        text = if (isArabic) "عدد الوجبات المرتبطة: $linkedProductsCount" else "Linked meals: $linkedProductsCount",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { categoryToEdit = cat }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Category", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }
                                IconButton(
                                    onClick = {
                                        val res = BunzoRepository.deleteCategory(cat.id)
                                        res.onFailure {
                                            categoryToDeleteError = it.message
                                        }.onSuccess {
                                            categoryToDeleteError = null
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Category", tint = StatusCancelled, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Product Delete Confirmation Dialog
    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text(if (isArabic) "تأكيد حذف الوجبة" else "Delete Meal Confirmation") },
            text = {
                Text(
                    if (isArabic)
                        "هل أنت متأكد من حذف وجبة '${productToDelete?.nameAr}'؟ سيتم إزالتها من القائمة نهائياً."
                    else
                        "Are you sure you want to delete '${productToDelete?.nameEn}'?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        productToDelete?.let { BunzoRepository.deleteProduct(it.id) }
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusCancelled)
                ) {
                    Text(if (isArabic) "حذف" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text(if (isArabic) "إلغاء" else "Cancel")
                }
            }
        )
    }

    // Add / Edit Category Dialog
    if (showAddCategoryDialog || categoryToEdit != null) {
        val isEditing = categoryToEdit != null
        var nameAr by remember { mutableStateOf(categoryToEdit?.nameAr ?: "") }
        var nameEn by remember { mutableStateOf(categoryToEdit?.nameEn ?: "") }
        var order by remember { mutableIntStateOf(categoryToEdit?.order ?: (categories.size + 1)) }

        Dialog(onDismissRequest = {
            showAddCategoryDialog = false
            categoryToEdit = null
        }) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (isEditing) (if (isArabic) "تعديل الصنف" else "Edit Category") else (if (isArabic) "إضافة صنف جديد" else "Add New Category"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = nameAr,
                        onValueChange = { nameAr = it },
                        label = { Text(if (isArabic) "الاسم بالعربية *" else "Name in Arabic *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nameEn,
                        onValueChange = { nameEn = it },
                        label = { Text(if (isArabic) "الاسم بالإنجليزية" else "Name in English") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = order.toString(),
                        onValueChange = { order = it.toIntOrNull() ?: order },
                        label = { Text(if (isArabic) "رقم الترتيب في القائمة" else "Display Order") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            showAddCategoryDialog = false
                            categoryToEdit = null
                        }) {
                            Text(if (isArabic) "إلغاء" else "Cancel")
                        }
                        Button(
                            onClick = {
                                if (nameAr.isNotBlank()) {
                                    if (isEditing) {
                                        BunzoRepository.updateCategory(categoryToEdit!!.id, nameAr, nameEn.ifBlank { nameAr }, order)
                                    } else {
                                        BunzoRepository.addCategory(nameAr, nameEn.ifBlank { nameAr }, order)
                                    }
                                    showAddCategoryDialog = false
                                    categoryToEdit = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FlameOrange)
                        ) {
                            Text(if (isEditing) (if (isArabic) "حفظ" else "Save") else (if (isArabic) "إضافة" else "Add"))
                        }
                    }
                }
            }
        }
    }

    // Add or Edit Product Dialog
    val editingProduct = productToEdit
    if (showAddProductDialog || editingProduct != null) {
        var nameAr by remember { mutableStateOf(editingProduct?.nameAr ?: "") }
        var nameEn by remember { mutableStateOf(editingProduct?.nameEn ?: "") }
        var descAr by remember { mutableStateOf(editingProduct?.descriptionAr ?: "") }
        var descEn by remember { mutableStateOf(editingProduct?.descriptionEn ?: "") }
        var priceStr by remember { mutableStateOf(editingProduct?.price?.toLong()?.toString() ?: "") }
        var discountPriceStr by remember { mutableStateOf(editingProduct?.discountPrice?.toLong()?.toString() ?: "") }
        var selectedCatId by remember { mutableStateOf(editingProduct?.categoryId ?: categories.firstOrNull()?.id ?: "") }
        var isAvailable by remember { mutableStateOf(editingProduct?.isAvailable ?: true) }
        var isFeatured by remember { mutableStateOf(editingProduct?.isFeatured ?: false) }
        var imageUrl by remember { mutableStateOf(editingProduct?.image ?: "") }
        var selectedImageRes by remember { mutableStateOf<Int?>(editingProduct?.imageRes ?: R.drawable.img_burger_combo) }
        var showUrlInput by remember { mutableStateOf(false) }

        // Standard zero-permission photo picker launcher
        val photoPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->
            if (uri != null) {
                imageUrl = uri.toString()
            }
        }

        val presetImages = listOf(
            Pair(R.drawable.img_burger_combo, if (isArabic) "كومبو برغر" else "Burger Combo"),
            Pair(R.drawable.img_bunzo_banner, if (isArabic) "بانر بونزو" else "Bunzo Special"),
            Pair(R.drawable.img_bunzo_logo, if (isArabic) "شعار بونزو" else "Bunzo Logo")
        )

        Dialog(onDismissRequest = {
            showAddProductDialog = false
            productToEdit = null
        }) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Text(
                        text = if (editingProduct == null) (if (isArabic) "إضافة وجبة جديدة" else "Add New Meal") else (if (isArabic) "تعديل الوجبة" else "Edit Meal"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Image Selection and Preview Section
                    Text(
                        text = if (isArabic) "صورة الوجبة" else "Meal Image",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                    ) {
                        BunzoProductImage(
                            imageUrl = imageUrl,
                            imageRes = selectedImageRes,
                            contentDescription = "Meal Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Floating action to pick/change image
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.65f),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Pick Image",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isArabic) "تغيير الصورة" else "Change Photo",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (imageUrl.isNotBlank()) {
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.65f),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                            ) {
                                IconButton(
                                    onClick = { imageUrl = "" },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove custom image",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Buttons for Photo Picker and URL input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isArabic) "من المعرض" else "From Gallery", fontSize = 11.5.sp)
                        }

                        OutlinedButton(
                            onClick = { showUrlInput = !showUrlInput },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isArabic) "رابط صورة" else "Image URL", fontSize = 11.5.sp)
                        }
                    }

                    if (showUrlInput) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = imageUrl,
                            onValueChange = { imageUrl = it },
                            label = { Text(if (isArabic) "رابط الصورة (URL أو مسار)" else "Image URL / Path") },
                            placeholder = { Text("https://example.com/meal.jpg") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset Quick Pickers
                    Text(
                        text = if (isArabic) "أو اختر صورة جاهزة:" else "Or pick a preset image:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetImages.forEach { (resId, label) ->
                            val isSelected = (imageUrl.isBlank() && selectedImageRes == resId)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) FlameOrange.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isSelected) BorderStroke(1.5.dp, FlameOrange) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedImageRes = resId
                                        imageUrl = "" // Clear custom uri/url to use preset
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = resId),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) FlameOrange else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category Selector Chips
                    Text(
                        text = if (isArabic) "اختيار الصنف / الفئة *" else "Select Category *",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories.sortedBy { it.order }, key = { it.id }) { cat ->
                            val isSelected = (selectedCatId == cat.id)
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCatId = cat.id },
                                label = { Text(if (isArabic) cat.nameAr else cat.nameEn, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FlameOrange,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = nameAr,
                        onValueChange = { nameAr = it },
                        label = { Text(if (isArabic) "اسم الوجبة (عربي) *" else "Meal Name (Arabic) *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nameEn,
                        onValueChange = { nameEn = it },
                        label = { Text("Meal Name (English)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = descAr,
                        onValueChange = { descAr = it },
                        label = { Text(if (isArabic) "الوصف والمكونات (عربي)" else "Description (Arabic)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = priceStr,
                            onValueChange = { priceStr = it },
                            label = { Text(if (isArabic) "السعر (ل.س)" else "Price") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = discountPriceStr,
                            onValueChange = { discountPriceStr = it },
                            label = { Text(if (isArabic) "سعر التخفيض" else "Discount Price") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(checked = isAvailable, onCheckedChange = { isAvailable = it })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (isArabic) "متاح للطلب" else "Available", fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(checked = isFeatured, onCheckedChange = { isFeatured = it })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (isArabic) "مميز في الرئيسية" else "Featured", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            showAddProductDialog = false
                            productToEdit = null
                        }) {
                            Text(if (isArabic) "إلغاء" else "Cancel")
                        }
                        Button(
                            onClick = {
                                val price = priceStr.toDoubleOrNull() ?: 0.0
                                val discountPrice = discountPriceStr.toDoubleOrNull()
                                if (nameAr.isBlank() || price <= 0) {
                                    return@Button
                                }
                                val effectiveCatId = selectedCatId.ifBlank { categories.firstOrNull()?.id ?: "general" }
                                if (editingProduct == null) {
                                    BunzoRepository.addProduct(
                                        nameAr = nameAr,
                                        nameEn = nameEn.ifBlank { nameAr },
                                        descriptionAr = descAr,
                                        descriptionEn = descEn.ifBlank { descAr },
                                        price = price,
                                        discountPrice = discountPrice,
                                        categoryId = effectiveCatId,
                                        isAvailable = isAvailable,
                                        isFeatured = isFeatured,
                                        image = imageUrl,
                                        imageRes = selectedImageRes
                                    )
                                } else {
                                    BunzoRepository.updateProduct(
                                        id = editingProduct.id,
                                        nameAr = nameAr,
                                        nameEn = nameEn.ifBlank { nameAr },
                                        descriptionAr = descAr,
                                        descriptionEn = descEn.ifBlank { descAr },
                                        price = price,
                                        discountPrice = discountPrice,
                                        categoryId = effectiveCatId,
                                        isAvailable = isAvailable,
                                        isFeatured = isFeatured,
                                        image = imageUrl,
                                        imageRes = selectedImageRes
                                    )
                                }
                                showAddProductDialog = false
                                productToEdit = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FlameOrange)
                        ) {
                            Text(if (isArabic) "حفظ الوجبة" else "Save Meal", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 3: Customers Management
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCustomerEditDialog(
    customer: User,
    regions: List<Region>,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var firstName by remember(customer) { mutableStateOf(customer.firstName) }
    var lastName by remember(customer) { mutableStateOf(customer.lastName) }
    var phone by remember(customer) { mutableStateOf(customer.phone) }
    var region by remember(customer) { mutableStateOf(customer.region) }
    var address by remember(customer) { mutableStateOf(customer.address) }
    var isRegionDropdownExpanded by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = FlameOrange.copy(alpha = 0.12f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = FlameOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = if (isArabic) "تعديل بيانات العميل" else "Edit Customer Details",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "UID: ${customer.uid.takeLast(8)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.outline)
                    }
                }

                Divider()

                if (errorMessage != null) {
                    Surface(
                        color = StatusCancelled.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = StatusCancelled,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // First and Last Name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text(if (isArabic) "الاسم الأول *" else "First Name *", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text(if (isArabic) "اسم العائلة *" else "Last Name *", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Phone Number
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(if (isArabic) "رقم الهاتف / الحساب *" else "Phone / Account *", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = FlameOrange, modifier = Modifier.size(18.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Region Selection / Input
                Column {
                    Text(
                        text = if (isArabic) "المنطقة / الحي *:" else "Region / District *:",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = isRegionDropdownExpanded,
                        onExpandedChange = { isRegionDropdownExpanded = !isRegionDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = region,
                            onValueChange = {
                                region = it
                                isRegionDropdownExpanded = true
                            },
                            placeholder = { Text(if (isArabic) "اختر أو اكتب المنطقة..." else "Select or type area...", fontSize = 12.sp) },
                            singleLine = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isRegionDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        val filteredRegions = if (region.isBlank()) regions else regions.filter {
                            it.nameAr.contains(region, ignoreCase = true) || it.nameEn.contains(region, ignoreCase = true)
                        }
                        ExposedDropdownMenu(
                            expanded = isRegionDropdownExpanded,
                            onDismissRequest = { isRegionDropdownExpanded = false }
                        ) {
                            filteredRegions.forEach { reg ->
                                val regName = if (isArabic) reg.nameAr else reg.nameEn
                                DropdownMenuItem(
                                    text = { Text(regName) },
                                    onClick = {
                                        region = regName
                                        isRegionDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Full Address
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(if (isArabic) "العنوان التفصيلي" else "Full Detailed Address", fontSize = 12.sp) },
                    placeholder = { Text(if (isArabic) "الشارع، البناء، الطابق، علامة مميزة..." else "Street, building, landmark...", fontSize = 11.sp) },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Customer Password Section (View & Modify by Admin)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, FlameOrange.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = FlameOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isArabic) "إدارة كلمة مرور العميل" else "Customer Password Management",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                            }
                            IconButton(
                                onClick = { showPassword = !showPassword },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Password Visibility",
                                    tint = FlameOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isArabic) "كلمة المرور الحالية:" else "Current Stored Password:",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (showPassword) {
                                    customer.password.ifBlank { if (isArabic) "(افتراضية: 123456)" else "(Default: 123456)" }
                                } else {
                                    if (customer.password.isNotBlank()) "••••••••" else if (isArabic) "(افتراضية: 123456)" else "(Default: 123456)"
                                },
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text(if (isArabic) "تعيين كلمة مرور جديدة للعميل (اختياري)" else "Set New Password (Optional)", fontSize = 11.sp) },
                            placeholder = { Text(if (isArabic) "أدخل 6 خانات أو أكثر لتغييرها..." else "Enter 6+ characters to change...", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = FlameOrange, modifier = Modifier.size(16.dp)) },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                // Action Buttons
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isArabic) "إلغاء" else "Cancel")
                    }
                    Button(
                        onClick = {
                            if (firstName.isBlank() || lastName.isBlank()) {
                                errorMessage = if (isArabic) "يرجى كتابة الاسم الأول واسم العائلة" else "First and last name are required"
                                return@Button
                            }
                            if (phone.isBlank()) {
                                errorMessage = if (isArabic) "يرجى كتابة رقم الهاتف" else "Phone number is required"
                                return@Button
                            }
                            if (newPassword.isNotBlank() && newPassword.length < 6) {
                                errorMessage = if (isArabic) "يجب أن تكون كلمة المرور 6 أحرف أو أرقام على الأقل" else "Password must be at least 6 characters"
                                return@Button
                            }

                            isSaving = true
                            errorMessage = null
                            coroutineScope.launch {
                                val res = BunzoRepository.adminUpdateCustomer(
                                    uid = customer.uid,
                                    firstName = firstName,
                                    lastName = lastName,
                                    phone = phone,
                                    region = region,
                                    address = address,
                                    newPassword = if (newPassword.isNotBlank()) newPassword else null
                                )
                                isSaving = false
                                res.onSuccess {
                                    Toast.makeText(
                                        context,
                                        if (isArabic) "تم تحديث بيانات العميل بنجاح" else "Customer updated successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onSaved()
                                }.onFailure { err ->
                                    errorMessage = err.message ?: (if (isArabic) "فشل التحديث" else "Update failed")
                                }
                            }
                        },
                        enabled = !isSaving,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FlameOrange),
                        modifier = Modifier.weight(1.4f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "حفظ التعديلات" else "Save Changes",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminCustomersTab(isArabic: Boolean) {
    val coroutineScope = rememberCoroutineScope()
    val users by BunzoRepository.users.collectAsState()
    val orders by BunzoRepository.orders.collectAsState()
    val regions by BunzoRepository.regions.collectAsState()
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedRegionFilter by remember { mutableStateOf<String?>(null) }
    var customerToEdit by remember { mutableStateOf<User?>(null) }
    var customerToDelete by remember { mutableStateOf<User?>(null) }

    val customers = remember(users, orders, searchQuery, selectedRegionFilter) {
        val registeredCustomers = users.filter { it.role.isBlank() || it.role.equals("customer", ignoreCase = true) }

        // Also derive any customers from placed orders who aren't yet in registered list
        val orderCustomers = orders.mapNotNull { ord ->
            if (ord.customerPhone.isNotBlank() && registeredCustomers.none { it.phone == ord.customerPhone }) {
                val nameParts = ord.customerName.trim().split(" ")
                User(
                    uid = ord.customerId.ifBlank { "order_${ord.customerPhone.filter { it.isDigit() }}" },
                    firstName = nameParts.firstOrNull() ?: ord.customerName,
                    lastName = nameParts.drop(1).joinToString(" "),
                    phone = ord.customerPhone,
                    region = ord.customerRegion,
                    address = ord.address,
                    role = "customer",
                    createdAt = ord.createdAt,
                    updatedAt = ord.updatedAt
                )
            } else null
        }.distinctBy { it.phone }

        val allCustomers = (registeredCustomers + orderCustomers).distinctBy { if (it.phone.isNotBlank()) it.phone else it.uid }

        allCustomers.filter { user ->
            val matchRegion = selectedRegionFilter == null || user.region == selectedRegionFilter
            val matchQuery = searchQuery.isBlank() ||
                    user.fullName.contains(searchQuery, ignoreCase = true) ||
                    user.phone.contains(searchQuery, ignoreCase = true) ||
                    user.address.contains(searchQuery, ignoreCase = true)
            matchRegion && matchQuery
        }.sortedByDescending { it.updatedAt }
    }

    LazyColumn(
        contentPadding = PaddingValues(14.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_customers_list")
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isArabic) "قائمة العملاء المسجلين (${customers.size})" else "Registered Customers (${customers.size})",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (isArabic) "ابحث باسم العميل أو رقم هاتفه..." else "Search customer name or phone...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
        }

        // Region Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedRegionFilter == null,
                        onClick = { selectedRegionFilter = null },
                        label = { Text(if (isArabic) "كل المناطق" else "All Regions", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FlameOrange,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                val availableRegions = regions.map { if (isArabic) it.nameAr else it.nameEn }.distinct()
                items(availableRegions) { regName ->
                    FilterChip(
                        selected = selectedRegionFilter == regName,
                        onClick = { selectedRegionFilter = if (selectedRegionFilter == regName) null else regName },
                        label = { Text(regName, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FlameOrange,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        if (customers.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.PeopleOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isArabic) "لا يوجد عملاء يطابقون شروط البحث" else "No customers found matching search",
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        items(customers, key = { it.uid.ifBlank { it.phone } }) { cust ->
            val custOrders = orders.filter { it.customerId == cust.uid || (cust.phone.isNotBlank() && it.customerPhone == cust.phone) }
            val totalSpent = custOrders.filter { it.status != "cancelled" }.sumOf { it.total }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = cust.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = cust.phone, fontSize = 12.5.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Edit Customer Button
                            IconButton(
                                onClick = { customerToEdit = cust }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Customer", tint = FlameOrange)
                            }
                            IconButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${cust.phone}"))
                                    context.startActivity(intent)
                                }
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(
                                onClick = {
                                    val uri = Uri.parse("https://api.whatsapp.com/send?phone=+963${cust.phone.removePrefix("0")}&text=${Uri.encode("مرحباً ${cust.firstName}، من مطعم بنزو!")}")
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    context.startActivity(intent)
                                }
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = StatusDelivered)
                            }
                            IconButton(
                                onClick = { customerToDelete = cust }
                            ) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Delete Customer", tint = StatusCancelled)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${cust.region} - ${cust.address}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Password Info Row for Admin
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = FlameOrange,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (isArabic) "كلمة المرور:" else "Password:",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = cust.password.ifBlank { if (isArabic) "(افتراضية: 123456)" else "(Default: 123456)" },
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (cust.password.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                            }
                            TextButton(
                                onClick = { customerToEdit = cust },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(12.dp), tint = FlameOrange)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isArabic) "تعديل" else "Edit", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FlameOrange)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isArabic) "إجمالي الطلبات: ${custOrders.size}" else "Orders: ${custOrders.size}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isArabic) "مجموع المشتريات: ${formatCurrency(totalSpent, isArabic)}" else "Spent: ${formatCurrency(totalSpent, isArabic)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = FlameOrange
                        )
                    }
                }
            }
        }
    }

    // Dialog for editing customer data & password
    if (customerToEdit != null) {
        AdminCustomerEditDialog(
            customer = customerToEdit!!,
            regions = regions,
            isArabic = isArabic,
            onDismiss = { customerToEdit = null },
            onSaved = { customerToEdit = null }
        )
    }

    // Dialog for confirming customer deletion
    if (customerToDelete != null) {
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            title = { Text(if (isArabic) "تأكيد حذف العميل" else "Confirm Delete Customer") },
            text = {
                Text(
                    if (isArabic)
                        "هل أنت متأكد من حذف حساب العميل ${customerToDelete?.fullName} (${customerToDelete?.phone})؟"
                    else
                        "Are you sure you want to delete ${customerToDelete?.fullName} (${customerToDelete?.phone})?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toDel = customerToDelete
                        if (toDel != null) {
                            coroutineScope.launch {
                                BunzoRepository.adminDeleteCustomer(toDel.uid)
                                Toast.makeText(
                                    context,
                                    if (isArabic) "تم حذف حساب العميل" else "Customer deleted",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        customerToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusCancelled)
                ) {
                    Text(if (isArabic) "حذف" else "Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { customerToDelete = null }) {
                    Text(if (isArabic) "إلغاء" else "Cancel")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// TAB 4: Settings, Branches, Coupons & Firestore Security Rules
// -------------------------------------------------------------
@Composable
fun AdminSettingsTab(isArabic: Boolean) {
    val regions by BunzoRepository.regions.collectAsState()
    val coupons by BunzoRepository.coupons.collectAsState()
    val branches by BunzoRepository.branches.collectAsState()
    val context = LocalContext.current

    var newRegionName by remember { mutableStateOf("") }
    var newCouponCode by remember { mutableStateOf("") }
    var newCouponPercent by remember { mutableIntStateOf(15) }
    var newCouponMinOrder by remember { mutableDoubleStateOf(20000.0) }
    var isCopiedRulesToast by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(14.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_settings_tab")
    ) {
        // Section 1: Delivery Regions Management
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isArabic) "إدارة مناطق التوصيل في حلب" else "Manage Delivery Regions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Add region row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newRegionName,
                            onValueChange = { newRegionName = it },
                            placeholder = { Text(if (isArabic) "اسم الحي (مثال: السليمانية)" else "Region name...", fontSize = 12.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                if (newRegionName.isNotBlank()) {
                                    BunzoRepository.addRegion(newRegionName, newRegionName)
                                    newRegionName = ""
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FlameOrange)
                        ) {
                            Text(if (isArabic) "إضافة" else "Add")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    regions.forEach { reg ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "• ${reg.nameAr}", fontSize = 13.sp)
                            IconButton(
                                onClick = { BunzoRepository.deleteRegion(reg.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Delete", tint = StatusCancelled, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Coupons Management
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isArabic) "إدارة كوبونات الخصم" else "Manage Promo Coupons",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newCouponCode,
                            onValueChange = { newCouponCode = it },
                            placeholder = { Text("BUNZO50", fontSize = 12.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                if (newCouponCode.isNotBlank()) {
                                    BunzoRepository.addCoupon(newCouponCode, newCouponPercent, newCouponMinOrder)
                                    newCouponCode = ""
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AmberGoldDark)
                        ) {
                            Text(if (isArabic) "إضافة كوبون" else "Add")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    coupons.forEach { c ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = c.code, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = FlameOrange)
                                Text(
                                    text = "خصم ${c.discountPercent}% - أدنى طلب ${c.minOrder.toInt()} ل.س",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = c.isActive,
                                    onCheckedChange = { BunzoRepository.toggleCouponActive(c.id) }
                                )
                                IconButton(onClick = { BunzoRepository.deleteCoupon(c.id) }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = StatusCancelled, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Branches
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isArabic) "فروع مطعم بنـزو" else "Bunzo Branches",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    branches.forEach { b ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(text = "📍 ${b.name}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = b.address, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "ساعات العمل: ${b.workingHours} | هاتف: ${b.phone}", fontSize = 11.sp, color = FlameOrange)
                        }
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }

        // Section 4: Firestore Security Rules Viewer (User prompt explicitly requested this!)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isArabic) "قواعد أمان Firestore (Security Rules)" else "Firestore Security Rules",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isArabic) "جاهزة للنسخ في Firebase Console لحماية البيانات" else "Production-ready security rules",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Firestore Rules", BunzoRepository.firestoreSecurityRules)
                                clipboard.setPrimaryClip(clip)
                                isCopiedRulesToast = true
                                Toast.makeText(context, "تم نسخ قواعد Firestore إلى الحافظة بنجاح!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = FlameOrange, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isArabic) "نسخ القواعد" else "Copy Rules",
                                color = FlameOrange,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = Color(0xFF1E1E1E),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = BunzoRepository.firestoreSecurityRules,
                            color = Color(0xFF98C379),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Admin Audit Log Tab
// -------------------------------------------------------------
@Composable
fun AdminAuditLogTab(
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    val auditLogs by BunzoRepository.auditLogs.collectAsState()
    val timeFormat = remember { java.text.SimpleDateFormat("yyyy-MM-dd hh:mm a", java.util.Locale.getDefault()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_audit_log_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                        tint = FlameOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isArabic) "سجل أمان العمليات والتدقيق (Security Audit Log)" else "Security Audit Log",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isArabic) "يتم تسجيل جميع عمليات الإدارة والمطبخ وتغيير الحالات آلياً" else "All staff actions and role changes are recorded",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (auditLogs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isArabic) "لا توجد سجلات عمليات حتى الآن" else "No audit logs yet",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(auditLogs, key = { it.id }) { log ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = when (log.action) {
                                    "STAFF_LOGIN" -> FlameOrange.copy(alpha = 0.15f)
                                    "STAFF_LOGOUT" -> StatusCancelled.copy(alpha = 0.15f)
                                    else -> StatusDelivered.copy(alpha = 0.15f)
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        when (log.action) {
                                            "STAFF_LOGIN" -> Icons.Default.Login
                                            "STAFF_LOGOUT" -> Icons.Default.Logout
                                            else -> Icons.Default.CheckCircle
                                        },
                                        contentDescription = null,
                                        tint = when (log.action) {
                                            "STAFF_LOGIN" -> FlameOrange
                                            "STAFF_LOGOUT" -> StatusCancelled
                                            else -> StatusDelivered
                                        },
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = log.details,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "${log.actorName} (${log.actorRole}) • ${timeFormat.format(java.util.Date(log.timestamp))}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
