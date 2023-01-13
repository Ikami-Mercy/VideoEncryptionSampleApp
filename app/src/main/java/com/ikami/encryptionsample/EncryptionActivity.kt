package com.ikami.encryptionsample

import android.content.ContextWrapper
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.SecretKeySpec

class EncryptionActivity : AppCompatActivity() {

    // on below line creating a variable
    // for key, image view, and buttons.
    private val key: String = "KERQIRUDYTH"
    lateinit var imageView: ImageView
    lateinit var encryptButton: Button
    lateinit var decryptButton: Button
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_encryption)
        encryptButton = findViewById(R.id.idBtnEncrypt)
        decryptButton = findViewById(R.id.idBtnDecrypt)
        imageView = findViewById(R.id.iVimage)

        // Setup image picker launcher
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { imageUri ->
            // on below line we are encrypting our image.
            try {
                imageUri?.let { encrypt(it) }
                // on below line we are encrypting our image.
                Toast.makeText(this, "Image encrypted..", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Fail to encrypt image : $e", Toast.LENGTH_SHORT).show()
            }
            }

        // Setup SELECT IMAGE button
        encryptButton.setOnClickListener {
            pickImageLauncher.launch("image/jpeg")
        }


        decryptButton.setOnClickListener {
            // method to decrypt image.
            decrypt()
        }
    }


    private fun decrypt() {
        // on below line creating and initializing
        // variable for context wrapper.
        val contextWrapper = ContextWrapper(application)

        // on below line creating a file for getting photo directory.
        val photoDir = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_DCIM)

        // on below line creating a new file for encrypted image.
        val file = File(photoDir, "encfile" + ".png")

        // on below line creating input stream for file with file path.
        val fis = FileInputStream(file.path)

        // on below line creating a file for decrypted image.
        val decFile = File(photoDir, "decfile.png")

        // on below line creating an file output
        // stream for decrypted image.
        val fos = FileOutputStream(decFile.path)
        val bytes = key.toByteArray()

        // creating a variable for secret key and passing
        // our secret key and algorithm for encryption.
        val sks = SecretKeySpec(bytes, "AES")

        // on below line creating a variable for
        // cipher and initializing it
        val cipher = Cipher.getInstance("AES")

        // on below line initializing cipher and
        // specifying decrypt mode to decrypt.
        cipher.init(Cipher.DECRYPT_MODE, sks)

        // on below line creating a variable
        // for cipher input stream.
        val cis = CipherInputStream(fis, cipher)

        // on below line creating a variable b.
        val d = ByteArray(8)
        var b: Int= cis.read(d)
        while (b != -1) {
            fos.write(d, 0, b)
        }

        // on below line flushing our fos,
        // closing fos and closing cis.
        fos.flush()
        fos.close()
        cis.close()

        // displaying toast message.
        Toast.makeText(this, "Image decrypted successfully..", Toast.LENGTH_SHORT).show()

        // on below line creating an image file
        // from decrypted image file path.
        val imgFile = File(decFile.path)
        if (imgFile.exists()) {
            // creating bitmap for image and displaying
            // that bitmap in our image view.
            val bitmap = BitmapFactory.decodeFile(imgFile.path)
            imageView.setImageBitmap(bitmap)
        }
    }

    // on below line creating a method to encrypt an image.
    private fun encrypt(uri: Uri) {
        // on below line creating a
        // variable for file input stream
       // val fis = FileInputStream(path)
        val fis = contentResolver.openInputStream(uri)

        // on below line creating a variable for context  wrapper.
        val contextWrapper = ContextWrapper(application)

        // on below line creating a variable for file
        val photoDir = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_DCIM)

        // on below line creating a file for encrypted file.
        val file = File(photoDir, "encfile" + ".png")

        // on below line creating a variable for file output stream.
        val fos = FileOutputStream(file.path)

        // on below line creating a variable for secret key.
        // creating a variable for secret key and passing our
        // secret key and algorithm for encryption.
        val sks = SecretKeySpec(key.toByteArray(), "AES")

        // on below line creating a variable for
        // cipher and initializing it
        val cipher = Cipher.getInstance("AES")

        // on below line initializing cipher and
        // specifying decrypt mode to encrypt.
        cipher.init(Cipher.ENCRYPT_MODE, sks)

        // on below line creating cos
        val cos = CipherOutputStream(fos, cipher)
        fos.use { output ->
            fis.use { input ->
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
//        val d = ByteArray(8)
//        var b: Int = fis.read(d)
//        while (b != -1) {
//            fos.write(d, 0, b)
//        }
//
//        // on below line
//        // closing our cos and fis.
//        cos.flush()
//        cos.close()
//        fis.close()
    }

}
