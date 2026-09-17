package com.example.ui.customer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import com.example.R
import com.example.data.model.*
import com.example.data.repository.BunzoRepository
import com.example.ui.components.BunzoProductImage
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.*
import com.example.util.BunzoSoundManager

// -------------------------------------------------------------
// 1. Customer Home Screen Helpers & Animated Components
// -------------------------------------------------------------

data class HeroOffer(
    val tagAr: String,
    val tagEn: String,
    val titleAr: String,
    val titleEn: String,
    val subtitleAr: String,
    val subtitleEn: String,
    val code: String,
    val imageRes: Int
)

data class VisualCategory(
    val id: String?,
    val nameAr: String,
    val nameEn: String,
    val subtitleAr: String,
    val subtitleEn: String,
    val imageUrl: String,
    val imageRes: Int,
    val iconEmoji: String
)

@Composable
fun BunzoWelcomeDialog(
    isArabic: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .testTag("bunzo_welcome_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Flame & Brand Badge
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(FlameOrange, FlameOrangeDark)
                                )
                            )
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    BunzoSoundManager.playClick()
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_bunzo_logo),
                            contentDescription = "Bunzo Logo",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = AmberGold,
                        border = BorderStroke(1.5.dp, Color.White),
                        modifier = Modifier
                            .size(26.dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🔥", fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isArabic) "أهلاً بك في مطعم بنـزو حلب! 🍔" else "Welcome to Bunzo Aleppo! 🍔",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isArabic)
                        "يسعدنا جداً انضمامك إلينا! استمتع بأشهى برجر سماش وكرسبي طازج يومياً في حلب مع خبز البريوش الفاخر، صوصاتنا السرية وبطاطا وافل المقرمشة."
                    else
                        "We're thrilled to welcome you! Enjoy Aleppo's freshest smash & crispy burgers with buttery brioche, signature dips, and crispy waffle fries.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Welcome Gift Coupon Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = FlameOrange.copy(alpha = 0.1f),
                    border = BorderStroke(1.5.dp, FlameOrange.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
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
                                        imageVector = Icons.Default.CardGiftcard,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = if (isArabic) "هدية الترحيب: خصم 10%" else "Welcome Gift: 10% OFF",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "WELCOME10",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = FlameOrange
                                )
                            }
                        }

                        Button(
                            onClick = {
                                BunzoSoundManager.playSuccess()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                val clip = ClipData.newPlainText("Coupon", "WELCOME10")
                                clipboard?.setPrimaryClip(clip)
                                Toast.makeText(
                                    context,
                                    if (isArabic) "تم نسخ الكوبون: WELCOME10 بنجاح! 🎉" else "Copied WELCOME10! 🎉",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FlameOrange),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = if (isArabic) "نسخ الكود" else "Copy",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Highlights Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WelcomeFeatureBadge(icon = "🛵", label = if (isArabic) "توصيل سريع" else "Fast Delivery")
                    WelcomeFeatureBadge(icon = "🥩", label = if (isArabic) "لحم أنجوس 100%" else "100% Angus")
                    WelcomeFeatureBadge(icon = "⭐", label = if (isArabic) "مذاق مضمون" else "Top Quality")
                }

                Spacer(modifier = Modifier.height(18.dp))

                // CTA Button
                Button(
                    onClick = {
                        BunzoSoundManager.playClick()
                        onDismiss()
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FlameOrange),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("welcome_explore_button")
                ) {
                    Text(
                        text = if (isArabic) "تصفح المنيو واطلب الآن 🍔" else "Explore Menu & Order 🍔",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                TextButton(
                    onClick = {
                        BunzoSoundManager.playClick()
                        onDismiss()
                    }
                ) {
                    Text(
                        text = if (isArabic) "تخطي والدخول للمطعم" else "Skip",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun WelcomeFeatureBadge(icon: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BunzoTopAnimatedMarquee(
    isArabic: Boolean,
    onOpenWelcome: () -> Unit
) {
    Surface(
        color = FlameOrangeDark,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = {
                BunzoSoundManager.playClick()
                onOpenWelcome()
            })
            .testTag("top_animated_marquee")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 7.dp)
                .basicMarquee(
                    iterations = Int.MAX_VALUE,
                    animationMode = MarqueeAnimationMode.Immediately,
                    velocity = 40.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            repeat(3) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (isArabic) "🔥 أهلاً بكم في بنـزو حلب" else "🔥 Welcome to Bunzo Aleppo",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isArabic) "✦ 🛵 توصيل ساخن وسريع لكافة الأحياء" else "✦ 🛵 Express Hot Delivery",
                        color = Color.White.copy(alpha = 0.92f),
                        fontSize = 11.5.sp
                    )
                    Text(
                        text = if (isArabic) "✦ 🍔 لحم أنجوس طازج 100% يومياً" else "✦ 🍔 100% Daily Fresh Angus Beef",
                        color = AmberGold,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isArabic) "✦ 🍟 بطاطا وافل مقرمشة وصوصات سرية" else "✦ 🍟 Crispy Waffles & Dips",
                        color = Color.White.copy(alpha = 0.92f),
                        fontSize = 11.5.sp
                    )
                    Text(
                        text = if (isArabic) "✦ 🎁 كود الترحيب: WELCOME10" else "✦ 🎁 Welcome Code: WELCOME10",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = if (isArabic) "✦ 🏷️ كود اليوم: BUNZO20 (خصم 20%)" else "✦ 🏷️ Today: BUNZO20 (20% OFF)",
                        color = AmberGold,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isArabic) "✦ 📍 فرع الشهباء & فرع الفرقان" else "✦ 📍 Al Shahbaa & Al Furqan Branches",
                        color = Color.White.copy(alpha = 0.88f),
                        fontSize = 11.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun BunzoTopAnimatedQuickMenu(
    categories: List<Category>,
    selectedCategoryId: String?,
    isArabic: Boolean,
    onSelectCategory: (String?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("top_quick_animated_menu")
    ) {
        item {
            val isSelected = selectedCategoryId == null
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) FlameOrange else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, if (isSelected) FlameOrangeDark else Color.Transparent),
                modifier = Modifier
                    .clickable {
                        BunzoSoundManager.playClick()
                        onSelectCategory(null)
                    }
                    .animateContentSize()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("🔥", fontSize = 13.sp)
                    Text(
                        text = if (isArabic) "كل المنيو" else "All Menu",
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        items(categories.sortedBy { it.order }) { cat ->
            val isSelected = selectedCategoryId == cat.id
            val catIcon = when (cat.id) {
                "cat_beef" -> "🥩"
                "cat_chicken" -> "🍗"
                "cat_sides" -> "🍟"
                "cat_drinks" -> "🥤"
                "cat_deals" -> "🏷️"
                else -> "🍔"
            }
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) FlameOrange else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, if (isSelected) FlameOrangeDark else Color.Transparent),
                modifier = Modifier
                    .clickable {
                        BunzoSoundManager.playClick()
                        onSelectCategory(if (isSelected) null else cat.id)
                    }
                    .animateContentSize()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(catIcon, fontSize = 13.sp)
                    Text(
                        text = if (isArabic) cat.nameAr else cat.nameEn,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun BunzoHeroShowcaseWithEffects(
    isArabic: Boolean
) {
    val context = LocalContext.current
    val offers = remember {
        listOf(
            HeroOffer(
                tagAr = "عرض اليوم الخاص 🔥",
                tagEn = "TODAY'S SPECIAL 🔥",
                titleAr = "خصم 20% بكود: BUNZO20",
                titleEn = "20% OFF with code: BUNZO20",
                subtitleAr = "أشهى برجر سماش وكرسبي طازج في حلب",
                subtitleEn = "Fresh brioche smash & crispy chicken",
                code = "BUNZO20",
                imageRes = R.drawable.img_bunzo_banner
            ),
            HeroOffer(
                tagAr = "وفر 11,000 ل.س 🍟",
                tagEn = "SAVE 11,000 SYP 🍟",
                titleAr = "بوكس الثنائي بنـزو بـ 61,000 ل.س",
                titleEn = "Bunzo Duo Combo for 61,000 SYP",
                subtitleAr = "2 برجر + 2 وافل فرايز + 2 صوص ومشروبين",
                subtitleEn = "2 Burgers + 2 waffle fries + 2 dips & drinks",
                code = "DUOCOMBO",
                imageRes = R.drawable.img_burger_combo
            ),
            HeroOffer(
                tagAr = "توصيل سريع مجاني 🛵",
                tagEn = "FREE DELIVERY 🛵",
                titleAr = "توصيل مجاني للطلبات فوق 60,000 ل.س",
                titleEn = "Free Delivery on orders above 60k",
                subtitleAr = "يصلك ساخناً وطازجاً بأسرع وقت في حلب",
                subtitleEn = "Hot, fresh, straight to your doorstep",
                code = "FREESHIP",
                imageRes = R.drawable.img_bunzo_banner
            )
        )
    }

    var currentOfferIndex by remember { mutableIntStateOf(0) }

    // Auto rotate every 4.5 seconds
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(4500)
            currentOfferIndex = (currentOfferIndex + 1) % offers.size
        }
    }

    val currentOffer = offers[currentOfferIndex]

    // Animations: Pulsing flame badge scale
    val infiniteTransition = rememberInfiniteTransition(label = "hero_effects")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Shimmer beam sweep offset
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -250f,
        targetValue = 650f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    // Twinkle alpha
    val twinkleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle_alpha"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable {
                BunzoSoundManager.playSuccess()
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                val clip = ClipData.newPlainText("Promo Code", currentOffer.code)
                clipboard?.setPrimaryClip(clip)
                Toast.makeText(
                    context,
                    if (isArabic) "🎉 تم نسخ كود الخصم: ${currentOffer.code}" else "🎉 Copied code: ${currentOffer.code}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .testTag("hero_showcase_banner")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            // Background Image
            AnimatedContent(
                targetState = currentOffer.imageRes,
                transitionSpec = {
                    fadeIn(animationSpec = tween(600)) togetherWith fadeOut(animationSpec = tween(600))
                },
                label = "banner_image_anim"
            ) { targetRes ->
                Image(
                    painter = painterResource(id = targetRes),
                    contentDescription = "Offer Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Dark Multi-step Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.25f),
                                Color.Black.copy(alpha = 0.55f),
                                Color.Black.copy(alpha = 0.92f)
                            )
                        )
                    )
            )

            // Shimmer Light-Beam Effect
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.16f),
                                Color.Transparent
                            ),
                            start = androidx.compose.ui.geometry.Offset(shimmerOffset, 0f),
                            end = androidx.compose.ui.geometry.Offset(shimmerOffset + 150f, 300f)
                        )
                    )
            )

            // Top-right Sparkles effect
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "✨",
                    fontSize = 16.sp,
                    modifier = Modifier.scale(pulseScale)
                )
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AmberGold.copy(alpha = twinkleAlpha))
                ) {
                    Text(
                        text = if (isArabic) "انقر لنسخ الكود" else "Tap to Copy Code",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Bottom Content
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                // Animated Pulsing Tag
                Surface(
                    color = FlameOrange,
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 4.dp,
                    modifier = Modifier.scale(pulseScale)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isArabic) currentOffer.tagAr else currentOffer.tagEn,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                AnimatedContent(
                    targetState = currentOffer,
                    transitionSpec = {
                        (slideInVertically { height -> height } + fadeIn()) togetherWith
                        (slideOutVertically { height -> -height } + fadeOut())
                    },
                    label = "offer_text_anim"
                ) { offer ->
                    Column {
                        Text(
                            text = if (isArabic) offer.titleAr else offer.titleEn,
                            color = Color.White,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isArabic) offer.subtitleAr else offer.subtitleEn,
                            color = Color.White.copy(alpha = 0.88f),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Indicators dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    offers.indices.forEach { index ->
                        val isSelected = index == currentOfferIndex
                        val widthAnim by animateDpAsState(
                            targetValue = if (isSelected) 22.dp else 7.dp,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "dot_width"
                        )
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(widthAnim)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isSelected) FlameOrange else Color.White.copy(alpha = 0.45f))
                                .clickable {
                                    BunzoSoundManager.playClick()
                                    currentOfferIndex = index
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BunzoVisualMenuCategories(
    categories: List<Category>,
    products: List<Product>,
    selectedCategoryId: String?,
    isArabic: Boolean,
    onSelectCategory: (String?) -> Unit
) {
    val context = LocalContext.current

    val visualCategories = remember(categories, products) {
        listOf(
            VisualCategory(
                id = null,
                nameAr = "الكل",
                nameEn = "All Menu",
                subtitleAr = "${products.size} وجبة",
                subtitleEn = "${products.size} items",
                imageUrl = "https://images.unsplash.com/photo-1550547660-d9450f859349?w=400&q=80",
                imageRes = R.drawable.img_bunzo_banner,
                iconEmoji = "🍔"
            ),
            VisualCategory(
                id = "cat_beef",
                nameAr = "برجر اللحم",
                nameEn = "Beef Burgers",
                subtitleAr = "سماش أنجوس",
                subtitleEn = "Angus Smash",
                imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&q=80",
                imageRes = R.drawable.img_burger_combo,
                iconEmoji = "🥩"
            ),
            VisualCategory(
                id = "cat_chicken",
                nameAr = "برجر الدجاج",
                nameEn = "Chicken Burgers",
                subtitleAr = "كرسبي مقرمش",
                subtitleEn = "Crispy Fried",
                imageUrl = "https://images.unsplash.com/photo-1625813506062-0aeb1d7a094b?w=400&q=80",
                imageRes = R.drawable.img_burger_combo,
                iconEmoji = "🍗"
            ),
            VisualCategory(
                id = "cat_sides",
                nameAr = "المقبلات والبطاطا",
                nameEn = "Sides & Fries",
                subtitleAr = "وافل وصوصات",
                subtitleEn = "Waffle & Dips",
                imageUrl = "https://images.unsplash.com/photo-1576107232684-1279f3908594?w=400&q=80",
                imageRes = R.drawable.img_burger_combo,
                iconEmoji = "🍟"
            ),
            VisualCategory(
                id = "cat_drinks",
                nameAr = "المشروبات والشيكات",
                nameEn = "Drinks & Shakes",
                subtitleAr = "شيكات وعصائر",
                subtitleEn = "Shakes & Sodas",
                imageUrl = "https://images.unsplash.com/photo-1572490122747-3968b75cc699?w=400&q=80",
                imageRes = R.drawable.img_bunzo_logo,
                iconEmoji = "🥤"
            ),
            VisualCategory(
                id = "cat_deals",
                nameAr = "عروض بنـزو",
                nameEn = "Bunzo Specials",
                subtitleAr = "توفير وكومبو",
                subtitleEn = "Combos & Deals",
                imageUrl = "https://images.unsplash.com/photo-1594212699903-ec8a3eca50f5?w=400&q=80",
                imageRes = R.drawable.img_bunzo_banner,
                iconEmoji = "🏷️"
            )
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("📋", fontSize = 16.sp)
                Text(
                    text = if (isArabic) "تصنيفات المنيو" else "Menu Categories",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = if (isArabic) "اختر القسم لعرض وجباته" else "Tap category to filter",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("visual_menu_categories_row")
        ) {
            items(visualCategories) { item ->
                val isSelected = selectedCategoryId == item.id

                val cardElevation by animateDpAsState(
                    targetValue = if (isSelected) 4.dp else 1.dp,
                    label = "card_elev"
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            FlameOrange.copy(alpha = 0.08f)
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.5.dp else 1.dp,
                        color = if (isSelected) FlameOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
                    modifier = Modifier
                        .width(108.dp)
                        .clickable {
                            BunzoSoundManager.playClick()
                            onSelectCategory(if (isSelected && item.id != null) null else item.id)
                        }
                        .testTag("visual_category_${item.id ?: "all"}")
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Category Image Area
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(82.dp)
                                .clip(RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(item.imageUrl)
                                    .crossfade(true)
                                    .error(item.imageRes)
                                    .placeholder(item.imageRes)
                                    .build(),
                                contentDescription = if (isArabic) item.nameAr else item.nameEn,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Top gradient shadow
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.35f),
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.2f)
                                            )
                                        )
                                    )
                            )

                            // Top Emoji Badge
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(5.dp)
                                    .size(22.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(item.iconEmoji, fontSize = 11.sp)
                                }
                            }

                            // If selected: checkmark badge
                            if (isSelected) {
                                Surface(
                                    shape = CircleShape,
                                    color = FlameOrange,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(5.dp)
                                        .size(20.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Category Name and Details UNDER THE IMAGE
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 7.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isArabic) item.nameAr else item.nameEn,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isSelected) FlameOrange else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = if (isArabic) item.subtitleAr else item.subtitleEn,
                                fontSize = 10.sp,
                                color = if (isSelected) FlameOrange.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 1. Customer Home Screen
// -------------------------------------------------------------
@Composable
fun CustomerHomeScreen(
    isArabic: Boolean,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onViewOrderTracking: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories by BunzoRepository.categories.collectAsState()
    val products by BunzoRepository.products.collectAsState()
    val orders by BunzoRepository.orders.collectAsState()
    val currentUser by BunzoRepository.currentUser.collectAsState()

    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showWelcomeDialog by rememberSaveable { mutableStateOf(true) }

    // Welcome Dialog on entry
    if (showWelcomeDialog) {
        BunzoWelcomeDialog(
            isArabic = isArabic,
            onDismiss = { showWelcomeDialog = false }
        )
    }

    // Check if user has an active order (received, preparing, on_the_way)
    val activeOrder = remember(orders, currentUser) {
        currentUser?.let { user ->
            orders.find { it.customerId == user.uid && it.status in listOf("received", "preparing", "on_the_way", "ready_for_pickup", "served") }
        }
    }

    val filteredProducts = remember(products, selectedCategoryId, searchQuery, isArabic) {
        products.filter { prod ->
            val matchCat = selectedCategoryId == null || prod.categoryId == selectedCategoryId
            val name = if (isArabic) prod.nameAr else prod.nameEn
            val desc = if (isArabic) prod.descriptionAr else prod.descriptionEn
            val matchQuery = searchQuery.isBlank() || name.contains(searchQuery, ignoreCase = true) || desc.contains(searchQuery, ignoreCase = true)
            matchCat && matchQuery
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("customer_home_list"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Top Marquee Banner (Animated Ticker)
        item {
            BunzoTopAnimatedMarquee(
                isArabic = isArabic,
                onOpenWelcome = { showWelcomeDialog = true }
            )
        }

        // Top Animated Quick-Menu
        item {
            BunzoTopAnimatedQuickMenu(
                categories = categories,
                selectedCategoryId = selectedCategoryId,
                isArabic = isArabic,
                onSelectCategory = { selectedCategoryId = it }
            )
        }

        // Active Order Banner Alert (if user has active order)
        if (activeOrder != null) {
            item {
                Surface(
                    color = FlameOrange,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { onViewOrderTracking(activeOrder.id) }
                        .testTag("active_order_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBike,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = if (isArabic) "طلبك قيد المتابعة #${activeOrder.id}" else "Active Order #${activeOrder.id}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (isArabic) "الحالة: ${activeOrder.orderStatus.titleAr}" else "Status: ${activeOrder.orderStatus.titleEn}",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Text(
                            text = if (isArabic) "تتبع الآن ←" else "Track →",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Hero Showcase Banner with Effects
        item {
            BunzoHeroShowcaseWithEffects(isArabic = isArabic)
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("search_food_input"),
                placeholder = {
                    Text(
                        text = if (isArabic) "ابحث عن وجبة أو برجر أو بطاطا أو صوص..." else "Search burgers, sides, shakes...",
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        // Visual Menu Categories (With photos and names under each photo)
        item {
            BunzoVisualMenuCategories(
                categories = categories,
                products = products,
                selectedCategoryId = selectedCategoryId,
                isArabic = isArabic,
                onSelectCategory = { selectedCategoryId = it }
            )
        }

        // Section Title
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("🍔", fontSize = 16.sp)
                    Text(
                        text = if (isArabic) "قائمة وجبات بنـزو" else "Bunzo Menu",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "${filteredProducts.size} ${if (isArabic) "وجبة" else "items"}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Products List
        if (filteredProducts.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isArabic) "لم يتم العثور على أي وجبات تطابق بحثك" else "No items found matching your search",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(filteredProducts, key = { it.id }) { product ->
                ProductCard(
                    product = product,
                    isArabic = isArabic,
                    onClick = { onProductClick(product) },
                    onAddToCart = { onAddToCart(product) }
                )
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    isArabic: Boolean,
    onClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = {
                BunzoSoundManager.playClick()
                onClick()
            })
            .testTag("product_card_${product.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Product Image
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                BunzoProductImage(
                    imageUrl = product.image,
                    imageRes = product.imageRes,
                    contentDescription = if (isArabic) product.nameAr else product.nameEn,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (product.discountPrice != null) {
                    val discountPercent = (((product.price - product.discountPrice) / product.price) * 100).toInt()
                    Surface(
                        color = FlameOrange,
                        shape = RoundedCornerShape(bottomEnd = 8.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "-$discountPercent%",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                if (!product.isAvailable) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isArabic) "غير متاح" else "Unavailable",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Product Details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 96.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isArabic) product.nameAr else product.nameEn,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (product.isFeatured) {
                            Surface(
                                color = AmberGold.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (isArabic) "مميز" else "Special",
                                    color = AmberGoldDark,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = if (isArabic) product.descriptionAr else product.descriptionEn,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.5.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }

                // Price and Add Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        if (product.discountPrice != null) {
                            Text(
                                text = formatCurrency(product.price, isArabic),
                                textDecoration = TextDecoration.LineThrough,
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = formatCurrency(product.effectivePrice, isArabic),
                            color = FlameOrange,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = onAddToCart,
                        enabled = product.isAvailable,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FlameOrange,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("add_to_cart_btn_${product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isArabic) "أضف" else "Add",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. Product Detail Dialog
// -------------------------------------------------------------
@Composable
fun ProductDetailDialog(
    product: Product,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onAddToCart: (Product, Int, String) -> Unit
) {
    var quantity by remember { mutableIntStateOf(1) }
    var notes by remember { mutableStateOf("") }

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
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Product Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    BunzoProductImage(
                        imageUrl = product.image,
                        imageRes = product.imageRes,
                        contentDescription = if (isArabic) product.nameAr else product.nameEn,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = {
                            BunzoSoundManager.playClick()
                            onDismiss()
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isArabic) product.nameAr else product.nameEn,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = formatCurrency(product.effectivePrice, isArabic),
                            color = FlameOrange,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isArabic) product.descriptionAr else product.descriptionEn,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))
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
                                Icons.Outlined.Edit,
                                contentDescription = null,
                                tint = FlameOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isArabic) "ملاحظات وتفضيلات الوجبة (اختياري)" else "Meal Notes & Preferences (Optional)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (notes.isNotBlank()) {
                            Text(
                                text = if (isArabic) "مسح" else "Clear",
                                fontSize = 11.5.sp,
                                color = FlameOrange,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { notes = "" }
                                    .padding(4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = {
                            Text(
                                if (isArabic) "مثال: بدون بصل، زيادة صوص، صوص على جنب..." else "e.g. no onions, extra sauce, sauce on side...",
                                fontSize = 12.sp
                            )
                        },
                        trailingIcon = {
                            if (notes.isNotBlank()) {
                                IconButton(onClick = { notes = "" }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FlameOrange,
                            cursorColor = FlameOrange
                        ),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    val quickNotesSuggestions = remember(isArabic) {
                        if (isArabic) listOf(
                            "بدون بصل",
                            "بدون مخلل",
                            "بدون طماطم",
                            "زيادة صوص بنزو",
                            "سبايسي / حار",
                            "بدون مايونيز",
                            "جبنة إضافية"
                        ) else listOf(
                            "No onions",
                            "No pickles",
                            "No tomatoes",
                            "Extra Bunzo sauce",
                            "Extra spicy",
                            "No mayo",
                            "Extra cheese"
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickNotesSuggestions.forEach { tag ->
                            val isSelected = notes.contains(tag, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) FlameOrange.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) FlameOrange else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                ),
                                onClick = {
                                    BunzoSoundManager.playClick()
                                    val currentList = notes.split("،", ",").map { it.trim() }.filter { it.isNotBlank() }.toMutableList()
                                    if (isSelected) {
                                        currentList.removeAll { it.equals(tag, ignoreCase = true) }
                                    } else {
                                        currentList.add(tag)
                                    }
                                    notes = currentList.joinToString(if (isArabic) "، " else ", ")
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = FlameOrange, modifier = Modifier.size(12.dp))
                                    }
                                    Text(
                                        text = tag,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) FlameOrange else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    // Quantity picker and Add Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                .padding(4.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    BunzoSoundManager.playRemove()
                                    if (quantity > 1) quantity--
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = quantity.toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            IconButton(
                                onClick = {
                                    BunzoSoundManager.playAddToCart()
                                    quantity++
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                            }
                        }

                        val totalAmount = product.effectivePrice * quantity
                        Button(
                            onClick = {
                                BunzoSoundManager.playAddToCart()
                                onAddToCart(product, quantity, notes)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FlameOrange),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                            modifier = Modifier.testTag("confirm_add_product_button")
                        ) {
                            Text(
                                text = "${if (isArabic) "أضف" else "Add"} (${formatCurrency(totalAmount, isArabic)})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Meal Notes Customization Dialog
// -------------------------------------------------------------
@Composable
fun MealNotesEditDialog(
    item: CartItem,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onSaveNotes: (String) -> Unit
) {
    var notes by remember(item) { mutableStateOf(item.specialNotes) }
    val quickNotesList = remember(isArabic) {
        if (isArabic) listOf(
            "بدون بصل",
            "بدون مخلل",
            "بدون طماطم",
            "بدون مايونيز",
            "زيادة صوص بنزو",
            "سبايسي / حار",
            "جبنة إضافية",
            "استواء كامل"
        ) else listOf(
            "No onions",
            "No pickles",
            "No tomatoes",
            "No mayo",
            "Extra Bunzo sauce",
            "Extra spicy",
            "Extra cheese",
            "Well done"
        )
    }

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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
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
                                    Icons.Outlined.Edit,
                                    contentDescription = null,
                                    tint = FlameOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = if (isArabic) "ملاحظات الوجبة" else "Meal Notes",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isArabic) item.product.nameAr else item.product.nameEn,
                                fontSize = 12.sp,
                                color = FlameOrange,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.outline)
                    }
                }

                Text(
                    text = if (isArabic) "اكتب تعليماتك الخاصة لتحضير هذه الوجبة في المطبخ:" else "Specify kitchen instructions for this meal:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = {
                        Text(
                            if (isArabic) "مثال: بدون بصل، زيادة صوص بنزو، صوص على جنب..." else "e.g. no onions, extra sauce, sauce on side...",
                            fontSize = 12.sp
                        )
                    },
                    trailingIcon = {
                        if (notes.isNotBlank()) {
                            IconButton(onClick = { notes = "" }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FlameOrange,
                        cursorColor = FlameOrange
                    ),
                    minLines = 2,
                    maxLines = 3
                )

                Text(
                    text = if (isArabic) "خيارات سريعة (اضغط للإضافة / الإزالة):" else "Quick options (tap to toggle):",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val firstRowTags = quickNotesList.take(4)
                val secondRowTags = quickNotesList.drop(4)

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        firstRowTags.forEach { tag ->
                            val isSelected = notes.contains(tag, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) FlameOrange.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) FlameOrange else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                ),
                                onClick = {
                                    BunzoSoundManager.playClick()
                                    val currentList = notes.split("،", ",").map { it.trim() }.filter { it.isNotBlank() }.toMutableList()
                                    if (isSelected) {
                                        currentList.removeAll { it.equals(tag, ignoreCase = true) }
                                    } else {
                                        currentList.add(tag)
                                    }
                                    notes = currentList.joinToString(if (isArabic) "، " else ", ")
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = FlameOrange, modifier = Modifier.size(12.dp))
                                    }
                                    Text(
                                        text = tag,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) FlameOrange else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        secondRowTags.forEach { tag ->
                            val isSelected = notes.contains(tag, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) FlameOrange.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) FlameOrange else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                ),
                                onClick = {
                                    BunzoSoundManager.playClick()
                                    val currentList = notes.split("،", ",").map { it.trim() }.filter { it.isNotBlank() }.toMutableList()
                                    if (isSelected) {
                                        currentList.removeAll { it.equals(tag, ignoreCase = true) }
                                    } else {
                                        currentList.add(tag)
                                    }
                                    notes = currentList.joinToString(if (isArabic) "، " else ", ")
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = FlameOrange, modifier = Modifier.size(12.dp))
                                    }
                                    Text(
                                        text = tag,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) FlameOrange else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

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
                            BunzoSoundManager.playClick()
                            onSaveNotes(notes.trim())
                            onDismiss()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FlameOrange),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isArabic) "حفظ الملاحظة" else "Save Note", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. Cart & Checkout Screen
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartAndCheckoutScreen(
    cartItems: List<CartItem>,
    isArabic: Boolean,
    onUpdateQuantity: (Product, Int) -> Unit,
    onRemoveItem: (Product) -> Unit,
    onClearCart: () -> Unit,
    onOrderPlaced: (Order) -> Unit,
    onRequireLogin: () -> Unit,
    modifier: Modifier = Modifier,
    onUpdateNotes: (Product, String) -> Unit = { _, _ -> }
) {
    val currentUser by BunzoRepository.currentUser.collectAsState()
    val regions by BunzoRepository.regions.collectAsState()

    var orderType by remember { mutableStateOf(OrderType.DELIVERY) }
    var selectedRegion by remember { mutableStateOf(currentUser?.region ?: regions.firstOrNull()?.nameAr ?: "") }
    var detailedAddress by remember { mutableStateOf(currentUser?.address ?: "") }
    var streetName by remember { mutableStateOf("") }
    var buildingAndFloor by remember { mutableStateOf("") }
    var landmarkNearby by remember { mutableStateOf("") }
    var recipientPhone by remember { mutableStateOf(currentUser?.phone ?: "") }
    var deliveryTimeSlot by remember { mutableStateOf("asap") } // "asap" or "schedule"
    var tableNumber by remember { mutableStateOf("5") }
    var notes by remember { mutableStateOf("") }
    var couponCode by remember { mutableStateOf("") }
    var appliedCoupon by remember { mutableStateOf<Coupon?>(null) }
    var discountAmount by remember { mutableDoubleStateOf(0.0) }
    var couponMessage by remember { mutableStateOf<String?>(null) }
    var couponError by remember { mutableStateOf(false) }
    var paymentMethod by remember { mutableStateOf("cash") }
    var isRegionDropdownExpanded by remember { mutableStateOf(false) }
    var showClearCartDialog by remember { mutableStateOf(false) }
    var itemToEditNotes by remember { mutableStateOf<CartItem?>(null) }

    // Synchronize defaults when user logs in or profile changes
    LaunchedEffect(currentUser) {
        currentUser?.let {
            if (it.region.isNotBlank()) selectedRegion = it.region
            if (it.address.isNotBlank() && detailedAddress.isBlank()) detailedAddress = it.address
            if (it.phone.isNotBlank() && recipientPhone.isBlank()) recipientPhone = it.phone
        }
    }

    val subtotal = remember(cartItems) { cartItems.sumOf { it.totalPrice } }
    val deliveryFee = remember(orderType) { if (orderType == OrderType.DELIVERY) 5000.0 else 0.0 }
    val finalTotal = (subtotal + deliveryFee - discountAmount).coerceAtLeast(0.0)

    if (cartItems.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(90.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isArabic) "سلة التسوق فارغة حالياً" else "Your cart is currently empty",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isArabic) "تصفح قائمة طعام بنزو الشهية وأضف البرجر والمقبلات المفضلة لديك!" else "Explore Bunzo menu and add your favorite burgers!",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("cart_screen"),
        contentPadding = PaddingValues(16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Cart Items Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isArabic) "الوجبات المطلوبة (${cartItems.size})" else "Order Items (${cartItems.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(
                    onClick = { showClearCartDialog = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = StatusCancelled)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteSweep,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isArabic) "حذف محتويات السلة" else "Clear Cart",
                        color = StatusCancelled,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                }
            }
        }

        // Cart Items List
        items(cartItems) { item ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BunzoProductImage(
                            imageUrl = item.product.image,
                            imageRes = item.product.imageRes,
                            contentDescription = if (isArabic) item.product.nameAr else item.product.nameEn,
                            modifier = Modifier
                                .size(58.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isArabic) item.product.nameAr else item.product.nameEn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = formatCurrency(item.totalPrice, isArabic),
                                color = FlameOrange,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        // Quantity +/- and direct Delete
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (item.quantity > 1) {
                                        onUpdateQuantity(item.product, item.quantity - 1)
                                    } else {
                                        onRemoveItem(item.product)
                                    }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease", tint = MaterialTheme.colorScheme.primary)
                            }
                            Text(
                                text = item.quantity.toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            IconButton(
                                onClick = { onUpdateQuantity(item.product, item.quantity + 1) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.AddCircle, contentDescription = "Increase", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(
                                onClick = { onRemoveItem(item.product) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = if (isArabic) "حذف من السلة" else "Delete",
                                    tint = StatusCancelled,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }

                    // Special Notes for this meal
                    Spacer(modifier = Modifier.height(8.dp))
                    if (item.specialNotes.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FlameOrange.copy(alpha = 0.09f),
                            border = BorderStroke(1.dp, FlameOrange.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    BunzoSoundManager.playClick()
                                    itemToEditNotes = item
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Edit,
                                    contentDescription = null,
                                    tint = FlameOrange,
                                    modifier = Modifier.size(14.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isArabic) "ملاحظة خاصة على الوجبة:" else "Special meal note:",
                                        fontSize = 10.sp,
                                        color = FlameOrange,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = item.specialNotes,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = if (isArabic) "تعديل الملاحظة" else "Edit note",
                                    tint = FlameOrange,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    BunzoSoundManager.playClick()
                                    itemToEditNotes = item
                                }
                                .padding(vertical = 4.dp, horizontal = 2.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "+ إضافة ملاحظة على هذه الوجبة (مثل: بدون بصل، صوص خارجي)" else "+ Add note to this meal (e.g. no onions, sauce on side)",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Order Type Selector
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isArabic) "نوع الاستلام" else "Order Type",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OrderType.entries.forEach { type ->
                            val isSelected = orderType == type
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) FlameOrange else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        BunzoSoundManager.playClick()
                                        orderType = type
                                    }
                                    .testTag("order_type_${type.rawValue}")
                            ) {
                                Text(
                                    text = if (isArabic) type.titleAr else type.titleEn,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.5.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Delivery / Pickup / Table Address Inputs
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    when (orderType) {
                        OrderType.DELIVERY -> {
                            // Section Header with Icon & Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = FlameOrange.copy(alpha = 0.12f),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Moped,
                                                contentDescription = null,
                                                tint = FlameOrange,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = if (isArabic) "بيانات التوصيل السريع" else "Express Delivery Details",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isArabic) "حدد موقع الاستلام بدقة لضمان وصول ساخن وسريع" else "Accurate location ensures fast and hot arrival",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Delivery Estimate / Service banner
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = null,
                                            tint = FlameOrange,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = if (isArabic) "وقت الوصول المتوقع: 30-45 دقيقة" else "Est. Delivery: 30-45 mins",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Surface(
                                        color = StatusDelivered.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (isArabic) "أجور التوصيل 5,000 ل.س" else "Fee: 5,000 SYP",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StatusDelivered,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Step 1: Region Dropdown
                            Text(
                                text = if (isArabic) "1. الحي أو المنطقة في حلب *" else "1. District / Region in Aleppo *",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            ExposedDropdownMenuBox(
                                expanded = isRegionDropdownExpanded,
                                onExpandedChange = { isRegionDropdownExpanded = !isRegionDropdownExpanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedRegion,
                                    onValueChange = {
                                        selectedRegion = it
                                        isRegionDropdownExpanded = true
                                    },
                                    placeholder = { Text(if (isArabic) "اختر أو اكتب اسم منطقتك..." else "Select or type your area...") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = FlameOrange
                                        )
                                    },
                                    trailingIcon = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (selectedRegion.isNotBlank()) {
                                                IconButton(
                                                    onClick = { selectedRegion = "" },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Clear,
                                                        contentDescription = if (isArabic) "مسح" else "Clear",
                                                        tint = MaterialTheme.colorScheme.outline,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isRegionDropdownExpanded)
                                        }
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                        .testTag("region_selector_dropdown"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = FlameOrange,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                    )
                                )

                                val filteredCartRegions = if (selectedRegion.isBlank()) regions else regions.filter { reg ->
                                    reg.nameAr.contains(selectedRegion, ignoreCase = true) || reg.nameEn.contains(selectedRegion, ignoreCase = true)
                                }

                                ExposedDropdownMenu(
                                    expanded = isRegionDropdownExpanded,
                                    onDismissRequest = { isRegionDropdownExpanded = false }
                                ) {
                                    if (filteredCartRegions.isNotEmpty()) {
                                        filteredCartRegions.forEach { reg ->
                                            val regionName = if (isArabic) reg.nameAr else reg.nameEn
                                            DropdownMenuItem(
                                                leadingIcon = {
                                                    Icon(
                                                        Icons.Outlined.Place,
                                                        contentDescription = null,
                                                        tint = FlameOrange,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                },
                                                text = {
                                                    Column {
                                                        Text(
                                                            text = regionName,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                        Text(
                                                            text = if (isArabic) "توصيل متوفر في هذه المنطقة" else "Delivery available",
                                                            fontSize = 10.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    selectedRegion = regionName
                                                    isRegionDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }

                                    if (selectedRegion.isNotBlank() && regions.none { it.nameAr.equals(selectedRegion.trim(), ignoreCase = true) || it.nameEn.equals(selectedRegion.trim(), ignoreCase = true) }) {
                                        DropdownMenuItem(
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Outlined.Edit,
                                                    contentDescription = null,
                                                    tint = FlameOrange,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            },
                                            text = {
                                                Column {
                                                    Text(
                                                        text = if (isArabic) "اعتماد حي مخصص: \"$selectedRegion\"" else "Use custom area: \"$selectedRegion\"",
                                                        fontWeight = FontWeight.Bold,
                                                        color = FlameOrange,
                                                        fontSize = 13.sp
                                                    )
                                                    Text(
                                                        text = if (isArabic) "سيتم توصيل طلبك إلى هذا الحي" else "Order will be delivered to this district",
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            },
                                            onClick = {
                                                isRegionDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Step 2: Street and Neighborhood
                            Text(
                                text = if (isArabic) "2. اسم الشارع ورقم البناء والطابق *" else "2. Street, Building & Floor *",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = detailedAddress,
                                onValueChange = { detailedAddress = it },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.Apartment,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                placeholder = {
                                    Text(
                                        if (isArabic) "مثال: شارع المهندسين، بناء الياسمين 14، طابق 3" else "Street name, building #, floor...",
                                        fontSize = 12.sp
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("detailed_address_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FlameOrange,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Step 3: Two Column Row (Landmark & Building/Floor details)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Landmark
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isArabic) "علامة مميزة قريبة" else "Nearby Landmark",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = landmarkNearby,
                                        onValueChange = { landmarkNearby = it },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Outlined.NearMe,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        },
                                        placeholder = {
                                            Text(
                                                if (isArabic) "بجانب الصيدلية، جامع..." else "Near pharmacy, mosque...",
                                                fontSize = 11.sp
                                            )
                                        },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }

                                // Recipient Phone
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isArabic) "هاتف مستلم الطلب *" else "Recipient Phone *",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = recipientPhone,
                                        onValueChange = { recipientPhone = it },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Phone,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        },
                                        placeholder = { Text("09xxxxxxxx", fontSize = 11.sp) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Step 4: Special Instructions for the Delivery Driver
                            Text(
                                text = if (isArabic) "ملاحظات وتوجيهات لكابتن التوصيل (اختياري)" else "Instructions for Driver (Optional)",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.EditNote,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                placeholder = {
                                    Text(
                                        if (isArabic) "مثال: الرجاء رن الجرس فقط، المصعد متوقف، الباب الخارجي مفتوح..." else "E.g. Ring bell only, gate open, elevator out...",
                                        fontSize = 11.5.sp
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        OrderType.PICKUP -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = FlameOrange.copy(alpha = 0.12f),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Storefront,
                                            contentDescription = null,
                                            tint = FlameOrange,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = if (isArabic) "استلام شخصي من فرع المطعم" else "Pickup from Restaurant Branch",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = if (isArabic) "بدون أجور توصيل - استلم وجبتك طازجة وساخنة" else "Zero delivery fee - pick up fresh and hot",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.Place, contentDescription = null, tint = FlameOrange, modifier = Modifier.size(18.dp))
                                        Text(
                                            text = if (isArabic) "فرع الشهباء الرئيسي: حلب، شارع الشهباء، جانب حديقة السبيل" else "Main Branch: Aleppo, Al Shahbaa St, near Al Sabeel Park",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = AmberGoldDark, modifier = Modifier.size(18.dp))
                                        Text(
                                            text = if (isArabic) "سيكون طلبك جاهزاً للاستلام خلال 20-25 دقيقة من قبوله" else "Ready for pickup in 20-25 mins from confirmation",
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        OrderType.TABLE -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = FlameOrange.copy(alpha = 0.12f),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.TableBar,
                                            contentDescription = null,
                                            tint = FlameOrange,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = if (isArabic) "طلب محلي داخل الصالة" else "Dine-in Table Order",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = if (isArabic) "سيقوم الويتر بإحضار الطلب إلى طاولتك مباشرة" else "Our waiter will serve directly to your table",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = tableNumber,
                                onValueChange = { tableNumber = it },
                                label = { Text(if (isArabic) "رقم الطاولة" else "Table Number") },
                                leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null, tint = FlameOrange) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // Coupon Code Input
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isArabic) "كوبون الخصم (جرب: BUNZO20)" else "Promo Coupon (Try: BUNZO20)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = couponCode,
                            onValueChange = { couponCode = it },
                            placeholder = { Text("BUNZO20", fontSize = 13.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("coupon_code_input")
                        )
                        Button(
                            onClick = {
                                val result = BunzoRepository.validateCoupon(couponCode, subtotal)
                                result.onSuccess { (coupon, discount) ->
                                    BunzoSoundManager.playSuccess()
                                    appliedCoupon = coupon
                                    discountAmount = discount
                                    couponMessage = if (isArabic) "تم تفعيل خصم ${coupon.discountPercent}% بنجاح!" else "Discount applied!"
                                    couponError = false
                                }.onFailure { error ->
                                    BunzoSoundManager.playRemove()
                                    appliedCoupon = null
                                    discountAmount = 0.0
                                    couponMessage = error.message
                                    couponError = true
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AmberGoldDark),
                            modifier = Modifier.testTag("apply_coupon_button")
                        ) {
                            Text(if (isArabic) "تطبيق" else "Apply", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (couponMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = couponMessage!!,
                            color = if (couponError) StatusCancelled else StatusDelivered,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Payment Method
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isArabic) "طريقة الدفع" else "Payment Method",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (paymentMethod == "cash") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (paymentMethod == "cash") BorderStroke(1.dp, FlameOrange) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    BunzoSoundManager.playClick()
                                    paymentMethod = "cash"
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.AttachMoney, contentDescription = null, tint = FlameOrange)
                                Text(
                                    text = if (isArabic) "نقدًا عند الاستلام" else "Cash on Delivery",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (paymentMethod == "card_demo") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (paymentMethod == "card_demo") BorderStroke(1.dp, FlameOrange) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    BunzoSoundManager.playClick()
                                    paymentMethod = "card_demo"
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.CreditCard, contentDescription = null, tint = FlameOrange)
                                Text(
                                    text = if (isArabic) "بطاقة بنكية (تجريبي)" else "Card (Demo)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bill Summary
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isArabic) "ملخص الفاتورة" else "Bill Summary",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = if (isArabic) "المجموع الفرعي:" else "Subtotal:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = formatCurrency(subtotal, isArabic), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    if (orderType == OrderType.DELIVERY) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = if (isArabic) "أجور التوصيل:" else "Delivery fee:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = formatCurrency(deliveryFee, isArabic), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (discountAmount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = if (isArabic) "قيمة الخصم:" else "Discount:", fontSize = 13.sp, color = StatusDelivered)
                            Text(text = "- ${formatCurrency(discountAmount, isArabic)}", fontSize = 13.sp, color = StatusDelivered, fontWeight = FontWeight.Bold)
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isArabic) "الإجمالي النهائي:" else "Total Amount:",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = formatCurrency(finalTotal, isArabic),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = FlameOrange
                        )
                    }
                }
            }
        }

        // Submit Button
        item {
            Button(
                onClick = {
                    val user = currentUser
                    if (user == null) {
                        onRequireLogin()
                        return@Button
                    }

                    val finalAddress = when (orderType) {
                        OrderType.DELIVERY -> {
                            val base = detailedAddress.ifBlank { user.address }
                            val parts = mutableListOf<String>()
                            if (base.isNotBlank()) parts.add(base)
                            if (landmarkNearby.isNotBlank()) parts.add(if (isArabic) "علامة: $landmarkNearby" else "Landmark: $landmarkNearby")
                            parts.joinToString(" • ").ifBlank { "حلب" }
                        }
                        OrderType.PICKUP -> if (isArabic) "استلام من فرع الشهباء الرئيسي" else "Pickup from Al Shahbaa Branch"
                        OrderType.TABLE -> if (isArabic) "طاولة رقم: $tableNumber داخل الصالة" else "Table #$tableNumber (Dine-in)"
                    }

                    val finalRegion = when (orderType) {
                        OrderType.DELIVERY -> selectedRegion.ifBlank { user.region }
                        else -> "حلب الشهباء"
                    }

                    val finalPhone = if (orderType == OrderType.DELIVERY && recipientPhone.isNotBlank()) {
                        recipientPhone
                    } else {
                        user.phone
                    }

                    val newOrder = Order(
                        customerId = user.uid,
                        customerName = user.fullName,
                        customerPhone = finalPhone,
                        customerRegion = finalRegion,
                        items = cartItems.map { OrderItem(it.product.id, if (isArabic) it.product.nameAr else it.product.nameEn, it.quantity, it.product.effectivePrice, it.specialNotes) },
                        subtotal = subtotal,
                        deliveryFee = deliveryFee,
                        discount = discountAmount,
                        total = finalTotal,
                        orderType = orderType.rawValue,
                        tableNumber = if (orderType == OrderType.TABLE) tableNumber else null,
                        address = finalAddress,
                        paymentMethod = paymentMethod,
                        notes = notes,
                        status = "received"
                    )

                    val placed = BunzoRepository.placeOrder(newOrder)
                    onClearCart()
                    onOrderPlaced(placed)
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FlameOrange),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirm_order_button")
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isArabic) "تأكيد وإرسال الطلب (${formatCurrency(finalTotal, isArabic)})" else "Place Order (${formatCurrency(finalTotal, isArabic)})",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }
        }
    }

    if (itemToEditNotes != null) {
        MealNotesEditDialog(
            item = itemToEditNotes!!,
            isArabic = isArabic,
            onDismiss = { itemToEditNotes = null },
            onSaveNotes = { updatedNotes ->
                val current = itemToEditNotes
                if (current != null) {
                    onUpdateNotes(current.product, updatedNotes)
                }
            }
        )
    }

    if (showClearCartDialog) {
        AlertDialog(
            onDismissRequest = { showClearCartDialog = false },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = StatusCancelled) },
            title = { Text(if (isArabic) "حذف محتويات السلة" else "Clear Cart") },
            text = {
                Text(
                    if (isArabic) "هل أنت متأكد من رغبتك في حذف جميع الوجبات وإفراغ السلة بالكامل؟"
                    else "Are you sure you want to remove all items from your cart?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearCartDialog = false
                        onClearCart()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusCancelled)
                ) {
                    Text(if (isArabic) "نعم، حذف الكل" else "Yes, Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCartDialog = false }) {
                    Text(if (isArabic) "إلغاء" else "Cancel")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// 4. Order Tracking Screen (Live Reactive Pipeline)
// -------------------------------------------------------------
@Composable
fun OrderTrackingScreen(
    orderId: String?,
    isArabic: Boolean,
    onBackHome: () -> Unit,
    onTrackOrder: (String) -> Unit = {},
    onOrderDeleted: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val orders by BunzoRepository.orders.collectAsState()
    val currentUser by BunzoRepository.currentUser.collectAsState()
    val order = if (!orderId.isNullOrBlank()) orders.find { it.id == orderId } else null
    val context = LocalContext.current
    var searchOrderIdInput by remember { mutableStateOf("") }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val myOrders = remember(orders, currentUser) {
        currentUser?.let { user ->
            orders.filter { it.customerId == user.uid }.sortedByDescending { it.createdAt }
        } ?: orders.sortedByDescending { it.createdAt }
    }

    if (order == null) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .testTag("empty_order_tracking_screen"),
            contentPadding = PaddingValues(20.dp, bottom = 96.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = CircleShape,
                    color = FlameOrange.copy(alpha = 0.12f),
                    modifier = Modifier.size(90.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBike,
                            contentDescription = null,
                            tint = FlameOrange,
                            modifier = Modifier.size(46.dp)
                        )
                    }
                }
            }

            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isArabic) "لا يوجد طلب قيد التتبع حالياً" else "No Active Order to Track",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isArabic)
                            "عند إرسال طلب وجبة من بنزو، ستتمكن من متابعة وتتبع مراحل تحضيرها وتوصيلها لحظة بلحظة هنا. يمكنك أيضاً البحث برقم أي طلب لمعرفة حالته ومراحله."
                        else
                            "Place an order to track its live preparation and delivery status here, or search by order number.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // Quick Order Search
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isArabic) "البحث وتتبع طلب برقم الطلب" else "Track by Order Number",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchOrderIdInput,
                                onValueChange = { searchOrderIdInput = it.uppercase() },
                                placeholder = { Text("BNZ-XXXX", fontSize = 13.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("search_order_id_input")
                            )
                            Button(
                                onClick = {
                                    val clean = searchOrderIdInput.trim().uppercase()
                                    if (clean.isNotBlank()) {
                                        val found = orders.find { it.id == clean }
                                        if (found != null) {
                                            onTrackOrder(clean)
                                        } else {
                                            Toast.makeText(
                                                context,
                                                if (isArabic) "الطلب غير موجود، تأكد من الرقم المدخل" else "Order not found",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FlameOrange),
                                modifier = Modifier.testTag("search_track_button")
                            ) {
                                Text(if (isArabic) "تتبع" else "Track", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // If user has orders in history, show them as quick cards with option to track or delete
            if (myOrders.isNotEmpty()) {
                item {
                    Text(
                        text = if (isArabic) "طلباتك السابقة:" else "Your Past Orders:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                items(myOrders, key = { it.id }) { myOrd ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = "#${myOrd.id}", fontWeight = FontWeight.Black, fontSize = 14.sp)
                                    StatusBadge(status = myOrd.orderStatus, isArabic = isArabic)
                                }
                                Text(
                                    text = formatTimestamp(myOrd.createdAt, isArabic),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatCurrency(myOrd.total, isArabic),
                                    fontSize = 12.sp,
                                    color = FlameOrange,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = { onTrackOrder(myOrd.id) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = FlameOrange),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(if (isArabic) "تتبع" else "Track", fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = {
                                        BunzoRepository.deleteOrder(myOrd.id)
                                        Toast.makeText(
                                            context,
                                            if (isArabic) "تم حذف الطلب من سجل التتبع" else "Order deleted from tracking",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.Delete,
                                        contentDescription = "Delete",
                                        tint = StatusCancelled,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onBackHome,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FlameOrange),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.RestaurantMenu, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "تصفح قائمة طعام بنزو والطلب الآن" else "Browse Menu & Order Now",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        return
    }

    val currentStatus = order.orderStatus

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("order_tracking_screen"),
        contentPadding = PaddingValues(16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Order Header Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isArabic) "رقم الطلب #${order.id}" else "Order #${order.id}",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = formatTimestamp(order.createdAt, isArabic),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            StatusBadge(status = currentStatus, isArabic = isArabic)
                            IconButton(
                                onClick = { showDeleteConfirmDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = if (isArabic) "حذف من التتبع" else "Delete",
                                    tint = StatusCancelled,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = FlameOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isArabic) "تحديث لحظي ومباشر لحالة الطلب فور تغييرها من المطعم" else "Live real-time status stream via Firestore",
                                fontSize = 12.sp,
                                color = FlameOrangeDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Live Pipeline Visual Timeline
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isArabic) "مراحل تجهيز وتوصيل طلبك" else "Order Progress",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (currentStatus == OrderStatus.CANCELLED) {
                        Surface(
                            color = StatusCancelled.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, StatusCancelled),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Cancel, contentDescription = null, tint = StatusCancelled)
                                Column {
                                    Text(
                                        text = if (isArabic) "تم إلغاء هذا الطلب" else "This order was cancelled",
                                        fontWeight = FontWeight.Bold,
                                        color = StatusCancelled
                                    )
                                    if (!order.cancelReason.isNullOrBlank()) {
                                        Text(
                                            text = if (isArabic) "السبب: ${order.cancelReason}" else "Reason: ${order.cancelReason}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Steps: 1. Received -> 2. Preparing -> 3. In Transit/Ready -> 4. Delivered
                        val stepIndex = when (currentStatus) {
                            OrderStatus.RECEIVED -> 1
                            OrderStatus.PREPARING -> 2
                            OrderStatus.ON_THE_WAY, OrderStatus.READY_FOR_PICKUP, OrderStatus.SERVED -> 3
                            OrderStatus.DELIVERED -> 4
                            OrderStatus.CANCELLED -> 0
                        }

                        val step3Title = when (order.parsedOrderType) {
                            OrderType.DELIVERY -> if (isArabic) "خرج للتوصيل مع السائق" else "Out for delivery"
                            OrderType.PICKUP -> if (isArabic) "جاهز للاستلام من الفرع" else "Ready for pickup"
                            OrderType.TABLE -> if (isArabic) "قُدِّم لطاولتك بالصالة" else "Served to your table"
                        }

                        TimelineStep(
                            stepNumber = 1,
                            title = if (isArabic) "تم استلام الطلب بالمطعم" else "Order Received",
                            subtitle = if (isArabic) "وصل طلبك لطاقم العمل بنجاح" else "Your order reached the restaurant",
                            isCompleted = stepIndex > 1,
                            isActive = stepIndex == 1,
                            isLast = false
                        )
                        TimelineStep(
                            stepNumber = 2,
                            title = if (isArabic) "قيد التحضير والشواء في المطبخ" else "Preparing in Kitchen",
                            subtitle = if (isArabic) "يتم إعداد البرجر الطازج والبطاطا" else "Patties are grilling fresh",
                            isCompleted = stepIndex > 2,
                            isActive = stepIndex == 2,
                            isLast = false
                        )
                        TimelineStep(
                            stepNumber = 3,
                            title = step3Title,
                            subtitle = if (isArabic) "طلبك جاهز وسينتهي قريباً" else "Order is in dispatch",
                            isCompleted = stepIndex > 3,
                            isActive = stepIndex == 3,
                            isLast = false
                        )
                        TimelineStep(
                            stepNumber = 4,
                            title = if (isArabic) "تم التسليم بنجاح" else "Delivered Successfully",
                            subtitle = if (isArabic) "صحتين وهنا من عائلة بنزو!" else "Enjoy your meal with Bunzo!",
                            isCompleted = stepIndex == 4,
                            isActive = stepIndex == 4,
                            isLast = true
                        )
                    }
                }
            }
        }

        // Delivery Information Card (Organized & Beautiful)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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
                                shape = RoundedCornerShape(8.dp),
                                color = FlameOrange.copy(alpha = 0.12f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (order.parsedOrderType == OrderType.DELIVERY) Icons.Default.Moped else if (order.parsedOrderType == OrderType.PICKUP) Icons.Default.Storefront else Icons.Default.TableBar,
                                        contentDescription = null,
                                        tint = FlameOrange,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isArabic) "بيانات التوصيل والاستلام" else "Delivery & Destination",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            color = FlameOrange.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (isArabic) order.parsedOrderType.titleAr else order.parsedOrderType.titleEn,
                                color = FlameOrange,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 2.dp))

                    // Destination Address Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = FlameOrange,
                                    modifier = Modifier.size(20.dp).padding(top = 2.dp)
                                )
                                Column {
                                    Text(
                                        text = if (isArabic) "الوجهة والعنوان المحدد:" else "Delivery Destination:",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${order.customerRegion} • ${order.address}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

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
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = StatusDelivered,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (isArabic) "هاتف التواصل للتسليم:" else "Contact Phone:",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = order.customerPhone,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${order.customerPhone}"))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Call",
                                        tint = StatusDelivered,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (!order.notes.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AmberGold.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, AmberGoldDark.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = AmberGoldDark,
                                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                )
                                Column {
                                    Text(
                                        text = if (isArabic) "ملاحظات وتوجيهات الطلب:" else "Special Instructions:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AmberGoldDark
                                    )
                                    Text(
                                        text = order.notes,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Order Items and Invoice
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isArabic) "تفاصيل الوجبات المطلوبة" else "Ordered Meals",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    order.items.forEach { item ->
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "${item.qty}x ${item.name}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = formatCurrency(item.price * item.qty, isArabic), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            if (item.notes.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = FlameOrange.copy(alpha = 0.09f),
                                    border = BorderStroke(0.5.dp, FlameOrange.copy(alpha = 0.3f)),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Outlined.Edit,
                                            contentDescription = null,
                                            tint = FlameOrange,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Text(
                                            text = item.notes,
                                            fontSize = 11.sp,
                                            color = FlameOrange,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = if (isArabic) "أجور التوصيل:" else "Delivery fee:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = if (order.deliveryFee > 0) formatCurrency(order.deliveryFee, isArabic) else if (isArabic) "مجاني" else "Free", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = if (isArabic) "المجموع الكلي المدفوع:" else "Total Paid:", fontWeight = FontWeight.Black, fontSize = 15.sp)
                        Text(text = formatCurrency(order.total, isArabic), color = FlameOrange, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                }
            }
        }

        // WhatsApp Contact Button
        item {
            OutlinedButton(
                onClick = {
                    val message = if (isArabic) {
                        "مرحباً مطعم بنزو، أستفسر عن طلبي رقم #${order.id}"
                    } else {
                        "Hello Bunzo, inquiring about order #${order.id}"
                    }
                    val uri = Uri.parse("https://api.whatsapp.com/send?phone=+963949159274&text=${Uri.encode(message)}")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(intent)
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Chat, contentDescription = null, tint = StatusDelivered)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isArabic) "تواصل مع المطعم عبر واتساب" else "Contact Bunzo via WhatsApp",
                    color = StatusDelivered,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Delete Order from Tracking Button
        item {
            OutlinedButton(
                onClick = { showDeleteConfirmDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusCancelled),
                border = BorderStroke(1.dp, StatusCancelled.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = null, tint = StatusCancelled)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isArabic) "حذف هذا الطلب من سجل التتبع" else "Delete Order from Tracking",
                    color = StatusCancelled,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Back to Home
        item {
            Button(
                onClick = onBackHome,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isArabic) "العودة للقائمة الرئيسية" else "Back to Main Menu",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = StatusCancelled) },
            title = { Text(if (isArabic) "حذف الطلب من سجل التتبع" else "Delete Order from Tracking") },
            text = {
                Text(
                    if (isArabic) "هل أنت متأكد من رغبتك في حذف هذا الطلب (#${order.id}) من سجل التتبع؟"
                    else "Are you sure you want to delete order #${order.id} from tracking?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        BunzoRepository.deleteOrder(order.id)
                        onOrderDeleted()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusCancelled)
                ) {
                    Text(if (isArabic) "نعم، حذف الطلب" else "Yes, Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(if (isArabic) "إلغاء" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun TimelineStep(
    stepNumber: Int,
    title: String,
    subtitle: String,
    isCompleted: Boolean,
    isActive: Boolean,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = CircleShape,
                color = when {
                    isCompleted -> StatusDelivered
                    isActive -> FlameOrange
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                },
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isCompleted) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text(
                            text = stepNumber.toString(),
                            color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .background(
                            if (isCompleted) StatusDelivered else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                )
            }
        }

        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 16.dp)) {
            Text(
                text = title,
                fontWeight = if (isActive || isCompleted) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive) FlameOrange else MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// -------------------------------------------------------------
// 5. Order History Screen
// -------------------------------------------------------------
@Composable
fun OrderHistoryScreen(
    isArabic: Boolean,
    onTrackOrder: (String) -> Unit,
    onHistoryCleared: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val orders by BunzoRepository.orders.collectAsState()
    val currentUser by BunzoRepository.currentUser.collectAsState()
    var orderToDelete by remember { mutableStateOf<Order?>(null) }
    var showClearAllConfirm by remember { mutableStateOf(false) }

    val myOrders = remember(orders, currentUser) {
        currentUser?.let { user ->
            orders.filter { it.customerId == user.uid }.sortedByDescending { it.createdAt }
        } ?: orders.sortedByDescending { it.createdAt }
    }

    if (myOrders.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isArabic) "لا توجد طلبات سابقة" else "No order history yet",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isArabic) "ستظهر هنا جميع طلباتك السابقة مع إمكانية تتبعها لحظياً." else "Your past orders and live tracking will appear here.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("order_history_list"),
        contentPadding = PaddingValues(16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isArabic) "سجل طلباتي السابقة" else "My Past Orders",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
                TextButton(
                    onClick = { showClearAllConfirm = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = StatusCancelled)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteSweep,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isArabic) "حذف السجل بالكامل" else "Clear History",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        items(myOrders, key = { it.id }) { order ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "#${order.id}",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        StatusBadge(status = order.orderStatus, isArabic = isArabic)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatTimestamp(order.createdAt, isArabic),
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = order.items.joinToString(separator = " + ") { "${it.qty}x ${it.name}" },
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatCurrency(order.total, isArabic),
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = FlameOrange
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconButton(
                                onClick = { orderToDelete = order },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = if (isArabic) "حذف من السجل" else "Delete",
                                    tint = StatusCancelled,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Button(
                                onClick = { onTrackOrder(order.id) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FlameOrange),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    text = if (isArabic) "تتبع الطلب" else "Track",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (orderToDelete != null) {
        val ord = orderToDelete!!
        AlertDialog(
            onDismissRequest = { orderToDelete = null },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = StatusCancelled) },
            title = { Text(if (isArabic) "حذف الطلب من السجل" else "Delete Order") },
            text = {
                Text(
                    if (isArabic) "هل أنت متأكد من رغبتك في حذف الطلب #${ord.id} من سجل التتبع والطلبات؟"
                    else "Are you sure you want to delete order #${ord.id} from your history?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = ord.id
                        orderToDelete = null
                        BunzoRepository.deleteOrder(id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusCancelled)
                ) {
                    Text(if (isArabic) "نعم، حذف" else "Yes, Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { orderToDelete = null }) {
                    Text(if (isArabic) "إلغاء" else "Cancel")
                }
            }
        )
    }

    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = StatusCancelled) },
            title = { Text(if (isArabic) "حذف سجل التتبع والطلبات بالكامل" else "Clear All History") },
            text = {
                Text(
                    if (isArabic) "هل أنت متأكد من رغبتك في مسح وحذف جميع طلباتك السابقة من السجل والتتبع؟ لا يمكن التراجع عن هذه الخطوة."
                    else "Are you sure you want to delete all your order history? This cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearAllConfirm = false
                        currentUser?.let { user ->
                            BunzoRepository.clearCustomerOrders(user.uid)
                        } ?: BunzoRepository.clearAllOrders()
                        onHistoryCleared()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusCancelled)
                ) {
                    Text(if (isArabic) "نعم، حذف السجل بالكامل" else "Yes, Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirm = false }) {
                    Text(if (isArabic) "إلغاء" else "Cancel")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// 6. Customer Auth Dialog (Login & Registration as requested)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerAuthDialog(
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    onStaffLoginSuccess: (User) -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Login, 1 = Sign up
    val regions by BunzoRepository.regions.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isSubmitting by remember { mutableStateOf(false) }

    // Login Method: 0 = Phone, 1 = Email
    var loginMethod by remember { mutableIntStateOf(0) }
    var loginPhone by remember { mutableStateOf("") }
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }

    // Sign Up Fields
    var regFirstName by remember { mutableStateOf("") }
    var regLastName by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regRegion by remember { mutableStateOf(regions.firstOrNull()?.nameAr ?: "حلب الشهباء") }
    var regAddress by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regPasswordVisible by remember { mutableStateOf(false) }
    var regConfirmPassword by remember { mutableStateOf("") }
    var regConfirmPasswordVisible by remember { mutableStateOf(false) }
    var agreeTerms by remember { mutableStateOf(false) }
    var regError by remember { mutableStateOf<String?>(null) }
    var isRegionDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .testTag("auth_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedTab == 0) (if (isArabic) "تسجيل الدخول إلى بنـزو" else "Login to Bunzo") else (if (isArabic) "إنشاء حساب زبون جديد" else "Create Bunzo Account"),
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs Switcher
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    indicator = {},
                    divider = {},
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0; loginError = null },
                        text = { Text(if (isArabic) "تسجيل الدخول" else "Login", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1; regError = null },
                        text = { Text(if (isArabic) "إنشاء حساب" else "Sign Up", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    // ---- LOGIN TAB ----
                    // Selector: Phone Number vs Email (Admin / Staff / Customer)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            onClick = {
                                loginMethod = 0
                                loginError = null
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (loginMethod == 0) FlameOrange else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("login_mode_phone")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 7.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = if (loginMethod == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isArabic) "رقم الهاتف" else "Phone",
                                    color = if (loginMethod == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            onClick = {
                                loginMethod = 1
                                loginError = null
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (loginMethod == 1) FlameOrange else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("login_mode_email")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 7.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = if (loginMethod == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isArabic) "البريد الإلكتروني" else "Email",
                                    color = if (loginMethod == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (loginMethod == 0) {
                        Text(
                            text = if (isArabic) "رقم الهاتف:" else "Phone Number:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = loginPhone,
                            onValueChange = {
                                loginPhone = it
                                loginError = null
                            },
                            placeholder = { Text("09XXXXXXXX", fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = FlameOrange) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_phone_input"),
                            shape = RoundedCornerShape(10.dp)
                        )
                    } else {
                        Text(
                            text = if (isArabic) "البريد الإلكتروني:" else "Email Address:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = loginEmail,
                            onValueChange = {
                                loginEmail = it
                                loginError = null
                            },
                            placeholder = { Text("example@domain.com", fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, autoCorrectEnabled = false),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = FlameOrange) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_email_input"),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isArabic) "كلمة المرور (يدعم 40+ خانة):" else "Password (supports 40+ chars):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = {
                            loginPassword = it
                            loginError = null
                        },
                        visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
                        singleLine = !loginPasswordVisible,
                        maxLines = if (loginPasswordVisible) 4 else 1,
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                Icon(
                                    imageVector = if (loginPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        supportingText = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isArabic) "يدعم كلمات سر طويلة (40 خانة وأكثر)" else "Supports 40+ chars passwords",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (loginPassword.isNotEmpty()) {
                                    Text(
                                        text = if (isArabic) "${loginPassword.length} خانة" else "${loginPassword.length} chars",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (loginPassword.length >= 40) FlameOrange else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (loginError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = loginError!!,
                            color = StatusCancelled,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    // Forgot password button -> WhatsApp direct contact
                    val forgotTarget = if (loginMethod == 0) loginPhone else loginEmail
                    TextButton(
                        onClick = {
                            val uri = Uri.parse("https://api.whatsapp.com/send?phone=+963949159274&text=${Uri.encode("أريد استرجاع كلمة السر لحسابي: $forgotTarget")}")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = if (isArabic) "نسيت كلمة السر؟ (استرجاع عبر واتساب)" else "Forgot password? (WhatsApp)",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val targetId = if (loginMethod == 0) loginPhone.trim() else loginEmail.trim()
                            if (targetId.isBlank() || loginPassword.isBlank()) {
                                loginError = if (isArabic) "يرجى ملء كافة الحقول المطلوبة" else "Please fill all required fields"
                                return@Button
                            }

                            isSubmitting = true
                            loginError = null
                            coroutineScope.launch {
                                val isEmailMode = loginMethod == 1 || targetId.contains("@")
                                if (isEmailMode) {
                                    BunzoRepository.ensureFirebaseInitialized(context)
                                    // 1. First attempt: Staff / Admin authentication
                                    val staffRes = BunzoRepository.loginStaff(targetId, loginPassword)
                                    if (staffRes.isSuccess) {
                                        val staffUser = staffRes.getOrNull()!!
                                        isSubmitting = false
                                        onDismiss()
                                        onStaffLoginSuccess(staffUser)
                                        return@launch
                                    }

                                    // 2. Second attempt: Customer login with email
                                    val custRes = BunzoRepository.loginCustomer(targetId, loginPassword)
                                    isSubmitting = false
                                    custRes.onSuccess {
                                        onSuccess()
                                        onDismiss()
                                    }.onFailure { err ->
                                        loginError = staffRes.exceptionOrNull()?.message
                                            ?: err.message
                                            ?: (if (isArabic) "البريد الإلكتروني أو كلمة السر غير صحيحة" else "Invalid email or password")
                                    }
                                } else {
                                    // Customer login with phone
                                    val custRes = BunzoRepository.loginCustomer(targetId, loginPassword)
                                    isSubmitting = false
                                    custRes.onSuccess {
                                        onSuccess()
                                        onDismiss()
                                    }.onFailure { err ->
                                        loginError = err.message ?: (if (isArabic) "رقم الهاتف أو كلمة السر غير صحيحة" else "Invalid phone or password")
                                    }
                                }
                            }
                        },
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FlameOrange),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_login_button")
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text(if (isArabic) "تسجيل الدخول" else "Sign In", fontWeight = FontWeight.Black)
                        }
                    }

                } else {
                    // ---- SIGN UP TAB ----
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = if (isArabic) "الاسم الأول *:" else "First Name *:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = regFirstName,
                                onValueChange = { regFirstName = it },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reg_first_name_input"),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = if (isArabic) "اسم العائلة *:" else "Last Name *:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = regLastName,
                                onValueChange = { regLastName = it },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reg_last_name_input"),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = if (isArabic) "رقم الهاتف (المعرف الرئيسي) *:" else "Phone Number *:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = regPhone,
                        onValueChange = { regPhone = it },
                        placeholder = { Text("09XXXXXXXX", fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_phone_input"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = if (isArabic) "المنطقة / الحي (اختيار أو كتابة يدوية) *:" else "Region / District (Type or Select) *:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    ExposedDropdownMenuBox(
                        expanded = isRegionDropdownExpanded,
                        onExpandedChange = { isRegionDropdownExpanded = !isRegionDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = regRegion,
                            onValueChange = {
                                regRegion = it
                                isRegionDropdownExpanded = true
                            },
                            placeholder = { Text(if (isArabic) "اختر أو اكتب منطقتك..." else "Select or type area...", fontSize = 12.sp) },
                            singleLine = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isRegionDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("reg_region_dropdown"),
                            shape = RoundedCornerShape(8.dp)
                        )
                        val filteredRegRegions = if (regRegion.isBlank()) regions else regions.filter { reg ->
                            reg.nameAr.contains(regRegion, ignoreCase = true) || reg.nameEn.contains(regRegion, ignoreCase = true)
                        }
                        ExposedDropdownMenu(
                            expanded = isRegionDropdownExpanded,
                            onDismissRequest = { isRegionDropdownExpanded = false }
                        ) {
                            filteredRegRegions.forEach { reg ->
                                val regionName = if (isArabic) reg.nameAr else reg.nameEn
                                DropdownMenuItem(
                                    text = { Text(regionName) },
                                    onClick = {
                                        regRegion = regionName
                                        isRegionDropdownExpanded = false
                                    }
                                )
                            }
                            if (regRegion.isNotBlank() && regions.none { it.nameAr.equals(regRegion.trim(), ignoreCase = true) || it.nameEn.equals(regRegion.trim(), ignoreCase = true) }) {
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            Icons.Outlined.Edit,
                                            contentDescription = null,
                                            tint = FlameOrange,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    text = { Text(if (isArabic) "استخدام: \"$regRegion\"" else "Use: \"$regRegion\"") },
                                    onClick = { isRegionDropdownExpanded = false }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = if (isArabic) "العنوان التفصيلي *:" else "Detailed Address *:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = regAddress,
                        onValueChange = { regAddress = it },
                        placeholder = { Text(if (isArabic) "جانب إكسبريس النزهة، بناء..." else "Street, landmark...", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_address_input"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = if (isArabic) "كلمة المرور (6 أحرف أو أكثر - يدعم 40+ خانة) *:" else "Password (min 6 chars - supports 40+ chars) *:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = regPassword,
                        onValueChange = {
                            regPassword = it
                            regError = null
                        },
                        visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
                        singleLine = !regPasswordVisible,
                        maxLines = if (regPasswordVisible) 4 else 1,
                        trailingIcon = {
                            IconButton(onClick = { regPasswordVisible = !regPasswordVisible }) {
                                Icon(
                                    imageVector = if (regPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        supportingText = {
                            if (regPassword.isNotEmpty()) {
                                Text(
                                    text = if (isArabic) "${regPassword.length} خانة" else "${regPassword.length} chars",
                                    fontSize = 10.sp,
                                    color = if (regPassword.length >= 40) FlameOrange else MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_password_input"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = if (isArabic) "تأكيد كلمة المرور *:" else "Confirm Password *:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = regConfirmPassword,
                        onValueChange = {
                            regConfirmPassword = it
                            regError = null
                        },
                        visualTransformation = if (regConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
                        singleLine = !regConfirmPasswordVisible,
                        maxLines = if (regConfirmPasswordVisible) 4 else 1,
                        trailingIcon = {
                            IconButton(onClick = { regConfirmPasswordVisible = !regConfirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (regConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        supportingText = {
                            if (regConfirmPassword.isNotEmpty()) {
                                Text(
                                    text = if (isArabic) "${regConfirmPassword.length} خانة" else "${regConfirmPassword.length} chars",
                                    fontSize = 10.sp,
                                    color = if (regConfirmPassword.length >= 40) FlameOrange else MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_confirm_password_input"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = agreeTerms,
                            onCheckedChange = { agreeTerms = it },
                            modifier = Modifier.testTag("agree_terms_checkbox")
                        )
                        Text(
                            text = if (isArabic) "أوافق على الشروط والأحكام وسياسة الخصوصية" else "I agree to Terms & Conditions",
                            fontSize = 11.sp
                        )
                    }

                    if (regError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = regError!!,
                            color = StatusCancelled,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (regFirstName.isBlank() || regLastName.isBlank() || regPhone.isBlank() || regAddress.isBlank()) {
                                regError = if (isArabic) "جميع الحقول مطلوبة" else "All fields are required"
                                return@Button
                            }
                            if (regPassword != regConfirmPassword) {
                                regError = if (isArabic) "كلمة المرور وتأكيدها غير متطابقين" else "Passwords do not match"
                                return@Button
                            }
                            if (!agreeTerms) {
                                regError = if (isArabic) "يجب الموافقة على الشروط والأحكام للمتابعة" else "Please accept terms"
                                return@Button
                            }

                            isSubmitting = true
                            regError = null
                            coroutineScope.launch {
                                val res = BunzoRepository.registerCustomer(
                                    firstName = regFirstName,
                                    lastName = regLastName,
                                    phone = regPhone,
                                    region = regRegion,
                                    address = regAddress,
                                    password = regPassword
                                )
                                isSubmitting = false
                                res.onSuccess {
                                    onSuccess()
                                    onDismiss()
                                }.onFailure { err ->
                                    regError = err.message ?: (if (isArabic) "فشل إنشاء الحساب" else "Registration failed")
                                }
                            }
                        },
                        enabled = agreeTerms && !isSubmitting,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FlameOrange),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_register_button")
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text(if (isArabic) "إنشاء الحساب ومتابعة الطلب" else "Create Account", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 7. Customer Profile Screen
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerProfileScreen(
    isArabic: Boolean,
    onOpenAuthDialog: () -> Unit,
    onNavigateToStaff: (User) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentUser by BunzoRepository.currentUser.collectAsState()
    val staffSession by BunzoRepository.staffSession.collectAsState()
    val regions by BunzoRepository.regions.collectAsState()
    val context = LocalContext.current

    val user = currentUser
    if (user == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // If logged in as staff / admin, provide quick direct access to dashboard
            if (staffSession != null) {
                val staff = staffSession!!
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CharcoalDark),
                    border = BorderStroke(1.5.dp, FlameOrange),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = FlameOrange.copy(alpha = 0.2f),
                            modifier = Modifier.size(50.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = FlameOrange,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isArabic) "حساب إداري نشط" else "Active Admin / Staff Session",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "${staff.fullName} (${staff.email})",
                            color = AmberGold,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = {
                                BunzoSoundManager.playClick()
                                onNavigateToStaff(staff)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FlameOrange),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (staff.userRole == UserRole.KITCHEN)
                                    (if (isArabic) "فتح شاشة المطبخ (KDS)" else "Open Kitchen KDS")
                                else
                                    (if (isArabic) "فتح لوحة الإدارة والتحكم" else "Open Admin Dashboard"),
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = FlameOrange.copy(alpha = 0.12f),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = null,
                                tint = FlameOrange,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Text(
                        text = if (isArabic) "حساب ضيف" else "Guest Account",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isArabic)
                            "سجّل دخولك الآن لحفظ بياناتك الشخصية وعناوين التوصيل، والاستمتاع بتتبع طلباتك لحظة بلحظة."
                        else
                            "Sign in to save your personal details, delivery addresses, and track orders in real time.",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Button(
                        onClick = {
                            BunzoSoundManager.playClick()
                            onOpenAuthDialog()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FlameOrange),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isArabic) "تسجيل الدخول / إنشاء حساب" else "Login / Sign Up", fontWeight = FontWeight.Bold)
                    }
                }
            }

            SoundSettingsCard(isArabic = isArabic)
        }
        return
    }

    var firstName by remember(user) { mutableStateOf(user.firstName) }
    var lastName by remember(user) { mutableStateOf(user.lastName) }
    var selectedRegion by remember(user) { mutableStateOf(user.region) }
    var address by remember(user) { mutableStateOf(user.address) }
    var isSavedSnackbarVisible by remember { mutableStateOf(false) }
    var isRegionDropdownExpanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val hasChanges = firstName != user.firstName ||
            lastName != user.lastName ||
            selectedRegion != user.region ||
            address != user.address

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen"),
        contentPadding = PaddingValues(16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Profile Identity Header Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(FlameOrange.copy(alpha = 0.08f), Color.Transparent),
                                    radius = 450f
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Avatar Circle with Initials & Star Accent
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Surface(
                                shape = CircleShape,
                                color = Color.Transparent,
                                modifier = Modifier
                                    .size(62.dp)
                                    .background(
                                        Brush.linearGradient(listOf(FlameOrange, AmberGold)),
                                        CircleShape
                                    )
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = user.firstName.take(1).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 26.sp
                                    )
                                }
                            }
                            Surface(
                                shape = CircleShape,
                                color = AmberGold,
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.surface),
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = CharcoalDark,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user.fullName.ifBlank { if (isArabic) "عميل بونزُو" else "Bunzo Customer" },
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(3.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Phone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = user.phone,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Surface(
                                    color = StatusDelivered.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = StatusDelivered,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = if (isArabic) "موثق" else "Verified",
                                            color = StatusDelivered,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Surface(
                                color = if (user.role == "admin") FlameOrange.copy(alpha = 0.12f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        if (user.role == "admin") Icons.Default.AdminPanelSettings else Icons.Default.Fastfood,
                                        contentDescription = null,
                                        tint = if (user.role == "admin") FlameOrange else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (user.role == "admin") {
                                            if (isArabic) "إدارة المطعم (Admin)" else "Restaurant Manager"
                                        } else {
                                            if (isArabic) "زبون بونزُو الدائم 🍔" else "Bunzo Regular Member"
                                        },
                                        color = if (user.role == "admin") FlameOrange else MaterialTheme.colorScheme.primary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Personal Information Section Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = FlameOrange.copy(alpha = 0.12f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Outlined.Badge,
                                    contentDescription = null,
                                    tint = FlameOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = if (isArabic) "البيانات الشخصية" else "Personal Information",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isArabic) "الاسم والبيانات المعتمدة لحسابك" else "Your profile identity & details",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // First Name & Last Name in Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            label = { Text(if (isArabic) "الاسم الأول" else "First Name") },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = if (firstName.isNotBlank()) FlameOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(19.dp)
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FlameOrange,
                                focusedLabelColor = FlameOrange,
                                cursorColor = FlameOrange
                            )
                        )
                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            label = { Text(if (isArabic) "اسم العائلة" else "Last Name") },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.PersonOutline,
                                    contentDescription = null,
                                    tint = if (lastName.isNotBlank()) FlameOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(19.dp)
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FlameOrange,
                                focusedLabelColor = FlameOrange,
                                cursorColor = FlameOrange
                            )
                        )
                    }

                    // Verified Phone Tile (Read-only security badge)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isArabic) "رقم الهاتف المعتمد" else "Registered Phone",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = user.phone,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Surface(
                                color = StatusDelivered.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (isArabic) "معرّف الحساب 🔒" else "Secured 🔒",
                                    color = StatusDelivered,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Delivery Address & Location Section Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AmberGold.copy(alpha = 0.15f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Outlined.LocationOn,
                                    contentDescription = null,
                                    tint = AmberGoldDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = if (isArabic) "عنوان التوصيل المعتمد" else "Default Delivery Address",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isArabic) "لتسريع توصيل وجباتك وتعبئة العنوان تلقائياً" else "Auto-applied at checkout for fast ordering",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Region & District Selection & Manual Input
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = isRegionDropdownExpanded,
                            onExpandedChange = { isRegionDropdownExpanded = !isRegionDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedRegion,
                                onValueChange = {
                                    selectedRegion = it
                                    isRegionDropdownExpanded = true
                                },
                                label = { Text(if (isArabic) "المنطقة / الحي (كتابة يدوية أو اختيار)" else "Region / District (Type or Select)") },
                                placeholder = {
                                    Text(
                                        if (isArabic) "اكتب اسم حيك أو اختر من القائمة..." else "Type neighborhood or select from list...",
                                        fontSize = 12.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.Place,
                                        contentDescription = null,
                                        tint = if (selectedRegion.isNotBlank()) FlameOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                trailingIcon = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (selectedRegion.isNotBlank()) {
                                            IconButton(
                                                onClick = { selectedRegion = "" },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Clear,
                                                    contentDescription = if (isArabic) "مسح" else "Clear",
                                                    tint = MaterialTheme.colorScheme.outline,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isRegionDropdownExpanded)
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FlameOrange,
                                    focusedLabelColor = FlameOrange,
                                    cursorColor = FlameOrange
                                )
                            )

                            val filteredRegions = if (selectedRegion.isBlank()) regions else regions.filter { reg ->
                                reg.nameAr.contains(selectedRegion, ignoreCase = true) || reg.nameEn.contains(selectedRegion, ignoreCase = true)
                            }

                            ExposedDropdownMenu(
                                expanded = isRegionDropdownExpanded,
                                onDismissRequest = { isRegionDropdownExpanded = false }
                            ) {
                                if (filteredRegions.isNotEmpty()) {
                                    filteredRegions.forEach { reg ->
                                        val regionName = if (isArabic) reg.nameAr else reg.nameEn
                                        val isSelected = selectedRegion.trim() == regionName.trim()
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(
                                                        if (isSelected) Icons.Default.CheckCircle else Icons.Outlined.LocationOn,
                                                        contentDescription = null,
                                                        tint = if (isSelected) FlameOrange else MaterialTheme.colorScheme.outline,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Text(
                                                        text = regionName,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) FlameOrange else MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            },
                                            onClick = {
                                                selectedRegion = regionName
                                                isRegionDropdownExpanded = false
                                            }
                                        )
                                    }
                                }

                                if (selectedRegion.isNotBlank() && regions.none { it.nameAr.equals(selectedRegion.trim(), ignoreCase = true) || it.nameEn.equals(selectedRegion.trim(), ignoreCase = true) }) {
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(
                                                Icons.Outlined.Edit,
                                                contentDescription = null,
                                                tint = FlameOrange,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        text = {
                                            Column {
                                                Text(
                                                    text = if (isArabic) "اعتماد حي مخصص: \"$selectedRegion\"" else "Use custom district: \"$selectedRegion\"",
                                                    fontWeight = FontWeight.Bold,
                                                    color = FlameOrange,
                                                    fontSize = 13.sp
                                                )
                                                Text(
                                                    text = if (isArabic) "سيتم حفظ هذا الحي لطلباتك" else "Will be saved as your delivery district",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            isRegionDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Quick suggestion chips for instant selection
                        if (regions.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isArabic) "اقتراحات سريعة:" else "Quick pick:",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                                regions.forEach { reg ->
                                    val regionName = if (isArabic) reg.nameAr else reg.nameEn
                                    val isSelected = selectedRegion.trim() == regionName.trim()
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) FlameOrange.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) FlameOrange else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        ),
                                        onClick = {
                                            BunzoSoundManager.playClick()
                                            selectedRegion = regionName
                                        }
                                    ) {
                                        Text(
                                            text = regionName,
                                            color = if (isSelected) FlameOrange else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Detailed Address Field
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text(if (isArabic) "العنوان التفصيلي" else "Detailed Address") },
                        placeholder = {
                            Text(
                                if (isArabic) "الشارع، رقم المبنى/الشقة، علامة مميزة..." else "Street, building/apt, landmark...",
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Home,
                                contentDescription = null,
                                tint = if (address.isNotBlank()) FlameOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        minLines = 2,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FlameOrange,
                            focusedLabelColor = FlameOrange,
                            cursorColor = FlameOrange
                        )
                    )

                    // Helpful info tip
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Outlined.TipsAndUpdates,
                                contentDescription = null,
                                tint = FlameOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isArabic)
                                    "يتم استخدام هذا العنوان تلقائياً لتسريع طلب وجبتك القادمة بضغطة واحدة."
                                else
                                    "This address is automatically pre-filled when placing your next order.",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // 4. Save & Actions Section
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        BunzoSoundManager.playClick()
                        BunzoRepository.updateUserProfile(firstName, lastName, selectedRegion, address)
                        isSavedSnackbarVisible = true
                        coroutineScope.launch {
                            delay(3500)
                            isSavedSnackbarVisible = false
                        }
                    },
                    enabled = firstName.isNotBlank() && (hasChanges || !isSavedSnackbarVisible),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FlameOrange,
                        disabledContainerColor = FlameOrange.copy(alpha = 0.5f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(
                            text = if (isArabic) {
                                if (hasChanges) "حفظ التعديلات الجديدة" else "البيانات محفوظة ومحدثة"
                            } else {
                                if (hasChanges) "Save Changes" else "Information Saved"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                // Discard changes option if modified
                AnimatedVisibility(visible = hasChanges) {
                    OutlinedButton(
                        onClick = {
                            BunzoSoundManager.playRemove()
                            firstName = user.firstName
                            lastName = user.lastName
                            selectedRegion = user.region
                            address = user.address
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isArabic) "إلغاء واستعادة البيانات السابقة" else "Discard Changes", fontSize = 12.sp)
                    }
                }

                // Feedback Banner
                AnimatedVisibility(
                    visible = isSavedSnackbarVisible,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = StatusDelivered.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, StatusDelivered.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusDelivered,
                                modifier = Modifier.size(20.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isArabic) "تم حفظ بياناتك بنجاح!" else "Saved successfully!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = StatusDelivered
                                )
                                Text(
                                    text = if (isArabic) "تم تحديث بياناتك الشخصية وعنوانك المفضل." else "Your personal information has been updated.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { isSavedSnackbarVisible = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sound Settings Card
        item {
            SoundSettingsCard(isArabic = isArabic)
        }

        // Logout
        item {
            OutlinedButton(
                onClick = {
                    BunzoSoundManager.playRemove()
                    BunzoRepository.logoutCustomer()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusCancelled),
                border = BorderStroke(1.dp, StatusCancelled),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isArabic) "تسجيل الخروج" else "Sign Out", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SoundSettingsCard(isArabic: Boolean) {
    val isSoundEnabled by BunzoRepository.isSoundEnabled.collectAsState()

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = FlameOrange.copy(alpha = 0.12f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = null,
                                tint = FlameOrange,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = if (isArabic) "أصوات النقر والطلبات" else "Fast-Food Sound Effects",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isSoundEnabled) {
                                if (isArabic) "مفعلة (نقرات طبيعية وجرس الكاونتر)" else "Enabled (Natural clicks & counter bell)"
                            } else {
                                if (isArabic) "الأصوات معطلة (صامت)" else "Muted (Silent mode)"
                            },
                            fontSize = 12.sp,
                            color = if (isSoundEnabled) StatusDelivered else MaterialTheme.colorScheme.outline
                        )
                    }
                }
                Switch(
                    checked = isSoundEnabled,
                    onCheckedChange = { checked ->
                        BunzoRepository.toggleSound(checked)
                        if (checked) {
                            BunzoSoundManager.playSuccess()
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = FlameOrange
                    )
                )
            }

            Text(
                text = if (isArabic)
                    "أصوات نقر طبيعية وعادية ملائمة لمطعم الوجبات السريعة، مع جرس كاونتر الاستلام الكلاسيكي عند تأكيد الطلب."
                else
                    "Clean, natural click sounds tailored for a fast-food restaurant, with a classic counter bell on order completion.",
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            if (isSoundEnabled) {
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Text(
                    text = if (isArabic) "تجربة أصوات المطعم:" else "Test Restaurant Sounds:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { BunzoSoundManager.playClick() },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text(if (isArabic) "🔘 نقرة عادية" else "🔘 Normal Click", fontSize = 10.5.sp)
                    }
                    OutlinedButton(
                        onClick = { BunzoSoundManager.playAddToCart() },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text(if (isArabic) "🍔 إضافة وجبة" else "🍔 Add Item", fontSize = 10.5.sp)
                    }
                    OutlinedButton(
                        onClick = { BunzoSoundManager.playSuccess() },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text(if (isArabic) "🛎️ جرس الكاونتر" else "🛎️ Counter Bell", fontSize = 10.5.sp)
                    }
                }
            }
        }
    }
}
