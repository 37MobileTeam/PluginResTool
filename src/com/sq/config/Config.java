package com.sq.config;

import java.util.Properties;

/**
 * 配置类
 */
public class Config {

    /**
     * apktool路径
     */
    public String apktoolPath;

    /**
     * 反编译的apk存放路径
     */
    public String tempApkPath;

    /**
     * 原始apk路径
     */
    public String originalApkPath;

    /**
     * public.xml文件各item增加的值
     */
    public String addBigValue;

    /**
     * @param apktoolPath     apktool 目录
     * @param originalApkPath 需要修改的apk
     * @param tempApkPath     工作目录（反编译和回编译）
     * @param addBigValue     在原来的基础上加多少值
     */
    public Config(String apktoolPath, String originalApkPath, String tempApkPath, String addBigValue) {
        this.apktoolPath = apktoolPath;
        this.originalApkPath = originalApkPath;
        this.tempApkPath = tempApkPath;
        this.addBigValue = addBigValue;
    }

    /**
     * 解析配置文件
     *
     * @param configPath
     * @return
     */
    public static Config parse(String configPath) {
        Properties properties = PropertiesUtils.getProperties(configPath);
        String addBigValue = properties.getProperty("addBigValue");
        String apktoolPath = properties.getProperty("apktoolPath");
        String tempApkPath = properties.getProperty("tempApkPath");
        String originalApkPath = properties.getProperty("originalApkPath");
        Config config = new Config(apktoolPath, originalApkPath, tempApkPath, addBigValue);
        return config;
    }

    @Override
    public String toString() {
        return "Config{" +
                ", apktoolPath='" + apktoolPath + '\'' +
                ", tempApkPath='" + tempApkPath + '\'' +
                ", originalApkPath='" + originalApkPath + '\'' +
                ", addBigValue='" + addBigValue + '\'' +
                '}';
    }
}
