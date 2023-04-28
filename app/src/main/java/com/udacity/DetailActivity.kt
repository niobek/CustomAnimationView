package com.udacity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity() {

    private lateinit var downloadStatusText:TextView
    private lateinit var downloadFileText:TextView
    private lateinit var okButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)
        downloadStatusText = findViewById(R.id.textStatus)
        val status = intent.getStringExtra("status")
        status?.let {
            downloadStatusText.setText(status)
        }

        downloadFileText = findViewById(R.id.textFileName)
        val fileName = intent.getStringExtra("fileName")
        fileName?.let {
            downloadFileText.setText(fileName)
        }

        okButton = findViewById(R.id.buttonOk)
        okButton.setOnClickListener {

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
