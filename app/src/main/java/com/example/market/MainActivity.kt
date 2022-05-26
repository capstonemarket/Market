package com.example.market

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.market.auth.Token
import com.example.market.auth.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    //요기 아래로 추가
        val token = FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
        })

        lateinit var database: DatabaseReference
        database = Firebase.database.reference
        val auth = Firebase.auth.currentUser
        val u_id = auth?.uid.toString()
        val u_email = auth?.email.toString()
        val u_token = token.result
        database.child("token").child(u_id).setValue(Token(u_id,u_token))

        val user = User(u_email.split("@")[0],null,u_id,u_email)
        database.child("users").child(u_id).setValue(user)
    }
}