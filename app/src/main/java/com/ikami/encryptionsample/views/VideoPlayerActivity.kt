package com.ikami.encryptionsample.views

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.TransferListener
import com.ikami.encryptionsample.databinding.ActivityVideoPlayerBinding
import com.ikami.encryptionsample.utils.DeviceIdEncryptionUtil
import com.ikami.encryptionsample.utils.EncryptedFileDataSource
import java.io.File
import java.io.IOException
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

const val FILE_PATH = "filePath"
class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoPlayerBinding
    private var exoPlayer: SimpleExoPlayer? = null
    private val deviceIdEncryptionUtils = DeviceIdEncryptionUtil()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
        playVideo()
    }

    @Throws(IOException::class)
    fun getFileFromAssets(context: Context, fileName: String): File = File(context.cacheDir, fileName)
        .also {
            if (!it.exists()) {
                it.outputStream().use { cache ->
                    context.assets.open(fileName).use { inputStream ->
                        inputStream.copyTo(cache)
                    }
                }
            }
        }


    private fun playVideo(){
        val absolutePath = intent.extras?.getString(FILE_PATH)
        val secretKey = deviceIdEncryptionUtils.getDeviceIMEI(this)

        val secretKeySpec = SecretKeySpec(secretKey.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)

        val fileDataSource = EncryptedFileDataSource(cipher, object : TransferListener {
            override fun onTransferInitializing(
                source: DataSource,
                dataSpec: DataSpec,
                isNetwork: Boolean
            ) {
            }

            override fun onTransferStart(
                source: DataSource,
                dataSpec: DataSpec,
                isNetwork: Boolean
            ) {
            }

            override fun onBytesTransferred(
                source: DataSource,
                dataSpec: DataSpec,
                isNetwork: Boolean,
                bytesTransferred: Int
            ) {
            }

            override fun onTransferEnd(source: DataSource, dataSpec: DataSpec, isNetwork: Boolean) {
            }
        })

        val dataSpec = DataSpec(Uri.parse(absolutePath))
        fileDataSource.open(dataSpec)

        val dataSourceFactory = DataSource.Factory {
            fileDataSource
        }

        val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(fileDataSource.uri!!))

        exoPlayer = SimpleExoPlayer.Builder(this).build().apply {
            setMediaSource(videoSource)
            prepare()
            playWhenReady = true
        }

        binding.playerContainer.videoPlayer.player = exoPlayer
    }

    override fun onStop() {
        binding.playerContainer.videoPlayer.player = null
        exoPlayer?.release()
        exoPlayer = null
        super.onStop()
    }
}