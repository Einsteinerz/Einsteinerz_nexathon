package com.example.test_max

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.test_max.data.ExcelResult
import com.example.test_max.services.ExcelService
import com.example.test_max.services.OpenAIService
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val excelService = ExcelService()
    private val openAIService = OpenAIService(BuildConfig.OPENAI_API_KEY)

    private lateinit var btnSelectFile: Button
    private lateinit var queryInputLayout: TextInputLayout
    private lateinit var queryInput: TextInputEditText
    private lateinit var btnAnalyze: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var responseCard: MaterialCardView
    private lateinit var responseText: TextView

    private var currentExcelData: ExcelResult.Success? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchFilePicker()
        }
    }

    private val getContent = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { handleFileSelection(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        btnSelectFile = findViewById(R.id.btnSelectFile)
        queryInputLayout = findViewById(R.id.queryInputLayout)
        queryInput = findViewById(R.id.queryInput)
        btnAnalyze = findViewById(R.id.btnAnalyze)
        progressBar = findViewById(R.id.progressBar)
        responseCard = findViewById(R.id.responseCard)
        responseText = findViewById(R.id.responseText)
    }

    private fun setupClickListeners() {
        btnSelectFile.setOnClickListener {
            checkPermissionAndPickFile()
        }

        btnAnalyze.setOnClickListener {
            val query = queryInput.text?.toString()
            if (!query.isNullOrBlank()) {
                analyzeData(query)
            }
        }

        queryInput.setOnTextChangedListener { text ->
            btnAnalyze.isEnabled = !text.isNullOrBlank()
        }
    }

    private fun checkPermissionAndPickFile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun launchFilePicker() {
        getContent.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    }

    private fun handleFileSelection(uri: Uri) {
        showLoading(true)
        lifecycleScope.launch {
            when (val result = excelService.readExcelFile(this@MainActivity, uri)) {
                is ExcelResult.Success -> {
                    currentExcelData = result
                    showQueryInput()
                    clearResponse()
                }
                is ExcelResult.Error -> {
                    showError(result.message)
                }
            }
            showLoading(false)
        }
    }

    private fun analyzeData(query: String) {
        showLoading(true)
        lifecycleScope.launch {
            val data = currentExcelData?.data
            if (data != null) {
                val response = openAIService.analyzeData(
                    query,
                    "Headers: ${data.headers.joinToString()}\n" +
                            "Sample data: ${data.rows.take(5)}"
                )
                showResponse(response)
            }
            showLoading(false)
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnAnalyze.isEnabled = !show
        btnSelectFile.isEnabled = !show
    }

    private fun showQueryInput() {
        queryInputLayout.visibility = View.VISIBLE
        btnAnalyze.visibility = View.VISIBLE
    }

    private fun showResponse(response: String) {
        responseCard.visibility = View.VISIBLE
        responseText.text = response
    }

    private fun showError(message: String) {
        responseCard.visibility = View.VISIBLE
        responseText.text = "Error: $message"
    }

    private fun clearResponse() {
        responseCard.visibility = View.GONE
        responseText.text = ""
    }
}

private fun TextInputEditText.setOnTextChangedListener(listener: (String?) -> Unit) {
    addTextChangedListener(object : android.text.TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            listener(s?.toString())
        }
        override fun afterTextChanged(s: android.text.Editable?) {}
    })
}