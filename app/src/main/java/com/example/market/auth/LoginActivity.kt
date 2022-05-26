package com.example.market.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.ScriptGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.market.MainActivity
import com.example.market.R
import com.example.market.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        auth = Firebase.auth

        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        // 로그인 함수
        binding.loginBtn.setOnClickListener{

            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(this,"이메일과 비밀번호를 입력해주세요!", Toast.LENGTH_SHORT).show()
            }else{
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            if(auth.currentUser?.isEmailVerified!!){
                                val intent = Intent(this, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                startActivity(intent)

                                Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                            } else{
                                auth.currentUser?.sendEmailVerification()
                                Toast.makeText(this, "인증 이메일이 발송되었습니다\n확인해주세요!", Toast.LENGTH_SHORT).show()
                            }

                        } else {
                            Toast.makeText(this, "회원가입을 진행해주세요", Toast.LENGTH_SHORT).show()
                        }

                    }
            }

        }
        binding.findPassword.setOnClickListener{
            val intent = Intent(this, FindPwActivity::class.java)
            startActivity(intent)
        }

        // 회원가입창으로 이동
        binding.joinBtn.setOnClickListener {
            val intent = Intent(this, JoinActivity::class.java)
            startActivity(intent)
        }

    }
}