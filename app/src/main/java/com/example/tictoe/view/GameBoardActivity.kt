package com.example.tictoe.view

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.gridlayout.widget.GridLayout
import com.example.tictoe.R

class GameBoardActivity : AppCompatActivity() {
    private val size = 16
    private lateinit var gridLayout: GridLayout
    private var isXTurn = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_board)

        gridLayout = findViewById(R.id.gridBoard)
        gridLayout.removeAllViews()
        gridLayout.rowCount = size
        gridLayout.columnCount = size

        for (row in 0 until size) {
            for (col in 0 until size) {
                val cell = TextView(this).apply {
                    id = View.generateViewId()
                    layoutParams = GridLayout.LayoutParams(
                        GridLayout.spec(row, 1f),
                        GridLayout.spec(col, 1f)
                    ).apply {
                        width = 0
                        height = 0
                        setMargins(2, 2, 2, 2)
                    }
                    setBackgroundResource(R.drawable.bg_board_cell)
                    gravity = Gravity.CENTER
                    textSize = 16f
                    setOnClickListener {
                        if (text.isEmpty()) {
                            if (isXTurn) {
                                text = "X"
                                setTextColor(Color.parseColor("#FFD600"))
                            } else {
                                text = "O"
                                setTextColor(Color.parseColor("#4EE6FA"))
                            }
                            isXTurn = !isXTurn
                        }
                    }
                }
                gridLayout.addView(cell)
            }
        }
    }
} 