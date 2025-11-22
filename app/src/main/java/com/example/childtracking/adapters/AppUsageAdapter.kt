package com.example.childtracking.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.childtracking.R
import com.example.childtracking.models.AppUsageData

class AppUsageAdapter(
    private val appList: List<AppUsageData>
) : RecyclerView.Adapter<AppUsageAdapter.AppViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app_usage, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val appData = appList[position]
        holder.bind(appData)
    }

    override fun getItemCount(): Int = appList.size

    inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val appNameTextView: TextView = itemView.findViewById(R.id.appNameTextView)
        private val packageNameTextView: TextView = itemView.findViewById(R.id.packageNameTextView)
        private val versionTextView: TextView = itemView.findViewById(R.id.versionTextView)
        private val sizeTextView: TextView = itemView.findViewById(R.id.sizeTextView)
        private val installDateTextView: TextView = itemView.findViewById(R.id.installDateTextView)
        private val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)

        fun bind(appData: AppUsageData) {
            appNameTextView.text = appData.appName
            packageNameTextView.text = appData.packageName
            versionTextView.text = "Version: ${appData.version}"
            sizeTextView.text = "Size: ${appData.size}"
            installDateTextView.text = "Installed: ${appData.installDate}"
            
            // Set status text and background color
            statusTextView.text = if (appData.status.contains("Installed")) "Installed" else "Uninstalled"
            
            if (appData.status.contains("Installed")) {
                statusTextView.setBackgroundResource(R.color.teal_200)
            } else {
                statusTextView.setBackgroundColor(Color.parseColor("#F39C12")) // Amber/Orange for uninstalled
            }
        }
    }
} 