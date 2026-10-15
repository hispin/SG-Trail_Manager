package com.sensoguard.trailmanager.global

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.createBitmap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory


//convert drawable resource to bitmap descriptor (for map markers)
fun convertBitmapToBitmapDescriptor(context: Context, resId: Int): BitmapDescriptor? {
    return getBitmapFromVectorDrawable(
        context,
        resId
    )?.let { BitmapDescriptorFactory.fromBitmap(it) }
}

//convert resId to bitmap
fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap? {
    val drawable = AppCompatResources.getDrawable(context, drawableId) ?: return null

    val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)

    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return bitmap
}
