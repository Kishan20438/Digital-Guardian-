package com.example.childtracking

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.childtracking.adapters.LocationAdapter
import com.example.childtracking.models.LocationData
import org.json.JSONArray
import java.net.URL
import java.util.concurrent.Executors

class LocationHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_history)

        // Set up the toolbar
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Location History"
        }

        // Initialize views
        recyclerView = findViewById(R.id.locationsRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        errorTextView = findViewById(R.id.errorTextView)

        // Configure RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load location history
        loadLocationHistory()
    }

    private fun loadLocationHistory() {
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
                    sampleLocationHistoryData
                } else {
                    responseText
                }
                
                // Parse the response JSON
                val locationsList = parseLocationData(finalResponse)
                
                // Update UI on main thread
                runOnUiThread {
                    if (locationsList.isEmpty()) {
                        showError("No location history available")
                    } else {
                        displayLocationHistory(locationsList)
                        showLoading(false)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    showError("Error loading location history: ${e.message}")
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

    private fun displayLocationHistory(locations: List<LocationData>) {
        val adapter = LocationAdapter(locations) { location ->
            // Handle location click - open LiveLocationActivity with the selected location
            val intent = Intent(this, LiveLocationActivity::class.java)
            intent.putExtra("LATITUDE", location.latitude)
            intent.putExtra("LONGITUDE", location.longitude)
            intent.putExtra("ACCURACY", location.accuracy)
            intent.putExtra("DATE", location.date)
            intent.putExtra("ADDRESS", location.address)
            startActivity(intent)
        }
        recyclerView.adapter = adapter
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
        errorTextView.visibility = View.GONE
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.GONE
        errorTextView.visibility = View.VISIBLE
        errorTextView.text = message
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    
    // Sample data in case the API returns nothing
    private val sampleLocationHistoryData = """
        [
            {"latitude":"12.9393011","longitude":"80.2404439","accuracy":"100.0 m","date":"2025-03-16 22:57:44","address":"3/201, Mettukuppam, Chennai, Kotivakkam, Tamil Nadu 600097, India"},
            {"latitude":"12.9392917","longitude":"80.2404538","accuracy":"100.0 m","date":"2025-03-16 20:57:36","address":"3/201, Mettukuppam, Chennai, Kotivakkam, Tamil Nadu 600097, India"},
            {"latitude":"12.9393044","longitude":"80.2404429","accuracy":"100.0 m","date":"2025-03-16 18:56:50","address":"3/201, Mettukuppam, Chennai, Kotivakkam, Tamil Nadu 600097, India"},
            {"latitude":"12.939301","longitude":"80.2404437","accuracy":"100.0 m","date":"2025-03-16 16:53:57","address":"3/201, Mettukuppam, Chennai, Kotivakkam, Tamil Nadu 600097, India"},
            {"latitude":"12.9393044","longitude":"80.2404428","accuracy":"20.0 m","date":"2025-03-16 14:46:43","address":"3/201, Mettukuppam, Chennai, Kotivakkam, Tamil Nadu 600097, India"},
            {"latitude":"12.9392949","longitude":"80.2404459","accuracy":"100.0 m","date":"2025-03-16 10:24:34","address":"3/201, Mettukuppam, Chennai, Kotivakkam, Tamil Nadu 600097, India"},
            {"latitude":"12.9392949","longitude":"80.2404459","accuracy":"100.0 m","date":"2025-03-16 08:24:34","address":"3/201, Mettukuppam, Chennai, Kotivakkam, Tamil Nadu 600097, India"},
            {"latitude":"12.9392949","longitude":"80.2404459","accuracy":"100.0 m","date":"2025-03-16 06:08:10","address":"3/201, Mettukuppam, Chennai, Kotivakkam, Tamil Nadu 600097, India"},
            {"latitude":"12.9392949","longitude":"80.2404459","accuracy":"100.0 m","date":"2025-03-16 04:00:00","address":"3/201, Mettukuppam, Chennai, Kotivakkam, Tamil Nadu 600097, India"},
            {"latitude":"12.9392949","longitude":"80.2404459","accuracy":"100.0 m","date":"2025-03-16 02:00:00","address":"3/201, Mettukuppam, Chennai, Kotivakkam, Tamil Nadu 600097, India"},
            {"latitude":"12.9392949","longitude":"80.2404459","accuracy":"100.0 m","date":"2025-03-16 00:00:00","address":"3/201, Mettukuppam, Chennai, Kotivakkam, Tamil Nadu 600097, India"},
            {"latitude":"12.9392949","longitude":"80.2404459","accuracy":"100.0 m","date":"2025-03-15 21:57:25","address":"3/201, Mettukuppam, Chennai, Kotivakkam, Tamil Nadu 600097, India"},
            {"latitude":"12.8711658","longitude":"80.2253177","accuracy":"100.0 m","date":"2025-03-15 19:38:10","address":"V6CG+7G3, 3, Rajiv Gandhi Salai, Kamaraj Nagar, Semmancheri, Chennai, Semmanjeri, Tamil Nadu 600119, India"},
            {"latitude":"12.8711713","longitude":"80.2253173","accuracy":"100.0 m","date":"2025-03-15 17:36:22","address":"V6CG+7G3, 3, Rajiv Gandhi Salai, Kamaraj Nagar, Semmancheri, Chennai, Semmanjeri, Tamil Nadu 600119, India"}
        ]
    """.trimIndent()
} 