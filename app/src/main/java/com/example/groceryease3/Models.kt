package com.example.groceryease3

// 🔵 Shop Model
data class Shop(
    var id: String = "",
    var shopName: String = "",
    var image: String = "",
    var address: String = "",
    var distanceText: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var isFavorite: Boolean = false
)
data class Location(
    var lat: Double = 0.0,
    var lng: Double = 0.0
)

// 🔵 Product Model
data class Product(
    var id:String="",
    var name:String="",
    var qty:String="",
    var unit:String="",
    var price:String="",
    var image:String="",
    var shopId: String = "",
    var category: String = ""
)

data class Category(
    val name: String = "",
    val image: String = "",     // ✅ Base64 / URL
    val imageResId: Int? = null // ✅ Default drawable

)


data class User(
    val id: String,
    val name: String,
    var email: String,
    var imageUrl: String,
    var time: String,
)