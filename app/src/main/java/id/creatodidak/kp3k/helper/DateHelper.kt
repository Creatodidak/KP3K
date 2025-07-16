package id.creatodidak.kp3k.helper
import android.app.DatePickerDialog
import android.content.Context
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import id.creatodidak.kp3k.api.newModel.Kuartal
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

    fun formatTanggalKeIndonesia(isoDate: String): String {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        isoFormat.timeZone = TimeZone.getTimeZone("UTC")

        val date: Date = isoFormat.parse(isoDate) ?: return ""

        val formatIndonesia = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
        return formatIndonesia.format(date)
    }

    fun getAgeFromDate(input: String): String {
        val inputDate = ZonedDateTime.parse(input).withZoneSameInstant(ZoneId.systemDefault()).toLocalDate()
        val now = LocalDate.now()

        // Hitung total hari
        val totalDays = ChronoUnit.DAYS.between(inputDate, now).toInt().coerceAtLeast(1) // minimal 1 hari

        val months = totalDays / 30
        val weeks = (totalDays % 30) / 7
        val days = (totalDays % 30) % 7

        val parts = mutableListOf<String>()

        if (months > 0) parts.add("$months Bulan")
        if (weeks > 0) parts.add("$weeks Minggu")
        if (days > 0) parts.add("$days Hari")

        // Jika semua 0 → tambahkan 1 hari
        if (parts.isEmpty()) parts.add("1 Hari")

        return parts.joinToString(" ")

}

fun getAgeBetweenDates(start: String, end: String): String {
    val startDate = ZonedDateTime.parse(start).withZoneSameInstant(ZoneId.systemDefault()).toLocalDate()
    val endDate = ZonedDateTime.parse(end).withZoneSameInstant(ZoneId.systemDefault()).toLocalDate()

    // Hitung total hari
    val totalDays = ChronoUnit.DAYS.between(startDate, endDate).toInt().coerceAtLeast(1) // minimal 1 hari

    val months = totalDays / 30
    val weeks = (totalDays % 30) / 7
    val days = (totalDays % 30) % 7

    val parts = mutableListOf<String>()
    if (months > 0) parts.add("$months Bulan")
    if (weeks > 0) parts.add("$weeks Minggu")
    if (days > 0) parts.add("$days Hari")

    if (parts.isEmpty()) parts.add("1 Hari")

    return parts.joinToString(" ")
}


fun parseIsoDate(dateString: String): Date? {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.parse(dateString)!!
}

fun Date.toIsoString(): String {
    val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    isoFormat.timeZone = TimeZone.getTimeZone("UTC")
    return isoFormat.format(this)
}

fun showCustomDatePicker(
    context: Context,
    mode: DatePickerMode,
    onDateSelected: (Date) -> Unit
) {
    when (mode) {
        DatePickerMode.DAY -> {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Pilih Tanggal")
                .build()

            picker.addOnPositiveButtonClickListener { millis ->
                onDateSelected(Date(millis))
            }

            picker.show((context as AppCompatActivity).supportFragmentManager, "DATE_PICKER")
        }

        DatePickerMode.MONTH -> {
            val calendar = Calendar.getInstance()
            val dialog = DatePickerDialog(
                context,
                { _, year, month, _ ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    onDateSelected(calendar.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            // Sembunyikan pilihan hari
            dialog.datePicker.findViewById<View>(
                context.resources.getIdentifier("day", "id", "android")
            )?.visibility = View.GONE

            dialog.show()
        }

        DatePickerMode.YEAR -> {
            val calendar = Calendar.getInstance()
            val dialog = DatePickerDialog(
                context,
                { _, year, _, _ ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, 0)
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    onDateSelected(calendar.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            // Sembunyikan pilihan bulan dan hari
            dialog.datePicker.findViewById<View>(
                context.resources.getIdentifier("month", "id", "android")
            )?.visibility = View.GONE
            dialog.datePicker.findViewById<View>(
                context.resources.getIdentifier("day", "id", "android")
            )?.visibility = View.GONE

            dialog.show()
        }
    }
}

enum class DatePickerMode {
    DAY, MONTH, YEAR
}

// 1. Bandingkan tanggal (yyyy-MM-dd)
fun Date.onlyDateEquals(other: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = this@onlyDateEquals }
    val cal2 = Calendar.getInstance().apply { time = other }

    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
            cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
}

// 2. Bandingkan bulan dan tahun (MM-yyyy)
fun Date.onlyMonthYearEquals(other: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = this@onlyMonthYearEquals }
    val cal2 = Calendar.getInstance().apply { time = other }

    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
}

// 3. Bandingkan tahun saja (yyyy)
fun Date.onlyYearEquals(other: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = this@onlyYearEquals }
    val cal2 = Calendar.getInstance().apply { time = other }

    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
}

fun Date.withStartOfDay(): Date {
    val cal = Calendar.getInstance()
    cal.time = this
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.time
}

fun Date.withEndOfDay(): Date {
    val cal = Calendar.getInstance()
    cal.time = this
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    cal.set(Calendar.MILLISECOND, 999)
    return cal.time
}

fun generateKuartalList(): List<Kuartal> {
    val calendar = Calendar.getInstance()
    val currentDate = calendar.time
    val currentYear = calendar.get(Calendar.YEAR)
    val startLimit = Calendar.getInstance().apply {
        set(Calendar.YEAR, 2024)
        set(Calendar.MONTH, Calendar.OCTOBER) // Oktober
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

    val kuartalNames = listOf("KUARTAL I", "KUARTAL II", "KUARTAL III", "KUARTAL IV")
    val monthRanges = listOf(
        1 to 3,
        4 to 6,
        7 to 9,
        10 to 12
    )

    val kuartalList = mutableListOf<Kuartal>()
    var id = 1

    for (year in currentYear downTo 2024) {
        for (i in 3 downTo 0) {
            val (startMonth, endMonth) = monthRanges[i]

            val startCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, startMonth - 1)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val endCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, endMonth - 1)
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }

            // Skip kuartal yang belum dimulai
            if (startCal.time.after(currentDate)) continue

            // Stop jika sudah melewati batas bawah: KUARTAL IV 2024
            if (endCal.time.before(startLimit)) continue

            kuartalList.add(
                Kuartal(
                    id = id++,
                    tanggalStart = startCal.time,
                    tanggalEnd = endCal.time,
                    tahun = year,
                    name = "${kuartalNames[i]} $year"
                )
            )
        }
    }

    return kuartalList
}

fun String.convertToServerFormat(): Date? {
    return try {
        val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        inputFormat.parse(this)?.withStartOfDay()
    } catch (e: Exception) {
        null
    }
}
