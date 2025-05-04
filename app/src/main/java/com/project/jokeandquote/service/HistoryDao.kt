package com.project.jokeandquote.service

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.project.jokeandquote.helper.DatabaseHelper
import com.project.jokeandquote.model.HistoryRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HistoryDao(private val context: Context) {

    private val dbHelper = DatabaseHelper(context)

    // Insert record into the database asynchronously
    suspend fun insertRecord(record: HistoryRecord) {
        withContext(Dispatchers.IO) { // Switch to the IO thread to avoid blocking the UI thread
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put("type", record.type)
                put("dateIssued", record.dateIssued)
                put("clientName", record.clientName)
                put("eventName", record.eventName)
                put("eventAddress", record.eventAddress)
                put("eventLocation", record.eventLocation)
                put("eventDate", record.eventDate)
                put("eventTime", record.eventTime)
                put("companyName", record.companyName)
                put("companyAddress", record.companyAddress)
                put("jobType", record.jobType)
                put("jobDuration", record.jobDuration)
                put("amountCharged", record.amountCharged)
                put("fileName", record.fileName)
            }
            db.insert("history", null, values)
            db.close() // Close the db after operation
        }
    }


    suspend fun getAllRecords(): List<HistoryRecord> {
        return withContext(Dispatchers.IO) {
            val db = dbHelper.readableDatabase
            val records = mutableListOf<HistoryRecord>()

            val cursor: Cursor = db.query(
                "history",
                null,
                null,  // No filter
                null,
                null,
                null,
                "id DESC" // Sort by newest first
            )

            if (cursor.moveToFirst()) {
                do {
                    val record = HistoryRecord(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
                        dateIssued = cursor.getString(cursor.getColumnIndexOrThrow("dateIssued")),
                        clientName = cursor.getString(cursor.getColumnIndexOrThrow("clientName")),
                        eventName = cursor.getString(cursor.getColumnIndexOrThrow("eventName")),
                        eventAddress = cursor.getString(cursor.getColumnIndexOrThrow("eventAddress")),
                        eventLocation = cursor.getString(cursor.getColumnIndexOrThrow("eventLocation")),
                        eventDate = cursor.getString(cursor.getColumnIndexOrThrow("eventDate")),
                        eventTime = cursor.getString(cursor.getColumnIndexOrThrow("eventTime")),
                        companyName = cursor.getString(cursor.getColumnIndexOrThrow("companyName")),
                        companyAddress = cursor.getString(cursor.getColumnIndexOrThrow("companyAddress")),
                        jobType = cursor.getString(cursor.getColumnIndexOrThrow("jobType")),
                        jobDuration = cursor.getString(cursor.getColumnIndexOrThrow("jobDuration")),
                        amountCharged = cursor.getString(cursor.getColumnIndexOrThrow("amountCharged")),
                        fileName = cursor.getString(cursor.getColumnIndexOrThrow("fileName"))
                    )
                    records.add(record)
                } while (cursor.moveToNext())
            }
            cursor.close()
            db.close()

            records
        }
    }

    suspend fun delete(record: HistoryRecord) {
        withContext(Dispatchers.IO) {
            val db = dbHelper.writableDatabase
            db.delete(
                "history",
                "id = ?",
                arrayOf(record.id.toString())
            )
            db.close()
        }
    }

    suspend fun searchRecords(query: String): List<HistoryRecord> {
        return withContext(Dispatchers.IO) {
            val db = dbHelper.readableDatabase
            val records = mutableListOf<HistoryRecord>()

            // Use wildcard match across multiple fields
            val selection = """
            dateIssued LIKE ? OR
            clientName LIKE ? OR
            eventName LIKE ? OR
            eventAddress LIKE ? OR
            eventLocation LIKE ? OR
            eventDate LIKE ? OR
            eventTime LIKE ? OR
            companyName LIKE ? OR
            companyAddress LIKE ? OR
            jobType LIKE ? OR
            jobDuration LIKE ? OR
            amountCharged LIKE ? OR
            type LIKE ?
        """.trimIndent()

            val likeQuery = "%$query%"
            val selectionArgs = Array(12) { likeQuery }

            val cursor: Cursor = db.query(
                "history",
                null,
                selection,
                selectionArgs,
                null,
                null,
                "id DESC"
            )

            if (cursor.moveToFirst()) {
                do {
                    val record = HistoryRecord(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
                        dateIssued = cursor.getString(cursor.getColumnIndexOrThrow("dateIssued")),
                        clientName = cursor.getString(cursor.getColumnIndexOrThrow("clientName")),
                        eventName = cursor.getString(cursor.getColumnIndexOrThrow("eventName")),
                        eventAddress = cursor.getString(cursor.getColumnIndexOrThrow("eventAddress")),
                        eventLocation = cursor.getString(cursor.getColumnIndexOrThrow("eventLocation")),
                        eventDate = cursor.getString(cursor.getColumnIndexOrThrow("eventDate")),
                        eventTime = cursor.getString(cursor.getColumnIndexOrThrow("eventTime")),
                        companyName = cursor.getString(cursor.getColumnIndexOrThrow("companyName")),
                        companyAddress = cursor.getString(cursor.getColumnIndexOrThrow("companyAddress")),
                        jobType = cursor.getString(cursor.getColumnIndexOrThrow("jobType")),
                        jobDuration = cursor.getString(cursor.getColumnIndexOrThrow("jobDuration")),
                        amountCharged = cursor.getString(cursor.getColumnIndexOrThrow("amountCharged")),
                        fileName = cursor.getString(cursor.getColumnIndexOrThrow("fileName"))
                    )
                    records.add(record)
                } while (cursor.moveToNext())
            }

            cursor.close()
            db.close()
            records
        }
    }

}
