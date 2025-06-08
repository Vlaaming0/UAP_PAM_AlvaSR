package com.example.uap_pam_alvasr

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.uap_pam_alvasr.*

class DetailActivity : AppCompatActivity() {
    // Simpan data sebagai variable class
    private var currentPlantName: String = ""
    private var currentPlantPrice: String = ""
    private var currentPlantDesc: String = ""

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        setContentView(R.layout.detail_tanaman)

        val namaKey = intent.getStringExtra("nama")!!
        val tvNama = findViewById<TextView>(R.id.tvNama)
        val tvHarga = findViewById<TextView>(R.id.tvHarga)
        val tvDesc = findViewById<TextView>(R.id.tvDeskripsi)
        val ivGambar = findViewById<ImageView>(R.id.ivGambar)
        val btnUpd = findViewById<Button>(R.id.btnUpdate)

        ivGambar.setImageResource(R.drawable.tumbuhan)

        // 👇 TAMBAHKAN LOG
        Log.d("DetailActivity", "namaKey dari intent: '$namaKey'")

        RetrofitClient.instance.getPlant(namaKey)
            .enqueue(object : Callback<PlantResponse> {
                override fun onResponse(call: Call<PlantResponse>, resp: Response<PlantResponse>) {
                    if (resp.isSuccessful) {
                        resp.body()?.let { plantResponse ->
                            val plant = plantResponse.data

                            // 👇 SIMPAN DATA KE VARIABLE
                            currentPlantName = plant.plant_name
                            currentPlantPrice = plant.price
                            currentPlantDesc = plant.description

                            tvNama.text = plant.plant_name
                            tvHarga.text = "Rp ${plant.price}"
                            tvDesc.text = plant.description

                            // 👇 TAMBAHKAN LOG
                            Log.d("DetailActivity", "Data berhasil dimuat: $currentPlantName")
                        }
                    }
                }
                override fun onFailure(call: Call<PlantResponse>, t: Throwable) {
                    Log.e("DetailActivity", "Failed to load: ${t.message}")
                }
            })

        btnUpd.setOnClickListener {
            // 👇 PERBAIKI PENGIRIMAN DATA
            Log.d("DetailActivity", "Mengirim namaKey: '$namaKey'")
            Log.d("DetailActivity", "Mengirim currentPlantName: '$currentPlantName'")

            val intent = Intent(this, UpdateItemActivity::class.java).apply {
                putExtra("nama", namaKey) // Nama asli dari parameter
                putExtra("plant_name", currentPlantName) // Data yang sudah dimuat
                putExtra("plant_price", currentPlantPrice)
                putExtra("plant_desc", currentPlantDesc)
            }
            startActivity(intent)
        }
    }
}
