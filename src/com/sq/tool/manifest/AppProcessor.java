package com.sq.tool.manifest;

import com.sq.tool.LogUtil;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.QName;

/**
 * Manifest中应用相关的一些设置
 * @author zhuxiaoxin
 * 设置包名，应用名等
 */
public class AppProcessor {

    private ManifestProcessor mManifestProcessor;
    private Element mApplicationElement;

    public AppProcessor(ManifestProcessor manifestProcessor) {
        mManifestProcessor = manifestProcessor;
        mApplicationElement = mManifestProcessor.getSelfDocument().getRootElement().element(APPLICATION.APPLICATION);
    }

    /**
     * 替换Application(只是单纯修改manifest的application的name属性，不会修改类继承关系)
     * @param application
     * @return
     */
    public AppProcessor resetApplication(String application){
        Attribute appNameAttr = mApplicationElement.attribute(ManifestProcessor.Manifest.NAME);
        if (appNameAttr == null) {
            QName name = new QName( "name", mManifestProcessor.getAndroidNameSpace());
            mApplicationElement.addAttribute(name, application);
        } else {
            mApplicationElement.attribute(ManifestProcessor.Manifest.NAME).setValue(application);
        }
        return this;
    }

    public String getApplicationName(){
        return mApplicationElement.attributeValue(ManifestProcessor.Manifest.NAME);
    }

    /**
     * 替换应用名称
     */
    public AppProcessor resetAppName(String appName) {
        Attribute attribute = mApplicationElement.attribute(APPLICATION.LABEL);
        if (attribute != null) {
            mApplicationElement.attribute(APPLICATION.LABEL).setValue(appName);
        } else {
            LogUtil.d("应用label标签未设置");
        }
        Element launchElement = mManifestProcessor.getPlugProcessor().getLaunchActivity();
        if(launchElement!=null){
            Attribute labelAttr = launchElement.attribute(APPLICATION.LABEL);
            if(labelAttr!=null){
                labelAttr.setValue(appName);
            }else{
                LogUtil.d("主activity未设置label标签");
            }
        }else{
            LogUtil.d("未找到主activity");
        }
        return this;
    }

    public AppProcessor setAttr(String attrName, String value, boolean isForceAdd) {
        Attribute attribute = mApplicationElement.attribute(attrName);
        if(attribute != null) {
            attribute.setValue(value);
        } else if (isForceAdd){
            mApplicationElement.addAttribute(new QName(attrName, mManifestProcessor.getAndroidNameSpace()), value);
        }
        return this;
    }

    /**
     * 替换包名
     */
    public AppProcessor resetPkgName(String pkgName) {
        Element manifestElement = mManifestProcessor.getSelfDocument().getRootElement();
        manifestElement.attribute(APPLICATION.PACKAGE).setValue(pkgName);
        return this;
    }

    public AppProcessor resetIcon(String iconName) {
        Attribute attribute = mApplicationElement.attribute(APPLICATION.ICON);
        if (attribute != null) {
            mApplicationElement.attribute(APPLICATION.ICON).setValue(iconName);
        } else {
            LogUtil.d("应用图标标签未设置");
        }
        return this;
    }

    public String getPkgName(){
        Element manifestElement = mManifestProcessor.getSelfDocument().getRootElement();
        return manifestElement.attribute(APPLICATION.PACKAGE).getStringValue();
    }

    public Element getApplicationElement(){
        return mApplicationElement;
    }

    public interface APPLICATION {
        String LABEL = "label";
        String PACKAGE = "package";
        String APPLICATION = "application";
        String ICON = "icon";
    }

}
