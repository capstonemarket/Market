package com.example.market.board

import android.app.ActionBar
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.market.MessageActivity
import com.example.market.R
import com.example.market.databinding.ActivityBoardBinding
import com.example.market.sendPostToFCM
import com.example.market.utils.FBRef
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.text.SimpleDateFormat
import java.util.*
import java.text.DecimalFormat
import kotlin.concurrent.timer



class BoardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBoardBinding
    private lateinit var key: String
    private lateinit var up: String
    private lateinit var postTime: String
    private lateinit var auctionTime: String
    private var isBookmark : Boolean = false
    private var timer: Timer? = null
    private var boardModel = BoardModel()
    private var boardModelKey = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_board)
        binding.img.clipToOutline = true
        binding.title.setSelected(true)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.menuBtn.setOnClickListener {
            showDialog()
        }

        key = intent.getStringExtra("key").toString()
        if (!::up.isInitialized) {
            up = binding.upCount.text.toString()
        }

        if (!::postTime.isInitialized || !::auctionTime.isInitialized) {
            if (!::postTime.isInitialized) {
                val postTimeL = FBRef.boardRef.child(key).child("time").get().addOnSuccessListener {
                    Log.i("firebase", "Got value ${it.value}")
                    postTime = "${it.value}"
                    if (!::auctionTime.isInitialized) {
                        val auctionTimeL = FBRef.boardRef.child(key).child("auction_time").get()
                            .addOnSuccessListener {
                                Log.i("firebase", "Got value ${it.value}")
                                auctionTime = "${it.value}"
                                updateTimer(postTime, "${it.value}")
                            }.addOnCanceledListener {
                                Log.e("firebase", "Error getting data")
                            }
                    }
                }.addOnCanceledListener {
                    Log.e("firebase", "Error getting data")
                }
            }
        } else {
            updateTimer(postTime, auctionTime)
        }



        getBoardData(key)
        getImageData(key)
        getBookmarkData(key)

        binding.upBtn.setOnClickListener {
            showUpDialog()
        }
        binding.chatBtn.setOnClickListener {
            if(boardModel.uid!=Firebase.auth.currentUser?.uid.toString()) {
                Intent(this, MessageActivity::class.java).apply {
                    putExtra("destinationUid", boardModel.uid)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run { startActivity(this) }
            }
        }

        binding.heartBtn.setOnClickListener {
            bookmark(key)
        }
        binding.img.setOnClickListener {
            showPhotoDialog(key)
        }

    }

    private fun getBookmarkData(key: String) {
        val bookmarkL = FBRef.bookmarkRef.child(Firebase.auth.uid.toString()).child(key).get()
            .addOnSuccessListener {
                Log.i("firebase", "Got value ${it.value}")
                if(it.value == null){
                    isBookmark = false
                }
                else {
                    isBookmark = true
                    binding.heartBtn.setImageResource(R.drawable.ic_full_heart)
                }
            }.addOnCanceledListener {
                Log.e("firebase", "Error getting data")
            }
    }

    private fun bookmark(key : String) {
        isBookmark = if (!isBookmark) {
            FBRef.bookmarkRef.child(Firebase.auth.uid.toString()).child(key).setValue("true")
            binding.heartBtn.setImageResource(R.drawable.ic_full_heart)
            true
        } else {
            FBRef.bookmarkRef.child(Firebase.auth.uid.toString()).child(key).removeValue()
            binding.heartBtn.setImageResource(R.drawable.ic_empty_heart)
            false
        }
    }

    //사진 클릭하면 확대된 사진보여주는 dialog
    private fun showPhotoDialog(key: String) {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.photo_dialog, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
        val alertDialog = mBuilder.show()
        val storageReference = Firebase.storage.reference.child("board").child(key + ".png")

        val imageViewFromFB = alertDialog.findViewById<ImageView>(R.id.image)

        storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
            if (task.isSuccessful) {
                if (imageViewFromFB != null) {
                    Glide.with(this).load(task.result).into(imageViewFromFB)
                }
            } else {
                binding.img.isVisible = false
            }
        })

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun showDialog() {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.menu_dialog, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)

        val alertDialog = mBuilder.show()
        alertDialog.window?.setLayout(600, WindowManager.LayoutParams.WRAP_CONTENT)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        alertDialog.findViewById<Button>(R.id.editBtn)?.setOnClickListener {
            val intent = Intent(this, BoardEditActivity::class.java)
            intent.putExtra("key", key)
            startActivity(intent)
            alertDialog.dismiss()
        }

        alertDialog.findViewById<Button>(R.id.removeBtn)?.setOnClickListener {
            showDeleteDialog()
            alertDialog.dismiss()
        }


    }

    private fun showDeleteDialog() {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.delete_dialog, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)

        val alertDialog = mBuilder.show()
        alertDialog.window?.setLayout(800, WindowManager.LayoutParams.WRAP_CONTENT)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.findViewById<Button>(R.id.yesBtn)?.setOnClickListener {
            FBRef.boardRef.child(key).removeValue()
            Firebase.storage.reference.child("board").child(key + ".png").delete()
                .addOnSuccessListener {
                    // File deleted successfully
                }.addOnFailureListener {
                    // Uh-oh, an error occurred!
                }
            alertDialog.dismiss()
        }

        alertDialog.findViewById<Button>(R.id.noBtn)?.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun showUpDialog() {
        //밑에서 올라오는 다이얼로그
        val bottomSheetView = layoutInflater.inflate(R.layout.up_dialog, null)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        bottomSheetDialog.show()

        bottomSheetDialog.findViewById<Button>(R.id.upBtn)?.setOnClickListener {
            val upT = bottomSheetDialog.findViewById<EditText>(R.id.upVal)?.text.toString()
            var isBlank = upT.isNullOrBlank()

            if(Firebase.auth.uid.toString() == boardModel.uid){
                android.app.AlertDialog.Builder(this)
                    .setMessage("자신의 게시물은 경매에 참여할 수 없습니다")
                    .setPositiveButton("확인", { dialogInterface: DialogInterface?, i: Int -> })
                    .show()
            } else if (!isBlank) {
                up = (up.toInt() + 1).toString()
                FBRef.boardRef
                    .child(key)
                    .child("up")
                    .setValue(up)
                binding.upCount.text = up

                val upV = upT.toInt()
                val newV = (binding.currentV.text.toString().toInt() + upV).toString()
                val currentUser = Firebase.auth.currentUser?.uid.toString()
                FBRef.boardRef
                    .child(key)
                    .child("current_v")
                    .setValue(newV)
                FBRef.boardRef
                    .child(key)
                    .child("buyer")
                    .setValue(currentUser)
                binding.currentV.text = newV
                bottomSheetDialog.dismiss()

                var title = "경매가가 갱신되었습니다."
                var message =boardModel.title+"의 경매가가 "+newV+"원으로 갱신되었습니다."
                sendPostToFCM(boardModel.buyer,title,message,boardModelKey)
            } else {
                android.app.AlertDialog.Builder(this)
                    .setMessage("up할 값을 입력해주세요")
                    .setPositiveButton("확인", { dialogInterface: DialogInterface?, i: Int -> })
                    .show()
            }

        }

    }

    private fun getImageData(key: String) {
        val storageReference = Firebase.storage.reference.child("board").child(key + ".png")

        val imageViewFromFB = binding.img

        storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
            if (task.isSuccessful) {
                Glide.with(this).load(task.result).into(imageViewFromFB)
            } else {
                binding.img.isVisible = false
            }
        })

    }

    private fun getBoardData(key: String) {

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    val dataModel = dataSnapshot.getValue(BoardModel::class.java)
                    boardModelKey = dataSnapshot.key.toString()
                    Log.w("데이터 키",boardModelKey)
                    boardModel = dataModel!!
                    binding.title.text = dataModel!!.title
                    binding.content.text = dataModel!!.content
                    binding.currentV.text = dataModel!!.current_v
                    binding.upCount.text = dataModel!!.up
                    binding.maxValue.text = dataModel!!.max_value
                    postTime = dataModel!!.time
                    auctionTime = dataModel!!.auction_time
                    val myUid = Firebase.auth.uid.toString()
                    val writerUid = dataModel.uid
                    up = dataModel!!.up

                    if (myUid == writerUid) {
                        binding.menuBtn.isVisible = true
                    } else {

                    }
                } catch (e: Exception) {

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("BoardActivity", "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.boardRef.child(key).addValueEventListener(postListener)
    }

    private fun updateTimer(postTime: String, auctionTime: String) {
        val currentTime = Calendar.getInstance().time
        Log.d("currentT", currentTime.toString())
        val convertTime = SimpleDateFormat("yyyy.MM.dd HH:mm:ss").parse(postTime)
        Log.d("convertT", convertTime.toString())

        var aTime = 0
        when (auctionTime) {
            "1일" -> aTime = 1
            "2일" -> aTime = 2
            "3일" -> aTime = 3
            "4일" -> aTime = 4
            "5일" -> aTime = 5
            else -> 0
        }

        val limitTime = (convertTime.time + aTime * 24 * 60 * 60 * 1000 - currentTime.time).toInt()
        Log.d("limitT", limitTime.toString())

        val df = DecimalFormat("00")
        if (limitTime > 0) {
            var time = limitTime / 1000
            Log.d("limitS", time.toString())
            timer = timer(period = 1000) {
                time--
                val hour = time / 3600
                val minute = df.format((time % 3600) / 60)
                val second = df.format(time % 60)
                runOnUiThread {
                    binding.lTime.text = "${hour}:${minute}:${second}"
                }
                if (time == 0) { // 타이머 종료시 거래 종료 표시
                    binding.end.isVisible = true
                    binding.lTime.isVisible = false
                    binding.bottomBar.isVisible = false
                }
            }
        } else {
            binding.end.isVisible = true
            binding.lTime.isVisible = false
            binding.bottomBar.isVisible = false
        }
    }

}
