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

object KonfirmasiFoto {
    private var currentDialog: AlertDialog? = null

    fun show(
        context: Context,
        absolutePath: String,
        onGunakan: () -> Unit,
        onUlangi: () -> Unit
    ) {
        if (currentDialog?.isShowing == true) return

        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.konfirmasi_foto_layout, null)

        val ivPreview = view.findViewById<ImageView>(R.id.ivPreview)
        Glide.with(context)
            .load(absolutePath)
            .into(ivPreview)

        val tvUlangi = view.findViewById<TextView>(R.id.tvUlangi)
        val tvGunakan = view.findViewById<TextView>(R.id.tvGunakan)

        tvUlangi.setOnClickListener {
            hide()
            onUlangi()
        }

        tvGunakan.setOnClickListener {
            hide()
            onGunakan()
        }

        builder.setCancelable(false)
        currentDialog = builder.create()
        currentDialog?.apply {
            setView(view)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            show()

            // Jadikan fullscreen
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            // Sembunyikan status bar dan navigation bar
            window?.decorView?.systemUiVisibility =
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
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
