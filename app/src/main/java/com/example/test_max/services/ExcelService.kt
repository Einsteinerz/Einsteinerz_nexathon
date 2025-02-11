package com.example.test_max.services

import android.content.Context
import android.net.Uri
import com.example.test_max.data.ExcelData
import com.example.test_max.data.ExcelResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.IOException

class ExcelService {
    suspend fun readExcelFile(context: Context, uri: Uri): ExcelResult = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val workbook = WorkbookFactory.create(inputStream)
                val sheet = workbook.getSheetAt(0)
                
                val headers = mutableListOf<String>()
                val rows = mutableListOf<List<String>>()
                
                // Read headers
                sheet.getRow(0)?.let { headerRow ->
                    for (cell in headerRow) {
                        headers.add(cell?.toString() ?: "")
                    }
                }
                
                // Read data rows
                for (rowIndex in 1..sheet.lastRowNum) {
                    val row = sheet.getRow(rowIndex)
                    if (row != null) {
                        val rowData = mutableListOf<String>()
                        for (cellIndex in headers.indices) {
                            val cell = row.getCell(cellIndex)
                            rowData.add(cell?.toString() ?: "")
                        }
                        rows.add(rowData)
                    }
                }
                
                ExcelResult.Success(
                    ExcelData(
                        headers = headers,
                        rows = rows,
                        sheetName = sheet.sheetName
                    )
                )
            } ?: ExcelResult.Error("Could not open file stream")
        } catch (e: IOException) {
            ExcelResult.Error("Error reading Excel file: ${e.message}")
        } catch (e: Exception) {
            ExcelResult.Error("Unexpected error: ${e.message}")
        }
    }
} 