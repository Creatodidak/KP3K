package id.creatodidak.kp3k.pimpinan

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.adapter.pemiliklahan.KontakAdapter
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.DataPimpinan
import id.creatodidak.kp3k.api.model.pimpinan.DataKontak
import id.creatodidak.kp3k.api.model.pimpinan.ValDataKontak
import id.creatodidak.kp3k.api.model.pimpinan.ValDataWilayah
import id.creatodidak.kp3k.helper.Loading
import kotlinx.coroutines.launch

class PanggilanVideo : AppCompatActivity() {
    private lateinit var sh: SharedPreferences
    private lateinit var lyJenisKontak: LinearLayout
    private lateinit var spJenisKontak: Spinner
    private lateinit var lyKabupaten: LinearLayout
    private lateinit var spKontakKabupaten: Spinner
    private lateinit var lyKecamatan: LinearLayout
    private lateinit var spKontakKecamatan: Spinner
    private lateinit var lyDesa: LinearLayout
    private lateinit var spKontakDesa: Spinner
    private lateinit var rvKontak: RecyclerView

    private val jenisKontak = mutableListOf("PILIH JENIS KONTAK")
    private val dataKontak = mutableListOf<DataKontak>()
    private lateinit var kontakAdapter: KontakAdapter
    private lateinit var currentRole: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sh = getSharedPreferences("session", MODE_PRIVATE)
        currentRole = sh.getString("role", "") ?: ""

