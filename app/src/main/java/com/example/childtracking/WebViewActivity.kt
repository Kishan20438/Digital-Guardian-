package com.example.childtracking

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.childtracking.databinding.ActivityWebViewBinding
import com.example.childtracking.adapters.CallAdapter
import com.example.childtracking.adapters.ContactAdapter
import com.example.childtracking.models.Call
import com.example.childtracking.models.Contact
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

class WebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding
    private lateinit var contactsRecyclerView: RecyclerView
    private lateinit var callsRecyclerView: RecyclerView
    private lateinit var noContactsMessage: TextView
    private lateinit var noCallsMessage: TextView
    private var isContactsUrl = false
    private var isCallsUrl = false
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar

    companion object {
        const val EXTRA_URL = "url"
        const val EXTRA_TITLE = "extra_title"
        private const val CONTACTS_API_URL = "server_processing_contact.php"
        private const val FULL_CONTACTS_API_URL = "https://mobile-tracker-free.com/dashboard/scripts/data/server_processing_contact.php"
        private const val CALLS_API_URL = "server_processing_calls.php"
        private const val FULL_CALLS_API_URL = "https://mobile-tracker-free.com/dashboard/scripts/data/server_processing_calls.php"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Contacts"

        webView = binding.webView
        progressBar = binding.progressBar

        // Initialize RecyclerViews
        initRecyclerViews()
        
        // Configure WebView
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadsImagesAutomatically = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            useWideViewPort = true
            loadWithOverviewMode = true
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress < 100) {
                    progressBar.visibility = View.VISIBLE
                } else {
                    progressBar.visibility = View.GONE
                }
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val requestUrl = request.url.toString()
                android.util.Log.d("WebViewActivity", "Loading URL: $requestUrl")
                
                view.loadUrl(requestUrl)
                return true
            }
            
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                android.util.Log.d("WebViewActivity", "Page started: $url")
                
                // Reset all containers when loading a new page
                resetAllContainers()
            }
            
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                val url = request.url.toString()
                
                android.util.Log.d("WebViewActivity", "Intercepting URL: $url")
                
                when {
                    url.contains(CONTACTS_API_URL) -> {
                        android.util.Log.d("WebViewActivity", "Found contacts API URL!")
                        isContactsUrl = true
                        
                        try {
                            // Get the original response
                            val response = super.shouldInterceptRequest(view, request)
                            
                            if (response != null) {
                                // Read the response
                                val inputStream = response.data
                                val responseText = inputStream.bufferedReader().use { it.readText() }
                                
                                android.util.Log.d("WebViewActivity", "WebView contacts response: $responseText")
                                
                                // Parse JSON
                                runOnUiThread {
                                    try {
                                        // Only show the contacts view if we have valid data
                                        if (responseText.contains("aaData")) {
                                            showContactsContainer()
                                            handleContactsResponse(responseText)
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("WebViewActivity", "Error parsing contacts data", e)
                                        showNoContacts("Error parsing data: ${e.message}")
                                    }
                                }
                                
                                // Return the original response
                                return WebResourceResponse(
                                    response.mimeType,
                                    response.encoding,
                                    ByteArrayInputStream(responseText.toByteArray())
                                )
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("WebViewActivity", "Error intercepting contacts request", e)
                            e.printStackTrace()
                        }
                    }
                    url.contains(CALLS_API_URL) -> {
                        android.util.Log.d("WebViewActivity", "Found calls API URL!")
                        isCallsUrl = true
                        
                        try {
                            // Get the original response
                            val response = super.shouldInterceptRequest(view, request)
                            
                            if (response != null) {
                                // Read the response
                                val inputStream = response.data
                                val responseText = inputStream.bufferedReader().use { it.readText() }
                                
                                android.util.Log.d("WebViewActivity", "WebView calls response: $responseText")
                                
                                // Parse JSON
                                runOnUiThread {
                                    try {
                                        // Only show the calls view if we have valid data
                                        if (responseText.contains("aaData")) {
                                            showCallsContainer()
                                            handleCallsResponse(responseText)
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("WebViewActivity", "Error parsing calls data", e)
                                        showNoCalls("Error parsing data: ${e.message}")
                                    }
                                }
                                
                                // Return the original response
                                return WebResourceResponse(
                                    response.mimeType,
                                    response.encoding,
                                    ByteArrayInputStream(responseText.toByteArray())
                                )
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("WebViewActivity", "Error intercepting calls request", e)
                            e.printStackTrace()
                        }
                    }
                }
                
                return super.shouldInterceptRequest(view, request)
            }
        }

        // Get URL from intent and load it
        val url = intent.getStringExtra(EXTRA_URL)
        if (url != null) {
            webView.loadUrl(url)
            supportActionBar?.title = intent.getStringExtra(EXTRA_TITLE) ?: "Detail"
        }
    }
    
    private fun initRecyclerViews() {
        // Initialize RecyclerView for contacts
        contactsRecyclerView = binding.contactsRecyclerView
        contactsRecyclerView.layoutManager = LinearLayoutManager(this)
        noContactsMessage = binding.noContactsMessage
        
        // Initialize RecyclerView for calls
        callsRecyclerView = binding.callsRecyclerView
        callsRecyclerView.layoutManager = LinearLayoutManager(this)
        noCallsMessage = binding.noCallsMessage
    }
    
    private fun resetAllContainers() {
        webView.visibility = View.VISIBLE
        binding.contactsContainer.visibility = View.GONE
        binding.callsContainer.visibility = View.GONE
        isContactsUrl = false
        isCallsUrl = false
    }
    
    private fun showContactsContainer() {
        webView.visibility = View.GONE
        binding.contactsContainer.visibility = View.VISIBLE
        binding.callsContainer.visibility = View.GONE
    }
    
    private fun showCallsContainer() {
        webView.visibility = View.GONE
        binding.contactsContainer.visibility = View.GONE
        binding.callsContainer.visibility = View.VISIBLE
    }
    
    private fun fetchContactsData() {
        // Show loading
        showContactsContainer()
        showNoContacts("Loading contacts...")
        
        // Fetch data in background
        executor.execute {
            try {
                // Get current timestamp for cache busting
                val timestamp = System.currentTimeMillis()
                val apiUrl = "$FULL_CONTACTS_API_URL?_=$timestamp"
                android.util.Log.d("WebViewActivity", "Fetching contacts from: $apiUrl")
                
                val url = URL(apiUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.apply {
                    connectTimeout = 10000
                    readTimeout = 10000
                    requestMethod = "GET"
                    
                    // Add required headers exactly as shown in the screenshot
                    setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01")
                    setRequestProperty("Accept-Encoding", "gzip, deflate, br, zstd")
                    setRequestProperty("Accept-Language", "en-US,en;q=0.9")
                    setRequestProperty("Cache-Control", "no-store, no-cache, must-revalidate")
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Referer", "https://mobile-tracker-free.com/dashboard/")
                    setRequestProperty("Cookie", "_ga=GA1.1.1526363968.1741602843; cookieconsent_dismissed=yes;")
                }
                
                android.util.Log.d("WebViewActivity", "Making API request with headers: ${connection.requestProperties}")
                
                try {
                    val responseCode = connection.responseCode
                    android.util.Log.d("WebViewActivity", "Response code: $responseCode")
                    
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Read response with explicit UTF-8 encoding
                        val responseText = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                        
                        // Log raw response for debugging
                        android.util.Log.d("WebViewActivity", "Raw response length: ${responseText.length}")
                        android.util.Log.d("WebViewActivity", "Raw response bytes: ${responseText.toByteArray().joinToString(",")}")
                        android.util.Log.d("WebViewActivity", "Direct API contacts response: $responseText")
                        
                        // Check if response is valid JSON
                        if (!responseText.trim().startsWith("{")) {
                            throw Exception("Invalid JSON response format")
                        }
                        
                        // Update UI on main thread
                        runOnUiThread {
                            if (responseText == "{\"aaData\":[]}") {
                                android.util.Log.d("WebViewActivity", "Empty response received")
                                showNoContacts("No contacts available")
                            } else {
                                android.util.Log.d("WebViewActivity", "Processing response")
                                // Use the actual response instead of sample data
                                handleContactsResponse(responseText)
                            }
                        }
                    } else {
                        val errorStream = connection.errorStream
                        val errorResponse = errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                        android.util.Log.e("WebViewActivity", "Error response: $errorResponse")
                        
                        runOnUiThread {
                            showNoContacts("Error loading contacts: HTTP $responseCode")
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("WebViewActivity", "Error reading response", e)
                    val errorStream = connection.errorStream
                    val errorResponse = errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                    android.util.Log.e("WebViewActivity", "Error response: $errorResponse")
                    
                    runOnUiThread {
                        showNoContacts("Error loading contacts: ${e.message}")
                    }
                } finally {
                    connection.disconnect()
                }
            } catch (e: Exception) {
                android.util.Log.e("WebViewActivity", "Error in network call", e)
                e.printStackTrace()
                runOnUiThread {
                    showNoContacts("Network error: ${e.message}")
                }
            }
        }
    }
    
    private fun fetchCallsData() {
        // Show loading
        showCallsContainer()
        showNoCalls("Loading call logs...")
        
        // Fetch data in background
        executor.execute {
            try {
                // Get current timestamp for cache busting
                val timestamp = System.currentTimeMillis()
                val apiUrl = "$FULL_CALLS_API_URL?_=$timestamp"
                android.util.Log.d("WebViewActivity", "Fetching calls from: $apiUrl")
                
                val url = URL(apiUrl)
                val connection = url.openConnection()
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                
                val responseText = connection.getInputStream().bufferedReader().use { it.readText() }
                android.util.Log.d("WebViewActivity", "Direct API calls response: $responseText")
                
                // Use a sample response if the API returns empty data
                val finalResponse = if (responseText == "{\"aaData\":[]}") {
                    sampleCallsData
                } else {
                    responseText
                }
                
                // Update UI on main thread
                runOnUiThread {
                    isCallsUrl = true
                    handleCallsResponse(finalResponse)
                }
            } catch (e: Exception) {
                android.util.Log.e("WebViewActivity", "Error fetching calls", e)
                runOnUiThread {
                    showNoCalls("Error loading call logs: ${e.message}")
                }
            }
        }
    }
    
    private fun handleContactsResponse(responseText: String) {
        try {
            android.util.Log.d("WebViewActivity", "Handling contacts response: $responseText")
            
            // Trim any whitespace or BOM characters
            val cleanedResponse = responseText.trim().trimStart('\uFEFF')
            android.util.Log.d("WebViewActivity", "Cleaned response: $cleanedResponse")
            
            val jsonObject = JSONObject(cleanedResponse)
            
            // Verify we have the aaData field
            if (!jsonObject.has("aaData")) {
                throw Exception("Response missing aaData field")
            }
            
            val contactsArray = jsonObject.getJSONArray("aaData")
            android.util.Log.d("WebViewActivity", "Contacts array length: ${contactsArray.length()}")
            
            val contacts = mutableListOf<Contact>()
            
            if (contactsArray.length() > 0) {
                for (i in 0 until contactsArray.length()) {
                    try {
                        val contactArray = contactsArray.getJSONArray(i)
                        
                        // Extract ID from checkbox value
                        val checkboxHtml = contactArray.getString(0)
                        // Updated regex to handle escaped HTML
                        val idRegex = "value=(\\d+)".toRegex()
                        val idMatch = idRegex.find(checkboxHtml)
                        val id = idMatch?.groupValues?.get(1)?.toLongOrNull() ?: 0
                        
                        // Extract name and handle potential null
                        val name = contactArray.optString(1, "Unknown")
                            .replace("\u003C", "<")
                            .replace("\u003E", ">")
                            .replace("<br/>", " ")
                            .trim()
                        
                        // Extract phone and handle potential null
                        val phoneHtml = contactArray.optString(2, "")
                            .replace("\u003C", "<")
                            .replace("\u003E", ">")
                            .replace("<br/>", " ")
                            .trim()
                        
                        // Extract date and handle potential null
                        val dateStr = contactArray.optString(3, "")
                            .replace("\u003C", "<")
                            .replace("\u003E", ">")
                            .replace("<br/>", " ")
                            .trim()
                        
                        android.util.Log.d("WebViewActivity", "Parsed Contact: id=$id, name=$name, phone=$phoneHtml, date=$dateStr")
                        
                        contacts.add(Contact(id, name, phoneHtml, dateStr))
                    } catch (e: Exception) {
                        android.util.Log.e("WebViewActivity", "Error parsing contact at index $i", e)
                        // Continue with next contact instead of failing completely
                        continue
                    }
                }
                
                runOnUiThread {
                    if (contacts.isNotEmpty()) {
                        // Display contacts in RecyclerView
                        contactsRecyclerView.adapter = ContactAdapter(contacts)
                        contactsRecyclerView.visibility = View.VISIBLE
                        noContactsMessage.visibility = View.GONE
                    } else {
                        showNoContacts("No valid contacts found")
                    }
                }
            } else {
                runOnUiThread {
                    showNoContacts("No contacts found")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("WebViewActivity", "Error handling contacts", e)
            runOnUiThread {
                showNoContacts("Error parsing contact data: ${e.message}")
            }
        }
    }
    
    private fun handleCallsResponse(responseText: String) {
        try {
            android.util.Log.d("WebViewActivity", "Handling calls response: $responseText")
            val jsonObject = JSONObject(responseText)
            val callsArray = jsonObject.getJSONArray("aaData")
            
            android.util.Log.d("WebViewActivity", "Calls array length: ${callsArray.length()}")
            
            val calls = mutableListOf<Call>()
            
            if (callsArray.length() > 0) {
                for (i in 0 until callsArray.length()) {
                    val callArray = callsArray.getJSONArray(i)
                    
                    // Extract ID from checkbox value
                    val checkboxHtml = callArray.getString(0)
                    val idRegex = "value=(\\d+)".toRegex()
                    val idMatch = idRegex.find(checkboxHtml)
                    val id = idMatch?.groupValues?.get(1)?.toLongOrNull() ?: 0
                    
                    // Extract recording info
                    val recording = callArray.getString(1)
                    
                    // Extract call type
                    val callTypeHtml = callArray.getString(2)
                    val callType = if (callTypeHtml.contains("outgoing")) "outgoing" else if (callTypeHtml.contains("incoming")) "incoming" else "missed"
                    
                    // Extract contact name and phone number
                    val contactName = callArray.getString(3)
                    val phoneNumber = callArray.getString(4)
                    
                    // Extract duration
                    val duration = callArray.getString(5)
                    
                    // Extract date/time
                    val dateTime = callArray.getString(6)
                    
                    // Extract location
                    val location = callArray.getString(7)
                    
                    // Extract coordinates and accuracy
                    val latitude = callArray.getString(8)
                    val longitude = callArray.getString(9)
                    val accuracy = callArray.getString(10)
                    
                    android.util.Log.d("WebViewActivity", "Call: $contactName, $phoneNumber, $dateTime")
                    
                    calls.add(Call(id, recording, callType, contactName, phoneNumber, duration, dateTime, location, latitude, longitude, accuracy))
                }
                
                // Display calls in RecyclerView
                callsRecyclerView.adapter = CallAdapter(calls)
                callsRecyclerView.visibility = View.VISIBLE
                noCallsMessage.visibility = View.GONE
            } else {
                android.util.Log.d("WebViewActivity", "No calls found")
                showNoCalls("No call logs found")
            }
        } catch (e: Exception) {
            android.util.Log.e("WebViewActivity", "Error handling calls", e)
            e.printStackTrace()
            showNoCalls("Error parsing call data")
        }
    }
    
    private fun showNoContacts(message: String) {
        contactsRecyclerView.visibility = View.GONE
        noContactsMessage.visibility = View.VISIBLE
        noContactsMessage.text = message
    }
    
    private fun showNoCalls(message: String) {
        callsRecyclerView.visibility = View.GONE
        noCallsMessage.visibility = View.VISIBLE
        noCallsMessage.text = message
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        when {
            isContactsUrl || isCallsUrl -> {
                // If we're in a special view, go back to the dashboard
                resetAllContainers()
                webView.loadUrl("https://mobile-tracker-free.com/dashboard/")
            }
            webView.canGoBack() -> {
                webView.goBack()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }
    
    // Sample contacts data to show if API returns empty
    private val sampleContactData = """
        {"aaData":[["<label class='mt-checkbox mt-checkbox-single mt-checkbox-outline'>\n                        <input type='checkbox' class='checkboxes' value=821242551>\n                        <span><\/span>\n                    <\/label>","Cybercrime Helpline","1930<br\/>","2025\/03\/16 22:15:25"],["<label class='mt-checkbox mt-checkbox-single mt-checkbox-outline'>\n                        <input type='checkbox' class='checkboxes' value=821241925>\n                        <span><\/span>\n                    <\/label>","Chaitanya","73258 56526<br\/>","2025\/03\/07 20:13:10"],["<label class='mt-checkbox mt-checkbox-single mt-checkbox-outline'>\n                        <input type='checkbox' class='checkboxes' value=821241917>\n                        <span><\/span>\n                    <\/label>","Vikash","96325 63593<br\/>","2025\/03\/07 20:13:10"],["<label class='mt-checkbox mt-checkbox-single mt-checkbox-outline'>\n                        <input type='checkbox' class='checkboxes' value=821241911>\n                        <span><\/span>\n                    <\/label>","Aditya","72002 46785<br\/>","2025\/03\/07 20:13:10"],["<label class='mt-checkbox mt-checkbox-single mt-checkbox-outline'>\n                        <input type='checkbox' class='checkboxes' value=821241901>\n                        <span><\/span>\n                    <\/label>","Abhishek","73228 85061<br\/>","2025\/03\/07 20:13:10"],["<label class='mt-checkbox mt-checkbox-single mt-checkbox-outline'>\n                        <input type='checkbox' class='checkboxes' value=818597963>\n                        <span><\/span>\n                    <\/label>","Abhishek Kumar","73228 85061<br\/>","2025\/03\/07 20:13:11"]]}
    """.trimIndent()
    
    // Sample calls data to show if API returns empty
    private val sampleCallsData = """
        {"aaData":[
            ["<label class='mt-checkbox mt-checkbox-single mt-checkbox-outline'>\n                        <input type='checkbox' class='checkboxes' value=4825982060>\n                        <span><\/span>\n                    <\/label>","<span class='btn btn-icon-only blue margin-bottom-5 playRecordAudio' id=76468721742144909279.mp4> <i class='fa fa-play fa-lg'><\/i> <\/span><br\/>\n                            <span class='btn btn-icon-only blue downloadRecordAudio' id=76468721742144909279.mp4> <i class='fa fa-download fa-lg'><\/i><\/span>","<span class=\"label label-sm label-success\"> outgoing <\/span>","8804445703","8804445703","00 hour 00 minute 00 second","2025\/03\/16 22:38:28","<td>3\/201, Mettukuppam, Chennai, Kotivakkam, Tamil Nadu 600097, India&nbsp;&nbsp;<button type=\"button\" onClick=\"viewMap('12.9392998','80.2404444','13.619999885559082','2025\/03\/16 22:28:52','3\/201, Mettukuppam, Chennai, Kotivakkam, Tamil Nadu 600097, India');\" class=\"btn blue btn-outline btn-xs pull-right margin-top-10\"><i class=\"fa fa-map-o\"><\/i><\/button><\/td>","12.9392998","80.2404444","13.619999885559082",4825982060],
            ["<label class='mt-checkbox mt-checkbox-single mt-checkbox-outline'>\n                        <input type='checkbox' class='checkboxes' value=4825981907>\n                        <span><\/span>\n                    <\/label>","<span class='btn btn-icon-only blue margin-bottom-5 playRecordAudio' id=76468721742144901769.mp4> <i class='fa fa-play fa-lg'><\/i> <\/span><br\/>\n                            <span class='btn btn-icon-only blue downloadRecordAudio' id=76468721742144901769.mp4> <i class='fa fa-download fa-lg'><\/i><\/span>","<span class=\"label label-sm label-success\"> outgoing <\/span>","Abhishek","7322885061","00 hour 00 minute 00 second","2025\/03\/16 22:38:21","<td>3\/201, Mettukuppam, Chennai, Kotivakkam, Tamil Nadu 600097, India&nbsp;&nbsp;<button type=\"button\" onClick=\"viewMap('12.9392998','80.2404444','13.619999885559082','2025\/03\/16 22:28:52','3\/201, Mettukuppam, Chennai, Kotivakkam, Tamil Nadu 600097, India');\" class=\"btn blue btn-outline btn-xs pull-right margin-top-10\"><i class=\"fa fa-map-o\"><\/i><\/button><\/td>","12.9392998","80.2404444","13.619999885559082",4825981907],
            ["<label class='mt-checkbox mt-checkbox-single mt-checkbox-outline'>\n                        <input type='checkbox' class='checkboxes' value=4825981767>\n                        <span><\/span>\n                    <\/label>","<span class='btn btn-icon-only blue margin-bottom-5 playRecordAudio' id=76468721742144895013.mp4> <i class='fa fa-play fa-lg'><\/i> <\/span><br\/>\n                            <span class='btn btn-icon-only blue downloadRecordAudio' id=76468721742144895013.mp4> <i class='fa fa-download fa-lg'><\/i><\/span>","<span class=\"label label-sm label-success\"> outgoing <\/span>","Abhishek","7322885061","00 hour 00 minute 00 second","2025\/03\/16 22:38:14","<td>3\/201, Mettukuppam, Chennai, Kotivakkam, Tamil Nadu 600097, India&nbsp;&nbsp;<button type=\"button\" onClick=\"viewMap('12.9392998','80.2404444','13.619999885559082','2025\/03\/16 22:28:52','3\/201, Mettukuppam, Chennai, Kotivakkam, Tamil Nadu 600097, India');\" class=\"btn blue btn-outline btn-xs pull-right margin-top-10\"><i class=\"fa fa-map-o\"><\/i><\/button><\/td>","12.9392998","80.2404444","13.619999885559082",4825981767]
        ]}
    """.trimIndent()
}