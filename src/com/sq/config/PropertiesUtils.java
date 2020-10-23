package com.sq.config;

import com.sq.tool.LogUtil;

import java.io.*;
import java.util.Date;
import java.util.Properties;

/**
 * @author zhuxiaoxin
 * 属性文件工具类
 */
public class PropertiesUtils {

    public static Properties getProperties(String filePath) {
        Properties properties = new Properties();
        Reader bf = null;
        try {
            bf = new InputStreamReader(new FileInputStream(filePath), "utf-8");
            properties.load(bf);
        } catch (Exception ex) {
            LogUtil.d("解析文件出错:" + filePath + ", " + ex.getCause());
            ex.printStackTrace();
        } finally {
            if (bf != null) {
                try {
                    bf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return properties;
    }

    /**
     * 保存
     * @param properties
     * @param filePath
     */
    public static void save(Properties properties, String filePath) {
        try {
            FileOutputStream sdkfos = new FileOutputStream(filePath);
            properties.store(sdkfos, new Date().toGMTString());
            sdkfos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
