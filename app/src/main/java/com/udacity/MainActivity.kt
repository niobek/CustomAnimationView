package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

private val NOTIFICATION_ID = 0
private val REQUEST_CODE = 0
private val FLAGS = 0

class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private lateinit var fileRadioGroup:RadioGroup
    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action
    private var fileChosen = URL1
    private var downloadStat:String = "FAILID"
    private lateinit var downloadButton:LoadingButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        downloadButton = findViewById(R.id.custom_button)
        fileRadioGroup = findViewById(R.id.radio_download)
        setFilesToDownload()

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        custom_button.setOnClickListener {
            if (fileRadioGroup.checkedRadioButtonId == -1)
            {
                custom_button.changeButtonState(ButtonState.Clicked)
                Toast.makeText(this, "Please select a file to download!!", Toast.LENGTH_LONG).show()

            } else {
                custom_button.changeButtonState(ButtonState.Loading)
                download()
            }
        }

        createChannel(getString(R.string.download_channel_id), getString(R.string.download_channel_name))
    }

    private fun setFilesToDownload() {
        val file1 = fileRadioGroup.get(0) as RadioButton
        file1.setText(URL1)
        val file2 = fileRadioGroup.get(1) as RadioButton
        file2.setText(URL2)
        val file3 = fileRadioGroup.get(2) as RadioButton
        file3.setText(URL3)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadID)
            {
                downloadButton.isEnabled = true
                val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                val query= DownloadManager.Query()
                query.setFilterById(downloadID)
                 val c = downloadManager.query(query);
                if (c.moveToFirst()) {
                    val columnIndex = c
                            .getColumnIndex(DownloadManager.COLUMN_STATUS);
                    val downloadStatus = c
                        .getInt(columnIndex)
                    when (downloadStatus) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            //val uri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                            Toast.makeText(context, "Download Successed", Toast.LENGTH_LONG).show()
                            downloadStat = "SUCCESS"
                        }
                        DownloadManager.STATUS_FAILED-> {
                            Toast.makeText(context, "Download Failed", Toast.LENGTH_LONG).show()
                            downloadStat = "FAILED"
                        }

                        DownloadManager.STATUS_PAUSED->{
                            Toast.makeText(context, "Download Paused", Toast.LENGTH_LONG).show()
                            downloadStat = "PAUSED"
                        }

                        DownloadManager.STATUS_PENDING->{
                            Toast.makeText(context, "Download Pending", Toast.LENGTH_LONG).show()
                            downloadStat = "PENDING"
                        }

                    }
                    downloadButton.changeButtonState(ButtonState.Completed)
                    sendNotification("Download Completed")
                }
        }
        }
    }

    private fun download() {

        fileChosen = when (fileRadioGroup.checkedRadioButtonId)
        {
            fileRadioGroup.get(0).id -> URL1
            fileRadioGroup.get(1).id -> URL2
            fileRadioGroup.get(2).id -> URL3
            else -> ""
        }
        val request =
            DownloadManager.Request(Uri.parse(fileChosen))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
        downloadButton.isEnabled = false
    }

    fun sendNotification(messageBody: String) {

        notificationManager = ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager
        notificationManager.cancelAll()

        val contentIntent = Intent(applicationContext, DetailActivity::class.java)
        contentIntent.putExtra("status", downloadStat)
        contentIntent.putExtra("fileName", fileChosen)

        pendingIntent = PendingIntent.getActivity(
            this,
            NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val downloadImage = BitmapFactory.decodeResource(
            applicationContext.resources,
            R.drawable.ic_assistant_black_24dp
        )
        val bigPicStyle = NotificationCompat.BigPictureStyle()
            .bigPicture(downloadImage)
            .bigLargeIcon(null)

        action = NotificationCompat.Action.Builder(R.drawable.ic_assistant_black_24dp, getString(R.string.notification_button), pendingIntent)
            .build()

        val builder = NotificationCompat.Builder(
            applicationContext,
            getString(R.string.download_channel_id)
        )

            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(applicationContext
                .getString(R.string.notification_title))

            .setContentText(messageBody)
            //.setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(bigPicStyle)
            .setLargeIcon(downloadImage)
            .addAction(action)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }


    private fun createChannel(channelId: String, channelName: String) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
                .apply {
                    setShowBadge(false)
                }

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.notification_description)

            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)

        }

    }

    companion object {
        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val URL1 = "https://github.com/bumptech/glide"
        private const val URL2 = "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter"
        private const val URL3 = "https://github.com/square/retrofit"
        private const val CHANNEL_ID = "channelId"
    }


}
