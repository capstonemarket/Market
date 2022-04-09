package com.example.market.auth

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.market.MainActivity
import com.example.market.R
import com.example.market.databinding.ActivityJoinBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class JoinActivity : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth

    private lateinit var binding : ActivityJoinBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_join)

        binding.joinBtn.setOnClickListener {

            auth = Firebase.auth

            // 가입 조건 확인 변수 (양식에 맞지 않으면 false)
            var isGoToJoin = true

            // 이메일, 비밀번호, 비밀번호 확인 databinding
            val email = binding.email.text.toString()
            val password1 = binding.password.text.toString()
            val password2 = binding.passwordConfirm.text.toString()

            //회원 가입 정보 오류 확인 부분

            if(email.isEmpty()){
                Toast.makeText(this,"이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
                isGoToJoin = false
            }

            if(password1.isEmpty()){
                Toast.makeText(this,"비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                isGoToJoin = false
            }

            if(password2.isEmpty()){
                Toast.makeText(this,"비밀번호를 확인해주세요", Toast.LENGTH_SHORT).show()
                isGoToJoin = false
            }
            if(!password1.equals(password2)){
                Toast.makeText(this,"비밀번호를 똑같이 입력해주세요", Toast.LENGTH_SHORT).show()
                isGoToJoin = false
            }

            if(password1.length<6){
                Toast.makeText(this,"비밀번호를 6자리 이상으로 입력해주세요", Toast.LENGTH_SHORT).show()
                isGoToJoin = false
            }

            // 회원 가입 부분
            if(isGoToJoin){
                auth.createUserWithEmailAndPassword(email, password1).addOnCompleteListener(this) { task ->

                    if (task.isSuccessful) {
                        Toast.makeText(this,"회원가입 성공", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                        Log.d(ContentValues.TAG, "createUserWithEmail:success")

                    } else {
                        Toast.makeText(this,"회원가입 실패", Toast.LENGTH_SHORT).show()
                    }

                }
            }

        }
    }
}