package org.eightbits.screeno

import android.app.Activity
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import androidx.core.content.getSystemService
import androidx.window.layout.WindowMetricsCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class ScreenRecorderService : Service() {

    private val mediaProjectionManager by lazy {
        getSystemService<MediaProjectionManager>()
    }
    private var mediaProjection: MediaProjection? = null
    private val mediaRecorder by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(applicationContext)
        } else {
            MediaRecorder()
        }
    }
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val filePath by lazy {
        "${externalCacheDir?.absolutePath}/screeno_${System.currentTimeMillis()}.mp4"
    }
    private var virtualDisplay: VirtualDisplay? = null
    private var resultCode: Int = 0
    private var data: Intent? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        resultCode = intent?.getIntExtra("RESULT_CODE", Activity.RESULT_CANCELED) ?: 0
        data = intent?.getParcelableExtra("DATA")

        val notification = NotificationHelper.createNotification(applicationContext)
        NotificationHelper.createNotificationChannel(applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        startRecording()
        return START_NOT_STICKY
    }

    /**
     *  Starts the screen recording
     */
    private fun startRecording() {
        mediaProjection = mediaProjectionManager?.getMediaProjection(resultCode, data!!)

        setupMediaRecorder()

        virtualDisplay = createVirtualDisplay()

        mediaRecorder.start()
    }


    private fun setupMediaRecorder() {
        val (width, height) = getWindowSize()
        val (scaledWidth, scaledHeight) = getScaledDimensions(width, height)

        with(mediaRecorder) {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(filePath)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setVideoSize(scaledWidth, scaledHeight)
            setVideoFrameRate(VIDEO_FRAME_RATE)
            setVideoEncodingBitRate(VIDEO_BIT_RATE_KILOBITS * 1000)

            prepare()
        }
    }

    /**
     *  Creates a MediaRecorder instance to record the screen
     */
    private fun createVirtualDisplay(): VirtualDisplay? {
        val (width, height) = getWindowSize()
        return mediaProjection?.createVirtualDisplay(
            "ScreenoDisplay",
            width,
            height,
            resources.displayMetrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mediaRecorder.surface,
            null,
            null
        )
    }


    /**
     * Get window size
     */
    private fun getWindowSize(): Pair<Int, Int> {
        val calculator = WindowMetricsCalculator.getOrCreate()
        val metrics = calculator.computeMaximumWindowMetrics(applicationContext)
        return metrics.bounds.width() to metrics.bounds.height()
    }

    /**
     * Get scaled window size
     */
    private fun getScaledDimensions(
        maxWidth: Int,
        maxHeight: Int,
        scaleFactor: Float = 0.8f
    ): Pair<Int, Int> {
        val aspectRatio = maxWidth / maxHeight.toFloat()

        var newWidth = (maxWidth * scaleFactor).toInt()
        var newHeight = (newWidth / aspectRatio).toInt()

        if (newHeight > (maxHeight * scaleFactor)) {
            newHeight = (maxHeight * scaleFactor).toInt()
            newWidth = (newHeight * aspectRatio).toInt()
        }

        return newWidth to newHeight
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
    }

    /**
     *  Releases resources and stops the service
     */
    private fun stopRecording() {
        try {
            mediaRecorder.stop()
            mediaProjection?.stop()
            mediaRecorder.reset()
            virtualDisplay?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val VIDEO_FRAME_RATE = 30
        private const val VIDEO_BIT_RATE_KILOBITS = 512
    }

}
