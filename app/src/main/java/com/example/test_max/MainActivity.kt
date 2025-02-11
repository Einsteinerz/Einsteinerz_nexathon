package com.example.test_max

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.test_max.data.ExcelData
import com.example.test_max.data.ExcelResult
import com.example.test_max.services.ExcelService
import com.example.test_max.services.OpenAIService
import com.example.test_max.ui.theme.Test_maxTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted
import android.Manifest
import android.os.Build
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val excelService = ExcelService()
    private val openAIService = OpenAIService(BuildConfig.OPENAI_API_KEY)

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Test_maxTheme {
                var excelData by remember { mutableStateOf<ExcelData?>(null) }
                var query by remember { mutableStateOf("") }
                var response by remember { mutableStateOf("") }
                var isLoading by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()

                val permissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    rememberPermissionState(Manifest.permission.READ_MEDIA_IMAGES)
                } else {
                    rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE)
                }

                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let {
                        scope.launch {
                            isLoading = true
                            when (val result = excelService.readExcelFile(this@MainActivity, it)) {
                                is ExcelResult.Success -> {
                                    excelData = result.data
                                    response = "" // Clear previous response
                                }
                                is ExcelResult.Error -> response = result.message
                            }
                            isLoading = false
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                if (permissionState.status.isGranted) {
                                    launcher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                                } else {
                                    permissionState.launchPermissionRequest()
                                }
                            }
                        ) {
                            Text("Select Excel File")
                        }

                        if (excelData != null) {
                            OutlinedTextField(
                                value = query,
                                onValueChange = { query = it },
                                label = { Text("Enter your query") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3
                            )

                            Button(
                                onClick = {
                                    scope.launch {
                                        isLoading = true
                                        response = openAIService.analyzeData(
                                            query,
                                            "Headers: ${excelData?.headers?.joinToString()}\n" +
                                                    "Sample data: ${excelData?.rows?.take(5)}"
                                        )
                                        isLoading = false
                                    }
                                },
                                enabled = query.isNotBlank() && !isLoading
                            ) {
                                Text("Analyze")
                            }

                            if (isLoading) {
                                CircularProgressIndicator()
                            }

                            if (response.isNotEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = response,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}