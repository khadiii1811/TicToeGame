package com.example.tictoe.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.tictoe.R
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu_tictor)

        findViewById<Button>(R.id.btnVsBot).setOnClickListener {
            startActivity(Intent(this, GameBoardActivity::class.java))
        }
    }
}