package com.sq.helper;

import com.sq.bean.PublicXmlBean;
import com.sq.tool.LogUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 处理R类和public.xml中的id问题
 * 1、纠正代码中R类的值
 * 2、调整R类的值，并把新增的添加到public.xml中
 */
public class PublicAndRHelper {

    List<String> mRClassFileList;

    public PublicAndRHelper() {
        mRClassFileList = new ArrayList<>();
    }

    public void handle(String tempApkPath) {

        //1、扫描母包中public.xml
        String publicXmlPath = tempApkPath + File.separator + "res" + File.separator + "values" + File.separator + "public.xml";

        File publicXmlFile = new File(publicXmlPath);

        if (publicXmlFile.exists()) {
            try {
                PublicXmlBean publicXmlBean = new PublicXmlBean(publicXmlPath);
                //代码中的smali路径
                String smaliPath = tempApkPath + File.separator + "smali";
                scannerRClass(smaliPath);
                for (String path : mRClassFileList) {
                    RValueHelper.handle(path, publicXmlBean);
                }
                publicXmlBean.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            LogUtil.e("找不到public.xml, 查找路径为: " + publicXmlPath);
        }
    }

    /**
     * 扫描代码中的R类
     * @return
     */
    private void scannerRClass(String path) {
        File smaliFilePath = new File(path);
        for (File file : smaliFilePath.listFiles()) {
            if (file.isDirectory()) {
                scannerRClass(file.getAbsolutePath());
            } else if(file.isFile()){
                if (file.getName().equals("R.smali") || file.getName().startsWith("R$")) {
                    //此处过滤掉styleable文件
                    if (!file.getName().endsWith("R$styleable.smali")) {
                        mRClassFileList.add(file.getAbsolutePath());
                    }
                }
            }
        }
    }

}
