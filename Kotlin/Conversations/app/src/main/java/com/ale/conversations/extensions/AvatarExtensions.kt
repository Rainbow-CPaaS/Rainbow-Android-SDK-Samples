package com.ale.conversations.extensions

import android.app.Activity
import android.content.res.Resources
import android.graphics.*
import android.text.TextPaint
import com.ale.conversations.R
import com.ale.infra.contact.IRainbowContact
import com.ale.infra.manager.room.Room
import java.util.*

fun IRainbowContact.getPictureForRainbowContact(activity: Activity) : Bitmap =
    if (this.photo != null)
        this.photo
    else
        getPictureByInitials(activity, this.firstName.substring(0,1) + this.lastName.substring(0,1))

fun Room.getPictureForRoom(activity: Activity) : Bitmap =
    when {
        this.photo != null -> this.photo
        this.name.isNotEmpty() -> {
            getPictureByInitials(activity, this.name.substring(0, 2).toUpperCase(Locale.getDefault()))
        }
        else -> {
            getPictureByInitials(activity, this.name)
        }
    }


fun getPictureByInitials(activity: Activity, initials: String) : Bitmap {
    val drawable = activity.getDrawable(R.drawable.circle) ?: throw Resources.NotFoundException()

    val picture = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(picture)

    drawable.apply {
        bounds = Rect(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        draw(canvas)
    }

    val paint = TextPaint()
    paint.apply {
        style = Paint.Style.FILL
        textSize = 48f
        color = Color.WHITE
    }

    canvas.drawText(
        initials,
        (picture.width / 2) - 30f,
        (picture.height / 2) + 12f,
        paint
    )

    return picture
}