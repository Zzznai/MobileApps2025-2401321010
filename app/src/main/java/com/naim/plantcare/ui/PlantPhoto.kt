package com.naim.plantcare.ui

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import coil.dispose
import coil.load
import com.naim.plantcare.R
import java.io.File

object PlantPhoto {
    fun showInBadge(photo: ImageView, initial: TextView, path: String?) {
        if (path == null) {
            photo.dispose()
            photo.setImageDrawable(null)
            photo.visibility = View.GONE
            initial.visibility = View.VISIBLE
        } else {
            photo.visibility = View.VISIBLE
            initial.visibility = View.INVISIBLE
            photo.load(File(path)) { crossfade(true) }
        }
    }

    fun showInForm(photo: ImageView, path: String?) {
        if (path == null) {
            photo.dispose()
            photo.scaleType = ImageView.ScaleType.CENTER
            photo.setImageResource(R.drawable.ic_add_photo)
        } else {
            photo.scaleType = ImageView.ScaleType.CENTER_CROP
            photo.load(File(path)) { crossfade(true) }
        }
    }
}
