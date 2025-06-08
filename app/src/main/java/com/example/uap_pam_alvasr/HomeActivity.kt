package com.example.uap_pam_alvasr

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {
    private lateinit var adapter: PlantAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        // 1. Inisialisasi RecyclerView dari XML
        val rv = findViewById<RecyclerView>(R.id.recyclerView).apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
        }

        // 2. Inisialisasi adapter
        adapter = PlantAdapter(this, mutableListOf())
        rv.adapter = adapter

        // 3. Tombol Tambah
        findViewById<Button>(R.id.btnTambahList).setOnClickListener {
            startActivity(Intent(this, TambahActivity::class.java))
        }

        // 4. Panggil API untuk fetch data
        loadPlants()
    }

    override fun onResume() {
        super.onResume()
        // Reload data setiap kembali ke sini
        loadPlants()
    }

    private fun loadPlants() {
        RetrofitClient.instance.getAllPlants()
            .enqueue(object : Callback<PlantListResponse> {
                override fun onResponse(
                    call: Call<PlantListResponse>,
                    response: Response<PlantListResponse>
                ) {
                    if (response.isSuccessful) {
                        val list = response.body()?.data ?: emptyList()
                        adapter.updateList(list)
                    }
                }
                override fun onFailure(call: Call<PlantListResponse>, t: Throwable) {
                    // optional: tampilkan Toast atau log error
                }
            })
    }
}
