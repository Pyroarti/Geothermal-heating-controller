package com.example.heating_controller

import android.content.Context
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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


class MainActivity : AppCompatActivity() {


    val url: String = "http://192.168.0.188:7777"
    lateinit var responseTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        responseTextView = findViewById<TextView>(R.id.responseTextView)

        getData()

    }

    fun getData() {
        val queue = Volley.newRequestQueue(this)
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
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
            },
            { error ->
                Log.e("Volley", "Error: ${error.message}")
            }
        )

        queue.add(jsonObjectRequest)
    }








}
