package com.dicoding.habitapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dicoding.habitapp.R
import com.dicoding.habitapp.ui.detail.DetailHabitActivity
import com.dicoding.habitapp.utils.HABIT_ID
import com.dicoding.habitapp.utils.HABIT_TITLE
import com.dicoding.habitapp.utils.NOTIFICATION_CHANNEL_ID

class NotificationWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    private val habitId = inputData.getInt(HABIT_ID, 0)
    private val habitTitle = inputData.getString(HABIT_TITLE)

    override fun doWork(): Result {
        val prefManager = androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val shouldNotify = prefManager.getBoolean(applicationContext.getString(R.string.pref_key_notify), false)

        //TODO 12 : If notification preference on, show notification with pending intent

        if (shouldNotify ) {
            if (habitTitle != null ) {

                val intent = Intent(
                    applicationContext,
                    DetailHabitActivity::class.java
                ).apply { putExtra(HABIT_ID , habitId) }

                val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

                val pendingIntent = android.app.TaskStackBuilder.create(applicationContext).run {
                    addNextIntentWithParentStack(intent)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        getPendingIntent(
                            0 ,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                    } else {

                        getPendingIntent(
                            0 ,
                            PendingIntent.FLAG_UPDATE_CURRENT)
                    }
                }

                val notificationCompactManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val notificationBuilder=  NotificationCompat.Builder(applicationContext , NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notifications)
                    .setContentText(applicationContext.getString(R.string.notify_content))
                    .setContentTitle(habitTitle)
                    .setContentIntent(pendingIntent)
                    .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                    .setSound(sound)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setAutoCancel(true)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel (
                        NOTIFICATION_CHANNEL_ID ,
                        "habit-notify" ,
                        NotificationManager.IMPORTANCE_DEFAULT
                    )

                    channel.enableVibration(true)
                    channel.vibrationPattern = longArrayOf(1000, 1000 , 1000 , 1000, 1000 , 1000)
                    notificationBuilder.setChannelId(NOTIFICATION_CHANNEL_ID)
                    notificationCompactManager.createNotificationChannel(channel)
                }

                val notification = notificationBuilder.build()

                notificationCompactManager.notify(1, notification)

            }

        }



        return Result.success()
    }

}
