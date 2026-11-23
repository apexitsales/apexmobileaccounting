package com.apexit.accounting

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object DocumentPdf {
    fun create(context: Context, company: CompanyProfile, document: Document, customer: Customer): Uri {
        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas

        val accent = try { Color.parseColor(company.primaryColor) } catch (_: Exception) { Color.rgb(215, 25, 32) }
        val title = Paint().apply { color = accent; textSize = 24f; isFakeBoldText = true }
        val normal = Paint().apply { color = Color.BLACK; textSize = 12f }
        val small = Paint().apply { color = Color.DKGRAY; textSize = 10f }

        canvas.drawText(company.name, 40f, 50f, title)
        canvas.drawText(company.address, 40f, 72f, small)
        canvas.drawText("${document.type}: ${document.number}", 380f, 50f, title)
        canvas.drawText("Date: ${document.date}", 380f, 72f, small)
        canvas.drawText("Bill to:", 40f, 120f, normal)
        canvas.drawText(customer.name, 40f, 142f, normal)
        canvas.drawText(customer.address, 40f, 160f, small)
        canvas.drawText(customer.email, 40f, 178f, small)

        var y = 230f
        canvas.drawLine(40f, y - 14f, 555f, y - 14f, normal)
        canvas.drawText("Description", 40f, y, normal)
        canvas.drawText("Qty", 390f, y, normal)
        canvas.drawText("Amount", 480f, y, normal)
        y += 32f

        canvas.drawText(document.notes.ifBlank { "Accounting transaction" }, 40f, y, small)
        canvas.drawText("1", 390f, y, small)
        canvas.drawText("%.2f".format(document.total), 480f, y, small)
        y += 50f
        canvas.drawLine(380f, y, 555f, y, normal)
        canvas.drawText("Subtotal", 380f, y + 28f, small)
        canvas.drawText("%.2f".format(document.subtotal), 480f, y + 28f, small)
        canvas.drawText("Tax", 380f, y + 50f, small)
        canvas.drawText("%.2f".format(document.tax), 480f, y + 50f, small)
        canvas.drawText("Total", 380f, y + 78f, title)
        canvas.drawText("%.2f".format(document.total), 480f, y + 78f, title)

        pdf.finishPage(page)
        val file = File(context.cacheDir, "${document.number}.pdf")
        pdf.writeTo(FileOutputStream(file))
        pdf.close()
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun share(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Email or share PDF"))
    }
}
