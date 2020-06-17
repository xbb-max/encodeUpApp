package cn.yunting.utils;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.yunting.encode.R;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
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


public class EncodeUpServer extends Service {
    public static final String ACTION_RUN = "start-service";
    public static final String ACTION_STOP = "stop-service";
//	private Location location;

    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */

    private PowerManager.WakeLock wakeLock = null;
    @Override
    public void onCreate() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationManager notificationManager = null;
            NotificationChannel notificationChannel = null;
            if (null == notificationManager) {
                notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            }
            if (notificationChannel == null) {
                notificationChannel = new NotificationChannel("encodeservice",
                        "yunting", NotificationManager.IMPORTANCE_HIGH);

//                notificationChannel.setSound(null, null);
//                notificationChannel.enableLights(false);
//                notificationChannel.enableVibration(false);
                notificationManager.createNotificationChannel(notificationChannel);
            }
            // 这里 针对8.0 我们如下设置 channel
            Notification notification = new Notification.Builder(this,"encodeservice")
                    // 小图标
                    .setSmallIcon(R.mipmap.ic_launcher)
                    // 标题文本
                    .setContentTitle("编码应用")
                    // 解释内容 - 子文本
                    .setContentText("编码")
                    .setAutoCancel(false)
                    .build();
//            Notification notification = new Notification.Builder(getApplicationContext(), "encodeservice").build();
            startForeground(50, notification);
//            notificationManager.notify(10, notification);


        }
        if (wakeLock == null) {
            PowerManager pm = (PowerManager) getApplicationContext()
                    .getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Service");
            wakeLock.setReferenceCounted(false);
            wakeLock.acquire();
        }
        LogUtils.x(" Encode UpServer oncreate");
        process();
    }

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
        handleStartIntent(intent);
        //startForeground(25011,new Notification());
        return super.onStartCommand(intent, flags, startId);
    }

    private void handleStartIntent(Intent intent) {
        if (intent == null) {
            return;
        }
//        String action = intent.getAction();
//        LogUtils.x("--action--"+action);
//        if (TextUtils.isEmpty(action))
//            return;
//        if (action.equals(ACTION_RUN)) {
//            process();
//        } else if (action.equals(ACTION_STOP)) {
//            isStop = true;
//        }
    }

    public static String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED);//判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir.toString();
    }

    public String getDoubleString(int i) {
        String str = "" + i;
        if (str.length() <= 1) {
            str = "0" + str;
        }
        return str;
    }

    public String getSixString(int i) {
        String str = "" + i;
        if (str.length() < 6) {
            int len = str.length();
            for (int j = 0; j < 6 - len; j++) {
                str = "0" + str;
            }
        }
        return str;
    }

    protected byte[] aac2byte(String path) {
        byte[] data = null;
        FileInputStream input = null;
        try {
            input = new FileInputStream(new File(path));
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int numBytesRead = 0;
            while ((numBytesRead = input.read(buf)) != -1) {
                output.write(buf, 0, numBytesRead);
            }
            data = output.toByteArray();
            output.close();
            input.close();
        } catch (FileNotFoundException ex1) {
            ex1.printStackTrace();
        } catch (IOException ex1) {
            ex1.printStackTrace();
        }
        return data;
    }

    protected String getAACData(String path) {
        byte[] data2 = aac2byte(path);
        try {
            BASE64Encoder encoder = new BASE64Encoder();
            String data3 = encoder.encode(data2);
            return URLEncoder.encode(URLEncoder.encode(data3, "UTF-8"),"UTF-8");
        } catch (Exception e) {
        }
        return "";
    }
    private boolean isStop = false;

    private void process() {
        new Thread() {
            public void run() {
                EncodeAAC encodeAAC = new EncodeAAC();
                int index = 0;
                while (true) {
                    if (isStop) break;
                    //1:读取索引文件数据
                    try {
                        Calendar c = Calendar.getInstance();//
                        int year = c.get(Calendar.YEAR); // 获取当前年份
                        int month = c.get(Calendar.MONTH) + 1;// 获取当前月份
                        int day = c.get(Calendar.DAY_OF_MONTH);// 获取当日期
                        String indexPath = getSDPath() + "/" + year + "-" + getDoubleString(month) + "-" +
                                getDoubleString(day) + "/list.txt";
                        LogUtils.x("mylog 开始轮询");
                        List<String> list = Txt(indexPath);
                        for (int i = 0; i < list.size(); i++) {
                            String filePath = list.get(i);
                            File file = new File(filePath);
                            if (file.exists()) {
                                int pos = filePath.lastIndexOf("/");
                                String outPutFilePath = filePath.substring(0, pos);
                                //LogUtils.x("mylog outPutFilePath =" + outPutFilePath +" inputPath="+filePath);
                                c = Calendar.getInstance();
                                int mYear = c.get(Calendar.YEAR); // 获取当前年份
                                int mMonth = c.get(Calendar.MONTH) + 1;// 获取当前月份
                                int mDay = c.get(Calendar.DAY_OF_MONTH);// 获取当日期
                                int mHour = c.get(Calendar.HOUR_OF_DAY);//时
                                int mMinute = c.get(Calendar.MINUTE);//分
                                int second = c.get(Calendar.SECOND);
                                String name = "segment" + mYear + getDoubleString(mMonth)
                                        + getDoubleString(mDay) + getDoubleString(mHour)
                                        + getDoubleString(mMinute) + getDoubleString(second) + "-"
                                        + getSixString(index);
                                outPutFilePath = outPutFilePath + "/" + name + ".aac";
                                LogUtils.x("mylog outPutFilePath2 =" + outPutFilePath);
                                //2:编码，删除pcm
                                int ret = encodeAAC.encodeAAc(filePath.getBytes(), filePath.length(), outPutFilePath.getBytes(), outPutFilePath.length());
                                if (ret > 0) {
                                    index++;
                                    file.delete();
                                    String data = getAACData(outPutFilePath);
                                    upData2Server("http://47.105.150.189/aac/ServiceCenter.do?action=uploadAcc",name,data,outPutFilePath);
                                }
                            }
                        }
//                        android.os.Process.killProcess(android.os.Process.myPid());
                    }catch (Exception e){}
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                    }

                    //3:上传文件片，删除文件片
                }
            }
        }.start();
    }

    private void upData2Server(String url, String fileName, String data,String filePath) {
        HttpURLConnection con = null;
        OutputStream os = null;
        DataOutputStream dos = null;
        try {
            StringBuffer sb = new StringBuffer();
            sb.append(url);
            URL dataUrl = new URL(sb.toString());
            con = (HttpURLConnection) dataUrl.openConnection();
            con.setUseCaches(false);
            StringBuffer commparam = new StringBuffer();
            commparam.append("name="+fileName+"&data="+data);

            LogUtils.x("-----" + "sb.toString() " + sb.toString() + "?" + commparam.toString());
//						LogUtils.writeLogtoFile(sb.toString() + "?" + commparam.toString());
            // commparam.append(InputParam);

            con.setRequestMethod("POST");
            con.setRequestProperty("Proxy-Connection", "Keep-Alive");

            con.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setConnectTimeout(10 * 1000);
            con.setReadTimeout(10 * 1000);
            os = con.getOutputStream();
            dos = new DataOutputStream(os);
            dos.write(commparam.toString().getBytes());

            int res = con.getResponseCode();
            LogUtils.x("----协议交互结果 res code ---- " + res);
            // 由于服务器返回的content-length并不准确，忽略此参数，以实际收到为准。
            int len = -1;

            if ((res == 200)) {
                InputStream is = con.getInputStream();
                byte[] pResultBuf = getBytesFromStream(is, len);
                String result = new String(pResultBuf);
                LogUtils.x("----协议交互结果 result ---- " + result);
                File file = new File(filePath);
                if (file.exists())
                {
                    file.delete();
                }
            }
        } catch (Exception e) {
        }finally {
            if (dos != null)
                try {
                    dos.close();
                } catch (IOException e) {
                }
            if (os != null)
                try {
                    os.close();
                } catch (IOException e) {
                }
            if (con != null)
                con.disconnect();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public List<String> Txt(String filePath) {
        //将读出来的一行行数据使用List存储
        List newList = new ArrayList<String>();
        try {
            File file = new File(filePath);
            int count = 0;//初始化 key值
            if (file.isFile() && file.exists()) {//文件存在
                InputStreamReader isr = new InputStreamReader(new FileInputStream(file));
                BufferedReader br = new BufferedReader(isr);
                String lineTxt = null;
                while ((lineTxt = br.readLine()) != null) {
                    if (!"".equals(lineTxt)) {
                        // String reds = lineTxt.split("\\+")[0];  //java 正则表达式
                        newList.add(count, lineTxt);
                        count++;
                    }
                }
                isr.close();
                br.close();
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newList;
    }
}
