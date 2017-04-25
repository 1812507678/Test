package com.amsu.test.wifiTramit;

import android.os.Environment;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by HP on 2017/4/12.
 */

public class DeviceOffLineFileUtil {
    public final static String HOST_SPOT_SSID = "AmsuCharge";
    public final static String HOST_SPOT_PASS_WORD = "0123456789";

    public final static String readDeviceVersion = "FF0100060616";
    public final static String readDeviceID = "FF0200060716";
    public final static String readDeviceFileList = "FF0300060816";
    public final static String generateDeviceFile = "FF0800060D16";
    private static final String TAG = "TestActivity";

    //3.4	查询某个数据文件长度
    public final static String readDeviceSpecialFileLength(String fileName){
        int allPackageLength = fileName.getBytes().length+6;
        String toHexString = Integer.toHexString(allPackageLength);//转化为16进制字符串
        String hexMiddleLenghtByte = "";
        if (toHexString.length()==1){
            hexMiddleLenghtByte = "000"+toHexString;
        }
        else if (toHexString.length()==2){
            hexMiddleLenghtByte = "00"+toHexString;
        }
        else if (toHexString.length()==3){
            hexMiddleLenghtByte = "0"+toHexString;
        }
        else {
            hexMiddleLenghtByte = toHexString;
        }
        String fileLength = Integer.toHexString(fileName.getBytes().length);
        String hexAddLenghtByte = "";
        if (fileLength.length()==1){

        }
        else {

        }
        byte[] bytes = hexStringToBytes(hexMiddleLenghtByte);


        //0XFF+0x04+bytes[0]+bytes[1]

        String hex = "FF040018"+stringToHexString(fileName)+"cd16";

        return hex;
    }

    //计算累加和，fileName为上传的文件名如 "20170412102300.ecg"
    static String readDeviceSpecialFileBeforeAddSum(String startOrder, String fileName){
        int sumFile =0;
        for (int i = 0; i < fileName.length(); i++) {
            int ch = (int) fileName.charAt(i);
            String string = Integer.toHexString(ch);
            int parseInt = Integer.parseInt(string,16);
            sumFile += parseInt;
        }
        String[] split2 = startOrder.split(" ");
        int sumStart = 0;
        for (int i = 0; i < split2.length; i++) {
            int parseInt = Integer.parseInt(split2[i], 16);
            sumStart += parseInt;
        }
        String  sumString = Integer.toHexString(sumFile+sumStart);
        String lastString = sumString.substring(sumString.length()-2, sumString.length());
        return lastString;

    }

    //计算累加和，startOrder为前面的命令
    static String readDeviceSpecialFileBeforeAddSum(String startOrder){
        String splitString= "";
        for (int i = 0; i < startOrder.length(); i++) {
            splitString += startOrder.charAt(i);
            if ((i+1)%2==0) {
                splitString += " ";
            }
        }

        String[] split = splitString.split(" ");
        int sum = 0;
        for (int i = 0; i < split.length; i++) {
            int parseInt = Integer.parseInt(split[i], 16);
            sum += parseInt;
        }
        String  sumString = Integer.toHexString(sum);
        String lastString = sumString.substring(sumString.length()-2, sumString.length());
        return lastString;
    }

    //将字节数组转换为16进制字符串
    public static String binaryToHexString(byte[] bytes,int length) {
        String hexStr = "0123456789ABCDEF";
        String result = "";
        String hex = "";
        for (int i=0;i<length;i++) {
            byte b = bytes[i];
            hex = String.valueOf(hexStr.charAt((b & 0xF0) >> 4));
            hex += String.valueOf(hexStr.charAt(b & 0x0F));
            result += hex + " ";
        }
        return result;
    }

    //将字节数组转换为16进制字符串
    public static String binaryToHexString(byte[] bytes,int length,String separator) {
        String hexStr = "0123456789ABCDEF";
        String result = "";
        String hex = "";
        for (int i=0;i<length;i++) {
            byte b = bytes[i];
            hex = String.valueOf(hexStr.charAt((b & 0xF0) >> 4));
            hex += String.valueOf(hexStr.charAt(b & 0x0F));
            result += hex + separator;
        }
        return result;
    }

