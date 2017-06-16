package com.test.utils;

import android.util.Log;

import com.test.objects.HeartRateResult;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class DiagnosisNDK {

	static {
		//System.loadLibrary("Guan");// shangguan ecg
		// System.loadLibrary("Wu");//wubo ppg
		System.loadLibrary("ecg");
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

	public native static HeartRateResult getEcgResult(double[] source,
                                                      long len, int s_rate);

/*	public native static PluseRateP_PP getPpgResult(double[] source, int len,
			int s_rate, int SBP, int DBP, int height, int age, int SPO,
			int weight);*/
	
	
	public static int []test = {100,40,67,89,10,100,56,65,100,100,
            100,40,67,89,10,100,56,65,100,100,
            100,40,67,89,10,100,56,65,100,100,
            100,40,67,89,10,100,56,65,100,100,
            100,40,67,89,10,100,56,65,100,100,
            100,40,67,89,10,100,56,65,100,100,
            100,40,67,89,10,100,56,65,100,100,
            100,40,67,89,10,100,56,65,100,100,
            100,40,67,89,10,100,56,65,100,100,
            100,40,67,89,10,100,56,65,100,100,
            100,40,67,89,10,100,56,65,100,100,
            100,40,67,89,10,100,56,65,100,100,
            100,40,67,89,10,100,56,65,100,100,
            100,40,67,89,10,100,56,65,100,100,
            100,40,67,89,10,100,56,65,100,100};
	

	/*
	 * 涵数名： getEcgHeart
	 * 参数1： source   被分析数据
	 * 参数2： len      被分析数据长度
	 * 参数3： s_rate   采样率 
	 * 返回值： 返回心率值
	 */
	public native static int getEcgHeart(int[] source,
			int len, int s_rate);
	
	/*
	 * 涵数名：                    getPedo
	 * 参数1：     in   source   被分析数据
	 * 参数2：     in   len      被分析数据长度
	 * 参数3：    out  arrayout  分析得出的结果，    arrayout[0] 返回状态     0静止，1走路，2跑步     arrayout[1] 返回步数
	 * 返回值：                     void
	 */
	public native static void getPedo(byte[] source, int len, int[] arrayout);
	
	/*
	 * 涵数名：                    getkcal
	 * 参数1：                     sex   性别  1为男性，其它女性
	 * 参数2：                      hr    心率
	 * 参数3：                     age   年龄
	 * 参数4       weight 体重 单位kg
	 * 参数5       time   时间  单位分钟 不给出的话默认按数据计算得出
	 * 返回值：                     消耗卡路里
	 */
	public native static float getkcal(int sex,int hr, int age,float weight,float time);
	
    public static int ecgHeart(int[] source, int len, int rate) {
		Log.d("ndk's c++", "len=" + len + " s_rate=" + rate);
    	return getEcgHeart(source,len, rate);
    }
    
    public static void AnalysisPedo(byte[] source, int len, int[] aout) {
    	getPedo(source, len, aout);
    	Log.d("before ndk's c++", "aout[0]=" + aout[0] + " aout[1]=" + aout[1]);
    }

    public static HeartRateResult AnalysisEcg(int[] source, int len, int s_rate) {
		double[] ecg = new double[len];

//		ecg = fir(source);
		for(int i=0;i<len;i++)
		{
			ecg[i]=source[i];
		}

//		ecg = fir(test);
//		len = test.length;
		s_rate = 150;
		
		
		Log.d("before ndk's c++", "len=" + len + " s_rate=" + s_rate);
        

		HeartRateResult result = getEcgResult(ecg, len, s_rate);

		int abnormal = result.RR_Apb + result.RR_Pvc + result.RR_Iovp
				+ result.RR_Boleakage + result.RR_Kuanbo + result.RR_2
				+ result.RR_3 + result.RR_ss + result.RR_Standstill;
		
		Log.d("NDK", "RR_sum=" + result.RR_Sum +" RR_Apb="+result.RR_Apb+" RR_pvc="+result.RR_Pvc+" RR_Iovp="+result.RR_Iovp
				+" RR_Bolekage="+result.RR_Boleakage+" RR_kuanbo="+result.RR_Kuanbo+" RR_2="+result.RR_2+" RR_3="+result.RR_3
				+" RR_SS="+ result.RR_ss+ " RR_Standstill="+result.RR_Standstill);
		
		Log.d("xxxxxxxxxxx", "RR_PNN50="+result.RR_PNN50 +" RR_SDNN="+result.RR_SDNN +" RR_HRVI="+result.RR_HRVI);
		Log.d("LFHF", "LF="+result.LF +" LF="+result.HF);

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
/*		double[] flx0 = new double[23];
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
*/
		double[] ecg = new double[len];
		for(int i=0; i<len; i++)
		{
			ecg[i] =d[i];
		}
		return ecg;
		
	}
	
}
