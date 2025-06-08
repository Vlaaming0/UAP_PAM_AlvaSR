package com.example.uap_pam_alvasr

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URLEncoder

class PlantAdapter(
    private val ctx: Context,
    private var items: MutableList<Plant>
) : RecyclerView.Adapter<PlantAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val iv: ImageView = view.findViewById(R.id.ivItemImage)
        val nama: TextView = view.findViewById(R.id.tvItemName)
        val harga: TextView = view.findViewById(R.id.tvItemPrice)
        val btnDel: Button = view.findViewById(R.id.btnItemDelete)
        val btnDet: Button = view.findViewById(R.id.btnItemDetail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plant, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val plant = items[position]
        holder.nama.text = plant.plant_name
        holder.harga.text = "Rp ${plant.price}"
        holder.iv.setImageResource(R.drawable.tumbuhan)

        holder.btnDel.setOnClickListener {
            deletePlant(plant, position)
        }

        holder.btnDet.setOnClickListener {
            val intent = Intent(ctx, DetailActivity::class.java).apply {
                putExtra("nama", plant.plant_name)
            }
            ctx.startActivity(intent)
        }
    }

    private fun deletePlant(plant: Plant, position: Int) {
        Log.d("PlantAdapter", "🗑️ Attempting to delete plant: '${plant.plant_name}'")

        // Buat encoding options dengan urutan yang benar
        val encodingOptions = mutableListOf<String>()

        try {
            // 1. Standard URL encoding dulu
            val urlEncoded = URLEncoder.encode(plant.plant_name, "UTF-8")
            encodingOptions.add(urlEncoded)

            // 2. Original jika berbeda
            if (urlEncoded != plant.plant_name) {
                encodingOptions.add(plant.plant_name)
            }

            // 3. Manual replacements jika ada space
            if (plant.plant_name.contains(" ")) {
                val spaceToPlus = plant.plant_name.replace(" ", "+")
                if (!encodingOptions.contains(spaceToPlus)) {
                    encodingOptions.add(spaceToPlus)
                }

                val spaceToPercent = plant.plant_name.replace(" ", "%20")
                if (!encodingOptions.contains(spaceToPercent)) {
                    encodingOptions.add(spaceToPercent)
                }
            }

        } catch (e: Exception) {
            Log.e("PlantAdapter", "Error creating encoding options", e)
            encodingOptions.add(plant.plant_name)
        }

        Log.d("PlantAdapter", "Trying ${encodingOptions.size} encoding methods: $encodingOptions")

        // Disable button selama proses
        notifyItemChanged(position)

        tryDeleteWithEncodingOptions(plant, position, encodingOptions, 0)
    }

    private fun tryDeleteWithEncodingOptions(plant: Plant, position: Int, options: List<String>, index: Int) {
        if (index >= options.size) {
            Log.e("PlantAdapter", "❌ All ${options.size} delete encoding options failed")
            Toast.makeText(ctx, "Gagal menghapus dengan semua metode encoding", Toast.LENGTH_SHORT).show()
            return
        }

        val encodedName = options[index]
        Log.d("PlantAdapter", "🔄 Trying delete with encoding ${index + 1}/${options.size}: '$encodedName'")

        RetrofitClient.instance.deletePlant(encodedName)
            .enqueue(object : Callback<GenericResponse> {
                override fun onResponse(
                    call: Call<GenericResponse>,
                    response: Response<GenericResponse>
                ) {
                    val requestUrl = call.request().url.toString()
                    Log.d("PlantAdapter", "Delete request URL: $requestUrl")
                    Log.d("PlantAdapter", "Delete response code: ${response.code()}")

                    when {
                        response.isSuccessful -> {
                            // Berhasil hapus
                            val currentPosition = items.indexOf(plant)
                            if (currentPosition >= 0) {
                                items.removeAt(currentPosition)
                                notifyItemRemoved(currentPosition)
                                notifyItemRangeChanged(currentPosition, items.size)
                            }
                            Toast.makeText(ctx, "✅ Item berhasil dihapus", Toast.LENGTH_SHORT).show()
                            Log.d("PlantAdapter", "✅ Delete successful with encoding ${index + 1}")
                        }

                        response.code() == 404 && index < options.size - 1 -> {
                            Log.d("PlantAdapter", "❌ Delete encoding ${index + 1} returned 404, trying next")
                            tryDeleteWithEncodingOptions(plant, position, options, index + 1)
                        }

                        response.code() == 404 -> {
                            Log.e("PlantAdapter", "❌ Item not found with any encoding method")
                            Toast.makeText(ctx, "Item tidak ditemukan", Toast.LENGTH_SHORT).show()
                        }

                        else -> {
                            val errorMsg = try {
                                response.errorBody()?.string() ?: "Unknown error"
                            } catch (e: Exception) {
                                "Error: ${response.code()}"
                            }
                            Log.e("PlantAdapter", "❌ Delete error with encoding ${index + 1}: $errorMsg")

                            if (index < options.size - 1) {
                                Log.d("PlantAdapter", "Trying next delete encoding...")
                                tryDeleteWithEncodingOptions(plant, position, options, index + 1)
                            } else {
                                Toast.makeText(ctx, "Gagal menghapus: ${response.code()}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    Log.e("PlantAdapter", "❌ Delete network error with encoding ${index + 1}: ${t.message}", t)

                    if (index < options.size - 1) {
                        Log.d("PlantAdapter", "Network error, trying next delete encoding...")
                        tryDeleteWithEncodingOptions(plant, position, options, index + 1)
                    } else {
                        Toast.makeText(ctx, "Error jaringan: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newList: List<Plant>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}