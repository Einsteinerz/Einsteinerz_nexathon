package com.example.test_max.data

data class ExcelData(
    val headers: List<String>,
    val rows: List<List<String>>,
    val sheetName: String
)

sealed class ExcelResult {
    data class Success(val data: ExcelData) : ExcelResult()
    data class Error(val message: String) : ExcelResult()
} 