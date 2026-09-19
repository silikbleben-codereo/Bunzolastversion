package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.CartItem
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.UserRole
import com.example.data.repository.BunzoRepository
import com.example.ui.admin.AdminDashboardContent
import com.example.ui.components.BunzoTopAppBar
import com.example.ui.customer.*
import com.example.ui.kitchen.KitchenKdsScreen
import com.example.ui.staff.StaffLoginScreen
import com.example.ui.theme.BunzoTheme
import com.example.ui.theme.FlameOrange
import com.example.util.BunzoSoundManager
import kotlinx.coroutines.flow.collectLatest

enum class AppDestination {
    CUSTOMER_MENU,
    CUSTOMER_CART,
    CUSTOMER_TRACKING,
    CUSTOMER_HISTORY,
    CUSTOMER_PROFILE,
    STAFF_LOGIN,
    ADMIN_DASHBOARD,
    KITCHEN_KDS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BunzoRepository.initAppContext(applicationContext)
        BunzoSoundManager.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            BunzoAppRoot()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        BunzoSoundManager.release()
    }
}

@Composable
fun BunzoAppRoot() {
    val isArabic by BunzoRepository.isArabic.collectAsState()
    val isDark by BunzoRepository.isDarkMode.collectAsState()

    // Global layout direction: RTL for Arabic, LTR for English
    val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

    BunzoTheme(darkTheme = isDark) {
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            BunzoMainScreen(
                isArabic = isArabic,
                isDark = isDark,
                onToggleLang = { BunzoRepository.isArabic.value = !isArabic },
                onToggleTheme = { BunzoRepository.isDarkMode.value = !isDark }
            )
        }
    }
}

