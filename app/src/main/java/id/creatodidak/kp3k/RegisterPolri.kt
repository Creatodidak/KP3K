package id.creatodidak.kp3k

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import id.creatodidak.kp3k.model.Polres
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RegisterPolri : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_polri)
//        window.statusBarColor = "#4CAF50".toColorInt()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Tangani insets keyboard agar layout naik saat keyboard muncul
        val rootView = findViewById<ScrollView>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, maxOf(imeInsets.bottom, navInsets.bottom))
            insets
        }


        val tanggallahir = findViewById<TextView>(R.id.tanggallahir)
        val btnDaftar = findViewById<Button>(R.id.btnDaftar)
        val calendar = Calendar.getInstance()
        val spPolres = findViewById<Spinner>(R.id.spPolres)

        tanggallahir.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this, { _, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, monthOfYear, dayOfMonth)
                    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    val formattedDate = dateFormat.format(selectedDate.time)
                    tanggallahir.text = formattedDate
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        val jsonString = readAssetFile(this, "polres/kalbar.json")

// Parsing
        val polresList: List<Polres> = Gson().fromJson(
            jsonString,
            object : TypeToken<List<Polres>>() {}.type
        )

// Ambil list nama untuk spinner
        val namaPolresList = mutableListOf("PILIH SATUAN KERJA") // Tambahkan opsi awal
        namaPolresList.addAll(polresList.map { it.nama }) // Tambahkan dari JSON


// Set ke Spinner
        val spinner = findViewById<Spinner>(R.id.spPolres)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, namaPolresList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                if (position == 0) {
                    // User belum pilih
                    Toast.makeText(this@RegisterPolri, "Silakan pilih polres", Toast.LENGTH_SHORT).show()
                } else {
                    val selectedPolres = polresList[position - 1] // Dikurangi 1 karena ada item tambahan
                    Toast.makeText(this@RegisterPolri, "ID: ${selectedPolres.id}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


    }

    fun readAssetFile(context: Context, fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
    }

}