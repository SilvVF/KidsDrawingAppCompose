package my.packlol.kidsdrawingapp.data

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.PixelCopy
import android.view.View
import android.view.Window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class ImageSaver(
    private val context: Context,
    private val folderName: String = "drawing_app"
) {

    suspend fun saveImage(view: View, height: Int) = withContext(Dispatchers.IO) {
        view.toBitmap(
            clipHeight = height,
            onBitmapError = {
                throw it
            },
            onBitmapReady = {
                saveToGallery(it)
            }
        )
    }

    private fun saveToGallery(bitmap: Bitmap) {
        if (Build.VERSION.SDK_INT >= 29) {
            val values = contentValues()
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$folderName")
            values.put(MediaStore.Images.Media.IS_PENDING, true)
            // RELATIVE_PATH and IS_PENDING are introduced in API 29.

            val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                saveImageToStream(bitmap, context.contentResolver.openOutputStream(uri))
                values.put(MediaStore.Images.Media.IS_PENDING, false)
                context.contentResolver.update(uri, values, null, null)
            }
        } else {
            val directory = File(
                Environment.getExternalStorageDirectory().toString() + File.separator + folderName
            )
            // getExternalStorageDirectory is deprecated in API 29

            if (!directory.exists()) {
                directory.mkdirs()
            }
            val fileName = System.currentTimeMillis().toString() + ".png"
            val file = File(directory, fileName)
            saveImageToStream(bitmap, FileOutputStream(file))
            val values = contentValues()
            values.put(MediaStore.Images.Media.DATA, file.absolutePath)
            // .DATA is deprecated in API 29
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        }
    }

    private fun contentValues() : ContentValues {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        return values
    }

    private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
        if (outputStream != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()
        }
    }

    // Fixes hardware bitmap not supported exception
// https://stackoverflow.com/questions/58314397/java-lang-illegalstateexception-software-rendering-doesnt-support-hardware-bit
// start of extension.
    private fun View.toBitmap(clipHeight: Int, onBitmapReady: (Bitmap) -> Unit, onBitmapError: (Exception) -> Unit) {
        try {
            val temporalBitmap =
                Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
            // Above Android O, use PixelCopy due
            // https://stackoverflow.com/questions/58314397/
            val window: Window = (this.context as Activity).window

            val location = IntArray(2)

            this.getLocationInWindow(location)

            val viewRectangle =
                Rect(location[0], location[1], location[0] + this.width, location[1] + this.height - clipHeight)

            val onPixelCopyListener: PixelCopy.OnPixelCopyFinishedListener =
                PixelCopy.OnPixelCopyFinishedListener { copyResult ->
                    if (copyResult == PixelCopy.SUCCESS) {
                        onBitmapReady(temporalBitmap)
                    } else {
                        error("Error while copying pixels, copy result: $copyResult")
                    }
                }

            PixelCopy.request(
                window,
                viewRectangle,
                temporalBitmap,
                onPixelCopyListener,
                Handler(Looper.getMainLooper()))
        } catch (e : Exception) {
            onBitmapError(e)
        }
    }
}