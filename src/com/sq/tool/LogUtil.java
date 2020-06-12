package com.sq.tool;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


/**
 * 日志规整类
 * LogUtil.e 打印错误日志 加粗红色
 * LogUtil.d 打印debug日志 蓝色
 * LogUtil.i 打印Info日志 绿色
 * LogUtil.logXml 打印xml日志 绿色
 * LogUtil.logProc 打印进程日志，进程正常日志蓝色，进程错误日志加粗红色
 * @author zhuxiaoxin
 */
public class LogUtil extends Thread {

	private InputStream is;
	private String type;
	private OutputStream os;

	public LogUtil(InputStream is, String type) {
		this(is, type, null);
	}

	LogUtil(InputStream is, String type, OutputStream redirect) {
		this.is = is;
		this.type = type;
		this.os = redirect;
	}

	public void run() {
		InputStreamReader isr = null;
		BufferedReader br = null;
		PrintWriter pw = null;
		try {
			if (os != null)
				pw = new PrintWriter(os);

			isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				if (pw != null){
					pw.println(line);
				}
				if (type.equals("Error")) {
					LogUtil.e(type + ">" + line);
				} else {
					LogUtil.d(type + ">" + line);
				}
			}
			if (pw != null) {
				pw.flush();
				pw.close();
			}
			br.close();
			isr.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {

		}
	}

	/**
	 * 打印进程日志
	 * 释放流信息，防止进程阻塞
	 * @param proc
	 */
	public static void logProc(Process proc){
		try {
			LogUtil errorGobbler = new LogUtil(proc.getErrorStream(), "Error");
			errorGobbler.start();
			LogUtil outputGobbler = new LogUtil(proc.getInputStream(),	"Output");
			outputGobbler.start();
		} catch (Exception e) {
			LogUtil.e("获取数据流出错");
		}
	}

	/**
	 * 普通日志，蓝色
	 * @param msg
	 */
	public static void d(String msg){
		String log = formatLog(msg);
        System.out.println("<font color='blue'>" + log + "</font>");
	}

	/**
	 * Info日志，绿色
	 */
	public static void i(String msg) {
		String log = formatLog(msg);
        System.out.println("<font color='green'>" + log + "</font>");
	}

	/**
	 * 错误日志 加粗红色
	 * @param msg
	 */
	public static void e(String msg) {
		String log = formatLog(msg);
		System.out.println("<strong><font color='red'>" + log + "</font></strong>");
	}

	/**
	 * 警告日志 加粗黄色
	 * @param msg
	 */
	public static void w(String msg) {
		String log = formatLog(msg);
		System.out.println("<strong><font color='yellow'>" + log + "</font></strong>");
	}

	/**
	 * 打印xml日志
	 * @param xmlContent
	 */
	public static void logXml(String xmlContent) {
		xmlContent = "<xmp>" + xmlContent + "</xmp>";
		i(xmlContent);
	}

	private static String formatLog(String msg){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA);
		format.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		return format.format(new Date())+"	"+msg;
	}
}