package com.young.lineloginsample

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.jp22601.login.IntentActivity
import com.example.jp22601.myapplication.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // get reference to button
        val btn_main_login = findViewById(R.id.btn_main_next) as Button
        // set on click listener
        btn_main_login.setOnClickListener {

            // define intent activity
            val intent = Intent(this, IntentActivity::class.java)

            // begin
            startActivity(intent)

        }
    }
}
