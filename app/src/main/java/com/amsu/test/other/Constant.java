/*
 *@author Dawin,2015-3-5
 *
 *
 *
 */
package com.amsu.test.other;

public class Constant
{
	public final static String END = "end";
	public final static String HOST_SPOT_SSID = "ESP8266";
	public final static String HOST_SPOT_PASS_WORD = "0123456789";

	/*1	0xFF	固定同步头	Hex
2	0x01	标志字，命令号	Hex
3	0x00	包长高字节。整个包的长度	Hex
4	0x06	包长低字节。整个包的长度	Hex
5	0x**	累加和，为同步头开始到前一位的累加和，取最低字节	Hex
6	0x16	结束符	Hex

	* */

	public final static String readDeviceVersion = "FF0100060616";
	public final static String readDeviceID = "FF0200060716";
	public final static String readDeviceFileList = "FF0300060816";


	public final static String readDeviceSpecialFileLength(String fileName){
		return "";
	}

	//将字节数组转换为16进制字符串
	public static String BinaryToHexString(byte[] bytes) {
		String hexStr = "0123456789ABCDEF";
		String result = "";
		String hex = "";
		for (byte b : bytes) {
			hex = String.valueOf(hexStr.charAt((b & 0xF0) >> 4));
			hex += String.valueOf(hexStr.charAt(b & 0x0F));
			result += hex + " ";
		}
		return result;
	}

	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	public static String bytesToHexString(byte[] src){
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

}