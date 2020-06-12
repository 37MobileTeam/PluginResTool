package com.sq;

import com.sq.bean.PublicXmlBean;
import com.sq.helper.PublicAndRHelper;
import com.sq.tool.ApkSignUtil;
import com.sq.tool.DecodeUtil;
import com.sq.tool.LogUtil;
import com.sq.tool.ZipAlignUtils;
import org.dom4j.DocumentException;

import java.io.File;

/**
 * 修改apk资源id值工具
 */
public class ResTool {

    public static void main(String[] args) {

        String apkPath = args[0];
        LogUtil.d("apk路径为: " + apkPath);

        String apkToolPath = "/Users/zhuxiaoxin/work/sq/channel/apktool_2.3.3.jar";
        String tempApkPath = "/Users/zhuxiaoxin/work/sq/channel/插件化/work/";
        File tempApkFile = new File(tempApkPath);
        if (tempApkFile.exists()) {
            tempApkFile.delete();
        }
        tempApkFile.mkdirs();

        File sourceApk = new File(tempApkPath + File.separator + "dist");

        DecodeUtil decodeUtil = new DecodeUtil(apkToolPath);
        decodeUtil.decode(apkPath, tempApkPath);

        //处理public逻辑
        handlePublicXml(tempApkPath);

        decodeUtil.encode(tempApkPath);

        String keyStoreFile =  System.getProperty("user.dir") + File.separator+"keystore"+File.separator + "37.keystore";
        ApkSignUtil apkSignUtil = new ApkSignUtil(LocalPropertiesConfig.getInstance().jarsignerAtLinux,
                keyStoreFile,
                LocalPropertiesConfig.getInstance().SECRET_STOREPASS,
                LocalPropertiesConfig.getInstance().SECRET_KEY_ALI,
                LocalPropertiesConfig.getInstance().SECRET_KEYPASS
        );
        File[] apks = sourceApk.listFiles();
        if (apks.length > 0) {
            File souceApkFile = apks[0];
            String signedApkPath =  souceApkFile.getParentFile().getAbsolutePath() + File.separator + "signed_" + souceApkFile.getName();
            apkSignUtil.signApk(souceApkFile.getAbsolutePath(), signedApkPath);
            String zipalignCmd = LocalPropertiesConfig.getInstance().zipalignAtLinux;
            String alignApkPath = souceApkFile.getParentFile().getAbsolutePath() + File.separator + "signed_aligned_" + souceApkFile.getName();
            ZipAlignUtils.execute(zipalignCmd, signedApkPath, alignApkPath);
        }
    }

    /**
     * 将0x7f改为0x8f
     */
    private static void handlePublicXml(String tempApkPath) {
        String publicXmlPath = tempApkPath + "res" + File.separator + "values" + File.separator + "public.xml";
        try {
            PublicXmlBean publicXmlBean = new PublicXmlBean(publicXmlPath);
            publicXmlBean.resetBigValue();
            publicXmlBean.flush();
            new PublicAndRHelper().handle(tempApkPath);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }


}
