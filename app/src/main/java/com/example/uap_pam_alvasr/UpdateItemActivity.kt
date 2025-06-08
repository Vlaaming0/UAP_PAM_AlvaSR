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
            Log.d("UpdateActivity", "Using data from intent")
            edNama.setText(plantName)
            edHarga.setText(plantPrice)
            edDesc.setText(plantDesc)
            setFormEnabled(true)
        } else {
            // Jika tidak ada data dari intent, load dari API
            Log.d("UpdateActivity", "Data from intent is empty, loading from API")
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

        // Coba beberapa metode encoding
        val possibleEncodings = listOf(
            originalNameKey, // Original tanpa encoding
            try { URLEncoder.encode(originalNameKey, "UTF-8") } catch (e: Exception) { originalNameKey },
            originalNameKey.replace(" ", "%20"), // Manual space encoding
            originalNameKey.replace(" ", "+") // Plus encoding
        ).distinct()

        Log.d("UpdateActivity", "Trying encodings: $possibleEncodings")

        tryLoadWithDifferentEncodings(possibleEncodings, 0)
    }

    private fun tryLoadWithDifferentEncodings(encodings: List<String>, index: Int) {
        if (index >= encodings.size) {
            setFormEnabled(true)
            Toast.makeText(this, "Gagal memuat data dengan semua encoding", Toast.LENGTH_SHORT).show()
            return
        }

        val encodedName = encodings[index]
        Log.d("UpdateActivity", "Trying encoding $index: '$encodedName'")

        RetrofitClient.instance.getPlant(encodedName)
            .enqueue(object : Callback<PlantResponse> {
                override fun onResponse(call: Call<PlantResponse>, resp: Response<PlantResponse>) {
                    Log.d("UpdateActivity", "Request URL: ${call.request().url}")
                    Log.d("UpdateActivity", "Response code: ${resp.code()}")

                    when {
                        resp.isSuccessful && resp.body() != null -> {
                            val plant = resp.body()!!.data
                            Log.d("UpdateActivity", "Plant loaded successfully: ${plant.plant_name}")

                            edNama.setText(plant.plant_name)
                            edHarga.setText(plant.price)
                            edDesc.setText(plant.description)
                            setFormEnabled(true)
                        }
                        resp.code() == 404 && index < encodings.size - 1 -> {
                            Log.d("UpdateActivity", "Encoding $index failed with 404, trying next")
                            tryLoadWithDifferentEncodings(encodings, index + 1)
                        }
                        resp.code() == 404 -> {
                            setFormEnabled(true)
                            Toast.makeText(this@UpdateItemActivity, "Data tidak ditemukan", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            val errorMsg = try {
                                resp.errorBody()?.string() ?: "Unknown error"
                            } catch (e: Exception) {
                                "Error reading response"
                            }
                            Log.e("UpdateActivity", "Error response: $errorMsg")

                            if (index < encodings.size - 1) {
                                Log.d("UpdateActivity", "Encoding $index failed with ${resp.code()}, trying next")
                                tryLoadWithDifferentEncodings(encodings, index + 1)
                            } else {
                                setFormEnabled(true)
                                Toast.makeText(this@UpdateItemActivity, "Gagal memuat data: ${resp.code()}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<PlantResponse>, t: Throwable) {
                    Log.e("UpdateActivity", "API call failed for encoding $index", t)

                    if (index < encodings.size - 1) {
                        Log.d("UpdateActivity", "Network error for encoding $index, trying next")
                        tryLoadWithDifferentEncodings(encodings, index + 1)
                    } else {
                        setFormEnabled(true)
                        Toast.makeText(this@UpdateItemActivity, "Error jaringan: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
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

        // Validasi harga
        try {
            newPrice.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Harga harus berupa angka", Toast.LENGTH_SHORT).show()
            return
        }

        setFormEnabled(false)

        // Encode nama asli untuk URL - coba beberapa metode
        val possibleEncodings = listOf(
            originalNameKey,
            try { URLEncoder.encode(originalNameKey, "UTF-8") } catch (e: Exception) { originalNameKey },
            originalNameKey.replace(" ", "%20"),
            originalNameKey.replace(" ", "+")
        ).distinct()

        Log.d("UpdateActivity", "Updating plant with original key: '$originalNameKey'")
        Log.d("UpdateActivity", "New data: name='$newName', price='$newPrice'")

        val updateRequest = PlantUpdateRequest(
            plant_name = newName,
            description = newDesc,
            price = newPrice
        )

        tryUpdateWithDifferentEncodings(possibleEncodings, 0, updateRequest)
    }

    private fun tryUpdateWithDifferentEncodings(encodings: List<String>, index: Int, updateRequest: PlantUpdateRequest) {
        if (index >= encodings.size) {
            setFormEnabled(true)
            Toast.makeText(this, "Update gagal dengan semua encoding", Toast.LENGTH_SHORT).show()
            return
        }

        val encodedName = encodings[index]
        Log.d("UpdateActivity", "Trying update with encoding $index: '$encodedName'")

        RetrofitClient.instance.updatePlant(encodedName, updateRequest)
            .enqueue(object : Callback<PlantResponse> {
                override fun onResponse(call: Call<PlantResponse>, resp: Response<PlantResponse>) {
                    Log.d("UpdateActivity", "Update request URL: ${call.request().url}")
                    Log.d("UpdateActivity", "Update response code: ${resp.code()}")

                    when {
                        resp.isSuccessful -> {
                            setFormEnabled(true)
                            Toast.makeText(this@UpdateItemActivity, "Update berhasil", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        resp.code() == 404 && index < encodings.size - 1 -> {
                            Log.d("UpdateActivity", "Update encoding $index failed with 404, trying next")
                            tryUpdateWithDifferentEncodings(encodings, index + 1, updateRequest)
                        }
                        resp.code() == 404 -> {
                            setFormEnabled(true)
                            Toast.makeText(this@UpdateItemActivity, "Data tidak ditemukan", Toast.LENGTH_SHORT).show()
                        }
                        resp.code() == 400 -> {
                            setFormEnabled(true)
                            Toast.makeText(this@UpdateItemActivity, "Data tidak valid", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            val errorMsg = try {
                                resp.errorBody()?.string() ?: "Unknown error"
                            } catch (e: Exception) {
                                "Error: ${resp.code()}"
                            }
                            Log.e("UpdateActivity", "Update error: $errorMsg")

                            if (index < encodings.size - 1) {
                                Log.d("UpdateActivity", "Update encoding $index failed with ${resp.code()}, trying next")
                                tryUpdateWithDifferentEncodings(encodings, index + 1, updateRequest)
                            } else {
                                setFormEnabled(true)
                                Toast.makeText(this@UpdateItemActivity, "Update gagal: ${resp.code()}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<PlantResponse>, t: Throwable) {
                    Log.e("UpdateActivity", "Update failed for encoding $index", t)

                    if (index < encodings.size - 1) {
                        Log.d("UpdateActivity", "Network error for update encoding $index, trying next")
                        tryUpdateWithDifferentEncodings(encodings, index + 1, updateRequest)
                    } else {
                        setFormEnabled(true)
                        Toast.makeText(this@UpdateItemActivity, "Error jaringan: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
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