package id.creatodidak.kp3k.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

fun resizeMapIcon(context: Context, drawableRes: Int, width: Int, height: Int): BitmapDescriptor {
    val imageBitmap = BitmapFactory.decodeResource(context.resources, drawableRes)
    val resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false)
    return BitmapDescriptorFactory.fromBitmap(resizedBitmap)
}
