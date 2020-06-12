package com.sq.helper;

import com.sq.bean.PublicXmlBean;
import com.sq.tool.FileUtil;
import com.sq.tool.StringUtils;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 纠正R类的值，对应于public.xml
 */
public class RValueHelper {

    public static void handle(String RFilePath, PublicXmlBean publicXmlBean) {
        File RFile = new File(RFilePath);
        String RStyleFilePath = "";
        Map<String, String> cacheMap = null;
        if (RFile.getName().endsWith("R$attr.smali")) {
            RStyleFilePath = RFilePath.replace("R$attr", "R$styleable");
            File RStyleAbleFile = new File(RStyleFilePath);
            //styleable存在，则把attr文件替换过的值缓存
            if (RStyleAbleFile.exists()) {
                cacheMap = new HashMap<>();
            }
        }
        String rFileContent = FileUtil.read(RFilePath);
        //找到RFile中是属性的每一行
        ArrayList<String> lines = FileUtil.readAllLines(RFilePath, ".field public static final");
        String regex = ".field public static final (.*):(.*) = (.*)";
        for (String line : lines) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                String type = RFile.getName().replace("R$", "").replace(".smali", "");
                String name = matcher.group(1);
                String resetValue = publicXmlBean.getValue(type, name);
                if (StringUtils.isEmpty(resetValue)) {
                    resetValue = publicXmlBean.addValue(type, matcher.group(1));
                }
                //替换到文件内容中
                rFileContent = rFileContent.replace(line, ".field public static final " + name + ":" + matcher.group(2) + " = " + resetValue);
                if (cacheMap != null) {
                    //换过的值缓存起来
                    cacheMap.put(matcher.group(3), resetValue);
                }
            }
        }
        FileUtil.write(RFilePath, rFileContent);
        if (cacheMap != null) {
            //纠正R$styleable的值
            List<String> styleAbleLines = FileUtil.readAllLines(RStyleFilePath);
            BufferedWriter bw = null;
            try {
                bw = new BufferedWriter(new FileWriter(RStyleFilePath));
                for (String styleAbleLine : styleAbleLines) {
                    for (String key : cacheMap.keySet()) {
                        if (styleAbleLine.contains(key)) {
                            styleAbleLine = styleAbleLine.replace(key, cacheMap.get(key));
                        }
                    }
                    bw.write(styleAbleLine);
                    bw.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (bw != null) {
                    try {
                        bw.close();
                    } catch (IOException e) {
                        bw = null;
                    }
                }
            }
        }
    }

}
