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

class UpdateItemActivity : AppCompatActivity() {
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        setContentView(R.layout.edit_activity)

        val namaKey = intent.getStringExtra("nama") ?: ""
        val plantName = intent.getStringExtra("plant_name") ?: ""
        val plantPrice = intent.getStringExtra("plant_price") ?: ""
        val plantDesc = intent.getStringExtra("plant_desc") ?: ""

        Log.d("UpdateActivity", "namaKey yang dikirim: '$namaKey'")
        Log.d("UpdateActivity", "namaKey length: ${namaKey.length}")
        Log.d("UpdateActivity", "URL akan jadi: https://uappam.kuncipintu.my.id/plant/$namaKey")
        Log.d("UpdateActivity", "namaKey: '$namaKey'")
        Log.d("UpdateActivity", "plantName: '$plantName'")


        val edNama = findViewById<EditText>(R.id.editNama)
        val edHarga = findViewById<EditText>(R.id.editHarga)
        val edDesc = findViewById<EditText>(R.id.editDeskripsi)
        val iv = findViewById<ImageView>(R.id.imagePreview)
        val btnSimpan = findViewById<Button>(R.id.btnSimpan)

        iv.setImageResource(R.drawable.tumbuhan)



        // Disable form sementara saat loading
        setFormEnabled(false)

        // muat data lama
        RetrofitClient.instance.getPlant(namaKey)
            .enqueue(object: Callback<PlantResponse>{
                override fun onResponse(call: Call<PlantResponse>, resp: Response<PlantResponse>) {
                    // 👇 TAMBAHKAN LOG DEBUGGING DI SINI
                    Log.d("UpdateActivity", "Request URL: ${call.request().url}")
                    Log.d("UpdateActivity", "Response code: ${resp.code()}")
                    Log.d("UpdateActivity", "Response body: ${resp.body()}")

                    if(resp.isSuccessful && resp.body() != null){
                        val p = resp.body()!!.data
                        // 👇 LOG UNTUK CEK DATA YANG DITERIMA
                        Log.d("UpdateActivity", "Plant data: ${p.plant_name}, ${p.price}, ${p.description}")

                        edNama.setText(p.plant_name)
                        edHarga.setText(p.price)
                        edDesc.setText(p.description)
                    } else {
                        // 👇 LOG UNTUK CEK ERROR
                        Log.e("UpdateActivity", "Response not successful or body is null")
                    }
                }
                override fun onFailure(call: Call<PlantResponse>, t: Throwable) {
                    // 👇 LOG UNTUK CEK NETWORK ERROR
                    Log.e("UpdateActivity", "API call failed: ${t.message}")
                }
            })


        RetrofitClient.instance.getPlant(namaKey)
            .enqueue(object: Callback<PlantResponse>{
                override fun onResponse(call: Call<PlantResponse>, resp: Response<PlantResponse>) {
                    setFormEnabled(true) // Enable form kembali

                    if(resp.isSuccessful && resp.body() != null){
                        val p = resp.body()!!.data
                        edNama.setText(p.plant_name)
                        edHarga.setText(p.price)
                        edDesc.setText(p.description)
                    } else {
                        Toast.makeText(this@UpdateItemActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<PlantResponse>, t: Throwable) {
                    setFormEnabled(true)
                    Toast.makeText(this@UpdateItemActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }

            })

        btnSimpan.setOnClickListener {
            val newName = edNama.text.toString().trim()
            val newPrice = edHarga.text.toString().trim()
            val newDesc = edDesc.text.toString().trim()

            if (newName.isEmpty() || newPrice.isEmpty() || newDesc.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            RetrofitClient.instance.updatePlant(
                namaKey,
                PlantUpdateRequest(newName, newDesc, newPrice)
            ).enqueue(object: Callback<PlantResponse>{
                override fun onResponse(call: Call<PlantResponse>, resp: Response<PlantResponse>) {
                    if(resp.isSuccessful){
                        Toast.makeText(this@UpdateItemActivity,"Update sukses",Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@UpdateItemActivity,"Update gagal",Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<PlantResponse>, t: Throwable) {
                    Toast.makeText(this@UpdateItemActivity,"Error: ${t.message}",Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun setFormEnabled(enabled: Boolean) {
        findViewById<EditText>(R.id.editNama).isEnabled = enabled
        findViewById<EditText>(R.id.editHarga).isEnabled = enabled
        findViewById<EditText>(R.id.editDeskripsi).isEnabled = enabled
        findViewById<Button>(R.id.btnSimpan).isEnabled = enabled
    }

    private fun loadDataFromAPI(namaKey: String, edNama: EditText, edHarga: EditText, edDesc: EditText) {
        RetrofitClient.instance.getPlant(namaKey)
            .enqueue(object: Callback<PlantResponse>{
                override fun onResponse(call: Call<PlantResponse>, resp: Response<PlantResponse>) {
                    Log.d("UpdateActivity", "Request URL: ${call.request().url}")
                    Log.d("UpdateActivity", "Response code: ${resp.code()}")

                    if(resp.isSuccessful && resp.body() != null){
                        val p = resp.body()!!.data
                        edNama.setText(p.plant_name)
                        edHarga.setText(p.price)
                        edDesc.setText(p.description)
                    } else {
                        Toast.makeText(this@UpdateItemActivity, "Data tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<PlantResponse>, t: Throwable) {
                    Toast.makeText(this@UpdateItemActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            })
    }
}

