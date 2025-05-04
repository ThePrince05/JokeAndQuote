package com.project.jokeandquote.service

import android.app.Activity
import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import android.graphics.Bitmap
import android.net.Uri
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.project.jokeandquote.R
import com.project.jokeandquote.model.HistoryRecord
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.encryption.AccessPermission
import com.tom_roush.pdfbox.pdmodel.encryption.StandardProtectionPolicy
import com.tom_roush.pdfbox.pdmodel.font.PDType0Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import java.util.*

class PdfService(private val context: Context) {

    init {
        PDFBoxResourceLoader.init(context.applicationContext)
    }



    suspend fun createQuotationPDF(
        comedianName: String,
        comedianOfficeAddress: String,
        comedianPhoneNumber: String,
        comedianEmailAddress: String,
        comedianBankName: String,
        comedianAccountNumber: String,
        comedianAccountType: String,
        comedianNameOnAccount: String,
        clientName: String,
        eventName: String,
        eventLocation: String,
        selectedDate: String,
        selectedTime: String,
        jobType: String,
        jobDuration: String,
        amountCharged: String,
        fileName: String,
        historyRecord: HistoryRecord? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val pageSize = PDRectangle.A4
        val margin = 36f
        val columnWidth = 120.47f
        val rowHeight = 32.85f
        val numRows = 2
        val numCols = 4

        val document = PDDocument()
        val page = PDPage(pageSize)
        document.addPage(page)

        try {
            // fonts
            val fontRegular = PDType0Font.load(document, context.assets.open("calibri.ttf"))
            val fontBold    = PDType0Font.load(document, context.assets.open("calibri_bold.ttf"))

            val contentStream = PDPageContentStream(document, page)

            // logo image
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.logo)
            val stream = ByteArrayOutputStream().apply {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, this)
            }
            val image = PDImageXObject.createFromByteArray(document, stream.toByteArray(), "image")

            val imageWidth  = 212.625f
            val imageHeight = 190.745f
            val imageX = (pageSize.width - imageWidth) / 2
            val imageY = pageSize.height - margin - imageHeight
            contentStream.drawImage(image, imageX, imageY, imageWidth, imageHeight)

            val textY = imageY - 50f

            // Header Information
            contentStream.beginText()
            contentStream.setFont(fontRegular, 11f)
            contentStream.newLineAtOffset(margin, textY)
            contentStream.showText(comedianName)
            contentStream.newLineAtOffset(0f, -15f)
            contentStream.showText(comedianOfficeAddress)
            contentStream.newLineAtOffset(0f, -15f)
            contentStream.showText("C: $comedianPhoneNumber")
            contentStream.newLineAtOffset(0f, -15f)
            contentStream.showText("E: $comedianEmailAddress")
            contentStream.endText()

            // Quote Heading
            contentStream.beginText()
            contentStream.setFont(fontBold, 14f)
            contentStream.newLineAtOffset(margin, textY - 80f)
            contentStream.showText("QUOTE")
            contentStream.endText()

            // Underline heading
            val tw = fontBold.getStringWidth("QUOTE") / 1000 * 14f
            contentStream.moveTo(margin, textY - 85f)
            contentStream.lineTo(margin + tw, textY - 85f)
            contentStream.setLineWidth(0.5f)
            contentStream.stroke()

