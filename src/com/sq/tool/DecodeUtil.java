package com.sq.tool;

import java.io.File;

/**
 * @author zhuxiaoxin
 * 编译，反编译
 * **/
public class DecodeUtil {

    private String apktoolPath;

    public DecodeUtil(String apktoolPath) {
        this.apktoolPath = apktoolPath;
    }

    /**
     * 反编译
     **/
    public void decode(String apkPath, String tempPath) {
        LogUtil.d("--------start 反编译-------------");
        File file = new File(tempPath);
        if (file.exists()) {
            FileUtil.delDir(file);
        }
        file.mkdirs();
        StringBuilder builder = new StringBuilder();
        builder.append("java -jar ")
                .append(apktoolPath)
                .append(" d -f ").append(apkPath)
                .append(" -o ")
                .append(tempPath);
        LogUtil.d("反编译命令:" + builder.toString());
        try {
            Process process = Runtime.getRuntime().exec(builder.toString());
            LogUtil.logProc(process);
            if (process.waitFor() != 0) {
                LogUtil.d("反编译apk失败 : exec cmd err..!!!!!!!");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("反编译apk失败 : err..!!!!!!! ＝ " + e.toString());
            return;
        }
        LogUtil.d("---------end 反编译--------------");
    }

    /**
     * 回编译
     * @param tempPath
     */
    public void encode(String tempPath) {
        LogUtil.d("---------start 回编译--------------");
        File file = new File(tempPath);
        if (!file.exists()) {
            LogUtil.d("文件不存在，回编译失败");
        }
        StringBuilder builder = new StringBuilder();
        builder.append("java -jar ")
                .append(apktoolPath)
                .append(" b ").append(tempPath);
        LogUtil.d("回编译命令:" + builder.toString());
        try {
            Process process = Runtime.getRuntime().exec(builder.toString());
            LogUtil.logProc(process);
            if (process.waitFor() != 0) {
                LogUtil.d("回编译apk失败 : exec cmd err..!!!!!!!");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("回编译apk失败 : err..!!!!!!! ＝ " + e.toString());
            return;
        }
        LogUtil.d("---------end 回编译--------------");
    }

}
