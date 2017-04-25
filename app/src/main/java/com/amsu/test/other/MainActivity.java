package com.amsu.test.other;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.amsu.test.R;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.test.objects.HeartRateResult;
import com.test.utils.DiagnosisNDK;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    /*static {
        System.loadLibrary("ecg");// shangguan ecg
        // System.loadLibrary("Wu");//wubo ppg
    }*/

    // 10阶FIR滤波器，用于计算心率值所用，该滤波器更能滤除信号的毛刺，有利于计算R波峰。
    private static double fly0;
    private static double[] flx0 = new double[23];
    private double[] a = { 0.0008, 0.0025, 0.0057, 0.0109, 0.0183, 0.0280,
            0.0393, 0.0513, 0.0629, 0.0724, 0.0788, 0.0810, 0.0788, 0.0724,
            0.0629, 0.0513, 0.0393, 0.0280, 0.0183, 0.0109, 0.0057, 0.0025,
            0.0008 };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int []test = {100,40,67,89,10,100,56,65,100,100,
                100,40,67,89,10,100,56,65,100,100,
                23,40,67,89,10,100,56,65,77,100,
                100,40,67,89,10,100,56,65,100,100,
                100,40,67,89,10,100,56,65,100,100,
                100,40,67,89,10,78,56,65,100,100,
                100,40,67,55,10,100,56,65,100,100,
                100,40,67,99,10,22,56,65,100,100,
                100,40,67,89,10,44,56,65,100,100,
                100,40,67,89,10,100,56,65,100,100,
                100,33,67,77,10,89,56,65,100,100,
                100,40,67,89,10,100,56,65,100,88,
                44,67,89,10,100,56,65,100,100,56,
                100,40,67,89,10,100,56,65,100,100,
                100,40,67,89,10,100,56,65,100,100};


        HeartRateResult ecgResult = DiagnosisNDK.AnalysisEcg(test, test.length, 150);
        Log.i(TAG,"ecgResult:"+ecgResult.toString());

        //int i = countEcgRate(test, test.length, 1);
        //Log.i(TAG,"i:"+i);

    }

    int fir(int d) {
        int i;
        try {
            for (i = 0; i < 22; i++)
                flx0[22 - i] = flx0[21 - i];
            flx0[0] = d;
            fly0 = 0;
            for (i = 0; i < 10; i++)
                fly0 += a[i] * flx0[i];
        } catch (Exception e) {
            fly0 = 0;
            Log.w(TAG,"Exception:"+e.toString());
        }

        return (int) fly0;
    }

    // 计算心率
    private int countEcgRate(int[] ecg, int len, int s_rate) {
        int result;
        try {
            for (int i = 0; i < len; i++) {
                ecg[i] = fir(ecg[i]);
                Log.i(TAG,"ecg:"+i+":"+ecg[i]);
            }

            List<Integer> R = new ArrayList<Integer>();
            int[] soc = new int[len];
            int[] diff = new int[len];

            diff[0] = 0;
            diff[1] = 0;
            for (int j = 2; j < len - 2; j++) {
                diff[j] = (ecg[j - 2] - 2 * ecg[j] + ecg[j + 2]);
            }
            diff[len - 1] = 0;
            diff[len - 2] = 0;

            int num = len / s_rate;
            int[] min = new int[num];

            for (int i = 0; i < num; i++) {
                min[i] = diff[s_rate * i];
                for (int j = 0; j < s_rate; j++) {
                    if (min[i] > diff[s_rate * i + j])
                        min[i] = diff[s_rate * i + j];
                }
            }

            float[] threshold = new float[num];
            for (int j = 0; j < num; j++)
                threshold[j] = (float) ((min[j]) * 0.6);

            int n = 0;
            for (int i = 0; i < num; i++) {
                for (int j = 0; j < s_rate && (s_rate * i + j) < len - 3; j++) {
                    if (diff[s_rate * i + j] > threshold[i]
                            && diff[s_rate * i + j + 1] > threshold[i]
                            && diff[s_rate * i + j + 2] <= threshold[i]
                            && diff[s_rate * i + j + 3] <= threshold[i])
                        soc[n++] = s_rate * i + j;
                }
            }
            for (int i = 0; i < n; i++) {
                int p = soc[i];
                int res = ecg[p];
                R.add(p);
                for (int j = p - 5; j < p + 5 && p > 5 && j < len; j++) {
                    if (res < ecg[j]) {
                        res = ecg[j];
                        R.set(R.size() - 1, j);
                    }
                }
            }

            for (int j = 0; j < n - 1; j++) {
                if ((R.get(j + 1) - R.get(j)) < (s_rate / 5)) {
                    if (ecg[R.get(j + 1)] > ecg[R.get(j)]) {
                        R.remove(j);
                        n--;
                        j--;
                    } else {
                        R.remove(j + 1);
                        n--;
                    }

                } else if ((R.get(j + 1) - R.get(j)) > (s_rate * 12 / 10)) {
                    int res = diff[R.get(j) + 100];
                    int pos = R.get(j) + 100;
                    for (int t = R.get(j) + 100; t < (R.get(j + 1) - 100); t++) {
                        if (res < diff[t]) {
                            res = diff[t];
                            pos = t;
                        }
                    }
                    res = ecg[pos];
                    int p_pos = pos;
                    for (int t = pos - 5; t < pos + 5; t++) {
                        if (res < ecg[t]) {
                            res = ecg[t];
                            p_pos = t;
                        }
                    }
                    R.add(j + 1, p_pos);
                    n++;
                    j++;
                }
            }

            int RR[] = new int[n - 1];
            for (int j = 0; j < n - 1; j++) {
                RR[j] = R.get(j + 1) - R.get(j);
            }

            int size = RR.length;
            int sum = 0;
            for (int j = 0; j < size; j++) {
                sum = sum + RR[j];
            }
            int avel = sum / size;
            int rate = 60 * s_rate / avel;
            if (rate > 60 && rate < 100) {
                result = rate;
            } else {
                result = 0;
            }

        } catch (Exception e) {
            result = 0;
        }
        return result;
    }

    private void getNetWorkData(String url){
        String userId = "userid"+System.currentTimeMillis();
        String leftFile = getCacheDir()+"/left.file";
        String rightFile = getCacheDir()+"/right.file";

        Log.i(TAG,"leftFile:"+leftFile);


        AssetManager assetManager = getAssets();
        try {
            InputStream inputStream = assetManager.open("left.file.txt");
            FileOutputStream fos = new FileOutputStream(new File(leftFile));
            byte[] buffer = new byte[1024];
            int byteCount=0;
            while((byteCount=inputStream.read(buffer))!=-1) {//循环从输入流读取 buffer字节
                fos.write(buffer, 0, byteCount);//将读取的输入流写入到输出流
            }
            fos.flush();//刷新缓冲区
            inputStream.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            InputStream inputStream = assetManager.open("right.file.txt");
            FileOutputStream fos = new FileOutputStream(new File(rightFile));
            byte[] buffer = new byte[1024];
            int byteCount=0;
            while((byteCount=inputStream.read(buffer))!=-1) {//循环从输入流读取 buffer字节
                fos.write(buffer, 0, byteCount);//将读取的输入流写入到输出流
            }
            fos.flush();//刷新缓冲区
            inputStream.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        HttpUtils httpUtils = new HttpUtils();
        RequestParams params = new RequestParams();

        params.addBodyParameter("createTime",getFormTime());
        params.addBodyParameter("type","0");
        params.addBodyParameter("testeeId",userId);
        params.addBodyParameter("name","haijun");
        params.addBodyParameter("gender","1");
        params.addBodyParameter("age","0");
        params.addBodyParameter("height","165");
        params.addBodyParameter("weight","54");
        params.addBodyParameter("phone","18689463192");
        params.addBodyParameter("tag","测试人员");
        params.addBodyParameter("left.file",new File(leftFile));
        params.addBodyParameter("right.file",new File(rightFile));
        //params.addBodyParameter("left.checksum",SHA256File.getFileSHA265Message(leftFile));
        //params.addBodyParameter("right.checksum",SHA256File.getFileSHA265Message(rightFile));


        httpUtils.send(HttpRequest.HttpMethod.POST, url, params, new RequestCallBack<String>() {

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result;
                Log.i(TAG,"onSuccess===result:"+result);
            }

            @Override
            public void onFailure(HttpException e, String s) {
                Log.i(TAG,"onFailure===:"+e);
            }
        });

    }

    private static String getFormTime() {
        Date nowTime=new Date();
        System.out.println(nowTime);
        SimpleDateFormat time=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return time.format(nowTime);
    }


    public void getData(View view) {
        String url = "http://120.76.98.227:8080/gait/data/c0?access_token=c93d1665-ea36-475b-84eb-483247820320";
        //String url = "http://120.76.98.227:8080";
        getNetWorkData(url);
    }
}
