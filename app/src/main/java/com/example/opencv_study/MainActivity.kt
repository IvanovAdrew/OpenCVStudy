package com.example.opencv_study

import android.Manifest.permission.CAMERA
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.CvType
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import android.hardware.Camera
import android.os.Environment
import android.view.MenuItem
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.app.NavUtils
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.opencv.core.Core
import org.opencv.core.Scalar
import org.opencv.video.BackgroundSubtractor
import org.opencv.video.BackgroundSubtractorMOG2
import org.opencv.video.Video
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    //lateinit var select: Button
    //lateinit var cameraButton: Button
    lateinit var bitmap: Bitmap
    lateinit var imageView: ImageView
    lateinit var imageViewFirst: ImageView
    lateinit var imageViewSecond: ImageView
    var SELECT_CODE = 100
    var CAMERA_SERVICE = 120
    lateinit var mat: Mat
    var camera: Camera? = null
    lateinit var surfaceView : SurfaceView
    var counterOfFrames = 0
    var counterOfChecks = 0
    lateinit var framesToAnalize: ArrayList<Mat>
    var counterOfSkipped = 0
    val learningFramesCount = 30


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val myToolbar = findViewById<Toolbar>(R.id.my_toolbar)
        setSupportActionBar(myToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if(OpenCVLoader.initDebug()) Log.d("LOADED","OpenCV LOADED successfully") else{
            Log.d("LOADED","OpenCV didn't loaded, error has come")
        }
        getPermission()
        //cameraButton = findViewById(R.id.camera)
        imageView = findViewById(R.id.imageView)
        imageViewFirst = findViewById(R.id.first_Mask)
        imageViewSecond = findViewById(R.id.second_Mask)
        //select = findViewById(R.id.select)
        surfaceView = findViewById(R.id.surfaceView)

//        select.setOnClickListener {
//            var intent = Intent(Intent.ACTION_GET_CONTENT)
//            intent.type = "image/*"
//            startActivityForResult(intent, SELECT_CODE)
//        }
//        cameraButton.setOnClickListener {
//            var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//            startActivityForResult(intent, CAMERA_SERVICE)
//        }
        var avg = Mat()
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            // Разрешение не предоставлено, поэтому запрашиваем его
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                Companion.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
            )
        } else {
            Toast.makeText(this,"all what need is here",Toast.LENGTH_SHORT).show()// Разрешение уже предоставлено
        }
        val mog2: BackgroundSubtractorMOG2 = Video.createBackgroundSubtractorMOG2()
        framesToAnalize = ArrayList<Mat>()
        val holder = surfaceView.holder
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                camera = Camera.open()
                camera?.setDisplayOrientation(90)
                camera?.setPreviewDisplay(holder)
                camera?.startPreview()


                val previewSize = camera?.parameters?.previewSize
                val yuv = Mat(previewSize?.height!! + previewSize.height / 2, previewSize.width, CvType.CV_8UC1)

                camera?.setPreviewCallback { data, _ ->
                    camera?.let {

                        yuv.put(0, 0, data)
                        CoroutineScope(Dispatchers.Main).launch {
                        val width = previewSize.width  // get the width from camera preview size
                        val height = previewSize.height // get the height from camera preview size
                        val matrix = Matrix()
                        matrix.postRotate(90f)
                        counterOfFrames++

                        if (counterOfFrames == 2) {
                            if(counterOfSkipped>0){
                                counterOfSkipped-=1
                            }
                            counterOfFrames = 0
                            val rgb = Mat()
                            Imgproc.cvtColor(yuv, rgb, Imgproc.COLOR_YUV2RGB_NV21, 3)
                            if (framesToAnalize.size == learningFramesCount ) {
                                if(counterOfSkipped==0) {

                                        val fgmaskCurrent = Mat()
                                        //mog2.apply(rgb, fgmaskCurrent)



                                            //val fgmaskPrevious = Mat()
//                                        mog2.apply(
//                                            i,
//                                            fgmaskPrevious
//                                        )
                                    val blackAndWhite = Mat()
                                    mog2.apply(rgb, blackAndWhite)
                                    Core.absdiff(blackAndWhite,avg, fgmaskCurrent)
                                    var whitePixelsCurrent = Core.countNonZero(fgmaskCurrent)
                                    val threshold:Double = 120.0
                                    val lowerBound = Scalar(threshold)
                                    val upperBound = Scalar(255.0)
                                    val mask = Mat()
                                    Core.inRange(fgmaskCurrent, lowerBound, upperBound, mask)
                                    whitePixelsCurrent = Core.countNonZero(mask)
                                        //val whitePixelsPrevious = Core.countNonZero(fgmaskPrevious)

                                        if (whitePixelsCurrent>(previewSize.width*previewSize.height)/4) {
                                            val bitmap =
                                                Bitmap.createBitmap(
                                                    width,
                                                    height,
                                                    Bitmap.Config.ARGB_8888
                                                )
                                            Utils.matToBitmap(rgb, bitmap)
                                            val rotatedBitmap = Bitmap.createBitmap(
                                                bitmap,
                                                0,
                                                0,
                                                bitmap.width,
                                                bitmap.height,
                                                matrix,
                                                true
                                            )
                                            imageView.setImageBitmap(rotatedBitmap)
                                            val sdf =
                                                SimpleDateFormat(
                                                    "yyyy-MM-dd HH:mm:ss",
                                                    Locale.getDefault()
                                                )
                                            val currentDateAndTime: String =
                                                sdf.format(Date()).replace(":", "_")
                                                    .replace(" ", "_") + ".png"
                                            val picturesDirectory =
                                                getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                                            val file = File(picturesDirectory, currentDateAndTime)
                                            Log.i(
                                                "place where wroted",
                                                picturesDirectory.toString()
                                            )
                                            val matToSave = Mat()
                                            Utils.bitmapToMat(rotatedBitmap, matToSave)
                                            val success = Imgcodecs.imwrite(file.absolutePath, matToSave)
                                            Log.i(
                                                "IS SUCCESSFUL WRITING IMAGE?",
                                                success.toString()
                                            )

                                            val bitmapMOG2First =
                                                Bitmap.createBitmap(
                                                    width,
                                                    height,
                                                    Bitmap.Config.ARGB_8888
                                                )
                                            Utils.matToBitmap(mask, bitmapMOG2First)
                                            val rotatedFirstBitmap = Bitmap.createBitmap(
                                                bitmapMOG2First,
                                                0,
                                                0,
                                                bitmapMOG2First.width,
                                                bitmapMOG2First.height,
                                                matrix,
                                                true
                                            )
                                            imageViewFirst.setImageBitmap(rotatedFirstBitmap)

                                            val bitmapMOG2Second =
                                                Bitmap.createBitmap(
                                                    width,
                                                    height,
                                                    Bitmap.Config.ARGB_8888
                                                )
                                            Utils.matToBitmap(avg, bitmapMOG2Second)
                                            val rotatedSecondBitmap = Bitmap.createBitmap(
                                                bitmapMOG2Second,
                                                0,
                                                0,
                                                bitmapMOG2Second.width,
                                                bitmapMOG2Second.height,
                                                matrix,
                                                true
                                            )
                                            imageViewSecond.setImageBitmap(rotatedSecondBitmap)
                                            counterOfSkipped = learningFramesCount + 30
                                            framesToAnalize.clear()
                                        }

                                }
                                if(framesToAnalize.size!=0) {
                                    framesToAnalize.removeFirst()
                                    framesToAnalize.add(rgb)
                                } else{
                                    framesToAnalize.add(rgb)
                                }


                            } else {
                                framesToAnalize.add(rgb)
                                var foregroundMask = Mat()
                                mog2.apply(rgb, foregroundMask)
                                if(framesToAnalize.size==1){
                                    mog2.apply(rgb,avg)
                                }
                                Core.addWeighted(avg, 1.0 - 1.0 / learningFramesCount, foregroundMask, 1.0 / learningFramesCount, 0.0, avg)
                            }


                        }
                    }
                    }
                }
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                // Handle surface changed
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                camera?.stopPreview()
                camera?.release()
                camera = null
            }
        })

    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            camera?.setPreviewCallback(null)
            camera?.stopPreview()
            camera?.release()
            camera = null
            val intent = Intent(this, ImageChecking::class.java)
            startActivity(intent)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun getPermission() {
        if(checkSelfPermission(android.Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 102)

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 102 && grantResults.isNotEmpty()){
            if (grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                getPermission()
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==SELECT_CODE && data!=null){
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, data.data)
            imageView.setImageBitmap(bitmap)

            mat = Mat()
            Utils.bitmapToMat(bitmap, mat)


            Imgproc.cvtColor(mat,mat, Imgproc.COLOR_RGB2GRAY)

            Utils.matToBitmap(mat, bitmap)
            imageView.setImageBitmap(bitmap)

        }
        if(requestCode==CAMERA_SERVICE && data!=null){
            bitmap = data.extras!!.get("data") as Bitmap

            imageView.setImageBitmap(bitmap)

            mat = Mat()
            Utils.bitmapToMat(bitmap, mat)

        }
    }

    companion object {
        const val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1
    }
    override fun onPause() {
        super.onPause()
        camera?.stopPreview()
        camera?.release()
        camera = null
    }


}