package com.sq.tool.manifest.plug;


import com.sq.tool.LogUtil;
import com.sq.tool.manifest.PlugProcessor;
import com.sq.tool.xml.ManifestHelper;
import org.dom4j.Attribute;
import org.dom4j.Element;
import java.util.List;

//todo 参考趣乐多activity操作

public class ActivityProcessor {

    private Element mElement;
    private Element mApplicationElment;
    private List<Element> mActivities;

    public ActivityProcessor(Element applicationElment) {
        if (applicationElment == null) {
            LogUtil.e("ActivityProcessor：出现异常");
            return;
        }
        this.mApplicationElment = applicationElment;
        init();
    }

    public void init() {
        mActivities = mApplicationElment.elements(PlugProcessor.PLUG.ACTIVITY);
        if (mActivities == null || mActivities.isEmpty()) {
            LogUtil.e("ActivityProcessor：没有设置启动activity，错误");
            return;
        }
        if(mActivities.size()>1){
            LogUtil.w("出现了多个启动activity，请检查是否处理了");
        }
        mElement = mActivities.get(0);
    }

    public void replaceAttributeUnderIntentFilter(String id, String orignValue, String destValue) {
        if (mElement == null) {
            return;
        }
        LogUtil.i("准备替换新值：" + orignValue + " || " + destValue);
        List<Element> filterElements = mElement.elements(PlugProcessor.PLUG.INTENT_FILTER);
        if (filterElements != null && !filterElements.isEmpty()) {
            for (Element element : filterElements) {
                Attribute attribute = ManifestHelper.getChildAttributeByValue(element, id, orignValue);
                if(attribute!= null){
                    LogUtil.i("找到了，准备替换：" + attribute.getValue());
                    attribute.setValue(destValue);
                }
            }
        }
    }

    public void removeAttributeUnderIntentFilter(String id, String orignValue){
        if (mElement == null) {
            return;
        }
        LogUtil.i("准备删除：" + orignValue);
        List<Element> filterElements = mElement.elements(PlugProcessor.PLUG.INTENT_FILTER);
        if (filterElements != null && !filterElements.isEmpty()) {
            for (Element element : filterElements) {
                Element element1 = ManifestHelper.getChildElementByValue(element, id, orignValue);
                if(element1!= null){
                    LogUtil.i("找到了，准备删除：" + element1.toString());
                    element.remove(element1);
                }
            }
        }
    }

}
