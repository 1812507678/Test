package com.amsu.test.other;

/**
 * Created by HP on 2016/12/12.
 */
public class EcgFilterUtil {
    private static final int CHAN_NUM=15;
    // 浣庨�氭护娉�
    static double[] ecglpa = { -0.025752007967429096,-0.0039873792682974382,0.027675893138943142,0.039063776734442571,
            0.011878523715990338,-0.038900255473135602,-0.068602874690148571,-0.033201826226201853,
            0.074464686451774523,0.21119272143920881,0.30616874214485318,0.30616874214485318,
            0.21119272143920881,0.074464686451774523,-0.033201826226201853,-0.068602874690148571,
            -0.038900255473135602,0.011878523715990338,0.039063776734442571,0.027675893138943142,
            -0.0039873792682974382,-0.025752007967429096
    };

    private static final int PPG_LPA_LEN=22;
    private static double[][] hx0 = new double[CHAN_NUM][PPG_LPA_LEN];

    // 楂橀�氭护娉�
    static double[] ecghpa = { -0.027754239887448615,-0.031025013327989945,-0.035028245514272062,-0.040056245259505345,
            -0.046581286468608291,-0.055419214321448383,-0.068111192804634471,-0.087959876284686156,
            -0.12355239243325551,-0.20637525614012925,-0.61980824822554015,0.61980824822554015,
            0.20637525614012925, 0.12355239243325551,0.087959876284686156,0.068111192804634471,
            0.055419214321448383,0.046581286468608291, 0.040056245259505345,0.035028245514272062,
            0.031025013327989945,0.027754239887448615
    };

    private static final int ECG_HPASS_LEN =22;
    private static double[][] lx0 = new double[CHAN_NUM][ECG_HPASS_LEN];

    public static int miniEcgFilterLp(int d,int leadno) {
        double ly0;

        int i;
        //try {
        for (i = 0; i < PPG_LPA_LEN-1; i++)
            lx0[leadno][PPG_LPA_LEN-1 - i] = lx0[leadno][PPG_LPA_LEN-2 - i];
        lx0[leadno][0] = d;
        ly0 = 0;
        for (i = 0; i < PPG_LPA_LEN; i++)
            ly0 += ecglpa[i] * lx0[leadno][i];
        //} catch (_com_error Error) {
        //	mfly0 = 0;
        //}

        return (int) ly0;
    }


    public static int miniEcgFilterHp(int d,int leadno) {
        double hy0;

        int i;
        for (i = 0; i < ECG_HPASS_LEN-1; i++)
            hx0[leadno][ECG_HPASS_LEN-1 - i] = hx0[leadno][ECG_HPASS_LEN-2 - i];
        hx0[leadno][0] = d;
        hy0 = 0;
        for (i = 0; i < ECG_HPASS_LEN; i++)
            hy0 += ecghpa[i] * hx0[leadno][i];

        return (int) hy0;
    }
}
