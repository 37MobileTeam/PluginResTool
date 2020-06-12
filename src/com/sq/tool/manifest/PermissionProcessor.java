package com.sq.tool.manifest;

import com.sq.tool.FileUtil;
import com.sq.tool.LogUtil;
import com.sq.tool.StringUtils;
import org.dom4j.*;
import org.dom4j.tree.DefaultElement;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhuxiaoxin
 * 权限操作器
 * Manifest权限相关操作，支持链式调用
 */
public class PermissionProcessor {

    private ManifestProcessor mManifestProcessor;

    public PermissionProcessor(ManifestProcessor manifestProcessor){
        mManifestProcessor = manifestProcessor;
    }

    /**
     * 增加权限，通过文件复制方式
     * 默认去重
     * @param permissionFile
     * @return
     */
    public PermissionProcessor addPermissionsByFile(String permissionFile){
        try {
            Document combinePermissionDocument = null;
            try {
                combinePermissionDocument = mManifestProcessor.getReader().read(permissionFile);
            } catch (DocumentException e) {
                //兼容以往data没有根标签的方式
                String content = FileUtil.read(permissionFile);
                content = "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\">" + content + "</manifest>";
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content.getBytes());
                combinePermissionDocument = mManifestProcessor.getReader().read(byteArrayInputStream);
            }

            Element manifestElement = mManifestProcessor.getSelfDocument().getRootElement();
            if (manifestElement == null) {
                LogUtil.e("manifest 节点为空，无法添加");
                return this;
            }
            List<Element> list = combinePermissionDocument.getRootElement().elements();
            List<Element> defaultPermissions = getUsePermissions();
            if (list != null && list.size() > 0) {
                for (Element element1 : list) {
                    boolean flag = false;
                    for (Element permissionElement : defaultPermissions) {
                        if (element1.attribute(ManifestProcessor.Manifest.NAME).getValue().equals(permissionElement.attribute(ManifestProcessor.Manifest.NAME).getValue())) {
                            flag = true;
                        }
                    }
                    if (!flag) {
                        manifestElement.add(element1.createCopy());
                    }
                }
            }
        } catch (DocumentException e) {
            LogUtil.e("很可能是xml文件格式错误");
            e.printStackTrace();
        }
        return this;
    }

    /**
     * 替换权限中包含的包名
     */
    public PermissionProcessor replacePermissionPkgName(String oldPkgName, String pkgName){
        //处理use-permission
        List<Element> usePermissions = getUsePermissions();
        for (int i = 0; i < usePermissions.size(); i++) {
            Attribute name = usePermissions.get(i).attribute("name");
            if (name != null) {
                String usePermissionName = name.getStringValue();
                if (usePermissionName.contains(oldPkgName)) {
                    usePermissionName = usePermissionName.replace(oldPkgName, pkgName);
                    name.setValue(usePermissionName);
                }
            }
        }
        //处理自定义的permission
        List<Element> permissions = getCustomPermissions();
        for (int i = 0; i < permissions.size(); i++) {
            Attribute name = permissions.get(i).attribute("name");
            if (name != null) {
                String usePermissionName = name.getStringValue();
                if (usePermissionName.contains(oldPkgName)) {
                    usePermissionName = usePermissionName.replace(oldPkgName, pkgName);
                    name.setValue(usePermissionName);
                }
            }
        }
        return this;
    }

    /**
     * 增加单个或多个权限，可变参数形式添加
     * @param permissionNames
     */
    public PermissionProcessor addPermissions(String... permissionNames) {
        for (String permissionName : permissionNames) {
            Element element = new DefaultElement(Permission.USES_PERMISSION);
            element.addAttribute(new QName(ManifestProcessor.Manifest.NAME, mManifestProcessor.getAndroidNameSpace()), permissionName);
            mManifestProcessor.getSelfDocument().getRootElement().add(element);
        }
        return this;
    }

    /**
     * 去掉权限，支持单个或多个权限去除，可变参数形式添加
     * @param permissionNames
     */
    public PermissionProcessor removePermissions(String... permissionNames) {
        List<String> shouldRemove = Arrays.asList(permissionNames);
        List<Element> permissions = getUsePermissions();
        List<Element> shouldRemovePermissions = new ArrayList<>();
        for (int i = 0; i < permissions.size(); i++) {
            Element element = permissions.get(i);
            if (shouldRemove.contains(element.attribute(ManifestProcessor.Manifest.NAME).getValue())) {
                shouldRemovePermissions.add(element);
            }
        }
        permissions.removeAll(shouldRemovePermissions);
        return this;
    }

    /**
     * 替换permission中的name为新值，救值为original的正则匹配
     */
    public PermissionProcessor resetPermissionRegex(String original,String target){
        //处理use-permission
        List<Element> usePermissions = getUsePermissions();
        for (int i = 0; i < usePermissions.size(); i++) {
            Attribute name = usePermissions.get(i).attribute("name");
            if (name != null) {
                String usePermissionName = name.getStringValue();
                if(!StringUtils.isEmpty(usePermissionName)){
                    Pattern pattern=Pattern.compile(original);
                    Matcher matcher=pattern.matcher(usePermissionName);
                    if(matcher.find()){
                        name.setValue(target);
                    }
                }
            }
        }
        //处理自定义的permission
        List<Element> permissions = getCustomPermissions();
        for (int i = 0; i < permissions.size(); i++) {
            Attribute name = permissions.get(i).attribute("name");
            if (name != null) {
                String usePermissionName = name.getStringValue();
                if(!StringUtils.isEmpty(usePermissionName)){
                    Pattern pattern=Pattern.compile(original);
                    Matcher matcher=pattern.matcher(usePermissionName);
                    if(matcher.find()){
                        name.setValue(target);
                    }
                }
            }
        }
        return this;
    }

    /**
     * 对应<use-permission></use-permission>
     * 获取已有的权限，注意：内存中的，并非获取文件中的
     * @return
     */
    public List<Element> getUsePermissions() {
        return mManifestProcessor.getSelfDocument().getRootElement().elements(Permission.USES_PERMISSION);
    }

    /**
     * 获取自定义权限
     * 对应<permission></permission>
     * @return
     */
    public List<Element> getCustomPermissions() {
        return mManifestProcessor.getSelfDocument().getRootElement().elements(Permission.PERMISSION);
    }

}
