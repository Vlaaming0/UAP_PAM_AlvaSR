package com.example.uap_pam_alvasr

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URLEncoder

class DetailActivity : AppCompatActivity() {
    // Simpan data sebagai variable class
    private var currentPlantName: String = ""
    private var currentPlantPrice: String = ""
    private var currentPlantDesc: String = ""
    private var isDataLoaded: Boolean = false

    private lateinit var btnUpd: Button

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        setContentView(R.layout.detail_tanaman)

        val namaKey = intent.getStringExtra("nama")!!
        val tvNama = findViewById<TextView>(R.id.tvNama)
        val tvHarga = findViewById<TextView>(R.id.tvHarga)
        val tvDesc = findViewById<TextView>(R.id.tvDeskripsi)
        val ivGambar = findViewById<ImageView>(R.id.ivGambar)
        btnUpd = findViewById<Button>(R.id.btnUpdate)

        ivGambar.setImageResource(R.drawable.tumbuhan)

        // Disable button sampai data selesai dimuat
        btnUpd.isEnabled = false
        btnUpd.text = "Loading..."

        // 👇 TAMBAHKAN LOG
        Log.d("DetailActivity", "namaKey dari intent: '$namaKey'")

        // Encode nama untuk URL yang aman
        val encodedName = try {
            URLEncoder.encode(namaKey, "UTF-8")
        } catch (e: Exception) {
            namaKey
        }

        Log.d("DetailActivity", "Encoded name for API: '$encodedName'")

        RetrofitClient.instance.getPlant(encodedName)
            .enqueue(object : Callback<PlantResponse> {
                override fun onResponse(call: Call<PlantResponse>, resp: Response<PlantResponse>) {
                    Log.d("DetailActivity", "API Request URL: ${call.request().url}")
                    Log.d("DetailActivity", "API Response code: ${resp.code()}")

                    if (resp.isSuccessful) {
                        resp.body()?.let { plantResponse ->
                            val plant = plantResponse.data

                            // 👇 SIMPAN DATA KE VARIABLE
                            currentPlantName = plant.plant_name
                            currentPlantPrice = plant.price
                            currentPlantDesc = plant.description
                            isDataLoaded = true

                            tvNama.text = plant.plant_name
                            tvHarga.text = "Rp ${plant.price}"
                            tvDesc.text = plant.description

                            // Enable button setelah data berhasil dimuat
                            btnUpd.isEnabled = true
                            btnUpd.text = "Update"

                            // 👇 TAMBAHKAN LOG
                            Log.d("DetailActivity", "Data berhasil dimuat: $currentPlantName")
                        }
                    } else {
                        Log.e("DetailActivity", "API Error: ${resp.code()}")
                        Toast.makeText(this@DetailActivity, "Gagal memuat data: ${resp.code()}", Toast.LENGTH_SHORT).show()
                        btnUpd.isEnabled = true
                        btnUpd.text = "Update"
                    }
                }
                override fun onFailure(call: Call<PlantResponse>, t: Throwable) {
                    Log.e("DetailActivity", "Failed to load: ${t.message}")
                    Toast.makeText(this@DetailActivity, "Error jaringan: ${t.message}", Toast.LENGTH_SHORT).show()
                    btnUpd.isEnabled = true
                    btnUpd.text = "Update"
                }
            })

        btnUpd.setOnClickListener {
            if (!isDataLoaded) {
                Toast.makeText(this, "Data belum selesai dimuat", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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