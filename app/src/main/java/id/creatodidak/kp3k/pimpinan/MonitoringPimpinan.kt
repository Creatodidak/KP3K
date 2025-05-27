package id.creatodidak.kp3k.pimpinan

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.adapter.pimpinan.BulanTanamAdapter
import id.creatodidak.kp3k.adapter.pimpinan.KabupatenAdapter
import id.creatodidak.kp3k.adapter.pimpinan.KabupatenAdapterBulan
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.DataPimpinan
import id.creatodidak.kp3k.api.DataPimpinan.Kabs
import id.creatodidak.kp3k.api.model.pimpinan.KabupatenSummaryByMasaTanam
import id.creatodidak.kp3k.api.model.pimpinan.KabupatenSummaryMonthly
import kotlinx.coroutines.launch

class MonitoringPimpinan : AppCompatActivity() {
    private lateinit var swipeMonitoring: SwipeRefreshLayout
    private lateinit var rvMonitoringPimpinan: RecyclerView
    private var kab: String? = null
    private lateinit var sh: SharedPreferences
    private lateinit var adaptermasatanam: KabupatenAdapter
    private lateinit var adapterbulantanam: KabupatenAdapterBulan
    private val listByMasaTanam = mutableListOf<KabupatenSummaryByMasaTanam>()
    private val listByBulanTanam = mutableListOf<KabupatenSummaryMonthly>()
    private lateinit var spJenisShowData : Spinner
    private val listJenisData = listOf("BULAN TANAM", "MASA TANAM")
    private var selectedJenisData = 0
    private var isFirstLaunch = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monitoring_pimpinan)
        sh = getSharedPreferences("session", MODE_PRIVATE)
        val role = sh.getString("role", "")
        kab = if(role.equals("PIMPINAN")){
            null
        }else{
            sh.getString("kabupaten_id", "")
        }
        rvMonitoringPimpinan = findViewById(R.id.rvMonitoringData)
        spJenisShowData = findViewById(R.id.spJenisShowData)
        swipeMonitoring = findViewById(R.id.swipeMonitoring)

        adaptermasatanam = KabupatenAdapter(listByMasaTanam)
        adapterbulantanam = KabupatenAdapterBulan(listByBulanTanam)

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listJenisData)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spJenisShowData.adapter = spinnerAdapter
        spJenisShowData.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when(position){
                    0 -> {
                        selectedJenisData = 0
                        lifecycleScope.launch {
                            loadData()
                        }
                    }
                    1 -> {
                        selectedJenisData = 1
                        lifecycleScope.launch {
                            loadData()
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
//                Nothing
            }

        }

        swipeMonitoring.setOnRefreshListener {
            lifecycleScope.launch {
                loadData()
            }
        }

        rvMonitoringPimpinan.layoutManager = LinearLayoutManager(this)
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun loadData(){
        swipeMonitoring.isRefreshing = true
        listByMasaTanam.clear()
        listByBulanTanam.clear()
        adaptermasatanam.notifyDataSetChanged()
        adapterbulantanam.notifyDataSetChanged()

        try {
            if(selectedJenisData == 0){
                val data = Client.retrofit.create(DataPimpinan::class.java).getDataLahanByBulan(Kabs(kab))
                listByBulanTanam.addAll(data)
                adapterbulantanam.notifyDataSetChanged()
                rvMonitoringPimpinan.adapter = adapterbulantanam
            } else {
                val data = Client.retrofit.create(DataPimpinan::class.java).getDataLahanByMasaTanam(Kabs(kab))
                listByMasaTanam.addAll(data)
                adaptermasatanam.notifyDataSetChanged()
                rvMonitoringPimpinan.adapter = adaptermasatanam
            }
        } catch (e: Exception){
            AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(e.message)
                .setPositiveButton("OK", null)
                .show()
        } finally {
            swipeMonitoring.isRefreshing = false
        }
    }

}