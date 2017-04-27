package com.amsu.test.wifiTramit;

import android.app.Activity;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.amsu.test.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by HP on 2017/4/26.
 */

public class DrawLineTestActivity extends BaseActivity {
    private static final String TAG = "TestActivity";
    private EcgView pv_ecg_path;
    private int mEcgGroupSize;
    private int mAllTimeAtSecond;
    private FlexibleThumbSeekbar sb_ecg_progress;
    private TextView tv_ecg_protime;
    private String mAllTimeString;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawline);

        initView();

    }

    private void initView() {
        initHeadView();
        setCenterText("绘制离线文件");
        setLeftImage(R.drawable.back_icon);
        getIv_base_leftimage().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ListView lv_filelist = (ListView) findViewById(R.id.lv_filelist);
        pv_ecg_path = (EcgView) findViewById(R.id.pv_ecg_path);
        sb_ecg_progress = (FlexibleThumbSeekbar) findViewById(R.id.sb_ecg_progress);
        tv_ecg_protime = (TextView) findViewById(R.id.tv_ecg_protime);

        final List<String> uploadFileToSP = DeviceOffLineFileUtil.getUploadFileToSP();
        Log.i(TAG,"uploadFileToSP.size()"+uploadFileToSP.size());

        List<String> fileListNeed = new ArrayList<>();
        for (String s:uploadFileToSP){
            String[] split = s.split("/");
            fileListNeed.add(split[split.length-1]);
        }
        FileListAdapter fileListAdapter = new FileListAdapter(fileListNeed,this);
        lv_filelist.setAdapter(fileListAdapter);

        lv_filelist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String filePath = uploadFileToSP.get(position);
                String[] split = filePath.split("/");
                String fileName = split[split.length - 1];
                MyUtil.showToask(DrawLineTestActivity.this,fileName);

                readFileData(filePath);
            }
        });

        //心电进度监听器
        pv_ecg_path.setOnEcgProgressChangeListener(new EcgView.OnEcgProgressChangeListener() {
            int percent;
            @Override
            public void onEcgDrawIndexChange(int countIndex) {
                //Log.i(TAG,"countIndex:"+countIndex);
                int temp = (int) ((float) countIndex / mEcgGroupSize *100);
                if (temp!=percent){
                    percent = temp;
                    final String currentTimeString = calcuEcgDataTimeAtSecond((int) ((percent / 100.f) * mAllTimeAtSecond));
                    Log.i(TAG,"percent:"+currentTimeString);
                    Log.i(TAG,"currentTimeString:"+currentTimeString);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_ecg_protime.setText(currentTimeString+"/"+ mAllTimeString);
                        }
                    });

                    //Log.i(TAG,"percent:"+percent);
                    sb_ecg_progress.setProgress(percent);

                }


            }
        });

        //设置拖动改变进度
        sb_ecg_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setPercent(seekBar.getProgress());
            }
        });
    }

    private void readFileData(final String fileName) {
        MyUtil.showDialog("读取数据",this);

        final File file = new File(fileName);
        if (file.exists()){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //List<Integer> integerList = DeviceOffLineFileUtil.readEcgDataToTextFileNew(fileName);
                    List<Integer> integerList = DeviceOffLineFileUtil.readEcgDataToBinaryFile(fileName);
                    mEcgGroupSize = integerList.size() / 10;
                    mAllTimeAtSecond = (int) (integerList.size()/(150*1f));  //计算总的时间秒数，1s为150帧，即为150个数据点
                    mAllTimeString = calcuEcgDataTimeAtSecond(mAllTimeAtSecond);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_ecg_protime.setText("0'0/"+mAllTimeString);
                        }
                    });

                    Log.i(TAG,"integerList.size(): "+integerList.size());
                    MyUtil.hideDialog();
                    startDrawLine(integerList);
                }
            }).start();

        }
        else {
            //文件不存在，没有上传
            MyUtil.showToask(this,"该文件没有上传");
        }
    }

    /*private void readFileData(final String fileName) {
        MyUtil.showDialog("读取数据",this);

        final File file = new File(fileName);
        if (file.exists()){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    List<Integer> integerList = DeviceOffLineFileUtil.readEcgDataToTextFileNew(fileName);
                    FileReader fileReader = null;
                    try {
                        fileReader = new FileReader(file);
                *//*Reader reader=new BufferedReader(fileReader);
                int temp;
                String fileStringData="";
                while ((temp=reader.read())!=-1) {
                    fileStringData+=(char)temp;
                }
                parStringDataToList(fileStringData);*//*

                        char[] cbuf = new char[1024*1024];

                        int length;
                        String allTextString ="";
                        String textString = "";
                        while ((length = fileReader.read(cbuf))>0) {
                            *//*String textString ="";
                            for (int i = 0; i < length; i++) {
                                textString += cbuf[i];
                            }
                            allTextString += textString;*//*
                            allTextString += String.valueOf(cbuf,0,length);
                            //Log.i(TAG,"textString:"+textString);

                        }

                        parStringDataToList(allTextString);

                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }
        else {
            //文件不存在，没有上传
            MyUtil.showToask(this,"该文件没有上传");
        }
    }*/

    private void parStringDataToList(String fileStringData) {
        List<Integer> integerList = new ArrayList<>();
        String[] split = fileStringData.split(" ");
        for (String s:split){
            integerList.add(Integer.parseInt(s));
            //Log.i(TAG,""+Integer.parseInt(s));
        }
        mEcgGroupSize = integerList.size() / 10;

        mAllTimeAtSecond = (int) (integerList.size()/(150*1f));  //计算总的时间秒数，1s为150帧，即为150个数据点
        mAllTimeString = calcuEcgDataTimeAtSecond(mAllTimeAtSecond);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_ecg_protime.setText("0'0/"+mAllTimeString);
            }
        });


        Log.i(TAG,"integerList.size(): "+integerList.size());
        MyUtil.hideDialog();
        startDrawLine(integerList);
    }

    private void startDrawLine(List<Integer> integerList) {
        pv_ecg_path.setEcgDatas(integerList);
        pv_ecg_path.startThread();
    }


    public void back(View view) {
        finish();
    }

    //将秒数换算成1'57这种时间个格式
    private String calcuEcgDataTimeAtSecond(int allTimeAtSecond) {
        int minute = (int) (allTimeAtSecond/60f);
        int second = (int) (allTimeAtSecond%60f);
        return minute+"'"+second;
    }

    //设置进度条，通过设置心电文件的ecgDatas的position
    private void setPercent(int percent){
        Log.i(TAG,"percent:"+percent);
        int position = (int) ((percent / 100.0) * mEcgGroupSize);
        Log.i(TAG,"position:"+position);
        pv_ecg_path.setCurrentcountIndex(position);
    }
}
