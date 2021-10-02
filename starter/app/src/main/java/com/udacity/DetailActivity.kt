package com.udacity

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        file_name_tv.text = intent.getStringExtra(getString(R.string.file_name_param)).toString()
        val status = intent.getStringExtra(getString(R.string.status_param)).toString()
        status_tv.text = status
        status_tv.setTextColor(getColor(status))
        detail_button.setOnClickListener {
            finish()
        }
    }

    private fun getColor(status: String): Int {
        return when (status) {
            getString(R.string.status_success) -> Color.GREEN
            else -> Color.RED
        }
    }

}
