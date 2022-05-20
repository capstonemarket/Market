package com.example.market.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.market.HomeList.HomeListAdapter
import com.example.market.R
import com.example.market.board.BoardActivity
import com.example.market.board.BoardModel
import com.example.market.databinding.FragmentSearchBinding
import com.example.market.utils.FBRef
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase


class SearchFragment : Fragment() {

    private lateinit var binding : FragmentSearchBinding
    private val boardList = ArrayList<BoardModel>()
    private val keyList = mutableListOf<String>()
    private lateinit var adapter : HomeListAdapter
    private lateinit var auth: FirebaseAuth
    val bookmarkIdList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)
        auth = Firebase.auth
        binding.searchBtn.setOnClickListener {
            val keyword = binding.searchText.text.toString()
            if(!(keyword.isEmpty())){
                getBookmark(keyword)
            }
            binding.notice.isVisible=false
        }
        // Tap을 클릭하면 Fragment간 전환 부분
        val rv : RecyclerView = binding.rv
        adapter = HomeListAdapter(boardList, keyList, bookmarkIdList)
        rv.adapter = adapter
        rv.layoutManager = GridLayoutManager(context,2)
        rv.setLayoutManager(rv.layoutManager)

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

        binding.homeTap.setOnClickListener {

            it.findNavController().navigate(R.id.action_searchFragment_to_homeFragment)

        }

        binding.bookmarkTap.setOnClickListener {

            it.findNavController().navigate(R.id.action_searchFragment_to_bookmarkFragment)

        }

        binding.chatTap.setOnClickListener {

            it.findNavController().navigate(R.id.action_searchFragment_to_chatFragment)
        }

        binding.userTap.setOnClickListener {

            it.findNavController().navigate(R.id.action_searchFragment_to_userFragment)
        }

        binding.writeTap.setOnClickListener {

            it.findNavController().navigate(R.id.action_searchFragment_to_writeFragment)

        }

        return binding.root
    }
    private fun getBookmark(keyword: String) {
        val key = FBRef.bookmarkRef.child(auth.currentUser!!.uid.toString())
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (dataModel in dataSnapshot.children) {
                    bookmarkIdList.add(dataModel.key.toString())
                }

            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("ContentListActivity", "loadPost:onCancelled", databaseError.toException())
            }
        }
        key.addValueEventListener(postListener)
        getBoard(keyword)
    }
    private fun getBoard(keyword:String) {
        val key = FBRef.boardRef
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (dataModel in dataSnapshot.children) {
                    val item = dataModel.getValue(BoardModel::class.java)
                    val itemTitle = item?.title.toString()
                    if(itemTitle.contains(keyword)){
                        binding.notice.isVisible=false
                        boardList.add(item!!)
                        keyList.add(dataModel.key.toString())
                    }else{
                        binding.notice.isVisible=true
                        boardList.clear()
                        keyList.clear()
                    }

                }
                adapter.notifyDataSetChanged()

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("ContentsListActivity", "loadPost:onCancelled", databaseError.toException())
            }
        }
        key.addValueEventListener(postListener)

    }

}