package com.runnershelp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.recyclerview.widget.RecyclerView

class PaceAdapter(
    private val paces: MutableList<Pace>
) : RecyclerView.Adapter<PaceAdapter.PaceViewHolder>() {

    class PaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaceViewHolder {
        return PaceViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.pace_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return paces.size
    }

    override fun onBindViewHolder(holder: PaceViewHolder, position: Int) {
        val curPace = paces[position]
        holder.itemView.apply {
            val tvPaceName = findViewById<TextView>(R.id.tvPaceName)
            val tvPaceValue = findViewById<TextView>(R.id.tvPaceValue)

            tvPaceName.text = curPace.title
            tvPaceValue.text = curPace.pace
        }
    }

    fun addPace(pace: Pace) {
        paces.add(pace)
        notifyItemInserted(paces.size -1)
    }

    fun deletePace() {
        paces.clear()
        notifyDataSetChanged()

    }

    fun empty():Boolean{
        return paces.isEmpty()
    }
}