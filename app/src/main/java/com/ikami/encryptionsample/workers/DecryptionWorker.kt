package com.ikami.encryptionsample.workers

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ikami.encryptionsample.utils.Constants
import com.ikami.encryptionsample.utils.DeviceIdEncryptionUtil
import com.ikami.encryptionsample.utils.EncryptionUtil
import com.ikami.encryptionsample.utils.FileUtil
import java.io.File


private val encryptionUtils = EncryptionUtil()
private val deviceIdEncryptionUtils = DeviceIdEncryptionUtil()
const val masterKey = Constants.MASTER_KEY
private lateinit var manager: NotificationManager


class DecryptionWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {

        showNotification(
            "Pre loaded content sync",
            "Preparing videos....."
        )

        folderFilesDecryption()
        folderFilesEncryption()
        cancelNotification()
        return Result.success()

    }

    private fun showNotification(task: String, desc: String) {
        manager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "decryption_channel"
        val channelName = "task_decryption"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }
        val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(
                applicationContext,
                channelId
            )
                .setContentTitle(task)
                .setContentText(desc)
                .setSmallIcon(R.drawable.btn_star)
                .setPriority(Notification.PRIORITY_HIGH)


        } else {
            TODO("VERSION.SDK_INT < O")
        }

        manager.notify(1, builder.build())
    }

    private fun cancelNotification() {
        Log.e(TAG, " Cancelling notification")
        manager.cancelAll()
    }

    private fun folderFilesDecryption(){
        Log.e("folderFilesDecryption", "called--->")
        val videoFileDirectory = File(applicationContext.filesDir, "masterKeyEncryptedVideos")

        val files = videoFileDirectory.listFiles()
        val fileSize = files.size
        Log.e("Encrypted Files", "Size: $fileSize")
        for (i in files.indices) {
            Log.e("Files", "FileName:" + files[i].name)
            val fileUri = files[i].toUri()
            applicationContext.contentResolver.openInputStream(fileUri)
                ?.let { it1 ->
                    encryptionUtils.decryptVideo(
                        it1,
                        masterKey,
                        i,
                        applicationContext
                    )
                }
        }
    }

    private fun folderFilesEncryption(){
        Log.e("folderFilesEncryption", "called--->")

        val videoFileDirectory = File(applicationContext.filesDir, "masterKeyEncryptedVideos")
        val files = videoFileDirectory.listFiles()
        val fileSize = files.size
        Log.e("Decrypted Files", "Size: $fileSize")
        for (i in files.indices) {
            Log.e("Files", "FileName:" + files[i].name)
            val fileUri = files[i].toUri()
            applicationContext.contentResolver.openInputStream(fileUri)
                ?.let { it1 ->
                    deviceIdEncryptionUtils.encryptFile(
                      applicationContext,
                        deviceIdEncryptionUtils.getDeviceIMEI(applicationContext),
                        it1,
                        i
                    )
                }
        }
    }
}