    static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }


    /**
     * 字符串转换为16进制字符串
     *
     * @param s
     * @return
     */
    public static String stringToHexString(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            int ch = (int) s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;
    }

    /**
     * 16进制字符串转换为字符串
     *
     * @param s
     * @return
     */
    public static String hexStringToString(String s) {
        if (s == null || s.equals("")) {
            return null;
        }
        s = s.replace(" ", "");
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(
                        s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword, "gbk");
            new String();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }

    public static String getFormatHexFileLenght(int needHexStringLength,int intLength){
        String fileHexlength = Integer.toHexString(intLength);
        if (fileHexlength.length()<needHexStringLength) {
            int a = needHexStringLength-fileHexlength.length();
            String temp = "";
            for (int i = 0; i < a; i++) {
                temp += 0;
            }
            fileHexlength = temp+fileHexlength;
        }
        return fileHexlength;
    }

    private static boolean mIsTimeerRunning;  //是否正在运行
    private static Timer mTimer;
    private static long timeSpan = 1000;
    private static TimerTask mTimerTask;
    private static int TimeOutCountIndex;
    private static int TimeOutAllCount = 3;

    public static void setTransferTimeOverTime(final OnTimeOutListener onTimeOutListener){
        mTimerTask=new TimerTask() {
            @Override
            public void run() {
                System.out.println("run TimeOutCountIndex:"+TimeOutCountIndex);
                if (mIsTimeerRunning){
                    TimeOutCountIndex++;
                }
                if (TimeOutCountIndex==TimeOutAllCount) {
                    TimeOutCountIndex = 0;
                    onTimeOutListener.onTomeOut();//时间到
                }
            }
        };
    }

    public static void startTime(){
        if (mTimer==null){
            mTimer = new Timer();
            mTimer.schedule(mTimerTask,timeSpan,timeSpan);
        }
        mIsTimeerRunning = true;
    }

    public static void stopTime(){
        if (mIsTimeerRunning && mTimer!=null){
            mIsTimeerRunning = false;
            TimeOutCountIndex = 0;
        }
    }

    public static void destoryTime(){
        if (!mIsTimeerRunning && mTimer!=null){
            mTimer.cancel();
            mTimer = null;
            /*mTimerTask.cancel();  //将原任务从队列中移除
            mTimerTask = null;  */
        }
    }

    public interface OnTimeOutListener{
        void onTomeOut();
    }

    //写到文件里，二进制方式写入
    public static void addEcgDataToList(String hexStringData, List<Integer> mAllData){
        int count = 16; //16为分10个包上传
        for (int i=0;i<count;i++){
            int start = 12+i*(512+14);
            List<Integer> integers = ECGUtil.geIntEcgaArrList(hexStringData, " ", start, 512);
            mAllData.addAll(integers);
        }
    }

    //写到文件里，二进制方式写入
    public static void addRemainderEcgDataToList(String hexStringData,int onePackageReadLength,List<Integer> mAllData){
        int count = onePackageReadLength / (512 + 14);
        int countRemain = onePackageReadLength % (512 + 14);
        for (int i=0;i<count;i++){
            int start = 12+i*(512+14);
            List<Integer> integers = ECGUtil.geIntEcgaArrList(hexStringData, " ", start, 512);
            mAllData.addAll(integers);
        }
        List<Integer> integers = ECGUtil.geIntEcgaArrList(hexStringData, " ", 12+count*(512+14), countRemain);
        mAllData.addAll(integers);
    }

    //写到文件里，文本方式写入
    public static void writeEcgDataToTextFile(List<Integer> mAllData){
        FileWriter a= null;
        try {
            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+ MyUtil.getECGFileNameDependFormatTime(new Date())+".ecg";
            a = new FileWriter(filePath);
            for(int i:mAllData){
                a.write(i+" ");
            }
            Log.i(TAG,"写入文件成功");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
