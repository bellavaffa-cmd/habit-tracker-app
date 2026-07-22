package com.habittracker.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.habittracker.app.data.calories.CalorieLog
import com.habittracker.app.data.calories.CalorieLogDao
import com.habittracker.app.data.hydration.HydrationDao
import com.habittracker.app.data.hydration.HydrationLog
import com.habittracker.app.data.smoking.CigarettePurchase
import com.habittracker.app.data.smoking.CigarettePurchaseDao
import com.habittracker.app.data.smoking.Converters
import com.habittracker.app.data.smoking.QuitPlan
import com.habittracker.app.data.smoking.QuitPlanDao
import com.habittracker.app.data.smoking.SmokingDao
import com.habittracker.app.data.smoking.SmokingLog
import com.habittracker.app.data.workout.GymExerciseDao
import com.habittracker.app.data.workout.GymExerciseLog
import com.habittracker.app.data.workout.WorkoutDao
import com.habittracker.app.data.workout.WorkoutLog

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `cigarette_purchase` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `timestampMillis` INTEGER NOT NULL,
                `packsBought` INTEGER NOT NULL,
                `pricePerPack` REAL NOT NULL,
                `sticksPerPack` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `quit_plan` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `type` TEXT NOT NULL,
                `startDateMillis` INTEGER NOT NULL,
                `targetQuitDateMillis` INTEGER NOT NULL,
                `startValue` INTEGER NOT NULL,
                `isActive` INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `calorie_log` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `timestampMillis` INTEGER NOT NULL,
                `photoPath` TEXT,
                `foodDescription` TEXT NOT NULL,
                `calories` INTEGER NOT NULL,
                `proteinGrams` REAL NOT NULL,
                `carbsGrams` REAL NOT NULL,
                `fatGrams` REAL NOT NULL
            )
            """.trimIndent()
        )
    }
}

private val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `hydration_log` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `timestampMillis` INTEGER NOT NULL,
                `amountMl` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `workout_log` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `timestampMillis` INTEGER NOT NULL,
                `type` TEXT NOT NULL,
                `durationMinutes` INTEGER NOT NULL,
                `notes` TEXT
            )
            """.trimIndent()
        )
    }
}

private val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `gym_log` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `timestampMillis` INTEGER NOT NULL,
                `exerciseName` TEXT NOT NULL,
                `muscleGroup` TEXT NOT NULL,
                `sets` INTEGER NOT NULL,
                `reps` INTEGER NOT NULL,
                `weightKg` REAL NOT NULL
            )
            """.trimIndent()
        )
    }
}

@Database(
    entities = [
        SmokingLog::class, CigarettePurchase::class, QuitPlan::class, CalorieLog::class,
        HydrationLog::class, WorkoutLog::class, GymExerciseLog::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun smokingDao(): SmokingDao
    abstract fun cigarettePurchaseDao(): CigarettePurchaseDao
    abstract fun quitPlanDao(): QuitPlanDao
    abstract fun calorieLogDao(): CalorieLogDao
    abstract fun hydrationDao(): HydrationDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun gymExerciseDao(): GymExerciseDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habit-tracker.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5).build().also { INSTANCE = it }
            }
    }
}
