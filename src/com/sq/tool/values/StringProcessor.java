package com.sq.tool.values;

import com.sq.tool.LogUtil;
import com.sq.tool.xml.XmlProcessor;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import java.util.List;

public class StringProcessor extends XmlProcessor {

    public StringProcessor(String fileName) throws DocumentException {
        super(fileName);
    }

    public StringProcessor resetValue(String name, String value) {
        List<Element> elementList = mDocument.getRootElement().elements();
        Element targetElement = null;
        if (elementList != null && elementList.size() > 0) {
            for (Element element : elementList) {
                if (element.getName().equals("string")) {
                    if (element.attribute("name") != null && element.attribute("name").getValue().equals(name)) {
                        targetElement = element;
                        break;
                    }
                }
            }
        }
        if (targetElement == null) {
            LogUtil.d("不存在name为: " + name + "的节点");
        } else {
            targetElement.setText(value);
        }
        return this;
    }
}
