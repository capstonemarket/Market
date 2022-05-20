package com.example.market.BookmarkList

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.market.HomeList.HomeListAdapter
import com.example.market.R
import com.example.market.board.BoardModel
import com.example.market.utils.FBRef
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class BookmarkListAdapter(val items : ArrayList<BoardModel>, val keyList : MutableList<String>,val bookmarkIdList :MutableList<String>) : RecyclerView.Adapter<BookmarkListAdapter.Viewholder>() {
    private lateinit var auth : FirebaseAuth
    private var isBookmark : Boolean = false
    interface OnItemClickListener{
        fun onItemClick(v: View, data: BoardModel, pos : Int)
    }
    private var listener : OnItemClickListener? = null
    fun setOnItemClickListener(listener : OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : Viewholder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.activity_bookmark_list_item, parent,false)
        return Viewholder(v)
    }

    override fun onBindViewHolder(holder: BookmarkListAdapter.Viewholder, position: Int){

        holder.bindItems(items[position], keyList[position])

    }
    override fun getItemCount() : Int {
        return items.size
    }
    private fun getBookmarkData(key: String, itemView: View) {
        val bookmarkBtn = itemView.findViewById<ImageView>(R.id.bookmarkHeart)
        val bookmarkL = FBRef.bookmarkRef.child(Firebase.auth.uid.toString()).child(key).get()
            .addOnSuccessListener {
                Log.i("firebase", "Got value ${it.value}")
                if(it.value == null){
                    isBookmark = false
                }
                else {
                    isBookmark = true
                    bookmarkBtn.setImageResource(R.drawable.ic_full_heart)
                }
            }.addOnCanceledListener {
                Log.e("firebase", "Error getting data")
            }
    }
    private fun bookmark(key : String, itemView: View) {
        val bookmarkBtn = itemView.findViewById<ImageView>(R.id.bookmarkHeart)
        isBookmark = if (!isBookmark) {
            FBRef.bookmarkRef.child(Firebase.auth.uid.toString()).child(key).setValue("true")
            bookmarkBtn.setImageResource(R.drawable.ic_full_heart)
            true
        } else {
            FBRef.bookmarkRef.child(Firebase.auth.uid.toString()).child(key).removeValue()
            bookmarkBtn.setImageResource(R.drawable.ic_empty_heart)
            false
        }
    }

    inner class Viewholder(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun bindItems(item : BoardModel, key : String){
            val storageReference = Firebase.storage.reference.child("board").child(key+".png")

            val imageViewFromFB = itemView.findViewById<ImageView>(R.id.image)

            storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener{ task ->
                if(task.isSuccessful){
                    Glide.with(itemView).load(task.result).transform(CenterCrop(), RoundedCorners(50)).into(imageViewFromFB)
                    Log.d("img","yyy")

                }else{
                    Log.d("img","XXX")
                }
            })

            val title = itemView.findViewById<TextView>(R.id.price)
            val productTitle = itemView.findViewById<TextView>(R.id.productName)
            val bookmarkBtn = itemView.findViewById<ImageView>(R.id.bookmarkHeart)
            if(bookmarkIdList.contains(key)){
                bookmarkBtn.setImageResource(R.drawable.ic_full_heart)
            }else{
                bookmarkBtn.setImageResource(R.drawable.ic_empty_heart)
            }
            auth = Firebase.auth
            getBookmarkData(key, itemView)
            bookmarkBtn.setOnClickListener {
                bookmark(key,itemView)
            }
            productTitle.text = item.title
            productTitle.setSelected(true)
            title.text = item.min_value

            val pos = adapterPosition
            if(pos!= RecyclerView.NO_POSITION)
            {
                itemView.setOnClickListener {
                    listener?.onItemClick(itemView,item,pos)
                }
            }
        }
    }


}