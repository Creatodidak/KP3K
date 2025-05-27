package id.creatodidak.kp3k.pimpinan

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.DataPimpinan
import id.creatodidak.kp3k.api.model.addAtensiItem
import id.creatodidak.kp3k.helper.Loading
import kotlinx.coroutines.launch

class AddAtensi : AppCompatActivity() {
    private lateinit var sh: SharedPreferences
    private lateinit var etJudulAtensi: EditText
    private lateinit var etIsiAtensi: EditText
    private lateinit var btKirimAtensi: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sh = getSharedPreferences("session", MODE_PRIVATE)
        val role = sh.getString("role", "")
        val kabupaten = sh.getString("kabupaten_id", "")
        val jabatan = sh.getString("jabatan", "")
        val username = sh.getString("username", "")

        setContentView(R.layout.activity_add_atensi)
        etJudulAtensi = findViewById(R.id.etJudulAtensi)
        etIsiAtensi = findViewById(R.id.etIsiAtensi)
        btKirimAtensi = findViewById(R.id.btKirimAtensi)

        btKirimAtensi.setOnClickListener {
            if(isValid()){
                lifecycleScope.launch {
                    sendData(etJudulAtensi.text.toString(), etIsiAtensi.text.toString(), username.toString(), kabupaten.toString(), jabatan.toString(), role.toString())
                }
            }
        }
    }

    private suspend fun sendData(judul: String, isi: String, username: String, kabupaten: String, jabatan: String, role: String){
        Loading.show(this)
        try{
            val res = Client.retrofit.create(DataPimpinan::class.java).addAtensi(addAtensiItem(judul, isi, username, jabatan, kabupaten, role))
            if(res.isSuccessful){
                Loading.hide()
                AlertDialog.Builder(this)
                    .setTitle("Berhasil")
                    .setMessage("Berhasil mengirimkan atensi!")
                    .setPositiveButton("OK"){ dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }
                    .show()
            }else{
                Loading.hide()
                AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Terjadi Kesalahan")
                    .setPositiveButton("OK"){ dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }catch (e: Exception){
            Loading.hide()
            AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Terjadi Kesalahan: ${e.message}")
                .setPositiveButton("OK"){ dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun isValid() : Boolean{
        if(etJudulAtensi.text.toString().isEmpty()){
            etJudulAtensi.error = "JudulAtensi tidak boleh kosong"
            etJudulAtensi.requestFocus()
            return false
        }
        if(etIsiAtensi.text.toString().isEmpty()){
            etIsiAtensi.error = "IsiAtensi tidak boleh kosong"
            etIsiAtensi.requestFocus()
            return false
        }

        return true
    }
}