package com.test.objects;

/**
 * 该类用于记录心电分析的结果
 */

public class HeartRateResult {
	/**
	 * RR间期总数
	 */
	public int RR_Sum = 0; // 0,RR间期总数

	/**
	 * 正常
	 */
	public int RR_Normal = 0; // 1,正常

	/**
	 * 心动过速
	 */
	public int RR_Tachycardia = 0; // 2,心动过速

	/**
	 * 心动过缓
	 */
	public int RR_Bradycardia = 0; // 3,心动过缓

	/**
	 * 停博
	 */
	public int RR_Standstill = 0; // 4,停博

	/**
	 * 房性早搏
	 */
	public int RR_Apb = 0; // 5,房性早搏

	/**
	 * 室性早搏
	 */
	public int RR_Pvc = 0; // 6,室性早搏,PVC

	/**
	 * 二联律
	 */
	public int RR_2 = 0; // 7,二联律

	/**
	 * 三联律
	 */
	public int RR_3 = 0; // 8,三联律

	/**
	 * 插入性室早
	 */
	public int RR_Iovp = 0; // 9,插入性室早

	/**
	 * 成对早搏
	 */
	public int RR_double = 0; // 10,成对早搏

	/**
	 * 室上速
	 */
	public int RR_sss = 0; // 11,室上速

	/**
	 * 室速
	 */
	public int RR_ss = 0; // 12,室速

	/**
	 * 漏博
	 */
	public int RR_Boleakage = 0; // 13,漏博

	/**
	 * 宽博
	 */
	public int RR_Kuanbo = 0; // 14,宽博

	/**
	 * RONT
	 */
	public int RR_RONT = 0; // RONT

	/**
	 * 分析结果
	 */
	public int RR_Result = 0; // 分析结果

	/**
	 * 是否心率失常
	 */
	public boolean RR_Abnormal = false;// 是否心率失常

	/**
	 * PNN50
	 */
	public int RR_PNN50 = 0;
	/**
	 * RR_SDNN
	 */
	public int RR_SDNN = 0;
	/**
	 * 三角指数
	 */
	public int RR_HRVI = 0;
	
	public double LF = 0.0;
	public double HF = 0.0;

	public int[] RR_list;

	public void setRrlist(int[] data, int len) {

		RR_list = new int[len];
		
		for (int i = 0; i < len; i++) {
			RR_list[i] = data[i];
		}
	}


	@Override
	public String toString() {
		return "HeartRateResult{" +
				"RR_Sum=" + RR_Sum +
				", RR_Normal=" + RR_Normal +
				", RR_Tachycardia=" + RR_Tachycardia +
				", RR_Bradycardia=" + RR_Bradycardia +
				", RR_Standstill=" + RR_Standstill +
				", RR_Apb=" + RR_Apb +
				", RR_Pvc=" + RR_Pvc +
				", RR_2=" + RR_2 +
				", RR_3=" + RR_3 +
				", RR_Iovp=" + RR_Iovp +
				", RR_double=" + RR_double +
				", RR_sss=" + RR_sss +
				", RR_ss=" + RR_ss +
				", RR_Boleakage=" + RR_Boleakage +
				", RR_Kuanbo=" + RR_Kuanbo +
				", RR_RONT=" + RR_RONT +
				", RR_Result=" + RR_Result +
				", RR_Abnormal=" + RR_Abnormal +
				", RR_PNN50=" + RR_PNN50 +
				", RR_SDNN=" + RR_SDNN +
				", RR_HRVI=" + RR_HRVI +
				", LF=" + LF +
				", HF=" + HF +
				'}';
	}
}
