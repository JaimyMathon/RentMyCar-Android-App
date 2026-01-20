package com.example.rentmycar_android_app.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.res.stringResource
import com.example.rentmycar_android_app.R
import androidx.core.content.ContextCompat
import com.example.rentmycar_android_app.network.RoutingService
import com.example.rentmycar_android_app.network.OSRMClient
import com.google.android.gms.location.LocationServices
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.LocationComponentOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.camera.CameraUpdateFactory
import retrofit2.Callback
import retrofit2.Response
import com.google.gson.JsonArray
import com.google.gson.JsonObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavigateBack: () -> Unit,
    carLatitude: Double,
    carLongitude: Double
) {
    val context = LocalContext.current
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

    // Request location permission on first launch
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.route_to_car)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
                MapLibreMapView(
                    carLatitude = carLatitude,
                    carLongitude = carLongitude
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.location_permission_required),
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
                        Text(stringResource(R.string.grant_permission))
                    }
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun MapLibreMapView(
    carLatitude: Double,
    carLongitude: Double
) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var isNavigating by remember { mutableStateOf(true) } // Route shown by default
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Pre-fetch string resources for use in callbacks
    val couldNotGetLocationStr = stringResource(R.string.could_not_get_location)
    val errorGettingLocationStr = stringResource(R.string.error_getting_location)
    val stopNavigationStr = stringResource(R.string.stop_navigation)
    val startNavigationStr = stringResource(R.string.start_navigation)
    val mapNotAvailableStr = stringResource(R.string.map_not_available)
    val mapStyleNotLoadedStr = stringResource(R.string.map_style_not_loaded)
    val mapNoLongerAvailableStr = stringResource(R.string.map_no_longer_available)
    val errorDrawingRouteStr = stringResource(R.string.error_drawing_route)
    val noRouteFoundStr = stringResource(R.string.no_route_found)
    val carLocationMarkerStr = stringResource(R.string.car_location_marker)
    val errorFetchingRouteStr = stringResource(R.string.error)
    val networkErrorStr = stringResource(R.string.network_error_message).substringBefore("%")

    DisposableEffect(Unit) {
        onDispose {
            try {
                mapView?.let { view ->
                    view.onStop()
                    view.onPause()
                    view.onDestroy()
                }
            } catch (e: Exception) {
                Log.e("MapLibre", "Error disposing map", e)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    mapView = this
                    onCreate(null)
                    onStart()
                    onResume()

                    getMapAsync { map ->
                        mapLibreMap = map

                        // Create a modern style with CartoDB Voyager tiles
                        val styleJson = """
                        {
                          "version": 8,
                          "name": "Modern Map",
                          "sources": {
                            "carto": {
                              "type": "raster",
                              "tiles": ["https://a.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}.png"],
                              "tileSize": 256,
                              "attribution": "© OpenStreetMap contributors, © CARTO",
                              "maxzoom": 19
                            }
                          },
                          "layers": [
                            {
                              "id": "carto-layer",
                              "type": "raster",
                              "source": "carto",
                              "minzoom": 0,
                              "maxzoom": 22
                            }
                          ]
                        }
                        """.trimIndent()

                        map.setStyle(
                            Style.Builder().fromJson(styleJson)
                        ) { style ->
                            // Enable location component
                            val locationComponentOptions = LocationComponentOptions.builder(ctx)
                                .pulseEnabled(true)
                                .build()

                            val locationComponentActivationOptions = LocationComponentActivationOptions
                                .builder(ctx, style)
                                .locationComponentOptions(locationComponentOptions)
                                .build()

                            map.locationComponent.apply {
                                activateLocationComponent(locationComponentActivationOptions)
                                isLocationComponentEnabled = true
                                cameraMode = CameraMode.TRACKING
                                renderMode = RenderMode.COMPASS
                            }

                            // Add car marker immediately
                            addCarMarker(style, carLatitude, carLongitude, carLocationMarkerStr)

                            // Get user location and fetch route automatically
                            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx)
                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                location?.let { userLoc ->
                                    // Automatically fetch and draw the route
                                    fetchAndDrawRouteOnLoad(
                                        map = map,
                                        style = style,
                                        userLatitude = userLoc.latitude,
                                        userLongitude = userLoc.longitude,
                                        carLatitude = carLatitude,
                                        carLongitude = carLongitude
                                    )
                                } ?: run {
                                    // No user location, center on car
                                    val carLatLng = LatLng(carLatitude, carLongitude)
                                    map.animateCamera(
                                        CameraUpdateFactory.newLatLngZoom(carLatLng, 14.0)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Navigate button
        FloatingActionButton(
            onClick = {
                isNavigating = !isNavigating
                if (isNavigating) {
                    // Fetch and display route
                    isLoading = true
                    errorMessage = null

                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let { userLoc ->
                            fetchAndDrawRoute(
                                mapLibreMap = mapLibreMap,
                                userLatitude = userLoc.latitude,
                                userLongitude = userLoc.longitude,
                                carLatitude = carLatitude,
                                carLongitude = carLongitude,
                                onSuccess = {
                                    isLoading = false
                                },
                                onError = { error ->
                                    isLoading = false
                                    errorMessage = error
                                },
                                mapNotAvailableMsg = mapNotAvailableStr,
                                mapStyleNotLoadedMsg = mapStyleNotLoadedStr,
                                mapNoLongerAvailableMsg = mapNoLongerAvailableStr,
                                errorDrawingRouteMsg = errorDrawingRouteStr,
                                noRouteFoundMsg = noRouteFoundStr,
                                errorFetchingRouteMsg = errorFetchingRouteStr,
                                networkErrorMsg = networkErrorStr
                            )
                        } ?: run {
                            isLoading = false
                            errorMessage = couldNotGetLocationStr
                        }
                    }.addOnFailureListener {
                        isLoading = false
                        errorMessage = errorGettingLocationStr
                    }
                } else {
                    // Clear route
                    clearRoute(mapLibreMap)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = if (isNavigating) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = if (isNavigating) stopNavigationStr else startNavigationStr
            )
        }

        // Loading indicator
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(50.dp)
            )
        }

        // Error message
        errorMessage?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { errorMessage = null }) {
                        Text(stringResource(R.string.close))
                    }
                }
            ) {
                Text(error)
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun fetchAndDrawRouteOnLoad(
    map: MapLibreMap,
    style: Style,
    userLatitude: Double,
    userLongitude: Double,
    carLatitude: Double,
    carLongitude: Double
) {
    val routingService = OSRMClient.instance.create(RoutingService::class.java)
    val coordinates = "${userLongitude},${userLatitude};${carLongitude},${carLatitude}"

    Log.d("MapLibre", "Fetching route for coordinates: $coordinates")

    routingService.getRoute(coordinates, "geojson", "full").enqueue(object : Callback<com.example.rentmycar_android_app.network.RouteResponse> {
        override fun onResponse(
            call: retrofit2.Call<com.example.rentmycar_android_app.network.RouteResponse>,
            response: Response<com.example.rentmycar_android_app.network.RouteResponse>
        ) {
            // Check if map is still valid before updating
            if (map.style == null) {
                Log.w("MapLibre", "Map style is null, skipping route update")
                return
            }

            if (response.isSuccessful && response.body() != null) {
                val routeData = response.body()!!
                if (routeData.routes.isNotEmpty()) {
                    val route = routeData.routes[0]

                    Log.d("MapLibre", "Route received, drawing line")

                    try {
                        // Add route line to map
                        val routeSource = GeoJsonSource("route-source", route.geometry.toString())
                        style.addSource(routeSource)

                        val routeLayer = LineLayer("route-layer", "route-source")
                            .withProperties(
                                lineColor("#0080FF"),
                                lineWidth(6f),
                                lineOpacity(0.9f),
                                lineCap("round"),
                                lineJoin("round")
                            )
                        style.addLayer(routeLayer)

                        // Adjust camera to show the entire route
                        val userLatLng = LatLng(userLatitude, userLongitude)
                        val carLatLng = LatLng(carLatitude, carLongitude)
                        val bounds = LatLngBounds.Builder()
                            .include(userLatLng)
                            .include(carLatLng)
                            .build()

                        map.easeCamera(
                            CameraUpdateFactory.newLatLngBounds(bounds, 100),
                            1000
                        )
                    } catch (e: Exception) {
                        Log.e("MapLibre", "Error drawing route", e)
                    }
                } else {
                    Log.e("MapLibre", "No routes found in response")
                    // Just center on both locations without route
                    val userLatLng = LatLng(userLatitude, userLongitude)
                    val carLatLng = LatLng(carLatitude, carLongitude)
                    val bounds = LatLngBounds.Builder()
                        .include(userLatLng)
                        .include(carLatLng)
                        .build()

                    map.easeCamera(
                        CameraUpdateFactory.newLatLngBounds(bounds, 100)
                    )
                }
            } else {
                Log.e("MapLibre", "Route response not successful: ${response.code()}")
                // Just center on both locations without route
                val userLatLng = LatLng(userLatitude, userLongitude)
                val carLatLng = LatLng(carLatitude, carLongitude)
                val bounds = LatLngBounds.Builder()
                    .include(userLatLng)
                    .include(carLatLng)
                    .build()

                map.easeCamera(
                    CameraUpdateFactory.newLatLngBounds(bounds, 100)
                )
            }
        }

        override fun onFailure(
            call: retrofit2.Call<com.example.rentmycar_android_app.network.RouteResponse>,
            t: Throwable
        ) {
            Log.e("MapLibre", "Route fetch failed", t)
            // Just center on both locations without route
            val userLatLng = LatLng(userLatitude, userLongitude)
            val carLatLng = LatLng(carLatitude, carLongitude)
            val bounds = LatLngBounds.Builder()
                .include(userLatLng)
                .include(carLatLng)
                .build()

            map.easeCamera(
                CameraUpdateFactory.newLatLngBounds(bounds, 100)
            )
        }
    })
}

@SuppressLint("MissingPermission")
private fun fetchAndDrawRoute(
    mapLibreMap: MapLibreMap?,
    userLatitude: Double,
    userLongitude: Double,
    carLatitude: Double,
    carLongitude: Double,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    mapNotAvailableMsg: String,
    mapStyleNotLoadedMsg: String,
    mapNoLongerAvailableMsg: String,
    errorDrawingRouteMsg: String,
    noRouteFoundMsg: String,
    errorFetchingRouteMsg: String,
    networkErrorMsg: String
) {
    val map = mapLibreMap ?: run {
        onError(mapNotAvailableMsg)
        return
    }

    val style = map.style ?: run {
        onError(mapStyleNotLoadedMsg)
        return
    }

    val routingService = OSRMClient.instance.create(RoutingService::class.java)
    val coordinates = "${userLongitude},${userLatitude};${carLongitude},${carLatitude}"

    Log.d("MapLibre", "Fetching route for coordinates: $coordinates")

    routingService.getRoute(coordinates, "geojson", "full").enqueue(object : Callback<com.example.rentmycar_android_app.network.RouteResponse> {
        override fun onResponse(
            call: retrofit2.Call<com.example.rentmycar_android_app.network.RouteResponse>,
            response: Response<com.example.rentmycar_android_app.network.RouteResponse>
        ) {
            // Check if map is still valid before updating
            if (map.style == null) {
                onError(mapNoLongerAvailableMsg)
                return
            }

            if (response.isSuccessful && response.body() != null) {
                val routeData = response.body()!!
                if (routeData.routes.isNotEmpty()) {
                    val route = routeData.routes[0]

                    Log.d("MapLibre", "Route received with ${route.geometry} geometry")

                    try {
                        // Clear existing route if any
                        clearRoute(mapLibreMap)

                        // Add route line to map
                        val routeSource = GeoJsonSource("route-source", route.geometry.toString())
                        style.addSource(routeSource)

                        val routeLayer = LineLayer("route-layer", "route-source")
                            .withProperties(
                                lineColor("#0080FF"),
                                lineWidth(6f),
                                lineOpacity(0.9f),
                                lineCap("round"),
                                lineJoin("round")
                            )
                        style.addLayer(routeLayer)

                        // Adjust camera to show the entire route
                        val userLatLng = LatLng(userLatitude, userLongitude)
                        val carLatLng = LatLng(carLatitude, carLongitude)
                        val bounds = LatLngBounds.Builder()
                            .include(userLatLng)
                            .include(carLatLng)
                            .build()

                        map.easeCamera(
                            CameraUpdateFactory.newLatLngBounds(bounds, 100),
                            1000
                        )

                        onSuccess()
                    } catch (e: Exception) {
                        Log.e("MapLibre", "Error drawing route", e)
                        onError(errorDrawingRouteMsg)
                    }
                } else {
                    Log.e("MapLibre", "No routes found in response")
                    onError(noRouteFoundMsg)
                }
            } else {
                Log.e("MapLibre", "Route response not successful: ${response.code()}")
                onError("$errorFetchingRouteMsg ${response.code()}")
            }
        }

        override fun onFailure(
            call: retrofit2.Call<com.example.rentmycar_android_app.network.RouteResponse>,
            t: Throwable
        ) {
            Log.e("MapLibre", "Route fetch failed", t)
            onError("$networkErrorMsg ${t.message}")
        }
    })
}

private fun clearRoute(mapLibreMap: MapLibreMap?) {
    val map = mapLibreMap ?: return
    val style = map.style ?: return

    try {
        // Remove route layer and source if they exist
        style.getLayer("route-layer")?.let {
            style.removeLayer("route-layer")
        }
        style.getSource("route-source")?.let {
            style.removeSource("route-source")
        }
    } catch (e: Exception) {
        Log.e("MapLibre", "Error clearing route", e)
    }
}

private fun addCarMarker(
    style: Style,
    carLatitude: Double,
    carLongitude: Double,
    markerTitle: String
) {
    // Create a GeoJSON feature for the car marker
    val carFeature = JsonObject().apply {
        addProperty("type", "Feature")
        val geometry = JsonObject().apply {
            addProperty("type", "Point")
            val coordinates = JsonArray().apply {
                add(carLongitude)
                add(carLatitude)
            }
            add("coordinates", coordinates)
        }
        add("geometry", geometry)

        val properties = JsonObject().apply {
            addProperty("title", markerTitle)
        }
        add("properties", properties)
    }

    val carSource = GeoJsonSource("car-marker-source", carFeature.toString())
    style.addSource(carSource)

    // Use a circle layer for the car marker
    val markerLayer = CircleLayer("car-marker-layer", "car-marker-source")
        .withProperties(
            circleRadius(12f),
            circleColor("#FF0000"),
            circleStrokeWidth(3f),
            circleStrokeColor("#FFFFFF")
        )
    style.addLayer(markerLayer)
}
