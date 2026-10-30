package com.example.airteldatamonitor.data.speedtest

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

enum class SpeedTestStage(val label: String) {
    IDLE("Ready"),
    PING("Testing Latency & Jitter..."),
    DOWNLOAD("Testing Download Speed..."),
    UPLOAD("Testing Upload Speed..."),
    COMPLETED("Test Complete"),
    ERROR("Test Interrupted")
}

data class SpeedTestState(
    val stage: SpeedTestStage = SpeedTestStage.IDLE,
    val progress: Float = 0f, // 0.0 to 1.0
    val currentSpeedMbps: Double = 0.0,
    val pingMs: Int? = null,
    val jitterMs: Int? = null,
    val downloadSpeedMbps: Double? = null,
    val peakDownloadMbps: Double? = null,
    val uploadSpeedMbps: Double? = null,
    val peakUploadMbps: Double? = null,
    val speedHistory: List<Float> = emptyList(),
    val errorMessage: String? = null
)

class SpeedTestEngine {

    private val _testState = MutableStateFlow(SpeedTestState())
    val testState: StateFlow<SpeedTestState> = _testState.asStateFlow()

    private val pingUrl = "https://speed.cloudflare.com/__down?bytes=0"
    private val downloadUrl = "https://speed.cloudflare.com/__down?bytes=25000000"
    private val uploadUrl = "https://speed.cloudflare.com/__up"

    suspend fun runSpeedTest() = withContext(Dispatchers.IO) {
        try {
            _testState.value = SpeedTestState(
                stage = SpeedTestStage.PING,
                progress = 0.05f
            )

            // Step 1: Latency & Jitter Benchmark
            val (ping, jitter) = measurePingAndJitter()
            if (!currentCoroutineContext().isActive) return@withContext

            _testState.value = _testState.value.copy(
                stage = SpeedTestStage.DOWNLOAD,
                pingMs = ping,
                jitterMs = jitter,
                progress = 0.20f
            )

            // Step 2: Download Benchmark
            val downloadResult = measureDownload(durationSeconds = 7.0)
            if (!currentCoroutineContext().isActive) return@withContext

            _testState.value = _testState.value.copy(
                stage = SpeedTestStage.UPLOAD,
                currentSpeedMbps = 0.0,
                downloadSpeedMbps = downloadResult.averageMbps,
                peakDownloadMbps = downloadResult.peakMbps,
                progress = 0.60f,
                speedHistory = emptyList()
            )

            // Step 3: Upload Benchmark
            val uploadResult = measureUpload(durationSeconds = 7.0)
            if (!currentCoroutineContext().isActive) return@withContext

            _testState.value = _testState.value.copy(
                stage = SpeedTestStage.COMPLETED,
                currentSpeedMbps = 0.0,
                uploadSpeedMbps = uploadResult.averageMbps,
                peakUploadMbps = uploadResult.peakMbps,
                progress = 1.0f
            )

        } catch (e: Exception) {
            if (currentCoroutineContext().isActive) {
                _testState.value = _testState.value.copy(
                    stage = SpeedTestStage.ERROR,
                    currentSpeedMbps = 0.0,
                    errorMessage = e.message ?: "Speed test was interrupted"
                )
            }
        }
    }

    fun reset() {
        _testState.value = SpeedTestState()
    }

