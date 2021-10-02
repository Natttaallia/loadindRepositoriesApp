package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.view.children
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        notificationManager = getSystemService(
            NotificationManager::class.java
        ) as NotificationManager

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        createChannelForNotification()

        custom_button.setOnClickListener {
            download()
        }
    }

    private fun createChannelForNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.channelName),
                NotificationManager.IMPORTANCE_HIGH
            )

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.channel_description)

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadID == id) {
                val query = DownloadManager.Query()
                    .setFilterById(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0))
                val downloadManager = context!!.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val cursor = downloadManager.query(query)

                if (cursor.moveToFirst()) {
                    val status = cursor.getInt(
                        cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    )
                    val fileName = cursor.getString(
                        cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)
                    )
                    notificationManager.cancelAll()
                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            notificationManager.sendNotification(fileName, getString(R.string.status_success))
                        }
                        DownloadManager.STATUS_FAILED -> {
                            notificationManager.sendNotification(fileName, getString(R.string.status_failed))
                        }
                    }
                }
                radio_group.children.iterator().forEach { it.isEnabled = true }
                custom_button.setState(ButtonState.Completed)
            }
        }
    }

    fun NotificationManager.sendNotification(fileName: String, status: String) {
        val contentIntent = Intent(applicationContext, DetailActivity::class.java)
        contentIntent.apply {
            putExtra(getString(R.string.file_name_param), fileName)
            putExtra(getString(R.string.status_param), status)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = NotificationCompat.Builder(
            applicationContext,
            CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(applicationContext
                .getString(R.string.notification_title))
            .setContentText(applicationContext
                .getString(R.string.notification_description))
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .addAction(
                0,
                applicationContext.getString(R.string.notification_button),
                contentPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        notify(NOTIFICATION_ID, builder.build())
    }

    private fun download() {
        custom_button.setState(ButtonState.Clicked)
        val link = getCurrentLink()
        if (link.isEmpty()) {
            showEmptyLinkToast()
            custom_button.setState(ButtonState.Completed)
        } else {
            custom_button.setState(ButtonState.Loading)
            radio_group.children.iterator().forEach { it.isEnabled = false }
            val request =
                DownloadManager.Request(Uri.parse(link))
                    .setTitle(getString(R.string.app_name))
                    .setDescription(getString(R.string.app_description))
                    .setRequiresCharging(false)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)

            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadID =
                downloadManager.enqueue(request)// enqueue puts the download request in the queue.
        }
    }

    private fun getCurrentLink(): String {
        return when (radio_group?.checkedRadioButtonId) {
            R.id.glide_download_btn -> URL_GLIDE
            R.id.load_app_download_btn -> URL
            R.id.retrofit_download_btn -> URL_RETROFIT
            else -> ""
        }
    }

    private fun showEmptyLinkToast() {
        Toast.makeText(this, getString(R.string.empty_link), Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val URL_GLIDE =
            "https://github.com/bumptech/glide/archive/master.zip"
        private const val URL_RETROFIT =
            "https://github.com/square/retrofit/archive/master.zip"
        private const val CHANNEL_ID = "channelId"
        private const val NOTIFICATION_ID = 0
    }

}
