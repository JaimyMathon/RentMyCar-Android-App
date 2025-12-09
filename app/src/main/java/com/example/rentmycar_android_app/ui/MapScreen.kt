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
import com.example.rentmycar_android_app.network.ApiClientWithToken
import com.example.rentmycar_android_app.network.CarService
import com.example.rentmycar_android_app.network.GeocodingService
import com.example.rentmycar_android_app.network.NominatimClient
import com.example.rentmycar_android_app.network.OSRMClient
import com.example.rentmycar_android_app.network.RoutingService
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import org.osmdroid.config.Configuration
import retrofit2.Callback
import retrofit2.Response
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
    onNavigateBack: () -> Unit,
    carId: String = "692f09ee1d8fa80521492f32"
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
                title = { Text("Find Cars Nearby") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (hasLocationPermission) {
                OpenStreetMapView(carId = carId)
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

fun fetchAddressForMarker(
    lat: Double,
    lon: Double,
    onAddressFound: (String) -> Unit
) {
    val geocodingService = NominatimClient.instance.create(GeocodingService::class.java)
    geocodingService.reverseGeocode(lat, lon).enqueue(object : Callback<com.example.rentmycar_android_app.network.GeocodingResponse> {
        override fun onResponse(call: retrofit2.Call<com.example.rentmycar_android_app.network.GeocodingResponse>, response: Response<com.example.rentmycar_android_app.network.GeocodingResponse>) {
            if (response.isSuccessful && response.body() != null) {
                val geoData = response.body()!!

                // Try display_name first (more reliable)
                if (!geoData.display_name.isNullOrEmpty()) {
                    Log.d("GeocodingDebug", "Got display_name: ${geoData.display_name}")
                    onAddressFound(geoData.display_name)
                    return
                }

                // Fall back to formatted address
                val formattedAddress = geoData.address?.toFullAddress()
                if (!formattedAddress.isNullOrEmpty()) {
                    Log.d("GeocodingDebug", "Got formatted address: $formattedAddress")
                    onAddressFound(formattedAddress)
                    return
                }

                Log.d("GeocodingDebug", "No address found in response: $geoData")
            } else {
                Log.d("GeocodingDebug", "Response not successful: ${response.code()}")
            }

            onAddressFound("Address not available")
        }

        override fun onFailure(call: retrofit2.Call<com.example.rentmycar_android_app.network.GeocodingResponse>, t: Throwable) {
            Log.d("GeocodingDebug", "Network error: ${t.message}")
            onAddressFound("Address not available")
        }
    })
}

@SuppressLint("MissingPermission")
@Composable
fun OpenStreetMapView(carId: String) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }

    LaunchedEffect(Unit) {
        // Initialize osmdroid configuration
        Configuration.getInstance().apply {
            userAgentValue = context.packageName
            osmdroidBasePath = context.getDir("osmdroid", android.content.Context.MODE_PRIVATE)
            osmdroidTileCache = context.getDir("osmdroid_tile_cache", android.content.Context.MODE_PRIVATE)
        }
    }

    LaunchedEffect(mapView) {
        mapView?.let { map ->
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val userLocation = GeoPoint(it.latitude, it.longitude)

                    // Fetch car data
                    val apiClientWithToken = ApiClientWithToken(context)
                    val carService = apiClientWithToken.instance.create(CarService::class.java)
                    carService.getCar(carId).enqueue(object : Callback<com.example.rentmycar_android_app.network.CarResponse> {
                        override fun onResponse(call: retrofit2.Call<com.example.rentmycar_android_app.network.CarResponse>, response: Response<com.example.rentmycar_android_app.network.CarResponse>) {
                            if (response.isSuccessful && response.body() != null) {
                                val car = response.body()!!
                                val carLocation = GeoPoint(car.latitude, car.longitude)

                                // Fetch actual route using OSRM
                                val routingService = OSRMClient.instance.create(RoutingService::class.java)
                                val coordinates = "${it.longitude},${it.latitude};${car.longitude},${car.latitude}"
                                routingService.getRoute(coordinates, "geojson", "full").enqueue(object : Callback<com.example.rentmycar_android_app.network.RouteResponse> {
                                    override fun onResponse(call: retrofit2.Call<com.example.rentmycar_android_app.network.RouteResponse>, response: Response<com.example.rentmycar_android_app.network.RouteResponse>) {
                                        if (response.isSuccessful && response.body() != null) {
                                            val routeData = response.body()!!
                                            if (routeData.routes.isNotEmpty()) {
                                                val route = routeData.routes[0]
                                                val routePoints = mutableListOf<GeoPoint>()

                                                // Extract coordinates from GeoJSON geometry
                                                val geometry = route.geometry
                                                val coordinates = geometry.getAsJsonArray("coordinates")

                                                for (coord in coordinates) {
                                                    val coordArray = coord.asJsonArray
                                                    val lon = coordArray.get(0).asDouble
                                                    val lat = coordArray.get(1).asDouble
                                                    routePoints.add(GeoPoint(lat, lon))
                                                }

                                                if (routePoints.isNotEmpty()) {
                                                    // Draw route polyline
                                                    val routeLine = Polyline().apply {
                                                        setPoints(routePoints)
                                                        outlinePaint.color = android.graphics.Color.BLUE
                                                        outlinePaint.strokeWidth = 8f
                                                    }
                                                    map.overlays.add(routeLine)

                                                    // Fetch address using reverse geocoding
                                                    fetchAddressForMarker(car.latitude, car.longitude) { address ->
                                                        val carMarker = Marker(map).apply {
                                                            position = carLocation
                                                            title = "Car Location"
                                                            subDescription = address
                                                        }
                                                        map.overlays.add(carMarker)
                                                        map.invalidate()
                                                    }

                                                    // Center map between user and car
                                                    val centerLat = (it.latitude + car.latitude) / 2
                                                    val centerLon = (it.longitude + car.longitude) / 2
                                                    map.controller.setCenter(GeoPoint(centerLat, centerLon))
                                                    map.controller.setZoom(16.0)
                                                    map.invalidate()
                                                }
                                            }
                                        }
                                    }

                                    override fun onFailure(call: retrofit2.Call<com.example.rentmycar_android_app.network.RouteResponse>, t: Throwable) {
                                        // Fallback: draw straight line if routing fails
                                        val routePoints = listOf(userLocation, carLocation)
                                        val routeLine = Polyline().apply {
                                            setPoints(routePoints)
                                            outlinePaint.color = android.graphics.Color.RED
                                            outlinePaint.strokeWidth = 5f
                                        }
                                        map.overlays.add(routeLine)

                                        // Fetch address using reverse geocoding
                                        fetchAddressForMarker(car.latitude, car.longitude) { address ->
                                            val carMarker = Marker(map).apply {
                                                position = carLocation
                                                title = "Car Location"
                                                subDescription = address
                                            }
                                            map.overlays.add(carMarker)
                                            map.invalidate()
                                        }

                                        val centerLat = (it.latitude + car.latitude) / 2
                                        val centerLon = (it.longitude + car.longitude) / 2
                                        map.controller.setCenter(GeoPoint(centerLat, centerLon))
                                        map.controller.setZoom(16.0)
                                        map.invalidate()
                                    }
                                })
                            }
                        }

                        override fun onFailure(call: retrofit2.Call<com.example.rentmycar_android_app.network.CarResponse>, t: Throwable) {
                            // Fallback: just show user location if car fetch fails
                            map.controller.setCenter(userLocation)
                            map.controller.setZoom(15.0)
                        }
                    })
                }
            }
        }
    }

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(14.0)

                // Add location overlay
                val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                locationOverlay.enableMyLocation()
                overlays.add(locationOverlay)

                mapView = this
            }
        },
        modifier = Modifier.fillMaxSize()
    )
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
