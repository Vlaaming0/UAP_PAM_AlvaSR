package com.example.uap_pam_alvasr

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TambahActivity : AppCompatActivity() {
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        setContentView(R.layout.tambah_tanaman)

        val edNama = findViewById<EditText>(R.id.editNama)
        val edHarga = findViewById<EditText>(R.id.editHarga)
        val edDesc = findViewById<EditText>(R.id.editDeskripsi)
        findViewById<Button>(R.id.btnTambah).setOnClickListener {
            val nama = edNama.text.toString().trim()
            val hrga = edHarga.text.toString().trim()
            val desc = edDesc.text.toString().trim()
            RetrofitClient.instance.createPlant(
                PlantCreateRequest(nama, desc, hrga)
            ).enqueue(object : Callback<PlantResponse> {
                override fun onResponse(
                    call: Call<PlantResponse>,
                    resp: Response<PlantResponse>
                ) {
                    if(resp.isSuccessful) {
                        Toast.makeText(this@TambahActivity, "Berhasil tambah", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                override fun onFailure(call: Call<PlantResponse>, t: Throwable) {}
            })
        }
    }
}
