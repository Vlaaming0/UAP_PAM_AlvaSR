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

        Log.d("DetailActivity", "namaKey dari intent: '$namaKey'")

        // PERBAIKAN: Coba beberapa metode encoding
        loadPlantData(namaKey)

        btnUpd.setOnClickListener {
            if (!isDataLoaded) {
                Toast.makeText(this, "Data belum selesai dimuat", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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

    private fun loadPlantData(namaKey: String) {
        // Coba beberapa metode encoding untuk mengatasi masalah URL
        val encodingOptions = listOf(
            namaKey, // Original tanpa encoding
            URLEncoder.encode(namaKey, "UTF-8"), // Standard URL encoding
            namaKey.replace(" ", "+"), // Plus encoding untuk space
            namaKey.replace(" ", "%20") // Manual space encoding
        ).distinct() // Hapus duplikat

        Log.d("DetailActivity", "Trying encoding options: $encodingOptions")

        tryLoadWithEncodingOptions(encodingOptions, 0)
    }

    private fun tryLoadWithEncodingOptions(options: List<String>, index: Int) {
        if (index >= options.size) {
            // Semua encoding gagal
            Log.e("DetailActivity", "All encoding options failed")
            Toast.makeText(this, "Gagal memuat data dengan semua metode encoding", Toast.LENGTH_LONG).show()
            btnUpd.isEnabled = true
            btnUpd.text = "Update"
            return
        }

        val encodedName = options[index]
        Log.d("DetailActivity", "Trying encoding option $index: '$encodedName'")

        RetrofitClient.instance.getPlant(encodedName)
            .enqueue(object : Callback<PlantResponse> {
                override fun onResponse(call: Call<PlantResponse>, resp: Response<PlantResponse>) {
                    Log.d("DetailActivity", "API Request URL: ${call.request().url}")
                    Log.d("DetailActivity", "API Response code: ${resp.code()}")

                    when {
                        resp.isSuccessful && resp.body() != null -> {
                            // Berhasil!
                            val plant = resp.body()!!.data

                            currentPlantName = plant.plant_name
                            currentPlantPrice = plant.price
                            currentPlantDesc = plant.description
                            isDataLoaded = true

                            findViewById<TextView>(R.id.tvNama).text = plant.plant_name
                            findViewById<TextView>(R.id.tvHarga).text = "Rp ${plant.price}"
                            findViewById<TextView>(R.id.tvDeskripsi).text = plant.description

                            btnUpd.isEnabled = true
                            btnUpd.text = "Update"

                            Log.d("DetailActivity", "Data berhasil dimuat dengan encoding $index: $currentPlantName")
                        }

                        resp.code() == 404 && index < options.size - 1 -> {
                            // 404 tapi masih ada opsi lain, coba yang berikutnya
                            Log.d("DetailActivity", "Encoding option $index returned 404, trying next")
                            tryLoadWithEncodingOptions(options, index + 1)
                        }

                        resp.code() == 404 -> {
                            // 404 dan ini opsi terakhir
                            Log.e("DetailActivity", "Data not found with any encoding method")
                            Toast.makeText(this@DetailActivity, "Data tanaman tidak ditemukan", Toast.LENGTH_SHORT).show()
                            btnUpd.isEnabled = true
                            btnUpd.text = "Update"
                        }

                        else -> {
                            // Error lain, coba opsi berikutnya jika ada
                            Log.e("DetailActivity", "API Error with encoding $index: ${resp.code()}")
                            if (index < options.size - 1) {
                                tryLoadWithEncodingOptions(options, index + 1)
                            } else {
                                Toast.makeText(this@DetailActivity, "Gagal memuat data: ${resp.code()}", Toast.LENGTH_SHORT).show()
                                btnUpd.isEnabled = true
                                btnUpd.text = "Update"
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<PlantResponse>, t: Throwable) {
                    Log.e("DetailActivity", "Network error with encoding $index: ${t.message}")

                    if (index < options.size - 1) {
                        // Masih ada opsi lain, coba
                        tryLoadWithEncodingOptions(options, index + 1)
                    } else {
                        // Opsi terakhir juga gagal
                        Toast.makeText(this@DetailActivity, "Error jaringan: ${t.message}", Toast.LENGTH_SHORT).show()
                        btnUpd.isEnabled = true
                        btnUpd.text = "Update"
                    }
                }
            })
    }
}