package com.example.market

import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.json.JSONObject
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

fun sendPostToFCM(
    id: String/*보내려는 아이디*/,
    title:String/*메시지 제목*/,
    message: String/*메시지 내용*/,
    board:String/*제품 id*/
)
{
    val FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send"
    val SERVER_KEY = "AAAAy9Ds5LY:APA91bHzbFThfXwYjj06RhI_4LTi731te0n9JXA3b5aepXHvq4q7q6BV5KeVjBRwS6Se2WgBS_4xCeIASc6mii5O-YrmgSh7vys9o1FSuvc805RU-6NgIQcKxs8jvq78zzCWZtUq4iZS"

    val DataBase = Firebase.database
    val myRef = DataBase.getReference("token")

    myRef.child(id).child("token")//메시지를 보낼 토큰
        .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userData = dataSnapshot.getValue(String::class.java)
                Log.w("메시지 수신 uid",id)
                Log.w("메시지 수신 토큰",userData.toString())
                Thread {
                    try {
                        val root = JSONObject()
                        val notification = JSONObject()
                        notification.put("body", message)
                        notification.put("title", title)//메시지 제목
                        root.put("notification", notification)
                        root.put("to", userData)

                        val url = URL(FCM_MESSAGE_URL)
                        val conn: HttpURLConnection = url.openConnection() as HttpURLConnection
                        conn.setRequestMethod("POST")
                        conn.setDoOutput(true)
                        conn.setDoInput(true)
                        conn.addRequestProperty("Authorization", "key=$SERVER_KEY")
                        conn.setRequestProperty("Accept", "application/json")
                        conn.setRequestProperty("Content-type", "application/json")
                        val os: OutputStream = conn.getOutputStream()
                        os.write(root.toString().toByteArray(charset("utf-8")))
                        os.flush()
                        conn.getResponseCode()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }.start()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("메시지를 보내는데 실패했습니다.", "메시지를 보내는데 실패했습니다.")
            }
        })
}