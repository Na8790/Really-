package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TajrubahAppContent(viewModel: TajrubahViewModel) {
    val isArabic by viewModel.isArabic.collectAsState()
    val isHostMode by viewModel.isHostMode.collectAsState()

    val user by viewModel.userFlow.collectAsState()
    val experiences by viewModel.experiencesFlow.collectAsState()
    val bookings by viewModel.bookingsFlow.collectAsState()
    val tourGuides by viewModel.tourGuidesFlow.collectAsState()
    val carRentals by viewModel.carRentalsFlow.collectAsState()
    val itineraries by viewModel.itinerariesFlow.collectAsState()
    val aiPlanningState by viewModel.aiPlanningState.collectAsState()

    var currentTab by remember { mutableStateOf(0) } // 0: Explore, 1: AI Planner, 2: Book Guides/Cars, 3: Bookings, 4: Profile/Wallet

    // Dialog state variables
    var showBookingDialog by remember { mutableStateOf<ExperienceEntity?>(null) }
    var bookingQty by remember { mutableStateOf(1) }
    var bookingDate by remember { mutableStateOf("") }

    var showGuideBookingDialog by remember { mutableStateOf<TourGuideEntity?>(null) }
    var guideBookingDays by remember { mutableStateOf(1) }
    var guideBookingDate by remember { mutableStateOf("") }

    var showCarBookingDialog by remember { mutableStateOf<CarRentalEntity?>(null) }
    var carBookingDays by remember { mutableStateOf(1) }
    var carBookingDate by remember { mutableStateOf("") }

    var showAddExperienceDialog by remember { mutableStateOf(false) }
    var showItineraryDialog by remember { mutableStateOf<ItineraryEntity?>(null) }
    var showAcademicDialog by remember { mutableStateOf(false) }
    var showAddFundsDialog by remember { mutableStateOf(false) }

    // RTL handling
    val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

    // Format helpers
    val currencyFormatter = remember { NumberFormat.getNumberInstance(Locale.US) }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (isArabic) "تِجربة" else "Tajrubah",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 24.sp
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (isHostMode) {
                                        if (isArabic) "وضع المضيف" else "Host"
                                    } else {
                                        if (isArabic) "سفر ذكي" else "Traveler"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    },
                    actions = {
                        // Quick Toggle Language
                        IconButton(onClick = { viewModel.toggleLanguage() }) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = if (isArabic) "EN" else "عربي",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(6.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Toggle Host/Traveler Mode
                        IconButton(onClick = { viewModel.toggleHostMode() }) {
                            Icon(
                                imageVector = if (isHostMode) Icons.Default.Home else Icons.Default.AccountCircle,
                                contentDescription = "Toggle Mode",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Info Button for Academic Details
                        IconButton(onClick = { showAcademicDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Academic Project Details",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    val tabs = listOf(
                        TabItem(
                            titleAr = "تصفح",
                            titleEn = "Explore",
                            icon = Icons.Default.Search,
                            index = 0
                        ),
                        TabItem(
                            titleAr = "المرشد الذكي",
                            titleEn = "AI Planner",
                            icon = Icons.Default.PlayArrow,
                            index = 1
                        ),
                        TabItem(
                            titleAr = "حجوزات الخدمة",
                            titleEn = "Guides/Cars",
                            icon = Icons.Default.Star,
                            index = 2
                        ),
                        TabItem(
                            titleAr = "حجوزاتي",
                            titleEn = "My Bookings",
                            icon = Icons.AutoMirrored.Filled.List,
                            index = 3
                        ),
                        TabItem(
                            titleAr = "المحفظة",
                            titleEn = "Wallet",
                            icon = Icons.Default.ShoppingCart,
                            index = 4
                        )
                    )

                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentTab == tab.index,
                            onClick = { currentTab = tab.index },
                            icon = { Icon(tab.icon, contentDescription = tab.titleEn) },
                            label = {
                                Text(
                                    text = if (isArabic) tab.titleAr else tab.titleEn,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.secondary
                            )
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "TabContent"
                ) { targetTab ->
                    when (targetTab) {
                        0 -> ExploreTab(
                            viewModel = viewModel,
                            experiences = experiences,
                            isArabic = isArabic,
                            isHostMode = isHostMode,
                            onBookClick = { showBookingDialog = it },
                            onAddExperienceClick = { showAddExperienceDialog = true }
                        )
                        1 -> AiPlannerTab(
                            viewModel = viewModel,
                            itineraries = itineraries,
                            isArabic = isArabic,
                            aiPlanningState = aiPlanningState,
                            onViewItinerary = { showItineraryDialog = it }
                        )
                        2 -> TransportAndGuidesTab(
                            viewModel = viewModel,
                            guides = tourGuides,
                            cars = carRentals,
                            isArabic = isArabic,
                            onBookGuide = { showGuideBookingDialog = it },
                            onBookCar = { showCarBookingDialog = it }
                        )
                        3 -> BookingsTab(
                            viewModel = viewModel,
                            bookings = bookings,
                            isArabic = isArabic
                        )
                        4 -> ProfileTab(
                            viewModel = viewModel,
                            user = user,
                            isArabic = isArabic,
                            onAddFundsClick = { showAddFundsDialog = true }
                        )
                    }
                }
            }
        }

        // --- Dialogs & Bottom Sheets ---

        // 1. Experience Booking Dialog
        showBookingDialog?.let { exp ->
            Dialog(onDismissRequest = { showBookingDialog = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = if (isArabic) "حجز تجربة محلية" else "Book Local Experience",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (isArabic) exp.titleAr else exp.titleEn,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )

                        // Date Picker Input (Simulated Text Input)
                        OutlinedTextField(
                            value = bookingDate,
                            onValueChange = { bookingDate = it },
                            label = { Text(if (isArabic) "التاريخ المقترح (مثال: 2026/08/15)" else "Suggested Date (e.g., 2026/08/15)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) }
                        )

                        // Quantity Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isArabic) "عدد المشتركين:" else "Participants:",
                                fontWeight = FontWeight.Medium
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = { if (bookingQty > 1) bookingQty-- },
                                    enabled = bookingQty > 1
                                ) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease")
                                }
                                Text(
                                    text = "$bookingQty",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = { if (bookingQty < exp.maxParticipants) bookingQty++ },
                                    enabled = bookingQty < exp.maxParticipants
                                ) {
                                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increase")
                                }
                            }
                        }

                        // Price Calculation
                        val totalYer = exp.priceYer * bookingQty
                        val totalUsd = exp.priceUsd * bookingQty
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = if (isArabic) "الإجمالي بالريال:" else "Total YER:", fontSize = 12.sp)
                                Text(
                                    text = "${currencyFormatter.format(totalYer)} YER",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = if (isArabic) "الإجمالي بالدولار:" else "Total USD:", fontSize = 12.sp)
                                Text(
                                    text = "$${currencyFormatter.format(totalUsd)} USD",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }

                        // Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showBookingDialog = null },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (isArabic) "إلغاء" else "Cancel")
                            }
                            Button(
                                onClick = {
                                    if (bookingDate.isEmpty()) {
                                        viewModel.showToast(
                                            if (isArabic) "يرجى تحديد التاريخ أولاً!" else "Please specify booking date!"
                                        )
                                    } else {
                                        viewModel.bookExperience(exp, bookingQty, bookingDate)
                                        showBookingDialog = null
                                        bookingDate = ""
                                        bookingQty = 1
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (isArabic) "تأكيد الدفع" else "Confirm Pay")
                            }
                        }
                    }
                }
            }
        }

        // 2. Guide Booking Dialog
        showGuideBookingDialog?.let { guide ->
            Dialog(onDismissRequest = { showGuideBookingDialog = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = if (isArabic) "حجز مرشد سياحي محلي" else "Book Local Tour Guide",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (isArabic) guide.nameAr else guide.nameEn,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )

                        OutlinedTextField(
                            value = guideBookingDate,
                            onValueChange = { guideBookingDate = it },
                            label = { Text(if (isArabic) "تاريخ بدء الجولة (مثال: 2026/08/20)" else "Tour Start Date (e.g., 2026/08/20)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = if (isArabic) "عدد الأيام:" else "Duration (Days):")
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (guideBookingDays > 1) guideBookingDays-- }) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                                }
                                Text("$guideBookingDays", fontWeight = FontWeight.Bold)
                                IconButton(onClick = { guideBookingDays++ }) {
                                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
                                }
                            }
                        }

                        val totalYer = guide.pricePerDayYer * guideBookingDays
                        val totalUsd = guide.pricePerDayUsd * guideBookingDays

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = if (isArabic) "الإجمالي بالريال:" else "Total YER:")
                                Text("${currencyFormatter.format(totalYer)} YER", fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = if (isArabic) "الإجمالي بالدولار:" else "Total USD:")
                                Text("$${currencyFormatter.format(totalUsd)} USD", fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showGuideBookingDialog = null },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (isArabic) "إلغاء" else "Cancel")
                            }
                            Button(
                                onClick = {
                                    if (guideBookingDate.isEmpty()) {
                                        viewModel.showToast(if (isArabic) "يرجى تحديد التاريخ!" else "Please specify date!")
                                    } else {
                                        viewModel.bookTourGuide(guide, guideBookingDays, guideBookingDate)
                                        showGuideBookingDialog = null
                                        guideBookingDate = ""
                                        guideBookingDays = 1
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (isArabic) "تأكيد الدفع" else "Confirm Pay")
                            }
                        }
                    }
                }
            }
        }

        // 3. Car Rental Booking Dialog
        showCarBookingDialog?.let { car ->
            Dialog(onDismissRequest = { showCarBookingDialog = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = if (isArabic) "تأجير سيارات دفع رباعي" else "Rent Adventure Car",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = car.model,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )

                        OutlinedTextField(
                            value = carBookingDate,
                            onValueChange = { carBookingDate = it },
                            label = { Text(if (isArabic) "تاريخ بدء الإيجار" else "Rental Start Date") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = if (isArabic) "عدد الأيام:" else "Duration (Days):")
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (carBookingDays > 1) carBookingDays-- }) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                                }
                                Text("$carBookingDays", fontWeight = FontWeight.Bold)
                                IconButton(onClick = { carBookingDays++ }) {
                                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
                                }
                            }
                        }

                        val totalYer = car.pricePerDayYer * carBookingDays
                        val totalUsd = car.pricePerDayUsd * carBookingDays

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = if (isArabic) "الإجمالي بالريال:" else "Total YER:")
                                Text("${currencyFormatter.format(totalYer)} YER", fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = if (isArabic) "الإجمالي بالدولار:" else "Total USD:")
                                Text("$${currencyFormatter.format(totalUsd)} USD", fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showCarBookingDialog = null },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (isArabic) "إلغاء" else "Cancel")
                            }
                            Button(
                                onClick = {
                                    if (carBookingDate.isEmpty()) {
                                        viewModel.showToast(if (isArabic) "يرجى تحديد التاريخ!" else "Please specify date!")
                                    } else {
                                        viewModel.bookCarRental(car, carBookingDays, carBookingDate)
                                        showCarBookingDialog = null
                                        carBookingDate = ""
                                        carBookingDays = 1
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (isArabic) "تأكيد الدفع" else "Confirm Pay")
                            }
                        }
                    }
                }
            }
        }

        // 4. Host: Add Experience Dialog
        if (showAddExperienceDialog) {
            var titleAr by remember { mutableStateOf("") }
            var titleEn by remember { mutableStateOf("") }
            var descAr by remember { mutableStateOf("") }
            var descEn by remember { mutableStateOf("") }
            var catAr by remember { mutableStateOf("زراعة وثقافة") }
            var catEn by remember { mutableStateOf("Agriculture & Culture") }
            var cityAr by remember { mutableStateOf("") }
            var cityEn by remember { mutableStateOf("") }
            var priceYerStr by remember { mutableStateOf("") }
            var priceUsdStr by remember { mutableStateOf("") }
            var durationStr by remember { mutableStateOf("") }
            var maxPartsStr by remember { mutableStateOf("") }
            var hostAr by remember { mutableStateOf(user?.name ?: "مضيف محلي") }
            var hostEn by remember { mutableStateOf("Local Host") }

            Dialog(onDismissRequest = { showAddExperienceDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.9f)
                        .padding(8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (isArabic) "إضافة تجربة محلية جديدة" else "Host a New Experience",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Arabic Fields
                        Text(text = if (isArabic) "البيانات باللغة العربية:" else "Details in Arabic:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary)
                        OutlinedTextField(
                            value = titleAr,
                            onValueChange = { titleAr = it },
                            label = { Text("عنوان التجربة (مثال: حصاد العسل)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = descAr,
                            onValueChange = { descAr = it },
                            label = { Text("وصف التجربة والأنشطة") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = cityAr,
                            onValueChange = { cityAr = it },
                            label = { Text("المدينة أو المنطقة (مثال: دوعن، حضرموت)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // English Fields
                        Text(text = if (isArabic) "البيانات باللغة الإنجليزية:" else "Details in English:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary)
                        OutlinedTextField(
                            value = titleEn,
                            onValueChange = { titleEn = it },
                            label = { Text("Experience Title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = descEn,
                            onValueChange = { descEn = it },
                            label = { Text("Description & Activities") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = cityEn,
                            onValueChange = { cityEn = it },
                            label = { Text("City/Region (e.g. Do'an, Hadramout)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Pricing and Logistics
                        Text(text = if (isArabic) "الخدمات والأسعار والمقاعد:" else "Pricing & Logistics:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = priceYerStr,
                                onValueChange = { priceYerStr = it },
                                label = { Text("السعر (YER)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = priceUsdStr,
                                onValueChange = { priceUsdStr = it },
                                label = { Text("السعر (USD)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = durationStr,
                                onValueChange = { durationStr = it },
                                label = { Text("المدة بالدقائق/الساعات") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = maxPartsStr,
                                onValueChange = { maxPartsStr = it },
                                label = { Text("أقصى عدد مقاعد") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Host Names
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = hostAr,
                                onValueChange = { hostAr = it },
                                label = { Text("اسم المضيف بالعربي") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = hostEn,
                                onValueChange = { hostEn = it },
                                label = { Text("Host Name (EN)") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showAddExperienceDialog = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (isArabic) "إلغاء" else "Cancel")
                            }
                            Button(
                                onClick = {
                                    val yer = priceYerStr.toDoubleOrNull() ?: 0.0
                                    val usd = priceUsdStr.toDoubleOrNull() ?: 0.0
                                    val dur = durationStr.toIntOrNull() ?: 2
                                    val maxP = maxPartsStr.toIntOrNull() ?: 10

                                    if (titleAr.isEmpty() || titleEn.isEmpty() || yer <= 0 || usd <= 0) {
                                        viewModel.showToast(
                                            if (isArabic) "يرجى تعبئة الحقول الأساسية بشكل صحيح!" else "Please fill out major fields correctly!"
                                        )
                                    } else {
                                        viewModel.addExperience(
                                            titleAr, titleEn, descAr, descEn,
                                            catAr, catEn, cityAr, cityEn,
                                            yer, usd, dur, maxP, hostAr, hostEn
                                        )
                                        showAddExperienceDialog = false
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (isArabic) "نشر التجربة" else "Publish")
                            }
                        }
                    }
                }
            }
        }

        // 5. Itinerary Details Dialog
        showItineraryDialog?.let { itinerary ->
            Dialog(onDismissRequest = { showItineraryDialog = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f)
                        .padding(8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxSize()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isArabic) itinerary.titleAr else itinerary.titleEn,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { showItineraryDialog = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                                Text(
                                    text = if (isArabic) {
                                        "تم التخطيط التلقائي بواسطة ذكاء اصطناعي محلي"
                                    } else {
                                        "Planned automatically via secure AI integration"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Scrollable detailed text representation
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = itinerary.responseText,
                                    fontSize = 14.sp,
                                    lineHeight = 22.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showItineraryDialog = null },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isArabic) "موافق" else "Dismiss")
                        }
                    }
                }
            }
        }

        // 6. Academic Graduation Project Info Dialog
        if (showAcademicDialog) {
            Dialog(onDismissRequest = { showAcademicDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Drawing a mini Yemeni Qamariyah Window
                        Box(modifier = Modifier.size(100.dp)) {
                            QamariyahWindowDrawing(modifier = Modifier.fillMaxSize())
                        }

                        Text(
                            text = "مشروع تخرج جامعة اليمن",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "تِجربة (Tajrubah)\nمنصة التجارب المحلية والسفر الذكي في اليمن",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )

                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AcademicDetailRow(
                                label = if (isArabic) "إعداد المهندسة:" else "Prepared by:",
                                value = "Raghad (رغد)"
                            )
                            AcademicDetailRow(
                                label = if (isArabic) "سنة التخرج:" else "Graduation Year:",
                                value = "2026 م"
                            )
                            AcademicDetailRow(
                                label = if (isArabic) "تحت إشراف:" else "Supervised by:",
                                value = "د. أنور الكميم"
                            )
                            AcademicDetailRow(
                                label = if (isArabic) "الأهداف البرمجية:" else "Tech Architecture:",
                                value = "Clean Architecture, SOLID Principles, Room Database Local Persistence, Gemini AI REST Integration, Material 3 Design System."
                            )
                        }

                        Button(
                            onClick = { showAcademicDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isArabic) "إغلاق" else "Close")
                        }
                    }
                }
            }
        }

        // 7. Wallet: Add Funds Simulator Dialog
        if (showAddFundsDialog) {
            var inputYer by remember { mutableStateOf("") }
            var inputUsd by remember { mutableStateOf("") }

            Dialog(onDismissRequest = { showAddFundsDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = if (isArabic) "شحن رصيد المحفظة (تجريبي)" else "Add Funds (Simulator)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (isArabic) {
                                "هذا جزء من محاكاة الدفع الرقمي الآمنة للمشروع للتحقق من تكامل حجز الخدمات وتحديث الأرصدة تلقائياً."
                            } else {
                                "This payment simulator validates booking triggers and balance accounting under real transaction limits."
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            lineHeight = 16.sp
                        )

                        OutlinedTextField(
                            value = inputYer,
                            onValueChange = { inputYer = it },
                            label = { Text(if (isArabic) "المبلغ بالريال اليمني (YER)" else "Amount in YER") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = inputUsd,
                            onValueChange = { inputUsd = it },
                            label = { Text(if (isArabic) "المبلغ بالدولار الأمريكي (USD)" else "Amount in USD") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showAddFundsDialog = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (isArabic) "إلغاء" else "Cancel")
                            }
                            Button(
                                onClick = {
                                    val yVal = inputYer.toDoubleOrNull() ?: 0.0
                                    val uVal = inputUsd.toDoubleOrNull() ?: 0.0
                                    if (yVal <= 0 && uVal <= 0) {
                                        viewModel.showToast(
                                            if (isArabic) "أدخل مبلغاً صالحاً!" else "Enter a valid amount!"
                                        )
                                    } else {
                                        viewModel.addFunds(yVal, uVal)
                                        showAddFundsDialog = false
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (isArabic) "شحن" else "Load")
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- EXPLORE TAB VIEW ---
@Composable
fun ExploreTab(
    viewModel: TajrubahViewModel,
    experiences: List<ExperienceEntity>,
    isArabic: Boolean,
    isHostMode: Boolean,
    onBookClick: (ExperienceEntity) -> Unit,
    onAddExperienceClick: () -> Unit
) {
    var selectedCategoryAr by remember { mutableStateOf("الكل") }
    var selectedCategoryEn by remember { mutableStateOf("All") }

    val categories = listOf(
        Pair("الكل", "All"),
        Pair("زراعة وثقافة", "Agriculture & Culture"),
        Pair("مغامرة وطبيعة", "Adventure & Nature"),
        Pair("حرف وفنون", "Crafts & Food"),
        Pair("مأكولات شعبية", "Culinary Arts"),
        Pair("حرف يدوية", "Handicrafts")
    )

    val filteredExperiences = experiences.filter { exp ->
        if (isArabic) {
            selectedCategoryAr == "الكل" || exp.categoryAr == selectedCategoryAr
        } else {
            selectedCategoryEn == "All" || exp.categoryEn == selectedCategoryEn
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header with custom drawn Yemeni Qamariyah
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    // Full-bleed panoramic Haraz sunset background
                    Image(
                        painter = painterResource(id = com.example.R.drawable.img_explore_hero),
                        contentDescription = "Haraz Mountains",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // High-contrast vertical scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.2f),
                                        Color.Black.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isArabic) "اكتشف اليمن الأصيل" else "Discover Authentic Yemen",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isArabic) {
                                    "تجارب محلية يصنعها السكان، لتخوض مغامرات حقيقية لا تنساها."
                                } else {
                                    "Genuine host-led local experiences, crafts, foods, and deep nature."
                                },
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                lineHeight = 16.sp
                            )
                            if (isHostMode) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = onAddExperienceClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isArabic) "استضف تجربة جديدة" else "Publish Experience",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Traditional Qamariyah visual element in an elegant Glassmorphic Ring
                        Surface(
                            color = Color.White.copy(alpha = 0.18f),
                            shape = CircleShape,
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .size(84.dp)
                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                            shadowElevation = 4.dp
                        ) {
                            Box(
                                modifier = Modifier.padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                QamariyahWindowDrawing(modifier = Modifier.fillMaxSize())
                            }
                        }
                    }
                }
            }
        }

        // Category Filter Row
        item {
            Text(
                text = if (isArabic) "التصنيفات المحلية" else "Local Categories",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = if (isArabic) {
                        selectedCategoryAr == cat.first
                    } else {
                        selectedCategoryEn == cat.second
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCategoryAr = cat.first
                            selectedCategoryEn = cat.second
                        },
                        label = {
                            Text(
                                text = if (isArabic) cat.first else cat.second,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Experiences Grid/List
        if (filteredExperiences.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isArabic) "لا توجد تجارب في هذا التصنيف حالياً" else "No experiences in this category",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        } else {
            items(filteredExperiences) { exp ->
                ExperienceCard(
                    exp = exp,
                    isArabic = isArabic,
                    isHostMode = isHostMode,
                    onBookClick = { onBookClick(exp) },
                    onDeleteClick = { viewModel.deleteExperience(exp.id) }
                )
            }
        }

        // Bottom Spacer for scrolling ease
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ExperienceCard(
    exp: ExperienceEntity,
    isArabic: Boolean,
    isHostMode: Boolean,
    onBookClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Hero Colored Background Band showing city & Category
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.9f)
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
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
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isArabic) exp.cityAr else exp.cityEn,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Surface(
                        color = Color.White.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isArabic) exp.categoryAr else exp.categoryEn,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Body Content
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = if (isArabic) exp.titleAr else exp.titleEn,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    // Show Delete if Host mode and custom created
                    if (isHostMode && exp.isCustomCreated) {
                        IconButton(onClick = onDeleteClick, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isArabic) exp.descriptionAr else exp.descriptionEn,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = 18.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        // Duration & Host
                        Text(
                            text = if (isArabic) "المضيف: ${exp.hostNameAr}" else "Host: ${exp.hostNameEn}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isArabic) "المدة: ${exp.durationHours} ساعات" else "Duration: ${exp.durationHours} hrs",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }

                    // Price display + Book Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (isArabic) "${NumberFormat.getNumberInstance(Locale.US).format(exp.priceYer)} ريال" else "${NumberFormat.getNumberInstance(Locale.US).format(exp.priceYer)} YER",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "$${exp.priceUsd.toInt()} USD",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Disable booking in host mode
                        if (!isHostMode) {
                            Button(
                                onClick = onBookClick,
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(if (isArabic) "احجز" else "Book", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- AI PLANNER TAB VIEW ---
@Composable
fun AiPlannerTab(
    viewModel: TajrubahViewModel,
    itineraries: List<ItineraryEntity>,
    isArabic: Boolean,
    aiPlanningState: AiPlanningState,
    onViewItinerary: (ItineraryEntity) -> Unit
) {
    var selectedCity by remember { mutableStateOf(if (isArabic) "صنعاء القديمة" else "Old Sana'a") }
    var durationDays by remember { mutableStateOf(3) }
    var budgetLevel by remember { mutableStateOf("Moderate") } // Economy, Moderate, Premium
    var interestsInput by remember { mutableStateOf("") }

    val cities = if (isArabic) {
        listOf("صنعاء القديمة", "جزيرة سقطرى", "وادي دوعن، حضرموت", "جبال حراز", "مدينة شبام التاريخية")
    } else {
        listOf("Old Sana'a", "Socotra Island", "Do'an Valley, Hadramout", "Haraz Mountains", "Historic Shibam City")
    }

    var showCityDropdown by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    // Full-bleed modern digital travel planner illustration
                    Image(
                        painter = painterResource(id = com.example.R.drawable.img_ai_planner),
                        contentDescription = "Smart AI Planner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // High-contrast gradient scrim to ensure extreme readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.2f),
                                        Color.Black.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = if (isArabic) "مخطط الرحلات الذكي بالذكاء الاصطناعي" else "Smart AI Travel Planner",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isArabic) {
                                "أدخل اهتماماتك وميزانيتك، وسيقوم الذكاء الاصطناعي (Gemini) بتصميم برنامج سياحي متكامل ومخصص لك في أرجاء اليمن."
                            } else {
                                "Specify your interests and budget, and Gemini AI will draft a complete authentic daily itinerary in Yemen."
                            },
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Planning Form Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // City Dropdown Selector
                    Box {
                        OutlinedTextField(
                            value = selectedCity,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (isArabic) "الوجهة السياحية" else "Destination City") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCityDropdown = true },
                            trailingIcon = {
                                IconButton(onClick = { showCityDropdown = true }) {
                                    Icon(Icons.Default.Place, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = showCityDropdown,
                            onDismissRequest = { showCityDropdown = false }
                        ) {
                            cities.forEach { city ->
                                DropdownMenuItem(
                                    text = { Text(city) },
                                    onClick = {
                                        selectedCity = city
                                        showCityDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Duration Slider
                    Column {
                        Text(
                            text = if (isArabic) "مدة الرحلة: $durationDays أيام" else "Trip Duration: $durationDays Days",
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                        Slider(
                            value = durationDays.toFloat(),
                            onValueChange = { durationDays = it.toInt() },
                            valueRange = 1f..7f,
                            steps = 5,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    // Budget Row Chip Choice
                    Column {
                        Text(
                            text = if (isArabic) "فئة الميزانية" else "Budget Level",
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Economy", "Moderate", "Premium").forEach { b ->
                                val isSelected = budgetLevel == b
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { budgetLevel = b },
                                    label = {
                                        Text(
                                            text = if (isArabic) {
                                                when (b) {
                                                    "Economy" -> "اقتصادية"
                                                    "Moderate" -> "متوسطة"
                                                    else -> "فاخرة"
                                                }
                                            } else b
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    // Interests Box
                    OutlinedTextField(
                        value = interestsInput,
                        onValueChange = { interestsInput = it },
                        label = { Text(if (isArabic) "الاهتمامات (مثال: تصوير طبيعة، أكل تقليدي، متاحف)" else "Interests (e.g. photography, food, hiking)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Generation Action Button
                    Button(
                        onClick = {
                            viewModel.generateAiItinerary(
                                selectedCity,
                                durationDays,
                                budgetLevel,
                                interestsInput.ifEmpty { if (isArabic) "ثقافة وتاريخ" else "Culture & History" }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "خطط رحلتي بالذكاء الاصطناعي" else "Plan My Trip with AI",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Live AI Loading/Status Banner
        item {
            AnimatedVisibility(visible = aiPlanningState != AiPlanningState.Idle) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (aiPlanningState) {
                            is AiPlanningState.Loading -> {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = if (isArabic) "جاري صياغة جدول رحلتك الفريد بالذكاء الاصطناعي..." else "Weaving your unique AI custom itinerary...",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            is AiPlanningState.Success -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isArabic) "تم توليد الرحلة بنجاح! ✨" else "Itinerary Generated Successfully! ✨",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        fontSize = 13.sp
                                    )
                                    Button(
                                        onClick = { viewModel.resetAiPlanningState() },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(if (isArabic) "إخفاء" else "Dismiss", fontSize = 11.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = aiPlanningState.itineraryText,
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp,
                                        maxLines = 10,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            is AiPlanningState.Error -> {
                                Text(
                                    text = "Error: ${aiPlanningState.message}",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp
                                )
                            }
                            else -> {}
                        }
                    }
                }
            }
        }

        // Historic Saved Routes Section
        item {
            Text(
                text = if (isArabic) "خططي السياحية السابقة" else "My Saved Smart Routes",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (itineraries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isArabic) "لا توجد رحلات سابقة محفوظة" else "No saved AI itineraries yet",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            items(itineraries) { iti ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onViewItinerary(iti) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (isArabic) iti.titleAr else iti.titleEn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = if (isArabic) "المدة: ${iti.durationDays} أيام" else "Duration: ${iti.durationDays} days",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = if (isArabic) "الاهتمامات: ${iti.interests}" else "Interests: ${iti.interests}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "View Details",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// --- TRANSPORT & GUIDES TAB VIEW ---
@Composable
fun TransportAndGuidesTab(
    viewModel: TajrubahViewModel,
    guides: List<TourGuideEntity>,
    cars: List<CarRentalEntity>,
    isArabic: Boolean,
    onBookGuide: (TourGuideEntity) -> Unit,
    onBookCar: (CarRentalEntity) -> Unit
) {
    var subTabState by remember { mutableStateOf(0) } // 0: Guides, 1: Car Rentals

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Tab Headers
        TabRow(
            selectedTabIndex = subTabState,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Tab(
                selected = subTabState == 0,
                onClick = { subTabState = 0 },
                text = { Text(if (isArabic) "مرشدين محليين" else "Local Guides", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = subTabState == 1,
                onClick = { subTabState = 1 },
                text = { Text(if (isArabic) "تأجير سيارات" else "Car Rentals", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (subTabState == 0) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(guides) { guide ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isArabic) guide.nameAr else guide.nameEn,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    // Rating
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Rating",
                                        tint = Color(0xFFFFC107),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${guide.rating}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(start = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isArabic) "المدينة: ${guide.cityAr}" else "City: ${guide.cityEn}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = if (isArabic) "اللغات: ${guide.languagesAr}" else "Languages: ${guide.languagesEn}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (isArabic) "${NumberFormat.getNumberInstance(Locale.US).format(guide.pricePerDayYer)} ريال/يوم" else "${NumberFormat.getNumberInstance(Locale.US).format(guide.pricePerDayYer)} YER/day",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { onBookGuide(guide) },
                                    enabled = !guide.isBooked,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary,
                                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (guide.isBooked) {
                                            if (isArabic) "محجوز" else "Booked"
                                        } else {
                                            if (isArabic) "حجز المرشد" else "Hire"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(cars) { car ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = car.model, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isArabic) "النوع: ${car.typeAr}" else "Type: ${car.typeEn}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = if (isArabic) "السعة: ${car.capacity} مقاعد" else "Capacity: ${car.capacity} seats",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (isArabic) "${NumberFormat.getNumberInstance(Locale.US).format(car.pricePerDayYer)} ريال/يوم" else "${NumberFormat.getNumberInstance(Locale.US).format(car.pricePerDayYer)} YER/day",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { onBookCar(car) },
                                    enabled = !car.isRented,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary,
                                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (car.isRented) {
                                            if (isArabic) "مؤجرة" else "Rented"
                                        } else {
                                            if (isArabic) "احجز" else "Rent"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

// --- BOOKINGS TAB VIEW (LEDGER & TICKET SYSTEM) ---
@Composable
fun BookingsTab(
    viewModel: TajrubahViewModel,
    bookings: List<BookingEntity>,
    isArabic: Boolean
) {
    var expandedBookingId by remember { mutableStateOf<Long?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (isArabic) "حجوزاتي وسجل تذاكري" else "My Bookings & Tickets",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isArabic) {
                            "تصفح تذاكرك وتأكيدات حجوزاتك المحلية النشطة. اضغط على أي حجز لإظهار رمز الـ QR وتفاصيل الدخول."
                        } else {
                            "Verify details of active bookings. Tap any ledger card to reveal check-in QR codes."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        if (bookings.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isArabic) "سجل حجوزاتك فارغ حالياً" else "Your booking ledger is empty",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isArabic) "تصفح التجارب والخدمات وابدأ رحلتك!" else "Explore experiences and start your journey!",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        } else {
            items(bookings) { booking ->
                val isExpanded = expandedBookingId == booking.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedBookingId = if (isExpanded) null else booking.id },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Icon Type Selector
                                Surface(
                                    color = when (booking.type) {
                                        "EXPERIENCE" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        "GUIDE" -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                                        else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = when (booking.type) {
                                            "EXPERIENCE" -> Icons.Default.Search
                                            "GUIDE" -> Icons.Default.Star
                                            else -> Icons.Default.Home
                                        },
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .size(20.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = if (isArabic) booking.titleAr else booking.titleEn,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isArabic) "التاريخ: ${booking.bookingDate}" else "Date: ${booking.bookingDate}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }

                            // Confirm Status Badge
                            Surface(
                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (isArabic) "مؤكد" else "Confirmed",
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isArabic) "الكمية/الأيام: ${booking.quantity}" else "Qty/Days: ${booking.quantity}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (isArabic) "${NumberFormat.getNumberInstance(Locale.US).format(booking.totalPriceYer)} YER" else "${NumberFormat.getNumberInstance(Locale.US).format(booking.totalPriceYer)} YER",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "$${booking.totalPriceUsd.toInt()} USD",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Expanded Ticket QR view
                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = if (isArabic) "رمز التحقق السريع للدخول (تذكرة رقمية)" else "Fast Entry Verification Token (Digital Ticket)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.tertiary
                                )

                                // Simulated QR code representation drawn beautifully using Canvas
                                Box(
                                    modifier = Modifier
                                        .size(130.dp)
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(8.dp)
                                ) {
                                    QrCodeDrawing(idSeed = booking.id, modifier = Modifier.fillMaxSize())
                                }

                                Text(
                                    text = "SECURE-TICKET-YEM-${10000 + booking.id}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// --- PROFILE & WALLET VIEW ---
@Composable
fun ProfileTab(
    viewModel: TajrubahViewModel,
    user: UserEntity?,
    isArabic: Boolean,
    onAddFundsClick: () -> Unit
) {
    val currencyFormatter = remember { NumberFormat.getNumberInstance(Locale.US) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Profile Avatar Placeholder
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (user != null && user.name.isNotEmpty()) user.name.take(1) else "ر",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column {
                        Text(
                            text = user?.name ?: "المهندسة رغد",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isArabic) "الحساب السياحي الموثق" else "Verified Capstone Account",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        // Wallet Balance Card with Gold/Amber Gradient
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    // Full-bleed traditional Yemeni architectural vector pattern background
                    Image(
                        painter = painterResource(id = com.example.R.drawable.img_profile_banner),
                        contentDescription = "Yemeni Patterns",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Gradient overlay with primary clay brown / emerald yemen
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFC68A4C).copy(alpha = 0.85f),
                                        Color(0xFF1E5E4E).copy(alpha = 0.9f)
                                    )
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isArabic) "رصيد محفظة تِجربة" else "Tajrubah Smart Wallet",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${currencyFormatter.format(user?.walletBalanceYer ?: 0.0)} YER",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$${currencyFormatter.format(user?.walletBalanceUsd ?: 0.0)} USD",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Add Funds Button
                        Button(
                            onClick = onAddFundsClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = if (isArabic) "شحن الرصيد" else "Load Funds",
                                color = Color(0xFF1E5E4E),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Gamification / Points Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isArabic) "نقاط المكافآت المحلية 🏅" else "Local Loyalty Points 🏅",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isArabic) {
                                "لديك ${user?.rewardPoints ?: 1250} نقطة. اكسب المزيد مع كل حجز تجربة."
                            } else {
                                "You have ${user?.rewardPoints ?: 1250} points. Earn rewards automatically on every local transaction."
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            lineHeight = 16.sp
                        )
                    }

                    Button(
                        onClick = {
                            val pts = user?.rewardPoints ?: 0
                            if (pts >= 1000) {
                                viewModel.addFunds(10000.0, 0.0)
                                viewModel.showToast(
                                    if (isArabic) "تم استبدال 1000 نقطة بـ 10,000 ريال يمني بنجاح!" else "Redeemed 1000 points for YER 10,000 successfully!"
                                )
                            } else {
                                viewModel.showToast(
                                    if (isArabic) "تحتاج إلى 1000 نقطة على الأقل للاستبدال!" else "You need at least 1000 points to redeem!"
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(if (isArabic) "استبدال" else "Redeem", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Referral & Gift Card Simulated Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isArabic) "الرحلات المفاجئة وبطاقات الهدايا" else "Surprise Trips & Gift Cards",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isArabic) {
                            "أهدِ صديقك تذكرة لتجربة قطاف البن الحرازي أو استخراج عسل السدر الأصيل."
                        } else {
                            "Send your loved ones an experience gift card (e.g. coffee harvesting or honey apiary tours)."
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                viewModel.showToast(
                                    if (isArabic) "تم نسخ كود إهداء الرحلة بنجاح!" else "Gift card code copied successfully!"
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(if (isArabic) "إهداء رحلة" else "Send Gift Card", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.showToast(
                                    if (isArabic) "تم توليد رحلتك المفاجئة!" else "Surprise trip calculated!"
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Text(if (isArabic) "رحلة مفاجئة" else "Surprise Me", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Graduation Capstone Details
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isArabic) "مشروع تخرج المهندسة رغد" else "Graduation Capstone of Eng. Raghad",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "University of Yemen - Faculty of Engineering & Computer Science\nAll rights reserved 2026 ©",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// --- HELPER COMPOSABLE CUSTOM VECTOR DRAWINGS ---

/**
 * Draws a beautiful, multi-colored Yemeni Qamariyah Window (القمريّة الصنعانيّة) using Compose Canvas.
 * This represents the highest form of custom UI/UX crafting in Android.
 */
@Composable
fun QamariyahWindowDrawing(modifier: Modifier = Modifier) {
    val outlineColor = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = width.coerceAtMost(height) / 2.2f

        // Draw semi-circular window frame
        drawArc(
            color = outlineColor,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            style = Stroke(width = 3.dp.toPx())
        )

        // Draw horizontal baseline
        drawLine(
            color = outlineColor,
            start = Offset(centerX - radius, centerY),
            end = Offset(centerX + radius, centerY),
            strokeWidth = 3.dp.toPx()
        )

        // Draw radial spokes and filled sectors representing colored glass panes
        val glassColors = listOf(
            Color(0xFFE63946), // Ruby red
            Color(0xFFF4A261), // Amber gold
            Color(0xFF2A9D8F), // Turquoise
            Color(0xFF457B9D), // Indigo
            Color(0xFF1D3557)  // Deep sapphire
        )

        val sweepStep = 180f / 5f
        for (i in 0 until 5) {
            val startAngle = 180f + (i * sweepStep)
            // Fill sector with stained glass color
            drawArc(
                color = glassColors[i % glassColors.size].copy(alpha = 0.7f),
                startAngle = startAngle + 2f,
                sweepAngle = sweepStep - 4f,
                useCenter = true
            )
            // Draw border spoke line
            val angleRad = Math.toRadians((startAngle + sweepStep).toDouble())
            val endX = centerX + radius * Math.cos(angleRad).toFloat()
            val endY = centerY + radius * Math.sin(angleRad).toFloat()
            drawLine(
                color = outlineColor,
                start = Offset(centerX, centerY),
                end = Offset(endX, endY),
                strokeWidth = 1.5.dp.toPx()
            )
        }

        // Inner decorative semi-circle outline
        drawArc(
            color = outlineColor,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            style = Stroke(width = 1.5.dp.toPx()),
            size = Size(radius, radius),
            topLeft = Offset(centerX - radius/2f, centerY - radius/2f)
        )
    }
}

/**
 * Draws a simulated, high-fidelity dynamic QR Code on Compose Canvas based on an ID seed.
 * Ensures the corner locator cubes are placed exactly to match standard QR layouts.
 */
@Composable
fun QrCodeDrawing(idSeed: Long, modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val rand = remember(idSeed) { Random(idSeed) }
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Fill background white
        drawRect(color = Color.White)

        // Draw three standard QR corner locator boxes
        val boxSize = w * 0.22f

        // Top-Left Box
        drawRect(color = primaryColor, topLeft = Offset(0f, 0f), size = Size(boxSize, boxSize))
        drawRect(color = Color.White, topLeft = Offset(boxSize * 0.2f, boxSize * 0.2f), size = Size(boxSize * 0.6f, boxSize * 0.6f))
        drawRect(color = primaryColor, topLeft = Offset(boxSize * 0.35f, boxSize * 0.35f), size = Size(boxSize * 0.3f, boxSize * 0.3f))

        // Top-Right Box
        drawRect(color = primaryColor, topLeft = Offset(w - boxSize, 0f), size = Size(boxSize, boxSize))
        drawRect(color = Color.White, topLeft = Offset(w - boxSize + (boxSize * 0.2f), boxSize * 0.2f), size = Size(boxSize * 0.6f, boxSize * 0.6f))
        drawRect(color = primaryColor, topLeft = Offset(w - boxSize + (boxSize * 0.35f), boxSize * 0.35f), size = Size(boxSize * 0.3f, boxSize * 0.3f))

        // Bottom-Left Box
        drawRect(color = primaryColor, topLeft = Offset(0f, h - boxSize), size = Size(boxSize, boxSize))
        drawRect(color = Color.White, topLeft = Offset(boxSize * 0.2f, h - boxSize + (boxSize * 0.2f)), size = Size(boxSize * 0.6f, boxSize * 0.6f))
        drawRect(color = primaryColor, topLeft = Offset(boxSize * 0.35f, h - boxSize + (boxSize * 0.35f)), size = Size(boxSize * 0.3f, boxSize * 0.3f))

        // Draw random QR noise grid (excluding the locator boxes)
        val gridCount = 12
        val cellW = w / gridCount
        val cellH = h / gridCount

        for (row in 0 until gridCount) {
            for (col in 0 until gridCount) {
                // Avoid drawing over the corner locator boxes
                val isCorner = (row < 4 && col < 4) || (row < 4 && col >= gridCount - 4) || (row >= gridCount - 4 && col < 4)
                if (!isCorner) {
                    if (rand.nextBoolean()) {
                        drawRect(
                            color = primaryColor,
                            topLeft = Offset(col * cellW, row * cellH),
                            size = Size(cellW, cellH)
                        )
                    }
                }
            }
        }
    }
}

// --- CORE UTILITY CLASS/HELPERS ---

data class TabItem(
    val titleAr: String,
    val titleEn: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val index: Int
)

@Composable
fun AcademicDetailRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            lineHeight = 16.sp
        )
    }
}

// Simulated simple scroll wrapper for form inside dialog
@Composable
fun rememberScrollState() = androidx.compose.foundation.rememberScrollState()
