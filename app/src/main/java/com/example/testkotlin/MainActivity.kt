package com.example.testkotlin

import android.hardware.Camera
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

public class MainActivity : AppCompatActivity() {

    private var camera: Camera? = null
    private var showCamera: ShowCamera? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var counter = 0;


        button.setOnClickListener(View.OnClickListener {

            camera = Camera.open()

            showCamera = ShowCamera(this, camera!!, imageView2)

            frameLayout.addView(showCamera)

        })
    }
}
