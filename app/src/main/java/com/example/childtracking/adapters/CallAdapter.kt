package com.example.childtracking.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.childtracking.R
import com.example.childtracking.models.Call
import android.text.Html
import android.widget.ImageView

class CallAdapter(private val calls: List<Call>) : 
    RecyclerView.Adapter<CallAdapter.CallViewHolder>() {
    
    class CallViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val callTypeIndicator: ImageView = view.findViewById(R.id.callTypeIndicator)
        val contactNameTextView: TextView = view.findViewById(R.id.contactName)
        val phoneNumberTextView: TextView = view.findViewById(R.id.phoneNumber)
        val durationTextView: TextView = view.findViewById(R.id.callDuration)
        val dateTimeTextView: TextView = view.findViewById(R.id.callDateTime)
        val locationTextView: TextView = view.findViewById(R.id.callLocation)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_call, parent, false)
        return CallViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: CallViewHolder, position: Int) {
        val call = calls[position]
        
        // Set call type icon
        if (call.callType.contains("outgoing")) {
            holder.callTypeIndicator.setImageResource(R.drawable.ic_call_made)
        } else if (call.callType.contains("incoming")) {
            holder.callTypeIndicator.setImageResource(R.drawable.ic_call_received)
        } else {
            holder.callTypeIndicator.setImageResource(R.drawable.ic_call_missed)
        }
        
        holder.contactNameTextView.text = call.contactName
        holder.phoneNumberTextView.text = call.phoneNumber
        holder.durationTextView.text = call.duration
        holder.dateTimeTextView.text = call.dateTime
        
        // Extract location text from HTML
        val locationText = Html.fromHtml(call.location, Html.FROM_HTML_MODE_COMPACT)
            .toString()
            .replace("\\s+".toRegex(), " ")
            .trim()
            .split("  ")[0] // Split before the button
        
        holder.locationTextView.text = locationText
    }
    
    override fun getItemCount() = calls.size
} 