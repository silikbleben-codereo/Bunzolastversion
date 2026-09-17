package com.example.ui.kitchen

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Order
import com.example.data.model.OrderItem
import com.example.data.repository.BunzoRepository
import com.example.ui.theme.*
import com.example.util.BunzoSoundManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitchenKdsScreen(
    isArabic: Boolean,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val staffUser by BunzoRepository.staffSession.collectAsState()
    val allOrders by BunzoRepository.orders.collectAsState()

    // Filter states for kitchen: "all", "received", "preparing", "ready"
    var selectedFilter by remember { mutableStateOf("active") }

    // Filter orders: exclude delivered and cancelled from default view
    val kitchenOrders = remember(allOrders, selectedFilter) {
        when (selectedFilter) {
            "active" -> allOrders.filter { it.status == "received" || it.status == "preparing" || it.status == "ready" }
            "received" -> allOrders.filter { it.status == "received" }
            "preparing" -> allOrders.filter { it.status == "preparing" }
            "ready" -> allOrders.filter { it.status == "ready" }
            else -> allOrders
        }
    }

    Scaffold(
        topBar = {
            Surface(
                tonalElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = FlameOrange,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.SoupKitchen,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isArabic) "شاشة المطبخ (KDS)" else "Kitchen Display System",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = staffUser?.fullName ?: if (isArabic) "شيف المطبخ" else "Kitchen Chef",
                                    fontSize = 11.sp,
                                    color = FlameOrange,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Active Orders Counter Badge
                            val activeCount = allOrders.count { it.status == "received" || it.status == "preparing" }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (activeCount > 0) FlameOrange else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.HourglassTop,
                                        contentDescription = null,
                                        tint = if (activeCount > 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$activeCount ${if (isArabic) "طلب للتحضير" else "Active"}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (activeCount > 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Logout Button
                            IconButton(
                                onClick = {
                                    BunzoSoundManager.playClick()
                                    BunzoRepository.logoutStaff()
                                    onLogout()
                                },
                                modifier = Modifier.testTag("kitchen_logout_button")
                            ) {
                                Icon(
                                    Icons.Default.Logout,
                                    contentDescription = "Logout",
                                    tint = StatusCancelled
                                )
                            }
                        }
                    }

                    // Status Filters Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilter == "active",
                            onClick = { selectedFilter = "active" },
                            label = { Text(if (isArabic) "الطلبات النشطة" else "Active Orders") },
                            leadingIcon = { Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = FlameOrange.copy(alpha = 0.15f))
                        )
                        FilterChip(
                            selected = selectedFilter == "received",
                            onClick = { selectedFilter = "received" },
                            label = { Text(if (isArabic) "جديدة (قيد الانتظار)" else "New / Pending") },
                            leadingIcon = { Icon(Icons.Default.FiberNew, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = FlameOrange.copy(alpha = 0.15f))
                        )
                        FilterChip(
                            selected = selectedFilter == "preparing",
                            onClick = { selectedFilter = "preparing" },
                            label = { Text(if (isArabic) "قيد التحضير" else "Preparing") },
                            leadingIcon = { Icon(Icons.Default.SoupKitchen, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = StatusPreparing.copy(alpha = 0.15f))
                        )
                        FilterChip(
                            selected = selectedFilter == "ready",
                            onClick = { selectedFilter = "ready" },
                            label = { Text(if (isArabic) "جاهزة للتسليم" else "Ready") },
                            leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = StatusDelivered.copy(alpha = 0.15f))
                        )
                        FilterChip(
                            selected = selectedFilter == "all",
                            onClick = { selectedFilter = "all" },
                            label = { Text(if (isArabic) "جميع الطلبات" else "All Orders") }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (kitchenOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CheckCircleOutline,
                        contentDescription = null,
                        tint = StatusDelivered,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isArabic) "لا توجد طلبات في هذا القسم حالياً" else "No orders in this category",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isArabic) "ستظهر الطلبات الجديدة تلقائياً فور إرسالها من الزبائن" else "New customer orders will appear here automatically",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("kitchen_orders_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(kitchenOrders, key = { it.id }) { order ->
                    KitchenOrderCard(
                        order = order,
                        isArabic = isArabic,
                        onUpdateStatus = { newStatus ->
                            BunzoSoundManager.playSuccess()
                            BunzoRepository.updateOrderStatus(order.id, newStatus)
                            Toast.makeText(
                                context,
                                if (isArabic) "تم تحديث حالة الطلب #${order.id}" else "Order #${order.id} status updated",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun KitchenOrderCard(
    order: Order,
    isArabic: Boolean,
    onUpdateStatus: (String) -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(order.createdAt) { timeFormat.format(Date(order.createdAt)) }

    // Privacy safeguard: Kitchen only sees order items, preparation notes, and dining method (no prices, customer phones, or coupons)
    val statusColor = when (order.status) {
        "received" -> FlameOrange
        "preparing" -> StatusPreparing
        "ready" -> StatusDelivered
        "on_the_way" -> StatusOnTheWay
        "delivered" -> StatusDelivered
        else -> StatusCancelled
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, statusColor.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("kitchen_order_card_${order.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Order ID, Type, Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = FlameOrange.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "#${order.id}",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = FlameOrange,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (order.orderType == "table") {
                                if (isArabic) "🍽️ صالة (طاولة #${order.tableNumber ?: "-"})" else "🍽️ Dine-In (Table #${order.tableNumber ?: "-"})"
                            } else if (order.orderType == "pickup") {
                                if (isArabic) "🛍️ سفري استلام" else "🛍️ Pickup"
                            } else {
                                if (isArabic) "🛵 توصيل منزلي" else "🛵 Delivery"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formattedTime,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Order Items to Cook (High clarity for kitchen staff)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isArabic) "الوجبات المطلوبة:" else "Items to Prepare:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    order.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = CircleShape,
                                    color = FlameOrange,
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${item.qty}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = item.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    if (item.notes.isNotBlank()) {
                                        Text(
                                            text = "⚠️ ${item.notes}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = FlameOrange
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // General Order Notes from customer (if any)
            if (!order.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFF3E0),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = FlameOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${if (isArabic) "ملاحظات الزبون:" else "Order Note:"} ${order.notes}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD84315)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Workflow Action Buttons (Status progression: received -> preparing -> ready)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (order.status) {
                    "received" -> {
                        Button(
                            onClick = { onUpdateStatus("preparing") },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FlameOrange),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("kds_start_preparing_${order.id}")
                        ) {
                            Icon(Icons.Default.SoupKitchen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "بدء التحضير 👨‍🍳" else "Start Cooking",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    }
                    "preparing" -> {
                        Button(
                            onClick = { onUpdateStatus("ready") },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusDelivered),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("kds_mark_ready_${order.id}")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "جاهز للتسليم / التوصيل ✅" else "Ready to Serve",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    }
                    "ready" -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = StatusDelivered.copy(alpha = 0.15f),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (isArabic) "✅ الوجبة جاهزة وتنتظر الاستلام/السائق" else "✅ Order is ready for pickup/delivery",
                                    color = StatusDelivered,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                    else -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = order.orderStatus.titleAr,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
