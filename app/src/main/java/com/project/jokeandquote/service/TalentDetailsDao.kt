package com.project.jokeandquote.service

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.project.jokeandquote.helper.DatabaseHelper
import com.project.jokeandquote.model.HistoryRecord
import com.project.jokeandquote.model.TalentDetailsRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TalentDetailsDao(private val context: Context) {

    private val dbHelper = DatabaseHelper(context)

    // Insert record into the database asynchronously
    suspend fun insertRecord(record: TalentDetailsRecord) {
        withContext(Dispatchers.IO) { // Switch to the IO thread to avoid blocking the UI thread
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put("name", record.name)
                put("officeAddress", record.officeAddress)
                put("phoneNumber", record.phoneNumber)
                put("emailAddress", record.emailAddress)
                put("bankName", record.bankName)
                put("accountNumber", record.accountNumber)
                put("accountType", record.accountType)
                put("nameOnAccount", record.nameOnAccount)
                put("imageUrl", record.nameOnAccount)
            }
            db.insert("talentDetails", null, values)
            db.close() // Close the db after operation
        }
    }

    // Get records from the database
    fun getRecords(): List<TalentDetailsRecord> {
        val db = dbHelper.readableDatabase
        val records = mutableListOf<TalentDetailsRecord>()

        val cursor: Cursor = db.query(
            "talentDetails",
            null,
            null,
            null,
            null,
            null,
            "id DESC" // newest first
        )

        if (cursor.moveToFirst()) {
            do {
                val record = TalentDetailsRecord(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    officeAddress = cursor.getString(cursor.getColumnIndexOrThrow("officeAddress")),
                    phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow("phoneNumber")),
                    emailAddress = cursor.getString(cursor.getColumnIndexOrThrow("emailAddress")),
                    bankName = cursor.getString(cursor.getColumnIndexOrThrow("bankName")),
                    accountNumber = cursor.getString(cursor.getColumnIndexOrThrow("accountNumber")),
                    accountType = cursor.getString(cursor.getColumnIndexOrThrow("accountType")),
                    nameOnAccount = cursor.getString(cursor.getColumnIndexOrThrow("nameOnAccount")),
                    imageUrl = cursor.getString(cursor.getColumnIndexOrThrow("imageUrl"))
                )
                records.add(record)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return records
    }


    suspend fun delete(record: TalentDetailsRecord) {
        withContext(Dispatchers.IO) {
            val db = dbHelper.writableDatabase
            db.delete(
                "talentDetails",
                "id = ?",
                arrayOf(record.id.toString())
            )
            db.close()
        }
    }
    suspend fun updateRecord(record: TalentDetailsRecord) {
        withContext(Dispatchers.IO) {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put("name", record.name)
                put("officeAddress", record.officeAddress)
                put("phoneNumber", record.phoneNumber)
                put("emailAddress", record.emailAddress)
                put("bankName", record.bankName)
                put("accountNumber", record.accountNumber)
                put("accountType", record.accountType)
                put("nameOnAccount", record.nameOnAccount)
                put("imageUrl", record.nameOnAccount)
            }

            db.update(
                "talentDetails",
                values,
                "id = ?",
                arrayOf(record.id.toString())
            )
            db.close()
        }
    }



}
