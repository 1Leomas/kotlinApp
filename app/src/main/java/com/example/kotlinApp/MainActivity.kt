package com.example.kotlinApp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import com.example.kotlinApp.databinding.ActivityMainBinding
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMainBinding

    private lateinit var searchInput: EditText
    private lateinit var buttonSearch: Button

    lateinit var buttonNotification: Button
    lateinit var builder: Notification.Builder
    private val channelId = "myapp.notifications"
    private val description = "Notification App Example"

    lateinit var photoButton: Button

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        search()

        notificationShortCode()

        photo()

    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }


    private fun photo()
    {
        photoButton = findViewById(R.id.open_camera_button)

        photoButton.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }
    }

    private fun search() {
        searchInput = findViewById(R.id.input_search)
        buttonSearch = findViewById(R.id.buttonSearch)

        buttonSearch.setOnClickListener {
            val searchKey = searchInput.text.toString()
            val urlIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/search?q=$searchKey")
            )
            startActivity(urlIntent)
        }
    }

    //RequiresApi indica versiunea minima a SDK pentru a executa functia
    //in cazul dat e 'O' echivalent cu SDK 26
    @RequiresApi(Build.VERSION_CODES.O)
    private fun notificationShortCode() {
        buttonNotification = findViewById(R.id.buttonPush)

        var notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        buttonNotification.setOnClickListener {

            val notificationChannel = NotificationChannel(
                channelId,
                description,
                NotificationManager.IMPORTANCE_HIGH
            )

            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification
                .Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_baseline_notifications_24)
                .setContentTitle("My Notification")
                .setContentText("Salut")

            //In primul parametru indicam cat timp va tine timer-ul in milisecunde
            //Eu aici am pus o secunda
            object : CountDownTimer(3000, 1000) {

                override fun onTick(millisUntilFinished: Long) { }

                override fun onFinish() {
                    notificationManager.notify(1234, builder.build())
                }
            }.start()
        }
    }

    private fun notification() {
        buttonNotification = findViewById(R.id.buttonPush)

        var notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        //Intent este o actiune, in acest cod este utilizat ca cand vom face
        //click pe notificare sa ne arunce pe alta activitate,
        //in cazul nostru activitatea sub numele NotificationActivity
        val intent = Intent(this, NotificationActivity::class.java)

        buttonNotification.setOnClickListener {

            findViewById<TextView>(R.id.textView).setText(Build.VERSION_CODES.O.toString())

            val pendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)

            //if-ul acesta verifica versiunea la SDK, la mine e 31
            //Verifica daca versiunea e mai mare ca 26
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(
                    channelId,
                    description,
                    NotificationManager.IMPORTANCE_HIGH
                )

                notificationManager.createNotificationChannel(notificationChannel)

                builder = Notification
                    .Builder(this, channelId)
                    .setSmallIcon(R.drawable.ic_baseline_notifications_24)
                    .setLargeIcon(
                        BitmapFactory.decodeResource(
                            this.resources,
                            R.drawable.ic_baseline_notifications_24
                        )
                    )
                    .setContentIntent(pendingIntent)
                    .setContentTitle("My Notification")
                    .setContentText("Salut")

            } /*else {

                builder = Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_baseline_notifications_24)
                    .setLargeIcon(
                        BitmapFactory.decodeResource(
                            this.resources,
                            R.drawable.ic_baseline_notifications_24
                        )
                    )
                    .setContentIntent(pendingIntent)
                    .setContentTitle("My Notification")
                    .setContentText("Salut")
            }*/

            object : CountDownTimer(1000, 1000) {

                // Callback function, fired on regular interval
                override fun onTick(millisUntilFinished: Long) { }

                // Callback function, fired when the time is up
                override fun onFinish() {
                    notificationManager.notify(1234, builder.build())
                }
            }.start()
        }
    }

}

