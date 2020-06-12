package com.sq.tool.xml;

import com.sq.tool.manifest.PlugProcessor;
import org.dom4j.Attribute;
import org.dom4j.Element;
import java.util.List;

public class ManifestHelper {
    public static Element getElementByValue(List<Element> elements, String name) {
        if (elements == null || elements.isEmpty() || name == null) {
            return null;
        }
        for (Element element : elements) {
            Attribute attr = element.attribute(PlugProcessor.PLUG.NAME);
            if (attr != null && name.equals(attr.getValue())) {
                return element;
            }
        }
        return null;
    }

    public static Element getChildElementByValue(Element element,String id, String value) {
        if(value ==null){
            return null;
        }
        List<Element> elements = element.elements(id);
        for (Element element1 : elements) {
            List<Attribute> attributes = element1.attributes();
            if(attributes != null && !attributes.isEmpty()){
                for(Attribute attribute1:attributes){
                    if(value.equals(attribute1.getValue())){
                        return element1;
                    }
                }
            }
        }
        return null;
    }

    public static Attribute getChildAttributeByValue(Element element,String id, String value) {
        if(value ==null){
            return null;
        }
        List<Element> elements = element.elements(id);
        for (Element element1 : elements) {
            List<Attribute> attributes = element1.attributes();
            if(attributes != null && !attributes.isEmpty()){
                for(Attribute attribute1:attributes){
                    if(value.equals(attribute1.getValue())){
                        return attribute1;
                    }
                }
            }
        }
        return null;
    }

}
