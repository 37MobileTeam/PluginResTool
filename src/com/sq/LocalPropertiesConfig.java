package com.sq;

import com.sq.tool.LogUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * localProperties.xml配置项定义类
 * 
 * @link gd_local_config.properties
 * @author tang
 * 
 */
public class LocalPropertiesConfig {
	
	private static LocalPropertiesConfig config;
	
	// --------------------------根据自己的电脑修改配置----------------------------
	public String rarPath = "";
	public String zipalign = "";
	public String JDK_BIN_PATH = "";
	//20160301新增linux下的几个命令
	public String jarsignerAtLinux = "";
	public String zipalignAtLinux = "";
	public String aaptAtLinux = "";
	// --------------------------根据自己的电脑修改配置----------------------------

	/** 密钥 名称 */
	public String SECRET_KEY_NAME = "";
	/** 密钥别名，alias */
	public String SECRET_KEY_ALI = "";
	/** 用于密钥库完整性的口令 */
	public String SECRET_STOREPASS = "";
	/** 专用密钥的口令 */
	public String SECRET_KEYPASS = "";
	/** APKTOOL version */
	public String APKTOOL_VERSION_CODE = "2";

	/** 是否替换assets下面的包名相关，默认不修改 */
	public String MODIFY_CONFIG_OF_PNAME = "0";
	
	public boolean isTest = false;

	
	private LocalPropertiesConfig() {
		
		getLocalProperty();
		
	}
	
	public synchronized static LocalPropertiesConfig getInstance(){
		if (config == null) {
			config = new LocalPropertiesConfig();
		}
		return config;
	}
	
	
	/**
	 * 从配置文件中获取
	 */
	private void getLocalProperty() {
		// 读取配置文件
		String CONFIG = System.getProperty("user.dir")+ File.separator + "local_config.properties";
		Properties prop = new Properties();
		FileInputStream fis;
		try {
			fis = new FileInputStream(CONFIG);
			prop.load(fis);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		rarPath = prop.getProperty("rarpath");
		zipalign = prop.getProperty("zipalign");
		JDK_BIN_PATH = prop.getProperty("jdk_bin");
		SECRET_KEY_NAME = prop.getProperty("keyname");
		SECRET_KEY_ALI = prop.getProperty("key_alias");
		SECRET_STOREPASS = prop.getProperty("storepass");
		SECRET_KEYPASS = prop.getProperty("keypass");
		//
		jarsignerAtLinux = prop.getProperty("jarsignerAtLinux");
		zipalignAtLinux = prop.getProperty("zipalignAtLinux");
		aaptAtLinux = prop.getProperty("aaptAtLinux");
		
		
		APKTOOL_VERSION_CODE = prop.getProperty("apktool_version");

		if (APKTOOL_VERSION_CODE == null) {
			APKTOOL_VERSION_CODE = "1"; // 默认为旧版本apktool V1.x
		}

		MODIFY_CONFIG_OF_PNAME = prop.getProperty("modify_assets_pname");

		if (MODIFY_CONFIG_OF_PNAME == null) {
			MODIFY_CONFIG_OF_PNAME = "0"; // 默认不扫描修改
		}

		LogUtil.d("------- APKTOOL_VERSION: " + APKTOOL_VERSION_CODE
				+ " ----------");
		LogUtil.d("------- MODIFY_CONFIG_OF_PNAME: "
				+ MODIFY_CONFIG_OF_PNAME + " ----------");
		//参数表中没有配置则跳过测试内容，不影响正常功能的使用
		String test = prop.getProperty("isTest")==null?"0":prop.getProperty("isTest");
		if ("1".equals(test)) {
			isTest = true;
		}
	}

}
