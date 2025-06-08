// PlantAdapter.kt
package com.example.uap_pam_alvasr

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
            RetrofitClient.instance.deletePlant(plant.plant_name)
                .enqueue(object : Callback<GenericResponse> {
                    override fun onResponse(
                        call: Call<GenericResponse>,
                        response: Response<GenericResponse>
                    ) {
                        items.removeAt(position)
                        notifyItemRemoved(position)
                    }
                    override fun onFailure(call: Call<GenericResponse>, t: Throwable) { }
                })
        }
        holder.btnDet.setOnClickListener {
            ctx.startActivity(
                Intent(ctx, DetailActivity::class.java)
                    .putExtra("nama", plant.plant_name)
            )
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newList: List<Plant>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}
