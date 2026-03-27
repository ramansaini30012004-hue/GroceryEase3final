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
    var category: String = ""

)