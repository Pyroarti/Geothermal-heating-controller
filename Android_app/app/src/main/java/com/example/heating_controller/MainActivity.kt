package com.example.heating_controller

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import java.net.URLDecoder
import javax.security.auth.login.LoginException
import com.google.gson.GsonBuilder
import org.json.JSONObject
import com.squareup.picasso.Picasso
import android.widget.ImageView
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi

private const val NOTIFICATION_ID = 1
private const val CHANNEL_ID = "com.example.heating_controller.channel"
class MainActivity : AppCompatActivity() {

    val mainURL: String = "http://192.168.0.188:7777"
    val graphURL: String = "http://192.168.0.188:7777/graph"

    lateinit var responseTextView: TextView
    lateinit var graphImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        responseTextView = findViewById<TextView>(R.id.responseTextView)
        graphImageView = findViewById<ImageView>(R.id.graphImageView)
        val editText = findViewById<EditText>(R.id.editTextNumber)
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                setThreshold(editText)
                true
            } else {
                false
            }
        }
        getData()
        createNotificationChannel()
    }

    private fun getData() {
        val queue = Volley.newRequestQueue(this)
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, mainURL, null,
            { response ->
                val mapper = ObjectMapper()
                mapper.enable(SerializationFeature.INDENT_OUTPUT)
                val jsonNode = mapper.readTree(response.toString())
                val prettyJson = mapper.writeValueAsString(jsonNode)
                val filename = "response.json"
                val fileContents = prettyJson.replace(Regex("[{}\",]"), "").replace("\\n", "\n")
                applicationContext.openFileOutput(filename, Context.MODE_PRIVATE).use {
                    it.write(fileContents.toByteArray())
                }
                responseTextView.text = fileContents
                Log.e("Volley", "Made it:")
            },
            { error ->
                Log.e("Volley", "Error: ${error.message}")
            }
        )

        queue.add(jsonObjectRequest)
        val picasso = Picasso.get()
        picasso.load(graphURL).into(graphImageView)
    }
    @SuppressLint("MissingPermission")
    private fun setThreshold(editText: EditText) {
        val queue = Volley.newRequestQueue(this)
        val thresholdValue = editText.text.toString()
        val jsonBody = JSONObject()
        jsonBody.put("threshold", thresholdValue)
        val thresholdURL = "http://192.168.0.188:7777/threshold"
        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, thresholdURL, jsonBody,

            { response ->

            },
            { error ->
                Log.e("setThreshold", "Error: ${error.message}")
                // Handle error response
            }
        )
        queue.add(jsonObjectRequest)

        // Handle success response
        val message = "Temperatur gräns är satt till $thresholdValue"
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Temperatur gräns")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
        Log.e("MainActivity", "Notification builder initialized")
        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, notificationBuilder.build())
            Log.d("MainActivity", "Notification displayed")
        }

    }
    private fun createNotificationChannel() {
        val name = "Threshold Notifications"
        val descriptionText = "Notifications for threshold changes"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

}

