package com.pranavkd.bustracker

import android.content.SharedPreferences
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.pranavkd.bustracker.Types.BookingHome
import com.pranavkd.bustracker.Types.Routes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private var navControllerr: NavHostController? = null

@Composable
fun Home(navController: NavHostController, sharedPreferences: SharedPreferences) {
    navControllerr = navController
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF1976D2),
            secondary = Color(0xFF42A5F5),
            background = Color(0xFFF5F7FA)
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            BookingLayout(sharedPreferences)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingLayout(sharedPreferences: SharedPreferences) {
    var bookingId by remember { mutableStateOf(sharedPreferences.getString("bookingId", null)) }
    var textFieldValue by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var bdata by remember { mutableStateOf(BookingHome(
        bookingId = "",
        fullname = "",
        email = "",
        phone = "",
        gender = "",
        busId = "",
        source = "",
        destination = "",
        conductor = "",
        timeD = "",
        timeA = "",
        status = "",
        routes = listOf()
    )) }
    val manager = Managers()
    if (bookingId != null) {
        manager.getBookingDetails(bookingId!!, onComplete = {
            bdata = it
            loading = false
        }, onFailure = {
            Log.e("Managers", "Failed to get booking details", it)
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (bookingId == null) {
            Text(
                text = "Bus Tracker",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = textFieldValue,
                        onValueChange = { textFieldValue = it },
                        label = { Text("Enter Booking ID") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Button(
                        onClick = {
                            sharedPreferences.edit().putString("bookingId", textFieldValue).apply()
                            bookingId = textFieldValue
                            loading = true
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(2000)
                                loading = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            "Track Bus",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            if (loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                    BusDetails(bookingIdData = bdata) {
                        sharedPreferences.edit().remove("bookingId").apply()
                        bookingId = null
                    }
            }
        }
    }
}

@Composable
fun BusDetails(bookingIdData: BookingHome, onClick: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    Text(
                        text = "Trip Details : ${bookingIdData.bookingId}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Passenger Info Card
                    InfoCard(
                        title = "Passenger Info",
                        content = {
                            DetailRow("Name", bookingIdData.fullname)
                            DetailRow("Email", bookingIdData.email)
                            DetailRow("Phone", bookingIdData.phone)
                            DetailRow("Gender", bookingIdData.gender)
                        }
                    )

                    // Trip Info Card
                    InfoCard(
                        title = "Trip Info",
                        content = {
                            DetailRow("Bus ID", bookingIdData.busId)
                            DetailRow("From", bookingIdData.source)
                            DetailRow("To", bookingIdData.destination)
                            DetailRow("Conductor", bookingIdData.conductor)
                            DetailRow("Departure", bookingIdData.timeD)
                            DetailRow("Arrival", bookingIdData.timeA)
                            DetailRow(
                                "Status",
                                bookingIdData.status,
                                valueColor = if (bookingIdData.status == "On Time") Color(0xFF2ECC71) else Color.Red
                            )
                        }
                    )

                    TrackButton { navControllerr?.navigate("TrackingScreen") }

                    Button(
                        onClick = onClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Change Booking", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Route Progress",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    bookingIdData.routes.forEach { route ->
                        RouteItem(route)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoCard(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        content()
    }
}

@Composable
fun RouteItem(route: Routes) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = "Route Stop",
            tint = if (route.completed) Color(0xFF2ECC71) else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = route.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (route.completed) FontWeight.Normal else FontWeight.Medium
        )
    }
}

@Composable
fun DetailRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TrackButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = "Live Tracking",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}