        if (currentRole == "BPKP") {
            Toast.makeText(this, "Fitur video call tidak tersedia untuk BPKP", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        when (currentRole) {
            "PIMPINAN" -> {
                jenisKontak.addAll(listOf("PAMATWIL", "KAPOLRES", "BINTARA PENGGERAK"))
            }
            "PAMATWIL" -> {
                jenisKontak.addAll(listOf("KAPOLRES", "BINTARA PENGGERAK"))
            }
            "KAPOLRES" -> {
                jenisKontak.add("BINTARA PENGGERAK")
            }
            "WAKAPOLRES" -> {
                jenisKontak.add("BINTARA PENGGERAK")
            }
        }

        setContentView(R.layout.activity_panggilan_video)

        lyJenisKontak = findViewById(R.id.lyJenisKontak)
        spJenisKontak = findViewById(R.id.spJenisKontak)
        lyKabupaten = findViewById(R.id.lyKabupaten)
        spKontakKabupaten = findViewById(R.id.spKontakKabupaten)
        lyKecamatan = findViewById(R.id.lyKecamatan)
        spKontakKecamatan = findViewById(R.id.spKontakKecamatan)
        lyDesa = findViewById(R.id.lyDesa)
        spKontakDesa = findViewById(R.id.spKontakDesa)
        rvKontak = findViewById(R.id.rvKontak)

        kontakAdapter = KontakAdapter(dataKontak, onDataClick = {
            val jabatan = sh.getString("jabatan", "") ?: ""
            val jenis = if(spJenisKontak.selectedItem.toString() == "BINTARA PENGGERAK") "BPKP" else spJenisKontak.selectedItem.toString()
            val (target, nama) = it.split("/")
            lifecycleScope.launch {
                callPersonil(jabatan, target, nama, "", jenis)
            }
        })


        rvKontak.adapter = kontakAdapter
        rvKontak.layoutManager = LinearLayoutManager(this)

        spJenisKontak.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, jenisKontak)

        spJenisKontak.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selected = spJenisKontak.getItemAtPosition(position).toString()
                when (selected) {
                    "PAMATWIL", "KAPOLRES" -> {
                        lyKabupaten.visibility = View.VISIBLE
                        lyKecamatan.visibility = View.GONE
                        lyDesa.visibility = View.GONE
                        dataKontak.clear()
                        kontakAdapter.notifyDataSetChanged()
                        lifecycleScope.launch {
                            loadKabupaten(selected)
                        }
                    }
                    "BINTARA PENGGERAK" -> {
                        lyKabupaten.visibility = View.VISIBLE
                        dataKontak.clear()
                        kontakAdapter.notifyDataSetChanged()
                        lifecycleScope.launch {
                            loadKabupaten(selected)
                        }
                    }
                    else -> {
                        lyKabupaten.visibility = View.GONE
                        lyKecamatan.visibility = View.GONE
                        lyDesa.visibility = View.GONE
                        dataKontak.clear()
                        kontakAdapter.notifyDataSetChanged()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spKontakKabupaten.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val roleSelected = spJenisKontak.selectedItem.toString()
                val kabupaten = parent.getItemAtPosition(position).toString()

                if (roleSelected == "BINTARA PENGGERAK") {
                    if (kabupaten != "PILIH KABUPATEN") {
                        lyKecamatan.visibility = View.VISIBLE
                        lifecycleScope.launch {
                            loadKecamatan(kabupaten)
                        }
                    } else {
                        lyKecamatan.visibility = View.GONE
                        lyDesa.visibility = View.GONE
                    }
                } else {
                    if (kabupaten != "PILIH KABUPATEN") {
                        lyKecamatan.visibility = View.GONE
                        lyDesa.visibility = View.GONE
                        dataKontak.clear()
                        kontakAdapter.notifyDataSetChanged()
                        lifecycleScope.launch {
                            loadKontak(roleSelected, kabupaten, null, null)
                        }
                    } else {
                        lyKecamatan.visibility = View.GONE
                        lyDesa.visibility = View.GONE
                        dataKontak.clear()
                        kontakAdapter.notifyDataSetChanged()
                    }
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spKontakKecamatan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val kecamatan = parent.getItemAtPosition(position).toString()

                if (kecamatan != "PILIH KECAMATAN") {
                    lyDesa.visibility = View.VISIBLE
                    dataKontak.clear()
                    kontakAdapter.notifyDataSetChanged()
                    lifecycleScope.launch {
                        loadDesa(kecamatan)
                    }
                } else {
                    lyDesa.visibility = View.GONE
                    dataKontak.clear()
                    kontakAdapter.notifyDataSetChanged()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


        spKontakDesa.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val desa = parent.getItemAtPosition(position).toString()
                if (desa != "PILIH DESA") {
                    val kabupaten = spKontakKabupaten.selectedItem.toString()
                    val kecamatan = spKontakKecamatan.selectedItem.toString()
                    dataKontak.clear()
                    kontakAdapter.notifyDataSetChanged()
                    lifecycleScope.launch {
                        loadKontak("BINTARA PENGGERAK", kabupaten, kecamatan, desa)
                    }
                } else {
                    dataKontak.clear()
                    kontakAdapter.notifyDataSetChanged()
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun loadKabupaten(jenis: String) {
        try {
            val kabupatenUser = sh.getString("kabupaten", "") ?: ""
            val kabupatenList = when (currentRole) {
                "PIMPINAN" -> listOf("PILIH KABUPATEN", "SAMBAS", "MEMPAWAH", "SANGGAU", "KETAPANG", "SINTANG", "KAPUAS HULU", "BENGKAYANG", "LANDAK", "SEKADAU", "MELAWI", "KAYONG UTARA", "KUBU RAYA", "KOTA PONTIANAK", "KOTA SINGKAWANG")
                "PAMATWIL", "KAPOLRES" -> listOf("PILIH KABUPATEN", kabupatenUser)
                else -> emptyList()
            }

            spKontakKabupaten.adapter = ArrayAdapter(
                this@PanggilanVideo,
                android.R.layout.simple_spinner_dropdown_item,
                kabupatenList
            )
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal memuat kabupaten: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun loadKecamatan(kabupaten: String) {
        try {
            val service = Client.retrofit.create(DataPimpinan::class.java)
            val response = service.loadWilayah(ValDataWilayah(kabupaten))
            val kecamatanList = mutableListOf("PILIH KECAMATAN")
            kecamatanList.addAll(response.map { it.nama ?: "" })
            spKontakKecamatan.adapter = ArrayAdapter(this@PanggilanVideo, android.R.layout.simple_spinner_dropdown_item, kecamatanList)
        } catch (e: Exception) {
            Toast.makeText(this, "Data tidak ditemukan", Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun loadDesa(kecamatan: String) {
        try {
            val kabupaten = spKontakKabupaten.selectedItem.toString()
            val service = Client.retrofit.create(DataPimpinan::class.java)
            val response = service.loadWilayah(ValDataWilayah(kabupaten, kecamatan))
            val desaList = mutableListOf("PILIH DESA")
            desaList.addAll(response.map { it.nama ?: "" })
            spKontakDesa.adapter = ArrayAdapter(this@PanggilanVideo, android.R.layout.simple_spinner_dropdown_item, desaList)
        } catch (e: Exception) {
            Toast.makeText(this, "Data tidak ditemukan", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun loadKontak(jenis: String, kab: String?, kec: String?, desa: String?) {
        try {
            if (currentRole != "PIMPINAN") {
                val allowedKab = sh.getString("kabupaten", "") ?: ""
                if (kab != null && kab != allowedKab && kab != "PILIH KABUPATEN") {
                    Toast.makeText(this@PanggilanVideo, "Tidak diizinkan akses kabupaten ini!", Toast.LENGTH_SHORT).show()
                    return
                }
            }

            val service = Client.retrofit.create(DataPimpinan::class.java)
            val data = service.loadKontakApi(ValDataKontak(jenis, kab, kec, desa))

            dataKontak.clear()
            if (data.isNotEmpty()) {
                dataKontak.addAll(data)
            }
            kontakAdapter.notifyDataSetChanged()
        } catch (e: Exception) {
            Toast.makeText(this, "Data tidak ditemukan", Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun callPersonil(jabatan: String, nrp: String, nama: String, pangkat: String, jenis: String) {
        Loading.show(this)
        try {
            val res = Client.retrofit.create(DataPimpinan::class.java).callPersonil(DataPimpinan.CallPimpinan(nrp, jabatan, jenis))
            if(res.token === null){
                Loading.hide()
                AlertDialog.Builder(this)
                    .setTitle("Informasi")
                    .setMessage("Token Tidak Ditemukan")
                    .setCancelable(false)
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            }else{
                Loading.hide()
                val i = Intent(this, PimpinanVideoCall::class.java)
                i.putExtra("token", res.token)
                i.putExtra("channel", nrp)
                i.putExtra("nama", nama)
                i.putExtra("pangkat", pangkat)
                startActivity(i)
            }
        }catch (e: Exception){
            Loading.hide()
            AlertDialog.Builder(this)
                .setTitle("Informasi")
                .setMessage("Terjadi kesalahan: ${e.message}")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        }
    }
}
