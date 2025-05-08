package id.creatodidak.kp3k

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import id.creatodidak.kp3k.dashboard.DashboardOpsional

class EndCallActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_end_call)

        val btnBackHome = findViewById<FloatingActionButton>(R.id.btnBackHome)

        btnBackHome.setOnClickListener {
            val i = Intent(this@EndCallActivity, DashboardOpsional::class.java)
            startActivity(i)
            finish()
        }
    }
}