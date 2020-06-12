package com.sq.tool.manifest;


import com.sq.tool.xml.IElementCompare;
import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 * @author zhuxiaoxin
 * 节点相等对比
 */
public class AndroidNameComparor implements IElementCompare {

    /**
     * 标签相同且android:name属性相等判定为相等
     * @param e1
     * @param e2
     * @return
     */
    @Override
    public boolean compare(Element e1, Element e2) {
        if (!e1.getName().equals(e2.getName())) {
         return false;
        }
        Attribute attribute1 = e1.attribute("name");
        Attribute attribute2 = e2.attribute("name");
        if (attribute1 == null || attribute2 == null) {
            return false;
        }
        if (attribute1.getStringValue().equals(attribute2.getStringValue())) {
            return true;
        }
        return false;
    }

}
