package com.example.delivman

class User(
    val login:String,
    val email: String,
    val pass:String,
    val popularAdress:MutableList<String> = mutableListOf()
) {
}