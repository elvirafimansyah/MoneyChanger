package com.example.moneychanger

import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.moneychanger.ui.theme.MoneyChangerTheme
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter

class ViewOrderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoneyChangerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var da by remember { mutableStateOf<Currency?>(null) }
                    var orders = remember { mutableStateListOf<Order>() }

                    val scope = rememberCoroutineScope()
                    val ctx = LocalContext.current

                    LaunchedEffect(Unit) {
                        scope.launch {
                            orders.clear()
                            orders.addAll(HttpClient.getOrders())

                        }
                    }
                    Column(modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(Color.White)
                        .padding(15.dp)
                    ) {
                        Text(
                            "Money Changer",
                            fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                        Spacer(Modifier.height(15.dp))

                        Text("Back",
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                            color = Color.Black,
                            modifier = Modifier
                                .clickable {
                                    finish()
                                }
                        )
                        Spacer(Modifier.height(15.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                            items(orders) { item ->
                                Column(modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .border(1.dp, Color.Black, RoundedCornerShape(12.dp))
                                    .padding(15.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "Code: ",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                            color = Color.Black,
                                            modifier = Modifier
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(item.code,
                                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                            color = Color.Black,
                                            modifier = Modifier
                                        )
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "From: ",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                            color = Color.Black,
                                            modifier = Modifier
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(item.originCurrency,
                                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                            color = Color.Black,
                                            modifier = Modifier
                                        )
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "To: ",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                            color = Color.Black,
                                            modifier = Modifier
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(item.targetCurrency,
                                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                            color = Color.Black,
                                            modifier = Modifier
                                        )
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "Amount: ",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                            color = Color.Black,
                                            modifier = Modifier
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(item.originNominal.toString(),
                                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                            color = Color.Black,
                                            modifier = Modifier
                                        )
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "Rate: ",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                            color = Color.Black,
                                            modifier = Modifier
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(item.rate.toString(),
                                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                            color = Color.Black,
                                            modifier = Modifier
                                        )
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "Total: ",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                            color = Color.Black,
                                            modifier = Modifier
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(item.targetNominal.toString(),
                                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                            color = Color.Black,
                                            modifier = Modifier
                                        )
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "Date: ",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                            color = Color.Black,
                                            modifier = Modifier
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(item.date.format(DateTimeFormatter.ofPattern(
                                            "dd-MM-yyyy")),
                                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                            color = Color.Black,
                                            modifier = Modifier
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
}

