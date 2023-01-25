package com.ikami.encryptionsample.workers

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.net.toUri
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.ikami.encryptionsample.utils.Constants
import com.ikami.encryptionsample.utils.Constants.ASSET_VIDEO_DIRECTORY
import com.ikami.encryptionsample.utils.DeviceIdEncryptionUtil
import com.ikami.encryptionsample.utils.EncryptionUtil


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
        val outputData = workDataOf("is_success" to true)
        return Result.success(outputData)

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

    private fun folderFilesDecryption() {
        Log.e("folderFilesDecryption", "called--->")

        val videoFileDirectory = fetchVideosFromAssets(applicationContext)

        for (i in videoFileDirectory.indices) {
            Log.e("Files", "FilePath:" + videoFileDirectory[i])
            val uri = applicationContext.assets.open(videoFileDirectory[i])
            uri .let { it1 ->
                    encryptionUtils.decryptVideo(
                        it1,
                        masterKey,
                        i,
                        applicationContext
                    )
                }
        }
    }

    private fun folderFilesEncryption() {
        Log.e("folderFilesEncryption", "called--->")

        val videoFileDirectory = applicationContext.getDir("MasterKeyDecryptionVids", Context.MODE_PRIVATE)

        if (!videoFileDirectory.exists()) {
            videoFileDirectory.mkdirs()
        }
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


    private fun fetchVideosFromAssets(context: Context): ArrayList<String> {
        return getListOfFilesFromAsset(ASSET_VIDEO_DIRECTORY, context)
    }
    private fun getListOfFilesFromAsset(path: String, context: Context): ArrayList<String> {
        val listOfAudioFiles = ArrayList<String>()
        context.assets.list(path)?.forEach { file ->
            val innerFiles = getListOfFilesFromAsset("$path/$file", context)
            if (innerFiles.isNotEmpty()) {
                listOfAudioFiles.addAll(innerFiles)
            } else {
                // it can be an empty folder or file you don't like, you can check it here
                listOfAudioFiles.add("$path/$file")
            }
        }
        return listOfAudioFiles
    }
}