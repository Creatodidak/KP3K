package id.creatodidak.kp3k.newversion.dashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.adapter.NewAdapter.KontakAdapter
import id.creatodidak.kp3k.api.newModel.ByEntity.Roles
import id.creatodidak.kp3k.api.newModel.Contact
import id.creatodidak.kp3k.database.AppDatabase
import id.creatodidak.kp3k.database.DatabaseInstance
import id.creatodidak.kp3k.helper.askUser
import id.creatodidak.kp3k.helper.getMyNrp
import id.creatodidak.kp3k.helper.getMyRole
import id.creatodidak.kp3k.helper.getMySatker
import id.creatodidak.kp3k.helper.getMyUsername
import id.creatodidak.kp3k.helper.getMyWilayah
import id.creatodidak.kp3k.helper.showError
import id.creatodidak.kp3k.network.SocketManager
import id.creatodidak.kp3k.newversion.VideoCall.HostVideoCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class VideoCall : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var rvKontak: RecyclerView
    private lateinit var etSearchKontak: EditText
    private val defKontak = mutableListOf<Contact>()
    private val listKontak = mutableListOf<Contact>()
    private lateinit var adapter: KontakAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        val socket = SocketManager.getSocket()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_video_call2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = getColor(R.color.default_bg)
        db = DatabaseInstance.getDatabase(this)
        rvKontak = findViewById(R.id.rvKontak)
        etSearchKontak = findViewById(R.id.etSearchKontak)

        socket.on("waiting-success") { args ->
            Log.i("DATA_SOCKET","WAITING SUCCESS")
            val obj = args[0] as JSONObject
            val room = obj.getString("room")
            val token = obj.getString("token")
            val receiver = obj.getString("namaReceiver")
            val nrpreceiver = obj.getString("nrpReceiver")

            val intent = Intent(this@VideoCall, HostVideoCall::class.java)
            intent.putExtra("room", room)
            intent.putExtra("token", token)
            intent.putExtra("receiver", receiver)
            intent.putExtra("receivernrp", nrpreceiver)
            startActivity(intent)
        }

        socket.on("caller-busy") { args ->
            Log.i("DATA_SOCKET","CALLER BUSY")
            val msg = (args[0] as JSONObject).getString("message")
            runOnUiThread {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        }


        adapter = KontakAdapter(
            listKontak,
            onCallClick = {it ->
                askUser(this@VideoCall, "Konfirmasi", "Apa anda yakin ingin melakukan panggilan kepada ${it.nama}"){
                    val nama = getMyUsername(this@VideoCall).ifEmpty { "KP3K" }
                    val data = JSONObject().apply {
                        put("nrpCaller", getMyNrp(this@VideoCall)) // Ganti dengan NRP caller sebenarnya
                        put("namaCaller", nama) // Ganti dengan nama caller
                        put("nrpReceiver", it.nrp) // Dari kontak yang ditekan
                        put("namaReceiver", it.nama) // Dari kontak yang ditekan
                    }
                    socket.emit("onwaiting", data)
                    Log.i("DATA_SOCKET","ON WAITING")
                }
            }
        )
        rvKontak.adapter = adapter
        rvKontak.layoutManager = LinearLayoutManager(this)

        etSearchKontak.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                listKontak.clear()
                val query = s.toString()
                val newdata = defKontak.filter {
                    it.nama.toLowerCase().contains(query.toLowerCase()) || it.nrp.contains(query) || it.jabatan.toLowerCase().contains(query.toLowerCase())
                }
                listKontak.addAll(newdata)
                adapter.notifyDataSetChanged()
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

        lifecycleScope.launch {
            loadKontak()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun loadKontak(){
        defKontak.clear()
        listKontak.clear()
        try {
            val listPejabat = withContext(Dispatchers.IO){
                when(getMyRole(this@VideoCall)){
                    in listOf("PJUPOLDA", "SUPERADMIN") -> db.userDao().getPejabat()
                    "PAMATWIL" -> {
                        if(getMyUsername(this@VideoCall) == "KABID TIK"){
                            db.userDao().getPejabat()
                        }else{
                            val satker = db.wilayahDao().getSatkerByKabId(getMyWilayah(this@VideoCall)).map { it.id }
                            db.userDao().getPejabatBySatkerId(satker)
                        }
                    }
                    "PJUPOLRES" -> {
                        db.userDao().getPejabatBySatkerId(listOf(getMySatker(this@VideoCall).id))
                    }
                    else -> {
                        if(getMyNrp(this@VideoCall) == "98070129"){
                            db.userDao().getPejabat()
                        }else{
                            emptyList()
                        }
                    }
                }
            }

            val listPersonil = withContext(Dispatchers.IO){
                when(getMyRole(this@VideoCall)){
                    in listOf("PJUPOLDA", "SUPERADMIN") -> db.userDao().getPersonil()
                    in listOf("PAMATWIL", "PJUPOLRES") -> {
                        if(getMyUsername(this@VideoCall) == "KABID TIK"){
                            db.userDao().getPersonil()
                        }else{
                            val satker = db.wilayahDao().getSatkerByKabId(getMyWilayah(this@VideoCall)).map { it.id }
                            db.userDao().getPersonilBySatkerIds(satker)
                        }
                    }
                    else -> {
                        if(getMyNrp(this@VideoCall) == "98070129"){
                            db.userDao().getPersonil()
                        }else{
                            emptyList()
                        }
                    }
                }
            }
            listPejabat.filter { it.nrp != getMyNrp(this@VideoCall) }.forEach {
                val d = Contact(
                    it.id,
                    it.nrp,
                    it.username,
                    it.jabatan
                )

                defKontak.add(d)
                listKontak.add(d)
            }

//            listPersonil.filter { it.role == Roles.BPKP }.forEach {
            listPersonil.forEach {
                if(it.desaBinaanId != null){
                    val desa = db.wilayahDao().getDesaById(it.desaBinaanId)
                    val kecamatan = db.wilayahDao().getKecamatanById(desa.kecamatanId)
                    val kabupaten = db.wilayahDao().getKabupatenById(kecamatan.kabupatenId)
                    val d = Contact(
                        it.id,
                        it.nrp,
                        "${it.pangkat} ${it.nama}",
                        "BA PENGGERAK DESA ${desa.nama} KEC. ${kecamatan.nama} KAB. ${kabupaten.nama}"
                    )
                    defKontak.add(d)
                    listKontak.add(d)
                }else{
                    val d = Contact(
                        it.id,
                        it.nrp,
                        "${it.pangkat} ${it.nama}",
                        it.role.name
                    )
                    defKontak.add(d)
                    listKontak.add(d)
                }
            }

            if(defKontak.isNotEmpty()){
                adapter.notifyDataSetChanged()
            }
        }catch (e: Exception){
            e.printStackTrace()
            showError(this, "Terjadi Kesalahan", e.message.toString())
        }
    }
}