package com.example.testkotlin

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.hardware.Camera
import android.hardware.camera2.CameraManager
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import java.io.ByteArrayOutputStream
import android.R.attr.bitmap
import android.widget.ImageView


class ShowCamera : SurfaceView, SurfaceHolder.Callback
{
    private var surfaceHolder: SurfaceHolder? = null
    private var camera: Camera? = null
    private var cameraManager: CameraManager? = null
    private var imageView: ImageView? = null

    private var count:Long = 0
    private var previewSize: Camera.Size? = null
    private var pixels: IntArray? = null


    constructor(context: Context, camera: Camera, imageView: ImageView) : super(context) {
        this.camera = camera
        this.surfaceHolder = holder
        this.imageView = imageView
        holder.addCallback(this)

        cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        camera.setPreviewCallback(Camera.PreviewCallback { data, camera ->
            count++
            previewCallBack(data, camera)
        })
    }

    fun previewCallBack(data: ByteArray, camera:Camera): Unit
    {

        //if(count % 5 == 0.toLong())
        //{
            var bitmap: Bitmap = HelperConverter.toBitmap(data, previewSize!!.width, previewSize!!.height, camera.parameters.previewFormat)

            HelperConverter.decodeYUV420SP(pixels!!, data, previewSize!!.width, previewSize!!.height)

            process(bitmap, previewSize!!.width, previewSize!!.height)
        //}
    }

    fun process(bitmap: Bitmap, width: Int, height: Int)
    {
        var _bitmap = bitmap.copy(bitmap.config, true)
        for(i in 0..(width -1))
        {
            for(j in 0..(height -1))
            {
                var pixel = bitmap.getPixel(i, j)
                //Log.i("BITMAP", "BITMAP[${i}][${j}]=${}")
                var red = Color.red(pixel)
                var blue = Color.blue(pixel)
                var green = Color.green(pixel)


                var newPixelValue = (red + blue + green) / 3
                Log.i("COLOR", "newPixelValue=${newPixelValue}")
                //if(((red + blue + green) / 3) > 125)
                //    newPixelValue = 255
                //else
                //    newPixelValue = 0

                _bitmap.setPixel(i, j, Color.argb(255, newPixelValue, newPixelValue, newPixelValue))
            }
        }

        imageView!!.setImageBitmap(_bitmap)
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        var params: Camera.Parameters = camera!!.parameters

        if(this.resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE)
        {
            params["orientation"] = "portrait"
            camera!!.setDisplayOrientation(90)
            params.setRotation(90)
        }
        else
        {
            params["orientation"] = "landscape"
            camera!!.setDisplayOrientation(0)
            params.setRotation(0)
        }

        params.flashMode = Camera.Parameters.FLASH_MODE_TORCH

        var sizes: List<Camera.Size> = camera!!.parameters.getSupportedPictureSizes()

        var mSize: Camera.Size? = sizes[0]

        for (size in sizes) {
            Log.i("SIZES", "Available resolution: "+size.width+" "+size.height);

            if(mSize!!.height > size.height)
                mSize = size
            mSize = size;
        }

        params.setPictureSize(mSize!!.width, mSize!!.height)
        params.setPreviewSize(mSize!!.width, mSize!!.height)
        previewSize = mSize

        count = 0
        pixels = IntArray(previewSize!!.width * previewSize!!.height)

        camera!!.parameters = params
        camera!!.setPreviewDisplay(surfaceHolder)
        camera!!.startPreview()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        // logme
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        // logme
    }
}


class HelperConverter
{
    companion object {

        //Method from Ketai project! Not mine! See below...
        public  fun decodeYUV420SP(rgb: IntArray, yuv420sp: ByteArray, width: Int, height: Int)
        {

            val frameSize = width * height

            var j = 0
            var yp = 0
            while (j < height) {
                var uvp = frameSize + (j shr 1) * width
                var u = 0
                var v = 0
                var i = 0
                while (i < width)
                {
                    var y = (0xff and yuv420sp[yp].toInt()) - 16
                    if (y < 0)
                        y = 0
                    if (i and 1 == 0) {
                        v = (0xff and yuv420sp[uvp++].toInt()) - 128
                        u = (0xff and yuv420sp[uvp++].toInt()) - 128
                    }

                    val y1192 = 1192 * y
                    var r = y1192 + 1634 * v
                    var g = y1192 - 833 * v - 400 * u
                    var b = y1192 + 2066 * u

                    if (r < 0)
                        r = 0
                    else if (r > 262143)
                        r = 262143
                    if (g < 0)
                        g = 0
                    else if (g > 262143)
                        g = 262143
                    if (b < 0)
                        b = 0
                    else if (b > 262143)
                        b = 262143

                    rgb[yp] = -0x1000000 or (r shl 6 and 0xff0000) or (g shr 2 and 0xff00) or (b shr 10 and 0xff)
                    i++
                    yp++
                }
                j++
            }
        }

        public fun toBitmap(data: ByteArray, width: Int, height: Int, previewFormat:Int ): Bitmap
        {

            var yuvImage: YuvImage = YuvImage(data, previewFormat, width, height, null)

            var outputStream: ByteArrayOutputStream = ByteArrayOutputStream()

            yuvImage.compressToJpeg(Rect(0,0, width, height), 50, outputStream)

            var bytes: ByteArray = outputStream.toByteArray()

            var bitmap: Bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

            return bitmap
        }
    }

}
