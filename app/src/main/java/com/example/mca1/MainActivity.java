package com.example.mca1;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.BaseColumns;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.security.AccessController.getContext;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity implements SensorEventListener {
    Button symptoms;
    Button buttonmhr;
    Button resrate;
    Button upload_signs;
    TextView textmhr;
    TextView textmrrx;
    TextView textmrry;
    TextView texttimer;
    TextView ack;
    double ax,ay,az;
    double heartrate;
    double resprate;

    private SensorManager sensorManager;
    private Sensor sensor;


    private TextureView textureView;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    private String cameraId;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private static final int REQUEST_CAMERA_PERMISSION = 1;

    public static int hrtratebpm;
    private int mCurrentRollingAverage;
    private int mLastRollingAverage;
    private int mLastLastRollingAverage;
    private long [] mTimeArray;
    private int numCaptures = 0;
    private int mNumBeats = 0;
    String[] projection = {
            BaseColumns._ID,
            AppTableContract.AppTable.COLUMN_1,
            AppTableContract.AppTable.COLUMN_2
    };
    SQLiteDatabase dbwrite;
    SQLiteDatabase dbread;
    long currowID;
    public static final String EXTRA_MESSAGE = "com.example.mca1.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appDbHelper dbHelper = new appDbHelper(getApplicationContext());
        System.out.println(getApplicationContext().getExternalFilesDir(null));
        dbwrite = dbHelper.getWritableDatabase();
        dbread = dbHelper.getReadableDatabase();

        heartrate = 0.0;
        resprate = 0.0;
        mTimeArray = new long [15];

        textmhr = (TextView) findViewById(R.id.textMHR);
        textmhr.setText("Heart Rate");
        textureView =  findViewById(R.id.texture1);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);
        buttonmhr = (Button) findViewById(R.id.buttonMHR);

        buttonmhr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hrtratebpm = 0;
                textmhr.setText(Integer.toString(hrtratebpm));
                openCamera();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        closeCamera();
                        textmhr.setText(Integer.toString(hrtratebpm));
                    }
                }, 45000);

            }
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener((SensorEventListener) this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        //heartrate = 0.0;
        //resprate = 1.0;
        ax = 1;

        Cursor cursor = dbread.query(
                AppTableContract.AppTable.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );

        List itemIds = new ArrayList<>();
        while(cursor.moveToNext()) {
            long itemId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(AppTableContract.AppTable._ID));
            itemIds.add(itemId);
        }
        System.out.println(itemIds);
        cursor.close();
        //textmhr = (TextView) findViewById(R.id.textMHR);
        //textmrrx = (TextView) findViewById(R.id.textMRR);
        textmrry = (TextView) findViewById(R.id.textMRR2);
        ack = (TextView) findViewById(R.id.Ack_msg);

        textmhr.setText(Double.toString(heartrate));
        //textmrrx.setText(Double.toString(ax));
        //textmrry.setText(Double.toString(ay));
        //textmrr.setText(Double.toString(az));


        symptoms = (Button) findViewById(R.id.buttonsymptoms);
        symptoms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadSymptoms();
            }
        });

        resrate = (Button) findViewById(R.id.buttonMRR);
        resrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {calculateRespRate();
            }
        });

        upload_signs = (Button) findViewById(R.id.buttonuploadsigns);
        upload_signs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadSigns();
            }
        });

    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            Bitmap bmp = textureView.getBitmap();
            int width = bmp.getWidth();
            int height = bmp.getHeight();
            int[] pixels = new int[height * width];

            bmp.getPixels(pixels, 0, width, width / 2, height / 2, width / 20, height / 20);
            int sum = 0;
            for (int i = 0; i < height * width; i++) {
                int red = (pixels[i] >> 16) & 0xFF;
                sum = sum + red;
            }
            // Waits 20 captures, to remove startup artifacts.  First average is the sum.
            if (numCaptures == 20) {
                mCurrentRollingAverage = sum;
            }
            // Next 18 averages needs to incorporate the sum with the correct N multiplier
            // in rolling average.
            else if (numCaptures > 20 && numCaptures < 49) {
                mCurrentRollingAverage = (mCurrentRollingAverage*(numCaptures-20) + sum)/(numCaptures-19);
            }
            // From 49 on, the rolling average incorporates the last 30 rolling averages.
            else if (numCaptures >= 49) {
                mCurrentRollingAverage = (mCurrentRollingAverage*29 + sum)/30;
                if (mLastRollingAverage > mCurrentRollingAverage && mLastRollingAverage > mLastLastRollingAverage && mNumBeats < 15) {
                    mTimeArray[mNumBeats] = System.currentTimeMillis();
                    mNumBeats++;
                    //textmhr.setText(Integer.toString(mNumBeats));
                    if (mNumBeats == 15) {
                        calcBPM();
                    }
                }
            }
            // Another capture
            numCaptures++;
            // Save previous two values
            mLastLastRollingAverage = mLastRollingAverage;
            mLastRollingAverage = mCurrentRollingAverage;
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            if (cameraDevice != null)
                cameraDevice.close();
            cameraDevice = null;
        }
    };

    private void calcBPM() {
        int med;
        long [] timedist = new long [14];
        for (int i = 0; i < 14; i++) {
            timedist[i] = mTimeArray[i+1] - mTimeArray[i];
        }
        Arrays.sort(timedist);
        med = (int) timedist[timedist.length/2];
        hrtratebpm= 60000/med;
    }

    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if (null == cameraDevice) {
                        return;
                    }
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(MainActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void updatePreview() {
        if (null == cameraDevice) {
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            ax=event.values[0];
            ay=event.values[1];
            az=event.values[2];
        }
        //textmrrx.setText(Double.toString(ax));
        //textmrry.setText(Double.toString(ay));
        //a 45 diff consec peak count 60 * no of peaks


    }

    public void uploadSymptoms() {
    Intent intent = new Intent(this, SecondActivity.class);
    intent.putExtra(EXTRA_MESSAGE, currowID);
    startActivity(intent);

}
    public void uploadSigns(){
        ContentValues values = new ContentValues();
        values.put(AppTableContract.AppTable.COLUMN_1, hrtratebpm);
        values.put(AppTableContract.AppTable.COLUMN_2, resprate);
        currowID = dbwrite.insert(AppTableContract.AppTable.TABLE_NAME, null, values);
        if(currowID != 0) {
            ack.setText("Signs Uploaded successfully");
        }
    }


    public void calculateRespRate() {
        final double[] temp = new double[45];
        //final double rr;
        //int count = 0;
        texttimer = (TextView) findViewById(R.id.Timer);
        new CountDownTimer(45000, 1000) {
            long s;
            int count = 0;
            double rr;

            public void onTick(long millisUntilFinished) {
                s = millisUntilFinished / 1000;
                texttimer.setText("seconds remaining: " + s);
                temp[45-(int)s-1] = ay;
            }

            public void onFinish() {
                texttimer.setText("done!");
                for(int i = 1 ; i < 44 ; i++){
                    if(temp[i-1] < temp[i] && temp[i+1] < temp[i]){
                        count++;
                    }
                }
                rr = (60/45) * (count);
                resprate = rr;
                textmrry.setText(Double.toString(rr));
            }
        }.start();

        //Intent intent = new Intent(this, SecondActivity.class);
        //startActivity(intent);
    }
}