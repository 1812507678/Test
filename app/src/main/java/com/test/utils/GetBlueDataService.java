package com.test.utils;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/*
 * 该类用于后台与蓝牙通信服务，取得数据，继承Service
 */

public class GetBlueDataService extends Service {
	private HandleThread thread;
	// 心电采样率
	private static final int ECGSampleRate = 250;
	// 脉搏采样率
	private static final int PPGSampleRate = 60;

	static public int xueyang = 127; // 血氧值默认127，表示无效数据

	// 接受缓存队列长度
	public static int BufferSize = 10000;

	// 文件存储长度
	private static final int FILELEN = 1000000;
	public static int ecgFileSum = 0; // 100,000点
	public static int ppgFileSum = 0;
	// 数据存储到文件标志位
	public static boolean DATA_TOFILE_FLAG = false;// startflag
	// 数据存储到队列标志位
	public static boolean DATA_TOQUEUE_FLAG = false;// do flag

	// private static final String PATH = "/data/data/com.test/files/ecg.txt";

	// 蓝牙数据包，包头标志位
	private int FF = 0xFF;
	// 生理参数
	public static int[] Data = new int[6];
	// public static int heartRate=0; Data[0]
	// public static int temperature; Data[1]
	// public static int pulseRate; Data[2]
	// public static int sop2; Data[3]
	// public static int signalLevel; Data[4]
	// public static int guangZhuTu; Data[5]

	// 23阶FIR滤波器，用于计算心率值所用，该滤波器更能滤除信号的毛刺，有利于计算R波峰。
	private static double fly0;
	private static double[] flx0 = new double[23];
	private double[] a = { 0.0008, 0.0025, 0.0057, 0.0109, 0.0183, 0.0280,
			0.0393, 0.0513, 0.0629, 0.0724, 0.0788, 0.0810, 0.0788, 0.0724,
			0.0629, 0.0513, 0.0393, 0.0280, 0.0183, 0.0109, 0.0057, 0.0025,
			0.0008 };

	int fir(int d) {
		int i;
		try {
			for (i = 0; i < 22; i++)
				flx0[22 - i] = flx0[21 - i];
			flx0[0] = d;
			fly0 = 0;
			for (i = 0; i < 23; i++)
				fly0 += a[i] * flx0[i];
		} catch (Exception e) {
			fly0 = 0;
		}

		return (int) fly0;
	}

	public class BlueBinder extends Binder {
		public GetBlueDataService getServiceObject() {
			return GetBlueDataService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	private BlueBinder mBinder = new BlueBinder();

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("startHandle", "in create");
		// 启动接受线程
		// startHandle();

	}

	// 退出
	public void stopHandle() {
		Log.d("Service", "stopHandle");
		thread.bRun = false;
	}

