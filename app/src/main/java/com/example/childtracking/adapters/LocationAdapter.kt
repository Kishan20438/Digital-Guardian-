package com.example.childtracking.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.childtracking.R
import com.example.childtracking.models.LocationData
import java.text.SimpleDateFormat
import java.util.Locale

class LocationAdapter(
    private val locations: List<LocationData>,
    private val onLocationClick: (LocationData) -> Unit
) : RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_location, parent, false)
        return LocationViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val location = locations[position]
        holder.bind(location)
        
        // Set click listener for the entire item
        holder.itemView.setOnClickListener {
            onLocationClick(location)
        }
    }

    override fun getItemCount(): Int = locations.size

    inner class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTimeTextView: TextView = itemView.findViewById(R.id.dateTimeTextView)
        private val addressTextView: TextView = itemView.findViewById(R.id.addressTextView)
        private val coordinatesTextView: TextView = itemView.findViewById(R.id.coordinatesTextView)

        fun bind(location: LocationData) {
            try {
                // Format date for display
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMMM d, yyyy HH:mm:ss", Locale.getDefault())
                val date = inputFormat.parse(location.date)
                val formattedDate = if (date != null) outputFormat.format(date) else location.date

                // Update views with location details
                dateTimeTextView.text = formattedDate
                addressTextView.text = location.address
                coordinatesTextView.text = 
                    "Lat: ${location.latitude}, Long: ${location.longitude} (Accuracy: ${location.accuracy})"
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
} 