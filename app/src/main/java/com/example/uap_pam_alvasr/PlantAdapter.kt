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
        // Disable button sementara
        val holder = items.indexOf(plant)
        if (holder >= 0) {
            // Optional: Show loading state
        }

        // Encode nama untuk URL yang aman
        val encodedName = try {
            URLEncoder.encode(plant.plant_name, "UTF-8")
        } catch (e: Exception) {
            plant.plant_name
        }

        Log.d("PlantAdapter", "Deleting plant: '${plant.plant_name}'")
        Log.d("PlantAdapter", "Encoded name: '$encodedName'")

        RetrofitClient.instance.deletePlant(encodedName)
            .enqueue(object : Callback<GenericResponse> {
                override fun onResponse(
                    call: Call<GenericResponse>,
                    response: Response<GenericResponse>
                ) {
                    Log.d("PlantAdapter", "Delete request URL: ${call.request().url}")
                    Log.d("PlantAdapter", "Delete response code: ${response.code()}")

                    when {
                        response.isSuccessful -> {
                            // Remove item dari list dan update RecyclerView
                            val currentPosition = items.indexOf(plant)
                            if (currentPosition >= 0) {
                                items.removeAt(currentPosition)
                                notifyItemRemoved(currentPosition)
                                // Update posisi item yang ada setelah penghapusan
                                notifyItemRangeChanged(currentPosition, items.size)
                            }
                            Toast.makeText(ctx, "Item berhasil dihapus", Toast.LENGTH_SHORT).show()
                        }
                        response.code() == 404 -> {
                            Toast.makeText(ctx, "Item tidak ditemukan", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            val errorMsg = try {
                                response.errorBody()?.string() ?: "Unknown error"
                            } catch (e: Exception) {
                                "Error: ${response.code()}"
                            }
                            Log.e("PlantAdapter", "Delete error: $errorMsg")
                            Toast.makeText(ctx, "Gagal menghapus: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    Log.e("PlantAdapter", "Delete failed", t)
                    Toast.makeText(ctx, "Error jaringan: ${t.message}", Toast.LENGTH_SHORT).show()
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