	public void startHandle() {

		thread = new HandleThread();
		if (!thread.isAlive()) {
			Log.d("in start handle", "no alive");

			thread.start();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("Service", "Destory");
		// 关闭线程
		stopHandle();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

	}

	// /////////////////////////////////////////////////////////////

	private class HandleThread extends Thread {
		private boolean bRun = false;
		// ecg缓存数据长度
		private int ECGBufferLength = 1000;
		// ppg缓存数据长度
		private int PPGBufferLength = 240;

		// 缓存数组
		private int[] ECGBuffer = new int[ECGBufferLength];
		private int[] PPGBuffer = new int[PPGBufferLength];
		// 缓存数组下标
		private int ecgBufferPointer = 0;
		private int ppgBufferPointer = 0;
		// 发送数据包序号
		private int transmit_no = 0;
		private byte[] ecgSendBuffer = new byte[2000];
		// 时间截字段数据
		private String[] time_stamp = new String[6];
		private String[] frame_time_stamp = new String[6];
		private String _USERNAME;
		private String _PASSWORD;
		private String _ip = "http://210.75.252.106:4080";
		int packageLength = 0;
		// 启动
		@Override
		public void run() {

		}

		// 读取数据包包头
		public int readHeader(InputStream input) throws IOException {
			int des = 0;
			boolean flag = true;
			while (flag) {

				des = input.read();
				//Log.d(TAG, "des=" + des);

				if (des == FF) {

					return input.read();

				}
			}
			// 如果读出现异常，是否需要退出
			bRun = false;
			return 0;
		}

		// 读取数据
		public boolean readData(InputStream input, byte[] buf, int offset,
				int num) throws IOException {
			boolean flag = true;
			int len;
			while (num > 0) {

				len = 0;
				len = input.read(buf, offset, num);
				if (len > 0) {
					offset = offset + len;
					num = num - len;
				}

			}
			return flag;
		}

		String TAG = "GetBlueDataService";


		// 发送数据
		private boolean sendDataToServer() {

			intToByte(ecgSendBuffer, ECGBuffer, 1000);
			getTimer(time_stamp);

			getTimer(frame_time_stamp);
			String content_head = "VER=0.1&CMD=rawdata&UID=" + _USERNAME
					+ "&PS=" + _PASSWORD
					+ "&DATA_TYPE=ECG&CHANNEL=1&CHANNEL_SUM=1&TIME_STAMP="
					+ time_stamp[0] + time_stamp[1] + time_stamp[2]
					+ time_stamp[3] + time_stamp[4] + time_stamp[5]
					+ "&TRANSMIT_NO=" + transmit_no + "&FRAME_TIME_STAMP="
					+ frame_time_stamp[0] + frame_time_stamp[1]
					+ frame_time_stamp[2] + frame_time_stamp[3]
					+ frame_time_stamp[4] + frame_time_stamp[5]
					+ "&TIME_INTERVAL=4&DATA_LEN=" + Integer.toString(2000)
					+ "\r\n";
			String commandLength = Integer.toString(content_head.length());
			// String
			// contentLength=Integer.toString(content_head.length()+EACH_TIME_NUM);
			byte[] content_head_byte = content_head.getBytes();
			byte[] content = new byte[content_head_byte.length + 2000];
			System.arraycopy(content_head_byte, 0, content, 0,
					content_head_byte.length);
			System.arraycopy(ecgSendBuffer, 0, content,
					content_head_byte.length, 2000);

			String res = post(_ip, content, commandLength);
			if (res != null) {
				int buf1 = res.indexOf("RES");

				char des = res.charAt(buf1 + 4);
				if (des == '1') {
					return true;
				}
			}
			return false;
		}

		private void intToByte(byte[] bbuf, int[] ibuf, int len) {
			for (int i = 0, j = 0; i < len; i++) {
				bbuf[j++] = (byte) ((ibuf[i] >> 8) & 0xff);
				bbuf[j++] = (byte) ((ibuf[i]) & 0xff);
			}
		}

		private void getTimer(String[] time_stamp) {

			Calendar c = Calendar.getInstance();
			int mYear = c.get(Calendar.YEAR);
			int mMonth = c.get(Calendar.MONTH);
			int mDay = c.get(Calendar.DAY_OF_MONTH);
			int mHour = c.get(Calendar.HOUR_OF_DAY);
			int mMinute = c.get(Calendar.MINUTE);
			int mSecond = c.get(Calendar.SECOND);

			time_stamp[0] = Integer.toString(mYear);
			mMonth++;
			if (mMonth < 10) {
				time_stamp[1] = '0' + Integer.toString(mMonth);
			} else {
				time_stamp[1] = Integer.toString(mMonth);
			}
			if (mDay < 10) {
				time_stamp[2] = '0' + Integer.toString(mDay);
			} else {
				time_stamp[2] = Integer.toString(mDay);
			}
			if (mHour < 10) {
				time_stamp[3] = '0' + Integer.toString(mHour);
			} else {
				time_stamp[3] = Integer.toString(mHour);
			}
			if (mMinute < 10) {
				time_stamp[4] = '0' + Integer.toString(mMinute);
			} else {
				time_stamp[4] = Integer.toString(mMinute);
			}
			if (mSecond < 10) {
				time_stamp[5] = '0' + Integer.toString(mSecond);
			} else {
				time_stamp[5] = Integer.toString(mSecond);
			}
		}

		private String post(String uri, byte[] data, String commandLength) {
			String des = null;
			String strResult = new String();
			HttpParams params = new BasicHttpParams();
			params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 20000)
					.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
							20000)
					.setBooleanParameter(
							CoreConnectionPNames.STALE_CONNECTION_CHECK, true);

			DefaultHttpClient httpclient = new DefaultHttpClient();

			// HttpHost proxy=new HttpHost("10.0.0.172",80);
			// httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
			// proxy);

			HttpPost httpRequest = new HttpPost(uri);
			httpclient.setParams(params);
			/*
			 * try { ByteArrayEntity reqEntity=new ByteArrayEntity(data);
			 * httpRequest.setHeader("Command-Length",commandLength);
			 * httpRequest.setEntity(reqEntity); HttpResponse httpResponse =
			 * httpclient.execute(httpRequest);
			 * 
			 * if(httpResponse.getStatusLine().getStatusCode() ==
			 * HttpStatus.SC_OK){ strResult =
			 * EntityUtils.toString(httpResponse.getEntity()); }
			 * des=Uri.decode(strResult);
			 * 
			 * } catch (ClientProtocolException e) { des=null; } catch
			 * (IOException e) { des=null; } catch (Exception e) { des=null; }
			 */
			des = new String("RES11111111");

			return des;
		}


