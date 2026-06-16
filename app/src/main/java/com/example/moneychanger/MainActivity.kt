package com.example.moneychanger

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.moneychanger.ui.theme.MoneyChangerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoneyChangerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var amount by remember { mutableStateOf("") }
                    var errMsg by remember { mutableStateOf("") }
                    var expandedOrigin by remember { mutableStateOf(false) }
                    var expandedTarget by remember { mutableStateOf(false) }
                    var currencies = remember { mutableStateListOf<Currency>() }
                    var selectedOriginCurrency by remember { mutableStateOf<Currency?>(null) }
                    var selectedTargetCurrency by remember { mutableStateOf<Currency?>(null) }
                    var exchangeRate by remember { mutableStateOf<ExchangeRate?>(null) }
                    val scope = rememberCoroutineScope()
                    val ctx = LocalContext.current

                    LaunchedEffect(Unit) {
                        scope.launch {
                            currencies.clear()
                            currencies.addAll(HttpClient.getCurrencies())
                        }
                    }
                    fun loadConvert() {
                        scope.launch {
                            if(amount != "" && amount.toDouble() > 1) {
                                if(selectedOriginCurrency != null && selectedTargetCurrency != null) {
                                    if(selectedOriginCurrency != selectedTargetCurrency) {
                                        exchangeRate = HttpClient.getExchangeRate(selectedOriginCurrency!!.id, selectedTargetCurrency!!.id, amount.toDouble())
                                    }
                                }
                            }
                        }
                    }


                    LaunchedEffect(amount) {
                        loadConvert()
                    }
                    LaunchedEffect(selectedTargetCurrency, selectedOriginCurrency) {
                        loadConvert()
                    }

                    Column(modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(Color.White)
                        .padding(15.dp)
                    ) {
                        Text("Money Changer",
                            fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                        )

                        Spacer(Modifier.height(20.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Amount",
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                color = Color.Black,
                                modifier = Modifier
                            )
                            Spacer(Modifier.width(20.dp))
                            OutlinedTextField(
                                value = amount,
                                onValueChange = {
                                    amount = it.filter { it.isDigit() || it == '.' }
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                            )
                        }

                        Spacer(Modifier.height(15.dp))
                        Box() {
                            OutlinedButton(
                                onClick = {expandedOrigin = !expandedOrigin},
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Text(selectedOriginCurrency?.abbreviation ?: "Select Origin Currency")
                            }

                            DropdownMenu(expandedOrigin, {expandedOrigin = false}) {
                                currencies.forEachIndexed { index, currency ->
                                    DropdownMenuItem({Text(currency.abbreviation)}, {
                                        if(selectedOriginCurrency != null && currency.id == selectedTargetCurrency!!.id) {
                                            selectedTargetCurrency= selectedOriginCurrency
                                        }
                                        selectedOriginCurrency = currency
                                        expandedOrigin = false
                                    })
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Converted to",
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                color = Color.Black,
                                modifier = Modifier
                            )
                            Spacer(Modifier.width(20.dp))
                            OutlinedTextField(
                                value = if(exchangeRate != null) exchangeRate!!.nominalResult.toString() else "0",
                                onValueChange = {},
                                readOnly = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                            )
                        }

                        Spacer(Modifier.height(15.dp))
                        Box() {
                            OutlinedButton(
                                onClick = {expandedTarget = !expandedTarget},
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Text(selectedTargetCurrency?.abbreviation ?: "Select Target Currency")
                            }

                            DropdownMenu(expandedTarget, {expandedTarget = false}) {
                                currencies.forEachIndexed { index, currency ->
                                    DropdownMenuItem({Text(currency.abbreviation)}, {
                                        if(selectedTargetCurrency != null && currency.id == selectedOriginCurrency!!.id) {
                                            selectedOriginCurrency =selectedTargetCurrency
                                        }
                                        selectedTargetCurrency = currency
                                        expandedTarget = false
                                    })
                                }
                            }
                        }
                        Text(errMsg,
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                            color = Color.Red,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = {
                                if(amount.isBlank()) {
                                    errMsg = "Please fill the amount "
                                    return@Button
                                }
                                if(exchangeRate == null) {
                                    errMsg = "Converted nominal is empty"
                                    return@Button
                                }
                                if(selectedOriginCurrency == null) {
                                    errMsg = "Please select origin currency"
                                    return@Button
                                }
                                if(selectedTargetCurrency == null) {
                                    errMsg = "Please target currency"
                                    return@Button
                                }

                                scope.launch {
                                    when(val msg = HttpClient.postOrder(selectedOriginCurrency!!.id
                                        , selectedTargetCurrency!!.id, exchangeRate!!.convertsationRate, amount.toDouble(), exchangeRate!!.nominalResult)) {
                                        "ok" -> {
                                            Toast.makeText(ctx, "Successfully ordered!", Toast.LENGTH_SHORT).show()
                                            amount = ""
                                            selectedTargetCurrency = null
                                            selectedOriginCurrency = null
                                            exchangeRate = null
                                        }
                                        else -> {
                                            errMsg = msg
                                        }
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Text("Submit",
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            )
                        }


                        Spacer(Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val intent = Intent(ctx, ViewOrderActivity::class.java)
                                ctx.startActivity(intent)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Text("View Order History",
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

