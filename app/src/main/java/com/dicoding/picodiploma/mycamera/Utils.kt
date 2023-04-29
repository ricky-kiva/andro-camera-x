package com.dicoding.picodiploma.mycamera

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

private const val FILENAME_FORMAT = "dd-MMM-yyyy"
private const val MAXIMAL_SIZE = 1000000 // maximal size for multipart to send

val timeStamp: String = SimpleDateFormat(
    FILENAME_FORMAT,
    Locale.US
).format(System.currentTimeMillis())

// make temporary file
fun createTempFile(context: Context): File {
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(timeStamp, ".jpg", storageDir)
}

// create a file
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

// convert URI to file
fun uriToFile(selectedImg: Uri, context: Context): File {

    // ContentResolver provides access to content providers
    // used to share data between apps
    val contentResolver: ContentResolver = context.contentResolver

    // make new empty temp file
    val myFile = createTempFile(context)

    // openInputStream for selectedImg (Uri) from ContentResolver
    val inputStream = contentResolver.openInputStream(selectedImg) as InputStream
    // make new FileOutputStream for myFile (temp file)
    val outputStream: OutputStream = FileOutputStream(myFile)

    val buf = ByteArray(1024)
    var len: Int
    //read inputStream in chunks of 1024 bytes & write it to outputStream
    while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)

    // close the outputStream & inputStream
    outputStream.close()
    inputStream.close()
    return myFile
}

fun reduceFileImage(file: File): File {
    // decode the input file to Bitmap
    val bitmap = BitmapFactory.decodeFile(file.path)
    var compressQuality = 100
    var streamLength: Int
    do {
        // compress Bitmap in while loop, until streamLength < MAXIMAL_SIZE
        val bmpStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
        val bmpPicByteArray = bmpStream.toByteArray()
        streamLength = bmpPicByteArray.size
        compressQuality -= 5
    } while (streamLength > MAXIMAL_SIZE)
    // compress & write the compressed byte array to the same file using FileOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))
    return file
}