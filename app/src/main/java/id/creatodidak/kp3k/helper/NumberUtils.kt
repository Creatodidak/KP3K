package id.creatodidak.kp3k.helper

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

fun formatDuaDesimal(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format("%.2f", value).trimEnd('0').trimEnd('.')
    }
}

fun formatDuaDesimalKoma(value: Double): String {
    val symbols = DecimalFormatSymbols(Locale("in", "ID")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }

    // Gunakan pola dengan maksimum 2 digit desimal, tanpa trailing nol
    val formatter = DecimalFormat("#,###.##", symbols)

    return formatter.format(value)
}

fun convertToHektar(value: Double): Double {
    return value / 10_000
}

fun convertToMeterPersegi(value: Double): Double {
    return value * 10_000
}

fun convertToTon(value: Double): Double {
    return value / 1000
}

fun convertToKilogram(value: Double): Double {
    return value * 1000
}

fun angkaIndonesia(value: Double): String {
    val rounded = "%.2f".format(Locale.US, value).toDouble() // bulatkan ke 2 digit
    val symbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    val formatter = DecimalFormat("#,###.##", symbols)
    return formatter.format(rounded)
}
