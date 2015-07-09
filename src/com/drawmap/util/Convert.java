package com.drawmap.util;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


public class Convert {  //ת�������࣬���а����ֽ�˳���ת����

		//���ֽ�����ת����С����ݣ�ת��4λ��������int����ݡ�bytes��?ת�����飬stbit��?ʼת�����ֽڵ�����
		//�����������Ҫ֪���洢������Ǵ�����
	public static int bigEndiantoint(byte[] bytes, int stbit) 
	{
		int result = 0;
		int temp = 0;
		for(int i = stbit; i< stbit+4; i++)
		{
			result <<= 8;
			result |= bytes[i]&0xff;
		}
		return result;
	}
	
	//С�����ת����int
	public static int littleEndiantoint(byte[] bytes, int stbit)
	{
		int result = 0;
		for(int i = stbit+3; i>= stbit; i--)
		{
			result <<= 8;
			result |= bytes[i]&0xff;
		}
		return result;
	}
	
	//С�����ת����short
	public static short littleEndiantoshort(byte[] bytes, int stbit)
	{
		short result = 0;
		for(int i = stbit+1; i>= stbit; i--)
		{
			result <<= 8;
			result |= bytes[i]&0xff;
		}
		return result;
	}
	
	//С�����ת����double
	public static double littleEndiantodouble(byte[] bytes, int stbit)
	{
		Long l = 0L;
		for(int i= stbit+7; i >= stbit; i--)
		{
			l <<= 8;
			l |= bytes[i]&0xff;
		}
		
		/*
		for(int i=0; i<64; i+= 8)
		{
//			int s = bytes[temp] & 0xFF;
//			if(temp<44)
//				System.out.println("-----"+s);
			if(bytes[temp] != 0)
				l |= (bytes[temp] & 0xFF) << i;
			temp++;
		}*/
		return Double.longBitsToDouble(l);
	}
	
	public static byte[] inttolittleEndian(int paramint)
	{
		byte bytes[] = new byte[4];
		for(int i = 0; i<4; i++)
		{
			bytes[i] = (byte) (paramint>>(i*8));
		}
		return bytes;
	}
	
	public static byte[] doubletolittleEndian(double paramdouble)
	{
		long l = Double.doubleToLongBits(paramdouble);
		byte bytes[] = new byte[8];
		for(int i = 0; i<8; i++)
		{
			bytes[i] = (byte) (l>>(i*8));
		}
		return bytes;
	}
	
	//��ȡ�ļ��Ĵ�С
	public static int getFileSize(String path)
	{
		
		int size = 0;  //�ļ���С
		byte[] buff = new byte[100];
		int length ;	//�����ж�ȡ���ĳ���
		try {
			FileInputStream fis = new FileInputStream(path);
			while((length=fis.read(buff)) > 0)
				size += length;		
			fis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return size;
	}
	/*
	 *����������γ�ȵ�֮��ľ���
	 *lona,lata�ֱ��ʾa��ľ��Ⱥ�γ��
	 *lonb,latb�ֱ��ʾb��ľ��Ⱥ�γ��
	 */
	public static double getDistanceA2B(double lona, double lata, double lonb, double latb){
		double EARTH_RADIUS = 6378137;
		double pk = 180/Math.PI;

		double a1 = lata / pk;
		double a2 = lona / pk;
		double b1 = latb / pk;
		double b2 = lonb / pk;
		
		double a = a1-b1;
		double b = a2-b2;
		
		double d = 2*Math.asin(Math.sqrt(Math.pow(Math.sin(a/2), 2)+
				Math.cos(a1)*Math.cos(b1)*Math.pow(Math.sin(b/2), 2)));
		d = d*EARTH_RADIUS;
//		s = Math.round(s*10000)/10000;
		return d;
	}
	
	//��WSG84��γ�����ת��Ϊī����ͶӰ��꣬���ص���X��
	public static double tranformW84toMtX(double lon){
		double coordsx = 0;
	    coordsx = lon * 20037508.34 / 180;
	   	return coordsx;
	}
	
	//��WSG84��γ�����ת��Ϊī����ͶӰ��꣬���ص���Y��
	public static double tranformW84toMtY(double lat){
		double coordsy = 0;
	    coordsy = Math.log(Math.tan((90 + lat) * Math.PI / 360)) / (Math.PI / 180);
	    coordsy = coordsy * 20037508.34 / 180;
		return coordsy;
	}
	
	
}
