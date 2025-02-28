package com.pranavkd.bustracker.ChatLogic

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class ChatViewModel : ViewModel() {
    var lock = true
    private val client = OkHttpClient()
    private val _messageList = mutableStateListOf<Message>()
    val messageList: List<Message> get() = _messageList
    val apiUrl = "http://207.211.188.157:4578/api/admin/chat"
    val apiUrl2 = "http://207.211.188.157:4578/api/admin/getChat"

    fun sendMessage(text: String, bookingId: String) {
        val mediaType = "application/json; charset=utf-8".toMediaType()

//        {
//            "id": "chat1",
//            "senderType": "user",
//            "senderBookingId": "booking1",
//            "receiverType": "user",
//            "receiverConductorId": "conductor1",
//            "messageText": "Hello, user1",
//            "sentAt": "2025-02-26T10:00:00Z"
//        }
        val body = JSONObject().apply{
            put("id", "chat1")
            put("senderType", "user")
            put("senderBookingId", bookingId)
            put("receiverType", "user")
            put("receiverConductorId", "conductor1")
            put("messageText", text)
            put("sentAt", "2025-02-26T10:00:00Z")
        }.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(apiUrl)
            .post(body)
            .build()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ChatViewModel", "Failed to send message ${e.message}", e)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("ChatViewModel", "Message sent successfully")
                Log.d("ChatViewModel", "Calling receive message")
                lock = true
                receiveMessage(bookingId)
            }

        })
    }

    fun receiveMessage(bookingId: String) {
        if(lock){
            lock = false
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = JSONObject().apply {
                put("bookingId", bookingId)
            }.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url(apiUrl2)
                .post(body)
                .build()
            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("ChatViewModel", "Failed to send message ${e.message}", e)
                }

                override fun onResponse(call: Call, response: Response) {
                    //Log.d("ChatViewModel", response.body!!.string())
//                [
//                    {
//                        "messageText": "hi",
//                        "direction": "send",
//                        "sentAt": "2025-02-26T10:00:00Z"
//                    },
//                    {
//                        "messageText": "hi",
//                        "direction": "send",
//                        "sentAt": "2025-02-26T10:00:00Z"
//                    },
//                    {
//                        "messageText": "Hello, iam comming",
//                        "direction": "receive",
//                        "sentAt": "2025-02-26T10:00:00Z"
//                    }
//                ]
                    response.use {
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")
                        var res = response.body!!.string()
                        //Log.d("ChatViewModel", "Response: $res")
                        //clear the list
                        _messageList.clear()
                        val resJson = JSONArray(res)
                        Log.d("ChatViewModel", "Response: $resJson")
                        for(int in 0 until resJson.length()){
                            val message = resJson.getJSONObject(int)
                            _messageList.add(Message(message.getString("messageText"), message.getString("direction"), message.getString("sentAt")))
                        }
                    }
                }
            })
            lock = true
        }

    }

    fun clearMessages(){
        _messageList.clear()
    }



}