package com.pranavkd.bustracker

import android.util.Log
import com.google.android.gms.maps.model.LatLng
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
            put("busId", busId)
        }.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("http://207.211.188.157:4578/api/getBusRoutes")
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
                    Log.d("Managers", "Response: $res")
                    //[{10.3012,76.3334},{10.3013,76.3335},{10.3015,76.3336}]
                    var routesArray = res.split("},{")
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
            .url("ws://207.211.188.157:4578/busLocation?busId=$busId")
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
}