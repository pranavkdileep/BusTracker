package com.pranavkd.bustracker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.pranavkd.bustracker.ui.theme.BusTrackerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BusTrackerTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    var showBottomSheet by remember { mutableStateOf(true) }
    val context = LocalContext.current
    var locat by remember { mutableStateOf(LatLng(10.517147, 76.212787)) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(locat, 15f)
    }
    val routeCoordinates by remember {
        mutableStateOf(listOf(
            LatLng(10.512503, 76.220678),
            LatLng(9.901145, 76.712371),
        ))
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.statusBars.only(WindowInsetsSides.Top).asPaddingValues())
    ) {
        MapLayoutBox(locat, cameraPositionState, routeCoordinates)
        StatusIndicator(onClick = {
            showBottomSheet = !showBottomSheet
        })
        BottomSheet(showBottomSheet = showBottomSheet, onDismiss = { showBottomSheet = false }, onApplly = { s: String, s1: String ->
            var lat = s.split(",")
            locat = LatLng(lat[0].toDouble(),lat[1].toDouble())
            Toast.makeText(context, "Location Updated", Toast.LENGTH_SHORT).show()

        })
    }
}

@Composable
fun StatusIndicator(onClick:()->Unit) {
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
                text = "Click to open bottom sheet",
                modifier = Modifier.padding(16.dp)
            )

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(showBottomSheet: Boolean, onDismiss: () -> Unit, onApplly: (text:String,routes:String) -> Unit ) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    if (showBottomSheet) {
        ModalBottomSheet(
            modifier = Modifier
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
            onDismissRequest = onDismiss,
            sheetState = sheetState
        ) {
            // Sheet content
            //TextFile For Bus Location
            var text by remember { mutableStateOf("") }
            var routesText by remember { mutableStateOf("") }
            TextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Enter Bus Location") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(16.dp)
            )
            TextField(
                value = routesText,
                onValueChange = { routesText = it },
                label = { Text("Enter Bus Routes") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(16.dp)
            )
            //Aplly Button
            Button(
                onClick = {
                    //
                    onApplly(text,routesText)
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            onDismiss()
                        }
                    }
                }
                ,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Apply")
            }
        }
    }
}

@Composable
fun MapLayoutBox(
    locat: LatLng,
    cameraPositionState: CameraPositionState,
    routeCoordinates: List<LatLng>
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ){
            MarkerComposable(
                state = MarkerState(position = locat),
            ) {
                Image(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width(50.dp)
                        .height(50.dp),
                    painter = painterResource(id = R.drawable.bus_station),
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BusTrackerTheme {
        MainScreen()
    }
}