package id.creatodidak.kp3k.newversion.DataOwner

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.adapter.NewAdapter.OwnerDataDraftVerifikasiAdapter
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.OwnerEndpoint
import id.creatodidak.kp3k.api.RequestClass.VerifikasiRequest
import id.creatodidak.kp3k.database.AppDatabase
import id.creatodidak.kp3k.database.Dao.OwnerDao
import id.creatodidak.kp3k.database.Dao.WilayahDao
import id.creatodidak.kp3k.database.DatabaseInstance
import id.creatodidak.kp3k.database.Entity.OwnerEntity
import id.creatodidak.kp3k.helper.KonfirmasiTolak
import id.creatodidak.kp3k.helper.Loading
import id.creatodidak.kp3k.helper.getMyNamePangkat
import id.creatodidak.kp3k.helper.isOnline
import id.creatodidak.kp3k.helper.showError
import kotlinx.coroutines.launch
import java.util.Locale

class RejectedOwner : AppCompatActivity() {
    private lateinit var db : AppDatabase
    private lateinit var dbOwner: OwnerDao
    private lateinit var dbWilayah: WilayahDao
    private lateinit var komoditas : String
    private lateinit var tvKeteranganKomoditas : TextView
    private lateinit var totalData : TextView
    private lateinit var rvRejectedDO : RecyclerView
    private lateinit var adapter : OwnerDataDraftVerifikasiAdapter
    private var datas = mutableListOf<OwnerEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rejected_owner)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        window.statusBarColor = getColor(R.color.gray_bg)
        db = DatabaseInstance.getDatabase(this)
        dbOwner = db.ownerDao()
        dbWilayah = db.wilayahDao()
        komoditas = intent.getStringExtra("komoditas").toString()
        tvKeteranganKomoditas = findViewById(R.id.tvKeteranganKomoditas)
        totalData = findViewById(R.id.totalData)
        tvKeteranganKomoditas.text = "pada Komoditas ${komoditas.capitalize(Locale.ROOT)}"
        rvRejectedDO = findViewById(R.id.rvRejectedDO)

        adapter = OwnerDataDraftVerifikasiAdapter(
            datas,
            onDisetujui = {owner ->
                Log.d("DataOwner", "Verifikasi: $owner")
            },
            onDitolak = {owner ->
                Log.d("DataOwner", "Verifikasi: $owner")
            },
            onDeleteClick = {owner ->
                Log.d("DataOwner", "Verifikasi: $owner")
            },
            onKirimDataKeServerUpdateClick = {owner ->
                Log.d("DataOwner", "Verifikasi: $owner")
            },
            onKirimDataKeServerCreateClick = {owner ->
                Log.d("DataOwner", "Verifikasi: $owner")
            },
            onEdit = {owner ->
                val i = Intent(this@RejectedOwner, DataOwnerCreateEdit::class.java)
                i.putExtra("komoditas", komoditas)
                i.putExtra("mode", "edit")
                i.putExtra("id", owner.id.toString())
                startActivity(i)
            },
            onDeleteOnServer = {owner ->
                AlertDialog.Builder(this)
                    .setTitle("Konfirmasi")
                    .setMessage("Apakah Anda yakin ingin menghapus data ini?")
                    .setIcon(R.drawable.query_what_how_why_icon)
                    .setPositiveButton("Ya") { dialog, _ ->
                        dialog.dismiss()
                        lifecycleScope.launch {
                            deleteOnServer(owner)
                        }
                    }
                    .setNegativeButton("Tidak") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            },
            lifecycleOwner = this
        )

        rvRejectedDO.adapter = adapter
        rvRejectedDO.layoutManager = LinearLayoutManager(this)
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun loadData(){
        try{
            datas.clear()
            val data = dbOwner.getRejectedData(komoditas)
            datas.addAll(data)
            adapter.notifyDataSetChanged()
            totalData.text = "${data.size} Data Ditemukan!"
        }catch (e: Exception){
            showError(this, "Error",e.message.toString())
        }
    }

    private suspend fun deleteOnServer(owner: OwnerEntity){
        Loading.show(this)
        try {
            if(isOnline(this)){
                val res = Client.retrofit.create(OwnerEndpoint::class.java).deleteOwner(owner.id.toString())
                if(res.isSuccessful && res.body() != null){
                    Loading.hide()
                    dbOwner.delete(owner)
                    AlertDialog.Builder(this)
                        .setTitle("Berhasil")
                        .setMessage(res.body()?.msg ?: "Data berhasil dihapus")
                        .setIcon(R.drawable.green_checkmark_line_icon)
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                            lifecycleScope.launch {
                                loadData()
                            }
                        }
                        .show()
                }else{
                    Loading.hide()
                    showError(this, "Error", res.body()?.msg ?: "Terjadi Kesalahan"){
                        lifecycleScope.launch {
                            loadData()
                        }
                    }
                }
            }else{
                Loading.hide()
                showError(this, "Error", "Tidak ada koneksi internet!"){
                    lifecycleScope.launch {
                        loadData()
                    }
                }
            }
        }catch (e: Exception){
            Loading.hide()
            showError(this, "Error",e.message.toString()){
                lifecycleScope.launch {
                    loadData()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            loadData()
        }
    }

}