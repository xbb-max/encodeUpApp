package cn.yunting.utils;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by beibei on 2016/3/2.
 */
public class LogUtils {
    public static boolean LOG_ON = true;

    public static void x(String msg) {
            Log.d("mylog", msg);
    }
    public static String MYLOGFILEName = "_Count_Log.txt";// 本类输出的日志文件名称
    public static SimpleDateFormat myLogSdf = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss:SSS");// 日志的输出格式
    public static SimpleDateFormat logfile = new SimpleDateFormat("yyyy-MM-dd");// 日志文件格式
    private static String MYLOG_PATH_SDCARD_DIR = "";

    public static void writeLogtoFile(String text) {// 新建或打开日志文件

        Date nowtime = new Date();
        String needWriteFiel = logfile.format(nowtime);
        String needWriteMessage = text;// myLogSdf.format(nowtime) +" : "+ "  "
        // + text;
        MYLOG_PATH_SDCARD_DIR = Environment.getExternalStorageDirectory()
                 + "/";// context.getFilesDir().getPath()+"/";
        File file = new File(MYLOG_PATH_SDCARD_DIR, needWriteFiel
                + MYLOGFILEName);
        try {
            FileWriter filerWriter = new FileWriter(file, true);// 后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
            BufferedWriter bufWriter = new BufferedWriter(filerWriter);

            String month;
            String day;
            String hour;
            String min;
            Calendar c = Calendar.getInstance();
            month = Integer.toString(c.get(Calendar.MONTH) + 1);
            day = Integer.toString(c.get(Calendar.DAY_OF_MONTH));
            hour = Integer.toString(c.get(Calendar.HOUR_OF_DAY));
            min = Integer.toString(c.get(Calendar.MINUTE));
            String sec = Integer.toString(c.get(Calendar.SECOND));
            bufWriter.write(month + ":" + day + ":" + hour + ":" + min + ":"
                    + sec + " " + needWriteMessage);
            bufWriter.newLine();
            bufWriter.close();
            filerWriter.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
