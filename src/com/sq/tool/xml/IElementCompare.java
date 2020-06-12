package com.sq.tool.xml;

import org.dom4j.Element;

/**
 * @author zhuxiaoxin
 * 节点比较器
 */
public interface IElementCompare {

    boolean compare(Element e1, Element e2);

}
