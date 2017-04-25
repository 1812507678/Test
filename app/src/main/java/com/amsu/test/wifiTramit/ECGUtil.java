package com.amsu.test.wifiTramit;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HP on 2016/12/6.
 */
public class ECGUtil {

    public static int[] geIntEcgaArr(String hexString,String splitSring,int startIndex,int parseLength) {
        int [] intEcgaArr = new int[parseLength];
        String[] split = hexString.split(splitSring);
        for (int i = startIndex; i < startIndex+parseLength; i++) {
            //System.out.println("i="+i+"="+split[i]);
            int parseInt = Integer.parseInt(split[i],16);
            intEcgaArr[i-startIndex] = parseInt;
        }
        return intEcgaArr;
    }

    public static List<Integer> geIntEcgaArrList(String hexString,String splitSring,int startIndex,int parseLength) {
        List<Integer> intEcgaArr = new ArrayList<>();
        String[] split = hexString.split(splitSring);
        for (int i = startIndex; i < startIndex+parseLength; i++) {
            //System.out.println("i="+i+"="+split[i]);
            int parseInt = Integer.parseInt(split[i],16);
            intEcgaArr.add(parseInt);
        }
        return intEcgaArr;
    }

    public static int intToHex(String hex){
        return 	Integer.parseInt(hex,16);
    }
    
    // 23阶FIR滤波器，用于计算心率值所用，该滤波器更能滤除信号的毛刺，有利于计算R波峰。
    private static double fly0;
    private static double[] flx0 = new double[23];
    private static double[] a = { 0.0008, 0.0025, 0.0057, 0.0109, 0.0183, 0.0280,
            0.0393, 0.0513, 0.0629, 0.0724, 0.0788, 0.0810, 0.0788, 0.0724,
            0.0629, 0.0513, 0.0393, 0.0280, 0.0183, 0.0109, 0.0057, 0.0025,
            0.0008 };

    // 计算心率
    /*public static  int countEcgRate(int[] ecg, int len, int s_rate) {
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
            result = rate;
            *//*if (rate > 60 && rate < 100) {

            } else {
                result = 0;
            }*//*

        } catch (Exception e) {
            result = 0;
        }
        return result;
    }*/

    public static int fir(int d) {
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


    public static int countEcgRate(int ecg[], int len, int s_rate) {
        int result;
        int datasum = 0;
        try {
            //      for (int i = 0; i < len; i++) {
            //  ecg[i] = fir(ecg[i]);
            //  datasum += abs(ecg[i] - 1810);
//            printf("%d\n",ecg[i]);
            //     }
      /* datasum /= len; //Ω??? ??a????±?‘??–???÷μ ????   ?°?Ω??
        if (datasum < 10)
        {
            result = 0;
            return result;
        }*/
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


            int num = len / (int)(s_rate*1.0f);//yly 20161013‘≠1s
            int[] min = new int[num];

            for (int i = 0; i < num; i++) {
                min[i] = diff[s_rate * i];
                for (int j = 0; j < (int)(s_rate*1.0f); j++) {
                    if (min[i] > diff[s_rate * i + j])
                        min[i] = diff[s_rate * i + j];
                }
            }

            float[] threshold = new float[num];
            for (int j = 0; j < num; j++)
            {
                threshold[j] = (float)((min[j]) * 0.5);
                //           printf("thd%f\n",threshold[j]);
            }

            int n = 0;
            for (int i = 0; i < num; i++) {
                for (int j = 0; j < s_rate && (s_rate * i + j) < len - 3; j++) {
                    if (diff[s_rate * i + j] > threshold[i] && diff[s_rate * i + j + 1] > threshold[i] && diff[s_rate * i + j + 2] <= threshold[i]
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
                //           printf("soc:%d",soc[i]);
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

                } else if ((R.get(j + 1) - R.get(j)) > (s_rate * 2)) {//yly 20161013原12/10，因50心率计算不对，所以改为
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
                    if(ecg[p_pos]>0.5*ecg[R.get(j)] || ecg[p_pos]>0.5*ecg[R.get(j + 1)])
                    {  //防止导联脱落时强行插入心博
                        //    	R.insert(R.begin() + j+1, p_pos);
                        R.add(j + 1, p_pos);
                        n++;
                        j++;
                    }
                }
            }
            int minRR=2000;
            int maxRR = 0;
            int minpos = 0;
            int maxpos = 0;
            int RR[] = new int[n - 1];
            for (int j = 0; j < n - 1; j++) {
                RR[j] = R.get(j + 1) - R.get(j);
                if(RR[j] > maxRR)
                {
                    maxRR = RR[j];
                    maxpos = j;
                }
                if(RR[j] < minRR)
                {
                    minRR = RR[j];
                    minpos = j;
                }
                //           printf("rr:%d",RR[j]);
            }

            int size = n - 1;
            int sum = 0;
            for (int j = 0; j < size; j++) {
                if(j != maxpos && j!= minpos && size > 2)
                {
                    sum = sum + RR[j];
                }
                else if(size <=2)
                    sum += RR[j];
            }

            int avel = sum/size;
            if(size > 2)
                avel = sum / (size-2);

            int rate = 60 * s_rate / avel;
            result = rate;
            if (rate > 40 && rate < 240) {

                result = rate;
            }
            else {
                result = 0;
            }
        }catch (Exception e) {
            result = 0;
        }
        return result;
    }

    public static  int countEcgR(int[] ecg, int len, int s_rate) {
        int result;
        int datasum = 0;
        try {
            for (int i = 0; i < len; i++) {
                ecg[i] = fir(ecg[i]);
                datasum += Math.abs(ecg[i]-1810);
            }
            datasum /= len; //解决 走基线时显示心率值 问题   取平均
            if (datasum < 10)
            {
                result = 0;
                return 0;//result;
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


            int num = len / (int)(s_rate*1.0f);//yly 20161013原1s
            int[] min = new int[num];

            for (int i = 0; i < num; i++) {
                min[i] = diff[s_rate * i];
                for (int j = 0; j < (int)(s_rate*1.5f); j++) {
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
                    if (diff[s_rate * i + j] > threshold[i] && diff[s_rate * i + j + 1] > threshold[i] && diff[s_rate * i + j + 2] <= threshold[i]
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

                } else if ((R.get(j + 1) - R.get(j)) > (s_rate * 2)) {//yly 20161013原12/10，因50心率计算不对，所以改为
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

            if(n<2)
                return 0;
            int RR[] = new int[n - 1];
            for (int j = 0; j < n - 1; j++) {
                RR[j] = R.get(j + 1) - R.get(j);
            }

            int ecgAmpSum = 0;
            for (int j = 0; j < n; j++) {
                ecgAmpSum += ecg[R.get(j)];
            }
            ecgAmpSum /= n;

            Log.d("ecgAmpSum", String.valueOf(ecgAmpSum)  );
            return ecgAmpSum;

        } catch (Exception e) {
        }
        return 0;
    }

}
