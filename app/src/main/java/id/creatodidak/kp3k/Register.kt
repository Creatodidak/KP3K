package id.creatodidak.kp3k

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Register : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        window.statusBarColor = "#4CAF50".toColorInt()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val RegPetani = findViewById<ImageView>(R.id.regPetani)
        val RegPolri = findViewById<ImageView>(R.id.regPolri)

        RegPolri.setOnClickListener {
            val i = Intent(this, RegisterPolri::class.java)
            startActivity(i)
        }

        RegPetani.setOnClickListener {
            val i = Intent(this, RegisterPetani::class.java)
            startActivity(i)
        }
    }
}