package id.creatodidak.kp3k.helper
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

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
