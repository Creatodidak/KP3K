package id.creatodidak.kp3k.helper

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import id.creatodidak.kp3k.R

object SumberFoto {
    private var currentDialog: AlertDialog? = null

    fun show(
        context: Context,
        onKamera: () -> Unit,
        onGaleri: () -> Unit
    ) {
        if (currentDialog?.isShowing == true) return

        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.dialog_pilih_sumber, null)

        currentDialog = builder.create()
        currentDialog?.setView(view, 50, 0, 50, 0)
        currentDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        currentDialog?.show()

        val tvKamera: TextView = view.findViewById(R.id.tvKamera)
        val tvGaleri: TextView = view.findViewById(R.id.tvGaleri)
        
        tvKamera.setOnClickListener {
            onKamera()
            currentDialog?.dismiss()
        }
        tvGaleri.setOnClickListener {
            onGaleri()
            currentDialog?.dismiss()
        }
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
