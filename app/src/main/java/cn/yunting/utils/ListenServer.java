package cn.yunting.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.yunting.encode.MainActivity;
import com.yunting.encode.R;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static cn.yunting.utils.NetUtils.getBytesFromStream;


public class ListenServer extends Service {

    @Override
    public void onCreate() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationManager notificationManager = null;
            NotificationChannel notificationChannel = null;
            if (null == notificationManager) {
                notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            }
            if (notificationChannel == null) {
                notificationChannel = new NotificationChannel("listen",
                        "listenEncode", NotificationManager.IMPORTANCE_HIGH);

//                notificationChannel.setSound(null, null);
//                notificationChannel.enableLights(false);
//                notificationChannel.enableVibration(false);
                notificationManager.createNotificationChannel(notificationChannel);
            }
            // 这里 针对8.0 我们如下设置 channel
            Notification notification = new Notification.Builder(this,"listen")
                    // 小图标
                    .setSmallIcon(R.mipmap.ic_launcher)
                    // 标题文本
                    .setContentTitle("监听编码应用")
                    // 解释内容 - 子文本
                    .setContentText("监听编码")
                    .setAutoCancel(false)
                    .build();
//            Notification notification = new Notification.Builder(getApplicationContext(), "encodeservice").build();
            startForeground(150, notification);
//            notificationManager.notify(10, notification);


        }
        if (wakeLock == null) {
            PowerManager pm = (PowerManager) getApplicationContext()
                    .getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "listenService");
            wakeLock.setReferenceCounted(false);
            wakeLock.acquire();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(broadcastReceiver, filter);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.ACTION_TIME_TICK.equals(intent.getAction())){
                boolean isServiceRunning = false;

                ActivityManager manager = (ActivityManager) getApplicationContext().getSystemService(
                                Context.ACTIVITY_SERVICE);
                //获取正在运行的服务去比较
                for (ActivityManager.RunningServiceInfo service : manager
                        .getRunningServices(Integer.MAX_VALUE)) {
                    LogUtils.x("service name ="+service.service.getClassName());
                    if ("cn.yunting.utils.EncodeUpServer"
                            .equals(service.service.getClassName()))
                    // Service的类名
                    {
                        isServiceRunning = true;
                    }
                }
                LogUtils.x("isRunning"+isServiceRunning);
                if (!isServiceRunning) {
                    LogUtils.x("isRunningOK");
                    MainActivity.SRVStartService(getApplicationContext());
                }
            }
        }
    };

    private PowerManager.WakeLock wakeLock = null;

    @SuppressLint("NewApi")
    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        stopForeground(true);
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
