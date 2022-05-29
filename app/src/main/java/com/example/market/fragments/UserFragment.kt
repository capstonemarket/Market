package com.example.market.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.market.HomeList.HomeListAdapter
import com.example.market.R
import com.example.market.auth.LoginActivity
import com.example.market.board.BoardActivity
import com.example.market.board.BoardModel
import com.example.market.databinding.FragmentChatBinding
import com.example.market.databinding.FragmentUserBinding
import com.example.market.utils.FBRef
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream


class UserFragment : Fragment() {
    private val boardList = ArrayList<BoardModel>()
    private val keyList = mutableListOf<String>()
    private lateinit var binding : FragmentUserBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var key : String
    private lateinit var adapter : HomeListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        auth = Firebase.auth
        key = auth.currentUser?.uid.toString()
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user,container,false)
        val rv : RecyclerView = binding.rv
        getImageData(key)
        binding.userImage.clipToOutline = true
        //user Email
        if (Firebase.auth.currentUser!= null){

            val fakeEmail = Firebase.auth.currentUser!!.email.toString().split("@")[0]  //email 부분만 보기
            binding.userId.text = fakeEmail + "님"

            Log.d("email",Firebase.auth.currentUser!!.email.toString() )

        }else {
            Log.d("email", "Email is Null")
            binding.userId.text = "" //비회원 로그인시 안 보임
        }

        //로그아웃
        binding.logOut.setOnClickListener {
            Firebase.auth.signOut()
            startActivity(Intent(context, LoginActivity::class.java))

        }
        adapter = HomeListAdapter(boardList, keyList)
        rv.adapter = adapter
        rv.layoutManager = GridLayoutManager(context,2) //Fragment내에서 this -> context사용
        rv.setLayoutManager(rv.layoutManager)

        adapter.setOnItemClickListener(object : HomeListAdapter.OnItemClickListener {
            override fun onItemClick(v: View, data: BoardModel, pos : Int) {

                Intent(activity , BoardActivity::class.java).apply {
                    putExtra("key", keyList[pos])
                    //putExtra("title", data.title)
                    //putExtra("currentP", data.minValue.toInt())  //Int는 toInt시켜서 전달

                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run {
                    startActivity(this)
                    requireActivity().overridePendingTransition( R.anim.slide_in_right,R.anim.hold)
                }
            }

        })
        //리사이클러뷰를 통한 게시물 불러오기
        val key = FBRef.boardRef
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

             for(dataModel in dataSnapshot.children) {
                 val item = dataModel.getValue(BoardModel::class.java)!!
                 if(item.uid==auth.uid.toString()){
                     boardList.add(item!!)
                     keyList.add(dataModel.key.toString())
                 }
             }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        key.addValueEventListener(postListener)


        // Tap을 클릭하면 Fragment간 전환 부분
        binding.searchTap.setOnClickListener {

            it.findNavController().navigate(R.id.action_userFragment_to_searchFragment)

        }

        binding.bookmarkTap.setOnClickListener {

            it.findNavController().navigate(R.id.action_userFragment_to_bookmarkFragment)

        }

        binding.homeTap.setOnClickListener {

            it.findNavController().navigate(R.id.action_userFragment_to_homeFragment)
        }

        binding.chatTap.setOnClickListener {

            it.findNavController().navigate(R.id.action_userFragment_to_chatFragment)
        }

        binding.writeTap.setOnClickListener {

            it.findNavController().navigate(R.id.action_userFragment_to_writeFragment)

        }
        //유저 프로필 하단 버튼 누를 시 갤러리 열림
        binding.changeBtn.setOnClickListener {
            gotoAlbum()
        }

        return binding.root
    }
    //유저 프로필 사진 바꾸기 dialog
    private fun showDialog(){
        val mDialogView = LayoutInflater.from(context).inflate(R.layout.user_profile_dialog, null)
        val mBuilder = AlertDialog.Builder(requireContext())
            .setView(mDialogView)
        val alertDialog = mBuilder.show()
        alertDialog.window?.setLayout(800, WindowManager.LayoutParams.WRAP_CONTENT)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.findViewById<Button>(R.id.yesBtn)?.setOnClickListener{
            alertDialog.dismiss()
            imageUpload(key)
        }
        alertDialog.findViewById<Button>(R.id.noBtn)?.setOnClickListener{
            getImageData(key)
            alertDialog.dismiss()
        }
    }
    //갤러리 열기
    private fun gotoAlbum(){
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, 100)
    }
    //유저 프로필 이미지 업로드
    private fun imageUpload(key:String) {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val mountainsRef = storageRef.child("userProfile").child(key+".png")

        val imageView = binding.userImage
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
    //유저 프로필 사진 받아오기
    private fun getImageData(key: String) {
        val storageReference = Firebase.storage.reference.child("userProfile").child(key+".png")

        val imageViewFromFB = binding.userImage

        storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener{ task ->
            if(task.isSuccessful){
                Glide.with(this).load(task.result).into(imageViewFromFB)
            }else{
                binding.userImage.setImageResource(R.drawable.user_basic_profile)
            }
        })
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == AppCompatActivity.RESULT_OK && requestCode == 100){
            binding.userImage.setImageURI(data?.data)
            showDialog()
        }
    }

}