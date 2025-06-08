package com.example.uap_pam_alvasr

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        setContentView(R.layout.register)
        auth = FirebaseAuth.getInstance()

        val edEmail  = findViewById<EditText>(R.id.editTextEmail)
        val edPass   = findViewById<EditText>(R.id.editTextPassword)
        val edPass2  = findViewById<EditText>(R.id.editTextConfirmPassword)
        findViewById<Button>(R.id.buttonRegister).setOnClickListener {
            val email = edEmail.text.toString().trim()
            val p1    = edPass.text.toString().trim()
            val p2    = edPass2.text.toString().trim()
            if (email.isEmpty()||p1.isEmpty()||p2.isEmpty()) {
                Toast.makeText(this, "Semua field wajib diisi",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (p1 != p2) {
                Toast.makeText(this, "Password tidak sama",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.createUserWithEmailAndPassword(email,p1)
                .addOnSuccessListener {
                    Toast.makeText(this,"Register sukses",Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this,"Gagal: ${it.message}",Toast.LENGTH_SHORT).show()
                }
        }
    }
}
