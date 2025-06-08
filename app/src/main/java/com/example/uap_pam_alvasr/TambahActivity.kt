package com.example.uap_pam_alvasr

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TambahActivity : AppCompatActivity() {
    private lateinit var edNama: EditText
    private lateinit var edHarga: EditText
    private lateinit var edDesc: EditText
    private lateinit var btnTambah: Button

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        setContentView(R.layout.tambah_tanaman)

        edNama = findViewById(R.id.editNama)
        edHarga = findViewById(R.id.editHarga)
        edDesc = findViewById(R.id.editDeskripsi)
        btnTambah = findViewById(R.id.btnTambah)

        btnTambah.setOnClickListener {
            createPlant()
        }
    }

    private fun createPlant() {
        val nama = edNama.text.toString().trim()
        val harga = edHarga.text.toString().trim()
        val desc = edDesc.text.toString().trim()

        // Validasi input
        if (nama.isEmpty()) {
            edNama.error = "Nama tanaman harus diisi"
            edNama.requestFocus()
            return
        }

        if (harga.isEmpty()) {
            edHarga.error = "Harga harus diisi"
            edHarga.requestFocus()
            return
        }

        if (desc.isEmpty()) {
            edDesc.error = "Deskripsi harus diisi"
            edDesc.requestFocus()
            return
        }

        // Validasi harga (harus berupa angka)
        try {
            harga.toDouble()
        } catch (e: NumberFormatException) {
            edHarga.error = "Harga harus berupa angka"
            edHarga.requestFocus()
            return
        }

        // Disable form saat proses
        setFormEnabled(false)

        Log.d("TambahActivity", "Creating plant: name='$nama', price='$harga'")

        val request = PlantCreateRequest(
            plant_name = nama,
            description = desc,
            price = harga
        )

        RetrofitClient.instance.createPlant(request)
            .enqueue(object : Callback<PlantResponse> {
                override fun onResponse(
                    call: Call<PlantResponse>,
                    resp: Response<PlantResponse>
                ) {
                    setFormEnabled(true)

                    Log.d("TambahActivity", "Create request URL: ${call.request().url}")
                    Log.d("TambahActivity", "Create response code: ${resp.code()}")

                    when {
                        resp.isSuccessful -> {
                            Toast.makeText(this@TambahActivity, "Berhasil menambah tanaman", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        resp.code() == 400 -> {
                            Toast.makeText(this@TambahActivity, "Data tidak valid", Toast.LENGTH_SHORT).show()
                        }
                        resp.code() == 409 -> {
                            Toast.makeText(this@TambahActivity, "Nama tanaman sudah ada", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            val errorMsg = try {
                                resp.errorBody()?.string() ?: "Unknown error"
                            } catch (e: Exception) {
                                "Error: ${resp.code()}"
                            }
                            Log.e("TambahActivity", "Create error: $errorMsg")
                            Toast.makeText(this@TambahActivity, "Gagal menambah: ${resp.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<PlantResponse>, t: Throwable) {
                    setFormEnabled(true)
                    Log.e("TambahActivity", "Create failed", t)
                    Toast.makeText(this@TambahActivity, "Error jaringan: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setFormEnabled(enabled: Boolean) {
        edNama.isEnabled = enabled
        edHarga.isEnabled = enabled
        edDesc.isEnabled = enabled
        btnTambah.isEnabled = enabled

        if (!enabled) {
            btnTambah.text = "Loading..."
        } else {
            btnTambah.text = "Tambah"
        }
    }
}