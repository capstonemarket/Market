package com.example.market

import android.annotation.SuppressLint
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.market.R
import com.example.market.R.color.white
import com.example.market.auth.User
import com.example.market.auth.chatModel
import com.google.android.datatransport.runtime.logging.Logging.w
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MessageActivity : AppCompatActivity() {
    private val fireDatabase = FirebaseDatabase.getInstance().reference
    private var chatRoomUid: String? = null
    private var destinationUid: String? = null
    private var uid: String? = null
    private var recyclerView: RecyclerView? = null

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        val imageView = findViewById<ImageView>(R.id.messageActivity_ImageView)
        val editText = findViewById<TextView>(R.id.messageActivity_editText)
        val time = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("MM월dd일 hh:mm")
        val curTime = dateFormat.format(Date(time)).toString()

        val textWatcher = object:TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                if(p0.toString().length==0) {
                    imageView.isEnabled = false
                }
                else {
                    imageView.isEnabled = true
                }
            }
        }
        editText.addTextChangedListener(textWatcher)

        destinationUid = intent.getStringExtra("destinationUid")
        uid = Firebase.auth.currentUser?.uid.toString()
        recyclerView = findViewById(R.id.messageActivity_recyclerview)

        imageView.setOnClickListener {
            val chatModel = chatModel()
            chatModel.users.put(uid.toString(), true)
            chatModel.users.put(destinationUid!!, true)

            val comment =
                com.example.market.auth.chatModel.Comment(uid, editText.text.toString(), curTime)
            if (chatRoomUid == null) {
                imageView.isEnabled = false
                fireDatabase.child("chatroom").push().setValue(chatModel).addOnSuccessListener {
                    checkChatRoom()
                    Handler().postDelayed({
                        println(chatRoomUid)
                        fireDatabase.child("chatroom").child(chatRoomUid.toString())
                            .child("comments").push().setValue(comment)
                        editText.text = null
                    }, 1000L)
                }
            } else {
                if(editText.text.toString().trim().isNotEmpty()) {
                    fireDatabase.child("chatroom").child(chatRoomUid.toString()).child("comments")
                        .push().setValue(comment)
                }
                editText.text = null
            }
        }
        checkChatRoom()
    }

    private fun checkChatRoom() {
        val imageView = findViewById<ImageView>(R.id.messageActivity_ImageView)

        fireDatabase.child("chatroom").orderByChild("users/$uid").equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (item in snapshot.children) {
                        Log.w("채팅snapshot", item.toString());
                        val chatModel = item.getValue<chatModel>()
                        if (chatModel?.users!!.containsKey(destinationUid)) {
                            chatRoomUid = item.key
                            Log.w("chatroomUid", chatRoomUid.toString())
                            imageView.isEnabled = true
                            recyclerView?.adapter = RecyclerViewAdapter()
                            recyclerView?.layoutManager = LinearLayoutManager(this@MessageActivity)
                            recyclerView?.setLayoutManager(recyclerView!!.layoutManager)
                        }
                    }
                }
            })
    }

    inner class RecyclerViewAdapter :
        RecyclerView.Adapter<RecyclerViewAdapter.MessageViewHolder>() {
        private val comments = ArrayList<chatModel.Comment>()
        val friend_name = findViewById<TextView>(R.id.messageActivity_textView_topName)
        private var friend: User? = null

        init {
            fireDatabase.child("users").child(destinationUid.toString())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {}
                    override fun onDataChange(snapshot: DataSnapshot) {
                        friend = snapshot.getValue<User>()
                        friend_name.text = friend?.name
                        getMessageList()
                    }
                })
        }

        fun getMessageList() {
            fireDatabase.child("chatroom").child(chatRoomUid.toString()).child("comments")
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {}
                    override fun onDataChange(snapshot: DataSnapshot) {
                        comments.clear()
                        for (data in snapshot.children) {
                            val item = data.getValue<chatModel.Comment>()
                            comments.add(item!!)
                        }
                        notifyDataSetChanged()
                        recyclerView?.scrollToPosition(comments.size - 1)
                    }
                })
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val view: View = LayoutInflater.from(parent.context).inflate(R.layout.message_item, parent, false)
            return MessageViewHolder(view)
        }

        @SuppressLint("RtlHardcoded", "ResourceAsColor")
        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
            holder.textView_message.textSize = 20F
            holder.textView_message.text = comments[position].message
            holder.textView_time.text = comments[position].time
            if (comments[position].uid.equals(uid)) {
                holder.textView_message.setBackgroundResource(R.drawable.bubbleright)
                holder.textView_name.visibility = View.INVISIBLE
                holder.layout_destination.visibility = View.INVISIBLE
                holder.layout_main.gravity = Gravity.RIGHT
            } else {
                Firebase.storage.reference.child("userProfile")
                    .child(friend?.uid.toString()+".png")
                    .downloadUrl.addOnCompleteListener {
                        if (it.isSuccessful) {
                            Glide.with(holder.itemView).load(it.result).apply(RequestOptions().centerCrop().circleCrop()).into(holder.imageView_profile)
                        }else{
                            holder.imageView_profile.setImageResource(R.drawable.user_basic_profile)
                            holder.imageView_profile.clipToOutline=true
                        }
                    }
                holder.textView_message.setTextColor(R.color.black)
                holder.textView_name.text = friend?.name
                holder.layout_destination.visibility = View.VISIBLE
                holder.textView_name.visibility = View.VISIBLE
                holder.textView_message.setBackgroundResource(R.drawable.bubbleleft)
                holder.layout_main.gravity = Gravity.LEFT
            }
        }

        inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textView_message: TextView = view.findViewById(R.id.messageItem_textView_message)
            val textView_name: TextView = view.findViewById(R.id.messageItem_textview_name)
            val imageView_profile: ImageView = view.findViewById(R.id.messageItem_imageview_profile)
            val layout_destination: LinearLayout =
                view.findViewById(R.id.messageItem_layout_destination)
            val layout_main: LinearLayout = view.findViewById(R.id.messageItem_linearlayout_main)
            val textView_time: TextView = view.findViewById(R.id.messageItem_textView_time)
        }

        override fun getItemCount(): Int {
            return comments.size
        }
    }
}