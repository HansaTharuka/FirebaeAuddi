package com.example.hummer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity  extends AppCompatActivity {

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String mFileName = null;
    private MediaRecorder recorder = null;
    private Button mRecoButton =null;
    private TextView recordLabel;
    private TextView responcetextview;

    private StorageReference mStorage;
    private ProgressDialog mProgressDialog;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    public MainActivity() {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }



    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        recorder.setOutputFile(mFileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        recorder.start();
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;

        uploadAudio();
    }

    private void uploadAudio() {

        mProgressDialog.setMessage("Uploading Audio ...");
        mProgressDialog.show();
        final StorageReference filepath = mStorage.child("Audio").child("new_audio.mp3");
        final Uri uri =Uri.fromFile(new File(mFileName));
        filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mProgressDialog.dismiss();
                recordLabel.setText("Uploading Finished..");
                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d("Download", "onSuccess: uri= "+ uri.toString());
                        APIService mAPIService= ApiUtils.getAPIService();

                        mAPIService.savePost(uri.toString(), "timecode,apple_music,deezer,spotify", "398e44c03aeda3538489f0b121c7d93c").enqueue(new Callback<JsonObject>() {
                            @Override
                            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                                if(response.isSuccessful()) {
                                    //showResponse(response.body().toString());
                                    Log.i("Result", "post submitted to API." + response.body().toString());
                                    responcetextview.setText(response.body().toString());
                                }
                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {
                                Log.e("Error", "Unable to submit post to API.");
                            }
                        });
                    }
                });


//                Uri downUri = taskSnapshot.getUploadSessionUri();
//
//                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
//                StorageReference dateRef = storageRef.child("Audio").child("new_audio.mp3");
//                Toast.makeText(MainActivity.this, ""+dateRef.toString(), Toast.LENGTH_SHORT).show();
//                Log.d("Download Url", "onComplete: Url: "+ dateRef.toString());






//                JSONObject postData = new JSONObject();
//                try {
//                    postData.put("url", "https://audd.tech/example1.mp3");
//                    postData.put("return", "timecode,apple_music,deezer,spotify");
//                    postData.put("api_token", "398e44c03aeda3538489f0b121c7d93c");
//
//                    new SendDeviceDetails().execute("https://api.audd.io/", postData.toString());
//                    Log.d("malli",postData.toString());
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }

            }
        });

    }


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_main);

        mStorage = FirebaseStorage.getInstance().getReference();

        mRecoButton =(Button)findViewById(R.id.recordBtn);
        recordLabel =(TextView)findViewById(R.id.recordLabel);
        responcetextview =(TextView)findViewById(R.id.textViewResponse);
        responcetextview.setMovementMethod(new ScrollingMovementMethod());

        mProgressDialog = new ProgressDialog(this);
        // Record to the external cache directory for visibility
        mFileName = getExternalCacheDir().getAbsolutePath();
        mFileName += "/audiorecordtest.mp3";

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        mRecoButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    startRecording();
                    recordLabel.setText("Start Recording ...");
                }else if(event.getAction()==MotionEvent.ACTION_UP){
                    stopRecording();
                    recordLabel.setText("Stopped Recording ...");
                }

                return false;
            }
        });

    }

}