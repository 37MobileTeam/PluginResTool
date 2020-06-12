package com.sq.tool.manifest;

import com.sq.tool.FileUtil;
import com.sq.tool.LogUtil;
import com.sq.tool.StringUtils;
import com.sq.tool.manifest.plug.ActivityProcessor;
import com.sq.tool.manifest.plug.ServiceProcessor;
import org.dom4j.*;
import org.dom4j.tree.DefaultElement;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhuxiaoxin
 * Manifest组件操作器
 * 提供一系列组件相关操作，支持链式调用
 */
public class PlugProcessor {

    private ManifestProcessor mManifestProcessor;
    private Element mApplicationElement;
    private ActivityProcessor mActivityProcessor;
    private ServiceProcessor mServiceProcessor;

    public PlugProcessor(ManifestProcessor manifestProcessor) {
        mManifestProcessor = manifestProcessor;
        mApplicationElement = mManifestProcessor.getSelfDocument().getRootElement().element(ManifestProcessor.Manifest.APPLICATION);
        mActivityProcessor = new ActivityProcessor(mApplicationElement);
        mServiceProcessor = new ServiceProcessor(mApplicationElement);
    }

    /*********************************通用操作**********************************/

    /**
     * 增加组件，通过文件直接复制的方式
     *
     * @param plugs
     * @return
     */
    public PlugProcessor addPlugs(String plugs) {
        try {
            Document combinePlugsDocument = null;
            try {
                combinePlugsDocument = mManifestProcessor.getReader().read(plugs);
            } catch (DocumentException e) {
                //兼容以往data没有根标签的方式
                String content = FileUtil.read(plugs);
                content = "<application xmlns:android=\"http://schemas.android.com/apk/res/android\">" + content + "</application>";
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content.getBytes());
                combinePlugsDocument = mManifestProcessor.getReader().read(byteArrayInputStream);
            }
            List<Element> list = combinePlugsDocument.getRootElement().elements();
            if (list != null && list.size() > 0) {
                for (Element element1 : list) {
                    mApplicationElement.add(element1.createCopy());
                }
            }
            refreshChildElements();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return this;
    }

    public PlugProcessor addPlugs(List<Element> elements) {
        if (elements != null && elements.size() > 0) {
            for (Element element1 : elements) {
                mApplicationElement.add(element1.createCopy());
            }
        }
        refreshChildElements();
        return this;
    }

    /**
     * 合并manifest的节点后重新初始化里面的数据，防止节点找不到的情况
     */
    private void refreshChildElements() {
        mActivityProcessor.init();
        mServiceProcessor.init();
    }

    /**
     * 修改组件属性，前提是有name属性
     *
     * @param plug，                 如activity，provider，service，meta-data等
     * @param name，                 如activity名称，meta-data的name
     * @param attributeName，修改的属性名
     * @param attributeValue，修改的属性值
     * @param forceAdd              true代表没有该属性时增加，false代表没有就不操作
     * @return
     */
    public PlugProcessor resetPlugAttribute(String plug, String name, String attributeName, String attributeValue, boolean forceAdd) {
        List<Element> plugs = mApplicationElement.elements(plug);
        for (Element element : plugs) {
            if (element.attribute(ManifestProcessor.Manifest.NAME) != null && element.attribute(ManifestProcessor.Manifest.NAME).getValue().equals(name)) {
                if (element.attribute(attributeName) != null) {
                    element.attribute(attributeName).setValue(attributeValue);
                } else {
                    LogUtil.d(name + "， 组件没有 " + attributeName + "属性");
                    if (forceAdd) {
                        LogUtil.d("没有属性，添加" + attributeName + "属性");
                        element.addAttribute(new QName(attributeName, mManifestProcessor.getAndroidNameSpace()), attributeValue);
                    }
                }
            }
        }
        return this;
    }

