package com.example.childtracking

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.childtracking.adapters.AppUsageAdapter
import com.example.childtracking.models.AppUsageData
import org.json.JSONObject
import java.net.URL
import java.util.concurrent.Executors

class AppUsageActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private val executor = Executors.newSingleThreadExecutor()
    private val TAG = "AppUsageActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_usage)

        // Set up the toolbar
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "App Usage"
        }

        // Initialize views
        recyclerView = findViewById(R.id.appsRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        errorTextView = findViewById(R.id.errorTextView)

        // Configure RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load app usage data - directly use sample data first
        displaySampleData()
    }

    private fun displaySampleData() {
        showLoading(true)
        try {
            val appList = parseAppUsageData(sampleAppUsageData)
            if (appList.isEmpty()) {
                showError("No app usage data available")
            } else {
                displayAppUsageData(appList)
                showLoading(false)
                
                // Show toast to indicate we're using sample data
                Toast.makeText(this, "Displaying sample app data", Toast.LENGTH_SHORT).show()
                
                // Try loading real data in background
                loadAppUsageData()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Error parsing sample data: ${e.message}")
            showError("Error loading app data: ${e.message}")
        }
    }

    private fun loadAppUsageData() {
        executor.execute {
            try {
                // Get current timestamp for cache busting
                val timestamp = System.currentTimeMillis()
                val apiUrl = "https://mobile-tracker-free.com/dashboard/scripts/data/server_processing_applications.php?_=$timestamp"
                
                Log.d(TAG, "Attempting to load app data from URL: $apiUrl")
                
                val url = URL(apiUrl)
                val connection = url.openConnection()
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                
                val responseText = connection.getInputStream().bufferedReader().use { it.readText() }
                Log.d(TAG, "API Response: ${responseText.take(100)}...")
                
                // If the response is not empty and valid, update UI
                if (responseText.isNotBlank() && responseText != "null") {
                    // Parse the response JSON
                    val appList = parseAppUsageData(responseText)
                    
                    // Update UI on main thread
                    runOnUiThread {
                        if (appList.isNotEmpty()) {
                            displayAppUsageData(appList)
                            Toast.makeText(this, "Loaded real app data", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "Error loading app data from network: ${e.message}")
                // We already have sample data showing, so no need to update UI
            }
        }
    }

    private fun parseAppUsageData(jsonString: String): List<AppUsageData> {
        val appList = mutableListOf<AppUsageData>()
        try {
            val jsonObject = JSONObject(jsonString)
            val dataArray = jsonObject.getJSONArray("aaData")
            
            for (i in 0 until dataArray.length()) {
                val appArray = dataArray.getJSONArray(i)
                
                // Extract app data from the array
                val appName = appArray.getString(0)
                val packageName = appArray.getString(1)
                val version = appArray.getString(2)
                val size = appArray.getString(3)
                val installDate = appArray.getString(4)
                val status = appArray.getString(5)
                
                // Create AppUsageData object and add to list
                val appData = AppUsageData(
                    appName = appName,
                    packageName = packageName,
                    version = version,
                    size = size,
                    installDate = installDate,
                    status = status
                )
                appList.add(appData)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Error parsing JSON: ${e.message}")
        }
        return appList
    }

    private fun displayAppUsageData(appList: List<AppUsageData>) {
        val adapter = AppUsageAdapter(appList)
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
    private val sampleAppUsageData = """
        {"aaData":[
            ["Child Tracking","com.example.childtracking","1.0","11.3 MB","2025/03/16 20:20:00","<span class=\"label label-sm label-info\"> Installed  </span>"],
            ["Parental control","com.mobiletrackerfree.app","1.0","24.6 MB","2025/03/15 17:42:32","<span class=\"label label-sm label-info\"> Installed  </span>"],
            ["locationTrack","com.example.locationtrack","1.0","10.7 MB","2025/03/15 16:45:53","<span class=\"label label-sm label-warning\">Uninstalled </span>"],
            ["Track Device","com.example.trackdevice","1.0","9.3 MB","2025/03/15 03:33:52","<span class=\"label label-sm label-warning\">Uninstalled </span>"],
            ["Gboard","com.google.android.inputmethod.latin","14.9.07.696880419-release-arm64-v8a","75.8 MB","2009/01/01 05:30:00","<span class=\"label label-sm label-info\"> Installed  </span>"],
            ["WhatsApp","com.whatsapp","2.25.5.74","60.5 MB","2025/03/03 02:09:03","<span class=\"label label-sm label-info\"> Installed  </span>"],
            ["YouTube","com.google.android.youtube","20.09.39","48.5 MB","2009/01/01 05:30:00","<span class=\"label label-sm label-info\"> Installed  </span>"],
            ["Facebook","com.facebook.katana","503.0.0.69.76","144.6 MB","2025/01/27 00:17:49","<span class=\"label label-sm label-info\"> Installed  </span>"],
            ["Gmail","com.google.android.gm","2025.02.23.732285849.Release","66.3 MB","2009/01/01 05:30:00","<span class=\"label label-sm label-info\"> Installed  </span>"],
            ["Maps","com.google.android.apps.maps","25.10.05.732665141","64 MB","2009/01/01 05:30:00","<span class=\"label label-sm label-info\"> Installed  </span>"]
        ]}
    """.trimIndent()
} 