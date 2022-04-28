package com.example.market.board

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.market.R
import com.example.market.databinding.ActivityBoardBinding
import com.example.market.utils.FBRef
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.roundToInt
class BoardActivity : AppCompatActivity() {
    private lateinit var binding : ActivityBoardBinding
    private lateinit var key : String
    private lateinit var up : String
    private lateinit var postTime: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_board)
        //val price : Int


        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.menuBtn.setOnClickListener {
            showDialog()
        }

        key = intent.getStringExtra("key").toString()
        //price = intent.getIntExtra("currentP",0)
        //Log.d("dddddd",price.toString())

        getBoardData(key)
        getImageData(key)

        binding.upBtn.setOnClickListener{
            showUpDialog()
        }
        binding.chatBtn.setOnClickListener{

        }
    //    updateTimer(postTime) ///////////////////////////run 위해 임의 주석처
    }

    private fun showDialog() {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.menu_dialog, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)

        val alertDialog = mBuilder.show()

        alertDialog.findViewById<Button>(R.id.editBtn)?.setOnClickListener{
            val intent = Intent(this, BoardEditActivity::class.java)
            intent.putExtra("key", key)
            startActivity(intent)
        }

        alertDialog.findViewById<Button>(R.id.removeBtn)?.setOnClickListener{
            showDeleteDialog()
        }

    }

    private fun showDeleteDialog() {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.delete_dialog, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)

        val alertDialog = mBuilder.show()

        alertDialog.findViewById<Button>(R.id.yesBtn)?.setOnClickListener{
            FBRef.boardRef.child(key).removeValue()
        }

        alertDialog.findViewById<Button>(R.id.noBtn)?.setOnClickListener{

        }
    }

    private fun showUpDialog() {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.up_dialog, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)

        val alertDialog = mBuilder.show()

        alertDialog.findViewById<Button>(R.id.upBtn)?.setOnClickListener{
            val upT = alertDialog.findViewById<EditText>(R.id.currentV)?.text.toString()
            var isBlank = upT.isNullOrBlank()

            if(!isBlank){
                up = (up.toInt() + 1).toString()
                FBRef.boardRef
                    .child(key)
                    .child("up")
                    .setValue(up)
                binding.upCount.text = up

                val upV = upT.toInt()
                val newV = (binding.currentV.text.toString().toInt() + upV).toString()
                FBRef.boardRef
                    .child(key)
                    .child("currentV")
                    .setValue(newV)
                binding.currentV.text = newV
            } else {
                android.app.AlertDialog.Builder(this)
                    .setMessage("up할 값을 입력해주세요")
                    .setPositiveButton("확인", { dialogInterface: DialogInterface?, i:Int->})
                    .show()
            }

        }

    }

    private fun getImageData(key: String) {
        val storageReference = Firebase.storage.reference.child("board").child(key+".png")

        val imageViewFromFB = binding.img

        storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener{ task ->
            if(task.isSuccessful){
                Glide.with(this).load(task.result).into(imageViewFromFB)
            }else{
                binding.img.isVisible=false
            }
        })
    }

    // 클래스가 이동이 안됌, String만
    private fun getBoardData(key:String){

        //binding.title.text = key
        //binding.currentV.text = price.toString()

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    val dataModel = dataSnapshot.getValue(BoardModel::class.java)
                    binding.title.text = dataModel!!.title
                    binding.content.text = dataModel!!.content
                    binding.currentV.text = dataModel!!.currentV
                    binding.upCount.text = dataModel!!.up
                    binding.maxValue.text = dataModel!!.maxValue
                    postTime = dataModel!!.time
                    val myUid = Firebase.auth.uid.toString()
                    val writerUid = dataModel.uid
                    up = dataModel!!.up

                    if(myUid.equals(writerUid)){
                        binding.menuBtn.isVisible = true
                    }else{

                    }
                } catch (e : Exception){

                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("BoardActivity", "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.boardRef.child(key).addValueEventListener(postListener)
    }

    private fun updateTimer(postTime : String){
        val currentTime = Calendar.getInstance().time
        val convertTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss") .parse(postTime)
        val compareTime = (currentTime.time - convertTime.time)
        val limitTime = 1000*60*60 - compareTime //타이머 기준시간 1시간
        val countDown = object : CountDownTimer(limitTime, 1000) {
            override fun onTick(p0: Long) {
                val totalSecond = (p0.toFloat() / 1000.0f).roundToInt()
                val minute = totalSecond/60
                val second = totalSecond-minute*60
                binding.lTime.text = "${minute}:${second}"
            }
            override fun onFinish() {
                // 타이머가 종료되면 호출 (이미지 위 거래 종료 표시, time up chat 찜 버튼 invisible)
                binding.end.isVisible = true
                binding.lTime.isVisible = false
                binding.bottomBar.isVisible = false
            }
        }.start()
    }
}