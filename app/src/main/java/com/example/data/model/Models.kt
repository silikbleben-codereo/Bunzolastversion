package com.example.data.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

enum class UserRole(val rawValue: String, val titleAr: String, val titleEn: String) {
    CUSTOMER("customer", "زبون", "Customer"),
    KITCHEN("kitchen", "المطبخ (KDS)", "Kitchen (KDS)"),
    ADMIN("admin", "المدير العام", "Administrator");

    companion object {
        fun fromString(value: String): UserRole {
            return entries.find { it.rawValue.equals(value.trim(), ignoreCase = true) } ?: CUSTOMER
        }
    }
}

@IgnoreExtraProperties
data class User(
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val region: String = "",
    val address: String = "",
    val password: String = "", // Stored customer password for admin visibility & recovery
    val role: String = "customer", // "customer", "kitchen", or "admin"
    val branchId: String = "main_branch",
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    @get:Exclude
    val fullName: String get() = "$firstName $lastName".trim()
    @get:Exclude
    val displayEmail: String get() = if (email.isNotBlank()) email else (if (phone.isNotBlank()) "$phone@bunzo.com" else "")
    @get:Exclude
    val userRole: UserRole get() = UserRole.fromString(role)
}

@IgnoreExtraProperties
data class AuditLog(
    val id: String = "",
    val actorUid: String = "",
    val actorName: String = "",
    val actorRole: String = "",
    val action: String = "",
    val targetId: String = "",
    val branchId: String = "main_branch",
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class Region(
    val id: String = "",
    val nameAr: String = "",
    val nameEn: String = ""
)

@IgnoreExtraProperties
data class Category(
    val id: String = "",
    val nameAr: String = "",
    val nameEn: String = "",
    val image: String = "",
    val imageRes: Int? = null,
    val order: Int = 0
)

@IgnoreExtraProperties
data class Product(
    val id: String = "",
    val nameAr: String = "",
    val nameEn: String = "",
    val descriptionAr: String = "",
    val descriptionEn: String = "",
    val price: Double = 0.0,
    val discountPrice: Double? = null,
    val categoryId: String = "",
    val image: String = "",
    val imageRes: Int? = null,
    val isAvailable: Boolean = true,
    val isFeatured: Boolean = false
) {
    @get:Exclude
    val effectivePrice: Double get() = discountPrice ?: price
}

@IgnoreExtraProperties
data class Branch(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val phone: String = "",
    val lat: Double = 36.2021,
    val lng: Double = 37.1343,
    val workingHours: String = "12:00 PM - 02:00 AM"
)

@IgnoreExtraProperties
data class Coupon(
    val id: String = "",
    val code: String = "",
    val discountPercent: Int = 0,
    val minOrder: Double = 0.0,
    val isActive: Boolean = true,
    val expiryDate: String = "2026-12-31"
)

@IgnoreExtraProperties
data class CartItem(
    val product: Product,
    val quantity: Int = 1,
    val specialNotes: String = ""
) {
    @get:Exclude
    val totalPrice: Double get() = product.effectivePrice * quantity
}

@IgnoreExtraProperties
data class OrderItem(
    val productId: String = "",
    val name: String = "",
    val qty: Int = 1,
    val price: Double = 0.0,
    val notes: String = ""
)

enum class OrderStatus(val rawValue: String, val titleAr: String, val titleEn: String) {
    RECEIVED("received", "تم الاستلام", "Received"),
    PREPARING("preparing", "قيد التحضير", "Preparing"),
    ON_THE_WAY("on_the_way", "خرج للتوصيل", "On the Way"),
    READY_FOR_PICKUP("ready_for_pickup", "جاهز للاستلام", "Ready for Pickup"),
    SERVED("served", "قُدِّم للطاولة", "Served to Table"),
    DELIVERED("delivered", "تم التسليم", "Delivered"),
    CANCELLED("cancelled", "ملغى", "Cancelled");

    companion object {
        fun fromString(value: String): OrderStatus {
            return entries.find { it.rawValue == value } ?: RECEIVED
        }
    }
}

enum class OrderType(val rawValue: String, val titleAr: String, val titleEn: String) {
    DELIVERY("delivery", "توصيل منزلي", "Home Delivery"),
    PICKUP("pickup", "استلام ذاتي", "Self Pickup"),
    TABLE("table", "طلب داخل الصالة (طاولة)", "Dine-in Table");

    companion object {
        fun fromString(value: String): OrderType {
            return entries.find { it.rawValue == value } ?: DELIVERY
        }
    }
}

@IgnoreExtraProperties
data class Order(
    val id: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val customerRegion: String = "",
    val items: List<OrderItem> = emptyList(),
    val subtotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double = 0.0,
    val orderType: String = "delivery",
    val tableNumber: String? = null,
    val branchId: String = "main_branch",
    val address: String = "",
    val paymentMethod: String = "cash", // "cash" or "card_demo"
    val notes: String = "",
    val status: String = "received",
    val cancelReason: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val acceptedAt: Long? = null,
    val deliveredAt: Long? = null,
    val isReadByAdmin: Boolean = false
) {
    @get:Exclude
    val orderStatus: OrderStatus get() = OrderStatus.fromString(status)
    @get:Exclude
    val parsedOrderType: OrderType get() = OrderType.fromString(orderType)
}
