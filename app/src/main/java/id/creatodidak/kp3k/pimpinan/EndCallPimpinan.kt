package id.creatodidak.kp3k.pimpinan

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import id.creatodidak.kp3k.R

class EndCallPimpinan : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_KP3K_PIMPINAN)
        setContentView(R.layout.activity_end_call_pimpinan)
        val fabBack = findViewById<FloatingActionButton>(R.id.fabBack)

        fabBack.setOnClickListener {
            finish()
        }
    }
}