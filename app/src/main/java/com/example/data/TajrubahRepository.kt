package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class TajrubahRepository(private val dao: TajrubahDao) {

    val userFlow: Flow<UserEntity?> = dao.getUserByIdFlow(1)
    val experiencesFlow: Flow<List<ExperienceEntity>> = dao.getAllExperiences()
    val bookingsFlow: Flow<List<BookingEntity>> = dao.getAllBookings()
    val tourGuidesFlow: Flow<List<TourGuideEntity>> = dao.getAllTourGuides()
    val carRentalsFlow: Flow<List<CarRentalEntity>> = dao.getAllCarRentals()
    val itinerariesFlow: Flow<List<ItineraryEntity>> = dao.getAllItineraries()

    suspend fun getDirectUser(): UserEntity? = dao.getUserById(1)

    suspend fun saveUser(user: UserEntity) = dao.insertUser(user)

    suspend fun bookExperience(experience: ExperienceEntity, quantity: Int, date: String): Boolean {
        val user = dao.getUserById(1) ?: return false
        val totalPriceYer = experience.priceYer * quantity
        val totalPriceUsd = experience.priceUsd * quantity

        if (user.walletBalanceYer < totalPriceYer) return false

        // Deduct balance and add reward points (1 point per 1000 YER spent)
        val updatedUser = user.copy(
            walletBalanceYer = user.walletBalanceYer - totalPriceYer,
            walletBalanceUsd = user.walletBalanceUsd - totalPriceUsd,
            rewardPoints = user.rewardPoints + (totalPriceYer / 1000).toInt()
        )
        dao.insertUser(updatedUser)

        val booking = BookingEntity(
            type = "EXPERIENCE",
            targetId = experience.id,
            titleAr = experience.titleAr,
            titleEn = experience.titleEn,
            bookingDate = date,
            quantity = quantity,
            totalPriceYer = totalPriceYer,
            totalPriceUsd = totalPriceUsd,
            status = "CONFIRMED"
        )
        dao.insertBooking(booking)
        return true
    }

    suspend fun bookTourGuide(guide: TourGuideEntity, days: Int, date: String): Boolean {
        val user = dao.getUserById(1) ?: return false
        val totalPriceYer = guide.pricePerDayYer * days
        val totalPriceUsd = guide.pricePerDayUsd * days

        if (user.walletBalanceYer < totalPriceYer) return false

        // Update user balance and points
        val updatedUser = user.copy(
            walletBalanceYer = user.walletBalanceYer - totalPriceYer,
            walletBalanceUsd = user.walletBalanceUsd - totalPriceUsd,
            rewardPoints = user.rewardPoints + (totalPriceYer / 1000).toInt()
        )
        dao.insertUser(updatedUser)

        // Mark guide as booked
        dao.updateTourGuide(guide.copy(isBooked = true))

        val booking = BookingEntity(
            type = "GUIDE",
            targetId = guide.id,
            titleAr = "المرشد: ${guide.nameAr}",
            titleEn = "Guide: ${guide.nameEn}",
            bookingDate = date,
            quantity = days,
            totalPriceYer = totalPriceYer,
            totalPriceUsd = totalPriceUsd,
            status = "CONFIRMED"
        )
        dao.insertBooking(booking)
        return true
    }

    suspend fun bookCarRental(car: CarRentalEntity, days: Int, date: String): Boolean {
        val user = dao.getUserById(1) ?: return false
        val totalPriceYer = car.pricePerDayYer * days
        val totalPriceUsd = car.pricePerDayUsd * days

        if (user.walletBalanceYer < totalPriceYer) return false

        // Update user balance and points
        val updatedUser = user.copy(
            walletBalanceYer = user.walletBalanceYer - totalPriceYer,
            walletBalanceUsd = user.walletBalanceUsd - totalPriceUsd,
            rewardPoints = user.rewardPoints + (totalPriceYer / 1000).toInt()
        )
        dao.insertUser(updatedUser)

        // Mark car as rented
        dao.updateCarRental(car.copy(isRented = true))

        val booking = BookingEntity(
            type = "CAR",
            targetId = car.id,
            titleAr = "سيارة: ${car.model} (${car.typeAr})",
            titleEn = "Car: ${car.model} (${car.typeEn})",
            bookingDate = date,
            quantity = days,
            totalPriceYer = totalPriceYer,
            totalPriceUsd = totalPriceUsd,
            status = "CONFIRMED"
        )
        dao.insertBooking(booking)
        return true
    }

    suspend fun addNewExperience(experience: ExperienceEntity) {
        dao.insertExperience(experience)
    }

    suspend fun deleteExperience(id: Long) {
        dao.deleteExperience(id)
    }

    suspend fun addItinerary(itinerary: ItineraryEntity) {
        dao.insertItinerary(itinerary)
    }

    suspend fun addFunds(yerAmount: Double, usdAmount: Double) {
        val user = dao.getUserById(1) ?: return
        dao.insertUser(
            user.copy(
                walletBalanceYer = user.walletBalanceYer + yerAmount,
                walletBalanceUsd = user.walletBalanceUsd + usdAmount
            )
        )
    }

    suspend fun populateIfEmpty() {
        val currentExperiences = dao.getAllExperiences().firstOrNull()
        if (currentExperiences.isNullOrEmpty()) {
            // Populate Default User
            dao.insertUser(
                UserEntity(
                    id = 1,
                    name = "المهندسة رغد",
                    role = "TRAVELER",
                    walletBalanceYer = 350000.0,
                    walletBalanceUsd = 1400.0,
                    rewardPoints = 1250
                )
            )

            // Populate Default Experiences
            val list = listOf(
                ExperienceEntity(
                    titleAr = "حصاد البن الحرازي الأصيل",
                    titleEn = "Authentic Harazi Coffee Harvesting",
                    descriptionAr = "انضم إلى المزارعين المحليين في جبال حراز التاريخية لتعلم جني ثمار البن العضوي وتحميصه بالطريقة التقليدية مع تذوق قهوة القشر الفاخرة.",
                    descriptionEn = "Join local farmers in the historic Haraz Mountains to harvest organic coffee beans, roast them traditionally, and enjoy premium Qishr coffee.",
                    categoryAr = "زراعة وثقافة",
                    categoryEn = "Agriculture & Culture",
                    cityAr = "حراز، صنعاء",
                    cityEn = "Haraz, Sana'a",
                    priceYer = 15000.0,
                    priceUsd = 60.0,
                    durationHours = 5,
                    maxParticipants = 8,
                    hostNameAr = "عم يحيى الحرازي",
                    hostNameEn = "Uncle Yahya Al-Harazi",
                    iconName = "coffee"
                ),
                ExperienceEntity(
                    titleAr = "مغامرة وتخييم في محمية ديتوا بسقطرى",
                    titleEn = "Socotra Island Detwah Lagoon Camping",
                    descriptionAr = "استكشف الكائنات البحرية النادرة والتنوع البيولوجي الفريد في أنقى محمية طبيعية بسقطرى مع مرشد محلي، ونم تحت سماء مرصعة بالنجوم.",
                    descriptionEn = "Explore rare marine species and unique biodiversity in Socotra's pristine Detwah Lagoon with a local guide, and camp under starry skies.",
                    categoryAr = "مغامرة وطبيعة",
                    categoryEn = "Adventure & Nature",
                    cityAr = "حديبو، سقطرى",
                    cityEn = "Hadiboh, Socotra",
                    priceYer = 100000.0,
                    priceUsd = 400.0,
                    durationHours = 24,
                    maxParticipants = 6,
                    hostNameAr = "سعيد السقطري",
                    hostNameEn = "Saeed Al-Socotri",
                    iconName = "camping"
                ),
                ExperienceEntity(
                    titleAr = "استخراج عسل السدر في وادي دوعن",
                    titleEn = "Sidr Honey Harvesting in Wadi Do'an",
                    descriptionAr = "شاهد طريقة تربية النحل وجني عسل السدر الحضرمي الأصيل، الأغلى والأنقى في العالم، من أشجار السدر التاريخية في قلب الوادي.",
                    descriptionEn = "Witness bee-keeping and harvest premium Hadrami Sidr Honey, the finest and most valuable honey in the world, from historic Sidr trees.",
                    categoryAr = "حرف وفنون",
                    categoryEn = "Crafts & Food",
                    cityAr = "دوعن، حضرموت",
                    cityEn = "Do'an, Hadramout",
                    priceYer = 25000.0,
                    priceUsd = 100.0,
                    durationHours = 4,
                    maxParticipants = 10,
                    hostNameAr = "الشيخ صالح باوزير",
                    hostNameEn = "Sheikh Saleh Ba-Wazir",
                    iconName = "honey"
                ),
                ExperienceEntity(
                    titleAr = "فنون الطبخ الصنعاني في بيت تقليدي",
                    titleEn = "Sana'ani Traditional Cooking Masterclass",
                    descriptionAr = "تعلم طريقة إعداد أطباق السلتة والشفوت والخبز الملوج الساخن في تنور حجري داخل بيت صنعاني قديم مبني من الطين والآجر.",
                    descriptionEn = "Learn to prepare authentic Saltah, Shafoot, and hot Malooj bread in a stone oven inside an ancient Sana'ani mud-brick tower house.",
                    categoryAr = "مأكولات شعبية",
                    categoryEn = "Culinary Arts",
                    cityAr = "صنعاء القديمة",
                    cityEn = "Old Sana'a",
                    priceYer = 10000.0,
                    priceUsd = 40.0,
                    durationHours = 3,
                    maxParticipants = 12,
                    hostNameAr = "أم أمين الصنعانية",
                    hostNameEn = "Um Amin Al-Sana'ani",
                    iconName = "cooking"
                ),
                ExperienceEntity(
                    titleAr = "صباغة النيلة وحياكة المعاوز في زبيد",
                    titleEn = "Indigo Dyeing & Weaving in Historic Zabid",
                    descriptionAr = "اكتشف أسرار صباغة الأقمشة بنبات النيلة الأزرق وحياكة المعاوز التهامية التقليدية في ورش زبيد التاريخية التي يعود عمرها لمئات السنين.",
                    descriptionEn = "Discover the secrets of natural Indigo fabric dyeing and weaving traditional Tihami Ma'awiz in historic workshops in Zabid.",
                    categoryAr = "حرف يدوية",
                    categoryEn = "Handicrafts",
                    cityAr = "زبيد، الحديدة",
                    cityEn = "Zabid, Al-Hudaydah",
                    priceYer = 8000.0,
                    priceUsd = 32.0,
                    durationHours = 4,
                    maxParticipants = 5,
                    hostNameAr = "المعلم عادل التهامي",
                    hostNameEn = "Master Adel Al-Tihami",
                    iconName = "weaving"
                )
            )

            for (exp in list) {
                dao.insertExperience(exp)
            }

            // Populate Tour Guides
            val guides = listOf(
                TourGuideEntity(
                    nameAr = "سعيد السقطري",
                    nameEn = "Saeed Al-Socotri",
                    cityAr = "سقطرى",
                    cityEn = "Socotra",
                    languagesAr = "العربية، الإنجليزية",
                    languagesEn = "Arabic, English",
                    rating = 4.9,
                    pricePerDayYer = 37500.0,
                    pricePerDayUsd = 150.0
                ),
                TourGuideEntity(
                    nameAr = "فاطمة الصنعانية",
                    nameEn = "Fatima Al-Sana'ani",
                    cityAr = "صنعاء القديمة",
                    cityEn = "Old Sana'a",
                    languagesAr = "العربية، الإنجليزية، الألمانية",
                    languagesEn = "Arabic, English, German",
                    rating = 4.8,
                    pricePerDayYer = 25000.0,
                    pricePerDayUsd = 100.0
                ),
                TourGuideEntity(
                    nameAr = "أحمد الحضرمي",
                    nameEn = "Ahmed Al-Hadrami",
                    cityAr = "سيئون وشبام",
                    cityEn = "Seiyun & Shibam",
                    languagesAr = "العربية، الإنجليزية",
                    languagesEn = "Arabic, English",
                    rating = 4.7,
                    pricePerDayYer = 20000.0,
                    pricePerDayUsd = 80.0
                )
            )

            for (guide in guides) {
                dao.insertTourGuide(guide)
            }

            // Populate Cars
            val cars = listOf(
                CarRentalEntity(
                    model = "Toyota Land Cruiser 4x4",
                    typeAr = "دفع رباعي جبلي",
                    typeEn = "Mountain SUV",
                    capacity = 7,
                    pricePerDayYer = 50000.0,
                    pricePerDayUsd = 200.0
                ),
                CarRentalEntity(
                    model = "Hyundai Elantra",
                    typeAr = "صالون اقتصادي",
                    typeEn = "Economic Sedan",
                    capacity = 5,
                    pricePerDayYer = 15000.0,
                    pricePerDayUsd = 60.0
                ),
                CarRentalEntity(
                    model = "Toyota Hilux Double Cabin",
                    typeAr = "نقل بري مغامرات",
                    typeEn = "Adventure Pick-up",
                    capacity = 5,
                    pricePerDayYer = 35000.0,
                    pricePerDayUsd = 140.0
                )
            )

            for (car in cars) {
                dao.insertCarRental(car)
            }
        }
    }
}
