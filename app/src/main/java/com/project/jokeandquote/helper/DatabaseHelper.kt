package com.project.jokeandquote.helper

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "Database.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createHistoryTable = """
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

        val createTalentDetailsTable = """
            CREATE TABLE talentDetails (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                officeAddress TEXT,
                phoneNumber TEXT,
                emailAddress TEXT,
                bankName TEXT,
                accountNumber TEXT,
                accountType TEXT,
                nameOnAccount TEXT,
                imageUrl TEXT
            );
        """.trimIndent()

        db.execSQL(createTalentDetailsTable)
        db.execSQL(createHistoryTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS history")
        db.execSQL("DROP TABLE IF EXISTS talentDetails")
        onCreate(db)
    }
}
