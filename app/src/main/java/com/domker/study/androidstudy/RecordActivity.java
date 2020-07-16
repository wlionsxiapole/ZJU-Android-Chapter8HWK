package com.domker.study.androidstudy;



import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;


import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class RecordActivity extends AppCompatActivity implements SurfaceHolder.Callback{

        private final static int REQUEST_CAMERA = 123;
        private Camera mCamera;
    private SurfaceHolder mHolder;
        private ImageView mImageView;
        private VideoView mVideoView;
        private Button mBtn_p;
        private Button mBtn_v;
        private MediaRecorder mMediaRecorder;
        private String mp4Path;
        private boolean isRecording;

        private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.i("Photo: ", "take!");
                FileOutputStream fos = null;
                String filePath = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator + "1.jpg";
                File file = new File(filePath);
                try {
                    fos = new FileOutputStream(file);
                    fos.write(data);
                    fos.flush();
                    Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                    // Bitmap rotateBitmap = PathUtils.rotateImage(bitmap, filePath);
                    mImageView.setVisibility(View.VISIBLE);
                    mVideoView.setVisibility(View.GONE);
                    mImageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    mCamera.startPreview();
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };

        @SuppressLint("WrongViewCast")
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_record);

            mBtn_p = findViewById(R.id.take_pho);
            mBtn_v = findViewById(R.id.take_vid);
            mImageView = findViewById(R.id.img);
            mVideoView = findViewById(R.id.vid);
            SurfaceView mSurfaceView = findViewById(R.id.mSur);
            mHolder = mSurfaceView.getHolder();
            mHolder.addCallback(this);
            isRecording = false;

            initCamara();
            mBtn_p.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCamera.takePicture(null, null, mPictureCallback);
                }
            });
            mBtn_v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    record(v);
                }
            });
        }

        private void initCamara() {
            mCamera = Camera.open();
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPictureFormat(ImageFormat.JPEG);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            parameters.set("orientation", "portrait");
            parameters.set("rotation", 90);
            mCamera.setParameters(parameters);
            mCamera.setDisplayOrientation(90);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            if (holder.getSurface() == null) {
                return;
            }
            mCamera.stopPreview();
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

        protected void onResume() {
            super.onResume();
            if (mCamera == null) {
                initCamara();
            }
            mCamera.startPreview();
        }

        protected void onPause() {
            super.onPause();
            mCamera.stopPreview();
        }

        private boolean prepareVideoRecorder() {
            mMediaRecorder = new MediaRecorder();
            mCamera.unlock();
            mMediaRecorder.setCamera(mCamera);
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

            mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
            mp4Path = getOutputMediaPath();
            mMediaRecorder.setOutputFile(mp4Path);
            mMediaRecorder.setPreviewDisplay(mHolder.getSurface());
            mMediaRecorder.setOrientationHint(90);
            try {
                mMediaRecorder.prepare();
            } catch (Exception e) {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                mMediaRecorder.release();
                return false;
            }
            return true;
        }

        public void record(View view) {
            if (isRecording) {
                mBtn_v.setText("Start");
                mMediaRecorder.setOnErrorListener(null);
                mMediaRecorder.setOnInfoListener(null);
                mMediaRecorder.setPreviewDisplay(null);
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                mMediaRecorder.release();
                mMediaRecorder = null;
                mCamera.lock();

                mVideoView.setVisibility(View.VISIBLE);
                mImageView.setVisibility(View.GONE);
                mVideoView.setVideoPath(mp4Path);
                mVideoView.start();
            } else {
                if (prepareVideoRecorder()) {
                    mBtn_v.setText("Stop");
                    mMediaRecorder.start();
                }
            }
            isRecording = !isRecording;
        }

        private  String getOutputMediaPath() {
            File mediaStorageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("findone").format(new Date());
            File mediaFile = new File(mediaStorageDir, "VID_" + timeStamp + ".mp4");
            if(!mediaFile.exists()) {
                mediaFile.getParentFile().mkdirs();
            }
            return mediaFile.getAbsolutePath();
        }
    }