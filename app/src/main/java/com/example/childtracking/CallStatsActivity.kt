package com.example.childtracking

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.childtracking.models.CallStat
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import org.json.JSONArray
import java.net.URL
import java.util.concurrent.Executors

class CallStatsActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var statsContainer: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private val executor = Executors.newSingleThreadExecutor()

    // Colors for the stats bars and chart
    private val colors = intArrayOf(
        Color.rgb(52, 152, 219),  // Blue
        Color.rgb(231, 76, 60),   // Red
        Color.rgb(46, 204, 113),  // Green
        Color.rgb(155, 89, 182),  // Purple
        Color.rgb(241, 196, 15),  // Yellow
        Color.rgb(230, 126, 34),  // Orange
        Color.rgb(149, 165, 166), // Gray
        Color.rgb(26, 188, 156)   // Turquoise
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_stats)

        // Set up the toolbar
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Call Statistics"
        }

        // Initialize views
        pieChart = findViewById(R.id.pieChart)
        statsContainer = findViewById(R.id.statsContainer)
        progressBar = findViewById(R.id.progressBar)
        errorTextView = findViewById(R.id.errorTextView)

        // Configure pie chart
        setupPieChart()

        // Load call statistics
        loadCallStats()
    }

    private fun setupPieChart() {
        pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            centerText = "Calls"
            setCenterTextSize(18f)
            setCenterTextColor(Color.parseColor("#00BCD4"))
            setUsePercentValues(true)
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(12f)
            legend.isEnabled = true
            setExtraOffsets(20f, 0f, 20f, 0f)
        }
    }

    private fun loadCallStats() {
        showLoading(true)

        executor.execute {
            try {
                // Get current timestamp for cache busting
                val timestamp = System.currentTimeMillis()
                val apiUrl = "https://mobile-tracker-free.com/dashboard/scripts/stats/getCallsStats.php?_=$timestamp"
                
                val url = URL(apiUrl)
                val connection = url.openConnection()
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                
                val responseText = connection.getInputStream().bufferedReader().use { it.readText() }
                
                // If the response is empty or null, use sample data
                val finalResponse = if (responseText.isBlank() || responseText == "null") {
                    sampleCallStatsData
                } else {
                    responseText
                }
                
                // Parse the response JSON
                val callStats = parseCallStats(finalResponse)
                
                // Update UI on main thread
                runOnUiThread {
                    if (callStats.isEmpty()) {
                        showError("No call statistics available")
                    } else {
                        displayCallStats(callStats)
                        setPieChartData(callStats)
                        showLoading(false)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    showError("Error loading call statistics: ${e.message}")
                }
            }
        }
    }

    private fun parseCallStats(jsonString: String): List<CallStat> {
        val callStats = mutableListOf<CallStat>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val label = jsonObject.getString("label")
                val data = jsonObject.getInt("data")
                callStats.add(CallStat(label, data))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return callStats
    }

    private fun setPieChartData(callStats: List<CallStat>) {
        val entries = ArrayList<PieEntry>()
        
        // Add entries for each call stat
        callStats.forEach { 
            entries.add(PieEntry(it.data.toFloat(), it.label)) 
        }
        
        // Create dataset and configure it
        val dataSet = PieDataSet(entries, "Call Statistics")
        dataSet.apply {
            sliceSpace = 3f
            selectionShift = 5f
            colors = this@CallStatsActivity.colors.toList()
            valueTextColor = Color.WHITE
            valueTextSize = 12f
            valueFormatter = PercentFormatter(pieChart)
        }
        
        // Create PieData object with our dataset
        val pieData = PieData(dataSet)
        pieData.apply {
            setValueFormatter(PercentFormatter(pieChart))
            setValueTextSize(12f)
            setValueTextColor(Color.WHITE)
        }
        
        // Set data to chart and refresh
        pieChart.data = pieData
        pieChart.highlightValues(null)
        pieChart.invalidate()
    }

    private fun displayCallStats(callStats: List<CallStat>) {
        statsContainer.removeAllViews()
        
        val totalCalls = callStats.sumOf { it.data }
        
        // Add a title
        val titleView = TextView(this).apply {
            text = "CALLS"
            textSize = 18f
            setTextColor(Color.parseColor("#00BCD4"))
            setPadding(32, 32, 32, 16)
        }
        statsContainer.addView(titleView)
        
        // Add stats for each call record
        callStats.forEachIndexed { index, stat ->
            val percentage = (stat.data.toFloat() / totalCalls * 100).toInt()
            
            // Create row container
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(32, 16, 32, 16)
                }
            }
            
            // Color indicator
            val colorIndicator = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(24, 24)
                setBackgroundColor(colors[index % colors.size])
            }
            
            // Stat info
            val statInfo = TextView(this).apply {
                text = "${stat.label}: ${stat.data} calls (${percentage}%)"
                setPadding(16, 0, 0, 0)
            }
            
            // Add views to row
            rowLayout.addView(colorIndicator)
            rowLayout.addView(statInfo)
            
            // Add row to container
            statsContainer.addView(rowLayout)
            
            // Add bar representation
            val barLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    24
                ).apply {
                    setMargins(32, 0, 32, 24)
                }
            }
            
            // Colored portion of bar
            val bar = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    percentage.toFloat()
                )
                setBackgroundColor(colors[index % colors.size])
            }
            
            // Empty portion of bar
            val emptyBar = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (100 - percentage).toFloat()
                )
                setBackgroundColor(Color.LTGRAY)
            }
            
            barLayout.addView(bar)
            barLayout.addView(emptyBar)
            
            statsContainer.addView(barLayout)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        statsContainer.visibility = if (isLoading) View.GONE else View.VISIBLE
        pieChart.visibility = if (isLoading) View.GONE else View.VISIBLE
        errorTextView.visibility = View.GONE
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        statsContainer.visibility = View.GONE
        pieChart.visibility = View.GONE
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
    private val sampleCallStatsData = """
        [
            {"label":"Abhishek","data":2},
            {"label":"8804445703","data":1},
            {"label":"198","data":1}
        ]
    """.trimIndent()
} 