package com.example.market.utils

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FBRef {
    companion object {
        private val database = Firebase.database

        val boardRef = database.getReference("board")

        val c_clothes = database.getReference("c_clothes")
        val c_electronics = database.getReference("c_electronics")
        val c_book = database.getReference("c_book")
        val c_food = database.getReference("c_food")
        //"옷", "전자제품", "책", "음식"
    }
}