package id.creatodidak.kp3k.pimpinan

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import id.creatodidak.kp3k.R

class DashboardPimpinan : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_KP3K_PIMPINAN)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_pimpinan)

    }
}