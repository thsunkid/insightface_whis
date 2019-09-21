package com.example.face;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.hardware.Camera.PictureCallback;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.util.Base64;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Bitmap.CompressFormat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity3 extends AppCompatActivity implements SurfaceHolder.Callback {
    //khai báo linh ta linh tinh
    Button btnclick;
    TextView txtpos, txttime, txt1, txt2, txt3, txtname;
    Camera camera;
    Camera.PreviewCallback frame;
    SurfaceView surfaceView;
    ImageView tick;
    CircleImageView circleAvt;
    SurfaceHolder surfaceHolder;
    static Bitmap bitmapCam;
    static String strimg = "";
    static String strJson = "{}";
    static boolean sign = false;
    static String nameSign = "";
    static String jsonSend;
    static boolean ghiHinh = false;
    static String speakName = "";
    TextToSpeech textToSpeech;

    MTCNN mtcnn;

    //cái này hổng biết
    public Bitmap readFromAssets() {
        Bitmap bitmap;
        AssetManager asm = getAssets();
        try {
            InputStream is = asm.open("warriors.jpg");
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            Log.e("MainActivity", "[*]failed to open ");
            e.printStackTrace();
            return null;
        }
        return Utils.copyBitmap(bitmap); //返回mutable的image
    }

    //img thành Base64
    public String img2str(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.JPEG, 90, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    //lấy vị trí khuôn mặt to nhất trong mảng
    public int getLargestBB(Vector<Box> boxes) {
        int choose = 0;
        Rect rects[] = Utils.boxes2rects(boxes);
        for (int i = 0; i < rects.length; i++) {
            if (rects[i].height() * rects[i].width() > rects[choose].height() * rects[choose].width())
                choose = i;
        }
        return choose;
    }

    public Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public Bitmap cropBitmap(Bitmap bitmap, Rect rect) {
        Bitmap result = Bitmap.createBitmap(rect.right - rect.left, rect.bottom - rect.top, Bitmap.Config.ARGB_8888);
        new Canvas(result).drawBitmap(bitmapCam, -rect.left, -rect.top, null);
        return result;
    }

    public Bitmap resizeBitmap(Bitmap bitmap, int width, int height) {
        Bitmap result = Bitmap.createScaledBitmap(
                bitmap, width, height, false);
        return result;
    }

    //cắt bitmap thành hình vuông để lấy avartar
    public Bitmap getAvt(Bitmap bitmapCam) {
        Rect rectAvt = new Rect();
        rectAvt.bottom = bitmapCam.getHeight() - (bitmapCam.getHeight() - bitmapCam.getWidth()) / 2;
        rectAvt.top = (bitmapCam.getHeight() - bitmapCam.getWidth()) / 2;
        rectAvt.left = 0;
        rectAvt.right = bitmapCam.getWidth();
        Bitmap bmavt = cropBitmap(bitmapCam, rectAvt);
        bmavt = resizeBitmap(bmavt, 140, 140);
        return bmavt;
    }

    public void checkPermissionCamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }
    }

    final int MY_CAMERA_REQUEST_CODE = 100;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    //ẩn object
    public void hideObject() {
        txtname.setVisibility(View.INVISIBLE);
        txtpos.setVisibility(View.INVISIBLE);
        txttime.setVisibility(View.INVISIBLE);
        circleAvt.setVisibility(View.INVISIBLE);
        txt1.setVisibility(View.INVISIBLE);
        txt2.setVisibility(View.INVISIBLE);
        txt3.setVisibility(View.INVISIBLE);
        tick.setVisibility(View.INVISIBLE);
    }

    //hiện object
    public void showObject() {
        txtname.setVisibility(View.VISIBLE);
        txtpos.setVisibility(View.VISIBLE);
        txttime.setVisibility(View.VISIBLE);
        circleAvt.setVisibility(View.VISIBLE);
        txt1.setVisibility(View.VISIBLE);
        txt2.setVisibility(View.VISIBLE);
        txt3.setVisibility(View.VISIBLE);
        tick.setVisibility(View.VISIBLE);
    }

    //text thành giọng nói
    private void speakWords(final String speech) {
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int ttsLang = textToSpeech.setLanguage(new Locale("vi"));
                    textToSpeech.speak(speech, TextToSpeech.QUEUE_FLUSH, null);

                    if (ttsLang == TextToSpeech.LANG_MISSING_DATA
                            || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "The Language is not supported!");
                    } else {
                        Log.i("TTS", "Language Supported.");
                    }
                    Log.i("TTS", "Initialization success.");
                } else {
                    Toast.makeText(getApplicationContext(), "TTS Initialization failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //vẽ thông tin lên màn hình
    public void draw(JSONObject jsonObj) throws JSONException {
        String avatar = jsonObj.getString("anhDaiDien");
        if (avatar.length() == 0) {
        } else {
            byte[] b = Base64.decode(avatar, 1);
            BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap mBitmap = BitmapFactory.decodeByteArray(b, 0, b.length, options);
            txtname.setText(jsonObj.getString("name"));
            txtpos.setText(jsonObj.getString("viTri"));
            txttime.setText(jsonObj.getString("time"));
            circleAvt.setImageBitmap(mBitmap);
            showObject();
            if (jsonObj.getString("name").compareTo(speakName) != 0) {

                speakWords("Xin chào" + jsonObj.getString("name"));
                speakName = jsonObj.getString("name");
            }
        }
    }

    //kiểm tra có ai cần lấy hình không
    public void checkSign() throws JSONException {
        if (nameSign.length() != 0) {
            btnclick.setVisibility(View.VISIBLE);
        } else {
            btnclick.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        //yêu cầu cammera
//        checkPermissionCamera();

        //khai báo đối tượng
        btnclick = (Button) findViewById(R.id.buttonsingup);
        surfaceView = (SurfaceView) findViewById(R.id.frame);
        circleAvt = (CircleImageView) findViewById(R.id.imgView);
        tick = (ImageView) findViewById(R.id.imgtick);
        surfaceView.setMinimumHeight(surfaceView.getWidth());
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback((SurfaceHolder.Callback) this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mtcnn = new MTCNN(getAssets());

        txtname = (TextView) findViewById(R.id.txtname);
        txtpos = (TextView) findViewById(R.id.txtpos);
        txttime = (TextView) findViewById(R.id.txttime);
        txt1 = (TextView) findViewById(R.id.txt1);
        txt2 = (TextView) findViewById(R.id.txt2);
        txt3 = (TextView) findViewById(R.id.txt3);

//        hideObject();
//        btnclick.setVisibility(View.INVISIBLE);

        //phần này chỉ lấy hình vô bitmapCam
        frame = new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                Camera.Parameters parameters = camera.getParameters();
                int width = parameters.getPreviewSize().width;
                int height = parameters.getPreviewSize().height;
                YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(), width, height, null);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                yuv.compressToJpeg(new Rect(0, 0, width, height), 100, out);
                byte[] bytes = out.toByteArray();
                final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                bitmapCam = bitmap;
            }
        };

        //Vòng lặp vô hạn gửi đi khuôn mặt
        final Handler handlerText = new Handler();
        final Runnable runnableText = new Runnable() {
            public void run() {
                try {
                    // xoay lại để phù hợp với moblie
                    bitmapCam = rotateBitmap(bitmapCam, -90);

                    //MTCNN
                    Vector<Box> boxes = mtcnn.detectFaces(bitmapCam, 112);

                    //nếu có khuôn mặt thì bật màn hình lên
                    if (boxes.size() > 0) {
                        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                                | PowerManager.ACQUIRE_CAUSES_WAKEUP
                                | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
                        wakeLock.acquire();
                        wakeLock.release();
                        if (BuildConfig.DEBUG) {
                            // These flags cause the device screen to turn on (and bypass screen guard if possible) when launching.
                            // This makes it easy for developers to test the app launch without needing to turn on the device
                            // each time and without needing to enable the "Stay awake" option.
                            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                                    | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                        }
                    }

                    //lấy khuôn mặt lớn nhất
                    int choose = getLargestBB(boxes);
                    Rect rects[] = Utils.boxes2rects(boxes);
                    Rect rect = rects[choose];
                    Box box = boxes.elementAt(choose);
                    Point point[] = box.landmark;

                    //tạo json landmark
                    JSONArray jsPoints = new JSONArray();
                    for (int i = 0; i < 5; i++) {
                        JSONArray temp = new JSONArray();
                        temp.put(point[i].x);
                        temp.put(point[i].y);
                        jsPoints.put(temp);
                    }

                    //tạo json boundingbox
                    JSONArray jsBB = new JSONArray();
                    for (int i = 0; i < box.box.length; i++) jsBB.put(box.box[i]);

                    strimg = img2str(bitmapCam);

                    // 3. build jsonObject
                    JSONObject jsonObject = new JSONObject();
                    if (ghiHinh == false) {
                        jsonObject.accumulate("image", strimg);
                        jsonObject.accumulate("sign", false);
                        jsonObject.accumulate("anhDaiDien", "");
                        jsonObject.accumulate("points", jsPoints);
                        jsonObject.accumulate("boundingbox", jsBB);
                        jsonSend = jsonObject.toString();
                        new HttpAsyncTask().execute("http://ai.whis.tech:6969/diemdanh");
                        JSONObject jsonObj = new JSONObject(strJson);
                        nameSign = jsonObj.getString("nameSign");

                        draw(jsonObj);
                    } else {
                        circleAvt.setVisibility(View.VISIBLE);
                        circleAvt.setImageBitmap(getAvt(bitmapCam));
                        txtname.setText(nameSign + " cần lấy dữ liệu khuôn mặt");
                        txt2.setText("Canh ảnh hợp lí và bấm Chụp ");
                        txtname.setVisibility(View.VISIBLE);
                        txt2.setVisibility(View.VISIBLE);

                        jsonObject.accumulate("image", strimg);
                        jsonObject.accumulate("sign", sign);
                        jsonObject.accumulate("anhDaiDien", img2str(getAvt(bitmapCam)));
                        jsonObject.accumulate("points", jsPoints);
                        jsonObject.accumulate("boundingbox", jsBB);
                        jsonSend = jsonObject.toString();
                        new HttpAsyncTask().execute("http://ai.whis.tech:6969/diemdanh");

                        JSONObject jsonObj = new JSONObject(strJson);
                        nameSign = jsonObj.getString("nameSign");

                        if (nameSign.length() == 0) {
                            Toast.makeText(getApplicationContext(), "Lấy khuôn mặt thành công!",
                                    Toast.LENGTH_LONG).show();
                            btnclick.setText("Đăng ký mới");
                            txt2.setText("Chức Vụ:");
                            ghiHinh = false;
                            sign = false;
                        }

                    }
//                    checkSign();


                } catch (Exception e) {
                }
                handlerText.post(this);
            }
        };
        handlerText.post(runnableText);

        btnclick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.stopPreview();
                openAct2();
            }
        });
    }
    public void openAct2()
    {
        Intent intent = new Intent(this,MainActivity2.class);
        startActivity(intent);

    }

    public static String POST(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);

            String json = "";

            // 3. build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("image",strimg);
            jsonObject.accumulate("sign",false);

            // 4. convert JSONObject to JSON to String
            json = jsonObject.toString();

            // ** Alternative way to convert Person object to JSON string usin Jackson Lib
            // ObjectMapper mapper = new ObjectMapper();
            // json = mapper.writeValueAsString(person);

            // 5. set json to StringEntity
            StringEntity se = new StringEntity(json);

            // 6. set httpPost Entity
            httpPost.setEntity(se);

            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";
            strJson = result;

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        // 11. return result
        return result;
    }


    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return POST(urls[0]);
        }

    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }


    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int w, int h) {
        if (camera == null) {
            return;
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        camera.release();

    }

    @Override
    public void surfaceCreated(SurfaceHolder hoder) {
        try {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        } catch (RuntimeException e) {
            System.err.println(e);
            return;
        }
        Camera.Parameters params = camera.getParameters();
        params.set("orientation", "portrait");
        camera.setDisplayOrientation(90);
        params.setRotation(90);
        params.setPreviewSize(320, 240);


        camera.setParameters(params);

        camera.setPreviewCallback(frame);
        List<Camera.Size> sizes = params.getSupportedPictureSizes();
        Camera.Size msize = null;

        for (Camera.Size size : sizes) {
            msize = size;
        }

        params.setPictureSize(msize.width, msize.height);
        //txtnd.setText(Integer.toString(msize.height) + " " + Integer.toString(msize.height));


        camera.setParameters(params);

        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    PictureCallback jpegCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            String a = Base64.encodeToString(data, Base64.DEFAULT);
            System.out.println(a);
            byte[] b = Base64.decode(a,1);
            BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap mBitmap = BitmapFactory.decodeByteArray(b, 0, b.length, options);

            Bitmap gray = Bitmap.createBitmap(mBitmap.getWidth(),mBitmap.getHeight(), Bitmap.Config.RGB_565);
//            imageView.setImageBitmap(gray);
        }
    };
}