@Composable
fun BunzoMainScreen(
    isArabic: Boolean,
    isDark: Boolean,
    onToggleLang: () -> Unit,
    onToggleTheme: () -> Unit
) {
    val context = LocalContext.current
    val isSoundEnabled by BunzoRepository.isSoundEnabled.collectAsState()
    val staffSession by BunzoRepository.staffSession.collectAsState()
    val initialDestination = remember {
        val staff = BunzoRepository.staffSession.value
        when (staff?.userRole) {
            UserRole.ADMIN -> AppDestination.ADMIN_DASHBOARD
            UserRole.KITCHEN -> AppDestination.KITCHEN_KDS
            else -> AppDestination.CUSTOMER_MENU
        }
    }
    var currentDestination by remember { mutableStateOf(initialDestination) }
    var trackingOrderId by remember { mutableStateOf<String?>(null) }

    // Request POST_NOTIFICATIONS permission on Android 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            // Notification permission granted or denied
        }
        LaunchedEffect(Unit) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Cart items state
    var cartItems by remember { mutableStateOf<List<CartItem>>(emptyList()) }

    // Dialogs state
    var selectedProductForDetail by remember { mutableStateOf<Product?>(null) }
    var showAuthDialog by remember { mutableStateOf(false) }

    // Listen for new orders alerts (for real-time staff notifications)
    LaunchedEffect(Unit) {
        BunzoRepository.newOrderAlert.collectLatest { newOrder ->
            Toast.makeText(
                context,
                if (isArabic) "🔔 طلب جديد وصل: #${newOrder.id} بقيمة ${newOrder.total.toInt()} ل.س"
                else "🔔 New Order Received: #${newOrder.id}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val cartTotalCount = remember(cartItems) { cartItems.sumOf { it.quantity } }

    val isStaffOrKitchenScreen = currentDestination in listOf(
        AppDestination.STAFF_LOGIN,
        AppDestination.ADMIN_DASHBOARD,
        AppDestination.KITCHEN_KDS
    )

    Scaffold(
        topBar = {
            if (!isStaffOrKitchenScreen) {
                BunzoTopAppBar(
                    title = if (isArabic) "بنـزو" else "Bunzo",
                    isArabic = isArabic,
                    isDark = isDark,
                    cartCount = cartTotalCount,
                    isSoundEnabled = isSoundEnabled,
                    onToggleSound = { BunzoRepository.toggleSound() },
                    onToggleLang = onToggleLang,
                    onToggleTheme = onToggleTheme,
                    onOpenCart = {
                        BunzoSoundManager.playClick()
                        currentDestination = AppDestination.CUSTOMER_CART
                    },
                    onOpenProfile = {
                        BunzoSoundManager.playClick()
                        currentDestination = AppDestination.CUSTOMER_PROFILE
                    },
                    onLogoClick = {
                        BunzoSoundManager.playClick()
                    },
                    modifier = Modifier.testTag("bunzo_top_app_bar")
                )
            }
        },
        bottomBar = {
            if (!isStaffOrKitchenScreen) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.testTag("bunzo_bottom_nav")
                ) {
                    NavigationBarItem(
                        selected = currentDestination == AppDestination.CUSTOMER_MENU,
                        onClick = {
                            BunzoSoundManager.playClick()
                            currentDestination = AppDestination.CUSTOMER_MENU
                        },
                        icon = { Icon(Icons.Default.RestaurantMenu, contentDescription = null) },
                        label = { Text(if (isArabic) "المنيو" else "Menu", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = FlameOrange, selectedTextColor = FlameOrange)
                    )

                    NavigationBarItem(
                        selected = currentDestination == AppDestination.CUSTOMER_CART,
                        onClick = {
                            BunzoSoundManager.playClick()
                            currentDestination = AppDestination.CUSTOMER_CART
                        },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (cartTotalCount > 0) {
                                        Badge(containerColor = FlameOrange, contentColor = Color.White) {
                                            Text(cartTotalCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null)
                            }
                        },
                        label = { Text(if (isArabic) "السلة" else "Cart", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = FlameOrange, selectedTextColor = FlameOrange)
                    )

                    NavigationBarItem(
                        selected = currentDestination == AppDestination.CUSTOMER_TRACKING,
                        onClick = {
                            BunzoSoundManager.playClick()
                            currentDestination = AppDestination.CUSTOMER_TRACKING
                        },
                        icon = { Icon(Icons.Default.DirectionsBike, contentDescription = null) },
                        label = { Text(if (isArabic) "التتبع" else "Tracking", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = FlameOrange, selectedTextColor = FlameOrange)
                    )

                    NavigationBarItem(
                        selected = currentDestination == AppDestination.CUSTOMER_HISTORY,
                        onClick = {
                            BunzoSoundManager.playClick()
                            currentDestination = AppDestination.CUSTOMER_HISTORY
                        },
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
                        label = { Text(if (isArabic) "طلباتي" else "Orders", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = FlameOrange, selectedTextColor = FlameOrange)
                    )

                    NavigationBarItem(
                        selected = currentDestination == AppDestination.CUSTOMER_PROFILE,
                        onClick = {
                            BunzoSoundManager.playClick()
                            currentDestination = AppDestination.CUSTOMER_PROFILE
                        },
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text(if (isArabic) "حسابي" else "Profile", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = FlameOrange, selectedTextColor = FlameOrange)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                AppDestination.CUSTOMER_MENU -> {
                    CustomerHomeScreen(
                        isArabic = isArabic,
                        onProductClick = { product ->
                            BunzoSoundManager.playClick()
                            selectedProductForDetail = product
                        },
                        onAddToCart = { product ->
                            // Quick add 1 item to cart with pleasant sound
                            BunzoSoundManager.playAddToCart()
                            val existingIndex = cartItems.indexOfFirst { it.product.id == product.id }
                            cartItems = if (existingIndex >= 0) {
                                cartItems.mapIndexed { idx, item ->
                                    if (idx == existingIndex) item.copy(quantity = item.quantity + 1) else item
                                }
                            } else {
                                cartItems + CartItem(product = product, quantity = 1)
                            }
                            Toast.makeText(
                                context,
                                if (isArabic) "تمت إضافة ${product.nameAr} إلى السلة" else "Added ${product.nameEn} to cart",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onViewOrderTracking = { orderId ->
                            BunzoSoundManager.playClick()
                            trackingOrderId = orderId
                            currentDestination = AppDestination.CUSTOMER_TRACKING
                        }
                    )
                }

                AppDestination.CUSTOMER_CART -> {
                    CartAndCheckoutScreen(
                        cartItems = cartItems,
                        isArabic = isArabic,
                        onUpdateQuantity = { product, newQty ->
                            val oldQty = cartItems.find { it.product.id == product.id }?.quantity ?: 0
                            if (newQty > oldQty) {
                                BunzoSoundManager.playAddToCart()
                            } else {
                                BunzoSoundManager.playRemove()
                            }
                            cartItems = cartItems.mapNotNull { item ->
                                if (item.product.id == product.id) {
                                    if (newQty > 0) item.copy(quantity = newQty) else null
                                } else item
                            }
                        },
                        onUpdateNotes = { product, newNotes ->
                            cartItems = cartItems.map { item ->
                                if (item.product.id == product.id) item.copy(specialNotes = newNotes) else item
                            }
                        },
                        onRemoveItem = { product ->
                            BunzoSoundManager.playRemove()
                            cartItems = cartItems.filterNot { it.product.id == product.id }
                        },
                        onClearCart = {
                            BunzoSoundManager.playRemove()
                            cartItems = emptyList()
                        },
                        onOrderPlaced = { placedOrder ->
                            BunzoSoundManager.playSuccess()
                            trackingOrderId = placedOrder.id
                            currentDestination = AppDestination.CUSTOMER_TRACKING
                            Toast.makeText(
                                context,
                                if (isArabic) "تم إرسال طلبك بنجاح! رقم الطلب #${placedOrder.id}" else "Order placed successfully! #${placedOrder.id}",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        onRequireLogin = {
                            BunzoSoundManager.playClick()
                            showAuthDialog = true
                        }
                    )
                }

                AppDestination.CUSTOMER_TRACKING -> {
                    OrderTrackingScreen(
                        orderId = trackingOrderId,
                        isArabic = isArabic,
                        onBackHome = {
                            BunzoSoundManager.playClick()
                            currentDestination = AppDestination.CUSTOMER_MENU
                        },
                        onTrackOrder = { orderId ->
                            BunzoSoundManager.playClick()
                            trackingOrderId = orderId
                        },
                        onOrderDeleted = { trackingOrderId = null }
                    )
                }

                AppDestination.CUSTOMER_HISTORY -> {
                    OrderHistoryScreen(
                        isArabic = isArabic,
                        onTrackOrder = { orderId ->
                            BunzoSoundManager.playClick()
                            trackingOrderId = orderId
                            currentDestination = AppDestination.CUSTOMER_TRACKING
                        },
                        onHistoryCleared = {
                            BunzoSoundManager.playRemove()
                            trackingOrderId = null
                        }
                    )
                }

                AppDestination.CUSTOMER_PROFILE -> {
                    CustomerProfileScreen(
                        isArabic = isArabic,
                        onOpenAuthDialog = {
                            BunzoSoundManager.playClick()
                            showAuthDialog = true
                        },
                        onNavigateToStaff = { staffUser ->
                            BunzoSoundManager.playClick()
                            currentDestination = if (staffUser.userRole == UserRole.KITCHEN) {
                                AppDestination.KITCHEN_KDS
                            } else {
                                AppDestination.ADMIN_DASHBOARD
                            }
                        }
                    )
                }

                AppDestination.STAFF_LOGIN -> {
                    StaffLoginScreen(
                        isArabic = isArabic,
                        onLoginSuccess = { staffUser ->
                            when (staffUser.userRole) {
                                UserRole.KITCHEN -> {
                                    currentDestination = AppDestination.KITCHEN_KDS
                                }
                                UserRole.ADMIN -> {
                                    currentDestination = AppDestination.ADMIN_DASHBOARD
                                }
                                else -> {
                                    currentDestination = AppDestination.CUSTOMER_MENU
                                }
                            }
                        },
                        onBackToCustomer = {
                            BunzoSoundManager.playClick()
                            currentDestination = AppDestination.CUSTOMER_MENU
                        }
                    )
                }

                AppDestination.ADMIN_DASHBOARD -> {
                    // Security guard: Ensure staff is authenticated and has ADMIN role
                    if (staffSession == null || staffSession?.userRole != UserRole.ADMIN) {
                        LaunchedEffect(Unit) {
                            currentDestination = AppDestination.STAFF_LOGIN
                        }
                    } else {
                        AdminDashboardContent(
                            isArabic = isArabic,
                            onBackToCustomer = {
                                BunzoSoundManager.playClick()
                                currentDestination = AppDestination.CUSTOMER_MENU
                            }
                        )
                    }
                }

                AppDestination.KITCHEN_KDS -> {
                    // Security guard: Ensure staff is authenticated and has KITCHEN or ADMIN role
                    if (staffSession == null || (staffSession?.userRole != UserRole.KITCHEN && staffSession?.userRole != UserRole.ADMIN)) {
                        LaunchedEffect(Unit) {
                            currentDestination = AppDestination.STAFF_LOGIN
                        }
                    } else {
                        KitchenKdsScreen(
                            isArabic = isArabic,
                            onLogout = {
                                currentDestination = AppDestination.CUSTOMER_MENU
                            }
                        )
                    }
                }
            }

            // Product Detail Dialog
            if (selectedProductForDetail != null) {
                ProductDetailDialog(
                    product = selectedProductForDetail!!,
                    isArabic = isArabic,
                    onDismiss = {
                        BunzoSoundManager.playClick()
                        selectedProductForDetail = null
                    },
                    onAddToCart = { product, quantity, notes ->
                        BunzoSoundManager.playAddToCart()
                        val existingIndex = cartItems.indexOfFirst { it.product.id == product.id }
                        cartItems = if (existingIndex >= 0) {
                            cartItems.mapIndexed { idx, item ->
                                if (idx == existingIndex) item.copy(quantity = item.quantity + quantity, specialNotes = notes) else item
                            }
                        } else {
                            cartItems + CartItem(product = product, quantity = quantity, specialNotes = notes)
                        }
                        Toast.makeText(
                            context,
                            if (isArabic) "تمت إضافة $quantity x ${product.nameAr} إلى السلة" else "Added to cart",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }

            // Customer Auth Dialog (Login / Register / Admin Staff)
            if (showAuthDialog) {
                CustomerAuthDialog(
                    isArabic = isArabic,
                    onDismiss = {
                        BunzoSoundManager.playClick()
                        showAuthDialog = false
                    },
                    onSuccess = {
                        BunzoSoundManager.playSuccess()
                        Toast.makeText(
                            context,
                            if (isArabic) "تم تسجيل الدخول بنجاح! مرحباً بك في بنزو" else "Welcome to Bunzo!",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onStaffLoginSuccess = { staffUser ->
                        BunzoSoundManager.playSuccess()
                        showAuthDialog = false
                        currentDestination = if (staffUser.userRole == UserRole.KITCHEN) {
                            AppDestination.KITCHEN_KDS
                        } else {
                            AppDestination.ADMIN_DASHBOARD
                        }
                        Toast.makeText(
                            context,
                            if (isArabic) "مرحباً بك في لوحة الإدارة، ${staffUser.fullName}" else "Welcome, ${staffUser.fullName}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }
    }
}
