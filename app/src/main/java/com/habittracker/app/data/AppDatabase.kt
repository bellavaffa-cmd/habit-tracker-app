package com.habittracker.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.habittracker.app.data.smoking.CigarettePurchase
import com.habittracker.app.data.smoking.CigarettePurchaseDao
import com.habittracker.app.data.smoking.Converters
import com.habittracker.app.data.smoking.QuitPlan
import com.habittracker.app.data.smoking.QuitPlanDao
import com.habittracker.app.data.smoking.SmokingDao
import com.habittracker.app.data.smoking.SmokingLog

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

@Database(
    entities = [SmokingLog::class, CigarettePurchase::class, QuitPlan::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun smokingDao(): SmokingDao
    abstract fun cigarettePurchaseDao(): CigarettePurchaseDao
    abstract fun quitPlanDao(): QuitPlanDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habit-tracker.db"
                ).addMigrations(MIGRATION_1_2).build().also { INSTANCE = it }
            }
    }
}