		// 清除历史数据
		private void init() {
			ecgFileSum = 0;
			ppgFileSum = 0;
			// clearFile();
		}

		/*
		 * private void clearFile(){ deleteFile(ecgFile); deleteFile(ppgFile); }
		 */
		// private void AddToECGFile(){
		// File write = new File(PATH);
		// try {
		// BufferedWriter bw = new BufferedWriter( new FileWriter(write));
		// for(int i=0; i<ECGBufferLength; i++){
		// bw.write(Integer.toString(ECGBuffer[i++])+"\r\n");
		// }
		// bw.close();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }


		// 计算脉率
		private int countPulseRate(int[] ppg, int len, int s_rate) {
			int result = 0;
			List<Integer> P = new ArrayList<Integer>();
			int[] diff = new int[len - 1];
			int[] soc = new int[len - 1];

			try {
				for (int i = 0; i < len - 1; i++) {
					diff[i] = ppg[i + 1] - ppg[i];
				}

				int max = diff[0];
				for (int i = 0; i < len - 1; i++) {
					if (max < diff[i])
						max = diff[i];
				}

				float threshold = (float) (max * 0.6);

				int n = 0;
				for (int i = 0; i < len - 3; i++) {
					if (diff[i] < threshold && diff[i + 1] < threshold
							&& diff[i + 2] >= threshold
							&& diff[i + 3] >= threshold)
						soc[n++] = i;
				}

				for (int i = 0; i < n; i++) {
					int p = soc[i];
					int res = ppg[p];
					P.add(p);
					for (int j = p - 20; j < p && j > 0; j++) {
						if (res > ppg[j]) {
							res = ppg[j];
							P.set(P.size() - 1, j);
						}
					}
				}
				int[] PP = new int[P.size() - 1];
				for (int i = 0; i < PP.length; i++) {
					PP[i] = P.get(i + 1) - P.get(i);
				}

				int size = PP.length;
				int sum = 0;
				for (int j = 0; j < size; j++) {
					sum = sum + PP[j];
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

		// 计算心率
		private int countEcgRate(int[] ecg, int len, int s_rate) {
			int result;
			try {
				for (int i = 0; i < len; i++) {
					ecg[i] = fir(ecg[i]);
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

		public int readByte(InputStream input) {
			int str = -1;
			return str;
		}

	}
}
