package com.example.weather.weather.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import com.example.weather.weather.data.local.room.entity.CityEntity
import com.example.weather.weather.data.local.room.entity.CurrentWeatherEntity
import com.example.weather.weather.data.local.room.entity.DailyWeatherEntity
import com.example.weather.weather.data.local.room.entity.HourlyWeatherEntity
import com.example.weather.weather.data.local.room.entity.SelectedCityEntity
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        CityEntity::class,
        CurrentWeatherEntity::class,
        DailyWeatherEntity::class,
        HourlyWeatherEntity::class,
        SelectedCityEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class WeatherRoomDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_NAME = "weather_room.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `selected_city` (
                        `singleton_id` INTEGER NOT NULL,
                        `city_id` TEXT NOT NULL,
                        PRIMARY KEY(`singleton_id`),
                        FOREIGN KEY(`city_id`) REFERENCES `cities`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS `index_selected_city_city_id`
                    ON `selected_city` (`city_id`)
                    """.trimIndent(),
                )
            }
        }
    }

    abstract fun weatherDao(): WeatherDao
}
