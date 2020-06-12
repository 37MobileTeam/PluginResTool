package com.sq.tool.xml;

import com.sq.tool.FileUtil;
import com.sq.tool.LogUtil;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * @author zhuxiaoxin
 * xml相关处理类
 * 功能：
 * 1、合并xml
 * 2、提供读取器，自身的xml文件对应的Document
 */
public class XmlProcessor {

    protected SAXReader mReader;

    protected Document mDocument;

    protected String mFileName;

    public XmlProcessor(String fileName) throws DocumentException {
        mFileName = fileName;
        mReader = new SAXReader();
        mDocument = mReader.read(mFileName);
    }

    //当xml标签缺乏根标签，不规范时自动处理增加temp根标签
    private void autoRepairDocument() throws DocumentException {
        String content = FileUtil.read(mFileName);
        content += "<temp>" + content + "</temp>";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content.getBytes());
        mDocument = mReader.read(byteArrayInputStream);
    }

    /**
     * @param fileName
     * @param force 强制合并，当有冲突时依然合并
     * @param forceType 强制合并时合并类型，0代表保留当前，1代表保留fileName文件内容
     */
    public XmlProcessor combine(String fileName, IElementCompare elementCompator, boolean force, int forceType) {
        LogUtil.d("合并xml文件： " + fileName + "--->" + mFileName);
        try {
            Document combineFile = mReader.read(fileName);
            List<Element> elementList = combineFile.getRootElement().elements();
            Element rootElement = mDocument.getRootElement();
            List<Element> originalList = rootElement.elements();
            if (elementList != null && elementList.size() > 0) {
                for (Element element : elementList) {
                    boolean flag = false;
                    Element tempElement = null;
                    for (Element element1 : originalList) {
                        if (elementCompator.compare(element, element1)) {
                            tempElement = element1;
                            flag = true;
                            break;
                        }
                    }
                    if (flag) {
                        //节点已存在，按策略处理
                        if (force) {
                            if (forceType == 0) {
                                //TODO 增加如果节点内容不一样，报错告警
//                                if (new NodeComparator().compare(tempElement, element) != 0) {
//                                    LogUtil.logXml("警告，存在名称相同，但内容不同的节点：" + element.toString() + "\n" + tempElement.toString());
//                                }
                            } else {
                                rootElement.remove(tempElement);
                                rootElement.add(element.createCopy());
                            }
                        } else {
                            LogUtil.d("合并出错了，有相同节点");
                        }
                    } else {
                        //节点不存在直接添加
                        rootElement.add(element.createCopy());
                    }
                }
            }
        } catch (DocumentException e) {
            LogUtil.d("合并xml文件" + fileName + "出错");
            e.printStackTrace();
        }
        return this;
    }

    public SAXReader getReader(){
        return mReader;
    }

    /**
     * 获取自身文件对应的document
     * @return
     */
    public Document getSelfDocument() {
        return mDocument;
    }

    public String getFileName() {
        return mFileName;
    }

    public void flush() {
        //写进去
        try {
            FileWriter writer = new FileWriter(mFileName);
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding("utf-8");
            XMLWriter xmlWriter = new XMLWriter(writer, format);
            xmlWriter.write(mDocument);
            xmlWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
