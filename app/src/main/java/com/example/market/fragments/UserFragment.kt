package com.example.market.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.market.HomeList.HomeListAdapter
import com.example.market.R
import com.example.market.UserList.UserListAdapter
import com.example.market.auth.LoginActivity
import com.example.market.board.BoardActivity
import com.example.market.board.BoardModel
import com.example.market.databinding.FragmentChatBinding
import com.example.market.databinding.FragmentUserBinding
import com.example.market.utils.FBRef
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase


class UserFragment : Fragment() {


    private lateinit var binding : FragmentUserBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter : UserListAdapter
    private val boardList = ArrayList<BoardModel>()
    private val keyList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user, container, false)


//        getImageData(key.toString())
        getBoard() //값 가져오


        val rv : RecyclerView = binding.rv


        adapter = UserListAdapter(boardList, keyList)
        rv.adapter = adapter
        rv.layoutManager = GridLayoutManager(context,2) //Fragment내에서 this -> context사용
        rv.setLayoutManager(rv.layoutManager)


        //클릭 리스
        adapter.setOnItemClickListener(object : UserListAdapter.OnItemClickListener {
            override fun onItemClick(v: View, data: BoardModel, pos : Int) {

                Intent(activity , BoardActivity::class.java).apply {
                    putExtra("key", keyList[pos])
                    //putExtra("title", data.title)
                    //putExtra("currentP", data.minValue.toInt())  //Int는 toInt시켜서 전달

                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run { startActivity(this) }
            }
        })

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

        //리사이클러뷰를 통한 게시물 불러오기
        val key = FBRef.boardRef
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

             for(dataModel in dataSnapshot.children) {
                 Log.d("title",dataModel.getValue(BoardModel::class.java)!!.title)
                 Log.d("minValue",dataModel.getValue(BoardModel::class.java)!!.min_value)
                 Log.d("maxValue",dataModel.getValue(BoardModel::class.java)!!.max_value)
                 Log.d("category",dataModel.getValue(BoardModel::class.java)!!.category)
                 Log.d("content",dataModel.getValue(BoardModel::class.java)!!.content)

//                 binding.title.text = dataModel.getValue(BoardModel::class.java)!!.title
//                 binding.price.text = "상한가:" + dataModel.getValue(BoardModel::class.java)!!.min_value + "원"
//                 binding.price.text = "상한가:" + dataModel.getValue(BoardModel::class.java)!!.max_value + "원"
             }
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

        return binding.root
    }

    private fun getBoard() {
        val key = FBRef.boardRef
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for(dataModel in dataSnapshot.children) {

                    val item =  dataModel.getValue(BoardModel::class.java)!!
                    Log.d("uids",item.uid)

                    Log.d("hello",Firebase.auth.currentUser?.uid!!) //현재 ID

//                    if (Firebase.auth.currentUser?.uid!! == price.uid){
                    boardList.add(item!!)
                    keyList.add(dataModel.key.toString())
//                    }
                }
                adapter.notifyDataSetChanged()


            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        key.addValueEventListener(postListener)
    }

}