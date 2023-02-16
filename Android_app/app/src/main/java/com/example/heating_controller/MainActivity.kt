package com.example.heating_controller

import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import javax.security.auth.login.LoginException

class MainActivity : AppCompatActivity() {

    val url: String = "http://192.168.0.188:7777"
    lateinit var responseTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        responseTextView = findViewById<TextView>(R.id.responseTextView)

        getData()

    }

    fun getData(){

        val queue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            {response ->
                responseTextView.text = response
            },
            { error->
                Log.e("Volley", "Error: ${error.message}")

            })

// Add the request to the RequestQueue.
        queue.add(stringRequest)
    }
}