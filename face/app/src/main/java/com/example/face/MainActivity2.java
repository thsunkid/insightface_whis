package com.example.face;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;


import java.io.IOException;
import java.util.List;

public class MainActivity2 extends AppCompatActivity implements SurfaceHolder.Callback {
    Camera camera2;
    SurfaceView surfaceView2;
    SurfaceHolder surfaceHolder2;
    Camera.PreviewCallback frame2;
    ImageView imgava;
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        surfaceView2 = findViewById(R.id.frame2);
        surfaceHolder2 = surfaceView2.getHolder();
        surfaceHolder2.addCallback((SurfaceHolder.Callback) this);
        surfaceHolder2.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        btn = findViewById(R.id.buttonsingup);

//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                openAct2();
//            }
//        });
    }


    public void openAct2()
    {
        Intent intent = new Intent(this,MainActivity3.class);
        startActivity(intent);

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int w, int h) {
        if (camera2 == null) {
            return;
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        camera2.stopPreview();
        camera2.release();

    }

    @Override
    public void surfaceCreated(SurfaceHolder hoder) {
        try {
            camera2 = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        } catch (RuntimeException e) {
            System.err.println(e);
            return;
        }
        Camera.Parameters params = camera2.getParameters();
        int width = params.getPreviewSize().width;
        int height = params.getPreviewSize().height;
        System.out.println(width);
        System.out.println(height);
        params.set("orientation", "portrait");
        camera2.setDisplayOrientation(90);
        params.setRotation(90);
        params.setPreviewSize(320, 240);


        camera2.setParameters(params);

        camera2.setPreviewCallback(frame2);
        List<Camera.Size> sizes = params.getSupportedPictureSizes();
        Camera.Size msize = null;

        for (Camera.Size size : sizes) {
            msize = size;
        }

        params.setPictureSize(msize.width, msize.height);
        //txtnd.setText(Integer.toString(msize.height) + " " + Integer.toString(msize.height));


        camera2.setParameters(params);

        try {
            camera2.setPreviewDisplay(surfaceHolder2);
            camera2.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
        }
    };
}
