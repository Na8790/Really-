package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AiPlanningState {
    object Idle : AiPlanningState
    object Loading : AiPlanningState
    data class Success(val itineraryText: String) : AiPlanningState
    data class Error(val message: String) : AiPlanningState
}

class TajrubahViewModel(application: Application) : AndroidViewModel(application) {

    private val db: TajrubahDatabase by lazy {
        Room.databaseBuilder(
            application,
            TajrubahDatabase::class.java,
            "tajrubah_db"
        ).build()
    }

    private val repository: TajrubahRepository by lazy {
        TajrubahRepository(db.dao())
    }

    // App Preferences
    private val _isArabic = MutableStateFlow(true)
    val isArabic: StateFlow<Boolean> = _isArabic.asStateFlow()

    private val _isHostMode = MutableStateFlow(false)
    val isHostMode: StateFlow<Boolean> = _isHostMode.asStateFlow()

    // Database states
    val userFlow: StateFlow<UserEntity?> = MutableStateFlow<UserEntity?>(null).apply {
        viewModelScope.launch {
            repository.userFlow.collect { value = it }
        }
    }

    val experiencesFlow: StateFlow<List<ExperienceEntity>> = MutableStateFlow<List<ExperienceEntity>>(emptyList()).apply {
        viewModelScope.launch {
            repository.experiencesFlow.collect { value = it }
        }
    }

    val bookingsFlow: StateFlow<List<BookingEntity>> = MutableStateFlow<List<BookingEntity>>(emptyList()).apply {
        viewModelScope.launch {
            repository.bookingsFlow.collect { value = it }
        }
    }

    val tourGuidesFlow: StateFlow<List<TourGuideEntity>> = MutableStateFlow<List<TourGuideEntity>>(emptyList()).apply {
        viewModelScope.launch {
            repository.tourGuidesFlow.collect { value = it }
        }
    }

    val carRentalsFlow: StateFlow<List<CarRentalEntity>> = MutableStateFlow<List<CarRentalEntity>>(emptyList()).apply {
        viewModelScope.launch {
            repository.carRentalsFlow.collect { value = it }
        }
    }

    val itinerariesFlow: StateFlow<List<ItineraryEntity>> = MutableStateFlow<List<ItineraryEntity>>(emptyList()).apply {
        viewModelScope.launch {
            repository.itinerariesFlow.collect { value = it }
        }
    }

    // UI Status
    private val _aiPlanningState = MutableStateFlow<AiPlanningState>(AiPlanningState.Idle)
    val aiPlanningState: StateFlow<AiPlanningState> = _aiPlanningState.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.populateIfEmpty()
        }
    }

    fun toggleLanguage() {
        _isArabic.value = !_isArabic.value
    }

    fun toggleHostMode() {
        _isHostMode.value = !_isHostMode.value
    }

    fun showToast(message: String) {
        viewModelScope.launch {
            _toastMessage.emit(message)
        }
    }

    fun bookExperience(experience: ExperienceEntity, quantity: Int, date: String) {
        viewModelScope.launch {
            val success = repository.bookExperience(experience, quantity, date)
            if (success) {
                showToast(
                    if (_isArabic.value) "تم تأكيد حجز التجربة بنجاح!" else "Experience booked successfully!"
                )
            } else {
                showToast(
                    if (_isArabic.value) "رصيد المحفظة غير كافٍ لإتمام الحجز!" else "Insufficient wallet balance!"
                )
            }
        }
    }

    fun bookTourGuide(guide: TourGuideEntity, days: Int, date: String) {
        viewModelScope.launch {
            val success = repository.bookTourGuide(guide, days, date)
            if (success) {
                showToast(
                    if (_isArabic.value) "تم حجز المرشد بنجاح!" else "Guide booked successfully!"
                )
            } else {
                showToast(
                    if (_isArabic.value) "رصيد المحفظة غير كافٍ لإتمام الحجز!" else "Insufficient wallet balance!"
                )
            }
        }
    }

    fun bookCarRental(car: CarRentalEntity, days: Int, date: String) {
        viewModelScope.launch {
            val success = repository.bookCarRental(car, days, date)
            if (success) {
                showToast(
                    if (_isArabic.value) "تم حجز السيارة بنجاح!" else "Car booked successfully!"
                )
            } else {
                showToast(
                    if (_isArabic.value) "رصيد المحفظة غير كافٍ لإتمام الحجز!" else "Insufficient wallet balance!"
                )
            }
        }
    }

    fun addExperience(
        titleAr: String,
        titleEn: String,
        descAr: String,
        descEn: String,
        categoryAr: String,
        categoryEn: String,
        cityAr: String,
        cityEn: String,
        priceYer: Double,
        priceUsd: Double,
        duration: Int,
        maxParticipants: Int,
        hostNameAr: String,
        hostNameEn: String
    ) {
        viewModelScope.launch {
            val exp = ExperienceEntity(
                titleAr = titleAr,
                titleEn = titleEn,
                descriptionAr = descAr,
                descriptionEn = descEn,
                categoryAr = categoryAr,
                categoryEn = categoryEn,
                cityAr = cityAr,
                cityEn = cityEn,
                priceYer = priceYer,
                priceUsd = priceUsd,
                durationHours = duration,
                maxParticipants = maxParticipants,
                hostNameAr = hostNameAr,
                hostNameEn = hostNameEn,
                isCustomCreated = true,
                iconName = "custom"
            )
            repository.addNewExperience(exp)
            showToast(
                if (_isArabic.value) "تمت إضافة تجربتك المحلية بنجاح!" else "Your local experience added successfully!"
            )
        }
    }

    fun deleteExperience(id: Long) {
        viewModelScope.launch {
            repository.deleteExperience(id)
            showToast(
                if (_isArabic.value) "تم حذف التجربة بنجاح" else "Experience deleted successfully"
            )
        }
    }

    fun generateAiItinerary(
        city: String,
        durationDays: Int,
        budget: String,
        interests: String
    ) {
        viewModelScope.launch {
            _aiPlanningState.value = AiPlanningState.Loading
            try {
                val isAr = _isArabic.value
                val result = GeminiClient.generateItinerary(city, durationDays, budget, interests, isAr)

                // Save itinerary to DB
                val itinerary = ItineraryEntity(
                    titleAr = "رحلة ذكية مخصصة إلى $city",
                    titleEn = "Custom Smart Trip to $city",
                    budgetLevel = budget,
                    durationDays = durationDays,
                    interests = interests,
                    responseText = result
                )
                repository.addItinerary(itinerary)

                _aiPlanningState.value = AiPlanningState.Success(result)
                showToast(
                    if (isAr) "تم إنشاء خطة رحلتك الذكية بالذكاء الاصطناعي!" else "Your smart itinerary has been generated by AI!"
                )
            } catch (e: Exception) {
                _aiPlanningState.value = AiPlanningState.Error(e.message ?: "Unknown error")
                showToast(
                    if (_isArabic.value) "فشل إنشاء الخطة: ${e.message}" else "Failed to plan itinerary: ${e.message}"
                )
            }
        }
    }

    fun resetAiPlanningState() {
        _aiPlanningState.value = AiPlanningState.Idle
    }

    fun addFunds(yerAmount: Double, usdAmount: Double) {
        viewModelScope.launch {
            repository.addFunds(yerAmount, usdAmount)
            showToast(
                if (_isArabic.value) "تم شحن المحفظة بنجاح! شكراً لك." else "Wallet loaded successfully! Thank you."
            )
        }
    }
}
