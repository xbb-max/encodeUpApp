package com.yunting.encode;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import cn.yunting.utils.EncodeUpServer;

public class MainActivity extends AppCompatActivity {

    public boolean ret = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();
        final Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ret) {
                    SRVStartService(v.getContext());
                    button.setText("停止编码上传");
                    ret = true;
                }else
                {
                    ret = false;
                    SRVStopService(v.getContext());
                }
            }
        });
    }
    private String[] permissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,

    };
    private void requestPermissions() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                ) {
            ActivityCompat.requestPermissions(this, permissions, 10086);
        }else
        {

        }
    }
    public static void startPlayService(Context context, Intent i) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(i);
        } else {
            context.startService(i);
        }
    }
    public static void SRVStartService(Context context) {
        Intent i = new Intent(context, EncodeUpServer.class);
        i.setAction(EncodeUpServer.ACTION_RUN);
        startPlayService(context, i);
    }
    public static void SRVStopService(Context context) {
        Intent i = new Intent(context, EncodeUpServer.class);
        i.setAction(EncodeUpServer.ACTION_STOP);
        startPlayService(context, i);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10086) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    String title = "云听检测到您拒绝了部分权限，请到系统设置中允许开通以下权限：";
                    String content = "- 存储权限\n";
                    Toast toast=Toast.makeText(MainActivity.this,
                            title+content, Toast.LENGTH_SHORT    );
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }
            }
        }
    }

}
