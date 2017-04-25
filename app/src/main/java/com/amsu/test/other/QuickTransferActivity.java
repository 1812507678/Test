/*
 *@author Dawin,2015-3-5
 *
 *
 *
 */
package com.amsu.test.other;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amsu.test.R;

/*测试需要两部安卓手机A,B。A手机创建WIFI热点作为服务器，B手机连接A手机WIFI热点，作为客户端。*/
//A手机服务器    接收数据步骤:1点击创建Wifi热点2点击"turn_on_receiver"接收数据
//B手机客户端    发送数据步骤：1点击连接Wifi2点击"turn_on_send"发送数据
public class QuickTransferActivity extends Activity
{
	/** Called when the activity is first created. */
	TextView content;
	// 接收到的内容
	TextView reciverTv;
	Button mBtn3, mBtn4;
	WifiAdmin mWifiAdmin;
	WifiApAdmin wifiAp;
	Context context;
	final static String TAG = "WifiConnection";
	private TextView serviceInfo;
	private PrintWriter out;
	private BufferedReader bufferedReader;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		content = (TextView) this.findViewById(R.id.content);
		reciverTv = (TextView) this.findViewById(R.id.reciver);
		mBtn3 = (Button) findViewById(R.id.button3);
		mBtn4 = (Button) findViewById(R.id.button4);
		mBtn3.setText("点击连接Wifi");
		mBtn4.setText("点击创建Wifi热点");
		serviceInfo = (TextView) findViewById(R.id.service);
		context = this;
		// 连接Wifi
		mBtn3.setOnClickListener(new Button.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub

				mWifiAdmin = new WifiAdmin(context)
				{

					@Override
					public void myUnregisterReceiver(BroadcastReceiver receiver)
					{
						// TODO Auto-generated method stub
						unregisterReceiver(receiver);
					}

					@Override
					public Intent myRegisterReceiver(BroadcastReceiver receiver, IntentFilter filter)
					{
						// TODO Auto-generated method stub
						registerReceiver(receiver, filter);
						return null;
					}

					@Override
					public void onNotifyWifiConnected()
					{
						// TODO Auto-generated method stub
						Log.v(TAG, "have connected success!");
						Log.v(TAG, "###############################");
					}

					@Override
					public void onNotifyWifiConnectFailed()
					{
						// TODO Auto-generated method stub
						Log.v(TAG, "have connected failed!");
						Log.v(TAG, "###############################");
					}
				};
				mWifiAdmin.openWifi();
				// 连的WIFI热点是用WPA方式保护
				mWifiAdmin.addNetwork(mWifiAdmin.createWifiInfo(Constant.HOST_SPOT_SSID, Constant.HOST_SPOT_PASS_WORD, WifiAdmin.TYPE_WPA));
			}
		});
		// 创建WIFI热点
		mBtn4.setOnClickListener(new Button.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				wifiAp = new WifiApAdmin(context);
				wifiAp.startWifiAp(Constant.HOST_SPOT_SSID, Constant.HOST_SPOT_PASS_WORD);
			}
		});
	}

	public void onClick(View view)
	{
		// 发送数据
		if (view.getId() == R.id.button1)
		{
			WifiManager wifiManage = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
			DhcpInfo info = wifiManage.getDhcpInfo();
			WifiInfo wifiinfo = wifiManage.getConnectionInfo();
			String ip = intToIp(wifiinfo.getIpAddress());
			String serverAddress = intToIp(info.serverAddress);
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
			// new Date()为获取当前系统时间
			String message = "Hello this is dawin ! send time:" + df.format(new Date());
			new Sender(serverAddress, message).start();
			String msg = "ip:" + ip + "serverAddress:" + serverAddress + info;
			serviceInfo.setText(msg);
			Log.w(TAG, msg);

		}
		// 接收数据
		else if (view.getId() == R.id.button2) {
			Toast.makeText(QuickTransferActivity.this,"接收数据",Toast.LENGTH_SHORT).show();
			/*if (service != null) {
				service.close();
				service = null;
				//service.close();
			}
			service = new Receiver();
			service.start();*/
			//test();
		}
	}



	StringBuffer strBuffer = new StringBuffer();

	/** 将获取的int转为真正的ip地址,参考的网上的，修改了下 */
	private String intToIp(int i)
	{
		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
	}

	WifiManager mWifiManager;

	// 打开WIFI
	public void openWifi()
	{
		if (mWifiAdmin != null)
		{
			mWifiAdmin.openWifi();
			return;
		}
		if (mWifiManager == null)
		{
			mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		}
		if (!mWifiManager.isWifiEnabled())
		{
			mWifiManager.setWifiEnabled(true);
		}
	}

	// 关闭WIFI
	public void closeWifi()
	{
		if (mWifiAdmin != null)
		{
			mWifiAdmin.closeWifi();
			return;
		}
		if (mWifiManager == null)
		{
			mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		}
		if (mWifiManager.isWifiEnabled())
		{
			mWifiManager.setWifiEnabled(false);
		}
	}


	public void sendData(View view) {
		String message = "sendData";
		if (out!=null){
			out.write(message);
			Log.i(TAG,"我说：" + message);
			out.flush();
		}
	}

	/* 客户端发送数据 */
	class Sender extends Thread {
		String serverIp;
		String message;

		Sender(String serverAddress, String message) {
			super();
			serverIp = serverAddress;
			this.message = message;
		}

		public void run()
		{
			Socket sock = null;
			try {
				// 声明sock，其中参数为服务端的IP地址与自定义端口
				Log.i(TAG,"serverIp：" + serverIp);
				sock = new Socket(serverIp, 8080);
				Log.w("WifiConnection", "I am try to writer" + sock);


			} catch (UnknownHostException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				// 向服务器端发送消息
				out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sock.getOutputStream())), true);
				//out.println(message);
				//Log.i(TAG,"我说：" + message);

				// 接收来自服务器端的消息
				/*BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				String msg = br.readLine();

				Log.i(TAG,"服务器msg：" + msg);*/
				bufferedReader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				String msg;

				Log.i(TAG,"socket.isClosed()：" + sock.isClosed());
				Log.i(TAG,"socket.isConnected()：" + 	sock.isConnected());
				Log.i(TAG,"socket.isInputShutdown()：" + 	sock.isInputShutdown());

				char[] bytes = new char[1024];
				int length;
				while ((length = bufferedReader.read(bytes))!=-1) {
					//length = bufferedReader.read(bytes);
					Log.i(TAG,"length：" + length);
					String s = String.valueOf(bytes,0,length);
					Log.i(TAG,"服务器msg：" + s);
					//output.append("服务器说：" + msg + "\n");

				}

				// 关闭流
				/*out.close();
				out.flush();
				bufferedReader.close();*/
				// 关闭Socket
				//sock.close();
			}
			catch (Exception e)
			{
				LogUtil.e("发送错误:[Sender]run()" + e.getMessage());
			}
		}
	}




}