    /**
     * 删除组件属性
     *
     * @param plug          组件类别，如activity，service，provider等
     * @param name          组件名称，指name属性值，比如"com.demo.MainActivity"
     * @param attributeName 指要移除的组件的属性名称，如launchMode,不需要写android:launchMode
     */
    public PlugProcessor removePlugAttribute(String plug, String name, String attributeName) {
        List<Element> plugs = mApplicationElement.elements(plug);
        for (Element element : plugs) {
            if (element.attribute(ManifestProcessor.Manifest.NAME) != null && element.attribute(ManifestProcessor.Manifest.NAME).getValue().equals(name)) {
                if (element.attribute(attributeName) != null) {
                    element.remove(element.attribute(attributeName));
                }
            }
        }
        return this;
    }

    /**
     * 包名替换的时候替换组件内相关包名部分
     *
     * @param oldPkgName 老包名
     * @param newPkgName 新包名
     * @return
     */
    public PlugProcessor replacePlugsPkgName(String oldPkgName, String newPkgName) {
        for (Element element : mApplicationElement.elements()) {
            //不对application做处理
            replaceElementAttrValueByPkgName(element, oldPkgName, newPkgName);
        }
        return this;
    }

    /**
     * 递归处理节点属性包名替换
     *
     * @param element
     * @param oldPkgName
     * @param newPkgName
     */
    private void replaceElementAttrValueByPkgName(Element element, String oldPkgName, String newPkgName) {
        //处理自身属性
        List<Attribute> attributes = element.attributes();
        if (attributes != null && attributes.size() > 0) {
            for (int i = 0; i < attributes.size(); i++) {
                //不对android:name属性做处理
                if (!element.attribute(i).getName().equals(ManifestProcessor.Manifest.NAME)) {
                    String value = element.attribute(i).getStringValue();
                    if (value.contains(oldPkgName)) {
                        value = value.replace(oldPkgName, newPkgName);
                        element.attribute(i).setValue(value);
                    }
                }
            }
        }
        //处理子节点
        List<Element> childElements = element.elements();
        if (childElements != null && childElements.size() > 0) {
            for (Element child : childElements) {
                replaceElementAttrValueByPkgName(child, oldPkgName, newPkgName);
            }
        }
    }

    /**
     * 删除组件
     *
     * @param plug 组件类别，如activity，service, provider等
     * @param name 组件名称，指name属性，比如"com.demo.MainActivity"
     */
    public PlugProcessor removePlug(String plug, String name) {
        List<Element> plugs = mApplicationElement.elements(plug);
        for (Element element : plugs) {
            if (element.attribute(ManifestProcessor.Manifest.NAME) != null && element.attribute(ManifestProcessor.Manifest.NAME).getValue().equals(name)) {
                mApplicationElement.remove(element);
            }
        }
        return this;
    }

    /**
     * 删除组件
     *
     * @param plug  组件类别，如activity，service, provider等
     * @param regex 组件名称正则匹配
     */
    public PlugProcessor removePlugRegex(String plug, String regex) {
        List<Element> plugs = mApplicationElement.elements(plug);
        for (Element element : plugs) {
            if (element != null) {
                Attribute attribute = element.attribute(ManifestProcessor.Manifest.NAME);
                if (attribute != null) {
                    String value = attribute.getValue();
                    if (!StringUtils.isEmpty(value)) {
                        Pattern pattern = Pattern.compile(regex);
                        Matcher matcher = pattern.matcher(value);
                        if (matcher.find()) {
                            mApplicationElement.remove(element);
                            LogUtil.logXml("找到了" + plug + "   " + regex + "，删除");
                        }
                    }
                }
            }
        }
        return this;
    }

    /**
     * 如果存在多个相同plug，则删除其他的保留一个
     *
     * @param plug
     * @param name
     * @return
     */
    public Element remainSinglePlug(String plug, String name) {
        List<Element> plugs = mApplicationElement.elements(plug);
        boolean founded = false;
        Element element = null;
        for (int i = 0; i < plugs.size(); i++) {
            Element tempElement = plugs.get(i);
            Attribute attribute = tempElement.attribute(ManifestProcessor.Manifest.NAME);
            if (attribute != null && attribute.getValue().equals(name)) {
                if (!founded) {
                    element = tempElement;
                    founded = true;
                } else {
                    removePlug(tempElement);
                }
            }
        }
        return element;
    }

