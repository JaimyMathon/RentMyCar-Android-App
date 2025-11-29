package com.example.rentmycar_android_app.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                            .edit()
                            .clear()
                            .apply()
                        onNavigateBack()
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Find Cars Nearby") },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (hasLocationPermission) {
                OpenStreetMapView()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Location permission is required to show the map",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    ) {
                        Text("Grant Permission")
                    }
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun OpenStreetMapView() {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    val destinationPoint = GeoPoint(51.58797364955005, 4.785664954090211)

    LaunchedEffect(Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                Log.d("MapScreen", "Last location: lat=${it.latitude}, lon=${it.longitude}")
                userLocation = GeoPoint(it.latitude, it.longitude)
                mapView?.controller?.setCenter(userLocation)
            }
        }

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location ->
            location?.let {
                Log.d("MapScreen", "Current location: lat=${it.latitude}, lon=${it.longitude}")
                userLocation = GeoPoint(it.latitude, it.longitude)
                mapView?.controller?.animateTo(userLocation)
            }
        }
    }

    LaunchedEffect(userLocation) {
        userLocation?.let { startPoint ->
            mapView?.let { map ->
                val routePoints = fetchRoute(startPoint, destinationPoint)
                drawRouteWithPoints(map, routePoints)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                Configuration.getInstance().userAgentValue = ctx.packageName
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(14.0)

                    MyLocationNewOverlay(GpsMyLocationProvider(ctx), this).apply {
                        enableMyLocation()
                        overlays.add(this)
                    }

                    val destinationMarker = Marker(this).apply {
                        position = destinationPoint
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "Destination"
                        snippet = "Lat: ${destinationPoint.latitude}, Lon: ${destinationPoint.longitude}"
                    }
                    overlays.add(destinationMarker)

                    mapView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        userLocation?.let { location ->
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Your Location", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Lat: ${String.format("%.6f", location.latitude)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "Lon: ${String.format("%.6f", location.longitude)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = {
                userLocation?.let {
                    mapView?.controller?.animateTo(it)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.LocationOn, "Recenter")
        }
    }
}

suspend fun fetchRoute(start: GeoPoint, end: GeoPoint): List<GeoPoint> = withContext(Dispatchers.IO) {
    try {
        val url = "https://router.project-osrm.org/route/v1/driving/" +
                "${start.longitude},${start.latitude};${end.longitude},${end.latitude}" +
                "?overview=full&geometries=geojson"

        val response = URL(url).readText()
        val json = JSONObject(response)

        if (json.getString("code") == "Ok") {
            val routes = json.getJSONArray("routes")
            val geometry = routes.getJSONObject(0)
                .getJSONObject("geometry")
                .getJSONArray("coordinates")

            val points = mutableListOf<GeoPoint>()
            for (i in 0 until geometry.length()) {
                val coord = geometry.getJSONArray(i)
                points.add(GeoPoint(coord.getDouble(1), coord.getDouble(0)))
            }
            points
        } else {
            Log.e("MapScreen", "OSRM Error: ${json.getString("code")}")
            listOf(start, end)
        }
    } catch (e: Exception) {
        Log.e("MapScreen", "Failed to fetch route: ${e.message}")
        listOf(start, end)
    }
}

fun drawRouteWithPoints(map: MapView, points: List<GeoPoint>) {
    map.overlays.removeAll { it is Polyline }

    val routeLine = Polyline().apply {
        setPoints(points)
        outlinePaint.color = Color.Blue.toArgb()
        outlinePaint.strokeWidth = 10f
    }

    map.overlays.add(routeLine)
    map.invalidate()
}
