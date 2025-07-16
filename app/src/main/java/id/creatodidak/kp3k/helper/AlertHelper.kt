package id.creatodidak.kp3k.helper

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import id.creatodidak.kp3k.R
import kotlinx.coroutines.launch

fun showError(
    ctx: Context,
    title: String,
    message: String,
    posButtonAction: (() -> Unit)? = null
) {
    AlertDialog.Builder(ctx)
        .setTitle(title)
        .setMessage(message)
        .setCancelable(false)
        .setPositiveButton(android.R.string.ok) { dialog, _ ->
            dialog.dismiss()                // pastikan dialog ditutup
            posButtonAction?.invoke()       // jalankan aksi jika ada
        }
        .setIcon(R.drawable.outline_warning_24)
        .show()
}

fun showSuccess(ctx: Context, title: String, msg: String, posButtonAction: (() -> Unit)? = null){
    AlertDialog.Builder(ctx)
        .setTitle(title)
        .setMessage(msg)
        .setCancelable(false)
        .setIcon(R.drawable.green_checkmark_line_icon)
        .setPositiveButton(android.R.string.ok) { dialog, _ ->
            dialog.dismiss()                // pastikan dialog ditutup
            posButtonAction?.invoke()       // jalankan aksi jika ada
        }
        .show()
}

fun askUser(ctx: Context, title: String, msg: String, posButtonAction: (() -> Unit)? = null){
    AlertDialog.Builder(ctx)
        .setTitle(title)
        .setMessage(msg)
        .setCancelable(false)
        .setIcon(R.drawable.query_what_how_why_icon)
        .setPositiveButton(android.R.string.ok) { dialog, _ ->
            dialog.dismiss()
            posButtonAction?.invoke()
        }
        .setNegativeButton("Tidak") { dialog, _ ->
            dialog.dismiss()
        }
        .show()
}
