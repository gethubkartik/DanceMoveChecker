//package com.kartikeya.DanceScoreAI
//
//import android.Manifest
//import android.annotation.SuppressLint
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.media.MediaPlayer
//import android.os.Bundle
//import androidx.annotation.RequiresPermission
//import androidx.appcompat.app.AppCompatActivity
//import androidx.camera.core.*
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.camera.video.*
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import com.kartikeya.DanceScoreAI.databinding.ActivityCameraBinding
//import java.io.File
//
//class CameraActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityCameraBinding
//
//    private var videoCapture: VideoCapture<Recorder>? = null
//    private var recording: Recording? = null
//    private var mediaPlayer: MediaPlayer? = null
//
//    private var songResId: Int = -1
//    private var isProcessingStarted = false   // 🔒 IMPORTANT FLAG
//
//    companion object {
//        private const val CAMERA_PERMISSION_CODE = 1001
//    }
//
//    @SuppressLint("MissingPermission")
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityCameraBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        // 🎵 Receive selected song
//        songResId = intent.getIntExtra("SONG_AUDIO", -1)
//
//        if (hasCameraPermission()) {
//            startCamera()
//        } else {
//            requestCameraPermission()
//        }
//
//        binding.btnBack.setOnClickListener {
//            if (recording != null) {
//                stopRecording()
//            } else {
//                stopSong()
//                finish()
//            }
//        }
//
//
//
//        binding.btnStartDance.setOnClickListener {
//            if (recording == null) {
//                startRecordingWithSong()
//            } else {
//                stopRecording()
//            }
//        }
//    }
//
//    // ---------------- PERMISSION ----------------
//
//    private fun hasCameraPermission(): Boolean {
//        return ContextCompat.checkSelfPermission(
//            this, Manifest.permission.CAMERA
//        ) == PackageManager.PERMISSION_GRANTED &&
//                ContextCompat.checkSelfPermission(
//                    this, Manifest.permission.RECORD_AUDIO
//                ) == PackageManager.PERMISSION_GRANTED
//    }
//
//    private fun requestCameraPermission() {
//        ActivityCompat.requestPermissions(
//            this,
//            arrayOf(
//                Manifest.permission.CAMERA,
//                Manifest.permission.RECORD_AUDIO
//            ),
//            CAMERA_PERMISSION_CODE
//        )
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        if (requestCode == CAMERA_PERMISSION_CODE &&
//            grantResults.all { it == PackageManager.PERMISSION_GRANTED }
//        ) {
//            startCamera()
//        }
//    }
//
//    // ---------------- CAMERA PREVIEW ----------------
//
//    private fun startCamera() {
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//
//        cameraProviderFuture.addListener({
//
//            val cameraProvider = cameraProviderFuture.get()
//
//            val preview = Preview.Builder()
//                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
//                .build().also {
//                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
//                }
//
//            val recorder = Recorder.Builder()
//                .setQualitySelector(QualitySelector.from(Quality.HD))
//                .build()
//
//            videoCapture = VideoCapture.withOutput(recorder)
//
//            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
//
//            cameraProvider.unbindAll()
//            cameraProvider.bindToLifecycle(
//                this,
//                cameraSelector,
//                preview,
//                videoCapture
//            )
//
//        }, ContextCompat.getMainExecutor(this))
//    }
//
//    // ---------------- RECORD + SONG ----------------
//
//    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
//    private fun startRecordingWithSong() {
//
//        isProcessingStarted = false
//        playSong()
//
//        val videoFile = File(
//            externalCacheDir,
//            "dance_${System.currentTimeMillis()}.mp4"
//        )
//
//        val outputOptions = FileOutputOptions.Builder(videoFile).build()
//
//        recording = videoCapture?.output
//            ?.prepareRecording(this, outputOptions)
//            ?.withAudioEnabled()
//            ?.start(ContextCompat.getMainExecutor(this)) { event ->
//
//                if (event is VideoRecordEvent.Finalize && !isProcessingStarted) {
//
//                    isProcessingStarted = true
//                    recording = null
//
//                    stopSong()
//                    releaseCamera()
//                    navigateSafely(videoFile.absolutePath)
//
//
//                }
//            }
//
//        binding.btnStartDance.text = "Stop Dance"
//        binding.btnStartDance.isEnabled = true
//    }
//
//    private fun navigateSafely(videoPath: String) {
//        Thread {
//            // 🔥 Give CameraX & MediaCodec enough time to die
//            Thread.sleep(1800)
//
//            runOnUiThread {
//                if (!isFinishing && !isDestroyed) {
//                    goToProcessing(videoPath)
//                }
//            }
//        }.start()
//    }
//
//    private fun stopRecording() {
//        recording?.stop()
//        recording = null
//
//        binding.btnStartDance.text = "Processing..."
//        binding.btnStartDance.isEnabled = false
//    }
//
//    private fun releaseCamera() {
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//        cameraProviderFuture.addListener({
//            try {
//                cameraProviderFuture.get().unbindAll()
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }, ContextCompat.getMainExecutor(this))
//    }
//
//    // ---------------- SONG PLAYER ----------------
//
//    private fun playSong() {
//        if (songResId == -1) return
//
//        mediaPlayer = MediaPlayer.create(this, songResId)
//
//        mediaPlayer?.setOnCompletionListener {
//            binding.root.post {
//                if (recording != null) {
//                    stopRecording()
//                }
//            }
//        }
//
//        mediaPlayer?.start()
//    }
//
//
//
//    private fun stopSong() {
//        mediaPlayer?.let {
//            if (it.isPlaying) it.stop()
//            it.release()
//        }
//        mediaPlayer = null
//    }
//
//    // ---------------- NEXT SCREEN ----------------
//
//    private fun goToProcessing(videoPath: String) {
//        val intent = Intent(this, ProcessingActivity::class.java)
//        intent.putExtra("VIDEO_PATH", videoPath)
//
//        // 🔥 Reference dance video
//        intent.putExtra("REFERENCE_VIDEO", R.raw.song1_ref)
//
//        startActivity(intent)
//        finish()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        stopSong()
//    }
//
//}
package com.kartikeya.DanceScoreAI

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kartikeya.DanceScoreAI.databinding.ActivityCameraBinding
import java.io.File

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var mediaPlayer: MediaPlayer? = null

    private var songResId: Int = -1
    private var isProcessingStarted = false

    companion object {
        private const val TAG = "CameraFlow"
        private const val CAMERA_PERMISSION_CODE = 1001
    }

    // ----------------------------------------------------

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate START")

        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        songResId = intent.getIntExtra("SONG_AUDIO", -1)
        Log.d(TAG, "SongResId = $songResId")

        if (hasCameraPermission()) {
            Log.d(TAG, "Camera permission GRANTED")
            startCamera()
        } else {
            Log.d(TAG, "Camera permission NOT granted")
            requestCameraPermission()
        }

        binding.btnBack.setOnClickListener {
            Log.d(TAG, "Back pressed | recording=$recording")
            if (recording != null) {
                stopRecording()
            } else {
                stopSong()
                finish()
            }
        }

        binding.btnStartDance.setOnClickListener {
            Log.d(TAG, "Start/Stop clicked | recording=$recording")
            if (recording == null) {
                startRecordingWithSong()
            } else {
                stopRecording()
            }
        }
    }

    // ----------------------------------------------------
    // PERMISSION
    // ----------------------------------------------------

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
            CAMERA_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_CODE &&
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        ) {
            Log.d(TAG, "Permissions granted from dialog")
            startCamera()
        } else {
            Log.e(TAG, "Permissions denied")
        }
    }

    // ----------------------------------------------------
    // CAMERA
    // ----------------------------------------------------

    private fun startCamera() {
        Log.d(TAG, "startCamera()")

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                Log.d(TAG, "CameraProvider READY")

                val preview = Preview.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                    .build().also {
                        it.setSurfaceProvider(binding.previewView.surfaceProvider)
                    }

                val recorder = Recorder.Builder()
                    .setQualitySelector(QualitySelector.from(Quality.HD))
                    .build()

                videoCapture = VideoCapture.withOutput(recorder)

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    videoCapture
                )

                Log.d(TAG, "Camera BOUND")

            } catch (e: Exception) {
                Log.e(TAG, "Camera start FAILED", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    // ----------------------------------------------------
    // RECORDING
    // ----------------------------------------------------

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun startRecordingWithSong() {
        Log.d(TAG, "startRecordingWithSong()")

        isProcessingStarted = false
        playSong()

        val videoFile = File(externalCacheDir, "dance_${System.currentTimeMillis()}.mp4")
        Log.d(TAG, "Video path=${videoFile.absolutePath}")

        val outputOptions = FileOutputOptions.Builder(videoFile).build()

        recording = videoCapture?.output
            ?.prepareRecording(this, outputOptions)
            ?.withAudioEnabled()
            ?.start(ContextCompat.getMainExecutor(this)) { event ->

                Log.d(TAG, "VideoRecordEvent = ${event.javaClass.simpleName}")

                if (event is VideoRecordEvent.Finalize && !isProcessingStarted) {
                    Log.d(TAG, "FINALIZE EVENT RECEIVED")

                    isProcessingStarted = true
                    recording = null

                    stopSong()
                    releaseCamera()

                    navigateSafely(videoFile.absolutePath)
                }
            }

        binding.btnStartDance.text = "Stop Dance"
    }

    private fun stopRecording() {
        Log.d(TAG, "stopRecording()")
        recording?.stop()
        recording = null

        binding.btnStartDance.text = "Processing..."
        binding.btnStartDance.isEnabled = false
    }

    private fun releaseCamera() {
        Log.d(TAG, "releaseCamera()")

        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            try {
                providerFuture.get().unbindAll()
                Log.d(TAG, "Camera unbind SUCCESS")
            } catch (e: Exception) {
                Log.e(TAG, "Camera release ERROR", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    // ----------------------------------------------------
    // SONG
    // ----------------------------------------------------

    private fun playSong() {
        if (songResId == -1) return

        Log.d(TAG, "playSong()")
        mediaPlayer = MediaPlayer.create(this, songResId)

        mediaPlayer?.setOnCompletionListener {
            Log.d(TAG, "Song COMPLETED")
            binding.root.post {
                if (recording != null) stopRecording()
            }
        }

        mediaPlayer?.start()
    }

    private fun stopSong() {
        Log.d(TAG, "stopSong()")
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null
    }

    // ----------------------------------------------------
    // NAVIGATION
    // ----------------------------------------------------

    private fun navigateSafely(videoPath: String) {
        Log.d(TAG, "navigateSafely()")

        Thread {
            Log.d(TAG, "Waiting before navigation")
            Thread.sleep(1800)

            runOnUiThread {
                Log.d(TAG, "Navigate UI | finishing=$isFinishing destroyed=$isDestroyed")
                if (!isFinishing && !isDestroyed) {
                    goToProcessing(videoPath)
                }
            }
        }.start()
    }

    private fun goToProcessing(videoPath: String) {
        Log.d(TAG, "goToProcessing()")

        val intent = Intent(this, ProcessingActivity::class.java)
        intent.putExtra("VIDEO_PATH", videoPath)
        intent.putExtra("REFERENCE_VIDEO_ASSET", "ref_dance(video-converter.com).mp4")

        startActivity(intent)
        finish()
    }

    // ----------------------------------------------------

    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        super.onDestroy()
        stopSong()
    }
}
