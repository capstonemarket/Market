package com.example.market.auth

import kotlin.collections.HashMap

class chatModel(
    val users: HashMap<String, Boolean> = HashMap(),
    val comments: HashMap<String, Comment> = HashMap()
) {
    class Comment(
        val uid: String? = null,
        val message: String? = null,
        val time: String? = null)
}
