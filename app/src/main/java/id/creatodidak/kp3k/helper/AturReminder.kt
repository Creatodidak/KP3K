package id.creatodidak.kp3k.helper

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import id.creatodidak.kp3k.R
import java.text.SimpleDateFormat
import java.util.Locale

object AturReminder {
    private var currentDialog: AlertDialog? = null

    fun show(
        context: Context,
        onSetReminder: (tanggal: String, jam: String) -> Unit
    ) {
        if (currentDialog?.isShowing == true) return

        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.ly_reminder, null)
        val etTanggalReminder: EditText = view.findViewById(R.id.etTanggalReminder)
        val etJamReminder: EditText = view.findViewById(R.id.etJamReminder)
        val btBatalPengingat: Button = view.findViewById(R.id.btBatalPengingat)
        val btAturReminder: Button = view.findViewById(R.id.btAturReminder)

        etTanggalReminder.isFocusable = false
        etJamReminder.isFocusable = false
        etTanggalReminder.inputType = InputType.TYPE_NULL
        etJamReminder.inputType = InputType.TYPE_NULL

        etTanggalReminder.setOnClickListener {
            showCustomDatePicker(context, DatePickerMode.DAY) { d ->
                val formattedDate =
                    SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(d)
                etTanggalReminder.setText(formattedDate)
            }
        }

        etJamReminder.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            val minute = calendar.get(java.util.Calendar.MINUTE)

            val timePicker = android.app.TimePickerDialog(
                context,
                { _, selectedHour, selectedMinute ->
                    val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                    etJamReminder.setText(formattedTime)
                },
                hour,
                minute,
                true
            )
            timePicker.show()
        }

        btBatalPengingat.setOnClickListener {
            hide()
        }

        btAturReminder.setOnClickListener {
            val tanggal = etTanggalReminder.text.toString()
            val jam = etJamReminder.text.toString()
            if (tanggal.isNotEmpty() && jam.isNotEmpty()) {
                onSetReminder(tanggal, jam)
                hide()
            }
        }

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
