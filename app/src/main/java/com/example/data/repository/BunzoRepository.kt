package com.example.data.repository

import android.util.Log
import com.example.R
import com.example.data.model.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

    val isFirebaseConnected = MutableStateFlow(false)

    init {
        initFirebase()
        seedInitialMenuAndBranches()
    }

    private fun initFirebase() {
        try {
            // Check if default FirebaseApp is initialized
            val app = try { FirebaseApp.getInstance() } catch (e: Exception) { null }
            if (app != null) {
                auth = FirebaseAuth.getInstance()
                firestore = FirebaseFirestore.getInstance()
                isFirebaseConnected.value = true
                initFirestoreListeners()
                Log.i("BunzoRepository", "Firebase Auth & Cloud Firestore successfully connected.")
            } else {
                Log.w("BunzoRepository", "Firebase not yet initialized. Please place google-services.json in /app/google-services.json.")
            }
        } catch (e: Exception) {
            Log.w("BunzoRepository", "Firebase initialization deferred: ${e.message}")
        }
    }

    private fun initFirestoreListeners() {
        val db = firestore ?: return

        // 1. Products collection
        db.collection("products").addSnapshotListener { snapshot, err ->
            if (err != null || snapshot == null) return@addSnapshotListener
            val prods = snapshot.documents.mapNotNull { it.toObject(Product::class.java) }
            if (prods.isNotEmpty()) {
                _products.value = prods
            }
        }

        // 2. Categories collection
        db.collection("categories").orderBy("order").addSnapshotListener { snapshot, err ->
            if (err != null || snapshot == null) return@addSnapshotListener
            val cats = snapshot.documents.mapNotNull { it.toObject(Category::class.java) }
            if (cats.isNotEmpty()) {
                _categories.value = cats
            }
        }

        // 3. Branches collection
        db.collection("branches").addSnapshotListener { snapshot, err ->
            if (err != null || snapshot == null) return@addSnapshotListener
            val brs = snapshot.documents.mapNotNull { it.toObject(Branch::class.java) }
            if (brs.isNotEmpty()) {
                _branches.value = brs
            }
        }

        // 4. Coupons collection
        db.collection("coupons").addSnapshotListener { snapshot, err ->
            if (err != null || snapshot == null) return@addSnapshotListener
            val cps = snapshot.documents.mapNotNull { it.toObject(Coupon::class.java) }
            if (cps.isNotEmpty()) {
                _coupons.value = cps
            }
        }

        // 5. Orders collection (Live Orders)
        db.collection("orders")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, err ->
                if (err != null || snapshot == null) return@addSnapshotListener
                val ords = snapshot.documents.mapNotNull { it.toObject(Order::class.java) }
                if (ords.isNotEmpty()) {
                    _orders.value = ords
                }
            }

        // 6. Users collection (Staff and Customers)
        db.collection("users").addSnapshotListener { snapshot, err ->
            if (err != null || snapshot == null) return@addSnapshotListener
            val usrs = snapshot.documents.mapNotNull { it.toObject(User::class.java) }
            if (usrs.isNotEmpty()) {
                _users.value = usrs
            }
        }

        // 7. Audit Logs collection (Strict Admin monitoring)
        db.collection("auditLogs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, err ->
                if (err != null || snapshot == null) return@addSnapshotListener
                val logs = snapshot.documents.mapNotNull { it.toObject(AuditLog::class.java) }
                if (logs.isNotEmpty()) {
                    _auditLogs.value = logs
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
            "customer_$cleanDigits@customer.bunzo.sy"
        }

        try {
            val currentAuth = auth
            val currentDb = firestore

            val uid = if (currentAuth != null) {
                val authResult = currentAuth.createUserWithEmailAndPassword(authEmail, password).await()
                authResult.user?.uid ?: "user_${System.currentTimeMillis()}"
            } else {
                "user_${System.currentTimeMillis()}"
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

            currentDb?.collection("users")?.document(uid)?.set(newUser)?.await()

            currentUser.value = newUser
            _users.update { list -> list.filterNot { it.uid == uid } + newUser }

            Result.success(newUser)
        } catch (e: Exception) {
            Log.e("BunzoRepository", "registerCustomer error", e)
            Result.failure(Exception(e.localizedMessage ?: "فشل إنشاء الحساب عبر خادم Firebase"))
        }
    }

    suspend fun loginCustomer(identifier: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        val clean = sanitizeInputString(identifier)
        val cleanDigits = clean.filter { it.isDigit() }
        val authEmail = if (clean.contains("@")) {
            clean.lowercase()
        } else {
            "customer_$cleanDigits@customer.bunzo.sy"
        }

        try {
            val currentAuth = auth
            val currentDb = firestore

            if (currentAuth != null) {
                val passwordCandidates = listOf(
                    password,
                    password.trim(),
                    sanitizeInputString(password),
                    sanitizeInputString(password).trim()
                ).distinct()

                var firebaseUser: com.google.firebase.auth.FirebaseUser? = null
                var lastErr: Exception? = null

                for (cand in passwordCandidates) {
                    try {
                        val authResult = currentAuth.signInWithEmailAndPassword(authEmail, cand).await()
                        firebaseUser = authResult.user
                        if (firebaseUser != null) break
                    } catch (e: Exception) {
                        lastErr = e
                    }
                }

                if (firebaseUser == null) {
                    throw lastErr ?: Exception("بيانات الدخول غير صحيحة")
                }

                val uid = firebaseUser.uid
                val doc = currentDb?.collection("users")?.document(uid)?.get()?.await()
                val user = doc?.toObject(User::class.java) ?: User(
                    uid = uid,
                    email = authEmail,
                    phone = clean,
                    role = "customer",
                    active = true
                )

                if (!user.active) {
                    currentAuth.signOut()
                    return@withContext Result.failure(Exception("هذا الحساب معطّل، يرجى مراجعة إدارة المطعم"))
                }

                currentUser.value = user
                Result.success(user)
            } else {
                // Standby local fallback if Firebase config is pending
                val existing = _users.value.find { it.phone == clean || it.email.equals(clean, ignoreCase = true) }
                if (existing != null) {
                    currentUser.value = existing
                    Result.success(existing)
                } else {
                    Result.failure(Exception("يرجى تزويد ملف google-services.json لتفعيل Firebase Auth الحقيقي"))
                }
            }
        } catch (e: Exception) {
            Log.e("BunzoRepository", "loginCustomer error", e)
            Result.failure(Exception("بيانات الدخول غير صحيحة أو الحساب غير موجود"))
        }
    }

    fun logoutCustomer() {
        auth?.signOut()
        currentUser.value = null
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
                if (effectiveRole.equals("admin", ignoreCase = true)) {
                    currentAdminUser.value = resolvedStaff
                }

                logAudit(
                    action = "STAFF_LOGIN",
                    details = "تسجيل دخول: ${resolvedStaff.fullName} (${resolvedStaff.userRole.titleAr})",
                    actor = resolvedStaff
                )

                Result.success(resolvedStaff)
            } else {
                Result.failure(Exception("تتطلب بوابة الموظفين تفعيل Firebase Auth عبر google-services.json الحقيقي"))
            }
        } catch (e: Exception) {
            Log.e("BunzoRepository", "loginStaff error", e)
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
        val existing = _users.value.find { it.uid == uid }
            ?: return@withContext Result.failure(Exception("العميل غير موجود"))

        val cleanPhone = phone.trim()
        if (cleanPhone.isBlank()) {
            return@withContext Result.failure(Exception("رقم الهاتف مطلوب"))
        }

        val updated = existing.copy(
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            phone = cleanPhone,
            region = region.trim(),
            address = address.trim(),
            updatedAt = System.currentTimeMillis()
        )

        try {
            firestore?.collection("users")?.document(uid)?.set(updated)?.await()
            _users.update { list -> list.map { if (it.uid == uid) updated else it } }
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
            ?: return@withContext Result.failure(Exception("العميل غير موجود"))

        if (existing.role == "admin") {
            return@withContext Result.failure(Exception("لا يمكن حذف حساب المدير"))
        }

        try {
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
                firestore?.collection("orders")?.document(newOrder.id)?.set(newOrder)?.await()
            } catch (e: Exception) {
                Log.e("BunzoRepository", "Failed to write order to Firestore", e)
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

                firestore?.collection("orders")?.document(orderId)?.update(updates)?.await()
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
