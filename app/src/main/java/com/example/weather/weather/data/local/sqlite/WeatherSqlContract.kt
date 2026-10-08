package com.example.weather.weather.data.local.sqlite

internal object WeatherSqlContract {

    object Cities {
        const val TABLE_NAME = "cities"

        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_COUNTRY_CODE = "country_code"
        const val COLUMN_LATITUDE = "latitude"
        const val COLUMN_LONGITUDE = "longitude"
        const val COLUMN_TIME_ZONE_ID = "time_zone_id"

        const val CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                $COLUMN_ID TEXT NOT NULL PRIMARY KEY,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_COUNTRY_CODE TEXT NOT NULL,
                $COLUMN_LATITUDE REAL NOT NULL,
                $COLUMN_LONGITUDE REAL NOT NULL,
                $COLUMN_TIME_ZONE_ID TEXT NOT NULL
            )
        """

        const val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
    }

    object CurrentWeather {
        const val TABLE_NAME = "current_weather"

        const val COLUMN_CITY_ID = "city_id"
        const val COLUMN_OBSERVED_AT_EPOCH_MILLIS = "observed_at_epoch_millis"
        const val COLUMN_TEMPERATURE_CELSIUS = "temperature_celsius"
        const val COLUMN_APPARENT_TEMPERATURE_CELSIUS = "apparent_temperature_celsius"
        const val COLUMN_RELATIVE_HUMIDITY_PERCENT = "relative_humidity_percent"
        const val COLUMN_WIND_SPEED_KMH = "wind_speed_kmh"
        const val COLUMN_WEATHER_CODE = "weather_code"
        const val COLUMN_UPDATED_AT_EPOCH_MILLIS = "updated_at_epoch_millis"

        const val CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                $COLUMN_CITY_ID TEXT NOT NULL PRIMARY KEY,
                $COLUMN_OBSERVED_AT_EPOCH_MILLIS INTEGER NOT NULL,
                $COLUMN_TEMPERATURE_CELSIUS REAL NOT NULL,
                $COLUMN_APPARENT_TEMPERATURE_CELSIUS REAL NOT NULL,
                $COLUMN_RELATIVE_HUMIDITY_PERCENT INTEGER NOT NULL,
                $COLUMN_WIND_SPEED_KMH REAL NOT NULL,
                $COLUMN_WEATHER_CODE INTEGER NOT NULL,
                $COLUMN_UPDATED_AT_EPOCH_MILLIS INTEGER NOT NULL,
                FOREIGN KEY ($COLUMN_CITY_ID)
                    REFERENCES ${Cities.TABLE_NAME}(${Cities.COLUMN_ID})
                    ON DELETE CASCADE
            )
        """

        const val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
    }

    object HourlyWeather {
        const val TABLE_NAME = "hourly_weather"

        const val COLUMN_CITY_ID = "city_id"
        const val COLUMN_FORECAST_AT_EPOCH_MILLIS = "forecast_at_epoch_millis"
        const val COLUMN_TEMPERATURE_CELSIUS = "temperature_celsius"
        const val COLUMN_APPARENT_TEMPERATURE_CELSIUS = "apparent_temperature_celsius"
        const val COLUMN_PRECIPITATION_PROBABILITY_PERCENT =
            "precipitation_probability_percent"
        const val COLUMN_RELATIVE_HUMIDITY_PERCENT = "relative_humidity_percent"
        const val COLUMN_WIND_SPEED_KMH = "wind_speed_kmh"
        const val COLUMN_WEATHER_CODE = "weather_code"

        const val CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                $COLUMN_CITY_ID TEXT NOT NULL,
                $COLUMN_FORECAST_AT_EPOCH_MILLIS INTEGER NOT NULL,
                $COLUMN_TEMPERATURE_CELSIUS REAL NOT NULL,
                $COLUMN_APPARENT_TEMPERATURE_CELSIUS REAL NOT NULL,
                $COLUMN_PRECIPITATION_PROBABILITY_PERCENT INTEGER NOT NULL,
                $COLUMN_RELATIVE_HUMIDITY_PERCENT INTEGER NOT NULL,
                $COLUMN_WIND_SPEED_KMH REAL NOT NULL,
                $COLUMN_WEATHER_CODE INTEGER NOT NULL,
                PRIMARY KEY ($COLUMN_CITY_ID, $COLUMN_FORECAST_AT_EPOCH_MILLIS),
                FOREIGN KEY ($COLUMN_CITY_ID)
                    REFERENCES ${Cities.TABLE_NAME}(${Cities.COLUMN_ID})
                    ON DELETE CASCADE
            )
        """

        const val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
    }

    object DailyWeather {
        const val TABLE_NAME = "daily_weather"

        const val COLUMN_CITY_ID = "city_id"
        const val COLUMN_FORECAST_DATE = "forecast_date"
        const val COLUMN_MINIMUM_TEMPERATURE_CELSIUS = "minimum_temperature_celsius"
        const val COLUMN_MAXIMUM_TEMPERATURE_CELSIUS = "maximum_temperature_celsius"
        const val COLUMN_PRECIPITATION_PROBABILITY_PERCENT =
            "precipitation_probability_percent"
        const val COLUMN_WEATHER_CODE = "weather_code"

        const val CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                $COLUMN_CITY_ID TEXT NOT NULL,
                $COLUMN_FORECAST_DATE TEXT NOT NULL,
                $COLUMN_MINIMUM_TEMPERATURE_CELSIUS REAL NOT NULL,
                $COLUMN_MAXIMUM_TEMPERATURE_CELSIUS REAL NOT NULL,
                $COLUMN_PRECIPITATION_PROBABILITY_PERCENT INTEGER NOT NULL,
                $COLUMN_WEATHER_CODE INTEGER NOT NULL,
                PRIMARY KEY ($COLUMN_CITY_ID, $COLUMN_FORECAST_DATE),
                FOREIGN KEY ($COLUMN_CITY_ID)
                    REFERENCES ${Cities.TABLE_NAME}(${Cities.COLUMN_ID})
                    ON DELETE CASCADE
            )
        """

        const val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
    }

    val createStatements = listOf(
        Cities.CREATE_TABLE,
        CurrentWeather.CREATE_TABLE,
        HourlyWeather.CREATE_TABLE,
        DailyWeather.CREATE_TABLE,
    )

    val dropStatements = listOf(
        DailyWeather.DROP_TABLE,
        HourlyWeather.DROP_TABLE,
        CurrentWeather.DROP_TABLE,
        Cities.DROP_TABLE,
    )
}
