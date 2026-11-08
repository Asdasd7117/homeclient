package com.example.homeclient

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var serverIp by remember { mutableStateOf("") }
            var serverResponse by remember { mutableStateOf("حالة الاتصال: بانتظار الإتصال...") }
            var isLoading by remember { mutableStateOf(false) }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تحكم العميل الخارجي",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = serverIp,
                        onValueChange = { serverIp = it },
                        label = { Text("أدخل آي بي الخادم المنزلي (IP)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (serverIp.isBlank()) {
                                Toast.makeText(this@MainActivity, "الرجاء إدخال عنوان الآي بي الخاص بالمنزل", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isLoading = true
                            serverResponse = "جاري الاتصال بالخادم..."

                            // إرسال الطلب للخادم عبر خيط خلفي (IO Thread)
                            GlobalScope.launch(Dispatchers.IO) {
                                try {
                                    // الاتصال على المنفذ 8080 المفتوح في الخادم المنزلي
                                    val socket = Socket(serverIp.trim(), 8080)
                                    val writer = PrintWriter(socket.getOutputStream(), true)
                                    val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                                    writer.println("GET / HTTP/1.1\r\n")
                                    val response = reader.readLine() ?: "استجابة فارغة"

                                    socket.close()

                                    withContext(Dispatchers.Main) {
                                        serverResponse = "نجح الاتصال! الرد: $response"
                                        isLoading = false
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        serverResponse = "فشل الاتصال: ${e.localizedMessage}"
                                        isLoading = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Text(text = if (isLoading) "جاري الاتصال..." else "اتصال بالخادم المنزلي")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = serverResponse,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