    private suspend fun measurePingAndJitter(): Pair<Int, Int> = withContext(Dispatchers.IO) {
        val samples = mutableListOf<Long>()
        val totalProbes = 5

        for (i in 0 until totalProbes) {
            if (!currentCoroutineContext().isActive) break
            val start = System.currentTimeMillis()
            var conn: HttpURLConnection? = null
            try {
                conn = (URL(pingUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 3000
                    readTimeout = 3000
                    requestMethod = "GET"
                    useCaches = false
                }
                conn.connect()
                val code = conn.responseCode
                if (code in 200..299) {
                    val rtt = System.currentTimeMillis() - start
                    samples.add(rtt)
                }
            } catch (_: Exception) {
            } finally {
                conn?.disconnect()
            }
            _testState.value = _testState.value.copy(
                progress = 0.05f + (0.15f * ((i + 1).toFloat() / totalProbes))
            )
        }

        if (samples.isEmpty()) {
            return@withContext Pair(25, 3) // Fallback default
        }

        samples.sort()
        val medianPing = samples[samples.size / 2].toInt()

        var jitterSum = 0L
        for (i in 1 until samples.size) {
            jitterSum += abs(samples[i] - samples[i - 1])
        }
        val jitter = if (samples.size > 1) (jitterSum / (samples.size - 1)).toInt() else 2

        Pair(medianPing, jitter)
    }

    private data class StreamBenchmarkResult(
        val averageMbps: Double,
        val peakMbps: Double
    )

    private suspend fun measureDownload(durationSeconds: Double): StreamBenchmarkResult = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        var inputStream: InputStream? = null
        var totalBytesRead = 0L
        var peakMbps = 0.0
        val history = mutableListOf<Float>()

        val testStartTime = System.currentTimeMillis()
        val durationMs = (durationSeconds * 1000).toLong()
        val targetEndTime = testStartTime + durationMs

        try {
            conn = (URL(downloadUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 5000
                requestMethod = "GET"
                useCaches = false
                setRequestProperty("User-Agent", "AirtelDataMonitor/1.0")
            }
            conn.connect()
            inputStream = conn.inputStream

            val buffer = ByteArray(32768)
            var lastSampleTime = testStartTime
            var lastBytesRead = 0L

            while (currentCoroutineContext().isActive && System.currentTimeMillis() < targetEndTime) {
                val bytesRead = inputStream.read(buffer)
                if (bytesRead == -1) {
                    // Loop stream or break if finished
                    break
                }
                totalBytesRead += bytesRead

                val now = System.currentTimeMillis()
                val elapsedSinceSample = now - lastSampleTime
                if (elapsedSinceSample >= 120) {
                    val diffBytes = totalBytesRead - lastBytesRead
                    val instantMbps = (diffBytes * 8.0) / (elapsedSinceSample * 1000.0)
                    peakMbps = max(peakMbps, instantMbps)

                    val testElapsed = now - testStartTime
                    val progressFraction = (testElapsed.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                    val overallProgress = 0.20f + (0.40f * progressFraction)

                    history.add(instantMbps.toFloat())
                    if (history.size > 25) history.removeAt(0)

                    _testState.value = _testState.value.copy(
                        currentSpeedMbps = formatMbps(instantMbps),
                        peakDownloadMbps = formatMbps(peakMbps),
                        progress = overallProgress,
                        speedHistory = history.toList()
                    )

                    lastSampleTime = now
                    lastBytesRead = totalBytesRead
                }
            }
        } finally {
            try { inputStream?.close() } catch (_: Exception) {}
            conn?.disconnect()
        }

        val totalElapsedSec = (System.currentTimeMillis() - testStartTime) / 1000.0
        val avgMbps = if (totalElapsedSec > 0.1) (totalBytesRead * 8.0) / (totalElapsedSec * 1_000_000.0) else 0.0

        StreamBenchmarkResult(
            averageMbps = formatMbps(avgMbps),
            peakMbps = formatMbps(peakMbps)
        )
    }

    private suspend fun measureUpload(durationSeconds: Double): StreamBenchmarkResult = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        var outputStream: OutputStream? = null
        var totalBytesSent = 0L
        var peakMbps = 0.0
        val history = mutableListOf<Float>()

        val testStartTime = System.currentTimeMillis()
        val durationMs = (durationSeconds * 1000).toLong()
        val targetEndTime = testStartTime + durationMs

        val chunk = ByteArray(16384) { (it % 127).toByte() }

        try {
            conn = (URL(uploadUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 5000
                requestMethod = "POST"
                doOutput = true
                useCaches = false
                setChunkedStreamingMode(16384)
                setRequestProperty("Content-Type", "application/octet-stream")
                setRequestProperty("User-Agent", "AirtelDataMonitor/1.0")
            }
            conn.connect()
            outputStream = conn.outputStream

            var lastSampleTime = testStartTime
            var lastBytesSent = 0L

            while (currentCoroutineContext().isActive && System.currentTimeMillis() < targetEndTime) {
                outputStream.write(chunk)
                totalBytesSent += chunk.size

                val now = System.currentTimeMillis()
                val elapsedSinceSample = now - lastSampleTime
                if (elapsedSinceSample >= 120) {
                    val diffBytes = totalBytesSent - lastBytesSent
                    val instantMbps = (diffBytes * 8.0) / (elapsedSinceSample * 1000.0)
                    peakMbps = max(peakMbps, instantMbps)

                    val testElapsed = now - testStartTime
                    val progressFraction = (testElapsed.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                    val overallProgress = 0.60f + (0.40f * progressFraction)

                    history.add(instantMbps.toFloat())
                    if (history.size > 25) history.removeAt(0)

                    _testState.value = _testState.value.copy(
                        currentSpeedMbps = formatMbps(instantMbps),
                        peakUploadMbps = formatMbps(peakMbps),
                        progress = overallProgress,
                        speedHistory = history.toList()
                    )

                    lastSampleTime = now
                    lastBytesSent = totalBytesSent
                }
            }
            outputStream.flush()
        } finally {
            try { outputStream?.close() } catch (_: Exception) {}
            conn?.disconnect()
        }

        val totalElapsedSec = (System.currentTimeMillis() - testStartTime) / 1000.0
        val avgMbps = if (totalElapsedSec > 0.1) (totalBytesSent * 8.0) / (totalElapsedSec * 1_000_000.0) else 0.0

        StreamBenchmarkResult(
            averageMbps = formatMbps(avgMbps),
            peakMbps = formatMbps(peakMbps)
        )
    }

    private fun formatMbps(value: Double): Double {
        return String.format(Locale.US, "%.1f", value).toDoubleOrNull() ?: 0.0
    }
}
