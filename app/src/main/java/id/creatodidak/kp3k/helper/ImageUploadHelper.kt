package id.creatodidak.kp3k.helper

import id.creatodidak.kp3k.api.newModel.DokumentasiUI
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.BitmapFactory
import android.os.Environment
import android.view.View
import id.creatodidak.kp3k.database.Dao.DraftMediaDao
import id.creatodidak.kp3k.database.Entity.MediaDraftEntity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.Date

object MediaDraftHelper {
    suspend fun saveDokumentasiToDraft(
        context: Context,
        dokumentasiList: List<DokumentasiUI>,
        currentId: Int?, // misalnya id tanaman
        nrp: String,
        dao: DraftMediaDao
    ): List<MediaDraftEntity> {
        return dokumentasiList.mapIndexed { index, dok ->
            val file = if (dok.isFromCamera) {
                compressCamera(context, File(dok.imagePath))
            } else {
                saveBitmapToFile(context, getBitmapFromView(dok.container))
            }

            val filename = file.name
            val url = file.absolutePath

            val entity = MediaDraftEntity(
                id = generateRandomId(),
                currentId = currentId,
                nrp = nrp,
                filename = filename,
                url = url,
                type = MediaType.IMAGE,
                createdAt = Date()
            )

            dao.insert(entity)
            entity
        }
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun saveBitmapToFile(context: Context, bitmap: Bitmap): File {
        val filename = "IMG_WM_${System.currentTimeMillis()}.jpg"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, out)
        }
        return file
    }

    private fun compressCamera(context: Context, inputFile: File, quality: Int = 40): File {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = false
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        val bitmap = BitmapFactory.decodeFile(inputFile.absolutePath, options)
        val compressedFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "COMPRESSED_${System.currentTimeMillis()}.jpg"
        )

        FileOutputStream(compressedFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        }

        return compressedFile
    }

    private fun generateRandomId(): Int {
        return (100000..999999).random()
    }
}
