package id.creatodidak.kp3k.helper

fun formatDuaDesimal(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format("%.2f", value).trimEnd('0').trimEnd('.')
    }
}