            val dateIssued = historyRecord?.dateIssued ?: SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())

            // Quote information
            contentStream.beginText()
            contentStream.setFont(fontBold, 11f)
            contentStream.newLineAtOffset(margin, textY - 100f)
            contentStream.showText("Date Issued: $dateIssued")
            contentStream.newLineAtOffset(0f, -15f)
            contentStream.showText("Client: " + clientName)
            contentStream.newLineAtOffset(0f, -15f)
            contentStream.showText("Event: " + eventName)
            contentStream.newLineAtOffset(0f, -15f)
            contentStream.showText("Location: " + eventLocation)
            contentStream.newLineAtOffset(0f, -15f)
            contentStream.showText("Event Date: " + selectedDate)
            contentStream.newLineAtOffset(0f, -15f)
            contentStream.showText("Time: " + selectedTime)
            contentStream.endText()

            // Table header
            val tableStartY = 345f
            val tableStartX = margin
            val headers = arrayOf("TALENT","DESCRIPTION","AMOUNT","DURATION")
            contentStream.setFont(fontBold, 11f)
            for (col in 0 until numCols) {
                contentStream.addRect(tableStartX + col*columnWidth, tableStartY, columnWidth, rowHeight)
                contentStream.stroke()
                contentStream.beginText()
                contentStream.newLineAtOffset(tableStartX + 5 + col*columnWidth, tableStartY + 8f)
                contentStream.showText(headers[col])
                contentStream.endText()
            }

            // Table body
            val bodyStartY = tableStartY - rowHeight
            val tableData = arrayOf(
                arrayOf(comedianName, jobType, "R$amountCharged", jobDuration),
                arrayOf(" ", "TOTAL", " ", "R$amountCharged")
            )
            contentStream.setFont(fontRegular, 11f)
            for (row in 0 until numRows) {
                for (col in 0 until numCols) {
                    val x = tableStartX + col*columnWidth
                    val y = bodyStartY - row*rowHeight
                    contentStream.addRect(x, y, columnWidth, rowHeight)
                    contentStream.stroke()
                    contentStream.beginText()
                    contentStream.newLineAtOffset(x + 5f, y + 8f)
                    contentStream.showText(tableData[row][col])
                    contentStream.endText()
                }
            }

            // Banking Details heading
            contentStream.beginText()
            contentStream.setFont(fontBold, 13f)
            contentStream.newLineAtOffset(margin, textY - 310f)
            contentStream.showText("Banking Details are as follows:")
            contentStream.endText()
            val bw = fontBold.getStringWidth("Banking Details are as follows:")/1000*13f
            contentStream.moveTo(margin, textY - 315f)
            contentStream.lineTo(margin + bw, textY - 315f)
            contentStream.setLineWidth(0.5f)
            contentStream.stroke()

            // Banking info
            contentStream.beginText()
            contentStream.setFont(fontBold, 11f)
            contentStream.newLineAtOffset(margin, textY - 330f)
            contentStream.showText(comedianBankName)
            contentStream.newLineAtOffset(0f, -20f)
            contentStream.showText(comedianAccountNumber)
            contentStream.newLineAtOffset(0f, -20f)
            contentStream.showText(comedianAccountType)
            contentStream.newLineAtOffset(0f, -20f)
            contentStream.showText(comedianNameOnAccount)
            contentStream.endText()

            // Disclaimer (red)
            contentStream.setNonStrokingColor(255,0,0)
            val first = "(The aforementioned quotation is only valid 7 days from the date it was issued, unless 50% of the agreed"
            contentStream.beginText()
            contentStream.setFont(fontBold,12f)
            val fmw = fontBold.getStringWidth(first)/1000*12f
            val fx = (pageSize.width - fmw)/2
            contentStream.newLineAtOffset(fx, textY - 480f)
            contentStream.showText(first)
            contentStream.endText()

            val second = "amount is deposited to reserve the date.)"
            contentStream.beginText()
            contentStream.setFont(fontBold,12f)
            val smw = fontBold.getStringWidth(second)/1000*12f
            val sx = (pageSize.width - smw)/2
            contentStream.newLineAtOffset(sx, textY - 500f)
            contentStream.showText(second)
            contentStream.endText()
            contentStream.setNonStrokingColor(0,0,0)

            // Footer phone
            contentStream.beginText()
            contentStream.setFont(fontBold,11f)
            contentStream.newLineAtOffset(margin, textY - 540f)
            contentStream.showText("C: $comedianPhoneNumber")
            contentStream.endText()

            contentStream.close()

            // Protect & save
           val password = fileName.extractDateTimeFromFilename()

            val perm = AccessPermission()
            perm.setCanModify(false)
            perm.setCanPrint(true)
            perm.setCanAssembleDocument(false)
            perm.setCanExtractContent(false)

            val policy = StandardProtectionPolicy(password, null, perm).apply {
                encryptionKeyLength = 128
            }
            document.protect(policy)

            // Saves here for Android 10 Upwards

            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/Records")
            }
            val uri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), values)
            uri?.let {
                context.contentResolver.openOutputStream(it).use { os ->
                    document.save(os)
                }

                // Switch to main thread to show dialog
                withContext(Dispatchers.Main) {
                    if (historyRecord == null && context is Activity) {
                        MaterialAlertDialogBuilder(context)
                            .setTitle("Document Generated!!!")
                            .setMessage("PDF saved successfully in: Phone Storage/Documents/Records/")
                            .setNegativeButton("Okay", null)
                            .show()
                    }

                }

                // Pass the URI to the sharePdf function
                if (historyRecord == null) {
                    sharePdf(context, it)
                }
            }
            document.close()

            return@withContext true // success
        } catch (ex: Exception) {
            Log.e("PDFCreation", "Exception occurred: ${ex.message}", ex)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Oops, something went wrong on: ${ex.message}", Toast.LENGTH_LONG).show()
            }
            return@withContext false // failure
        }

    }

    suspend fun createInvoicePDF(
        comedianName: String,
        comedianOfficeAddress: String,
        comedianPhoneNumber: String,
        comedianEmailAddress: String,
        comedianBankName: String,
        comedianAccountNumber: String,
        comedianAccountType: String,
        comedianNameOnAccount: String,
        clientName: String,
        eventName: String,
        eventAddress: String,
        eventDate: String,
        companyName: String,
        companyAddress: String,
        time: String,
        jobType: String,
        jobDuration: String,
        amountCharged: String,
        fileName: String,
        historyRecord: HistoryRecord? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val pageSize = PDRectangle.A4
        val margin = 36f
        val columnWidth = 120.47f
        val rowHeight = 32.85f
        val numRows = 2
        val numCols = 4

        val document = PDDocument()
        val page = PDPage(pageSize)
        document.addPage(page)

        try {
            val fontRegular = PDType0Font.load(document, context.assets.open("calibri.ttf"))
            val fontBold = PDType0Font.load(document, context.assets.open("calibri_bold.ttf"))
            val contentStream = PDPageContentStream(document, page)

            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.logo)
            val stream = ByteArrayOutputStream().apply {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, this)
            }
            val image = PDImageXObject.createFromByteArray(document, stream.toByteArray(), "image")

            val imageWidth = 212.625f
            val imageHeight = 190.745f
            val imageX = (pageSize.width - imageWidth) / 2
            val imageY = pageSize.height - margin - imageHeight
            contentStream.drawImage(image, imageX, imageY, imageWidth, imageHeight)

            val textY = imageY - 50f

            // Company Information
            contentStream.beginText()
            contentStream.setFont(fontRegular, 11f)
            contentStream.newLineAtOffset(margin, textY)
            contentStream.showText(comedianName)
            contentStream.newLineAtOffset(0f, -15f)
            contentStream.showText(comedianOfficeAddress)
            contentStream.newLineAtOffset(0f, -15f)
            contentStream.showText("C: $comedianPhoneNumber")
            contentStream.newLineAtOffset(0f, -15f)
            contentStream.showText("E: $comedianEmailAddress")
            contentStream.endText()

            // Invoice Heading
            contentStream.beginText()
            contentStream.setFont(fontBold, 14f)
            contentStream.newLineAtOffset(margin, textY - 80f)
            contentStream.showText("Invoice:")
            contentStream.endText()

            contentStream.moveTo(margin, textY - 85f)
            contentStream.lineTo(margin + 50f, textY - 85f)
            contentStream.setLineWidth(0.5f)
            contentStream.stroke()

            val dateIssued = historyRecord?.dateIssued ?: SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())

            // Invoice information
            val invoiceNumber = comedianName.sliceAndCapitalize() + SimpleDateFormat("ddMMyyyyHHmmss", Locale.getDefault()).format(Date())
            contentStream.beginText()
            contentStream.setFont(fontBold, 11f)
            contentStream.newLineAtOffset(margin, textY - 100f)
            contentStream.showText("Date Issued: $dateIssued")
            contentStream.newLineAtOffset(0f, -15f)
            contentStream.showText("Invoice No: $invoiceNumber")
            contentStream.newLineAtOffset(0f, -15f)
            contentStream.showText("Client: $clientName")
            contentStream.newLineAtOffset(0f, -15f)
            contentStream.showText("Company Name: $companyName")
            contentStream.newLineAtOffset(0f, -15f)
            contentStream.showText("Company Address: $companyAddress")
            contentStream.newLineAtOffset(0f, -15f)
            contentStream.showText("Event Name: $eventName")
            contentStream.newLineAtOffset(0f, -15f)
            contentStream.showText("Event Address: $eventAddress")
            contentStream.newLineAtOffset(0f, -15f)
            contentStream.showText("Event Date: $eventDate")
            contentStream.newLineAtOffset(0f, -15f)
            contentStream.showText("Time: $time")
            contentStream.endText()


            val tableStartY = 300f
            val tableStartX = margin
            val headers = arrayOf("TALENT", "DESCRIPTION", "AMOUNT", "DURATION")
            contentStream.setFont(fontBold, 11f)
            for (col in 0 until numCols) {
                contentStream.addRect(tableStartX + col * columnWidth, tableStartY, columnWidth, rowHeight)
                contentStream.stroke()
                contentStream.beginText()
                contentStream.newLineAtOffset(tableStartX + 5 + col * columnWidth, tableStartY + 8f)
                contentStream.showText(headers[col])
                contentStream.endText()
            }

            val bodyStartY = tableStartY - rowHeight
            val tableData = arrayOf(
                arrayOf(comedianName, jobType, "R$amountCharged", jobDuration),
                arrayOf(" ", "TOTAL", " ", "R$amountCharged")
            )
            contentStream.setFont(fontRegular, 11f)
            for (row in 0 until numRows) {
                for (col in 0 until numCols) {
                    val x = tableStartX + col * columnWidth
                    val y = bodyStartY - row * rowHeight
                    contentStream.addRect(x, y, columnWidth, rowHeight)
                    contentStream.stroke()
                    contentStream.beginText()
                    contentStream.newLineAtOffset(x + 5f, y + 8f)
                    contentStream.showText(tableData[row][col])
                    contentStream.endText()
                }
            }

            // Banking Details (Heading Underlined)
            contentStream.beginText()
            contentStream.setFont(fontBold, 13f)
            contentStream.newLineAtOffset(margin, tableStartY - 90f)
            contentStream.showText("Banking Details are as follows:")
            contentStream.endText()
            val bw = fontBold.getStringWidth("Banking Details are as follows:") / 1000 * 13f
            contentStream.moveTo(margin, tableStartY - 95f)
            contentStream.lineTo(margin + bw, tableStartY - 95f)
            contentStream.setLineWidth(0.5f)
            contentStream.stroke()

            // Bank Details Body
            contentStream.beginText()
            contentStream.setFont(fontBold, 11f)
            contentStream.newLineAtOffset(margin, tableStartY - 110f)
            contentStream.showText(comedianBankName)
            contentStream.newLineAtOffset(0f, -20f)
            contentStream.showText(comedianAccountNumber)
            contentStream.newLineAtOffset(0f, -20f)
            contentStream.showText(comedianAccountType)
            contentStream.newLineAtOffset(0f, -20f)
            contentStream.showText(comedianNameOnAccount)
            contentStream.endText()

            // Footer Contact
            contentStream.beginText()
            contentStream.setFont(fontBold, 11f)
            contentStream.newLineAtOffset(margin, 30f)
            contentStream.showText("C: $comedianPhoneNumber")
            contentStream.endText()

            contentStream.close()

            // Password and protection
            val password = fileName.extractDateTimeFromFilename()

            val perm = AccessPermission()
            perm.setCanModify(false)
            perm.setCanPrint(true)
            perm.setCanAssembleDocument(false)
            perm.setCanExtractContent(false)

            val policy = StandardProtectionPolicy(password, null, perm).apply {
                encryptionKeyLength = 128
            }
            document.protect(policy)

            // Saving the file with MediaStore (Android 10+)
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/Records")
            }


            val uri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

            uri?.let {
                context.contentResolver.openOutputStream(it).use { os ->
                    document.save(os)
                }

                withContext(Dispatchers.Main) {
                    if (historyRecord == null && context is Activity) {
                        MaterialAlertDialogBuilder(context)
                            .setTitle("Invoice Created!")
                            .setMessage("PDF saved successfully in: Phone Storage/Documents/Records/")
                            .setNegativeButton("Okay", null)
                            .show()
                    }
                }

                // Pass the URI to the sharePdf function
                if (historyRecord == null) {
                    sharePdf(context, it)
                }
            }
            document.close()
            return@withContext true
        } catch (ex: Exception) {
            Log.e("PDFCreation", "Exception occurred: ${ex.message}", ex)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Oops, something went wrong: ${ex.message}", Toast.LENGTH_LONG).show()
            }
            return@withContext false
        }
    }


    fun sharePdf(context: Context, pdfUri: Uri) {
        try {
            Log.d("SharePDF", "Starting sharePdf for file URI: $pdfUri")

            // Intent to share
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, pdfUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grant URI permission
                clipData = ClipData.newRawUri("PDF", pdfUri) // Attach the URI to the intent
            }

            Log.d("SharePDF", "Intent created with EXTRA_STREAM and ClipData set.")

            val chooser = Intent.createChooser(intent, "Share PDF via").apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            Log.d("SharePDF", "Launching share chooser.")
            context.startActivity(chooser)

        } catch (e: Exception) {
            Log.e("SharePDF", "Error during sharePdf: ${e.message}", e)
        }
    }


    fun String.extractDateTimeFromFilename(): String {
        return if (this.lowercase().endsWith(".pdf")) {
            this.dropLast(4).substringAfter(" - ")
        } else {
            this.substringAfter(" - ")
        }
    }


    fun String.sliceAndCapitalize(): String {
        return this.take(3).uppercase()
    }





}
