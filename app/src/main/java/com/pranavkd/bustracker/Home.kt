package com.pranavkd.bustracker

import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private var navControllerr : NavHostController? = null;

@Composable
fun Home(navController: NavHostController, sharedPreferences: SharedPreferences) {
    navControllerr = navController;
    MaterialTheme {
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
    var loading by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        if (bookingId == null) {
            // Show text field and submit button
            Text(text = "Enter Booking ID:")
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = textFieldValue,
                onValueChange = { textFieldValue = it },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    // Save booking ID to shared preferences
                    sharedPreferences.edit().putString("bookingId", textFieldValue).apply()
                    bookingId = textFieldValue
                    loading = true
                    //wait for 2 seconds
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(2000)
                        loading = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit")
            }
        } else {
            // Show bus details
            if(!loading){
                BusDetails(bookingId = bookingId!!,onClick = {
                    sharedPreferences.edit().remove("bookingId").apply()
                    bookingId = null
                })
            }else{
                Text("Loading...")
            }
        }
    }
}

@Composable
fun BusDetails(bookingId: String,onClick: () -> Unit) {
    // In a real app, you would fetch bus details based on the booking ID
    // For this example, we'll use hardcoded data
    val busId = "BUS123"
    val source = "City A"
    val destination = "City B"
    val conductor = "John Doe"
    val time = "10:00 AM"
    val routes = listOf("Stop 1", "Stop 2", "Stop 3")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Text(
                text = "Booking ID: $bookingId",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = {
                    onClick()
                },
                modifier = Modifier.fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Text("Change Booking ID",
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            DetailRow(label = "Bus ID", value = busId)
            DetailRow(label = "Source", value = source)
            DetailRow(label = "Destination", value = destination)
            DetailRow(label = "Conductor", value = conductor)
            DetailRow(label = "Time", value = time)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Routes:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            TrackButton(onClick = {
                navControllerr?.navigate("TrackingScreen")
            })
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(routes) { route ->
                    RouteItem(route = route)
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun RouteItem(route: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = "Route Stop",
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.padding(4.dp))
        Text(text = route, fontSize = 16.sp)
    }
}

@Composable
fun TrackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(16.dp)
    ) {
        Text(
            text = "Track",
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 24.dp, vertical = 8.dp)
        )
    }
}