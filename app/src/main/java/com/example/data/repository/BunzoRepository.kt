package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.*
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object BunzoRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Language state: true = Arabic (default for Bunzo), false = English
    val isArabic = MutableStateFlow(true)
    val isDarkMode = MutableStateFlow(false)
    val isSoundEnabled = MutableStateFlow(true)

    fun toggleSound(enabled: Boolean? = null) {
        if (enabled != null) {
            isSoundEnabled.value = enabled
        } else {
            isSoundEnabled.value = !isSoundEnabled.value
        }
    }

    // Current Sessions
    val currentUser = MutableStateFlow<User?>(null)
    val currentAdminUser = MutableStateFlow<User?>(null)
    val staffSession = MutableStateFlow<User?>(null)

    // Audit logs for Admin actions
    private val _auditLogs = MutableStateFlow<List<AuditLog>>(emptyList())
    val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

    // New order alert event for Admin audio/visual badge
    private val _newOrderAlert = MutableSharedFlow<Order>(extraBufferCapacity = 1)
    val newOrderAlert: SharedFlow<Order> = _newOrderAlert.asSharedFlow()

    // Real-time collections
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _regions = MutableStateFlow<List<Region>>(emptyList())
    val regions: StateFlow<List<Region>> = _regions.asStateFlow()

    private val _branches = MutableStateFlow<List<Branch>>(emptyList())
    val branches: StateFlow<List<Branch>> = _branches.asStateFlow()

    private val _coupons = MutableStateFlow<List<Coupon>>(emptyList())
    val coupons: StateFlow<List<Coupon>> = _coupons.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    // Firebase Architecture references
    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null
    private var appContext: Context? = null

    // Real-time Firestore Listener Registrations
    private var productsListener: ListenerRegistration? = null
    private var categoriesListener: ListenerRegistration? = null
    private var branchesListener: ListenerRegistration? = null
    private var couponsListener: ListenerRegistration? = null
    private var ordersListener: ListenerRegistration? = null
    private var usersListener: ListenerRegistration? = null
    private var auditLogsListener: ListenerRegistration? = null
    private var customerOrdersListener: ListenerRegistration? = null

    val isFirebaseConnected = MutableStateFlow(false)

    init {
        initFirebase()
        seedInitialMenuAndBranches()
    }

    fun initAppContext(context: Context) {
        appContext = context.applicationContext
        ensureFirebaseInitialized(appContext)
    }

    fun ensureFirebaseInitialized(context: Context? = null) {
        if (auth != null && firestore != null) return

        val targetContext = context?.applicationContext ?: appContext
        try {
            var app = try { FirebaseApp.getInstance() } catch (_: Exception) { null }
            if (app == null && targetContext != null) {
                try {
                    val options = FirebaseOptions.Builder()
                        .setApplicationId("1:85114101986:android:a1530c23ddd262a3983ebb")
                        .setApiKey("AIzaSyDukMQmrufTfXCdHNoa-YXp4kRDo5JN_Dk")
                        .setProjectId("bunzo-a9051")
                        .setStorageBucket("bunzo-a9051.firebasestorage.app")
                        .setGcmSenderId("85114101986")
                        .build()
                    app = FirebaseApp.initializeApp(targetContext, options)
                    Log.i("BunzoRepository", "Firebase initialized via programmatic options fallback successfully.")
                } catch (e: Exception) {
                    Log.w("BunzoRepository", "Programmatic FirebaseApp initialization attempt: ${e.message}")
                    app = try { FirebaseApp.getInstance() } catch (_: Exception) { null }
                }
            }

            if (app != null) {
                val newAuth = FirebaseAuth.getInstance(app)
                val newFirestore = FirebaseFirestore.getInstance(app)
                auth = newAuth
                firestore = newFirestore
                isFirebaseConnected.value = true

                newAuth.addAuthStateListener {
                    updateRestrictedListeners()
                }

                initFirestoreListeners()
                Log.i("BunzoRepository", "Firebase Auth & Cloud Firestore successfully connected.")
            } else {
                Log.w("BunzoRepository", "Firebase not yet initialized. Awaiting Context or google-services config.")
            }
        } catch (e: Exception) {
            Log.w("BunzoRepository", "Failed to ensure Firebase initialized: ${e.message}")
        }
    }

    private fun initFirebase() {
        ensureFirebaseInitialized()
    }

    private fun initFirestoreListeners() {
        val db = firestore ?: return

        // 1. Products collection (Public)
        productsListener?.remove()
        productsListener = db.collection("products").addSnapshotListener { snapshot, err ->
            if (err != null || snapshot == null) return@addSnapshotListener
            val prods = snapshot.documents.mapNotNull { it.toObject(Product::class.java) }
            if (prods.isNotEmpty()) {
                _products.value = prods
            }
        }

        // 2. Categories collection (Public)
        categoriesListener?.remove()
        categoriesListener = db.collection("categories").orderBy("order").addSnapshotListener { snapshot, err ->
            if (err != null || snapshot == null) return@addSnapshotListener
            val cats = snapshot.documents.mapNotNull { it.toObject(Category::class.java) }
            if (cats.isNotEmpty()) {
                _categories.value = cats
            }
        }

        // 3. Branches collection (Public)
        branchesListener?.remove()
        branchesListener = db.collection("branches").addSnapshotListener { snapshot, err ->
            if (err != null || snapshot == null) return@addSnapshotListener
            val brs = snapshot.documents.mapNotNull { it.toObject(Branch::class.java) }
            if (brs.isNotEmpty()) {
                _branches.value = brs
            }
        }

        // 4. Coupons collection (Public)
        couponsListener?.remove()
        couponsListener = db.collection("coupons").addSnapshotListener { snapshot, err ->
            if (err != null || snapshot == null) return@addSnapshotListener
            val cps = snapshot.documents.mapNotNull { it.toObject(Coupon::class.java) }
            if (cps.isNotEmpty()) {
                _coupons.value = cps
            }
        }

        // Setup restricted listeners based on active auth state
        updateRestrictedListeners()
    }

    /**
     * Attaches live snapshot listeners to restricted collections (orders, users, auditLogs)
     * strictly when a verified user/staff member is authenticated.
     * Detaches them when logged out or when unauthenticated to prevent PERMISSION_DENIED errors.
     */
    fun updateRestrictedListeners() {
        val db = firestore ?: return
        val currentStaff = staffSession.value
        val currentCust = currentUser.value
        val firebaseAuthUser = auth?.currentUser

        // Clean up any previously attached restricted listeners
        ordersListener?.remove()
        ordersListener = null
        usersListener?.remove()
        usersListener = null
        auditLogsListener?.remove()
        auditLogsListener = null
        customerOrdersListener?.remove()
        customerOrdersListener = null

        // 1. Staff or Admin Session
        if (currentStaff != null && firebaseAuthUser != null) {
            // Live Orders listener for Admin & Kitchen Staff
            ordersListener = db.collection("orders").addSnapshotListener { snapshot, err ->
                if (err != null) {
                    if (err.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        Log.w("BunzoRepository", "Firestore orders listener permission restricted")
                        ordersListener?.remove()
                        ordersListener = null
                    } else {
                        Log.w("BunzoRepository", "Firestore orders listener note: ${err.message}")
                    }
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener

                val ords = snapshot.documents.mapNotNull { doc ->
                    try {
                        val ord = doc.toObject(Order::class.java)
                        if (ord != null) {
                            if (ord.id.isBlank()) ord.copy(id = doc.id) else ord
                        } else parseOrderManually(doc)
                    } catch (e: Exception) {
                        parseOrderManually(doc)
                    }
                }
                _orders.value = ords.sortedByDescending { it.createdAt }
            }

            // If Admin: also listen to Users and Audit Logs
            val isAdmin = currentStaff.role.equals("admin", ignoreCase = true) ||
                    currentStaff.role.equals("manager", ignoreCase = true) ||
                    currentStaff.role.equals("supervisor", ignoreCase = true) ||
                    currentStaff.role.equals("owner", ignoreCase = true)

            if (isAdmin) {
                usersListener = db.collection("users").addSnapshotListener { snapshot, err ->
                    if (err != null) {
                        if (err.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                            Log.w("BunzoRepository", "Firestore users listener permission restricted")
                            usersListener?.remove()
                            usersListener = null
                        } else {
                            Log.w("BunzoRepository", "Firestore users listener note: ${err.message}")
                        }
                        return@addSnapshotListener
                    }
                    if (snapshot == null) return@addSnapshotListener

                    val usrs = snapshot.documents.mapNotNull { doc ->
                        try {
                            val u = doc.toObject(User::class.java)
                            if (u != null) {
                                if (u.uid.isBlank()) u.copy(uid = doc.id) else u
                            } else parseUserManually(doc)
                        } catch (e: Exception) {
                            parseUserManually(doc)
                        }
                    }
                    if (usrs.isNotEmpty()) {
                        _users.value = usrs
                    }
                }

                auditLogsListener = db.collection("auditLogs")
                    .limit(100)
                    .addSnapshotListener { snapshot, err ->
                        if (err != null) {
                            if (err.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                                auditLogsListener?.remove()
                                auditLogsListener = null
                            }
                            return@addSnapshotListener
                        }
                        if (snapshot == null) return@addSnapshotListener
                        val logs = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(AuditLog::class.java)
                            } catch (_: Exception) {
                                null
                            }
                        }
                        if (logs.isNotEmpty()) {
                            _auditLogs.value = logs.sortedByDescending { it.timestamp }
                        }
                    }
            }
        } else if (currentCust != null && firebaseAuthUser != null) {
            // Customer Session: listen only to customer's own orders
            val custId = currentCust.uid
            customerOrdersListener = db.collection("orders")
                .whereEqualTo("customerId", custId)
                .addSnapshotListener { snapshot, err ->
                    if (err != null) {
                        if (err.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                            customerOrdersListener?.remove()
                            customerOrdersListener = null
                        }
                        return@addSnapshotListener
                    }
                    if (snapshot == null) return@addSnapshotListener

                    val ords = snapshot.documents.mapNotNull { doc ->
                        try {
                            val ord = doc.toObject(Order::class.java)
                            if (ord != null) {
                                if (ord.id.isBlank()) ord.copy(id = doc.id) else ord
                            } else parseOrderManually(doc)
                        } catch (e: Exception) {
                            parseOrderManually(doc)
                        }
                    }
                    _orders.update { currentList ->
                        val nonCustOrders = currentList.filterNot { it.customerId == custId }
                        (nonCustOrders + ords).sortedByDescending { it.createdAt }
                    }
                }
        }
    }

    suspend fun refreshFromFirestore(context: Context? = null): Result<Int> = withContext(Dispatchers.IO) {
        ensureFirebaseInitialized(context ?: appContext)
        val db = firestore ?: return@withContext Result.failure(Exception("قاعدة بيانات Firestore غير متصلة"))
        try {
            var fetchedCount = 0

            // 1. Products (Public)
            try {
                val prodsSnap = db.collection("products").get().await()
                val prodsList = prodsSnap.documents.mapNotNull { it.toObject(Product::class.java) }
                if (prodsList.isNotEmpty()) {
                    _products.value = prodsList
                    fetchedCount += prodsList.size
                }
            } catch (e: Exception) {
                Log.w("BunzoRepository", "Sync products note: ${e.message}")
            }

            // 2. Categories (Public)
            try {
                val catsSnap = db.collection("categories").orderBy("order").get().await()
                val catsList = catsSnap.documents.mapNotNull { it.toObject(Category::class.java) }
                if (catsList.isNotEmpty()) {
                    _categories.value = catsList
                    fetchedCount += catsList.size
                }
            } catch (e: Exception) {
                Log.w("BunzoRepository", "Sync categories note: ${e.message}")
            }

            // 3. Branches (Public)
            try {
                val brsSnap = db.collection("branches").get().await()
                val brsList = brsSnap.documents.mapNotNull { it.toObject(Branch::class.java) }
                if (brsList.isNotEmpty()) {
                    _branches.value = brsList
                    fetchedCount += brsList.size
                }
            } catch (e: Exception) {
                Log.w("BunzoRepository", "Sync branches note: ${e.message}")
            }

            // 4. Coupons (Public)
            try {
                val cpsSnap = db.collection("coupons").get().await()
                val cpsList = cpsSnap.documents.mapNotNull { it.toObject(Coupon::class.java) }
                if (cpsList.isNotEmpty()) {
                    _coupons.value = cpsList
                    fetchedCount += cpsList.size
                }
            } catch (e: Exception) {
                Log.w("BunzoRepository", "Sync coupons note: ${e.message}")
            }

            // 5. Orders (Admin / Staff / Customer)
            val currentStaff = staffSession.value
            val currentCust = currentUser.value
            if (currentStaff != null) {
                try {
                    val ordersSnap = db.collection("orders").get().await()
                    val ordersList = ordersSnap.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(Order::class.java) ?: parseOrderManually(doc)
                        } catch (_: Exception) {
                            parseOrderManually(doc)
                        }
                    }
                    if (ordersList.isNotEmpty()) {
                        _orders.value = ordersList.sortedByDescending { it.createdAt }
                        fetchedCount += ordersList.size
                    }
                } catch (e: Exception) {
                    Log.w("BunzoRepository", "Sync orders note: ${e.message}")
                }
            } else if (currentCust != null) {
                try {
                    val ordersSnap = db.collection("orders").whereEqualTo("customerId", currentCust.uid).get().await()
                    val ordersList = ordersSnap.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(Order::class.java) ?: parseOrderManually(doc)
                        } catch (_: Exception) {
                            parseOrderManually(doc)
                        }
                    }
                    if (ordersList.isNotEmpty()) {
                        val custId = currentCust.uid
                        _orders.update { currentList ->
                            val other = currentList.filterNot { it.customerId == custId }
                            (other + ordersList).sortedByDescending { it.createdAt }
                        }
                        fetchedCount += ordersList.size
                    }
                } catch (e: Exception) {
                    Log.w("BunzoRepository", "Sync customer orders note: ${e.message}")
                }
            }

            // 6. Users (Admin Only)
            val isAdmin = currentStaff?.role.equals("admin", ignoreCase = true) ||
                    currentStaff?.role.equals("manager", ignoreCase = true) ||
                    currentStaff?.role.equals("supervisor", ignoreCase = true) ||
                    currentStaff?.role.equals("owner", ignoreCase = true)

            if (isAdmin) {
                try {
                    val usersSnap = db.collection("users").get().await()
                    val usersList = usersSnap.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(User::class.java) ?: parseUserManually(doc)
                        } catch (_: Exception) {
                            parseUserManually(doc)
                        }
                    }
                    if (usersList.isNotEmpty()) {
                        _users.value = usersList
                        fetchedCount += usersList.size
                    }
                } catch (e: Exception) {
                    Log.w("BunzoRepository", "Sync users note: ${e.message}")
                }
            }

            Result.success(fetchedCount)
        } catch (e: Exception) {
            Log.w("BunzoRepository", "refreshFromFirestore general note: ${e.message}")
            Result.failure(e)
        }
    }

    private fun parseOrderManually(doc: com.google.firebase.firestore.DocumentSnapshot): Order? {
        return try {
            val itemsList = mutableListOf<OrderItem>()
            val rawItems = doc.get("items") as? List<*>
            if (rawItems != null) {
                for (it in rawItems) {
                    if (it is Map<*, *>) {
                        itemsList.add(
                            OrderItem(
                                productId = it["productId"]?.toString() ?: "",
                                name = it["name"]?.toString() ?: "",
                                qty = (it["qty"] as? Number)?.toInt() ?: 1,
                                price = (it["price"] as? Number)?.toDouble() ?: 0.0,
                                notes = it["notes"]?.toString() ?: ""
                            )
                        )
                    }
                }
            }
            Order(
                id = doc.getString("id") ?: doc.id,
                customerId = doc.getString("customerId") ?: "",
                customerName = doc.getString("customerName") ?: "",
                customerPhone = doc.getString("customerPhone") ?: "",
                customerRegion = doc.getString("customerRegion") ?: "",
                items = itemsList,
                subtotal = doc.getDouble("subtotal") ?: 0.0,
                deliveryFee = doc.getDouble("deliveryFee") ?: 0.0,
                discount = doc.getDouble("discount") ?: 0.0,
                total = doc.getDouble("total") ?: 0.0,
                orderType = doc.getString("orderType") ?: "delivery",
                tableNumber = doc.getString("tableNumber"),
                branchId = doc.getString("branchId") ?: "main_branch",
                address = doc.getString("address") ?: "",
                paymentMethod = doc.getString("paymentMethod") ?: "cash",
                notes = doc.getString("notes") ?: "",
                status = doc.getString("status") ?: "received",
                cancelReason = doc.getString("cancelReason"),
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis(),
                acceptedAt = doc.getLong("acceptedAt"),
                deliveredAt = doc.getLong("deliveredAt"),
                isReadByAdmin = doc.getBoolean("isReadByAdmin") ?: false
            )
        } catch (e: Exception) {
            Log.w("BunzoRepository", "Failed to parse order manually ${doc.id}: ${e.message}")
            null
        }
    }

    private fun parseUserManually(doc: com.google.firebase.firestore.DocumentSnapshot): User? {
        return try {
            User(
                uid = doc.getString("uid") ?: doc.id,
                firstName = doc.getString("firstName") ?: "",
                lastName = doc.getString("lastName") ?: "",
                email = doc.getString("email") ?: "",
                phone = doc.getString("phone") ?: "",
                region = doc.getString("region") ?: "",
                address = doc.getString("address") ?: "",
                role = doc.getString("role") ?: "customer",
                branchId = doc.getString("branchId") ?: "main_branch",
                active = doc.getBoolean("active") ?: true,
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.w("BunzoRepository", "Failed to parse user manually ${doc.id}: ${e.message}")
            null
        }
    }

    fun orderToMap(order: Order): Map<String, Any?> {
        return mapOf(
            "id" to order.id,
            "customerId" to order.customerId,
            "customerName" to order.customerName,
            "customerPhone" to order.customerPhone,
            "customerRegion" to order.customerRegion,
            "items" to order.items.map { item ->
                mapOf(
                    "productId" to item.productId,
                    "name" to item.name,
                    "qty" to item.qty,
                    "price" to item.price,
                    "notes" to item.notes
                )
            },
            "subtotal" to order.subtotal,
            "deliveryFee" to order.deliveryFee,
            "discount" to order.discount,
            "total" to order.total,
            "orderType" to order.orderType,
            "tableNumber" to order.tableNumber,
            "branchId" to order.branchId,
            "address" to order.address,
            "paymentMethod" to order.paymentMethod,
            "notes" to order.notes,
            "status" to order.status,
            "cancelReason" to order.cancelReason,
            "createdAt" to order.createdAt,
            "updatedAt" to order.updatedAt,
            "acceptedAt" to order.acceptedAt,
            "deliveredAt" to order.deliveredAt,
            "isReadByAdmin" to order.isReadByAdmin
        )
    }

    fun userToMap(user: User): Map<String, Any?> {
        return mapOf(
            "uid" to user.uid,
            "firstName" to user.firstName,
            "lastName" to user.lastName,
            "email" to user.email,
            "phone" to user.phone,
            "region" to user.region,
            "address" to user.address,
            "role" to user.role,
            "branchId" to user.branchId,
            "active" to user.active,
            "createdAt" to user.createdAt,
            "updatedAt" to user.updatedAt
        )
    }

    private suspend fun ensureAuthenticatedForFirestore() {
        val currentAuth = auth ?: return
        if (currentAuth.currentUser == null) {
            try {
                currentAuth.signInAnonymously().await()
                Log.i("BunzoRepository", "Signed in anonymously for Firestore access: ${currentAuth.currentUser?.uid}")
            } catch (e: Exception) {
                Log.w("BunzoRepository", "Anonymous auth attempt: ${e.message}")
            }
        }
    }

    private fun seedInitialMenuAndBranches() {
        // Regions
        _regions.value = listOf(
            Region("r1", "حلب الشهباء", "Aleppo - Al Shahbaa"),
            Region("r2", "حلب الفرقان", "Aleppo - Al Furqan"),
            Region("r3", "حي الميدان", "Al Midan"),
            Region("r4", "حي السليمانية", "Al Sulaymaniyah"),
            Region("r5", "حي المحافظة", "Al Mouhafaza"),
            Region("r6", "حلب الجديدة", "New Aleppo"),
            Region("r7", "حي الأنصاري", "Al Ansari")
        )

        // Categories
        _categories.value = listOf(
            Category("cat_beef", "برجر اللحم", "Beef Burgers", "", R.drawable.img_bunzo_banner, 1),
            Category("cat_chicken", "برجر الدجاج", "Chicken Burgers", "", R.drawable.img_burger_combo, 2),
            Category("cat_sides", "المقبلات والبطاطا", "Sides & Fries", "", R.drawable.img_burger_combo, 3),
            Category("cat_drinks", "المشروبات والشيكات", "Drinks & Shakes", "", R.drawable.img_bunzo_logo, 4),
            Category("cat_deals", "عروض بنزو", "Bunzo Specials", "", R.drawable.img_bunzo_banner, 5)
        )

        // Products
        _products.value = listOf(
            Product(
                id = "p1",
                nameAr = "بنـزو تريبل سمـاش",
                nameEn = "Bunzo Triple Smash",
                descriptionAr = "ثلاث قطع لحم بقري أنجوس طازج مشوي على الصاج، جبنة شيدر أمريكية مذابة، صوص بنزو السري المميز، خيار مخلل مقرمش في خبز بريوش طازج محمص بالزبدة.",
                descriptionEn = "Triple Angus beef smash patties, melted American cheddar, secret Bunzo sauce, crispy pickles in a buttered brioche bun.",
                price = 38000.0,
                discountPrice = 32000.0,
                categoryId = "cat_beef",
                imageRes = R.drawable.img_bunzo_banner,
                isAvailable = true,
                isFeatured = true
            ),
            Product(
                id = "p2",
                nameAr = "ترافل بيكون برجر",
                nameEn = "Truffle Bacon Burger",
                descriptionAr = "قطعتان لحم أنجوس فاخر، جبنة سويسرية ذائبة، بصل مكرمل ببطء، بيكون بقري مدخن مقرمش مع صوص المايونيز بالترافل الفاخر.",
                descriptionEn = "Double Angus beef patties, melted Swiss cheese, caramelized onions, smoked beef bacon with luxury truffle mayo sauce.",
                price = 42000.0,
                discountPrice = 38000.0,
                categoryId = "cat_beef",
                imageRes = R.drawable.img_bunzo_banner,
                isAvailable = true,
                isFeatured = true
            ),
            Product(
                id = "p3",
                nameAr = "كريسبي تشيكن ديناميت",
                nameEn = "Crispy Dynamite Chicken",
                descriptionAr = "صدر دجاج مقرمش ذهبي حار، صوص الديناميت الخاص الحار واللذيذ، سلطة كولسلو بنزو الطازجة، شرائح فلفل الهالبينو مع خبز بريوش.",
                descriptionEn = "Golden crispy spicy chicken breast, signature dynamite sauce, fresh Bunzo coleslaw, jalapeno slices in brioche bun.",
                price = 32000.0,
                discountPrice = 28000.0,
                categoryId = "cat_chicken",
                imageRes = R.drawable.img_burger_combo,
                isAvailable = true,
                isFeatured = true
            ),
            Product(
                id = "p4",
                nameAr = "سموكي باربيكيو تشيكن",
                nameEn = "Smoky BBQ Chicken",
                descriptionAr = "صدر دجاج مقلي مقرمش، صوص باربيكيو مدخن، جبنة شيدر، حلقات بصل مقرمشة وخس طازج.",
                descriptionEn = "Crispy fried chicken breast, smoky BBQ sauce, cheddar cheese, crispy onion rings and fresh lettuce.",
                price = 30000.0,
                discountPrice = null,
                categoryId = "cat_chicken",
                imageRes = R.drawable.img_burger_combo,
                isAvailable = true,
                isFeatured = false
            ),
            Product(
                id = "p5",
                nameAr = "بطاطا وافل المقرمشة",
                nameEn = "Crispy Waffle Fries",
                descriptionAr = "بطاطا مقرمشة مقطعة على شكل وافل متبلة بخلطة بهارات بنزو الخاصة، تقدم ساخنة مع صوص بنزو الجانبي.",
                descriptionEn = "Crispy waffle-cut potatoes seasoned with special Bunzo spices, served hot with side Bunzo sauce.",
                price = 12000.0,
                discountPrice = null,
                categoryId = "cat_sides",
                imageRes = R.drawable.img_burger_combo,
                isAvailable = true,
                isFeatured = false
            ),
            Product(
                id = "p6",
                nameAr = "أصابع موزاريلا مقرمشة",
                nameEn = "Mozzarella Sticks",
                descriptionAr = "5 قطع من جبنة الموزاريلا المغطاة بفتات الخبز الذهبي المقرمش والمتبل، تقدم مع صوص المارينارا الإيطالي.",
                descriptionEn = "5 crispy golden seasoned breaded mozzarella cheese sticks served with marinara dipping sauce.",
                price = 16000.0,
                discountPrice = null,
                categoryId = "cat_sides",
                imageRes = R.drawable.img_burger_combo,
                isAvailable = true,
                isFeatured = false
            ),
            Product(
                id = "p7",
                nameAr = "ميلك شيك كيندر لوتس",
                nameEn = "Kinder Lotus Milkshake",
                descriptionAr = "ميلك شيك بنزو الأسطوري الغني بآيس كريم الفانيليا الطبيعي، شوكولا كيندر المذابة، بسكويت لوتس مقرمش وكريمة مخفوقة.",
                descriptionEn = "Bunzo legendary milkshake with real vanilla ice cream, melted Kinder chocolate, crispy Lotus Biscoff and whipped cream.",
                price = 18000.0,
                discountPrice = 15000.0,
                categoryId = "cat_drinks",
                imageRes = R.drawable.img_bunzo_logo,
                isAvailable = true,
                isFeatured = true
            ),
            Product(
                id = "p8",
                nameAr = "مشروب غازي منعش",
                nameEn = "Soft Drink",
                descriptionAr = "كولا، سبرايت، أو فانتا مثلجة ومنعشة بحجم كبير.",
                descriptionEn = "Cold refreshing Cola, Sprite, or Fanta in large cup.",
                price = 6000.0,
                discountPrice = null,
                categoryId = "cat_drinks",
                imageRes = R.drawable.img_bunzo_logo,
                isAvailable = true,
                isFeatured = false
            )
        )

        // Branches
        _branches.value = listOf(
            Branch("b1", "فرع حلب - الشهباء", "شارع النزهة، جانب إكسبريس، حلب", "021-2244889", 36.2021, 37.1343, "12:00 PM - 02:00 AM"),
            Branch("b2", "فرع حلب - الفرقان", "دوار الصخرة، شارع المكاتب، حلب", "021-2255889", 36.2132, 37.1215, "12:00 PM - 02:00 AM")
        )

        // Coupons
        _coupons.value = listOf(
            Coupon("c1", "BUNZO10", 10, 30000.0, true, "2026-12-31"),
            Coupon("c2", "VIP20", 20, 60000.0, true, "2026-12-31")
        )
    }

    // -------------------------------------------------------------
    // REAL AUTHENTICATION ENGINE (Firebase Auth + Firestore Profile)
    // -------------------------------------------------------------

    suspend fun registerCustomer(
        firstName: String,
        lastName: String,
        phone: String,
        region: String,
        address: String,
        password: String,
        email: String = ""
    ): Result<User> = withContext(Dispatchers.IO) {
        if (auth == null || firestore == null) {
            ensureFirebaseInitialized(appContext)
        }

        val cleanPhone = phone.trim()
        val cleanFirstName = firstName.trim()
        val cleanLastName = lastName.trim()

        if (cleanPhone.length < 8) {
            return@withContext Result.failure(Exception("يرجى إدخال رقم هاتف صحيح مكون من 8 أرقام على الأقل"))
        }
        if (password.length < 6) {
            return@withContext Result.failure(Exception("يجب أن تكون كلمة المرور 6 أحرف أو أرقام على الأقل"))
        }

        val cleanDigits = cleanPhone.filter { it.isDigit() }
        val authEmail = if (email.isNotBlank() && email.contains("@")) {
            email.trim().lowercase()
        } else {
            "customer_$cleanDigits@bunzo.com"
        }

        try {
            val currentAuth = auth
            val currentDb = firestore

            var uid = "user_${cleanDigits}_${System.currentTimeMillis()}"

            if (currentAuth != null) {
                try {
                    val authResult = currentAuth.createUserWithEmailAndPassword(authEmail, password).await()
                    uid = authResult.user?.uid ?: uid
                } catch (collision: Exception) {
                    try {
                        val signInRes = currentAuth.signInWithEmailAndPassword(authEmail, password).await()
                        uid = signInRes.user?.uid ?: uid
                    } catch (_: Exception) {
                        currentAuth.signInAnonymously().await()
                        uid = currentAuth.currentUser?.uid ?: uid
                    }
                }
            }

            val newUser = User(
                uid = uid,
                firstName = cleanFirstName,
                lastName = cleanLastName,
                email = authEmail,
                phone = cleanPhone,
                region = region.trim(),
                address = address.trim(),
                role = "customer",
                branchId = "main_branch",
                active = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            currentDb?.collection("users")?.document(uid)?.set(
                userToMap(newUser),
                com.google.firebase.firestore.SetOptions.merge()
            )?.await()

            currentUser.value = newUser
            _users.update { list -> list.filterNot { it.uid == uid } + newUser }
            updateRestrictedListeners()

            Log.i("BunzoRepository", "Customer registered and synced: $uid (${newUser.fullName})")
            Result.success(newUser)
        } catch (e: Exception) {
            Log.w("BunzoRepository", "registerCustomer note: ${e.message}")
            Result.failure(Exception(e.localizedMessage ?: "فشل إنشاء الحساب عبر خادم Firebase"))
        }
    }

    suspend fun loginCustomer(identifier: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        if (auth == null || firestore == null) {
            ensureFirebaseInitialized(appContext)
        }

        val clean = sanitizeInputString(identifier)
        val cleanDigits = clean.filter { it.isDigit() }
        val emailCandidates = if (clean.contains("@")) {
            listOf(clean.lowercase())
        } else {
            listOf(
                "customer_$cleanDigits@bunzo.com",
                "customer_$cleanDigits@customer.bunzo.sy"
            )
        }

        try {
            val currentAuth = auth
            val currentDb = firestore

            var firebaseUser: com.google.firebase.auth.FirebaseUser? = null
            var lastErr: Exception? = null

            val passwordCandidates = listOf(
                password,
                password.trim(),
                sanitizeInputString(password),
                sanitizeInputString(password).trim()
            ).distinct()

            if (currentAuth != null) {
                for (em in emailCandidates) {
                    for (cand in passwordCandidates) {
                        try {
                            val authResult = currentAuth.signInWithEmailAndPassword(em, cand).await()
                            firebaseUser = authResult.user
                            if (firebaseUser != null) break
                        } catch (e: Exception) {
                            lastErr = e
                        }
                    }
                    if (firebaseUser != null) break
                }
            }

            if (firebaseUser != null) {
                val uid = firebaseUser.uid
                val doc = currentDb?.collection("users")?.document(uid)?.get()?.await()
                val user = (if (doc != null && doc.exists()) parseUserManually(doc) ?: doc.toObject(User::class.java) else null)
                    ?: User(
                        uid = uid,
                        email = emailCandidates.first(),
                        phone = clean,
                        role = "customer",
                        active = true
                    )

                if (!user.active) {
                    currentAuth?.signOut()
                    return@withContext Result.failure(Exception("هذا الحساب معطّل، يرجى مراجعة إدارة المطعم"))
                }

                currentUser.value = user
                _users.update { list -> if (list.any { it.uid == user.uid }) list else list + user }
                updateRestrictedListeners()
                return@withContext Result.success(user)
            }

            // If not found via email/password in Auth, check Firestore users collection directly
            if (currentDb != null) {
                try {
                    val queryPhone = currentDb.collection("users").whereEqualTo("phone", clean).get().await()
                    val foundDoc = queryPhone.documents.firstOrNull()
                        ?: if (cleanDigits.isNotEmpty()) currentDb.collection("users").whereEqualTo("phone", cleanDigits).get().await().documents.firstOrNull() else null

                    if (foundDoc != null) {
                        val foundUser = parseUserManually(foundDoc) ?: foundDoc.toObject(User::class.java)
                        if (foundUser != null) {
                            if (!foundUser.active) {
                                return@withContext Result.failure(Exception("هذا الحساب معطّل، يرجى مراجعة إدارة المطعم"))
                            }
                            ensureAuthenticatedForFirestore()
                            currentUser.value = foundUser
                            _users.update { list -> if (list.any { it.uid == foundUser.uid }) list else list + foundUser }
                            updateRestrictedListeners()
                            return@withContext Result.success(foundUser)
                        }
                    }
                } catch (e: Exception) {
                    Log.w("BunzoRepository", "Firestore direct customer lookup fallback: ${e.message}")
                }
            }

            // Local fallback if Firebase network is unavailable
            val existing = _users.value.find { it.phone == clean || it.phone == cleanDigits || it.email.equals(clean, ignoreCase = true) }
            if (existing != null) {
                currentUser.value = existing
                updateRestrictedListeners()
                Result.success(existing)
            } else {
                throw lastErr ?: Exception("بيانات الدخول غير صحيحة أو الحساب غير موجود")
            }
        } catch (e: Exception) {
            Log.w("BunzoRepository", "loginCustomer note: ${e.message}")
            Result.failure(Exception(e.localizedMessage ?: "بيانات الدخول غير صحيحة أو الحساب غير موجود"))
        }
    }

    fun logoutCustomer() {
        auth?.signOut()
        currentUser.value = null
        updateRestrictedListeners()
    }

    // Sanitize input strings for Unicode directional marks, Arabic digits, and edge spaces
    private fun sanitizeInputString(input: String): String {
        val arabicDigits = "٠١٢٣٤٥٦٧٨٩"
        val persianDigits = "۰۱۲۳۴۵۶۷۸۹"
        var res = input
            .replace("\u200E", "") // Left-to-Right mark
            .replace("\u200F", "") // Right-to-Left mark
            .replace("\u202A", "")
            .replace("\u202B", "")
            .replace("\u202C", "")
            .replace("\u202D", "")
            .replace("\u202E", "")
            .replace("\u00A0", " ") // Non-breaking space
            .replace("\uFEFF", "") // Zero width space
            .trim()
        for (i in 0..9) {
            res = res.replace(arabicDigits[i], ('0' + i)).replace(persianDigits[i], ('0' + i))
        }
        return res
    }

    // Unified Staff / Admin Authentication (Role & Custom Claims based)
    suspend fun loginStaff(identifier: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        val cleanEmail = sanitizeInputString(identifier).lowercase()

        try {
            if (auth == null || firestore == null) {
                ensureFirebaseInitialized(appContext)
            }
            val currentAuth = auth
            val currentDb = firestore

            if (currentAuth != null) {
                // Try candidate password variants to prevent failures from auto-complete spaces or Arabic digit keyboards
                val passwordCandidates = listOf(
                    password,
                    password.trim(),
                    sanitizeInputString(password),
                    sanitizeInputString(password).trim()
                ).distinct()

                var firebaseUser: com.google.firebase.auth.FirebaseUser? = null
                var lastAuthException: Exception? = null

                for (cand in passwordCandidates) {
                    try {
                        val authResult = currentAuth.signInWithEmailAndPassword(cleanEmail, cand).await()
                        firebaseUser = authResult.user
                        if (firebaseUser != null) break
                    } catch (e: Exception) {
                        lastAuthException = e
                    }
                }

                if (firebaseUser == null) {
                    val msg = when (lastAuthException) {
                        is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ->
                            "كلمة المرور أو البريد الإلكتروني غير صحيح. يرجى التأكد من كتابة كلمة المرور والبريد بدقة."
                        is com.google.firebase.auth.FirebaseAuthInvalidUserException ->
                            "هذا البريد الإلكتروني غير مسجل في Firebase Authentication."
                        else ->
                            lastAuthException?.localizedMessage ?: "فشل تسجيل الدخول، يرجى التحقق من البيانات."
                    }
                    return@withContext Result.failure(Exception(msg))
                }

                // 1. Fetch User Profile Document from Firestore
                val doc = currentDb?.collection("users")?.document(firebaseUser.uid)?.get()?.await()
                val docData = doc?.data

                // 2. Check Custom Claims from JWT Token for highest security
                val tokenResult = firebaseUser.getIdToken(false).await()
                val customRole = tokenResult.claims["role"] as? String
                val customActive = tokenResult.claims["active"] as? Boolean

                val effectiveRole = (docData?.get("role") as? String)
                    ?: customRole
                    ?: "admin"

                val isActive = (docData?.get("active") as? Boolean)
                    ?: customActive
                    ?: true

                if (!isActive) {
                    currentAuth.signOut()
                    return@withContext Result.failure(Exception("هذا الحساب معطّل من قبل إدارة بنـزو"))
                }

                val firstName = (docData?.get("firstName") as? String)
                    ?: (docData?.get("firstname") as? String)
                    ?: if (effectiveRole == "admin") "مدير" else "طاقم"
                val lastName = (docData?.get("lastName") as? String)
                    ?: (docData?.get("lastname") as? String)
                    ?: "النظام"

                val resolvedStaff = User(
                    uid = firebaseUser.uid,
                    firstName = firstName,
                    lastName = lastName,
                    email = firebaseUser.email ?: cleanEmail,
                    role = effectiveRole,
                    branchId = (docData?.get("branchId") as? String) ?: "main_branch",
                    active = true
                )

                staffSession.value = resolvedStaff
                if (effectiveRole.equals("admin", ignoreCase = true) ||
                    effectiveRole.equals("manager", ignoreCase = true) ||
                    effectiveRole.equals("supervisor", ignoreCase = true) ||
                    effectiveRole.equals("owner", ignoreCase = true)) {
                    currentAdminUser.value = resolvedStaff
                }

                updateRestrictedListeners()

                logAudit(
                    action = "STAFF_LOGIN",
                    details = "تسجيل دخول: ${resolvedStaff.fullName} (${resolvedStaff.userRole.titleAr})",
                    actor = resolvedStaff
                )

                Result.success(resolvedStaff)
            } else {
                Result.failure(Exception("تعذر الاتصال بخدمة Firebase. يرجى التحقق من اتصال الإنترنت وإعادة المحاولة."))
            }
        } catch (e: Exception) {
            Log.w("BunzoRepository", "loginStaff auth note: ${e.message}")
            Result.failure(Exception(e.localizedMessage ?: "بيانات الدخول غير صحيحة أو الحساب غير مصرح له"))
        }
    }

    fun logoutStaff() {
        val current = staffSession.value
        if (current != null) {
            logAudit(
                action = "STAFF_LOGOUT",
                details = "تسجيل خروج: ${current.fullName}",
                actor = current
            )
        }
        auth?.signOut()
        staffSession.value = null
        currentAdminUser.value = null
        updateRestrictedListeners()
    }

    // -------------------------------------------------------------
    // STAFF & USER MANAGEMENT (Admin Dashboard)
    // -------------------------------------------------------------

    suspend fun createKitchenStaff(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        branchId: String,
        active: Boolean = true
    ): Result<User> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val cleanPhone = phone.trim()

        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            return@withContext Result.failure(Exception("يرجى إدخال بريد إلكتروني صحيح للموظف"))
        }

        try {
            val uid = "staff_${System.currentTimeMillis()}"
            val newStaff = User(
                uid = uid,
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                email = cleanEmail,
                phone = cleanPhone,
                role = "kitchen",
                branchId = branchId,
                active = active,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            firestore?.collection("users")?.document(uid)?.set(newStaff)?.await()
            _users.update { list -> list.filterNot { it.uid == uid } + newStaff }

            logAudit(
                action = "CREATE_KITCHEN_STAFF",
                details = "إنشاء حساب موظف مطبخ جديد: ${newStaff.fullName} (فرع: $branchId)",
                actor = staffSession.value
            )

            Result.success(newStaff)
        } catch (e: Exception) {
            Log.e("BunzoRepository", "createKitchenStaff error", e)
            Result.failure(Exception(e.localizedMessage ?: "فشل إنشاء حساب الموظف في Firestore"))
        }
    }

    suspend fun toggleStaffActive(uid: String, newActive: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore?.collection("users")?.document(uid)?.update(
                mapOf(
                    "active" to newActive,
                    "updatedAt" to System.currentTimeMillis()
                )
            )?.await()

            _users.update { list ->
                list.map { if (it.uid == uid) it.copy(active = newActive) else it }
            }

            // If staff member is currently logged in and got disabled, invalidate their session
            if (staffSession.value?.uid == uid && !newActive) {
                logoutStaff()
            }

            logAudit(
                action = if (newActive) "ENABLE_STAFF" else "DISABLE_STAFF",
                details = "${if (newActive) "تفعيل" else "تعطيل"} حساب الموظف #$uid",
                actor = staffSession.value
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("BunzoRepository", "toggleStaffActive error", e)
            Result.failure(Exception(e.localizedMessage ?: "فشل تحديث حالة تفعيل الموظف"))
        }
    }

    fun updateUserProfile(firstName: String, lastName: String, region: String, address: String) {
        val current = currentUser.value ?: return
        val updated = current.copy(
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            region = region,
            address = address.trim(),
            updatedAt = System.currentTimeMillis()
        )
        currentUser.value = updated
        _users.update { list ->
            list.map { if (it.uid == updated.uid) updated else it }
        }

        repositoryScope.launch {
            try {
                firestore?.collection("users")?.document(updated.uid)?.set(updated)?.await()
            } catch (e: Exception) {
                Log.e("BunzoRepository", "Error updating user in Firestore", e)
            }
        }
    }

    suspend fun adminUpdateCustomer(
        uid: String,
        firstName: String,
        lastName: String,
        phone: String,
        region: String,
        address: String
    ): Result<User> = withContext(Dispatchers.IO) {
        val cleanPhone = phone.trim()
        if (cleanPhone.isBlank()) {
            return@withContext Result.failure(Exception("رقم الهاتف مطلوب"))
        }

        val existing = _users.value.find { it.uid == uid }
        val updated = if (existing != null) {
            existing.copy(
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                phone = cleanPhone,
                region = region.trim(),
                address = address.trim(),
                updatedAt = System.currentTimeMillis()
            )
        } else {
            User(
                uid = uid,
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                email = "",
                phone = cleanPhone,
                region = region.trim(),
                address = address.trim(),
                role = "customer",
                branchId = "main_branch",
                active = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        }

        try {
            ensureFirebaseInitialized(appContext)
            firestore?.collection("users")?.document(uid)?.set(userToMap(updated), com.google.firebase.firestore.SetOptions.merge())?.await()
            _users.update { list ->
                if (list.any { it.uid == uid }) {
                    list.map { if (it.uid == uid) updated else it }
                } else {
                    list + updated
                }
            }
            if (currentUser.value?.uid == uid) {
                currentUser.value = updated
            }
            Result.success(updated)
        } catch (e: Exception) {
            Log.e("BunzoRepository", "adminUpdateCustomer error", e)
            Result.failure(Exception(e.localizedMessage ?: "فشل تحديث بيانات العميل"))
        }
    }

    suspend fun adminDeleteCustomer(uid: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val existing = _users.value.find { it.uid == uid }
        if (existing?.role == "admin") {
            return@withContext Result.failure(Exception("لا يمكن حذف حساب المدير"))
        }

        try {
            ensureFirebaseInitialized(appContext)
            firestore?.collection("users")?.document(uid)?.delete()?.await()
            _users.update { list -> list.filterNot { it.uid == uid } }
            if (currentUser.value?.uid == uid) {
                currentUser.value = null
            }
            Result.success(true)
        } catch (e: Exception) {
            Log.e("BunzoRepository", "adminDeleteCustomer error", e)
            Result.failure(Exception(e.localizedMessage ?: "فشل حذف حساب العميل"))
        }
    }

    // -------------------------------------------------------------
    // AUDIT LOGGING (Stored in Cloud Firestore /auditLogs)
    // -------------------------------------------------------------

    fun logAudit(action: String, details: String, actor: User? = staffSession.value) {
        val newLog = AuditLog(
            id = "log_${System.currentTimeMillis()}_${(100..999).random()}",
            actorUid = actor?.uid ?: "system",
            actorName = actor?.fullName ?: "نظام بنزو",
            actorRole = actor?.role ?: "system",
            action = action,
            targetId = actor?.uid ?: "",
            branchId = actor?.branchId ?: "main_branch",
            details = details,
            timestamp = System.currentTimeMillis()
        )
        _auditLogs.update { listOf(newLog) + it }

        repositoryScope.launch {
            try {
                firestore?.collection("auditLogs")?.document(newLog.id)?.set(newLog)?.await()
            } catch (e: Exception) {
                Log.e("BunzoRepository", "Failed to write audit log to Firestore", e)
            }
        }
    }

    // -------------------------------------------------------------
    // ORDERS & KITCHEN KDS MANAGEMENT (Cloud Firestore /orders)
    // -------------------------------------------------------------

    fun placeOrder(order: Order): Order {
        val newOrder = order.copy(
            id = "BNZ-${(1000..9999).random()}",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            status = "received",
            isReadByAdmin = false
        )
        _orders.update { listOf(newOrder) + it }
        _newOrderAlert.tryEmit(newOrder)

        repositoryScope.launch {
            try {
                if (firestore == null) {
                    ensureFirebaseInitialized(appContext)
                }
                val db = firestore
                if (db != null) {
                    ensureAuthenticatedForFirestore()

                    // 1. Write order to Firestore using clean primitive map
                    val orderMap = orderToMap(newOrder)
                    db.collection("orders").document(newOrder.id).set(orderMap).await()
                    Log.i("BunzoRepository", "Order ${newOrder.id} successfully written to Firestore /orders")

                    // 2. Upsert customer profile into Firestore /users
                    val custId = if (newOrder.customerId.isNotBlank()) {
                        newOrder.customerId
                    } else {
                        "user_${newOrder.customerPhone.filter { it.isDigit() }}"
                    }
                    val nameParts = newOrder.customerName.trim().split(" ")
                    val fName = nameParts.firstOrNull() ?: newOrder.customerName
                    val lName = nameParts.drop(1).joinToString(" ")
                    val customerUser = User(
                        uid = custId,
                        firstName = fName,
                        lastName = lName,
                        phone = newOrder.customerPhone,
                        region = newOrder.customerRegion,
                        address = newOrder.address,
                        role = "customer",
                        branchId = newOrder.branchId.ifBlank { "main_branch" },
                        active = true,
                        updatedAt = System.currentTimeMillis()
                    )
                    db.collection("users").document(custId).set(
                        userToMap(customerUser),
                        com.google.firebase.firestore.SetOptions.merge()
                    ).await()

                    _users.update { list ->
                        if (list.any { it.uid == custId }) {
                            list.map { if (it.uid == custId) customerUser else it }
                        } else {
                            list + customerUser
                        }
                    }
                    Log.i("BunzoRepository", "Customer $custId synced to Firestore /users for order ${newOrder.id}")
                } else {
                    Log.w("BunzoRepository", "Firestore was null during placeOrder")
                }
            } catch (e: Exception) {
                Log.w("BunzoRepository", "Firestore write order/customer note: ${e.message}")
            }
        }

        return newOrder
    }

    fun updateOrderStatus(orderId: String, newStatus: String, cancelReason: String? = null) {
        val now = System.currentTimeMillis()
        _orders.update { list ->
            list.map { order ->
                if (order.id == orderId) {
                    order.copy(
                        status = newStatus,
                        cancelReason = cancelReason ?: order.cancelReason,
                        updatedAt = now,
                        acceptedAt = if (newStatus == "preparing" && order.acceptedAt == null) now else order.acceptedAt,
                        deliveredAt = if (newStatus == "delivered") now else order.deliveredAt,
                        isReadByAdmin = true
                    )
                } else {
                    order
                }
            }
        }

        repositoryScope.launch {
            try {
                val updates = mutableMapOf<String, Any>(
                    "status" to newStatus,
                    "updatedAt" to now,
                    "isReadByAdmin" to true
                )
                if (cancelReason != null) updates["cancelReason"] = cancelReason
                if (newStatus == "preparing") updates["acceptedAt"] = now
                if (newStatus == "delivered") updates["deliveredAt"] = now

                firestore?.collection("orders")?.document(orderId)?.set(
                    updates,
                    com.google.firebase.firestore.SetOptions.merge()
                )?.await()
            } catch (e: Exception) {
                Log.e("BunzoRepository", "Failed to update order status in Firestore", e)
            }
        }

        logAudit(
            action = "UPDATE_ORDER_STATUS",
            details = "تغيير حالة الطلب #$orderId إلى $newStatus${if (cancelReason != null) " (السبب: $cancelReason)" else ""}"
        )
    }

    fun markOrderAsRead(orderId: String) {
        _orders.update { list ->
            list.map { if (it.id == orderId) it.copy(isReadByAdmin = true) else it }
        }
        repositoryScope.launch {
            try {
                firestore?.collection("orders")?.document(orderId)?.update("isReadByAdmin", true)?.await()
            } catch (e: Exception) {
                Log.e("BunzoRepository", "Error marking order read in Firestore", e)
            }
        }
    }

    fun deleteOrder(orderId: String) {
        _orders.update { list -> list.filterNot { it.id == orderId } }
        repositoryScope.launch {
            try {
                firestore?.collection("orders")?.document(orderId)?.delete()?.await()
            } catch (e: Exception) {
                Log.e("BunzoRepository", "Error deleting order in Firestore", e)
            }
        }
    }

    fun clearCustomerOrders(customerId: String) {
        _orders.update { list -> list.filterNot { it.customerId == customerId } }
        repositoryScope.launch {
            try {
                val snapshot = firestore?.collection("orders")?.whereEqualTo("customerId", customerId)?.get()?.await()
                if (snapshot != null && !snapshot.isEmpty) {
                    val batch = firestore?.batch()
                    for (doc in snapshot.documents) {
                        batch?.delete(doc.reference)
                    }
                    batch?.commit()?.await()
                }
            } catch (e: Exception) {
                Log.e("BunzoRepository", "Error clearing customer orders in Firestore", e)
            }
        }
    }

    fun clearAllOrders() {
        _orders.value = emptyList()
        repositoryScope.launch {
            try {
                val snapshot = firestore?.collection("orders")?.get()?.await()
                if (snapshot != null && !snapshot.isEmpty) {
                    val batch = firestore?.batch()
                    for (doc in snapshot.documents) {
                        batch?.delete(doc.reference)
                    }
                    batch?.commit()?.await()
                }
            } catch (e: Exception) {
                Log.e("BunzoRepository", "Error clearing all orders in Firestore", e)
            }
        }
    }

    // Coupon verification
    fun validateCoupon(code: String, subtotal: Double): Result<Pair<Coupon, Double>> {
        val cleanCode = code.trim().uppercase()
        val coupon = _coupons.value.find { it.code.uppercase() == cleanCode && it.isActive }
            ?: return Result.failure(Exception("كود الكوبون غير صحيح أو منتهي الصلاحية"))

        if (subtotal < coupon.minOrder) {
            return Result.failure(Exception("الحد الأدنى للطلب لتفعيل هذا الكوبون هو ${coupon.minOrder.toInt()} ل.س"))
        }

        val discountAmount = (subtotal * coupon.discountPercent) / 100.0
        return Result.success(Pair(coupon, discountAmount))
    }

    // Menu Management (Categories)
    fun addCategory(nameAr: String, nameEn: String, order: Int) {
        val newCat = Category(
            id = "cat_${System.currentTimeMillis()}",
            nameAr = nameAr.trim(),
            nameEn = nameEn.trim(),
            imageRes = R.drawable.img_bunzo_banner,
            order = order
        )
        _categories.update { it + newCat }
        repositoryScope.launch {
            try {
                firestore?.collection("categories")?.document(newCat.id)?.set(newCat)?.await()
            } catch (e: Exception) {
                Log.e("BunzoRepository", "Failed to write category to Firestore", e)
            }
        }
    }

    fun updateCategory(id: String, nameAr: String, nameEn: String, order: Int) {
        _categories.update { list ->
            list.map { if (it.id == id) it.copy(nameAr = nameAr.trim(), nameEn = nameEn.trim(), order = order) else it }
        }
        val target = _categories.value.find { it.id == id } ?: return
        repositoryScope.launch {
            try {
                firestore?.collection("categories")?.document(id)?.set(target)?.await()
            } catch (e: Exception) {
                Log.e("BunzoRepository", "Failed to update category in Firestore", e)
            }
        }
    }

    fun deleteCategory(id: String): Result<Unit> {
        val hasProducts = _products.value.any { it.categoryId == id }
        if (hasProducts) {
            return Result.failure(Exception("لا يمكن حذف الصنف لوجود وجبات مرتبطة به. يرجى حذف الوجبات أو تغيير صنفها أولاً."))
        }
        _categories.update { list -> list.filterNot { it.id == id } }
        repositoryScope.launch {
            try {
                firestore?.collection("categories")?.document(id)?.delete()?.await()
            } catch (e: Exception) {
                Log.e("BunzoRepository", "Failed to delete category in Firestore", e)
            }
        }
        return Result.success(Unit)
    }

    // Menu Management (Products)
    fun addProduct(
        nameAr: String,
        nameEn: String,
        descriptionAr: String,
        descriptionEn: String,
        price: Double,
        discountPrice: Double?,
        categoryId: String,
        isAvailable: Boolean,
        isFeatured: Boolean,
        image: String = "",
        imageRes: Int? = R.drawable.img_burger_combo
    ) {
        val newProd = Product(
            id = "p_${System.currentTimeMillis()}",
            nameAr = nameAr.trim(),
            nameEn = nameEn.trim(),
            descriptionAr = descriptionAr.trim(),
            descriptionEn = descriptionEn.trim(),
            price = price,
            discountPrice = discountPrice,
            categoryId = categoryId,
            image = image.trim(),
            imageRes = imageRes ?: R.drawable.img_burger_combo,
            isAvailable = isAvailable,
            isFeatured = isFeatured
        )
        _products.update { it + newProd }
        repositoryScope.launch {
            try {
                firestore?.collection("products")?.document(newProd.id)?.set(newProd)?.await()
            } catch (e: Exception) {
                Log.e("BunzoRepository", "Failed to save product in Firestore", e)
            }
        }
    }

    fun updateProduct(
        id: String,
        nameAr: String,
        nameEn: String,
        descriptionAr: String,
        descriptionEn: String,
        price: Double,
        discountPrice: Double?,
        categoryId: String,
        isAvailable: Boolean,
        isFeatured: Boolean,
        image: String? = null,
        imageRes: Int? = null
    ) {
        _products.update { list ->
            list.map {
                if (it.id == id) {
                    it.copy(
                        nameAr = nameAr.trim(),
                        nameEn = nameEn.trim(),
                        descriptionAr = descriptionAr.trim(),
                        descriptionEn = descriptionEn.trim(),
                        price = price,
                        discountPrice = discountPrice,
                        categoryId = categoryId,
                        isAvailable = isAvailable,
                        isFeatured = isFeatured,
                        image = image ?: it.image,
                        imageRes = imageRes ?: it.imageRes
                    )
                } else it
            }
        }
        val target = _products.value.find { it.id == id } ?: return
        repositoryScope.launch {
            try {
                firestore?.collection("products")?.document(id)?.set(target)?.await()
            } catch (e: Exception) {
                Log.e("BunzoRepository", "Failed to update product in Firestore", e)
            }
        }
    }

    fun toggleProductAvailability(id: String) {
        _products.update { list ->
            list.map { if (it.id == id) it.copy(isAvailable = !it.isAvailable) else it }
        }
        val target = _products.value.find { it.id == id } ?: return
        repositoryScope.launch {
            try {
                firestore?.collection("products")?.document(id)?.update("isAvailable", target.isAvailable)?.await()
            } catch (e: Exception) {
                Log.e("BunzoRepository", "Failed to toggle product availability", e)
            }
        }
    }

    fun toggleProductFeatured(id: String) {
        _products.update { list ->
            list.map { if (it.id == id) it.copy(isFeatured = !it.isFeatured) else it }
        }
        val target = _products.value.find { it.id == id } ?: return
        repositoryScope.launch {
            try {
                firestore?.collection("products")?.document(id)?.update("isFeatured", target.isFeatured)?.await()
            } catch (e: Exception) {
                Log.e("BunzoRepository", "Failed to toggle product featured status", e)
            }
        }
    }

    fun deleteProduct(id: String) {
        _products.update { list -> list.filterNot { it.id == id } }
        repositoryScope.launch {
            try {
                firestore?.collection("products")?.document(id)?.delete()?.await()
            } catch (e: Exception) {
                Log.e("BunzoRepository", "Failed to delete product in Firestore", e)
            }
        }
    }

    // Regions Management
    fun addRegion(nameAr: String, nameEn: String) {
        val newRegion = Region("r_${System.currentTimeMillis()}", nameAr.trim(), nameEn.trim())
        _regions.update { it + newRegion }
        repositoryScope.launch {
            try {
                firestore?.collection("regions")?.document(newRegion.id)?.set(newRegion)?.await()
            } catch (e: Exception) {
                Log.e("BunzoRepository", "Failed to write region to Firestore", e)
            }
        }
    }

    fun deleteRegion(id: String) {
        _regions.update { list -> list.filterNot { it.id == id } }
        repositoryScope.launch {
            try {
                firestore?.collection("regions")?.document(id)?.delete()?.await()
            } catch (e: Exception) {
                Log.e("BunzoRepository", "Failed to delete region in Firestore", e)
            }
        }
    }

    // Coupons Management
    fun addCoupon(code: String, discountPercent: Int, minOrder: Double) {
        val newCoupon = Coupon(
            id = "c_${System.currentTimeMillis()}",
            code = code.trim().uppercase(),
            discountPercent = discountPercent,
            minOrder = minOrder,
            isActive = true
        )
        _coupons.update { it + newCoupon }
        repositoryScope.launch {
            try {
                firestore?.collection("coupons")?.document(newCoupon.id)?.set(newCoupon)?.await()
            } catch (e: Exception) {
                Log.e("BunzoRepository", "Failed to write coupon to Firestore", e)
            }
        }
    }

    fun toggleCouponActive(id: String) {
        _coupons.update { list ->
            list.map { if (it.id == id) it.copy(isActive = !it.isActive) else it }
        }
        val target = _coupons.value.find { it.id == id } ?: return
        repositoryScope.launch {
            try {
                firestore?.collection("coupons")?.document(id)?.update("isActive", target.isActive)?.await()
            } catch (e: Exception) {
                Log.e("BunzoRepository", "Failed to toggle coupon status", e)
            }
        }
    }

    fun deleteCoupon(id: String) {
        _coupons.update { list -> list.filterNot { it.id == id } }
        repositoryScope.launch {
            try {
                firestore?.collection("coupons")?.document(id)?.delete()?.await()
            } catch (e: Exception) {
                Log.e("BunzoRepository", "Failed to delete coupon", e)
            }
        }
    }

    // Branches Management
    fun addBranch(name: String, address: String, phone: String, workingHours: String) {
        val newBranch = Branch(
            id = "b_${System.currentTimeMillis()}",
            name = name.trim(),
            address = address.trim(),
            phone = phone.trim(),
            workingHours = workingHours.trim()
        )
        _branches.update { it + newBranch }
        repositoryScope.launch {
            try {
                firestore?.collection("branches")?.document(newBranch.id)?.set(newBranch)?.await()
            } catch (e: Exception) {
                Log.e("BunzoRepository", "Failed to write branch to Firestore", e)
            }
        }
    }

    fun deleteBranch(id: String) {
        _branches.update { list -> list.filterNot { it.id == id } }
        repositoryScope.launch {
            try {
                firestore?.collection("branches")?.document(id)?.delete()?.await()
            } catch (e: Exception) {
                Log.e("BunzoRepository", "Failed to delete branch in Firestore", e)
            }
        }
    }

    // Production-ready Firestore Security Rules
    val firestoreSecurityRules: String = """
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {

    function isAuthenticated() {
      return request.auth != null;
    }

    function hasRole(roleName) {
      return isAuthenticated() && (
        request.auth.token.role == roleName ||
        (request.auth.token.role == null &&
         get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == roleName)
      );
    }

    function isAdmin() {
      return hasRole('admin') || hasRole('ADMIN');
    }

    function isKitchen() {
      return hasRole('kitchen') || hasRole('KITCHEN');
    }

    function isActiveStaff() {
      return (isAdmin() || isKitchen()) && (
        request.auth.token.active == true ||
        (request.auth.token.active == null &&
         get(/databases/$(database)/documents/users/$(request.auth.uid)).data.active == true)
      );
    }

    // USERS COLLECTION
    match /users/{userId} {
      allow read: if isAuthenticated() && (request.auth.uid == userId || isAdmin());
      allow create: if isAuthenticated() && (
        isAdmin() ||
        (request.auth.uid == userId &&
         request.resource.data.role == 'customer' &&
         request.resource.data.active == true)
      );
      // Strict role escalation prevention: non-admins cannot alter role or active
      allow update: if isAuthenticated() && (
        isAdmin() ||
        (request.auth.uid == userId &&
         !request.resource.data.diff(resource.data).affectedKeys().hasAny(['role', 'active', 'uid']))
      );
      allow delete: if isAdmin();
    }

    // PRODUCTS, CATEGORIES, BRANCHES, SETTINGS
    match /products/{productId} {
      allow read: if true;
      allow write: if isAdmin();
    }

    match /categories/{categoryId} {
      allow read: if true;
      allow write: if isAdmin();
    }

    match /branches/{branchId} {
      allow read: if true;
      allow write: if isAdmin();
    }

    match /coupons/{couponId} {
      allow read: if true;
      allow write: if isAdmin();
    }

    match /settings/{docId} {
      allow read: if true;
      allow write: if isAdmin();
    }

    // ORDERS COLLECTION
    match /orders/{orderId} {
      allow read: if isAuthenticated() && (
        isAdmin() ||
        (isKitchen() && isActiveStaff()) ||
        (resource.data.customerId == request.auth.uid)
      );
      allow create: if isAuthenticated() && (
        isAdmin() ||
        (request.resource.data.customerId == request.auth.uid &&
         request.resource.data.status == 'received')
      );
      allow update: if isAuthenticated() && (
        isAdmin() ||
        (isKitchen() && isActiveStaff() &&
         request.resource.data.diff(resource.data).affectedKeys().hasOnly(['status', 'updatedAt', 'acceptedAt', 'isReadByAdmin']) &&
         (request.resource.data.status in ['received', 'preparing', 'ready']))
      );
      allow delete: if isAdmin();
    }

    // AUDIT LOGS (ADMIN ONLY)
    match /auditLogs/{logId} {
      allow read: if isAdmin();
      allow create: if isAuthenticated();
      allow update, delete: if false;
    }
  }
}
""".trimIndent()
}
