package com.pranavkd.bustracker

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.pranavkd.bustracker.Types.BookingHome
import com.pranavkd.bustracker.Types.Routes
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONObject
import java.io.IOException

class Managers {
    private val client = OkHttpClient()
    fun getTravelRoute(busId:String, callback: (List<LatLng>)->Unit) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = JSONObject().apply {
            put("bookingId", busId)
        }.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://bus-tracker-backend-one.vercel.app/api/client/getBusRoutes")
            .post(body)
            .build()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Managers", "Failed to get route", e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if(!response.isSuccessful) throw IOException("Unexpected code $response")
                    val res = response.body!!.string()
                    Log.d("Managers", "Response: ${res.replace("\"", "").replace("\n", "")}")
                    //[{10.3012,76.3334},{10.3013,76.3335},{10.3015,76.3336}]
                    var routesArray = res.replace("\"", "").replace("\\n", "").split("},{")
                    var routes : List<LatLng> = listOf()
                    for(route in routesArray){
                        val latlng = route.replace("[{","").replace("}]","").replace("{","").replace("}","").split(",")
                        val lat = latlng[0].toDouble()
                        val lng = latlng[1].toDouble()
                        routes += LatLng(lat,lng)
                    }
                    callback(routes)
                }
            }

        })

    }
    fun sendBusLocationWs(busId: String,callback: (lotlog:LatLng) -> Unit) {
        //websoket ws://207.211.188.157:4578/busLocation
        val request = Request.Builder()
            .url("ws://207.211.188.157:4578/busLocation?bookId=$busId")
            .build()
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("Managers", "WebSocket Opened")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                //{"busId":"123","location":"10.3086,76.3328"}
                val json = JSONObject(text)
                val location = json.getString("location").split(",")
                callback(LatLng(location[0].toDouble(),location[1].toDouble()))
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("Managers", "WebSocket Failure", t)
            }
        }

        client.newWebSocket(request, listener)
        client.dispatcher.executorService.shutdown()
    }
    fun getBookingDetails(
        bookingId:String,
        onComplete : (BookingData:BookingHome) -> Unit,
        onFailure : (e:Exception) -> Unit

    ){
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = JSONObject().apply {
            put("bookingId", bookingId)
        }.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://bus-tracker-backend-one.vercel.app/api/client/getBookingDetails")
            .post(body)
            .build()
        try {
            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onFailure(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")
                        val res = response.body!!.string()
                        Log.d("Managers", "Response: $res")
                        val json = JSONObject(res)
                        val routesJson = json.getJSONArray("routes")
                        val routesList = mutableListOf<Routes>()
                        for (i in 0 until routesJson.length()) {
                            val routeObj = routesJson.getJSONObject(i)
                            val routeName = routeObj.getString("name")
                            val routeCompleted = routeObj.getBoolean("completed")
                            routesList.add(Routes(routeName, routeCompleted))
                        }

                        val booking = BookingHome(
                            json.getString("bookingId"),
                            json.getString("fullname"),
                            json.getString("email"),
                            json.getString("phone"),
                            json.getString("gender"),
                            json.getString("busId"),
                            json.getString("source"),
                            json.getString("destination"),
                            json.getString("conductor"),
                            json.getString("timeD"),
                            json.getString("timeA"),
                            json.getString("status"),
                            routesList
                        )
                        onComplete(booking)
                    }
                }
            });
        }catch (e:Exception){
            onFailure(e)
        }
    }
}