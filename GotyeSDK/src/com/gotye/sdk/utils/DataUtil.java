package com.gotye.sdk.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 有关byte转换的工具类
 */
public class DataUtil {

	/** Gets int value */
	public static int getInt(byte b) {
		return b & 0xff;
	}

	/**
	 * 注释：字节数组到short的转换！
	 * 
	 * @param b
	 * @return
	 */
	public static int byteToShort(byte one, byte two) {
		return (((two & 0xff) << 8) | one & 0xff);
	}

	public static short[] bytesToShorts(byte[] b) {
		short[] s = new short[b.length / 2];
		for (int i = 0, j = 0; i < b.length - 1; i += 2, j++) {
			s[j] = (short) byteToShort(b[i], b[i + 1]);
		}
		return s;
	}

	public static int getInt(byte[] buff) {
		return ((buff[0] & 0xff) << 24) | ((buff[1] & 0xff) << 16)
				| ((buff[2] & 0xff) << 8) | (buff[3] & 0xff);
	}

	public static byte[] shortToBytes(short val) {
		byte[] buff = new byte[2];
		buff[1] = (byte) (val >> 8);
		buff[0] = (byte) val;
		return buff;
	}

	public static short bytesToShort(byte[] buff) {
		return (short) (((buff[1] & 0xff) << 8) | (buff[0] & 0xff));
	}

	public static byte getBit(int index, byte c) {
		switch (index) {
		case 0:
			c = (byte) (c >> 6);
			return (byte) (c & 3);
		case 1:
			c = (byte) (c >> 4);
			return (byte) (c & 3);
		case 2:
			c = (byte) (c >> 2);
			return (byte) (c & 3);
		case 3:
			break;
		}
		return (byte) (c & 3);
	}

	public static byte setBit(int index, byte c, byte val) {
		switch (index) {
		case 0:
			val <<= 6;
			break;
		case 1:
			val <<= 4;
			break;
		case 2:
			val <<= 2;
			break;
		case 3:
			break;
		}
		c += val;
		return c;
	}

	public static byte setBits(int index, byte c, byte val) {
		// 12345
		switch (index) {
		case 0:
			val *= 81;
			break;
		case 1:
			val *= 27;
			break;
		case 2:
			val *= 9;
			break;
		case 3:
			val *= 3;
			break;
		case 4:
		}
		c = (byte) (val + (c & 0xff));
		return c;
	}

	public static byte getBits(int index, byte c) {
		// 12345
		int s;
		switch (index) {
		case 0:
			return (byte) ((c & 0xff) / 81); // 12345 / 10000
		case 1:
			return (byte) ((c & 0xff) % 81 / 27); // 12345 % 10000 / 1000
		case 2:
			return (byte) ((c & 0xff) % 27 / 9); // 12345 % 1000 / 100
		case 3:
			return (byte) ((c & 0xff) % 9 / 3); // 12345 % 100 / 10
		case 4:
			return (byte) ((c & 0xff) % 3); // 12345 % 10
		}
		return 0;
	}
//
//	public static long byteToInt(byte[] bytes) {
//		// TODO 4个字节转int
//		return 3;
//	}

	public static long byteToInt(byte a, byte b, byte c, byte d) {
		// TODO 4个字节转int
		return ((d & 0xff) << 24) | ((c & 0xff) << 16) | 
				((b & 0xff) << 8) | (a & 0xff);
	}

	public static byte[] fourToBytes(long val) {
		// TODO 转换
		byte[] data = new byte[4];
		data[0] = (byte) (val);
		data[1] = (byte) (val >> 8);
		data[2] = (byte) (val >> 16);
		data[3] = (byte) (val >> 24);
		return data;
	}
	
	public static byte[] twoToBytes(int val) {
		// TODO 转换
		byte[] data = new byte[2];
		data[0] = (byte) (val);
		data[1] = (byte) (val >> 8);
		return data;
	}
	
	public static byte[] intsToBytes(long val[]) {
		// TODO 转换
		byte[] data = new byte[val.length * 4];
		for(int i = 0; i < val.length;i++){
			System.arraycopy(fourToBytes(val[i]), 0, data, i * 4, 4);
		}
		return data;
	}
	public static byte[] shortToBytes(int value) {
		// TODO 转换
		byte[] data = new byte[2];
		data[1] = (byte) (value >> 8);
		data[0] = (byte) value;
		return data;
	}
	
	/**  
     * MD5 加密  
     */   
    public static byte[] getMD5Str(byte[] data) {   
        MessageDigest messageDigest = null;   
   
        try {   
            messageDigest = MessageDigest.getInstance("MD5");   
   
            messageDigest.reset();   
   
            messageDigest.update(data);   
        } catch (NoSuchAlgorithmException e) {   
            System.out.println("NoSuchAlgorithmException caught!");   
            System.exit(-1);   
        }   
   
        byte[] byteArray = messageDigest.digest();   
   
        StringBuffer md5StrBuff = new StringBuffer();   
   
        for (int i = 0; i < byteArray.length; i++) {               
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)   
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));   
            else   
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));   
        }   
   
        try {
			return md5StrBuff.toString().getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}   
        return null;
    }   

	public static byte[] shortArray2ByteArray(short[] data, int items) {
		byte[] retVal = new byte[items];
		for (int i = 0; i < data.length; i++) {
			retVal[2 * i] = (byte) data[i];
			retVal[2 * i + 1] = (byte) (data[i] >> 8);
		}

		return retVal;
	}
	
	public static byte[] shortArray2ByteArray(short[] data, int len, int items) {
		byte[] retVal = new byte[items];
		for (int i = 0; i < len; i++) {
			retVal[2 * i] = (byte) data[i];
			retVal[2 * i + 1] = (byte) (data[i] >> 8);
		}

		return retVal;
	}

	
	public static String inputStreamToString(InputStream in) throws IOException{
		
		InputStreamReader inr = new InputStreamReader(in, "utf-8");
		BufferedReader br = new BufferedReader(inr);
		char[] buf = new char[1024];
		int len = 0;
		StringBuffer sb = new StringBuffer();
		while((len = br.read(buf)) != -1){
			sb = sb.append(buf, 0, len);
		}
		String json = sb.toString();
		
		
		return json;
	}
	
	public static void inputStreamToFile(InputStream in, File destFile) throws Exception{
		
		FileOutputStream fout = null;
		try{
			fout = new FileOutputStream(destFile);
			BufferedOutputStream bout = new BufferedOutputStream(fout);
			BufferedInputStream bin = new BufferedInputStream(in);
			byte[] buf = new byte[2046];
			int len = 0;
			while((len = bin.read(buf)) != -1){
				bout.write(buf, 0, len);
			}
			bout.flush();
			bout.close();
		}catch(Exception e){
			throw e;
		}finally {
			if(fout != null){
				try{
					fout.close();
				}catch(Exception e){
				}
			}
		}
		
	}
}
