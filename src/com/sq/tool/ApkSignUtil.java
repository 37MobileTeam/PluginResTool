package com.sq.tool;

import java.io.IOException;

/**
 * @author zhuxiaoxin
 * 签名工具
 */
public class ApkSignUtil {

    private String apkSignCommand;
    private String keyStore;
    private String storePwd;
    private String alias;
    private String pwd;

    public ApkSignUtil(String command, String keyStore, String storePwd, String alias, String pwd){
        apkSignCommand = command;
        this.keyStore = keyStore;
        this.storePwd = storePwd;
        this.alias = alias;
        this.pwd = pwd;
    }

    public void signApk(String unsignedApk, String signedApk) {
        StringBuilder sb = new StringBuilder();
        sb.append(apkSignCommand)
                .append(" -keystore ")
                .append(keyStore)
                .append(" -storepass ")
                .append(storePwd)
                .append(" -keypass ")
                .append(pwd)
                .append(" -signedjar ")
                .append(signedApk)
                .append(" ")
                .append(unsignedApk)
                .append(" ")
                .append(alias)
                .append(" -digestalg SHA1 -sigalg MD5withRSA");
        LogUtil.d("执行签名");
        LogUtil.d("签名命令： " + sb.toString());
        try {
            Process process = Runtime.getRuntime().exec(sb.toString());
            LogUtil.logProc(process);
            try {
                if (process.waitFor() != 0)
                    LogUtil.d("签名失败~~");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
