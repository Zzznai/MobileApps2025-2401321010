package com.naim.plantcare.ui

import android.content.Context
import android.widget.TextView
import com.google.android.material.color.MaterialColors
import com.naim.plantcare.R
import com.naim.plantcare.logic.WateringCalculator.WateringStatus

object WateringStatusText {
    fun text(context: Context, status: WateringStatus): String = when (status) {
        is WateringStatus.InDays -> context.resources.getQuantityString(
            R.plurals.watering_in_days, status.days.toInt(), status.days
        )
        is WateringStatus.Today -> context.getString(R.string.watering_today)
        is WateringStatus.Overdue -> context.resources.getQuantityString(
            R.plurals.watering_overdue, status.days.toInt(), status.days
        )
    }

    fun show(textView: TextView, status: WateringStatus) {
        textView.text = text(textView.context, status)
        val colorAttr = when (status) {
            is WateringStatus.InDays -> com.google.android.material.R.attr.colorOnSurfaceVariant
            is WateringStatus.Today -> androidx.appcompat.R.attr.colorPrimary
            is WateringStatus.Overdue -> androidx.appcompat.R.attr.colorError
        }
        textView.setTextColor(MaterialColors.getColor(textView, colorAttr))
    }
}
