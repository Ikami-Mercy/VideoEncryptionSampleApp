package com.ikami.encryptionsample.utils

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Log
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.spec.SecretKeySpec

class DeviceIdEncryptionUtil {

    open fun encryptFile(
        context: Context,
        encryptionKey: String,
        base64Video: InputStream,
        counter: Int
    ) {
        try {
            val secretKeySpec = SecretKeySpec(encryptionKey.toByteArray(), "AES")
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
            val inputBytes = ByteArray(base64Video.available())
            Log.e(this::class.java.simpleName, "inputBytes22222 size: ${inputBytes.size}")
            saveEncryptedVideo(context, cipher, base64Video, counter)
        } catch (e: java.security.NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: javax.crypto.NoSuchPaddingException) {
            e.printStackTrace()
        } catch (e: java.security.InvalidKeyException) {
            e.printStackTrace()
        } catch (e: javax.crypto.BadPaddingException) {
            e.printStackTrace()
        } catch (e: javax.crypto.IllegalBlockSizeException) {
            e.printStackTrace()
        }

    }

    @SuppressLint("HardwareIds")
    open fun getDeviceIMEI(ctx: Context): String {
        var deviceID = Settings.Secure.getString(
            ctx.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        if (deviceID.length > 16) {
            deviceID = deviceID.substring(0, 16)
        }

        Log.i("deviceID", deviceID.substring(0, 5))
        return deviceID
    }

    private fun saveEncryptedVideo(
        context: Context,
        cipher: Cipher,
        base64Video: InputStream,
        counter: Int
    ) {
        val videoFileDirectory = context.getDir("uLessonEncryptedVideos", Context.MODE_PRIVATE)
        try {
            val fileObject = File(videoFileDirectory, "encryptedVideoFile$counter.mp4")
            fileObject.createNewFile()


            val fo = FileOutputStream(fileObject)
            val bufferedOutputStream = BufferedOutputStream(fo)

            val cipherOutputStream = CipherOutputStream(bufferedOutputStream, cipher)
            val byteArray = ByteArray(base64Video.available())
            base64Video.read(byteArray)

            byteArray.let {
                cipherOutputStream.write(it)
                cipherOutputStream.flush()
                cipherOutputStream.close()
            }

            Log.e(this::class.java.simpleName, "Encrypted successfully")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(this::class.java.simpleName, "encryption exception: $e")
        }
    }
}