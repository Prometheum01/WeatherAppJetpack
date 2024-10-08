package com.rurouni.weatherapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
import androidx.recyclerview.widget.RecyclerView
import com.rurouni.weatherapp.R
import com.rurouni.weatherapp.databinding.DayItemBinding
import com.rurouni.weatherapp.ui.animation.CustomAnimation.animateImageViewTintColorChange
import com.rurouni.weatherapp.ui.animation.CustomAnimation.animateTextColorChange
import com.rurouni.weatherapp.ui.model.ColorState
import com.rurouni.weatherapp.ui.model.ForecastDayItem
import com.rurouni.weatherapp.utils.Utils.codeToIconId

class DayListAdapter : RecyclerView.Adapter<DayListAdapter.ViewHolder>() {

    private var list = listOf<ForecastDayItem>()

    fun setList(list: List<ForecastDayItem>) {
        this.list = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: DayItemBinding) : RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DayItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding

        //This function hides this fields in first position
        if (position == 0) {
            binding.imgRainPercentage.visibility = View.GONE
            binding.tvRainPercentage.visibility = View.GONE
            binding.imgCondition.visibility = View.GONE
        }

        //This function starts the animation of the list elements if they are in animation mode.
        if (isAnimate) {
            if (position == 0) {
                animateTextColorChange(binding.tvDay, colorState.currentPalette.onSecondary, colorState.nextPalette.onSecondary)
                animateTextColorChange(binding.tvHighTemperature, colorState.currentPalette.onSecondary, colorState.nextPalette.onSecondary)
                animateTextColorChange(binding.tvLowTemperature, colorState.currentPalette.onSecondary, colorState.nextPalette.onSecondary)
            }
            else{
                animateTextColorChange(binding.tvDay, colorState.currentPalette.onPrimary, colorState.nextPalette.onPrimary)
                animateTextColorChange(binding.tvHighTemperature, colorState.currentPalette.onPrimary, colorState.nextPalette.onPrimary)
                animateTextColorChange(binding.tvLowTemperature, colorState.currentPalette.onPrimary, colorState.nextPalette.onPrimary)
                animateTextColorChange(binding.tvRainPercentage, colorState.currentPalette.onSecondary, colorState.nextPalette.onSecondary)
                animateImageViewTintColorChange(binding.imgRainPercentage, colorState.currentPalette.dropColor, colorState.nextPalette.dropColor).doOnEnd {
                    isAnimate = false
                    setColorState(nextColorState)
                }
            }
        }

        binding.tvDay.text = list[position].day
        binding.tvHighTemperature.text = "${list[position].highTemperature}°"
        binding.tvLowTemperature.text = "${list[position].lowTemperature}°"
        binding.tvRainPercentage.text = "%${list[position].rainChance}"

        if (list[position].rainChance <= 25) {
            binding.imgRainPercentage.setImageResource(R.drawable.ic_no_drop)
        }
        else if(list[position].rainChance <= 75) {
            binding.imgRainPercentage.setImageResource(R.drawable.ic_half_drop)
        }
        else {
            binding.imgRainPercentage.setImageResource(R.drawable.ic_full_drop)
        }

        val iconCode = list[position].condition.toIntOrNull()

        iconCode?.let {
            val icon = codeToIconId(holder.itemView.context, it)

            icon?.let { id ->
                binding.imgCondition.setImageResource(id)
            }
        }

        //This function sets color of the list elements if they are not in animation mode.
        if (!isAnimate) {
            if (position == 0) {
                binding.tvDay.setTextColor(colorState.currentPalette.onSecondary)
                binding.tvHighTemperature.setTextColor(colorState.currentPalette.onSecondary)
                binding.tvLowTemperature.setTextColor(colorState.currentPalette.onSecondary)
            }else {
                binding.tvDay.setTextColor(colorState.currentPalette.onPrimary)
                binding.tvHighTemperature.setTextColor(colorState.currentPalette.onPrimary)
                binding.tvLowTemperature.setTextColor(colorState.currentPalette.onPrimary)
                binding.tvRainPercentage.setTextColor(colorState.currentPalette.onSecondary)
                binding.imgRainPercentage.setColorFilter(colorState.currentPalette.dropColor)
            }
        }
    }

    private lateinit var colorState: ColorState
    private lateinit var nextColorState: ColorState
    private var isAnimate = false

    fun setColorState(state: ColorState) {
        this.colorState = state
        notifyDataSetChanged()
    }

    fun animate(nextColorState: ColorState) {
        this.isAnimate = true
        this.nextColorState = nextColorState
        notifyDataSetChanged()
    }
}