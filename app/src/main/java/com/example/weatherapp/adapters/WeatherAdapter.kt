package com.example.weatherapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.databinding.ListItemBinding
import com.squareup.picasso.Picasso

class WeatherAdapter(val listener: Listener?) : ListAdapter<WeatherModel, WeatherAdapter.Holder>(Comparator()) {

    class Holder(view: View, private val listener: Listener?) : RecyclerView.ViewHolder(view) {
        private val binding = ListItemBinding.bind(view)
        var itemTemp: WeatherModel? = null
        init {
            itemView.setOnClickListener {
                itemTemp?.let { it1 -> listener?.onClick(item = it1) }
            }
        }
        fun bind(item: WeatherModel) = with(binding) {
            itemTemp = item
            tvData.text = item.time
            tvCondition.text = item.condition
            val curTemp = if (item.currentTemp.isNotEmpty())
                item.currentTemp + "ºC"
            else
                "${item.maxTemp}ºC / ${item.minTemp}ºC"

            tvTemp.text = curTemp
            Picasso.get().load("https:" + item.imageUrl).into(imImage)
        }
    }

    class Comparator : DiffUtil.ItemCallback<WeatherModel>() {

        override fun areItemsTheSame(oldItem: WeatherModel, newItem: WeatherModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: WeatherModel, newItem: WeatherModel): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item, parent, false), listener)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    interface Listener {
        fun onClick(item: WeatherModel)
    }
}