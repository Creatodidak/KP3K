package id.creatodidak.kp3k.helper

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import id.creatodidak.kp3k.R

object KonfirmasiTolak {
    private var currentDialog: AlertDialog? = null

    fun show(
        context: Context,
        onBatal: () -> Unit,
        onLanjut: (String) -> Unit
    ) {
        if (currentDialog?.isShowing == true) return

        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.tolak_dialog, null)

        val etKeterangan = view.findViewById<EditText>(R.id.etKeteranganTolak)
        val btnBatal = view.findViewById<Button>(R.id.btCancel)
        val btnKirim = view.findViewById<Button>(R.id.btEksekusi)

        val builder = AlertDialog.Builder(context)
            .setView(view)
            .setCancelable(false)

        currentDialog = builder.create()
        currentDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        currentDialog?.setIcon(R.drawable.why_icon)

        btnBatal.setOnClickListener {
            onBatal()
            currentDialog?.dismiss()
            currentDialog = null
        }

        btnKirim.setOnClickListener {
            val alasan = etKeterangan.text.toString().trim()
            if (alasan.isEmpty()) {
                etKeterangan.error = "Alasan tidak boleh kosong"
                etKeterangan.requestFocus()
            } else {
                onLanjut(alasan)
                currentDialog?.dismiss()
                currentDialog = null
            }
        }

        currentDialog?.show()
    }

    fun hide() {
        currentDialog?.dismiss()
        currentDialog = null
    }
}
