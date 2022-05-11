package com.example.market.HomeList


import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.market.R
import com.example.market.board.BoardModel
import com.example.market.databinding.FragmentHomeBinding
import com.google.android.gms.common.util.Strings
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage


class HomeListAdapter(val items : ArrayList<BoardModel>, val keyList : MutableList<String>) : RecyclerView.Adapter<HomeListAdapter.Viewholder>(){

    interface OnItemClickListener{
        fun onItemClick(v:View, data: BoardModel, pos : Int)
    }
    private var listener : OnItemClickListener? = null
    fun setOnItemClickListener(listener : OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : HomeListAdapter.Viewholder{

        val v = LayoutInflater.from(parent.context).inflate(R.layout.activity_home_list_item, parent,false)
        return Viewholder(v)
    }

    override fun onBindViewHolder(holder: HomeListAdapter.Viewholder, position: Int){

        holder.bindItems(items[position], keyList[position])

    }
    override fun getItemCount() : Int {
        return items.size
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