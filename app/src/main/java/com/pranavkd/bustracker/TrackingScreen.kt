package com.pranavkd.bustracker

import android.content.Context
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.pranavkd.bustracker.ui.theme.BusTrackerTheme
import kotlinx.coroutines.launch

@Composable
fun MainScreen(navController: NavHostController?,sharedPreferences :  android.content.SharedPreferences) {
    val context = LocalContext.current
    var bookingId by remember { mutableStateOf(sharedPreferences.getString("bookingId", "") ?: "") }
    var showBottomSheet by remember { mutableStateOf(false) }
    if(bookingId.isEmpty()) {
        showBottomSheet = true
    }
    var locat by remember { mutableStateOf(LatLng(10.517147, 76.212787)) }
    var rotation by remember { mutableStateOf(0f) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(locat,15f,45f,0f)
    }
    var routeCoordinates by remember {
        mutableStateOf(listOf(
            LatLng(10.512503, 76.220678),
            LatLng(9.901145, 76.712371),
        ))
    }
    LaunchedEffect(bookingId) {
        Log.d("MainScreen", "Booking Id: $bookingId")
        sharedPreferences.edit().putString("bookingId", bookingId).apply()
        Toast.makeText(context, "Bus Id: $bookingId", Toast.LENGTH_SHORT).show()
        val managers = Managers()
        managers.getTravelRoute(bookingId) {
            routeCoordinates = it
            Log.d("Managers", "Response: $it")
        }
        managers.sendBusLocationWs(bookingId, callback = {
            Log.d("MainActivity", "Bus Location: $it")
            val newLocation = it
            rotation = calculateBearing(locat, newLocation)
            locat = newLocation
        })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            //.padding(WindowInsets.statusBars.only(WindowInsetsSides.Top).asPaddingValues())
    ) {
        Scaffold(
            floatingActionButton = {
                androidx.compose.material3.FloatingActionButton(
                    onClick = {
                        val oldPosition = cameraPositionState.position
                        cameraPositionState.position = CameraPosition(
                            locat,
                            oldPosition.zoom,
                            oldPosition.tilt,
                            oldPosition.bearing
                        )
                    }
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.LocationOn,
                        contentDescription = "Add"
                    )
                }
            }
        ) { paddingValues ->
            Log.e("MainScreen", "Padding Values: $paddingValues")
            MapLayoutBox(
                locat = locat,
                rotation = rotation,
                cameraPositionState = cameraPositionState,
                routeCoordinates = routeCoordinates
            )
            StatusIndicator(onClick = {
                navController?.navigate("Home")
            }, bookingId = bookingId)

        }

    }
}

@Composable
fun StatusIndicator(onClick:()->Unit, bookingId: String = "") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            onClick = {onClick()}
        ) {
            Text(
                text = "Enter New Bus ID, now Traking: $bookingId",
                modifier = Modifier.padding(16.dp)
            )

        }
        Button(
            onClick = {onClick(

            )},
            modifier = Modifier
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Home,
                contentDescription = "Add"
            )
        }
    }
}



@Composable
fun MapLayoutBox(
    locat: LatLng,
    rotation: Float = 0f,
    cameraPositionState: CameraPositionState,
    routeCoordinates: List<LatLng>
) {
    val context = LocalContext.current
    val mapStyleOptions = remember {
        MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(mapStyleOptions = mapStyleOptions)
        ){
            MarkerComposable(
                state = MarkerState(position = locat),
                rotation = rotation
            ) {
                Image(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width(50.dp)
                        .height(50.dp),
                    painter = painterResource(id = R.drawable.marker_dafault),
                    contentDescription = "",
                )
            }
            Polyline(
                points = routeCoordinates,
                clickable = true,
                color = Color.Blue,
                width = 5f
            )


        }

    }
}

fun calculateBearing(startLatLng: LatLng, endLatLng: LatLng): Float {
    val startLocation = Location("").apply {
        latitude = startLatLng.latitude
        longitude = startLatLng.longitude
    }
    val endLocation = Location("").apply {
        latitude = endLatLng.latitude
        longitude = endLatLng.longitude
    }
    return startLocation.bearingTo(endLocation)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BusTrackerTheme {
        MainScreen(null, LocalContext.current.getSharedPreferences("BusTracker", Context.MODE_PRIVATE))
    }
}