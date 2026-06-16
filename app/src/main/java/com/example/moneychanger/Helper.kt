package com.example.moneychanger

import android.util.Log
import androidx.compose.ui.text.platform.URLSpanCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime

data class HttpReq (
    val url : String,
    val body : String = "",
    val method : String = "GET",
    val headers : Map<String, String> = emptyMap(),
    val timeout : Int= 10000
)
data class HttpRes (
    val code : Int,
    val body : String? = null,
    val headers : Map<String, List<String>> = emptyMap(),
    val errors : String?= null
)
data class Currency (
    val id : Int,
    val country : String,
    val name : String,
    val abbreviation : String,
)
data class USDRate (
    val id : Int,
    val currencyId : Int,
    val rate : Double,
)
data class ExchangeRate (
    val convertsationRate : Double,
    val realRate : Double,
    val nominalResult : Double,
)

data class Order (
    val id : Int,
    val code : String,
    val originCurrency : String,
    val targetCurrency : String,
    val rate : Double,
    val originNominal : Double,
    val targetNominal : Double,
    val date : LocalDateTime,
)

object HttpClient {
    val baseURL = "http://10.0.2.2:5000/api/"

    fun send(req : HttpReq) : HttpRes {
        val conn = URL(req.url).openConnection() as HttpURLConnection
        return try {
            conn.requestMethod= req.method
            conn.readTimeout= req.timeout
            conn.connectTimeout= req.timeout
            req.headers.forEach { (k,v) -> conn.setRequestProperty(k,v) }
            conn.connect()
            if(req.body.isNotEmpty() && req.method in listOf("POST", "PUT", "PATCH")) {
                conn.getOutputStream().buffered().use { it.write(req.body.toByteArray()) }
            }

            val code = conn.responseCode
            val body = if(code in 200 until 300) {
                conn.getInputStream().bufferedReader()?.use { it.readText() }
            } else {
                conn.errorStream.bufferedReader()?.use { it.readText() }
            }

            HttpRes(code, body, conn.headerFields)
        } catch (e: Exception) {
            HttpRes(-1, e.message ?: "Network Error")
        } finally {
            conn.disconnect()
        }
    }

    suspend fun jsonReq(route : String, body : String = "", method : String = "GET") : HttpRes {
        val headers = mapOf(
            "Content-Type" to "application/json"
        )

        return withContext(Dispatchers.IO) {
            send(HttpReq(baseURL + route, body, method, headers))
        }
    }

    suspend fun getCurrencies() : List<Currency> {
        val res = jsonReq("currency")
        if(res.code != 200 && res.body.isNullOrEmpty()) return emptyList()
        val json = JSONArray(res.body)
        var arr = mutableListOf<Currency>()
        for(i in 0 until json.length()) {
            val obj = json.getJSONObject(i)
            arr.add(Currency(
                obj.getInt("id"),
                obj.getString("country"),
                obj.getString("name"),
                obj.getString("abbreviation"),
            ))
        }

        return arr
    }

    suspend fun getCurrencyById(id: Int) : Currency? {
        val res = jsonReq("currency/$id")
        if(res.code != 200 && res.body.isNullOrEmpty()) return null
        val obj = JSONObject(res.body)

        return Currency(
            obj.getInt("id"),
            obj.getString("country"),
            obj.getString("name"),
            obj.getString("abbreviation"),
        )
    }

    suspend fun getUSDRateById(id: Int) : USDRate? {
        val res = jsonReq("currency/usd-rate/$id")
        if(res.code != 200 && res.body.isNullOrEmpty()) return null
        val obj = JSONObject(res.body)

        return USDRate(
            obj.getInt("id"),
            obj.getInt("currencyId"),
            obj.getDouble("rate"),
        )
    }
    suspend fun getOrders() : List<Order> {
        val res = jsonReq("currency/orders")
        if(res.code != 200 && res.body.isNullOrEmpty()) return emptyList()
        val json = JSONArray(res.body)
        var arr = mutableListOf<Order>()
        for(i in 0 until json.length()) {
            val obj = json.getJSONObject(i)
            arr.add(Order(
                obj.getInt("id"),
                obj.getString("code"),
                obj.getString("originCurrency"),
                obj.getString("targetCurrency"),
                obj.getDouble("rate"),
                obj.getDouble("originNominal"),
                obj.getDouble("targetNominal"),
                LocalDateTime.parse(obj.getString("date")),
            ))
        }

        return arr
    }

    suspend fun getExchangeRate(originCurrencyId : Int, targetCurrencyId : Int, amount : Double) : ExchangeRate? {
        val res = jsonReq("currency/exchange-rate/${originCurrencyId}/${targetCurrencyId}/${amount}")
        if(res.code != 200 && res.body.isNullOrEmpty()) return null
        val obj = JSONObject(res.body)

        return ExchangeRate(
            obj.getDouble("convertsationRate"),
            obj.getDouble("realRate"),
            obj.getDouble("nominalResult"),
        )
    }

    suspend fun postOrder(originCurrencyId : Int,targetCurrencyId : Int, convertsationRate : Double, originNominal : Double, targetNominal: Double) : String {
        val body = JSONObject().apply {
            put("originCurrencyId", originCurrencyId)
            put("targetCurrencyId", targetCurrencyId)
            put("convertsationRate", convertsationRate)
            put("originNominal", originNominal)
            put("targetNominal", targetNominal)
        }.toString()
        val res = jsonReq("currency/order", body, "POST")
        if(res.body.isNullOrEmpty()) return "Failed to order"
        Log.d("order", res.body)

        if(res.code == 200) {
            return "ok"
        }

        return try {
            res.body
        }catch (e: Exception) {
            "Failed to order"
        }
    }

}

