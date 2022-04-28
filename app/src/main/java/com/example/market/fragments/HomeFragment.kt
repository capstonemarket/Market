package com.example.market.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.market.MainActivity
import com.example.market.R
import com.example.market.board.BoardActivity
import com.example.market.board.BoardModel
import com.example.market.HomeList.HomeListAdapter
import com.example.market.databinding.FragmentHomeBinding
import com.example.market.utils.FBRef
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.w3c.dom.Text


class HomeFragment : Fragment() {

    private val boardList = ArrayList<BoardModel>()
    private val keyList = mutableListOf<String>()

    private lateinit var binding : FragmentHomeBinding
    private lateinit var adapter : HomeListAdapter
//    val intent  = Intent(context,MainActivity::class.java)
//    val key = intent.getStringExtra("key")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

//        getImageData(key.toString())
        getBoard() //값 가져오


        val rv : RecyclerView = binding.rv


        adapter = HomeListAdapter(boardList, keyList)
        rv.adapter = adapter
        rv.layoutManager = GridLayoutManager(context,2) //Fragment내에서 this -> context사용
        rv.setLayoutManager(rv.layoutManager)


        //클릭 리스
        adapter.setOnItemClickListener(object : HomeListAdapter.OnItemClickListener {
            override fun onItemClick(v: View, data: BoardModel, pos : Int) {

                Intent(activity , BoardActivity::class.java).apply {
                    putExtra("key", keyList[pos])
                    //putExtra("title", data.title)
                    //putExtra("currentP", data.minValue.toInt())  //Int는 toInt시켜서 전달

                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run { startActivity(this) }
            }
        })


        // Tap을 클릭하면 Fragment간 전환 부분

        binding.searchTap.setOnClickListener {

            it.findNavController().navigate(R.id.action_homeFragment_to_searchFragment)

        }

        binding.bookmarkTap.setOnClickListener {

            it.findNavController().navigate(R.id.action_homeFragment_to_bookmarkFragment)

        }

        binding.chatTap.setOnClickListener {

            it.findNavController().navigate(R.id.action_homeFragment_to_chatFragment)
        }

        binding.userTap.setOnClickListener {

            it.findNavController().navigate(R.id.action_homeFragment_to_userFragment)
        }

        binding.writeTap.setOnClickListener {

            it.findNavController().navigate(R.id.action_homeFragment_to_writeFragment)

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