package com.example.market.board

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.market.R
import com.example.market.databinding.ActivityBoardEditBinding
import com.example.market.utils.FBRef
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class BoardEditActivity : AppCompatActivity() {
    private lateinit var key : String
    private lateinit var binding : ActivityBoardEditBinding
    private lateinit var category: String
    private lateinit var up: String
    private lateinit var currentV: String
    private var isImageUpload = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_board_edit)

        key = intent.getStringExtra("key").toString()

        getBoardData(key)
        getImageData(key)

        binding.editBtn.setOnClickListener{
            editBoardData(key)
        }
    }

    private fun editBoardData(key: String){
        val title = binding.title.text.toString()
        val maxValue = binding.maxValue.text.toString()
        val minValue = binding.minValue.text.toString()
        val content = binding.content.text.toString()
        val uId = Firebase.auth.uid.toString()
        val time = getTime()
        var isBlank = title.isNullOrBlank()||maxValue.isNullOrBlank()
                ||minValue.isNullOrBlank() ||content.isNullOrBlank()

        var isLow = maxValue.toInt() < minValue.toInt()

        if(!isBlank&&!isLow) {
            FBRef.boardRef
                .child(key)
                .setValue(BoardModel(title, category, maxValue, minValue, content, currentV, uId, time, up))

            if (isImageUpload) {
                imageUpload(key)
            }
            finish()

        } else if(isLow) {
            AlertDialog.Builder(this)
                .setMessage("상한가보다 하한가를 낮게 입력하실 수 없습니다.")
                .setPositiveButton("확인", { dialogInterface: DialogInterface?, i:Int->})
                .show()
        }
        else {
            AlertDialog.Builder(this)
                .setMessage("모든 내용을 빈칸 없이 입력해주세요")
                .setPositiveButton("확인", { dialogInterface: DialogInterface?, i:Int->})
                .show()
        }

        binding.categorySelect.setOnClickListener {
            val items = arrayOf("전자제품", "책", "식품","옷", "화장품", "기타")
            val builder = AlertDialog.Builder(this)
                .setTitle("카테고리 선택")
                .setItems(items) { dialog, which ->
                    category=items[which]
                }
                .show()
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

    private fun getBoardData(key:String){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    val dataModel = dataSnapshot.getValue(BoardModel::class.java)
                    binding.title.setText(dataModel?.title)
                    binding.content.setText(dataModel?.content)
                    binding.maxValue.setText(dataModel?.max_value)
                    binding.minValue.setText(dataModel?.min_value)
                    currentV = dataModel!!.current_v
                    up = dataModel!!.up
                    category = dataModel!!.category
                } catch (e : Exception){

                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("BoardEditActivity", "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.boardRef.child(key).addValueEventListener(postListener)
    }

    private fun getTime() : String {
        val currentTime = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).format(currentTime)
        return dateFormat
    }

    private fun imageUpload(key:String) {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val mountainsRef = storageRef.child("board").child(key+".png")

        val imageView = binding.img
        imageView.isDrawingCacheEnabled = true
        imageView.buildDrawingCache()
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = mountainsRef.putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
        }
    }
}