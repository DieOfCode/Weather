package com.example.weather.weather.data.local.sqlite

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "weather_sqlite.db"
        private const val DATABASE_VERSION = 1
    }


    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        WeatherSqlContract.createStatements.forEach(db::execSQL)

    }

    //понятно, что это не прод вариант, но сразу описывать миграции не имеет смысла
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        WeatherSqlContract.dropStatements.forEach(db::execSQL)
        WeatherSqlContract.createStatements.forEach(db::execSQL)
    }
}
