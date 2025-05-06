package id.creatodidak.kp3k.helper

import android.widget.TextView
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private var updateJob: Job? = null

    fun startUpdatingTime(textView: TextView) {
        updateJob?.cancel() // pastikan tidak ada job duplikat
        updateJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                val currentTime = timeFormat.format(Date())
                textView.text = currentTime
                delay(1000)
            }
        }
    }

    fun stopUpdatingTime() {
        updateJob?.cancel()
        updateJob = null
    }

    fun getFormattedDateTime(): String {
        // Mendapatkan locale bahasa Indonesia
        val locale = Locale("id", "ID")

        // Membuat formatter untuk format "EEEE, d MMMM yyyy HH:mm"
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy HH:mm", locale)

        // Mengambil tanggal dan waktu saat ini
        val currentDate = Date()

        // Format tanggal dan waktu
        return dateFormat.format(currentDate)
    }
}
