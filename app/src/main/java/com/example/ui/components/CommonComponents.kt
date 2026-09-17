package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderStatus
import com.example.ui.theme.*
import com.example.util.BunzoSoundManager
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

fun formatCurrency(amount: Double, isArabic: Boolean): String {
    val formatter = NumberFormat.getNumberInstance(if (isArabic) Locale("ar") else Locale("en"))
    val formatted = formatter.format(amount.toLong())
    return if (isArabic) "$formatted ل.س" else "$formatted SYP"
}

fun formatTimestamp(timeMs: Long, isArabic: Boolean): String {
    val sdf = SimpleDateFormat(if (isArabic) "yyyy/MM/dd - hh:mm a" else "dd MMM yyyy, hh:mm a", if (isArabic) Locale("ar") else Locale.ENGLISH)
    return sdf.format(Date(timeMs))
}

@Composable
fun StatusBadge(status: OrderStatus, isArabic: Boolean) {
    val (bgColor, textColor, icon) = when (status) {
        OrderStatus.RECEIVED -> Triple(Color(0xFFE3F2FD), Color(0xFF1976D2), Icons.Default.Inbox)
        OrderStatus.PREPARING -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), Icons.Default.Restaurant)
        OrderStatus.ON_THE_WAY -> Triple(Color(0xFFF3E5F5), Color(0xFF7B1FA2), Icons.Default.DeliveryDining)
        OrderStatus.READY_FOR_PICKUP -> Triple(Color(0xFFEDE7F6), Color(0xFF512DA8), Icons.Default.Storefront)
        OrderStatus.SERVED -> Triple(Color(0xFFE0F2F1), Color(0xFF00796B), Icons.Default.RoomService)
        OrderStatus.DELIVERED -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), Icons.Default.CheckCircle)
        OrderStatus.CANCELLED -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), Icons.Default.Cancel)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("status_badge_${status.rawValue}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = if (isArabic) status.titleAr else status.titleEn,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BunzoTopAppBar(
    title: String,
    isArabic: Boolean,
    isDark: Boolean,
    cartCount: Int,
    isSoundEnabled: Boolean = true,
    onToggleSound: () -> Unit = {},
    onToggleLang: () -> Unit,
    onToggleTheme: () -> Unit,
    onOpenCart: () -> Unit,
    onOpenProfile: () -> Unit,
    onLogoClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = onLogoClick
                )
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = FlameOrange,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "B",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                    }
                }
                Column {
                    Text(
                        text = if (isArabic) "بنـزو | Bunzo" else "Bunzo Burger",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isArabic) "طعم البرجر الأصيل" else "Gourmet Smash & Crispy",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        actions = {
            // Sound Effects Toggle (صوت النقر المريح)
            IconButton(
                onClick = {
                    onToggleSound()
                    if (!isSoundEnabled) {
                        // Will turn ON: play warm chime to celebrate
                        BunzoSoundManager.playAddToCart()
                    } else {
                        BunzoSoundManager.playRemove()
                    }
                },
                modifier = Modifier.testTag("toggle_sound_button")
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isSoundEnabled) FlameOrange.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = if (isArabic) "صوت النقر المريح" else "Comfortable Click Sound",
                            tint = if (isSoundEnabled) FlameOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Language Toggle
            IconButton(
                onClick = {
                    BunzoSoundManager.playClick()
                    onToggleLang()
                },
                modifier = Modifier.testTag("toggle_lang_button")
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isArabic) "EN" else "عربي",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Dark/Light Theme Toggle
            IconButton(
                onClick = {
                    BunzoSoundManager.playClick()
                    onToggleTheme()
                },
                modifier = Modifier.testTag("toggle_theme_button")
            ) {
                Icon(
                    imageVector = if (isDark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                    contentDescription = "Toggle Theme",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Cart Button with badge
            IconButton(
                onClick = {
                    BunzoSoundManager.playClick()
                    onOpenCart()
                },
                modifier = Modifier.testTag("open_cart_button")
            ) {
                BadgedBox(
                    badge = {
                        if (cartCount > 0) {
                            Badge(
                                containerColor = FlameOrange,
                                contentColor = Color.White
                            ) {
                                Text(cartCount.toString(), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = "Cart",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Profile Button
            IconButton(
                onClick = {
                    BunzoSoundManager.playClick()
                    onOpenProfile()
                },
                modifier = Modifier.testTag("open_profile_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun BunzoProductImage(
    imageUrl: String?,
    imageRes: Int?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: androidx.compose.ui.layout.ContentScale = androidx.compose.ui.layout.ContentScale.Crop
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val defaultRes = imageRes ?: com.example.R.drawable.img_burger_combo

    if (!imageUrl.isNullOrBlank()) {
        coil.compose.AsyncImage(
            model = coil.request.ImageRequest.Builder(context)
                .data(imageUrl)
                .crossfade(true)
                .error(defaultRes)
                .placeholder(defaultRes)
                .build(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = defaultRes),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}

