package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TajrubahDao {
    // Users
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserByIdFlow(id: Int = 1): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int = 1): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // Experiences
    @Query("SELECT * FROM experiences ORDER BY id DESC")
    fun getAllExperiences(): Flow<List<ExperienceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExperience(experience: ExperienceEntity)

    @Query("DELETE FROM experiences WHERE id = :id")
    suspend fun deleteExperience(id: Long)

    // Bookings
    @Query("SELECT * FROM bookings ORDER BY timestamp DESC")
    fun getAllBookings(): Flow<List<BookingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity)

    @Query("DELETE FROM bookings WHERE id = :id")
    suspend fun deleteBooking(id: Long)

    // Tour Guides
    @Query("SELECT * FROM tour_guides ORDER BY rating DESC")
    fun getAllTourGuides(): Flow<List<TourGuideEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTourGuide(guide: TourGuideEntity)

    @Update
    suspend fun updateTourGuide(guide: TourGuideEntity)

    // Car Rentals
    @Query("SELECT * FROM car_rentals")
    fun getAllCarRentals(): Flow<List<CarRentalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCarRental(car: CarRentalEntity)

    @Update
    suspend fun updateCarRental(car: CarRentalEntity)

    // Itineraries
    @Query("SELECT * FROM itineraries ORDER BY timestamp DESC")
    fun getAllItineraries(): Flow<List<ItineraryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItinerary(itinerary: ItineraryEntity)
}

@Database(
    entities = [
        UserEntity::class,
        ExperienceEntity::class,
        BookingEntity::class,
        TourGuideEntity::class,
        CarRentalEntity::class,
        ItineraryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TajrubahDatabase : RoomDatabase() {
    abstract fun dao(): TajrubahDao
}
