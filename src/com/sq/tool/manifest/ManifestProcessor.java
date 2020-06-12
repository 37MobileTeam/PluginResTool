package com.sq.tool.manifest;

import com.sq.tool.xml.XmlProcessor;
import org.dom4j.DocumentException;
import org.dom4j.Namespace;

/**
 * @author zhuxiaoxin
 * Android Manifest文件相关处理
 * 作用：
 * 1、权限操作器操作权限相关
 * 2、组件操作器操作组件相关
 * 3、应用操作器操作包名，应用名等
 * 支持链式调用
 */
public class ManifestProcessor extends XmlProcessor {

    //权限操作器
    private PermissionProcessor mPermissionProcessor;

    //组件操作器
    private PlugProcessor mPlugProcessor;

    //应用操作器
    private AppProcessor mAppProcessor;

    //android命名空间
    private Namespace mAndroidNameSpace;

    public ManifestProcessor(String fileName) throws DocumentException {
        super(fileName);
        mPermissionProcessor = new PermissionProcessor(this);
        mPlugProcessor = new PlugProcessor(this);
        mAppProcessor = new AppProcessor(this);
    }

    public PermissionProcessor getPermissionProcessor() {
        return mPermissionProcessor;
    }

    public PlugProcessor getPlugProcessor() {
        return mPlugProcessor;
    }

    public AppProcessor getAppProcessor() {
        return mAppProcessor;
    }

    /*
     * Manifest通用常量
     */
    public interface Manifest {
        String META_DATA = "meta-data";
        String ANDROID = "android";
        String NAME = "name";
        String APPLICATION = "application";
        String VALUE = "value";
        String ACTIVITY_ALIAS= "activity-alias";
    }

    /**
     * 获取android命名空间
     * @return
     */
    public Namespace getAndroidNameSpace() {
        if (mAndroidNameSpace == null) {
            mAndroidNameSpace = new Namespace(Manifest.ANDROID, "http://schemas.android.com/apk/res/android");
        }
        return mAndroidNameSpace;
    }

}