    /**
     * 删除组件
     *
     * @param element 组件节点
     */
    public PlugProcessor removePlug(Element element) {
        if (element != null)
            mApplicationElement.remove(element);
        return this;
    }

    public Element getPlug(String plug, String name) {
        List<Element> plugs = mApplicationElement.elements(plug);
        for (int i = 0; i < plugs.size(); i++) {
            Element element = plugs.get(i);
            if (element.attribute(ManifestProcessor.Manifest.NAME) != null && element.attribute(ManifestProcessor.Manifest.NAME).getValue().equals(name)) {
                return element;
            }
        }
        return null;
    }

    public List<Element> getAllPlug() {
        return mApplicationElement.elements();
    }

    public Element getApplicationElement() {
        return mApplicationElement;
    }

    /**
     * 增加组件
     *
     * @param plug     需要增加的组件
     * @param forceAdd 假如组件已存在是否强制增加，true则移除原组件，增加新组件，false则不处理
     * @return
     */
    public PlugProcessor addPlug(Element plug, boolean forceAdd) {
        List<Element> plugs = getAllPlug();
        AndroidNameComparor comparor = new AndroidNameComparor();
        Element sameNamePlug = null;
        for (Element hasPlug : plugs) {
            if (comparor.compare(hasPlug, plug)) {
                sameNamePlug = hasPlug;
                break;
            }
        }
        //存在相同节点，如果强制增加则先移除节点，非强制添加则不处理
        if (sameNamePlug != null) {
            if (!forceAdd) {
                return this;
            }
            //移除节点
            mApplicationElement.remove(sameNamePlug);
        }
        mApplicationElement.add(plug);
        return this;
    }

    /*********************************常用操作**********************************/

    /**
     * 更换Activity 启动模式
     *
     * @param activity   要改变启动模式的activity
     * @param launchMode 结果启动模式
     * @param forceAdd   是否强制增加，true时，没有启动模式属性就增加，false时无启动模式则不改
     */
    private PlugProcessor resetActivityLaunchMode(String activity, String launchMode, boolean forceAdd) {
        resetPlugAttribute(PLUG.ACTIVITY, activity, PLUG.LAUNCH_MODE, launchMode, forceAdd);
        return this;
    }

