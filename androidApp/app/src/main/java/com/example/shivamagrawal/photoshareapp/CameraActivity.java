package com.example.shivamagrawal.photoshareapp;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;

import android.hardware.Camera;
import android.util.Base64;
import android.view.View;
import android.view.OrientationEventListener;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.view.animation.RotateAnimation;
import android.view.animation.Animation;
import android.hardware.SensorManager;

import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.ByteArrayOutputStream;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.android.volley.toolbox.StringRequest;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import com.example.shivamagrawal.photoshareapp.Objects.ResponseHandler;
import com.example.shivamagrawal.photoshareapp.Objects.Server;

import org.json.JSONObject;

@SuppressWarnings("deprecation")
// TODO: make a camera object (only one instance though)
public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private Camera mCamera;

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    private ImageView finishCamera;
    private ImageView captureImage;
    private ImageView switchCameras;

    private Context context;
    private String groupID;
    private int screenWidth;
    private int screenHeight;

    private boolean portrait = true;
    private int currentAngle = 0;
    private int currrentCamID = Camera.CameraInfo.CAMERA_FACING_BACK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        context = this;

        Bundle extras = getIntent().getExtras();
        groupID = extras.getString("groupID");

        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;

        // Camera Preview
        surfaceView = (SurfaceView) findViewById(R.id.camera_preview_surfaceview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(CameraActivity.this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // Buttons

        finishCamera = (ImageView) findViewById(R.id.finish_camera_button);
        finishCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishCamera();
            }
        });

        captureImage = (ImageView) findViewById(R.id.capture_image_button);
        captureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capture();
            }
        });

        switchCameras = (ImageView) findViewById(R.id.switch_cameras_button);
        switchCameras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCameras();
            }
        });

        // Orientation Listener
        setOrientationListener();
    }

    private void setOrientationListener() {
        new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (45 <= orientation && orientation <= 135) {
                    // LANDSCAPE
                    if (portrait) {
                        finishCamera.startAnimation(getRotateAnim(-90));
                        switchCameras.startAnimation(getRotateAnim(-90));
                        currentAngle = -90;
                    }
                    portrait = false;
                } else if(225 <= orientation && orientation <= 315) {
                    // LANDSCAPE
                    if (portrait) {
                        finishCamera.startAnimation(getRotateAnim(-90));
                        switchCameras.startAnimation(getRotateAnim(90));
                        currentAngle = 90;
                    }
                    portrait = false;
                } else {
                    // PORTRAIT
                    if (!portrait) {
                        finishCamera.startAnimation(getRotateAnim(0));
                        switchCameras.startAnimation(getRotateAnim(0));
                        currentAngle = 0;
                    }
                    portrait = true;
                }
            }
        }.enable();
    }

    private void openCamera() {
        // Camera should be null
        try {
            mCamera = Camera.open(currrentCamID);

            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
            mCamera.setDisplayOrientation(90);

            Camera.Parameters parameters = mCamera.getParameters();

            int width = surfaceView.getWidth();
            int height = surfaceView.getHeight();
            Camera.Size size = getOptimalPreviewSize(
                    mCamera.getParameters().getSupportedPreviewSizes(), width, height);
            parameters.setPictureSize(size.width, size.height);

            if (parameters.getFocusMode().equals("auto")) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                mCamera.setParameters(parameters);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void finishCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        finish();
    }

    private void switchCameras() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        if (currrentCamID == Camera.CameraInfo.CAMERA_FACING_BACK)
            currrentCamID = Camera.CameraInfo.CAMERA_FACING_FRONT;
        else currrentCamID = Camera.CameraInfo.CAMERA_FACING_BACK;
        openCamera();
    }


    private void capture() {
        mCamera.takePicture(null, null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                if (data != null)
                    uploadImage(new UploadPhotoParams(data, currentAngle, currrentCamID, portrait));
                mCamera.startPreview();
            }
        });
    }

    private class UploadPhotoParams {
        byte[] data;
        int cAngle;
        int currentCam;
        boolean portrait;
        public UploadPhotoParams(byte[] data, int cAngle, int currentCam, boolean portrait) {
            this.data = data;
            this.cAngle = cAngle;
            this.currentCam = currentCam;
            this.portrait = portrait;
        }
    }

    private void uploadToServer(String encodedImage) {
        Map<String, String> postParameters = new HashMap<String, String>();
        postParameters.put("image", encodedImage);
        postParameters.put("groupID", groupID);
        postParameters.put("capturedAt", Long.toString(System.currentTimeMillis()));
        postParameters.put("token", Server.getToken(context));
        // TODO: put rotate parameters
        StringRequest sr = Server.POST(postParameters, Server.uploadPhotoURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        JSONObject body = new ResponseHandler(context, res).parseRes();
                        if (body == null) ResponseHandler
                                .errorToast(context, "An error occured while uploading photo");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ResponseHandler
                                .errorToast(context, "An error occured while uploading photo");
                    }
                }
        );
        Server.makeRequest(context, sr);
    }

    private void uploadImage(final UploadPhotoParams params) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] data = params.data;

                Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
                Bitmap scaled = Bitmap.createScaledBitmap(bm, screenHeight, screenWidth, true);
                int w = scaled.getWidth();
                int h = scaled.getHeight();
                Matrix mtx = new Matrix();
                if (portrait) {
                    if (params.currentCam == Camera.CameraInfo.CAMERA_FACING_BACK)
                        mtx.postRotate(90);
                    else if (params.currentCam == Camera.CameraInfo.CAMERA_FACING_FRONT)
                        mtx.postRotate(-90);
                } else {
                    if (params.cAngle == -90)
                        mtx.postRotate(180);
                }
                bm = Bitmap.createBitmap(scaled, 0, 0, w, h, mtx, true);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();

                String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                uploadToServer(encodedImage);
            }
        }).start();
    }

    private RotateAnimation getRotateAnim(int change) {
        RotateAnimation rotate = new RotateAnimation(currentAngle, change,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(500);
        rotate.setFillAfter(true);
        return rotate;
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {}

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mCamera == null) { openCamera(); }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Make Full Screen
        findViewById(R.id.camera_container_layout).setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private float mDist;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Camera.Parameters params = mCamera.getParameters();
        int action = event.getAction();

        if (event.getPointerCount() > 1) {
            if (action == MotionEvent.ACTION_POINTER_DOWN) {
                mDist = getFingerSpacing(event);
            } else if (action == MotionEvent.ACTION_MOVE && params.isZoomSupported()) {
                handleZoom(event, params);
            }
        }
        return true;
    }

    private void handleZoom(MotionEvent event, Camera.Parameters params) {
        int maxZoom = params.getMaxZoom();
        int zoom = params.getZoom();
        float newDist = getFingerSpacing(event);
        if (newDist > mDist) {
            //zoom in
            if (zoom < maxZoom)
                zoom++;
        } else if (newDist < mDist) {
            //zoom out
            if (zoom > 1)
                zoom = zoom - 2;
        }
        mDist = newDist;
        params.setZoom(zoom);
        mCamera.setParameters(params);
    }

    /** Determine the space between the first two fingers */
    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        double sqrt = Math.sqrt(x*x + y*y);
        return (float) sqrt;
    }
}