package id.creatodidak.kp3k.api.newModel

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

data class DokumentasiUI(
    val imageView: ImageView,
    val errorText: TextView,
    val watermark: View,
    val container: View,
    val nrpText: TextView,
    val button: Button,
    var isFromCamera: Boolean = false,
    var imagePath: String = ""
)
