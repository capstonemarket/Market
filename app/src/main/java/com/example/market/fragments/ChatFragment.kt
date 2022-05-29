package com.example.market.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.market.MainActivity
import com.example.market.MessageActivity
import com.example.market.R
import com.example.market.auth.User
import com.example.market.auth.chatModel
import com.example.market.databinding.FragmentChatBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.util.*
import java.util.Collections.reverse
import java.util.Collections.reverseOrder
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatFragment : Fragment() {
    companion object {
        fun newInstance(): ChatFragment {
            return ChatFragment()
        }
    }

    private lateinit var binding: FragmentChatBinding
    private val fireDatabase = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false)
        val rv: RecyclerView = binding.rv
        rv.adapter = RecyclerViewAdapter()
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.setLayoutManager(rv.layoutManager)

        binding.searchTap.setOnClickListener {

            it.findNavController().navigate(R.id.action_chatFragment_to_searchFragment)
        }

        binding.bookmarkTap.setOnClickListener {

            it.findNavController().navigate(R.id.action_chatFragment_to_bookmarkFragment)
        }

        binding.homeTap.setOnClickListener {

            it.findNavController().navigate(R.id.action_chatFragment_to_homeFragment)
        }

        binding.userTap.setOnClickListener {

            it.findNavController().navigate(R.id.action_chatFragment_to_userFragment)
        }

        binding.writeTap.setOnClickListener {

            it.findNavController().navigate(R.id.action_chatFragment_to_writeFragment)

        }
        return binding.root
    }

    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.CustomViewHolder>() {
        private val chatModel = ArrayList<chatModel>()
        private var uid: String? = null
        private val destinationUsers: ArrayList<String> = arrayListOf()

        init {
            uid = Firebase.auth.currentUser?.uid.toString()
            fireDatabase.child("chatroom").orderByChild("users/$uid").equalTo(true)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        Log.w("ContentListActivity", "loadPost:onCancelled", error.toException())
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        chatModel.clear()
                        for (data in snapshot.children) {
                            Log.w("data", data.toString())
                            chatModel.add(data.getValue<chatModel>()!!)
                        }
                        notifyDataSetChanged()
                    }
                })
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            return CustomViewHolder(
                LayoutInflater.from(context).inflate(R.layout.chat_item, parent, false)
            )
        }

        inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView = itemView.findViewById<ImageView>(R.id.chat_item_imageview)
            val textView_title: TextView = itemView.findViewById(R.id.chat_textview_title)
            val textView_lastMessage: TextView = itemView.findViewById(R.id.chat_item_textview_lastmessage)
        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            var destinationUid: String? = null
            for (user in chatModel[position].users.keys) {
                Log.w("chatdata",user)
                if (!user.equals(uid)) {
                    destinationUid = user
                    destinationUsers.add(destinationUid)
                }
            }
            fireDatabase.child("users").child("$destinationUid")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {}

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val friend = snapshot.getValue<User>()

                        Firebase.storage.reference.child("userProfile")
                            .child(friend?.uid.toString()+".png")
                            .downloadUrl.addOnCompleteListener {
                            if (it.isSuccessful) {
                                Glide.with(holder.itemView).load(it.result).apply(RequestOptions().centerCrop().circleCrop()).into(holder.imageView)
                            }
                        }
                        holder.textView_title.text = friend?.name
                    }
                })
            val commentMap = TreeMap<String, chatModel.Comment>(reverseOrder())
            commentMap.putAll(chatModel[position].comments)
            val lastMessageKey = commentMap.keys.toTypedArray()[0]
            Log.w("마지막채팅",lastMessageKey)
            holder.textView_lastMessage.text = chatModel[position].comments[lastMessageKey]?.message

            holder.itemView.setOnClickListener {
                Intent(activity , MessageActivity::class.java).apply {
                    putExtra("destinationUid", destinationUsers[position])
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run { startActivity(this) }
            }
        }

        override fun getItemCount(): Int {
            return chatModel.size
        }
    }
}