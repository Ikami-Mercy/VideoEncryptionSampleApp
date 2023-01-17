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
import android.util.Base64
import android.util.Log
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.ikami.encryptionsample.utils.EncryptionUtil
import com.ikami.encryptionsample.utils.FileUtil
import java.io.*


class MainActivity : AppCompatActivity() {
    private var encryptedVideoFile = ""
    private lateinit var masterKey: MasterKey
    private lateinit var pickVideoLauncher: ActivityResultLauncher<String>
    private lateinit var pickTextFileLauncher: ActivityResultLauncher<String>
    private lateinit var encryptedVideoString: String
    private val util = FileUtil()
    private val encryptionUtils = EncryptionUtil()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeUI()

    }


    private fun initializeUI() {
        println("initializeUI called ======")


        // Setup video picker launcher
        pickVideoLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { videoUri ->
                videoUri?.let {

                    Log.e("Encode video file called ======", "TRUE")
                    //encode my video file
//                    val encodedVideo = Base64.encode(
//                        contentResolver.openInputStream(it)?.let { videoUri ->
//                            util.getFileBytes(
//                                videoUri
//                            )
//                        }, NO_PADDING
//                    )
//                    Log.e("Encode video file successful ======", "TRUE")

                    //  encryptionUtils.encryptFile("AAAAAAAAAAAAAAAA", encodedVideo)
                    contentResolver.openInputStream(it)
                        ?.let { it1 -> encryptionUtils.encryptVideo(it1, "AAAAAAAAAAAAAAAA") }


                }
            }
        // Setup file picker launcher
        pickTextFileLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { fileUri ->
                fileUri?.let {

                    Log.e("file picker URI path is ======", "${it.path}")
                    //  readFile(it)
                    contentResolver.openInputStream(it)
                        ?.let { it1 -> encryptionUtils.decryptVideo(it1, "AAAAAAAAAAAAAAAA") }

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

//            val decryptedEncodedVideoString =
//                encryptionUtils.decryptFile("AAAAAAAAAAAAAAAA", encryptedVideoString)


        }
    }

    private fun readFile(uri: Uri): String {
        Log.e("readFile is called", "TRue")
        var content = ""
        try {
            val readFileContent = contentResolver.openInputStream(uri)?.let {
                util.getFileBytes(it)
            }

            content = readFileContent?.let { String(it) }.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        Log.e("readFileContent size is ", "${content.length}")
        encryptedVideoString = content
        var decryptedEncodedVideoString =
            encryptionUtils.decryptFile("AAAAAAAAAAAAAAAA", this.encryptedVideoString)
        Log.e("Save decrypted, decoded video file locally", "Called!!!!")
        util.saveFile(
            "decryptedEncodedVideoFile.mp4",
            Base64.decode(decryptedEncodedVideoString, 0)
        )
        return this.encryptedVideoString
    }

    private fun askForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
                return
            }
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

    /**
     * Save video as encrypted file from [videoUri].
     */
    private fun saveEncryptedVideo(videoUri: Uri) {
        val uniqueName = getFileNameFromUri(videoUri)
        println("the file unique name is $uniqueName")
        encryptedVideoFile = "encrypted_video_$uniqueName.txt"
        val root = Environment.getExternalStorageDirectory().toString()

        deleteFileIfExist(encryptedVideoFile, root)
        println("saveEncryptedVideo method called $videoUri")
        val encryptedFile = EncryptedFile.Builder(
            applicationContext,
            File(filesDir, encryptedVideoFile),
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        val videoInputStream = contentResolver.openInputStream(videoUri)
        writeFile(encryptedFile.openFileOutput(), videoInputStream)
    }

    /**
     * Delete file with name [filename] in internal storage if exist.
     */
    private fun deleteFileIfExist(filename: String, storagePath: String) {
        val file = File(filesDir, filename)
        // val file = File(storagePath, filename)
        if (file.exists()) {
            file.delete()
        }
    }

    /**
     * Write [inputStream] into [outputStream].
     */
    private fun writeFile(outputStream: FileOutputStream, inputStream: InputStream?) {
        println("writeFile method called ++++++++++++==============")
        outputStream.use { output ->
            inputStream.use { input ->
                input?.let {
                    val buffer =
                        ByteArray(4 * 1024) // buffer size
                    while (true) {
                        val byteCount = input.read(buffer)
                        if (byteCount < 0) break
                        output.write(buffer, 0, byteCount)
                    }
                    output.flush()
                }
            }
        }
    }

    private fun readFile() {
        println("readFile method called ++++++++++++==============")
        var encryptedVideoFilePath = "encrypted_video_cat_four.mp4.txt"
        val encryptedFile = EncryptedFile.Builder(
            applicationContext,
            File(filesDir, encryptedVideoFilePath),
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        val inputStream = encryptedFile.openFileInput()
        val byteArrayOutputStream = ByteArrayOutputStream()
        var nextByte: Int = inputStream.read()
        while (nextByte != -1) {
            byteArrayOutputStream.write(nextByte)
            nextByte = inputStream.read()
        }

        val plainVideo: ByteArray = byteArrayOutputStream.toByteArray()
        convertBytesToFile(plainVideo)

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

    private fun convertBytesToFile(bytearray: ByteArray) {
        try {
            val outputFile = File.createTempFile("decrypted_video", ".mp4", cacheDir)
            outputFile.deleteOnExit()
            val fileOutputStream = FileOutputStream("decrypted_videos")
            fileOutputStream.write(bytearray)
            fileOutputStream.close()


        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

}
