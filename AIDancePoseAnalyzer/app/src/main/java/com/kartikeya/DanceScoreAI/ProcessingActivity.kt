//package com.kartikeya.DanceScoreAI
//
//import android.content.Intent
//import android.graphics.Bitmap
//import android.media.MediaMetadataRetriever
//import android.net.Uri
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import com.kartikeya.DanceScoreAI.databinding.ActivityProcessingBinding
//import com.google.mlkit.vision.common.InputImage
//import com.google.mlkit.vision.pose.Pose
//import com.google.mlkit.vision.pose.PoseDetection
//import com.google.mlkit.vision.pose.PoseLandmark
//import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
//import kotlin.math.abs
//import kotlin.math.min
//
//class ProcessingActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityProcessingBinding
//
//    private val poseDetector by lazy {
//        val options = PoseDetectorOptions.Builder()
//            .setDetectorMode(PoseDetectorOptions.SINGLE_IMAGE_MODE)
//            .build()
//        PoseDetection.getClient(options)
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityProcessingBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        val userVideoPath = intent.getStringExtra("VIDEO_PATH")
//        val refVideoResId = intent.getIntExtra("REFERENCE_VIDEO", -1)
//
//        if (userVideoPath == null || refVideoResId == -1) {
//            binding.tvStatus.text = "Video data missing"
//            return
//        }
//
//        binding.tvStatus.text = "Preparing AI processing..."
//
//        // 🔥 MOVE EVERYTHING TO BACKGROUND
//        Thread {
//            processVideos(userVideoPath, refVideoResId)
//        }.start()
//    }
//
//    // ---------------- MAIN PIPELINE ----------------
//    private fun processVideos(userVideoPath: String, refVideoResId: Int) {
//
//        updateStatus("Extracting user frames...")
//        val userFrames = extractFrames(userVideoPath)
//
//        updateStatus("Extracting reference frames...")
//        val refUri =
//            Uri.parse("android.resource://${packageName}/$refVideoResId")
//        val refFrames = extractFrames(refUri.toString())
//
//        val totalFrames = min(userFrames.size, refFrames.size)
//
//        if (totalFrames == 0) {
//            updateStatus("No frames to process")
//            return
//        }
//
//        updateStatus("Analyzing dance with AI...")
//        processAllFrames(userFrames, refFrames, totalFrames)
//    }
//
//    // ---------------- FRAME EXTRACTION ----------------
//    private fun extractFrames(videoPath: String): List<Bitmap> {
//        val retriever = MediaMetadataRetriever()
//        retriever.setDataSource(videoPath)
//
//        val duration =
//            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
//                ?.toLong() ?: 0L
//
//        val frames = mutableListOf<Bitmap>()
//        val intervalMs = 100L // 10 FPS
//
//        var timeMs = 0L
//        while (timeMs < duration) {
//            val bitmap = retriever.getFrameAtTime(
//                timeMs * 1000,
//                MediaMetadataRetriever.OPTION_CLOSEST
//            )
//            bitmap?.let { frames.add(it) }
//            timeMs += intervalMs
//        }
//
//        retriever.release()
//        return frames
//    }
//
//    // ---------------- PROCESS ALL FRAMES ----------------
//    private fun processAllFrames(
//        userFrames: List<Bitmap>,
//        refFrames: List<Bitmap>,
//        totalFrames: Int
//    ) {
//
//        var currentFrame = 0
//        var totalScore = 0
//        val maxScorePerFrame = 20
//
//        fun processNext() {
//            if (currentFrame >= totalFrames) {
//
//                val maxScore = totalFrames * maxScorePerFrame
//                val finalScore =
//                    if (maxScore > 0) (totalScore * 100) / maxScore else 0
//
//                runOnUiThread {
//                    val intent =
//                        Intent(this, ResultActivity::class.java)
//                    intent.putExtra("FINAL_SCORE", finalScore)
//                    startActivity(intent)
//                    finish()
//                }
//                return
//            }
//
//            val userImage =
//                InputImage.fromBitmap(userFrames[currentFrame], 0)
//            val refImage =
//                InputImage.fromBitmap(refFrames[currentFrame], 0)
//
//            poseDetector.process(userImage)
//                .addOnSuccessListener { userPose ->
//                    poseDetector.process(refImage)
//                        .addOnSuccessListener { refPose ->
//
//                            val userAngles = extractAngles(userPose)
//                            val refAngles = extractAngles(refPose)
//
//                            totalScore +=
//                                compareAngles(userAngles, refAngles)
//
//                            currentFrame++
//                            processNext()
//                        }
//                        .addOnFailureListener {
//                            currentFrame++
//                            processNext()
//                        }
//                }
//                .addOnFailureListener {
//                    currentFrame++
//                    processNext()
//                }
//        }
//
//        processNext()
//    }
//
//    // ---------------- ANGLE EXTRACTION ----------------
//    private fun extractAngles(pose: Pose): Map<String, Double> {
//        val angles = mutableMapOf<String, Double>()
//
//        val lShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
//        val lElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
//        val lWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
//
//        val rShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
//        val rElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
//        val rWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
//
//        if (lShoulder != null && lElbow != null && lWrist != null) {
//            angles["LEFT_ELBOW"] =
//                calculateAngle(lShoulder, lElbow, lWrist)
//        }
//
//        if (rShoulder != null && rElbow != null && rWrist != null) {
//            angles["RIGHT_ELBOW"] =
//                calculateAngle(rShoulder, rElbow, rWrist)
//        }
//
//        return angles
//    }
//
//    private fun calculateAngle(
//        p1: PoseLandmark,
//        p2: PoseLandmark,
//        p3: PoseLandmark
//    ): Double {
//
//        val angle =
//            Math.toDegrees(
//                Math.atan2(
//                    (p3.position.y - p2.position.y).toDouble(),
//                    (p3.position.x - p2.position.x).toDouble()
//                ) -
//                        Math.atan2(
//                            (p1.position.y - p2.position.y).toDouble(),
//                            (p1.position.x - p2.position.x).toDouble()
//                        )
//            )
//
//        return abs(angle)
//    }
//
//    private fun compareAngles(
//        userAngles: Map<String, Double>,
//        refAngles: Map<String, Double>
//    ): Int {
//
//        var score = 0
//        for (key in userAngles.keys) {
//            val diff =
//                abs(userAngles[key]!! - (refAngles[key] ?: continue))
//
//            score += when {
//                diff <= 10 -> 10
//                diff <= 20 -> 5
//                else -> 0
//            }
//        }
//        return score
//    }
//
//    private fun updateStatus(text: String) {
//        runOnUiThread {
//            binding.tvStatus.text = text
//        }
//    }
//}
package com.kartikeya.DanceScoreAI