    /**
     * 替换启动Activity
     *
     * @param activity 当activity为null时，表示只去掉启动activity，不增加设置其他activity为启动activity
     *                 <intent-filter>
     *                 <action android:channelName="android.intent.action.MAIN"/>
     *                 <category android:channelName="android.intent.category.LAUNCHER"/>
     *                 </intent-filter>
     */
    public PlugProcessor resetLaunchActivity(String activityName) {
        List<Element> activities = mApplicationElement.elements(PLUG.ACTIVITY);
        Element targetActivity = null;
        //移除原主activity的子节点
        for (Element activity : activities) {
            if (activity.attribute(ManifestProcessor.Manifest.NAME).getValue().equals(activityName)) {
                targetActivity = activity;
            } else {
                List<Element> intentFilterElements = activity.elements(PLUG.INTENT_FILTER);
                if (intentFilterElements == null && intentFilterElements.size() == 0) {
                    continue;
                } else {
                    for (Element intentFilterElement : intentFilterElements) {
                        List<Element> actionElements = intentFilterElement.elements(PLUG.ACTION);
                        if (actionElements != null && actionElements.size() > 0) {
                            for (Element actionElement : actionElements) {
                                //判断是主activity的条件：<action android:channelName="android.intent.action.MAIN"/>
                                if (actionElement.attribute(ManifestProcessor.Manifest.NAME) != null && actionElement.attribute(ManifestProcessor.Manifest.NAME).getValue().equals("android.intent.action.MAIN")) {
                                    activity.clearContent();
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        //设置主activity
        if (targetActivity != null) {
            //增加Intent-Filter节点
            Element intentFilterElement = new DefaultElement(PLUG.INTENT_FILTER);
            Element actionElement = new DefaultElement(PLUG.ACTION);
            actionElement.addAttribute(new QName(ManifestProcessor.Manifest.NAME, mManifestProcessor.getAndroidNameSpace()), "android.intent.action.MAIN");
            Element categoryElement = new DefaultElement(PLUG.CATEGORY);
            categoryElement.addAttribute(new QName(ManifestProcessor.Manifest.NAME, mManifestProcessor.getAndroidNameSpace()), "android.intent.category.LAUNCHER");
            intentFilterElement.add(actionElement);
            intentFilterElement.add(categoryElement);
            targetActivity.add(intentFilterElement);
        }
        return this;
    }

    public Element getLaunchActivity() {
        List<Element> activities = mApplicationElement.elements(PLUG.ACTIVITY);
        for (Element activity : activities) {
            List<Element> intentFilterElements = activity.elements(PLUG.INTENT_FILTER);
            if (intentFilterElements == null && intentFilterElements.size() == 0) {
                continue;
            } else {
                for (Element intentFilterElement : intentFilterElements) {
                    List<Element> actionElements = intentFilterElement.elements(PLUG.ACTION);
                    if (actionElements != null && actionElements.size() > 0) {
                        for (Element actionElement : actionElements) {
                            //判断是主activity的条件：<action android:channelName="android.intent.action.MAIN"/>
                            if (actionElement.attribute(ManifestProcessor.Manifest.NAME) != null && actionElement.attribute(ManifestProcessor.Manifest.NAME).getValue().equals("android.intent.action.MAIN")) {
                                return activity;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public PlugProcessor resetLaunchMode(String launchMode) {
        Element launchActivity = getLaunchActivity();
        String name = launchActivity.attribute("name").getStringValue();
        resetActivityLaunchMode(name, launchMode, true);
        return this;
    }

    public String getLaunchMode() {
        Element launchActivity = getLaunchActivity();
        Attribute attribute = launchActivity.attribute("launchMode");
        if (attribute == null) {
            return null;
        }
        return attribute.getStringValue();
    }

    /**
     * android:authorities="包名.xxx"
     *
     * @param providerName
     * @param auth
     * @return
     */
    public PlugProcessor resetProviderAuth(String providerName, String auth) {
        List<Element> providerElements = mApplicationElement.elements(PLUG.PROVIDER);
        if (providerElements != null && providerElements.size() > 0) {
            int count = 0;
            for (Element provider : providerElements) {
                LogUtil.d(provider.attributeValue("name"));
                if (provider.attribute(ManifestProcessor.Manifest.NAME) != null && provider.attribute(ManifestProcessor.Manifest.NAME).getValue().equals(providerName)) {
                    provider.attribute(PLUG.AUTHORITIES).setValue(auth);
                    LogUtil.i("替换成功，新auth：" + auth);
                    count++;
                }
            }
            if (count == 0) {
                LogUtil.i("resetProviderAuth：没有替换成功");
            }
        } else {
            LogUtil.d(providerName + "不存在");
        }
        return this;
    }

    /**
     * <meta-data android:name="wx_appid" android:value="wxa948197f75f248df"/>
     * 替换meta-data
     *
     * @param name
     * @param value
     * @return
     */
    public PlugProcessor resetMetadata(String name, String value) {
        return resetPlugAttribute(ManifestProcessor.Manifest.META_DATA, name, ManifestProcessor.Manifest.VALUE, value, false);
    }

    public ActivityProcessor getActivityProcessor() {
        return mActivityProcessor;
    }

    public ServiceProcessor getServiceProcessor() {
        return mServiceProcessor;
    }

    /**
     * 组件相关常量
     */
    public interface PLUG {

        String NAME = "name";

        String ACTIVITY = "activity";

        String ACTIVITY_ALIAS = "activity-alias";

        String SERVICE = "service";

        String PROVIDER = "provider";

        String STANDARD = "standard";

        String SINGLE_TOP = "singleTop";

        String SINGLE_TASK = "singleTask";

        String SINGLE_INSTANCE = "singleInstance";

        String LAUNCH_MODE = "launchMode";

        String INTENT_FILTER = "intent-filter";

        String ACTION = "action";

        String CATEGORY = "category";

        String AUTHORITIES = "authorities";

    }

}
