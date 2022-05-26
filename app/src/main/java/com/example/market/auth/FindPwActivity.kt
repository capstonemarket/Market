package com.example.market.auth

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.market.R
import com.example.market.databinding.ActivityFindPwBinding
import com.example.market.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class FindPwActivity : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth
    private lateinit var binding :ActivityFindPwBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_find_pw)
        auth = FirebaseAuth.getInstance()

        binding.sendBtn.setOnClickListener {
            val email = binding.email.text.toString()
            if(email.isEmpty()){
                Toast.makeText(this,"이메일을 입력해주세요!",Toast.LENGTH_SHORT).show()
            }else{
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this,"이메일이 전송되었습니다\n확인해주세요!",Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            // successful!
                        } else {
                            Toast.makeText(this,"실패",Toast.LENGTH_SHORT).show()
                            // failed!
                        }
                    }
            }

        }
    }
}