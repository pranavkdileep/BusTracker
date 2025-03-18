package com.pranavkd.bustracker.Types

data class BookingHome(
    val bookingId : String,
    val fullname : String,
    val email : String,
    val phone : String,
    val gender : String,
    val busId : String,
    val source : String,
    val destination : String,
    val conductor : String,
    val timeD : String,
    val timeA : String,
    val status : String,
    val routes : List<Routes>
)

data class Routes(
    val name : String,
    val completed : Boolean
)