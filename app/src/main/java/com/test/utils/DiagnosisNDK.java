package com.test.utils;

import android.util.Log;

import com.test.objects.HeartRateResult;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class DiagnosisNDK {

	static {
		System.loadLibrary("ecg");// shangguan ecg
		// System.loadLibrary("Wu");//wubo ppg
	}

	private static void writeToFile(int[] source, String filename) {

		DataOutputStream outs;
		try {
			outs = new DataOutputStream(new FileOutputStream(filename));
			int length = source.length;
			Log.d("writeToFile", "source[0]=" + source[0]);
			for (int i = 0; i < length; i++) {
				outs.writeBytes(source[i] + "\n");
			}
			outs.close();
		} catch (IOException e) {

			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void writeToFile(double[] source, String filename) {

		DataOutputStream outs;
		try {
			outs = new DataOutputStream(new FileOutputStream(filename));
			int length = source.length;
			Log.d("writeToFile", "source[0]=" + source[0]);
			for (int i = 0; i < length; i++) {
				outs.writeBytes(source[i] + "\n");
			}
			outs.close();
		} catch (IOException e) {

			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public native static HeartRateResult getEcgResult(double[] source, long len, int s_rate);

	public static HeartRateResult AnalysisEcg(int[] source, int len, int s_rate) {
		double[] ecg = new double[len];

		ecg = fir(source);

		Log.d("before ndk's c++", "len=" + len + " s_rate=" + s_rate);

		HeartRateResult result = getEcgResult(ecg, (long) len, s_rate);

		int abnormal = result.RR_Apb + result.RR_Pvc + result.RR_Iovp
				+ result.RR_Boleakage + result.RR_Kuanbo + result.RR_2
				+ result.RR_3 + result.RR_ss + result.RR_Standstill;
		Log.d("NDK", "RR_sum=" + result.RR_Sum);
		if (abnormal > 0) {
			result.RR_Abnormal = true;
			result.RR_Normal = result.RR_Sum - abnormal;
			Log.d("Normal and Abnormal", "sum=" + result.RR_Sum + " normal="
					+ result.RR_Normal);
		} else {
			result.RR_Normal = result.RR_Sum;
		}

		// Log.d("1234567890-=",
		// " rrlist.length"+result.RR_list.length+" result.RR_PNN50"+result.RR_PNN50+" result.RR_HRVI"+result.RR_HRVI+" result.RR_SDNN"+result.RR_SDNN);

		return result;
	}


	/**
	 * 23阶FIR滤波器，用于计算心率值所用，该滤波器更能滤除信号的毛刺，有利于计算R波峰。
	 * 
	 * @param d
	 * @return
	 */

	private static double[] fir(int d[]) {
		int len = d.length;
		double[] flx0 = new double[23];
		double fly0;

		double[] a = { 0.0008, 0.0025, 0.0057, 0.0109, 0.0183, 0.0280, 0.0393,
				0.0513, 0.0629, 0.0724, 0.0788, 0.0810, 0.0788, 0.0724, 0.0629,
				0.0513, 0.0393, 0.0280, 0.0183, 0.0109, 0.0057, 0.0025, 0.0008 };
		double[] ecg = new double[len];
		// 首次的特殊处理
		for (int j = 0; j < 22; j++) {
			flx0[j] = d[j];
		}
		for (int i = 0; i < len; i++) {
			try {
				for (int j = 0; j < 22; j++) {
					flx0[22 - j] = flx0[21 - j];
				}
				flx0[0] = d[i];
				fly0 = 0;
				for (int j = 0; j < 23; j++) {
					fly0 += a[j] * flx0[j];
				}
			} catch (Exception e) {
				fly0 = 0;
			}
			ecg[i] = fly0;
		}

		return ecg;
	}
}
