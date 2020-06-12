package com.sq.tool.manifest.plug;

import com.sq.tool.LogUtil;
import com.sq.tool.manifest.PlugProcessor;
import org.dom4j.Attribute;
import org.dom4j.Element;
import java.util.List;

public class ServiceProcessor {

    private Element mApplicationElment;
    private List<Element> mServices;

    public ServiceProcessor(Element applicationElment) {
        if (applicationElment == null) {
            LogUtil.e("ActivityProcessor：出现异常");
            return;
        }
        this.mApplicationElment = applicationElment;
        init();
    }

    public void init() {
        mServices = mApplicationElment.elements(PlugProcessor.PLUG.SERVICE);
    }

    /**
     * example: <action android:name="{包名}.intent.ACTION_REMOTE_SERVICE"/>
     *
     * @param serviceName
     * @param id
     * @param newName
     */
    public void replaceAttributeNameUnderIntentFilter(String serviceName, String id, String newName) {
        if (mServices == null) {
            return;
        }
        for (Element element : mServices) {
            Attribute attribute = element.attribute(PlugProcessor.PLUG.NAME);
            if(attribute == null){
                continue;
            }
            LogUtil.i("查找service：" + attribute.getValue());
            if (serviceName.equals(attribute.getValue())) {
                List<Element> filterElements = element.elements(PlugProcessor.PLUG.INTENT_FILTER);
                LogUtil.i("找intent-filter");
                if (filterElements != null && !filterElements.isEmpty()) {
                    for (Element element1 : filterElements) {
                        LogUtil.i("找intent-filter下对应id的element");
                        Element targetElement = element1.element(id);
                        if(targetElement!= null){
                            LogUtil.i("找到对应id的element了");
                            Attribute attribute1 = targetElement.attribute(PlugProcessor.PLUG.NAME);
                            if (attribute1 != null) {
                                LogUtil.i("找到对应id的attribute了：" + newName);
                                attribute1.setValue(newName);
                            }
                        }
                    }
                }
                return;
            }
        }
    }

}
