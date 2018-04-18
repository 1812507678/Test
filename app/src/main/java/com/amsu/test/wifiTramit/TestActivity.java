package com.amsu.test.wifiTramit;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amsu.test.R;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TestActivity extends BaseActivity {

    private static final String TAG = "TestActivity";
    private TextView servic_info;
    private TextView recive_msg;
    private TextView recive_text;
    private OutputStream socketWriter;
    private FileOutputStream fileOutputStream;
    private DataOutputStream dataOutputStream;  //二进制文件输出流，写入文件
    private ByteBuffer byteBuffer;
    private ListView list_item;
    private List<String> stringList;
    private String serverMsg;
    private int length;

    int mUploadFileCountIndex;  //当前传输的索引
    int mOneUploadMaxByte = 512*16;
    int mAllFileCount ;  //文件分几次上传，mAllFileCount = 按mOneUploadMaxByte传的次数(整数上传)
    int mFileLastRemainder ;  //最后一次需要上传的，mFileLastRemainder = 文件字节数%mOneUploadMaxByte
    long startTimeMillis = 0;
    long endTimeMillis;
    int requireRetransmissionCount;

    int onePackageReadLength = 0;
    List<Integer> onePackageData = new ArrayList<>();
    String onePackageDataHexString = "";
    private List<Byte> mAllData = new ArrayList<>();
    private TextView tv_filelength;
    private TextView tv_uploadprogress;
    private String currentUploadFileName;
    private FileListAdapter fileListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        initView();
        /*byte n = 2;
        Byte aByte  = n;
        mAllData.add(n);*/


    }

    private void initView() {
        initHeadView();
        setCenterText("WiFi底座测试");

        servic_info = (TextView) findViewById(R.id.servic_info);
        recive_msg = (TextView) findViewById(R.id.recive_msg);
        recive_text = (TextView) findViewById(R.id.recive_text);
        tv_filelength = (TextView) findViewById(R.id.tv_filelength);
        tv_uploadprogress = (TextView) findViewById(R.id.tv_uploadprogress);
        list_item = (ListView) findViewById(R.id.list_item);

        stringList = new ArrayList<>();
        fileListAdapter = new FileListAdapter(stringList,this);

        list_item.setAdapter(fileListAdapter);

        list_item.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String fileName = stringList.get(position);
                currentUploadFileName = fileName;
                Toast.makeText(TestActivity.this,fileName+" 开始上传",Toast.LENGTH_LONG).show();
                //String fileName = "20170413172800.ecg";
                String startOrder = "FF040018";
                String deviceOrder = startOrder + DeviceOffLineFileUtil.stringToHexString(fileName) + DeviceOffLineFileUtil.readDeviceSpecialFileBeforeAddSum("FF 04 00 18",fileName)+"16";
                Log.i(TAG,"deviceOrder:"+deviceOrder);
                sendReadDeviceOrder(deviceOrder);
                isStartUploadData = false;
                recive_msg.setText("");
            }
        });

        list_item.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String fileName = stringList.get(position);
                deleteOneFile(fileName);
                return false;
            }
        });

        DeviceOffLineFileUtil.setTransferTimeOverTime(new DeviceOffLineFileUtil.OnTimeOutListener() {
            @Override
            public void onTomeOut() {
                Log.i(TAG,"onTomeOut 判断是否需要重传");
                judgeRequireRetransmission();
            }
        });

    }

    public void deleteOneFile(String fileName) {
        String startOrder = "FF060018";
        String deviceOrder = startOrder + DeviceOffLineFileUtil.stringToHexString(fileName) + DeviceOffLineFileUtil.readDeviceSpecialFileBeforeAddSum("FF 06 00 18",fileName)+"16";
        Log.i(TAG,"删除文件deviceOrder:"+deviceOrder);
        sendReadDeviceOrder(deviceOrder);
    }

    public void connectToWifi(View view) {
        /*new Thread(){
            @Override
            public void run() {
                super.run();
                WifiAdmin.connectToWifi(TestActivity.this);
            }
        }.start();*/
        WifiAdmin.connectToWifi(TestActivity.this);
    }

    public void createSocket(View view) {
        WifiManager wifiManage = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo info = wifiManage.getDhcpInfo();
        WifiInfo wifiinfo = wifiManage.getConnectionInfo();
        String ip = intToIp(wifiinfo.getIpAddress());
        String serverAddress = intToIp(info.serverAddress);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
        String message = "Hello this is dawin ! send time:" + df.format(new Date());
        new Sender(serverAddress, message).start();
        String msg = "ip:" + ip + "serverAddress:" + serverAddress + info;
        //servic_info.setText(msg);
        Log.i(TAG, msg);
    }

    /** 将获取的int转为真正的ip地址,参考的网上的，修改了下 */
    private String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
    }


    boolean isStartUploadData;



    /* 客户端发送数据 */
    private class Sender extends Thread {
        String serverIp;
        String message;

        Sender(String serverAddress, String message) {
            super();
            serverIp = serverAddress;
            this.message = message;
        }

        public void run() {
            Socket sock = null;
            try {
                // 声明sock，其中参数为服务端的IP地址与自定义端口
                Log.i(TAG,"serverIp：" + serverIp);
                sock = new Socket(serverIp, 8080);
                Log.i(TAG,"WifiConnection I am try to writer" + sock);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        servic_info.setTextColor(Color.parseColor("#00CD00"));
                        servic_info.setText("主机连接成功");
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        servic_info.setTextColor(Color.parseColor("#EE3B3B"));
                        servic_info.setText("主机连接失败");
                    }
                });
            }
            try {
                socketWriter = sock.getOutputStream();
                InputStream inputStream = sock.getInputStream();

                //byte[] bytes = new byte[2048];
                final byte[] bytes = new byte[1024*10];
                //byte[] bytes = new byte[512+14];
                while ((length =inputStream.read(bytes))!=-1){
                    Log.i(TAG,"length:" + length);
                    if (isStartUploadData){
                        DeviceOffLineFileUtil.stopTime();
                        //final String toHexString = DeviceOffLineFileUtil.binaryToHexString(bytes, length);
                        //final String s = DeviceOffLineFileUtil.binaryToHexString(bytes, length,"");
                        //Log.i(TAG,"收到数据:" + toHexString);
                        //开始传输数据了
                        //dealWithDeviceFileUpload(toHexString,length);
                        dealWithDeviceFileUploadByte(length,bytes);
                        DeviceOffLineFileUtil.startTime();
                    }
                    else {
                        final String toHexString = DeviceOffLineFileUtil.binaryToHexString(bytes, length);
                        //final String s = DeviceOffLineFileUtil.binaryToHexString(bytes, length,"");
                        Log.i(TAG,"收到数据:" + toHexString);

                        if (toHexString.startsWith("FF 81")){  //版本号：
                            dealWithDeviceVersion(toHexString);
                        }
                        else if (toHexString.startsWith("FF 82")){ //设备id:
                            dealWithDeviceID(toHexString);
                        }
                        else if (toHexString.startsWith("FF 83")){ //文件列表：
                            dealWithDeviceFileList(toHexString);
                        }
                        else if (toHexString.startsWith("FF 84")){  //文件长度：
                            dealWithDeviceFileLength(toHexString);
                        }
                        else if (toHexString.startsWith("FF 85")){  //上传文件：
                            //startTimeOutTiming();
                            //dealWithDeviceFileUpload(toHexString,length);
                            dealWithDeviceFileUploadByte(length,bytes);
                            DeviceOffLineFileUtil.startTime();
                            isStartUploadData = true;
                        }
                        else if (toHexString.startsWith("FF 86")){  //删除文件：
                            dealWithDeviceDeleteFile(toHexString);
                        }
                        else if (toHexString.startsWith("FF 87")){  //一键删除文件：
                            dealWithDeviceDeleteAllFile(toHexString);
                        }
                        else if (toHexString.startsWith("FF 88")){  //生成文件：
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    serverMsg ="主机hex: "+toHexString;
                                    recive_msg.setText(serverMsg);
                                    dealWithGenerateFile(toHexString);
                                }
                            });
                        }
                    }

                    /*runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isStartUploadData){
                                //开始传输数据了
                                //dealWithDeviceFileUpload(toHexString,length);
                                dealWithDeviceFileUpload(bytes,length);
                            }
                            else {
                                if (toHexString.startsWith("FF 81")){  //版本号：
                                    dealWithDeviceVersion(toHexString);
                                }
                                else if (toHexString.startsWith("FF 82")){//设备id:
                                    dealWithDeviceID(toHexString);
                                }
                                else if (toHexString.startsWith("FF 83")){  //文件列表：
                                    dealWithDeviceFileList(toHexString);
                                }
                                else if (toHexString.startsWith("FF 84")){  //文件长度：
                                    dealWithDeviceFileLength(toHexString);
                                }
                                else if (toHexString.startsWith("FF 85")){  //上传文件：
                                    isStartUploadData = true;
                                    //dealWithDeviceFileUpload(toHexString,length);
                                    dealWithDeviceFileUpload(bytes,length);
                                }
                                else if (toHexString.startsWith("FF 86")){  //删除文件：
                                    dealWithDeviceDeleteFile(toHexString);
                                }
                                else if (toHexString.startsWith("FF 87")){  //删除文件：
                                    dealWithDeviceDeleteAllFile(toHexString);
                                }
                            }


                            Toast.makeText(TestActivity.this,"收到数据",Toast.LENGTH_LONG).show();
                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
                            serverMsg +="主机hex: "+df.format(new Date()) +":\n"+ toHexString;
                            recive_msg.setText(serverMsg);
                        }
                    });*/

                }
                //sock.close();
            }
            catch (Exception e) {
                Log.e(TAG,"发送错误:[Sender]run()" + e.getMessage());
            }
        }

    }

    private void dealWithGenerateFile(String toHexString) {

    }

    public void getVersion(View view) {
        //"FF0100060616"
        /*if (socketWriter!=null){
            byte[] b = new byte[6];
            b[0] = (byte) 0xFF;
            b[1] = (byte) 0x01;
            b[2] = (byte) 0x00;
            b[3] = (byte) 0x06;
            b[4] = (byte) 0x06;
            b[5] = (byte) 0x16;

            try {
                socketWriter.write(b);
                Log.i(TAG,"硬件版本号：" + String.valueOf(b));
                socketWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
        sendReadDeviceOrder(DeviceOffLineFileUtil.readDeviceVersion);
    }

    public void getDeviceID(View view) {
        sendReadDeviceOrder(DeviceOffLineFileUtil.readDeviceID);
    }

    public void getFileList(View view) {
        sendReadDeviceOrder(DeviceOffLineFileUtil.readDeviceFileList);
    }

    //给设备发送16进制指令
    private void sendReadDeviceOrder(String deviceOrder) {
        byte[] bytes = DeviceOffLineFileUtil.hexStringToBytes(deviceOrder);
        if (socketWriter!=null){
            try {
                socketWriter.write(bytes);
                Log.i(TAG,"发送命令：" + deviceOrder);
                socketWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void getFileLength(View view) {
        // FF04001832303137303431323130323330302e6563672f16
        String fileName = "20170413172800.ecg";
        String startOrder = "FF040018";
        String deviceOrder = startOrder + DeviceOffLineFileUtil.stringToHexString(fileName) + DeviceOffLineFileUtil.readDeviceSpecialFileBeforeAddSum("FF 04 00 18",fileName)+"16";
        Log.i(TAG,"deviceOrder:"+deviceOrder);
        sendReadDeviceOrder(deviceOrder);
    }

    public void uploadFile(View view) {
        //int oneUploadMaxByte = 1024*10;
        int fileLength = 5632;

        if (fileLength>mOneUploadMaxByte){
            mAllFileCount = fileLength / mOneUploadMaxByte; // 需要传的次数
            mFileLastRemainder = fileLength % mOneUploadMaxByte;  //余数，最后一次需要的传的次数
            String offsetHexLenght = DeviceOffLineFileUtil.getFormatHexFileLenght(8, mOneUploadMaxByte*mUploadFileCountIndex);
            String fileHexLenght = DeviceOffLineFileUtil.getFormatHexFileLenght(8, mOneUploadMaxByte);
            String startOrder = "FF05000e"+offsetHexLenght+fileHexLenght;
            String deviceOrder = startOrder+DeviceOffLineFileUtil.readDeviceSpecialFileBeforeAddSum(startOrder)+"16";
            Log.i(TAG,mUploadFileCountIndex+"分段上传deviceOrder:"+deviceOrder);
            sendReadDeviceOrder(deviceOrder);

        }
        else {
            String offsetHexLenght = DeviceOffLineFileUtil.getFormatHexFileLenght(8, 0);
            String fileHexLenght = DeviceOffLineFileUtil.getFormatHexFileLenght(8, fileLength);
            String startOrder = "FF05000e"+offsetHexLenght+fileHexLenght;
            String deviceOrder = startOrder+DeviceOffLineFileUtil.readDeviceSpecialFileBeforeAddSum(startOrder)+"16";
            Log.i(TAG,"一次上传deviceOrder:"+deviceOrder);
            sendReadDeviceOrder(deviceOrder);
        }


        /*int count = 1;
        int remainder = 0;
        if (fileLength>oneUploadMaxByte){
            count = fileLength / oneUploadMaxByte; // 需要传的次数
            remainder = fileLength % oneUploadMaxByte;  //余数，最后一次需要的传的次数
        }
        for (int i=0;i<count;i++){
            String offsetHexLenght = DeviceOffLineFileUtil.getFormatHexFileLenght(8, oneUploadMaxByte*i);
            String fileHexLenght = DeviceOffLineFileUtil.getFormatHexFileLenght(8, oneUploadMaxByte);

            String startOrder = "FF05000e"+offsetHexLenght+fileHexLenght;
            String deviceOrder = startOrder+DeviceOffLineFileUtil.readDeviceSpecialFileBeforeAddSum(startOrder)+"16";
            Log.i(TAG,i+"分段上传deviceOrder:"+deviceOrder);
            sendReadDeviceOrder(deviceOrder);
        }
        if (remainder>0){
            String offsetHexLenght = DeviceOffLineFileUtil.getFormatHexFileLenght(8, oneUploadMaxByte*count);
            String fileHexLenght = DeviceOffLineFileUtil.getFormatHexFileLenght(8, remainder);

            String startOrder = "FF05000e"+offsetHexLenght+fileHexLenght;
            String deviceOrder = startOrder+DeviceOffLineFileUtil.readDeviceSpecialFileBeforeAddSum(startOrder)+"16";
            Log.i(TAG,"余数上传deviceOrder:"+deviceOrder);
            sendReadDeviceOrder(deviceOrder);
        }*/

    }

    public void deleteFile(View view) {
        String fileName = "20170420121230.ecg";
        String startOrder = "FF060018";
        String deviceOrder = startOrder + DeviceOffLineFileUtil.stringToHexString(fileName) + DeviceOffLineFileUtil.readDeviceSpecialFileBeforeAddSum("FF 06 00 18",fileName)+"16";
        Log.i(TAG,"deviceOrder:"+deviceOrder);
        sendReadDeviceOrder(deviceOrder);
    }

    public void deleteAllFile(View view) {
        String deviceOrder  ="FF0700060C16";
        sendReadDeviceOrder(deviceOrder);
    }

    public void generateFile(View view) {
        sendReadDeviceOrder(DeviceOffLineFileUtil.generateDeviceFile);
    }

    public void lookupDraw(View view) {
        startActivity(new Intent(this,DrawLineTestActivity.class));
    }

    //版本号：
    private void dealWithDeviceVersion(final String toHexString) {
        // FF 81 00 0C 10 04 07 10 04 07 C2 16
        String[] split = toHexString.split(" ");
        String deviceVersion = "";
        for (int i = 4; i <10; i++) {
            deviceVersion += Integer.parseInt(split[i], 16);
        }
        // deviceVersion:16471647
        Log.i(TAG,"deviceVersion:"+deviceVersion);
        final String finalDeviceVersion = deviceVersion;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recive_text.setText("版本号:"+ finalDeviceVersion);

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
                serverMsg ="主机hex: "+df.format(new Date()) +":\n"+ toHexString;
                recive_msg.setText(serverMsg);
            }
        });

    }

    //设备id:
    private void dealWithDeviceID(final String allHexString) {
        //  "FF 82 00 12 AF C1 50 3E 07 00 F8 FF 04 B3 FB 4C 8D 16";
        String[] split = allHexString.split(" ");
        String hexString ="";
        for (int i = 15; i >=4; i--) {
            hexString += split[i];
        }
        final String deviceID = parseHexStringToBigInt(hexString);
        //deviceID:23825146522977399412272185775
        Log.i(TAG,"deviceID:"+deviceID);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recive_text.setText("设备号: "+deviceID);

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
                serverMsg ="主机hex: "+df.format(new Date()) +":\n"+ allHexString;
                recive_msg.setText(serverMsg);
            }
        });

    }

    //文件列表：
    private void dealWithDeviceFileList(final String allHexString) {
        stringList.clear();
        // 32 30 31 37 30 34 31 32 31 30 32 33 30 30 2E 65 63 67
        String[] split = allHexString.split(" ");
        int fileLength = Integer.parseInt(split[4], 16);
        Log.i(TAG,"fileLength:"+fileLength);
        String[] fileList = new String[fileLength];

        for (int i = 0; i < fileLength; i++) {
            String fileNameString ="";
            for (int j = 0; j < 18; j++) {
                fileNameString += DeviceOffLineFileUtil.hexStringToString(split[5+i*18+j]);
            }
            // fileNameString:20170412102300.ecg
            Log.i(TAG,"fileNameString:"+fileNameString);
            fileList[i] = fileNameString;
            stringList.add(i,fileNameString);
        }
        Log.i(TAG,"fileList.length:"+fileList.length);

        String test ="";
        for (int i = 0; i < fileLength; i++) {
            test += fileList[i]+"\n";
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(TestActivity.this,"收到数据",Toast.LENGTH_LONG).show();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
                serverMsg ="主机hex: "+df.format(new Date()) +":\n"+ allHexString;
                recive_msg.setText(serverMsg);

                fileListAdapter.notifyDataSetChanged();
            }
        });
    }

    /*//文件长度
    private void dealWithDeviceFileLength(String allHexString) {
        //FF 84 00 0A 00 00 02 00 8F 16
        String[] split = allHexString.split(" ");
        String hexLength = split[4]+split[5]+split[6]+split[7];
        int fileLengthInt = Integer.parseInt(hexLength, 16);
        Log.i(TAG,"fileLengthInt:"+fileLengthInt);

        recive_text.setText("fileLengthInt:\n"+fileLengthInt);

    }*/

    //文件长度
    private void dealWithDeviceFileLength(String allHexString) {
        //FF 84 00 0A 00 00 02 00 8F 16
        String[] split = allHexString.split(" ");
        String hexLength = split[4]+split[5]+split[6]+split[7];
        final int fileLengthInt = Integer.parseInt(hexLength, 16);
        Log.i(TAG,"fileLengthInt:"+fileLengthInt);

        //servic_info.setText("fileLengthInt:"+fileLengthInt);
        mUploadFileCountIndex = 0;

        if (fileLengthInt>mOneUploadMaxByte){
            mAllFileCount = fileLengthInt / mOneUploadMaxByte; // 需要传的次数
            mFileLastRemainder = fileLengthInt % mOneUploadMaxByte;  //余数，最后一次需要的传的次数
            Log.i(TAG,"需要传的次数 mAllFileCount:"+mAllFileCount);
            Log.i(TAG,"余数 mFileLastRemainder:"+mFileLastRemainder);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    float fileLength = (float) (fileLengthInt/(1024*1024.0));
                    DecimalFormat decimalFormat=new DecimalFormat("0.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
                    String fileLengthAtM=decimalFormat.format(fileLength)+"M";//format 返回的是字符串
                    tv_filelength.setText("文件长度 字节:"+fileLengthInt+"    "+fileLengthAtM+"  需要传输次数(一次8K):"+(mAllFileCount+1));
                }
            });

            String offsetHexLenght = "";
            offsetHexLenght = DeviceOffLineFileUtil.getFormatHexFileLenght(8, mOneUploadMaxByte*mUploadFileCountIndex);


            //String offsetHexLenght = DeviceOffLineFileUtil.getFormatHexFileLenght(8, offsetTest);
            String fileHexLenght = DeviceOffLineFileUtil.getFormatHexFileLenght(8, mOneUploadMaxByte);
            String startOrder = "FF05000e"+offsetHexLenght+fileHexLenght;
            String deviceOrder = startOrder+DeviceOffLineFileUtil.readDeviceSpecialFileBeforeAddSum(startOrder)+"16";
            Log.i(TAG,mUploadFileCountIndex+"分段上传deviceOrder:"+deviceOrder);
            sendReadDeviceOrder(deviceOrder);
        }
        else {
            mAllFileCount = 0; // 需要传的次数
            mFileLastRemainder = fileLengthInt % mOneUploadMaxByte;  //余数，最后一次需要的传的次数

            String offsetHexLenght = DeviceOffLineFileUtil.getFormatHexFileLenght(8, 0);
            String fileHexLenght = DeviceOffLineFileUtil.getFormatHexFileLenght(8, fileLengthInt);
            String startOrder = "FF05000e"+offsetHexLenght+fileHexLenght;
            String deviceOrder = startOrder+DeviceOffLineFileUtil.readDeviceSpecialFileBeforeAddSum(startOrder)+"16";
            Log.i(TAG,"一次上传deviceOrder:"+deviceOrder);
            sendReadDeviceOrder(deviceOrder);
        }
    }

    //文件上传
    private void dealWithDeviceFileUpload(int length, final String toHexString) {
        if (startTimeMillis==0){
            startTimeMillis = System.currentTimeMillis();
        }
        /*if (startTimeMillis==0){
            startTimeMillis = System.currentTimeMillis();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
                    serverMsg ="主机hex: "+df.format(new Date()) +":\n"+ toHexString;
                    recive_msg.setText(serverMsg);
                }
            });
        }
        else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    serverMsg += "\n" +toHexString;
                    recive_msg.setText(serverMsg);
                }
            });

        }*/

        if (mUploadFileCountIndex<mAllFileCount){
            //前几次整数上传

            //int allHexlength = Integer.parseInt(allHexString, 16);

            //List<Integer> geIntEcgaArr = ECGUtil.geIntEcgaArrList(allHexString, " ", 12 ,allHexlength);
            //onePackageData.addAll(geIntEcgaArr);
            onePackageReadLength += length;
            onePackageDataHexString += toHexString;
            Log.i(TAG,"分包 当前收到总长度length:"+onePackageReadLength);
            if (onePackageReadLength==mOneUploadMaxByte+16*14){
                Log.i(TAG,"当前包上传成功:"+mUploadFileCountIndex);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_uploadprogress.setText("当前进度:"+mUploadFileCountIndex);
                    }
                });

                //writeEcgDataToBinaryFile(onePackageDataHexString,16);
                //DeviceOffLineFileUtil.addEcgDataToList(onePackageDataHexString,mAllData);
                //onePackageData.clear();
                onePackageReadLength = 0;
                onePackageDataHexString = "";
                uploadNextPackageData();

                if (mUploadFileCountIndex==mAllFileCount-1 && mFileLastRemainder==0){
                    //上传完成
                    uploadCurrentFileSuccess();
                }
            }
        }
        else if (mUploadFileCountIndex==mAllFileCount){

            //int allHexlength = Integer.parseInt(allHexString, 16);

            //List<Integer> geIntEcgaArr = ECGUtil.geIntEcgaArrList(allHexString, " ", 12 ,uploadLengthInt);
            //onePackageData.addAll(geIntEcgaArr);
            onePackageReadLength += length;
            onePackageDataHexString += toHexString;
            Log.i(TAG,"余数 当前包收到总长度length:"+onePackageReadLength);
            //Log.i(TAG,"mFileLastRemainder:"+mFileLastRemainder);
            if (onePackageReadLength == mFileLastRemainder+(int) Math.ceil(mFileLastRemainder/512.0)*14){
                Log.i(TAG,"余数上传成功:"+mUploadFileCountIndex);
                //则文件数据传完，isStartUploadData置为false
                //DeviceOffLineFileUtil.addRemainderEcgDataToList(onePackageDataHexString,onePackageReadLength, mAllData);
                isStartUploadData = false;
                onePackageReadLength = 0;
                onePackageDataHexString = "";
                //writeEcgDataToBinaryFile(onePackageData);
                //onePackageData.clear();

                uploadCurrentFileSuccess();


            }

        }

    }


    //文件上传
    private void dealWithDeviceFileUploadByte(int length, byte[] bytes) {
        if (startTimeMillis==0){
            startTimeMillis = System.currentTimeMillis();
        }
        if (mUploadFileCountIndex<mAllFileCount){
            //前几次整数上传

            //int allHexlength = Integer.parseInt(allHexString, 16);

            //List<Integer> geIntEcgaArr = ECGUtil.geIntEcgaArrList(allHexString, " ", 12 ,allHexlength);
            //onePackageData.addAll(geIntEcgaArr);
            onePackageReadLength += length;
            //onePackageDataHexString += toHexString;
            Log.i(TAG,"分包 当前收到总长度length:"+onePackageReadLength);
            if (onePackageReadLength==mOneUploadMaxByte+16*14){
                Log.i(TAG,"当前包上传成功:"+mUploadFileCountIndex);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_uploadprogress.setText("当前进度:"+mUploadFileCountIndex);
                    }
                });

                //writeEcgDataToBinaryFile(onePackageDataHexString,16);
                //DeviceOffLineFileUtil.addEcgDataToList(bytes,mAllData);

                for (byte b:bytes){
                    mAllData.add(b);
                }

                //onePackageData.clear();
                onePackageReadLength = 0;
                onePackageDataHexString = "";
                uploadNextPackageData();

                if (mUploadFileCountIndex==mAllFileCount-1 && mFileLastRemainder==0){
                    //上传完成
                    uploadCurrentFileSuccess();
                }
            }
            else if (mOneUploadMaxByte-512+(16-1)*14<onePackageReadLength && onePackageReadLength<mOneUploadMaxByte+16*14){

            }
        }
        else if (mUploadFileCountIndex==mAllFileCount){
            //int allHexlength = Integer.parseInt(allHexString, 16);

            //List<Integer> geIntEcgaArr = ECGUtil.geIntEcgaArrList(allHexString, " ", 12 ,uploadLengthInt);
            //onePackageData.addAll(geIntEcgaArr);
            onePackageReadLength += length;
            //onePackageDataHexString += toHexString;
            Log.i(TAG,"余数 当前包收到总长度length:"+onePackageReadLength);
            //Log.i(TAG,"mFileLastRemainder:"+mFileLastRemainder);
            if (onePackageReadLength == mFileLastRemainder+(int) Math.ceil(mFileLastRemainder/512.0)*14){
                Log.i(TAG,"余数上传成功:"+mUploadFileCountIndex);
                //则文件数据传完，isStartUploadData置为false
                //DeviceOffLineFileUtil.addRemainderEcgDataToList(onePackageDataHexString,onePackageReadLength, mAllData);
                isStartUploadData = false;
                onePackageReadLength = 0;
                onePackageDataHexString = "";
                //writeEcgDataToBinaryFile(onePackageData);
                //onePackageData.clear();

                for (byte b:bytes){
                    mAllData.add(b);
                }
                uploadCurrentFileSuccess();
            }

        }

    }

    //当前文件上传成功
    private void uploadCurrentFileSuccess() {
        //上传完成
        endTimeMillis = System.currentTimeMillis();
        Log.i(TAG,"整个文件上传完成");
        Log.i(TAG,"startTimeMillis"+new Date(startTimeMillis));
        Log.i(TAG,"endTimeMillis"+new Date(endTimeMillis));

        Log.i(TAG,"求情重传次数requireRetransmissionCount "+requireRetransmissionCount);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG,"endTimeMillis - startTimeMillis："+(endTimeMillis - startTimeMillis));
                double timeMin =  (endTimeMillis - startTimeMillis) / (1000 * 60.0);
                Log.i(TAG,"timeMin："+timeMin);

                DecimalFormat decimalFormat=new DecimalFormat("0.0");//构造方法的字符格式这里如果小数不足2位,会以0补足.
                String time=decimalFormat.format(timeMin);//format 返回的是字符串
                Log.i(TAG,"time："+time);
                tv_uploadprogress.setText("传输完成  耗时(分钟):"+time+"  丢包次数："+requireRetransmissionCount);
                startTimeMillis = 0;
            }
        });

        Log.i(TAG,"mAllData.size(): "+mAllData.size());
        //boolean isWriteSuccess = DeviceOffLineFileUtil.writeEcgByteDataToBinaryFile(mAllData, currentUploadFileName);
        //Log.i(TAG,"写入文件isWriteSuccess:"+isWriteSuccess);

    }

    //判断是否需要重传
    private void judgeRequireRetransmission() {
        if (mUploadFileCountIndex==mAllFileCount){
            //文件长传完成
            DeviceOffLineFileUtil.stopTime();
            //DeviceOffLineFileUtil.destoryTime();
        }
        else {
            requireRetransmission();
        }
    }

    //请求重传
    private void requireRetransmission() {
        Log.i(TAG,"请求重传requireRetransmission:");
        requireRetransmissionCount++;
        onePackageData.clear();
        onePackageReadLength = 0;
        mUploadFileCountIndex--;
        uploadNextPackageData();

    }

    //文件上传
    private void dealWithDeviceFileUpload(String allHexString,int length) {
        if (mUploadFileCountIndex<mAllFileCount){
            //前几次整数上传

            //int allHexlength = Integer.parseInt(allHexString, 16);
            Log.i(TAG,"收到length:"+length);
            //List<Integer> geIntEcgaArr = ECGUtil.geIntEcgaArrList(allHexString, " ", 12 ,allHexlength);
            //onePackageData.addAll(geIntEcgaArr);
            onePackageReadLength += length;

            if (onePackageReadLength==mOneUploadMaxByte+16*14){
                Log.i(TAG,"当前包上传成功:"+mUploadFileCountIndex);
                writeEcgDataToBinaryFile(onePackageData);
                onePackageData.clear();
                uploadNextPackageData();
            }

        }
        else if (mUploadFileCountIndex==mAllFileCount){

            //int allHexlength = Integer.parseInt(allHexString, 16);
            Log.i(TAG,"收到length:"+length);
            //List<Integer> geIntEcgaArr = ECGUtil.geIntEcgaArrList(allHexString, " ", 12 ,uploadLengthInt);
            //onePackageData.addAll(geIntEcgaArr);
            onePackageReadLength += length;
            if (onePackageReadLength==mFileLastRemainder+(mFileLastRemainder/512+1)*14){
                Log.i(TAG,"余数上传成功:"+mUploadFileCountIndex);
                writeEcgDataToBinaryFile(onePackageData);
                onePackageData.clear();
            }
        }

        /*if (mUploadFileCountIndex<mAllFileCount){
            //前几次整数上传
            if (onePackageReadCountIndex<mOneUploadMaxByte/512){
                //是上传一个包
                String[] split = allHexString.split(" ");
                String offsetHexString = split[4]+split[5]+split[6]+split[7];
                String uploadHexLengthString = split[8]+split[9]+split[10]+split[11];

                int offsetInt = Integer.parseInt(offsetHexString, 16);
                int uploadLengthInt = Integer.parseInt(uploadHexLengthString, 16);
                Log.i(TAG,"偏移量offsetInt:"+offsetInt+"  上传成功uploadLengthInt:"+uploadLengthInt);
                recive_text.setText("偏移量offsetInt:"+offsetInt+"  上传成功uploadLengthInt:"+uploadLengthInt);

                List<Integer> geIntEcgaArr = ECGUtil.geIntEcgaArrList(allHexString, " ", 12 ,uploadLengthInt);
                onePackageData.addAll(geIntEcgaArr);
                onePackageReadLength += uploadLengthInt;

                onePackageReadCountIndex++;


            }
            else{
                //上传次数确定，需要校验文件长度是否正确，正确则写入文件，当前包上传完成，开始下一个包，不正确则重新上传
                if (onePackageReadLength==mOneUploadMaxByte){
                    Log.i(TAG,"当前包上传成功:"+mUploadFileCountIndex);
                    writeEcgDataToBinaryFile(onePackageData);
                    onePackageData.clear();
                    onePackageReadCountIndex = 0;
                    uploadNextPackageData();
                }
                else {
                    //重传
                    Log.i(TAG,"丢包重传:"+mUploadFileCountIndex);

                }

            }
        }

        else if (mUploadFileCountIndex==mAllFileCount) {
            //最后一次余数
            if (onePackageReadCountIndex<mFileLastRemainder/512){
                //是上传一个包
                String[] split = allHexString.split(" ");
                String offsetHexString = split[4]+split[5]+split[6]+split[7];
                String uploadHexLengthString = split[8]+split[9]+split[10]+split[11];

                int offsetInt = Integer.parseInt(offsetHexString, 16);
                int uploadLengthInt = Integer.parseInt(uploadHexLengthString, 16);
                Log.i(TAG,"偏移量offsetInt:"+offsetInt+"  上传成功uploadLengthInt:"+uploadLengthInt);
                recive_text.setText("偏移量offsetInt:"+offsetInt+"  上传成功uploadLengthInt:"+uploadLengthInt);
                List<Integer> geIntEcgaArr = ECGUtil.geIntEcgaArrList(allHexString, " ", 12 ,uploadLengthInt);
                onePackageData.addAll(geIntEcgaArr);
                onePackageReadLength += uploadLengthInt;
                onePackageReadCountIndex++;

            }
            else {
                if (onePackageReadLength==mFileLastRemainder){
                    Log.i(TAG,"余数上传成功:"+mUploadFileCountIndex);
                    writeEcgDataToBinaryFile(onePackageData);
                    onePackageData.clear();
                    onePackageReadCountIndex = 0;

                }
                else {
                    //重传
                    Log.i(TAG,"余数丢包重传:"+mUploadFileCountIndex);

                }

            }
        }*/



        /*String[] split = allHexString.split(" ");
        String offsetHexString = split[4]+split[5]+split[6]+split[7];
        String uploadHexLengthString = split[8]+split[9]+split[10]+split[11];

        int offsetInt = Integer.parseInt(offsetHexString, 16);
        int uploadLengthInt = Integer.parseInt(uploadHexLengthString, 16);
        Log.i(TAG,"偏移量offsetInt:"+offsetInt+"  上传成功uploadLengthInt:"+uploadLengthInt);
        recive_text.setText("偏移量offsetInt:"+offsetInt+"  上传成功uploadLengthInt:"+uploadLengthInt);

        if (mUploadFileCountIndex<mAllFileCount){
            //前几次整数上传
            if(offsetInt==mUploadFileCountIndex*mOneUploadMaxByte && uploadLengthInt==mOneUploadMaxByte){
                //偏移值正确+长度正确
                //写入文件
                int[] geIntEcgaArr = ECGUtil.geIntEcgaArr(allHexString, " ", 12 ,uploadLengthInt);
                writeEcgDataToBinaryFile(geIntEcgaArr);
            }
            else {
                //上传出现问题，索引值-1，重新上传该次
                mUploadFileCountIndex--;
            }
        }
        else if (mUploadFileCountIndex==mAllFileCount){
            //最后一次余数
            if(offsetInt==mUploadFileCountIndex*mOneUploadMaxByte && uploadLengthInt==mFileLastRemainder){
                //偏移值正确+长度正确(余数)
                //写入文件
                int[] geIntEcgaArr = ECGUtil.geIntEcgaArr(allHexString, " ", 12 ,uploadLengthInt);
                writeEcgDataToBinaryFile(geIntEcgaArr);
            }
            else {
                //上传出现问题，索引值-1，重新上传该次
                mUploadFileCountIndex--;
            }
        }

        uploadNextPackageData();*/

    }

    //写到文件里，二进制方式写入
    private void writeEcgDataToBinaryFile(String hexStringData,int count){
        List<Integer> integerList = new ArrayList<>();
        for (int i=0;i<count;i++){
            int start = 12+i*(512+14);
            List<Integer> integers = ECGUtil.geIntEcgaArrList(hexStringData, " ", start, 512);
            integerList.addAll(integers);
        }

        try {
            if (fileOutputStream==null){
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+ MyUtil.getECGFileNameDependFormatTime(new Date())+".ecg";
                //String filePath = getCacheDir()+"/"+System.currentTimeMillis()+".ecg";
                Log.i(TAG,"filePath:"+filePath);
                fileOutputStream = new FileOutputStream(filePath,true);
                dataOutputStream = new DataOutputStream(fileOutputStream);
                byteBuffer = ByteBuffer.allocate(2);
            }
            String x="";
            for (int anInt : integerList) {
                byteBuffer.clear();
                byteBuffer.putShort((short) anInt);
                dataOutputStream.writeByte(byteBuffer.get(1));
                dataOutputStream.writeByte(byteBuffer.get(0));
                x += anInt;
            }
            //Log.i(TAG,"x:"+x);
            dataOutputStream.flush();
            Log.i(TAG,"写入文件成功:");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //写到文件里，二进制方式写入
    private void writeEcgDataToBinaryFile(List<Integer> ints) {

        try {
            if (fileOutputStream==null){
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+ MyUtil.getECGFileNameDependFormatTime(new Date())+".ecg";
                //String filePath = getCacheDir()+"/"+System.currentTimeMillis()+".ecg";
                Log.i(TAG,"filePath:"+filePath);
                fileOutputStream = new FileOutputStream(filePath,true);
                dataOutputStream = new DataOutputStream(fileOutputStream);
                byteBuffer = ByteBuffer.allocate(2);
            }
            String x="";
            for (int anInt : ints) {
                byteBuffer.clear();
                byteBuffer.putShort((short) anInt);
                dataOutputStream.writeByte(byteBuffer.get(1));
                dataOutputStream.writeByte(byteBuffer.get(0));
                x += anInt;
            }
            //Log.i(TAG,"x:"+x);
            dataOutputStream.flush();
            Log.i(TAG,"写入文件成功:");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //写到文件里，二进制方式写入
    private void writeEcgDataToBinaryFile(int[] ints) {
        try {
            if (fileOutputStream==null){
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+ MyUtil.getECGFileNameDependFormatTime(new Date())+".ecg";
                //String filePath = getCacheDir()+"/"+System.currentTimeMillis()+".ecg";
                Log.i(TAG,"filePath:"+filePath);
                fileOutputStream = new FileOutputStream(filePath,true);
                dataOutputStream = new DataOutputStream(fileOutputStream);
                byteBuffer = ByteBuffer.allocate(2);
                Log.i(TAG,"ok:");
            }
            String x="";
            for (int anInt : ints) {
                byteBuffer.clear();
                byteBuffer.putShort((short) anInt);
                dataOutputStream.writeByte(byteBuffer.get(1));
                dataOutputStream.writeByte(byteBuffer.get(0));
                x += anInt;
            }
            //Log.i(TAG,"x:"+x);
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //当前包传输成功，进行下一个包传输
    private void uploadNextPackageData(){
        mUploadFileCountIndex++;
        if (mUploadFileCountIndex<mAllFileCount){
            String offsetHexLenght = DeviceOffLineFileUtil.getFormatHexFileLenght(8, mOneUploadMaxByte*mUploadFileCountIndex);
            String fileHexLenght = DeviceOffLineFileUtil.getFormatHexFileLenght(8, mOneUploadMaxByte);
            String startOrder = "FF05000e"+offsetHexLenght+fileHexLenght;
            String deviceOrder = startOrder+DeviceOffLineFileUtil.readDeviceSpecialFileBeforeAddSum(startOrder)+"16";
            Log.i(TAG,mUploadFileCountIndex+"分段上传deviceOrder:"+deviceOrder);
            sendReadDeviceOrder(deviceOrder);
        }
        else {
            if (mFileLastRemainder>0){
                String offsetHexLenght = DeviceOffLineFileUtil.getFormatHexFileLenght(8, mOneUploadMaxByte*mUploadFileCountIndex);
                String fileHexLenght = DeviceOffLineFileUtil.getFormatHexFileLenght(8, mFileLastRemainder);
                String startOrder = "FF05000e"+offsetHexLenght+fileHexLenght;
                String deviceOrder = startOrder+DeviceOffLineFileUtil.readDeviceSpecialFileBeforeAddSum(startOrder)+"16";
                Log.i(TAG,"余数上传deviceOrder:"+deviceOrder);
                sendReadDeviceOrder(deviceOrder);
                //mFileLastRemainder = 0;
            }
            else {
                //没有余数，刚好整除，则文件数据传完，isStartUploadData置为false
                isStartUploadData = false;
            }
        }
    }

    private void dealWithDeviceDeleteFile(String allHexString) {
        String[] split = allHexString.split(" ");
        final int deleteState = Integer.parseInt(split[4], 16);
        /*
        * 0：表示删除成功失败；
            1：没有该文件；
            2：删除成功失败。
            */

        Log.i(TAG,"deleteState:"+deleteState);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recive_text.setText("删除deleteState:"+deleteState);
            }
        });

    }

    private void dealWithDeviceDeleteAllFile(String allHexString) {
        String[] split = allHexString.split(" ");
        final int deleteState = Integer.parseInt(split[4], 16);
        /*
        * 0：表示删除成功失败；
            1：没有该文件；
            2：删除成功失败。
            */

        Log.i(TAG,"deleteState:"+deleteState);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recive_text.setText("一键删除deleteState:"+deleteState);
            }
        });

    }

    private  String parseHexStringToBigInt(String hexString){
        BigInteger bigInteger = new BigInteger(hexString, 16);
        return bigInteger.toString();
    }

}
