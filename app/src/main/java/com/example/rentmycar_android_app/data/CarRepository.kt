package com.example.rentmycar_android_app.data

import android.content.Context
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.util.Log
import com.example.rentmycar_android_app.network.ApiClientWithToken
import com.example.rentmycar_android_app.network.CarUploadService
import com.example.rentmycar_android_app.network.SimpleResponseDto
import com.example.rentmycar_android_app.util.uriToTempFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

class CarRepository(private val context: Context) {

    private val retrofit = ApiClientWithToken(context).instance
    private val uploadService = retrofit.create(CarUploadService::class.java)

    // HTTP client voor Nominatim
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Probeert meerdere geocoding methodes en adres formaten
     */
    private suspend fun geocodeAddress(
        street: String,
        houseNumber: String,
        postalCode: String,
        city: String,
        country: String
    ): Pair<Double, Double> = withContext(Dispatchers.IO) {

        // Verschillende adres formaten om te proberen
        val addressFormats = listOf(
            "$street $houseNumber, $postalCode $city, $country",
            "$street $houseNumber, $city, $country",
            "$postalCode, $city, $country",
            "$city, $country"
        )

        // Probeer Android Geocoder eerst met verschillende formaten
        for (format in addressFormats) {
            Log.d("CarRepository", "Trying Android Geocoder with: $format")
            val result = tryAndroidGeocoder(format)
            if (result != null) {
                Log.d("CarRepository", "SUCCESS Android Geocoder: $result")
                return@withContext result
            }
        }

        // Probeer Nominatim met gestructureerde query (meest betrouwbaar)
        Log.d("CarRepository", "Trying Nominatim structured query")
        val nominatimStructured = tryNominatimStructured(street, houseNumber, postalCode, city, country)
        if (nominatimStructured != null) {
            Log.d("CarRepository", "SUCCESS Nominatim structured: $nominatimStructured")
            return@withContext nominatimStructured
        }

        // Probeer Nominatim met vrije tekst query
        for (format in addressFormats) {
            Log.d("CarRepository", "Trying Nominatim free text: $format")
            val result = tryNominatimFreeText(format)
            if (result != null) {
                Log.d("CarRepository", "SUCCESS Nominatim free text: $result")
                return@withContext result
            }
        }

        // Als alles faalt, gebruik default coördinaten voor Nederland (Amsterdam)
        // en log een warning
        Log.w("CarRepository", "All geocoding failed, using default Amsterdam coordinates")
        return@withContext 52.3676 to 4.9041  // Amsterdam centrum als fallback
    }

    private suspend fun tryAndroidGeocoder(address: String): Pair<Double, Double>? {
        return try {
            withTimeoutOrNull(5000L) {
                val geocoder = Geocoder(context, Locale("nl", "NL"))

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { cont ->
                        geocoder.getFromLocationName(address, 1) { results ->
                            val first = results.firstOrNull()
                            if (first != null) {
                                cont.resume(first.latitude to first.longitude)
                            } else {
                                cont.resume(null)
                            }
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val results = geocoder.getFromLocationName(address, 1)
                    results?.firstOrNull()?.let { it.latitude to it.longitude }
                }
            }
        } catch (e: Exception) {
            Log.w("CarRepository", "Android Geocoder failed: ${e.message}")
            null
        }
    }

    /**
     * Nominatim met gestructureerde query parameters (meest betrouwbaar)
     */
    private fun tryNominatimStructured(
        street: String,
        houseNumber: String,
        postalCode: String,
        city: String,
        country: String
    ): Pair<Double, Double>? {
        return try {
            val url = buildString {
                append("https://nominatim.openstreetmap.org/search?")
                append("street=${URLEncoder.encode("$houseNumber $street", "UTF-8")}")
                append("&city=${URLEncoder.encode(city, "UTF-8")}")
                append("&postalcode=${URLEncoder.encode(postalCode, "UTF-8")}")
                append("&country=${URLEncoder.encode(country, "UTF-8")}")
                append("&format=json&limit=1")
            }

            Log.d("CarRepository", "Nominatim structured URL: $url")
            executeNominatimRequest(url)
        } catch (e: Exception) {
            Log.w("CarRepository", "Nominatim structured failed: ${e.message}")
            null
        }
    }

    /**
     * Nominatim met vrije tekst query
     */
    private fun tryNominatimFreeText(address: String): Pair<Double, Double>? {
        return try {
            val encoded = URLEncoder.encode(address, "UTF-8")
            val url = "https://nominatim.openstreetmap.org/search?q=$encoded&format=json&limit=1&countrycodes=nl"

            Log.d("CarRepository", "Nominatim free text URL: $url")
            executeNominatimRequest(url)
        } catch (e: Exception) {
            Log.w("CarRepository", "Nominatim free text failed: ${e.message}")
            null
        }
    }

    private fun executeNominatimRequest(url: String): Pair<Double, Double>? {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "RentMyCar-Android-App/1.0 (contact@example.com)")
            .header("Accept", "application/json")
            .get()
            .build()

        val response = httpClient.newCall(request).execute()
        val body = response.body?.string()

        Log.d("CarRepository", "Nominatim response: $body")

        if (body.isNullOrBlank()) return null

        val jsonArray = JSONArray(body)
        if (jsonArray.length() > 0) {
            val first = jsonArray.getJSONObject(0)
            val lat = first.getString("lat").toDouble()
            val lon = first.getString("lon").toDouble()
            return lat to lon
        }
        return null
    }

    suspend fun addCarWithPhoto(
        brand: String,
        model: String,
        licensePlate: String,
        category: String,
        pricePerTimeSlot: String,
        costPerKm: String,
        country: String,
        city: String,
        postalCode: String,
        street: String,
        houseNumber: String,
        photoUri: Uri
    ): SimpleResponseDto {
        Log.d("CarRepository", "=== addCarWithPhoto STARTED ===")
        Log.d("CarRepository", "Input: brand=$brand, model=$model, licensePlate=$licensePlate")
        Log.d("CarRepository", "Address: $street $houseNumber, $postalCode $city, $country")

        // Geocode het adres naar lat/lon
        Log.d("CarRepository", "Starting geocoding...")
        val (lat, lon) = geocodeAddress(street, houseNumber, postalCode, city, country)

        Log.d("CarRepository", "Final coordinates: lat=$lat, lon=$lon")

        val textPlain = "text/plain".toMediaType()

        Log.d("CarRepository", "Converting photo URI to temp file: $photoUri")
        val imageFile = uriToTempFile(context, photoUri)
        Log.d("CarRepository", "Temp file created: ${imageFile.absolutePath}, size=${imageFile.length()}")

        val imagePart = MultipartBody.Part.createFormData(
            name = "image",
            filename = imageFile.name,
            body = imageFile.asRequestBody("image/*".toMediaType())
        )

        Log.d("CarRepository", "Calling uploadService.addCar()...")
        val response = uploadService.addCar(
            brand = brand.toRequestBody(textPlain),
            model = model.toRequestBody(textPlain),
            licensePlate = licensePlate.toRequestBody(textPlain),
            category = category.toRequestBody(textPlain),
            pricePerTimeSlot = pricePerTimeSlot.toRequestBody(textPlain),
            costPerKm = costPerKm.toRequestBody(textPlain),
            latitude = lat.toString().toRequestBody(textPlain),
            longitude = lon.toString().toRequestBody(textPlain),
            image = imagePart
        )
        Log.d("CarRepository", "uploadService.addCar() returned: isSuccess=${response.isSuccess}, message=${response.message}")
        return response
    }
}