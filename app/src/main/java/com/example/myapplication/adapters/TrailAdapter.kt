package com.example.myapplication.adapters

import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.db.Trekk
import com.example.myapplication.other.TrackingUtility
import kotlinx.android.synthetic.main.item_trekk.view.*
import java.text.SimpleDateFormat
import java.util.*
// This class is made with the RecyclerView to display all custom trails and their data
class TrailAdapter: RecyclerView.Adapter<TrailAdapter.TrailViewHolder>() {
    inner class TrailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    val diffCallback = object : DiffUtil.ItemCallback<Trekk>() {
        override fun areItemsTheSame(oldItem: Trekk, newItem: Trekk): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Trekk, newItem: Trekk): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }
    val differ = AsyncListDiffer(this, diffCallback)
    fun submitList(list: List<Trekk>) = differ.submitList(list)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrailAdapter.TrailViewHolder {
        return TrailViewHolder(
                LayoutInflater.from(parent.context).inflate(
                        R.layout.item_trekk,
                        parent,
                        false
                )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: TrailAdapter.TrailViewHolder, position: Int) {
        val trail = differ.currentList[position]
        holder.itemView.apply {
            Glide.with(this).load(trail.img).into(ivTrailImage)
            val calendar = Calendar.getInstance().apply {
                timeInMillis = trail.timestamp
            }
            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            tvDate.text = dateFormat.format(calendar.time)

            val avgSpeed = "${trail.avgSpeedInKMH}km/h"
            tvAvgSpeed.text = avgSpeed

            val distanceInKM = "${trail.distanceInMeters/1000f}km"
            tvDistance.text = distanceInKM

            tvTime.text = TrackingUtility.getStopWatchTime(trail.timeInMillis)


        }
    }
}