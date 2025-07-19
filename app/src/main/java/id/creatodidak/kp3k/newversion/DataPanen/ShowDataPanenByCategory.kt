package id.creatodidak.kp3k.newversion.DataPanen

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.room.PrimaryKey
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.database.Entity.LahanEntity
import id.creatodidak.kp3k.database.Entity.OwnerEntity
import id.creatodidak.kp3k.database.Entity.TanamanEntity
import java.util.Date

class ShowDataPanenByCategory : AppCompatActivity() {
    data class NewPanenEntity(
        val id: Int,
        val showCaseName: String,
        val tanaman: TanamanEntity?,
        val lahan: LahanEntity?,
        val owner: OwnerEntity?,
        val tanaman_id: Int,
        val jumlahpanen: String,
        val luaspanen: String,
        val tanggalpanen: Date,
        val keterangan: String?,
        val analisa: String?,
        val foto1: String = "/media/default.jpg",
        val foto2: String = "/media/default.jpg",
        val foto3: String = "/media/default.jpg",
        val foto4: String = "/media/default.jpg",
        val status: String = "VERIFIED",
        val alasan: String?,
        val createAt: Date = Date(),
        val updateAt: Date = Date(),
        val komoditas: String,
        val submitter: String
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_show_data_panen_by_category)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}