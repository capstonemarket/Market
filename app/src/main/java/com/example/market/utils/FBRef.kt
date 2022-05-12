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
        //"옷", "전자제품", "책", "음식" 카데고리별 저장 수정 필요

        val bookmarkRef = database.getReference("bookmark")
        val upRef = database.getReference("up")
    }
}