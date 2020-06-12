package com.sq.bean;

import com.sq.tool.LogUtil;
import com.sq.tool.StringUtils;
import com.sq.tool.xml.XmlProcessor;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 对应public.xml类
 * <public type="drawable" name="abc_vector_test" id="0x7f020052"/>
 */
public class PublicXmlBean extends XmlProcessor {

    public static String ID = "id";
    private static String PUBLIC = "public";
    private static String TYPE = "type";
    private static String NAME = "name";

    private Map<String, Map<String, String>> mTypeMap;

    public PublicXmlBean(String fileName) throws DocumentException {
        super(fileName);
        mTypeMap = new HashMap<>();
        init();
    }

    /**
     * 合并策略：当public.xml中已存在就不处理，不存在则增加，增加的值重新生成
     * @param file
     * @return
     */
    public PublicXmlBean combine(String file) {
        try {
            PublicXmlBean combineBean = new PublicXmlBean(file);
            List<Element> elements = combineBean.mDocument.getRootElement().elements();
            for (Element element : elements) {
                String tempType = element.attribute(TYPE).getStringValue();
                String tempName = element.attribute(NAME).getStringValue();
                addValue(tempType, tempName);
            }
        } catch (DocumentException e) {
            e.printStackTrace();
            LogUtil.e("合并public.xml文件失败,因为" + file + "解析失败");
        }
        return this;
    }

    private void init() {
        List<Element> elements = mDocument.getRootElement().elements();
        for (Element element : elements) {
            String type = element.attribute(TYPE).getStringValue();
            String name = element.attribute(NAME).getStringValue();
            String id = element.attribute(ID).getStringValue();
            Map<String, String> typeMap = mTypeMap.get(type);
            if (typeMap == null) {
                typeMap = new HashMap<>();
                typeMap.put(name, id);
                mTypeMap.put(type, typeMap);
            } else {
                typeMap.put(name, id);
            }
        }
    }

    public void resetBigValue() {
        Map<String, Map<String, Element>> typeElementMap = new HashMap<>();
        List<Element> elements = mDocument.getRootElement().elements();
        for (Element element : elements) {
            String type = element.attribute(TYPE).getStringValue();
            String name = element.attribute(NAME).getStringValue();
            Map<String, Element> typeElement = typeElementMap.get(type);
            if (typeElement == null) {
                typeElement = new HashMap<>();
                typeElement.put(name, element);
                typeElementMap.put(type, typeElement);
            } else {
                typeElement.put(name, element);
            }
        }

        for (String type : mTypeMap.keySet()) {
            Map<String, String> typeMap = mTypeMap.get(type);
            for (String name : typeMap.keySet()) {
                String value = typeMap.get(name);
                if (value != null) {
                    String hex = value.replace("0x", "");
                    int number = Integer.parseInt(hex, 16);
                    number += Integer.parseInt("00500000", 16);
                    String tempValue = "0x" + Integer.toHexString(number);
                    typeMap.put(name, tempValue);
                    List<Element> elements0 = mDocument.getRootElement().elements();
                    for (Element element : elements0) {
                        String elementType = element.attribute(TYPE).getStringValue();
                        String elementName = element.attribute(NAME).getStringValue();
                        if (type.equals(elementType) && name.equals(elementName)) {
                            element.attribute(ID).setValue(tempValue);
                        }
                    }
                }
            }
        }
    }

    public String getValue(String type, String name) {
        String result = "";
        Map<String, String> typeMap = mTypeMap.get(type);
        if (typeMap.get(name) != null) {
            result = typeMap.get(name);
        } else {
            //存在public.xml中是.,而R中是_下划线的情况，此时要遍历一下
            for (String key : typeMap.keySet()) {
                String tempName = key.replaceAll("\\.", "_");
                if (name.equals(tempName)) {
                    result = typeMap.get(key);
                    break;
                }
            }
        }
        return result;
    }

    public String addValue(String type, String name, String value) {
        Element element = new DefaultElement(PUBLIC);
        element.addAttribute(TYPE, type);
        element.addAttribute(NAME, name);
        element.addAttribute(ID, value);
        mDocument.getRootElement().add(element);
        //map中也要加
        Map<String, String> typeMap = mTypeMap.get(type);
        if (typeMap != null){
            typeMap.put(name, value);
        } else {
            typeMap = new HashMap<String, String>();
            typeMap.put(name, value);
            mTypeMap.put(type, typeMap);
        }
        return value;
    }

    //默认在最后加，假如已有值，则不处理
    public String addValue(String type, String name) {
        String tempValue = getValue(type, name);
        if (!StringUtils.isEmpty(tempValue)) {
            return tempValue;
        } else {
            int typeNextValue = getTypeNextValue(type);
            String value = "0x" + Integer.toHexString(typeNextValue);
            return addValue(type, name, value);
        }
    }

    /**
     * 取类型的下一个值
     * @param type
     * @return
     */
    private int getTypeNextValue(String type) {
        Map<String, String> typeMap = mTypeMap.get(type);
        //类型不存在时
        if (typeMap == null) {
            return getNextTypeValue();
        }
        //类型存在时
        int max = 0;
        for (Map.Entry<String, String> entry : typeMap.entrySet()) {
            String hex = entry.getValue().replace("0x", "");
            int number = Integer.parseInt(hex, 16);
            if (number > max) {
                max = number;
            }
        }
        return max + 1;
    }

    /**
     * 获取下一个类型的第一个值：假如当前所有类型中的最大值为0x7f035555,则下个类型的值为0x7f040000
     * @return
     */
    private int getNextTypeValue() {
        int maxValue = 0;
        //取各类型中的最大值的最大值
        for (String type : mTypeMap.keySet()) {
            int typeMaxValue = getTypeNextValue(type);
            if (typeMaxValue > maxValue) {
                maxValue =  typeMaxValue;
            }
        }
        //将后两个字节取最大
        maxValue |= 65535;
        return maxValue + 1;
    }

}
