package com.sq;

import com.sq.bean.PublicXmlBean;
import com.sq.config.Config;
import com.sq.helper.PublicAndRHelper;
import com.sq.tool.DecodeUtil;
import com.sq.tool.LogUtil;

import org.dom4j.DocumentException;

import java.io.File;

/**
 * 修改apk资源id值工具
 */
public class ResTool {

    public static void main(String[] args) throws Exception {
        //获得配置
        File directory = new File("");
        String rootDir = directory.getCanonicalPath();
        Config config = new Config(
                rootDir + File.separator + "apktool_2.3.3.jar",
                rootDir + File.separator + "plugin.apk",
                rootDir + File.separator + "work",
                "00500000");
        //处理
        handleResPkg(config);
    }

    public static void handleResPkg(Config config) {

        LogUtil.d("配置文件内容： " + config.toString());
        String apkPath = config.originalApkPath;
        LogUtil.d("apk路径为: " + apkPath);

        String tempApkPath = config.tempApkPath;
        LogUtil.d("反编译临时路径为: " + tempApkPath);

        File tempApkFile = new File(tempApkPath);
        if (tempApkFile.exists()) {
            tempApkFile.delete();
        }
        tempApkFile.mkdirs();

        File sourceApk = new File(tempApkPath + File.separator + "dist");

        DecodeUtil decodeUtil = new DecodeUtil(config.apktoolPath);
        decodeUtil.decode(apkPath, tempApkPath);

        //处理public逻辑
        handlePublicXml(tempApkPath, config.addBigValue);

        decodeUtil.encode(tempApkPath);
    }

    /**
     * 处理xml文件
     */
    private static void handlePublicXml(String tempApkPath, String addBigValue) {
        String publicXmlPath = tempApkPath + File.separator + "res" + File.separator + "values" + File.separator + "public.xml";
        try {
            PublicXmlBean publicXmlBean = new PublicXmlBean(publicXmlPath);
            publicXmlBean.resetBigValue(addBigValue);
            publicXmlBean.flush();
            new PublicAndRHelper().handle(tempApkPath);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }


}
