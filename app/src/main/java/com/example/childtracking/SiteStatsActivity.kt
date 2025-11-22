package com.example.childtracking

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.childtracking.models.SiteStat
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import org.json.JSONArray
import java.net.URL
import java.util.concurrent.Executors

class SiteStatsActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var statsContainer: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private val executor = Executors.newSingleThreadExecutor()

    private val colors = intArrayOf(
        Color.rgb(244, 208, 63),
        Color.rgb(133, 193, 233),
        Color.rgb(231, 76, 60),
        Color.rgb(46, 134, 44),
        Color.rgb(155, 89, 182),
        Color.rgb(230, 126, 34),
        Color.rgb(52, 152, 219),
        Color.rgb(22, 160, 133)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_site_stats)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Site Web Statistics"
        }

        pieChart = findViewById(R.id.pieChart)
        statsContainer = findViewById(R.id.statsContainer)
        progressBar = findViewById(R.id.progressBar)
        errorTextView = findViewById(R.id.errorTextView)

        setupPieChart()
        loadSiteStats()
    }

    private fun setupPieChart() {
        pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            setUsePercentValues(true)
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(11f)
            legend.isEnabled = true
            legend.textSize = 12f
        }
    }

    private fun loadSiteStats() {
        showLoading(true)

        executor.execute {
            try {
                val timestamp = System.currentTimeMillis()
                val apiUrl = "https://mobile-tracker-free.com/dashboard/scripts/stats/getSiteStats.php"

                val responseText = URL(apiUrl).readText()
                val siteStats = parseSiteStats(responseText)

                runOnUiThread {
                    if (siteStats.isEmpty()) {
                        showError("No site statistics available")
                    } else {
                        displaySiteStats(siteStats)
                        setPieChartData(siteStats)
                        showLoading(false)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    showError("Error loading site statistics: ${e.message}")
                }
            }
        }
    }

    private fun parseSiteStats(jsonString: String): List<SiteStat> {
        val siteStats = mutableListOf<SiteStat>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val label = jsonObject.getString("label")
                val data = jsonObject.getInt("data")
                siteStats.add(SiteStat(label, data))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return siteStats
    }

    private fun setPieChartData(siteStats: List<SiteStat>) {
        val entries = siteStats.map { PieEntry(it.data.toFloat(), it.label) }
        val dataSet = PieDataSet(entries, "Site Web Statistics").apply {
            colors = this@SiteStatsActivity.colors.toList()
            valueTextColor = Color.WHITE
            valueTextSize = 12f
            valueFormatter = PercentFormatter(pieChart)
        }

        pieChart.data = PieData(dataSet)
        pieChart.invalidate()
    }

    private fun displaySiteStats(siteStats: List<SiteStat>) {
        statsContainer.removeAllViews()
        val totalVisits = siteStats.sumOf { it.data }

        siteStats.forEachIndexed { index, stat ->
            val percentage = (stat.data.toFloat() / totalVisits * 100).toInt()

            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(32, 16, 32, 16)
            }

            val colorIndicator = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(24, 24)
                setBackgroundColor(colors[index % colors.size])
            }

            val statInfo = TextView(this).apply {
                text = "${stat.label}: ${stat.data} visits (${percentage}%)"
                setPadding(16, 0, 0, 0)
            }

            rowLayout.addView(colorIndicator)
            rowLayout.addView(statInfo)
            statsContainer.addView(rowLayout)
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
}
