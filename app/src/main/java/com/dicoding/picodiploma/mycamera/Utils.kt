package com.dicoding.picodiploma.mycamera

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

private const val FILENAME_FORMAT = "dd-MMM-yyyy"

val timeStamp: String = SimpleDateFormat(
    FILENAME_FORMAT,
    Locale.US
).format(System.currentTimeMillis())

fun createTempFile(context: Context): File {
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(timeStamp, ".jpg", storageDir)
}

fun createFile(application: Application): File {
    val mediaDir = application.externalMediaDirs.firstOrNull()?.let {
        File(it, application.resources.getString(R.string.app_name)).apply { mkdirs() }
    }

    val outputDirectory = if (
        mediaDir != null && mediaDir.exists()
    ) mediaDir else application.filesDir

    return File(outputDirectory, "$timeStamp.jpg")
}

fun rotateBitmap(bitmap: Bitmap, isBackCamera: Boolean = false): Bitmap {
    val matrix = Matrix()
    return if (isBackCamera) {
        matrix.postRotate(90f)
        Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    } else {
        matrix.postRotate(-90f)
        matrix.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
        Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }
}

// rotate and compress file
fun rotateAndCompressFile(file: File, isBackCamera: Boolean = false) {
    // make identity matrix for later scaling
    val matrix = Matrix()
    // decode the file into bitmap
    val bitmap = BitmapFactory.decodeFile(file.path)
    // set rotation according to which camera captured the picture
    val rotation = if (isBackCamera) 90f else -90f
    // rotate the image
    matrix.postRotate(rotation)
    // flip image in x, 3rd & 4th arguments set the pivot point to flip
    if (!isBackCamera) {
        matrix.postScale(-1f, 1f, (bitmap.width)/2f, (bitmap.height)/2f)
    }
    // create modified bitmap with old bitmap
    val result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    // compress the image and save to device using FileOutputStream
    result.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(file))
}