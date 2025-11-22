package com.example.childtracking.ui.home

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.childtracking.R
import com.example.childtracking.WebViewActivity
import com.example.childtracking.databinding.FragmentHomeBinding
import com.example.childtracking.ui.home.HomeViewModel
import com.example.childtracking.CallStatsActivity
import com.example.childtracking.SiteStatsActivity
import com.example.childtracking.LiveLocationActivity
import com.example.childtracking.LocationHistoryActivity
import com.example.childtracking.AppUsageActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupDashboardCards()
        
        return root
    }

    private fun setupDashboardCards() {
        // Setup card: Dashboard
        setupCard(
            binding.cardDashboard.root as CardView,
            R.drawable.ic_dashboard,
            "Dashboard"
        )

        // Setup card: Location
        setupCard(
            binding.cardLocation.root as CardView,
            R.drawable.ic_location,
            "Location"
        )

        // Setup card: Message
        setupCard(
            binding.cardMessage.root as CardView,
            R.drawable.ic_message,
            "Message"
        )

        // Setup card: Contact
        setupCard(
            binding.cardContact.root as CardView,
            R.drawable.ic_contact,
            "Contact"
        )

        // Setup card: Call Log
        setupCard(
            binding.cardCallLog.root as CardView,
            R.drawable.ic_call_log,
            "Call Log"
        )

        // Setup card: App Usage
        setupCard(
            binding.cardAppUsage.root as CardView,
            R.drawable.ic_app_usage,
            "App Usage"
        )

        // Setup card: Details
        setupCard(
            binding.cardDetails.root as CardView,
            R.drawable.ic_details,
            "Details"
        )

        // Setup card: Alerts
        setupCard(
            binding.cardAlerts.root as CardView,
            R.drawable.ic_alerts,
            "Alerts"
        )

        // Setup card: Reports
        setupCard(
            binding.cardReports.root as CardView,
            R.drawable.ic_reports,
            "Reports"
        )
    }

    private fun setupCard(cardView: CardView, iconResId: Int, title: String) {
        val iconView = cardView.findViewById<ImageView>(R.id.card_icon)
        val titleView = cardView.findViewById<TextView>(R.id.card_title)
        
        iconView.setImageResource(iconResId)
        titleView.text = title
        
        cardView.setOnClickListener {
            // Handle navigation or specific actions based on the card
            when (title) {
                "Dashboard" -> {
                    // Open the dashboard URL in the in-app WebView
                    val dashboardIntent = Intent(context, WebViewActivity::class.java)
                    dashboardIntent.putExtra(WebViewActivity.EXTRA_URL, "https://mobile-tracker-free.com/dashboard/")
                    dashboardIntent.putExtra(WebViewActivity.EXTRA_TITLE, "Dashboard")
                    startActivity(dashboardIntent)
                }
                "Location" -> {
                    // Show location options dialog
                    showLocationOptions()
                }
                "Message" -> {
                    Toast.makeText(requireContext(), "$title clicked", Toast.LENGTH_SHORT).show()
                    /* Handle Message click */
                }
                "Contact" -> {
                    // Open the contacts in WebView
                    val contactsIntent = Intent(context, WebViewActivity::class.java)
                    contactsIntent.putExtra(WebViewActivity.EXTRA_URL, "https://mobile-tracker-free.com/dashboard/contacts.php")
                    contactsIntent.putExtra(WebViewActivity.EXTRA_TITLE, "Contacts")
                    startActivity(contactsIntent)
                }
                "Call Log" -> {
                    // Open the call logs in WebView
                    val callsIntent = Intent(context, WebViewActivity::class.java)
                    callsIntent.putExtra(WebViewActivity.EXTRA_URL, "https://mobile-tracker-free.com/dashboard/calllog.php")
                    callsIntent.putExtra(WebViewActivity.EXTRA_TITLE, "Call Log")
                    startActivity(callsIntent)
                }
                "App Usage" -> {
                    // Open AppUsageActivity to show installed apps and their details
                    val intent = Intent(context, AppUsageActivity::class.java)
                    startActivity(intent)
                }
                "Details" -> {
                    Toast.makeText(requireContext(), "$title clicked", Toast.LENGTH_SHORT).show()
                    /* Handle Details click */
                }
                "Alerts" -> {
                    Toast.makeText(requireContext(), "$title clicked", Toast.LENGTH_SHORT).show()
                    /* Handle Alerts click */
                }
                "Reports" -> {
                    showReportsOptions()
                }
            }
        }
    }

    private fun showReportsOptions() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_reports_options)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        
        // Set click listeners for each option
        dialog.findViewById<View>(R.id.callsOption).setOnClickListener {
            // Open Calls stats activity with graph
            val intent = Intent(context, CallStatsActivity::class.java)
            startActivity(intent)
            dialog.dismiss()
        }
        
        dialog.findViewById<View>(R.id.siteWebOption).setOnClickListener {
            // Open Site Web stats activity with graph
            val intent = Intent(context, SiteStatsActivity::class.java)
            startActivity(intent)
            dialog.dismiss()
        }
        
        dialog.findViewById<View>(R.id.appsOption).setOnClickListener {
            // Open Apps report
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra(WebViewActivity.EXTRA_URL, "https://mobile-tracker-free.com/dashboard/reports-apps.php")
            intent.putExtra(WebViewActivity.EXTRA_TITLE, "App Reports")
            startActivity(intent)
            dialog.dismiss()
        }
        
        dialog.show()
    }

    private fun showLocationOptions() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_location_options)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        
        // Set click listener for Live Location option
        dialog.findViewById<View>(R.id.liveLocationOption).setOnClickListener {
            // Open LiveLocationActivity to show current location on map
            val intent = Intent(context, LiveLocationActivity::class.java)
            startActivity(intent)
            dialog.dismiss()
        }
        
        // Set click listener for Location History option
        dialog.findViewById<View>(R.id.locationHistoryOption).setOnClickListener {
            // Open LocationHistoryActivity to show location history list
            val intent = Intent(context, LocationHistoryActivity::class.java)
            startActivity(intent)
            dialog.dismiss()
        }
        
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}