import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.kartikeya.DanceScoreAI.databinding.ActivityProcessingBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import java.io.File
import kotlin.math.abs
import kotlin.math.min

class ProcessingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProcessingBinding
    private val TAG = "ProcessingFlow"

    private val poseDetector by lazy {
        Log.d(TAG, "Initializing PoseDetector")
        val options = PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.SINGLE_IMAGE_MODE)
            .build()
        PoseDetection.getClient(options)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProcessingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "ProcessingActivity onCreate")

        val userVideoPath = intent.getStringExtra("VIDEO_PATH")
        val refAssetName = intent.getStringExtra("REFERENCE_VIDEO_ASSET")

        Log.d(TAG, "UserVideoPath=$userVideoPath RefAsset=$refAssetName")

        if (userVideoPath.isNullOrEmpty() || refAssetName.isNullOrEmpty()) {
            binding.tvStatus.text = "Video data missing"
            Log.e(TAG, "❌ Video data missing")
            return
        }

        binding.tvStatus.text = "Preparing AI processing..."

        Thread {
            Log.d(TAG, "🚀 Background thread started")
            processVideos(userVideoPath, refAssetName)
        }.start()
    }

    // ---------------- MAIN PIPELINE ----------------
    private fun processVideos(userVideoPath: String, refAssetName: String) {

        // ---------- USER VIDEO ----------
        updateStatus("Extracting user frames...")
        Log.d(TAG, "Extracting USER frames")

        val userFrames = extractFramesFromFile(userVideoPath)
        Log.d(TAG, "User frames count = ${userFrames.size}")

        if (userFrames.isEmpty()) {
            updateStatus("User video processing failed")
            Log.e(TAG, "❌ User frames empty")
            return
        }

        // ---------- REFERENCE VIDEO (ASSET) ----------
        updateStatus("Extracting reference frames...")
        Log.d(TAG, "Extracting REF frames from ASSET")

        val refVideoPath = copyAssetVideoToCache(refAssetName)
        val refFrames = extractFramesFromFile(refVideoPath)
        Log.d(TAG, "Ref frames count = ${refFrames.size}")

        if (refFrames.isEmpty()) {
            updateStatus("Reference video processing failed")
            Log.e(TAG, "❌ Reference frames empty")
            return
        }

        // ---------- FRAME MATCH ----------
        val totalFrames = min(userFrames.size, refFrames.size)
        Log.d(TAG, "Total frames to process = $totalFrames")

        updateStatus("Analyzing dance with AI...")
        processAllFrames(userFrames, refFrames, totalFrames)
    }

    // ---------------- ASSET FRAME EXTRACTION ----------------
    private fun copyAssetVideoToCache(assetName: String): String {
        val outFile = File(cacheDir, assetName)

        if (outFile.exists() && outFile.length() > 0) {
            Log.d(TAG, "Asset already copied: ${outFile.absolutePath}")
            return outFile.absolutePath
        }

        Log.d(TAG, "Copying asset to cache: $assetName")

        assets.open(assetName).use { input ->
            outFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        Log.d(
            TAG,
            "Asset copied → path=${outFile.absolutePath}, size=${outFile.length()}"
        )

        return outFile.absolutePath
    }


    // ---------------- FILE FRAME EXTRACTION ----------------
    private fun extractFramesFromFile(videoPath: String): List<Bitmap> {
        Log.d(TAG, "extractFramesFromFile path=$videoPath")

        val retriever = MediaMetadataRetriever()

        try {
            retriever.setDataSource(videoPath)

            Log.d(TAG, "HAS_VIDEO=${retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO)}")
            Log.d(TAG, "MIME=${retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ File setDataSource failed", e)
            return emptyList()
        }

        return extractFramesInternal(retriever)
    }

    // ---------------- COMMON FRAME LOGIC ----------------
    private fun extractFramesInternal(retriever: MediaMetadataRetriever): List<Bitmap> {

        val duration =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLong() ?: 0L

        Log.d(TAG, "Video duration(ms)=$duration")

        val frames = mutableListOf<Bitmap>()
        val intervalMs = 300L // performance friendly

        var timeMs = 0L
        while (timeMs < duration) {
            retriever.getFrameAtTime(
                timeMs * 1000,
                MediaMetadataRetriever.OPTION_CLOSEST
            )?.let { frames.add(it) }

            timeMs += intervalMs
        }

        retriever.release()
        Log.d(TAG, "extractFrames DONE count=${frames.size}")
        return frames
    }

    // ---------------- AI PROCESS ----------------
    private fun processAllFrames(
        userFrames: List<Bitmap>,
        refFrames: List<Bitmap>,
        totalFrames: Int
    ) {

        var currentFrame = 0
        var totalScore = 0
        val maxScorePerFrame = 20

        fun processNext() {
            if (currentFrame >= totalFrames) {

                val finalScore =
                    (totalScore * 100) / (totalFrames * maxScorePerFrame)

                Log.d(TAG, "FINAL SCORE = $finalScore")

                runOnUiThread {
                    val intent = Intent(this, ResultActivity::class.java)
                    intent.putExtra("FINAL_SCORE", finalScore)
                    startActivity(intent)
                    finish()
                }
                return
            }

            updateStatus("Analyzing frame ${currentFrame + 1}/$totalFrames")

            val userImage = InputImage.fromBitmap(userFrames[currentFrame], 0)
            val refImage = InputImage.fromBitmap(refFrames[currentFrame], 0)

            poseDetector.process(userImage)
                .addOnSuccessListener { userPose ->
                    poseDetector.process(refImage)
                        .addOnSuccessListener { refPose ->
                            totalScore += compareAngles(
                                extractAngles(userPose),
                                extractAngles(refPose)
                            )
                            currentFrame++
                            processNext()
                        }
                        .addOnFailureListener {
                            currentFrame++
                            processNext()
                        }
                }
                .addOnFailureListener {
                    currentFrame++
                    processNext()
                }
        }

        processNext()
    }

    // ---------------- ANGLES ----------------
    private fun extractAngles(pose: Pose): Map<String, Double> {
        val angles = mutableMapOf<String, Double>()

        fun angle(a: PoseLandmark?, b: PoseLandmark?, c: PoseLandmark?) {
            if (a != null && b != null && c != null) {
                angles[a.landmarkType.toString()] =
                    calculateAngle(a, b, c)
            }
        }

        angle(
            pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER),
            pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW),
            pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        )

        angle(
            pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER),
            pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW),
            pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
        )

        return angles
    }

    private fun calculateAngle(p1: PoseLandmark, p2: PoseLandmark, p3: PoseLandmark): Double {
        val angle =
            Math.toDegrees(
                Math.atan2(
                    (p3.position.y - p2.position.y).toDouble(),
                    (p3.position.x - p2.position.x).toDouble()
                ) -
                        Math.atan2(
                            (p1.position.y - p2.position.y).toDouble(),
                            (p1.position.x - p2.position.x).toDouble()
                        )
            )
        return abs(angle)
    }

    private fun compareAngles(
        userAngles: Map<String, Double>,
        refAngles: Map<String, Double>
    ): Int {
        var score = 0
        for (key in userAngles.keys) {
            val diff = abs(userAngles[key]!! - (refAngles[key] ?: continue))
            score += when {
                diff <= 10 -> 10
                diff <= 20 -> 5
                else -> 0
            }
        }
        return score
    }

    private fun updateStatus(text: String) {
        runOnUiThread { binding.tvStatus.text = text }
    }
}
