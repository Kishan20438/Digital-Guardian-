package com.example.childtracking

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.childtracking.models.LocationData
import org.json.JSONArray
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors

class LiveLocationActivity : AppCompatActivity() {

    private lateinit var mapWebView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private lateinit var dateTimeTextView: TextView
    private lateinit var addressTextView: TextView
    private lateinit var coordinatesTextView: TextView
    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_location)

        // Set up the toolbar
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Live Location"
        }

        // Initialize views
        mapWebView = findViewById(R.id.mapWebView)
        progressBar = findViewById(R.id.progressBar)
        errorTextView = findViewById(R.id.errorTextView)
        dateTimeTextView = findViewById(R.id.dateTimeTextView)
        addressTextView = findViewById(R.id.addressTextView)
        coordinatesTextView = findViewById(R.id.coordinatesTextView)

        // Configure WebView
        setupWebView()
        
        // Check if we have a specific location to display from intent
        val hasSpecificLocation = intent.hasExtra("LATITUDE") && intent.hasExtra("LONGITUDE")
        
        if (hasSpecificLocation) {
            // Display the specific location passed from LocationHistoryActivity
            displaySpecificLocation()
        } else {
            // Load latest location data from API
            loadLocationData()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        mapWebView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
        }
        
        mapWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.visibility = View.GONE
                super.onPageFinished(view, url)
            }
        }
    }

    private fun loadLocationData() {
        showLoading(true)

        executor.execute {
            try {
                // Get current timestamp for cache busting
                val timestamp = System.currentTimeMillis()
                val apiUrl = "https://mobile-tracker-free.com/dashboard/scripts/locations/getLast15GPS.php?_=$timestamp"
                
                val url = URL(apiUrl)
                val connection = url.openConnection()
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                
                val responseText = connection.getInputStream().bufferedReader().use { it.readText() }
                
                // If the response is empty or null, use sample data
                val finalResponse = if (responseText.isBlank() || responseText == "null") {
                    sampleLocationData
                } else {
                    responseText
                }
                
                // Parse the response JSON
                val locationsList = parseLocationData(finalResponse)
                
                // Update UI on main thread
                runOnUiThread {
                    if (locationsList.isEmpty()) {
                        showError("No location data available")
                    } else {
                        // Get the most recent location (first item in the list)
                        val currentLocation = locationsList.first()
                        displayLocationOnMap(currentLocation)
                        displayLocationDetails(currentLocation)
                        showLoading(false)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    showError("Error loading location data: ${e.message}")
                }
            }
        }
    }

    private fun parseLocationData(jsonString: String): List<LocationData> {
        val locationsList = mutableListOf<LocationData>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val locationData = LocationData(
                    latitude = jsonObject.getString("latitude"),
                    longitude = jsonObject.getString("longitude"),
                    accuracy = jsonObject.getString("accuracy"),
                    date = jsonObject.getString("date"),
                    address = jsonObject.getString("address")
                )
                locationsList.add(locationData)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return locationsList
    }

    private fun displayLocationOnMap(location: LocationData) {
        try {
            val latitude = location.latitude
            val longitude = location.longitude
            
            // Create HTML with embedded OSM map that works better on mobile
            val mapHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
                    <style>
                        body, html, #map { width: 100%; height: 100%; margin: 0; padding: 0; }
                    </style>
                </head>
                <body>
                    <div id="map" style="width: 100%; height: 100%;">
                        <iframe 
                            width="100%" 
                            height="100%" 
                            frameborder="0" 
                            scrolling="no" 
                            marginheight="0" 
                            marginwidth="0" 
                            src="https://www.openstreetmap.org/export/embed.html?bbox=${longitude.toDouble()-0.005},${latitude.toDouble()-0.005},${longitude.toDouble()+0.005},${latitude.toDouble()+0.005}&amp;layer=mapnik&amp;marker=$latitude,$longitude">
                        </iframe>
                    </div>
                </body>
                </html>
            """.trimIndent()
            
            // Load the HTML directly
            mapWebView.loadDataWithBaseURL(
                "https://www.openstreetmap.org/",
                mapHtml,
                "text/html",
                "UTF-8",
                null
            )
            
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Error displaying location on map: ${e.message}")
        }
    }

    private fun displayLocationDetails(location: LocationData) {
        try {
            // Format date for display
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMMM d, yyyy HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(location.date)
            val formattedDate = if (date != null) outputFormat.format(date) else location.date

            // Update UI with location details
            dateTimeTextView.text = formattedDate
            addressTextView.text = location.address
            coordinatesTextView.text = "Lat: ${location.latitude}, Long: ${location.longitude} (Accuracy: ${location.accuracy})"
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        errorTextView.visibility = View.GONE
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        errorTextView.visibility = View.VISIBLE
        errorTextView.text = message
    }

    private fun displaySpecificLocation() {
        try {
            val latitude = intent.getStringExtra("LATITUDE") ?: ""
            val longitude = intent.getStringExtra("LONGITUDE") ?: ""
            val accuracy = intent.getStringExtra("ACCURACY") ?: ""
            val date = intent.getStringExtra("DATE") ?: ""
            val address = intent.getStringExtra("ADDRESS") ?: ""
            
            // Create a LocationData object
            val locationData = LocationData(
                latitude = latitude,
                longitude = longitude,
                accuracy = accuracy,
                date = date,
                address = address
            )
            
            // Display on map and in details section
            displayLocationOnMap(locationData)
            displayLocationDetails(locationData)
            showLoading(false)
            
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Error displaying selected location: ${e.message}")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    
    // Sample data in case the API returns nothing
    private val sampleLocationData = """
        [
            {"latitude":"12.9393021","longitude":"80.2404465","accuracy":"100.0 m","date":"2025-03-17 00:57:44","address":"3/201, Mettukuppam, Chennai, Kotivakkam, Tamil Nadu 600097, India"},
            {"latitude":"12.9393011","longitude":"80.2404439","accuracy":"100.0 m","date":"2025-03-16 22:57:44","address":"3/201, Mettukuppam, Chennai, Kotivakkam, Tamil Nadu 600097, India"},
            {"latitude":"12.9392917","longitude":"80.2404538","accuracy":"100.0 m","date":"2025-03-16 20:57:36","address":"3/201, Mettukuppam, Chennai, Kotivakkam, Tamil Nadu 600097, India"}
        ]
    """.trimIndent()
} 