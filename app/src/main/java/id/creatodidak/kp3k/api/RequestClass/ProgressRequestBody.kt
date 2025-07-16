package id.creatodidak.kp3k.api.RequestClass

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File

class ProgressRequestBody(
    private val file: File,
    private val contentType: String,
    private val onProgress: (progress: Double, uploadedMb: Double, totalMb: Double) -> Unit
) : RequestBody() {

    override fun contentType(): MediaType? = contentType.toMediaTypeOrNull()

    override fun contentLength(): Long = file.length()

    override fun writeTo(sink: BufferedSink) {
        val totalLength = contentLength()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var uploaded: Long = 0
        var lastUpdateTime = 0L

        file.inputStream().use { input ->
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                sink.write(buffer, 0, read)
                uploaded += read

                val now = System.currentTimeMillis()
                if (now - lastUpdateTime > 50 || uploaded == totalLength) {
                    lastUpdateTime = now

                    val progress = uploaded.toDouble() / totalLength.toDouble()
                    val uploadedMb = uploaded.toDouble() / (1024.0 * 1024.0)
                    val totalMb = totalLength.toDouble() / (1024.0 * 1024.0)

                    onProgress(progress, uploadedMb, totalMb)
                }
            }
        }
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 2048
    }
}
