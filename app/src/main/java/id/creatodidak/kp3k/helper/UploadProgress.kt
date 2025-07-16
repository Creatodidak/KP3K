package id.creatodidak.kp3k.helper

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.material.progressindicator.LinearProgressIndicator
import id.creatodidak.kp3k.R

object UploadProgress {
    private var currentDialog: AlertDialog? = null

    fun show(context: Context, progress: Double, progressSize: Double, totalSize: Double) {
        if (currentDialog?.isShowing == true) return

        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.upload_progress, null)

        val tvProgressPercent = view.findViewById<TextView>(R.id.tvProgressPercent)
        val pbUpload = view.findViewById<LinearProgressIndicator>(R.id.pbUpload)
        val tvProgressMb = view.findViewById<TextView>(R.id.tvProgressMb)
        val tvTotalMb = view.findViewById<TextView>(R.id.tvTotalMb)

        tvTotalMb.text = "${angkaIndonesia(totalSize)} Mb"
        tvProgressMb.text = "${angkaIndonesia(progressSize)} Mb"
        pbUpload.progress = (progress * 100).toInt()
        tvProgressPercent.text = "${angkaIndonesia(progress*100)}%"

        builder.setCancelable(false)
        currentDialog = builder.create()
        currentDialog?.setView(view, 50, 0, 50, 0)
        currentDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        currentDialog?.show()
    }

    fun hide() {
        currentDialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
        currentDialog = null
    }
}
