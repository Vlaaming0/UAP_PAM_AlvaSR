package com.example.uap_pam_alvasr

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()

        val edEmail = findViewById<EditText>(R.id.editTextEmail)
        val edPass  = findViewById<EditText>(R.id.editTextPassword)
        findViewById<Button>(R.id.buttonLogin).setOnClickListener {
            val email = edEmail.text.toString().trim()
            val pass  = edPass.text.toString().trim()
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Email & password wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Login gagal: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
