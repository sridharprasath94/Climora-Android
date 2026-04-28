package com.flash.climora.presentation.weather

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flash.climora.databinding.ItemForecastDayBinding
import com.flash.climora.domain.model.ForecastDay
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class ForecastAdapter : ListAdapter<ForecastDay, ForecastAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemForecastDayBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemForecastDayBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(day: ForecastDay) {
            val date = LocalDate.parse(day.date)
            binding.textDay.text = if (date == LocalDate.now()) {
                binding.root.context.getString(com.flash.climora.R.string.forecast_today)
            } else {
                date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            }
            binding.imageConditionSmall.setImageResource(
                weatherIconRes(conditionCode = day.conditionCode, isDay = true)
            )
            binding.textTempRange.text = binding.root.context.getString(
                com.flash.climora.R.string.forecast_temp_range,
                day.maxTemp.toInt(),
                day.minTemp.toInt()
            )
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<ForecastDay>() {
        override fun areItemsTheSame(oldItem: ForecastDay, newItem: ForecastDay) =
            oldItem.date == newItem.date

        override fun areContentsTheSame(oldItem: ForecastDay, newItem: ForecastDay) =
            oldItem == newItem
    }
}
