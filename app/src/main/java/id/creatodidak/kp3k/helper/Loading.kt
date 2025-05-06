package id.creatodidak.kp3k.helper

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import id.creatodidak.kp3k.R

object Loading {
    private var currentDialog: AlertDialog? = null

    fun show(context: Context) {
        if (currentDialog?.isShowing == true) return

        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.loading_dialog, null)

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
