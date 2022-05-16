package com.example.market.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.market.R
import com.example.market.board.BoardModel
import com.example.market.databinding.FragmentWriteBinding
import com.example.market.utils.FBRef
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class WriteFragment : Fragment() {
    private lateinit var binding : FragmentWriteBinding
    private var category: String? = null
    private var auctionTime : String? = null
    private var isImageUpload = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_write, container, false)
        binding.image.clipToOutline = true
        binding.writeBtn.setOnClickListener{
            val title = binding.title.text.toString()
            val maxValue = binding.maxValue.text.toString()
            val minValue = binding.minValue.text.toString()
            val content = binding.content.text.toString()
            val currentV = binding.minValue.text.toString()
            val uid = Firebase.auth.uid.toString()
            val time = getTime()
            var isBlank = title.isNullOrBlank()||maxValue.isNullOrBlank()
                    ||minValue.isNullOrBlank() ||content.isNullOrBlank()
                    || category.isNullOrBlank() || auctionTime.isNullOrBlank()

            var isLow = false
            if(!isBlank) { isLow = maxValue?.toInt() < minValue?.toInt()}


            if(!isBlank && !isLow && isImageUpload) {

                val key = FBRef.boardRef.push().key.toString()

                FBRef.boardRef
                    .child(key)
                    .setValue(BoardModel(title, category!!, maxValue, minValue, content, currentV, uid, time, "0","", auctionTime!!))

                if (isImageUpload) {
                    imageUpload(key)
                }

                it.findNavController().navigate(R.id.action_writeFragment_to_homeFragment)
            } else if(isLow) {
                AlertDialog.Builder(context)
                    .setMessage("상한가보다 하한가를 낮게 입력하실 수 없습니다.")
                    .setPositiveButton("확인", {dialogInterface:DialogInterface?, i:Int->})
                    .show()
            } else if(!isImageUpload){
                AlertDialog.Builder(context)
                    .setMessage("상품 이미지를 첨부해주세요.")
                    .setPositiveButton("확인", {dialogInterface:DialogInterface?, i:Int->})
                    .show()
            } else {
                AlertDialog.Builder(context)
                    .setMessage("모든 내용을 빈칸 없이 입력해주세요")
                    .setPositiveButton("확인", {dialogInterface:DialogInterface?, i:Int->})
                    .show()
            }
        }

        binding.categorySelect.setOnClickListener {
            val items = arrayOf("전자제품", "책", "식품","옷", "화장품", "기타")
            val builder = AlertDialog.Builder(context)
                .setTitle("카테고리 선택")
                .setItems(items) { dialog, which ->
                    category=items[which]
                    binding.category.text = "카테고리 : $category"
                }
                .show()
        }

        binding.auctionTimeSelect.setOnClickListener {
            val items = arrayOf("1일", "2일", "3일","4일", "5일")
            val builder = AlertDialog.Builder(context)
                .setTitle("경매시간 선택")
                .setItems(items) { dialog, which ->
                    auctionTime=items[which]
                    binding.auctionTime.text = "경매 시간 : $auctionTime"
                }
                .show()
        }

        binding.image.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, 100)
            isImageUpload = true
        }

        // Tap을 클릭하면 Fragment간 전환 부분
        binding.searchTap.setOnClickListener {

            it.findNavController().navigate(R.id.action_writeFragment_to_searchFragment)

        }

        binding.bookmarkTap.setOnClickListener {

            it.findNavController().navigate(R.id.action_writeFragment_to_bookmarkFragment)

        }

        binding.homeTap.setOnClickListener {

            it.findNavController().navigate(R.id.action_writeFragment_to_homeFragment)
        }

        binding.chatTap.setOnClickListener {

            it.findNavController().navigate(R.id.action_writeFragment_to_chatFragment)
        }

        binding.userTap.setOnClickListener {

            it.findNavController().navigate(R.id.action_writeFragment_to_userFragment)
        }


        return binding.root
    }

    private fun imageUpload(key:String) {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val mountainsRef = storageRef.child("board").child(key+".png")

        val imageView = binding.image
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == AppCompatActivity.RESULT_OK && requestCode == 100){
            binding.image.setImageURI(data?.data)
        }
    }

    private fun getTime() : String {
        val currentTime = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).format(currentTime)
        return dateFormat
    }
}