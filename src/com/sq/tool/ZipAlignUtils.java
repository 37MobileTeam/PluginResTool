package com.sq.tool;

import java.io.IOException;

public class ZipAlignUtils {

    public static void execute(String zipalignCmd, String apkPath, String alignApkPath) {
        if (zipalignCmd ==null || "".equals(zipalignCmd)) {
            zipalignCmd = " zipalign ";
        }

        LogUtil.d("--优化命令-->"+zipalignCmd);
        StringBuilder buffer = new StringBuilder();
        buffer.append(zipalignCmd+"  4 " + apkPath + " " + alignApkPath);
        try {
            Process process = Runtime.getRuntime().exec(buffer.toString());
            LogUtil.logProc(process);
            if (process.waitFor() != 0) {
                LogUtil.d("优化失败！！！");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
