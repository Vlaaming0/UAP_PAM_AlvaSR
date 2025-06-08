package com.example.uap_pam_alvasr

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URLEncoder

class UpdateItemActivity : AppCompatActivity() {
    private lateinit var edNama: EditText
    private lateinit var edHarga: EditText
    private lateinit var edDesc: EditText
    private lateinit var btnSimpan: Button

    private var originalNameKey: String = ""

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        setContentView(R.layout.edit_activity)

        // Initialize views
        edNama = findViewById(R.id.editNama)
        edHarga = findViewById(R.id.editHarga)
        edDesc = findViewById(R.id.editDeskripsi)
        val iv = findViewById<ImageView>(R.id.imagePreview)
        btnSimpan = findViewById(R.id.btnSimpan)

        iv.setImageResource(R.drawable.tumbuhan)

        // Get data from intent
        originalNameKey = intent.getStringExtra("nama") ?: ""
        val plantName = intent.getStringExtra("plant_name") ?: ""
        val plantPrice = intent.getStringExtra("plant_price") ?: ""
        val plantDesc = intent.getStringExtra("plant_desc") ?: ""

        Log.d("UpdateActivity", "Original namaKey: '$originalNameKey'")
        Log.d("UpdateActivity", "Received plantName: '$plantName'")

        // Set form dengan data yang sudah ada jika tersedia
        if (plantName.isNotEmpty()) {
            edNama.setText(plantName)
            edHarga.setText(plantPrice)
            edDesc.setText(plantDesc)
            setFormEnabled(true)
        } else {
            // Jika tidak ada data dari intent, load dari API
            loadDataFromAPI()
        }

        btnSimpan.setOnClickListener {
            updatePlant()
        }
    }

    private fun loadDataFromAPI() {
        if (originalNameKey.isEmpty()) {
            Toast.makeText(this, "Data tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setFormEnabled(false)

        // Encode nama untuk URL yang aman
        val encodedName = try {
            URLEncoder.encode(originalNameKey, "UTF-8")
        } catch (e: Exception) {
            originalNameKey
        }

        Log.d("UpdateActivity", "Loading data for: '$originalNameKey'")
        Log.d("UpdateActivity", "Encoded name: '$encodedName'")

        RetrofitClient.instance.getPlant(encodedName)
            .enqueue(object : Callback<PlantResponse> {
                override fun onResponse(call: Call<PlantResponse>, resp: Response<PlantResponse>) {
                    setFormEnabled(true)

                    Log.d("UpdateActivity", "Request URL: ${call.request().url}")
                    Log.d("UpdateActivity", "Response code: ${resp.code()}")

                    when {
                        resp.isSuccessful && resp.body() != null -> {
                            val plant = resp.body()!!.data
                            Log.d("UpdateActivity", "Plant loaded: ${plant.plant_name}")

                            edNama.setText(plant.plant_name)
                            edHarga.setText(plant.price)
                            edDesc.setText(plant.description)
                        }
                        resp.code() == 404 -> {
                            Toast.makeText(this@UpdateItemActivity, "Data tidak ditemukan", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        else -> {
                            val errorMsg = try {
                                resp.errorBody()?.string() ?: "Unknown error"
                            } catch (e: Exception) {
                                "Error reading response"
                            }
                            Log.e("UpdateActivity", "Error response: $errorMsg")
                            Toast.makeText(this@UpdateItemActivity, "Gagal memuat data: ${resp.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<PlantResponse>, t: Throwable) {
                    setFormEnabled(true)
                    Log.e("UpdateActivity", "API call failed", t)
                    Toast.makeText(this@UpdateItemActivity, "Error jaringan: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updatePlant() {
        val newName = edNama.text.toString().trim()
        val newPrice = edHarga.text.toString().trim()
        val newDesc = edDesc.text.toString().trim()

        if (newName.isEmpty() || newPrice.isEmpty() || newDesc.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        setFormEnabled(false)

        // Encode nama asli untuk URL
        val encodedOriginalName = try {
            URLEncoder.encode(originalNameKey, "UTF-8")
        } catch (e: Exception) {
            originalNameKey
        }

        Log.d("UpdateActivity", "Updating plant with key: '$originalNameKey'")
        Log.d("UpdateActivity", "New data: name='$newName', price='$newPrice'")

        val updateRequest = PlantUpdateRequest(
            plant_name = newName,
            description = newDesc,
            price = newPrice
        )

        RetrofitClient.instance.updatePlant(encodedOriginalName, updateRequest)
            .enqueue(object : Callback<PlantResponse> {
                override fun onResponse(call: Call<PlantResponse>, resp: Response<PlantResponse>) {
                    setFormEnabled(true)

                    Log.d("UpdateActivity", "Update request URL: ${call.request().url}")
                    Log.d("UpdateActivity", "Update response code: ${resp.code()}")

                    when {
                        resp.isSuccessful -> {
                            Toast.makeText(this@UpdateItemActivity, "Update berhasil", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        resp.code() == 404 -> {
                            Toast.makeText(this@UpdateItemActivity, "Data tidak ditemukan", Toast.LENGTH_SHORT).show()
                        }
                        resp.code() == 400 -> {
                            Toast.makeText(this@UpdateItemActivity, "Data tidak valid", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            val errorMsg = try {
                                resp.errorBody()?.string() ?: "Unknown error"
                            } catch (e: Exception) {
                                "Error: ${resp.code()}"
                            }
                            Log.e("UpdateActivity", "Update error: $errorMsg")
                            Toast.makeText(this@UpdateItemActivity, "Update gagal: ${resp.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<PlantResponse>, t: Throwable) {
                    setFormEnabled(true)
                    Log.e("UpdateActivity", "Update failed", t)
                    Toast.makeText(this@UpdateItemActivity, "Error jaringan: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setFormEnabled(enabled: Boolean) {
        edNama.isEnabled = enabled
        edHarga.isEnabled = enabled
        edDesc.isEnabled = enabled
        btnSimpan.isEnabled = enabled

        if (!enabled) {
            btnSimpan.text = "Loading..."
        } else {
            btnSimpan.text = "Simpan"
        }
    }
}