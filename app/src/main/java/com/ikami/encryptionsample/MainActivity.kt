package com.ikami.encryptionsample

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.ikami.encryptionsample.utils.Constants
import com.ikami.encryptionsample.utils.EncryptionUtil
import com.ikami.encryptionsample.utils.FileUtil
import com.ikami.encryptionsample.workers.DecryptionWorker
import java.io.File


class MainActivity : AppCompatActivity() {
    private lateinit var pickVideoLauncher: ActivityResultLauncher<String>
    private lateinit var pickTextFileLauncher: ActivityResultLauncher<String>
    private val encryptionUtils = EncryptionUtil()
    private val masterKey = Constants.MASTER_KEY
    private var videoFileCount: Int = 1
    private var decryptedVideoFileCount: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeUI()

    }


    @SuppressLint("SuspiciousIndentation")
    private fun initializeUI() {
        println("initializeUI called ======")


        // Setup video picker launcher
        pickVideoLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { videoUri ->
                videoUri?.let {

                    Log.e("Encode video file called ======", "TRUE")

                    contentResolver.openInputStream(it)
                        ?.let { it1 ->
                            encryptionUtils.encryptVideo(it1, masterKey, videoFileCount, this)
                            videoFileCount++

                        }


                }
            }
        // Setup file picker launcher
        pickTextFileLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { fileUri ->
                fileUri?.let {

                    Log.e("file picker URI path is ======", "${it.path}")
                    //  readFile(it)
                    contentResolver.openInputStream(it)
                        ?.let { it1 ->
                            encryptionUtils.decryptVideo(
                                it1,
                                masterKey,
                                decryptedVideoFileCount,
                                this
                            )
                            decryptedVideoFileCount++
                        }

                }
            }


        // Setup SELECT VIDEO button
        findViewById<Button>(R.id.button_select_video).setOnClickListener {
            askForPermissions()
            pickVideoLauncher.launch("video/*")

        }

        findViewById<Button>(R.id.button_decrypt_video).setOnClickListener {
            askForPermissions()
            pickTextFileLauncher.launch("text/plain")

        }

        findViewById<Button>(R.id.button_decrypt_multi_video).setOnClickListener {
            //askForPermissions()
            // Create a Work Request
            val uploadWorkRequest: WorkRequest = OneTimeWorkRequestBuilder<DecryptionWorker>().build()
            WorkManager.getInstance().enqueue(uploadWorkRequest)
            val workInfo = WorkManager.getInstance().getWorkInfoById(uploadWorkRequest.id).get()
            val wasSuccess = workInfo.outputData.getBoolean("is_success", false)
            //Toast.makeText(this, "Decryption successful:-> $wasSuccess", Toast.LENGTH_LONG).show()

        }
        findViewById<Button>(R.id.button_lesson_view).setOnClickListener {
            startActivity(Intent(this, LessonViewActivity::class.java))
        }

    }


    private fun askForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
                return
            }
            createDir()
        } else {
            createDir()
        }
    }

    private fun createDir() {
        val decryptionDirectory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            FileUtil.DOCUMENT_DECRYPTION_DIRECTORY
        )

        val encryptionDirectory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            FileUtil.DOCUMENT_ENCRYPTION_DIRECTORY
        )
        if (!decryptionDirectory.exists()) {
            decryptionDirectory.mkdirs()
        }
        if (!encryptionDirectory.exists()) {
            encryptionDirectory.mkdirs()
        }
    }

    @SuppressLint("Range")
    private fun getFileNameFromUri(uri: Uri): String {
        // return File(uri.path).name

        var result = ""
        if (uri.scheme.equals("content")) {
            val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
            cursor.use { it ->
                if (it != null && it.moveToFirst()) {
                    result = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        return result

    }

}
