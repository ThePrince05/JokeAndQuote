package com.project.jokeandquote.helper

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class HistoryDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "history.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                type TEXT NOT NULL,  -- "Invoice" or "Quotation"
                dateIssued TEXT,
                clientName TEXT,
                eventName TEXT,
                eventAddress TEXT,
                eventLocation TEXT,
                eventDate TEXT,
                eventTime TEXT,
                companyName TEXT,
                companyAddress TEXT,
                jobType TEXT,
                jobDuration TEXT,
                amountCharged TEXT,
                fileName TEXT
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS history")
        onCreate(db)
    }
}
