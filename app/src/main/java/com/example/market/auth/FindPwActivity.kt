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

    private lateinit var binding : ActivityFindPwBinding

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        auth = Firebase.auth
        val email = binding.email.text.toString()
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_find_pw)
        binding.sendBtn.setOnClickListener {
            if(email.isEmpty()){
                Toast.makeText(this,"이메일을 입력해주세요",Toast.LENGTH_SHORT).show()
            }else{
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "Email sent.")
                            Toast.makeText(this,"비밀번호 재설정 메일이 전송되었습니다.",Toast.LENGTH_SHORT).show()
                        }
                    }
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }

        }
    }
}