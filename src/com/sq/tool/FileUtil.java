package com.sq.tool;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FileUtil {

    /**
     * 读取所有的行
     * @param filePath
     * @return
     */
    public static ArrayList<String> readAllLines(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        ArrayList<String> contentLines = new ArrayList<>();
        BufferedReader br = null;
        String line = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
            while ((line = br.readLine()) != null) {
                //所有的行都加到list中
                contentLines.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭流
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    br = null;
                }
            }
        }
        return contentLines;
    }


    /**
     * 读取包含指定内容的行
     * @param filePath
     * @param content
     * @return
     */
    public static ArrayList<String> readAllLines(String filePath, String content) {
        //判断表示和内容不能为空，否则不能查找
        if (content == null || "".equals(content)) {
            return null;
        }
        //处理读取的逻辑
        ArrayList<String> contentLines = new ArrayList<String>();
        BufferedReader br = null;
        String line = null;

        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));

            while ((line = br.readLine()) != null) {
                //找到特定的行数
                if (line.contains(content)) {
                    contentLines.add(line);
                }

            }

        } catch (Exception e) {

        } finally {
            // 关闭流
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    br = null;
                }
            }
        }

        return contentLines;
    }


    // 复制文件
    public static void copyFile(File sourceFile, File targetFile) {

        try {
            // 新建文件输入流并对它进行缓冲
            FileInputStream input = new FileInputStream(sourceFile);
            BufferedInputStream inBuff = new BufferedInputStream(input);

            // 新建文件输出流并对它进行缓冲
            if (!targetFile.exists()) {
                targetFile.createNewFile();
            }
            FileOutputStream output = new FileOutputStream(targetFile);
            BufferedOutputStream outBuff = new BufferedOutputStream(output);

            // 缓冲数组
            byte[] b = new byte[1024 * 5];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }
            // 刷新此缓冲的输出流
            outBuff.flush();

            // 关闭流
            inBuff.close();
            outBuff.close();
            output.close();
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 差异覆盖
     * <p>
     * 将sourceDir中有的而targetDir中没有的文件，拷贝至targetDir
     *
     * @param sourceDir
     * @param targetDir
     */
    public static void diffCover(String sourceDir, String targetDir) {
        File sourceFile = new File(sourceDir);
        File targetFile = new File(targetDir);
        if (sourceFile.isDirectory() && sourceFile.listFiles() != null) {
            if (!targetFile.exists() || targetFile.listFiles() == null) {
                copyDirectiory(sourceDir, targetDir, false);
            } else {
                List<String> targetDirFiles = new ArrayList<>();
                List<String> targetDirDirs = new ArrayList<>();
                for (File file : targetFile.listFiles()) {
                    if (file.isFile()) {
                        targetDirFiles.add(file.getName());
                    } else if (file.isDirectory()) {
                        targetDirDirs.add(file.getName());
                    }
                }
                for (File file : sourceFile.listFiles()) {
                    File target = new File(targetDir + File.separator + file.getName());
                    if (file.isFile() && !targetDirFiles.contains(file.getName())) {
                        copyFile(file, target);
                    } else if (file.isDirectory() && !targetDirDirs.contains(file.getName())) {
                        copyDirectiory(file.getAbsolutePath(), target.getAbsolutePath(), false);
                    } else if (file.isDirectory() && targetDirDirs.contains(file.getName())) {
                        diffCover(file.getAbsolutePath(), target.getAbsolutePath());
                    }
                }
            }
        }
    }

    public static boolean isExist(String path) {
        return new File(path).exists();
    }

    public static void copyDirectiory(String sourceDir, String targetDir, boolean reset) {
        File file = new File(sourceDir);
        if (!file.exists()) {
            LogUtil.d("源文件都不存在，不需要复制: " + sourceDir);
            return;
        }
        if (file.getName().equals(".DS_Store")) {
            LogUtil.d("文件时mac系统的压缩生成的.DS_Store，不处理");
            return;
        }
        if (MachineUtils.getOSName() == MachineUtils.IS_LINUX_OS) {
            //Linux下需要特殊的复制方式，需要大小写不敏感。
            copyDirectioryInLinux(sourceDir, targetDir, reset);
        } else {
            //Mac和windows下用此复制
            copyDirectioryNormal(sourceDir, targetDir, reset);
        }

    }

    public static void copyDirectoryWithFilter(String sourceDir, String targetDir, boolean reset, FileFilter fileFilter) {
        File file = new File(sourceDir);
        if (!file.exists()) {
            LogUtil.d("源文件都不存在，不需要复制: " + sourceDir);
            return;
        }
        if (file.getName().equals(".DS_Store")) {
            LogUtil.d("文件时mac系统的压缩生成的.DS_Store，不处理");
            return;
        }
        if (MachineUtils.getOSName() == MachineUtils.IS_LINUX_OS) {
            //Linux下需要特殊的复制方式，需要大小写不敏感。
            copyDirectioryInLinuxWithFilter(sourceDir, targetDir, reset, fileFilter);
        } else {
            //Mac和windows下用此复制
            copyDirectoryNormalWithFilter(sourceDir, targetDir, reset, fileFilter);
        }
    }

    // 复制文件夹
    public static void copyDirectioryNormal(String sourceDir, String targetDir,
                                            boolean reset) {
        // 新建目标目录
        (new File(targetDir)).mkdirs();
        // 获取源文件夹当前下的文件或目录
        File[] file = (new File(sourceDir)).listFiles();
        for (int i = 0; i < file.length; i++) {
            if (file[i].isFile()) {
                // 源文件
                File sourceFile = file[i];
                String filename = file[i].getName();//.toLowerCase();
                // 目标文件
                File targetFile = new File(
                        new File(targetDir).getAbsolutePath() + File.separator
                                + filename);

                copyFile(sourceFile, targetFile);
            }
            if (file[i].isDirectory()) {
                // 准备复制的源文件夹
                String dir1 = sourceDir + File.separator + file[i].getName();
                // 准备复制的目标文件夹
                String dir2 = targetDir + File.separator + file[i].getName();
                copyDirectiory(dir1, dir2, reset);
            }
        }
    }

    public static void copyDirectoryNormalWithFilter(String sourceDir, String targetDir,
                                                     boolean reset, FileFilter fileFilter) {
        // 新建目标目录
        (new File(targetDir)).mkdirs();
        // 获取源文件夹当前下的文件或目录
        File[] file = (new File(sourceDir)).listFiles(fileFilter);
        for (int i = 0; i < file.length; i++) {
            if (file[i].isFile()) {
                // 源文件
                File sourceFile = file[i];
                String filename = file[i].getName();//.toLowerCase();
                // 目标文件
                File targetFile = new File(
                        new File(targetDir).getAbsolutePath() + File.separator
                                + filename);

                copyFile(sourceFile, targetFile);
            }
            if (file[i].isDirectory()) {
                // 准备复制的源文件夹
                String dir1 = sourceDir + File.separator + file[i].getName();
                // 准备复制的目标文件夹
                String dir2 = targetDir + File.separator + file[i].getName();
                copyDirectoryWithFilter(dir1, dir2, reset, fileFilter);
            }
        }
    }

    // 复制文件夹
    public static void copyDirectioryInLinux(String sourceDir, String targetDir,
                                             boolean reset) {

        File sourceDirFile = new File(sourceDir);
        if (!sourceDirFile.exists()) {
            //源路径不存在，直接返回
            LogUtil.e("copyDirectioryInLinux:源路径不存在，直接返回");
            return;
        }

        // 新建目标目录
        File tmpTargetFile = new File(targetDir);
        tmpTargetFile.mkdirs();
        if(!tmpTargetFile.exists()){
            LogUtil.i("文件夹创建失败");
            return;
        }
        // 获取源文件夹当前下的文件或目录
        File[] file = sourceDirFile.listFiles();
        for (int i = 0; i < file.length; i++) {
            if (file[i].isFile()) {
                // 源文件
                File sourceFile = file[i];
                String filename = file[i].getName();//.toLowerCase();
                //把当前文件名，转成小写的文件名
                String filename2 = file[i].getName();//.toLowerCase();
                String filename_lowercase = filename2.toLowerCase();
                // 目标文件
                File targetFile = new File(
                        new File(targetDir).getAbsolutePath() + File.separator
                                + filename);
                copyFile(sourceFile, targetFile);
            }
            if (file[i].isDirectory()) {
                // 准备复制的源文件夹
                String dir1 = sourceDir + File.separator + file[i].getName();
                // 准备复制的目标文件夹
                String dir2 = targetDir + File.separator + file[i].getName();
                copyDirectiory(dir1, dir2, reset);
            }
        }
    }

    // 复制文件夹
    public static void copyDirectioryInLinuxWithFilter(String sourceDir, String targetDir,
                                             boolean reset, FileFilter fileFilter) {

        File sourceDirFile = new File(sourceDir);
        if (!sourceDirFile.exists()) {
            //源路径不存在，直接返回
            LogUtil.e("copyDirectioryInLinux:源路径不存在，直接返回");
            return;
        }

        // 新建目标目录
        File tmpTargetFile = new File(targetDir);
        tmpTargetFile.mkdirs();
        if(!tmpTargetFile.exists()){
            LogUtil.i("文件夹创建失败");
            return;
        }
        // 获取源文件夹当前下的文件或目录
        File[] file = sourceDirFile.listFiles(fileFilter);
        for (int i = 0; i < file.length; i++) {
            if (file[i].isFile()) {
                // 源文件
                File sourceFile = file[i];
                String filename = file[i].getName();//.toLowerCase();
                //把当前文件名，转成小写的文件名
                String filename2 = file[i].getName();//.toLowerCase();
                String filename_lowercase = filename2.toLowerCase();
                // 目标文件
                File targetFile = new File(
                        new File(targetDir).getAbsolutePath() + File.separator
                                + filename);
                copyFile(sourceFile, targetFile);
            }
            if (file[i].isDirectory()) {
                // 准备复制的源文件夹
                String dir1 = sourceDir + File.separator + file[i].getName();
                // 准备复制的目标文件夹
                String dir2 = targetDir + File.separator + file[i].getName();
                copyDirectoryWithFilter(dir1, dir2, reset, fileFilter);
            }
        }
    }

    /**
     * 读取文件内容
     *
     * @param filePath
     * @return
     */
    public static String read(String filePath) {
        BufferedReader br = null;
        String line = null;
        StringBuffer buf = new StringBuffer();

        try {
            // 根据文件路径创建缓冲输入流
            br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));

            // 循环读取文件的每一行, 对需要修改的行进行修改, 放入缓冲对象中
            while ((line = br.readLine()) != null) {
                // // 此处根据实际需要修改某些行的内容
                // if (line.startsWith("a")) {
                // buf.append(line).append(" start with a");
                // }
                // else if (line.startsWith("b")) {
                // buf.append(line).append(" start with b");
                // }
                // // 如果不用修改, 则按原来的内容回写
                // else {
                buf.append(line);
                // }
                buf.append(System.getProperty("line.separator"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭流
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    br = null;
                }
            }
        }

        return buf.toString();
    }

    /**
     * 将内容回写到文件中
     *
     * @param filePath
     * @param content
     */
    public static void write(String filePath, String content) {
        BufferedWriter bw = null;

        try {
            // 根据文件路径创建缓冲输出流
            //bw = new BufferedWriter(new FileWriter(filePath));
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"));
            // 将内容写入文件中
            bw.write(content);
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭流
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    bw = null;
                }
            }
        }
    }

    private static void listFolder(String path, String sStr, String xStr) {

        String ssStr = sStr.replaceAll("\\.", "/");
        String xxStr = xStr.replaceAll("\\.", "/");

        File file = new File(path);
        File[] list = file.listFiles();
        for (int i = 0; i < list.length; i++) {
            if (list[i].isDirectory())
                listFolder(list[i].getPath(), sStr, xStr);
            else {
                String filePath = list[i].getPath();
                String content = read(filePath);
                content = content.replaceAll(sStr, xStr);
                content = content.replaceAll(ssStr, xxStr);
                write(filePath, content);
                // LogUtil.d(filePath+" OK");
            }
        }
    }

    public static void deleteDir(String path){
        delDir(new File(path));
    }

    public static void delDir(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                File files[] = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    delDir(files[i]);
                }
            }
            file.delete();
        } else {
            LogUtil.d("所删除的文件不存在！" + '\n');
        }
    }

    public static void replaceDir(File file, final String content, final String replacement) {

        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                replaceDir(files[i], content, replacement);
            } else {
                String sContent = FileUtil.read(files[i].getAbsolutePath());

                //TODO ?
                if (sContent.contains(content)) {
                    LogUtil.d("找到替换内容为【" + content + "】 的文件:" + files[i].getName());
                }

                sContent = sContent.replaceAll(content, replacement);
                FileUtil.write(files[i].getAbsolutePath(), sContent);

            }
        }

    }

    /*
     * 带文件过滤器的替换方法
     * @dataTime 2015年1月13日11:20:22
     */
    public static void replaceDir(File file, final String content, final String replacement, FileFilter filter) {

        if (!file.exists()) {

            LogUtil.d("不存在" + file.getAbsolutePath() + "----- 已跳过处理");
            return;
        }

        File[] files = file.listFiles(filter);
        for (int i = 0; i < files.length; i++) {

//        	LogUtil.d("-----readFile:"+files[i].getAbsolutePath()+"------");

            if (files[i].isDirectory()) {
                replaceDir(files[i], content, replacement, filter);
            } else {
                String sContent = FileUtil.read(files[i].getAbsolutePath());

                if (sContent.contains(content)) {
                    LogUtil.d("找到替换内容为【" + content + "】 的文件:" + files[i].getName());

                    sContent = sContent.replaceAll(content, replacement);
                    FileUtil.write(files[i].getAbsolutePath(), sContent);
                }

            }
        }

    }

    /**
     * 删除指定目录的第三方支付
     *
     * @param filePath
     */
    public static void delPayFiles(String filePath) {
        File root = new File(filePath);
        File[] files = root.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {

                delPayFiles(file.getAbsolutePath());

            } else {

            }
            if (file.getName().equals("alipay")
                    || file.getName().equals("unionpay")) {
                FileUtil.delDir(file);
            }
        }
    }


    public static void main(String[] args) {
        LogUtil.d("com.app.lo->" + getFilePathFrom("com.app.lo"));
    }

    /**
     * 正则替换 android manifest 配置文件参数,
     * 匹配<meta-data android:channelName="key" android:value="value"
     *
     * @param sdkPlugin sdk plugin
     * @param prop      prop
     * @param map       map
     */
    public static void replaceContent(String sdkPlugin, Properties prop,
                                      Map<String, String> map) {

        if (map == null || map.size() == 0) {
            LogUtil.d("replace Content err !! -> map is null or empty ");
            return;
        }

        String content = FileUtil.read(sdkPlugin);
        Pattern p = null;
        Matcher m = null;
        String propKey = "";
        String propValue = "";
        Object[] mapValue = null;
        Object[] mapKey = null;

        if (map.keySet().toArray().length != 0) {
            mapValue = map.values().toArray();
            mapKey = map.keySet().toArray();
        }

        if (mapKey == null) {
            LogUtil.d("replace Content err !! -> mapKey is null !!!!!! ");
            return;
        }

        try {
            for (int i = 0; i < map.size(); i++) {
                propKey = mapKey[i].toString();
                propValue = mapValue[i].toString();
                LogUtil.d("replaceContent propKey : " + propKey);
                LogUtil.d("replaceContent propValue : " + propValue);

                p = Pattern.compile("<meta-data android:name=\"" + propValue + "\" android:value=\"(.*?)\"");
                m = p.matcher(content);
                boolean findResult = m.find();
                LogUtil.d("replace Content findResult : " + findResult);
                String key = m.group(1);
                LogUtil.d("replace Content key : " + key);
                content = content.replace("android:channelName=\"" + propValue + "\" android:value=\"" + key,
                        "android:channelName=\"" + propValue + "\" android:value=\"" + prop.getProperty(propKey));
                LogUtil.d(propKey + " >>>>>>> " + propValue + " ---> " + prop.getProperty(propKey));
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(" !!!!! 配置meta值时出错 !!!!!  : " + e.toString());
            return;
        }

        FileUtil.write(sdkPlugin, content);
    }

    /**
     * 读取需要替换资源的文件路径
     *
     * @param filePath 文件路径
     * @return
     */
    public static List<String> readReplasePath(String filePath) {

        BufferedReader br = null;
        String line = null;
        StringBuffer buf = new StringBuffer();
        List listPath = new ArrayList<String>();

        File file_path = new File(filePath);

        if (!file_path.isFile()) {
            LogUtil.d("当前读取的不是一个文件,读取失败！");
            return null;
        }

        try {
            // 根据文件路径创建缓冲输入流
            br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));

            // 循环读取文件的每一行, 对需要修改的行进行修改, 放入缓冲对象中
            while ((line = br.readLine()) != null) {
                buf.append(line);
                listPath.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭流
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    br = null;
                }
            }
        }

        return listPath;
    }

    /**
     * 文件重命名
     *
     * @param path    文件目录
     * @param oldName 原来的文件名
     * @param newName 新文件名
     */
    public static void renameFile(String path, String oldName, String newName) {

        if (!oldName.equals(newName)) {// 新的文件名和以前文件名不同时,才有必要进行重命名
            File oldfile = new File(path + "/" + oldName);
            File newfile = new File(path + "/" + newName);
            if (!oldfile.exists()) {
                LogUtil.d("重命名的文件不存在!");
                return;// 重命名文件不存在
            }
            if (newfile.exists()) {// 若在该目录下已经有一个文件和新文件名相同，则不允许重命名
                LogUtil.d(newName + "已经存在！删除" + oldfile + "文件");
                FileUtil.delDir(oldfile);
            } else {
                oldfile.renameTo(newfile);
            }
        } else {
            LogUtil.d("新文件名和旧文件名相同...");
        }

    }

    /**
     * 从文件中读取，含有某一段内容的某一行
     *
     * @param filePath
     * @param content
     * @return
     */
    public static String readLine(String filePath, String content) {

        if (content == null || "".equals(content)) {

            return null;
        }

        BufferedReader br = null;
        String line = null;

        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));

            while ((line = br.readLine()) != null) {
                //找到特定的行数
                if (line.contains(content)) {

                    return line;
                }
            }

        } catch (Exception e) {

        } finally {
            // 关闭流
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    br = null;
                }
            }
        }

        return line;
    }

    /**
     * 得到AndroidManifest中类的名称对应的文件路径，其实就是将"."换成"/"
     * 如：com.demo.Main的路径就是com/demo/Main
     * 但是要考虑不同系统下的兼容性，不能直接写/
     *
     * @param name
     */
    public static String getFilePathFrom(String name) {

        StringBuilder namePath = new StringBuilder();

        if (name != null && !"".equals(name) && name.contains(".")) {

            String[] names = name.split("\\.");


            for (int i = 0; i < names.length; i++) {

                LogUtil.d("按顺序解析类名-channelName:" + names[i]);

                if (i == names.length - 1) {
                    namePath.append(names[i]);
                } else {
                    namePath.append(names[i] + File.separator);
                }
            }

            LogUtil.d(name + " 替换后的路径为：" + namePath.toString());

            return namePath.toString();

        } else {
            LogUtil.d("\n\n类名为空，或者不包含\".\"，无法转换！\n\n");
            return null;
        }

    }

    public static String hanleFileByLine(String filePath, FileUtilHelper<String> helper) {
        if (helper == null) {

            return null;
        }

        BufferedReader br = null;
        String line = null;
        StringBuilder sb = new StringBuilder();

        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));

            while ((line = br.readLine()) != null) {
                //找到特定的行数
                sb.append(helper.handleLine(line));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭流
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    br = null;
                }
            }
        }

        return sb.toString();
    }

    public interface FileUtilHelper<T> {
        public T handleLine(T lineContent);
    }

    /**
     *
     * @param dirs 需要扫描的目录
     * @param targetFilePath 被复制的文件
     * @param replacesFileName 需要替换的文件名称
     */
    public static void replaceFilesWithTarget(File[] dirs, String targetFilePath, String replacesFileName){
        LogUtil.d("目标文件路径为：" + targetFilePath);
        if (targetFilePath == null || "".equals(targetFilePath)){
            LogUtil.d("目标文件路径为空，不执行替换");
            return;
        }
        File targetFile = new File(targetFilePath);
        if (!targetFile.exists()){
            LogUtil.d("目标文件路径指向的文件不存在，不执行替换");
            return;
        }

        if (replacesFileName == null || "".equals(replacesFileName)) {
            LogUtil.d("被替换文件的名称为空了，不执行替换");
            return;
        }

        if (dirs != null){
            for (File dir: dirs){
                File temp = new File(dir.getPath() + File.separator + replacesFileName);
                if (temp.exists()){
                    LogUtil.d("存在需要替换的文件：" + temp.getPath() + ", 替换！");
                    FileUtil.copyFile(targetFile, temp);
                }
            }
        }
    }

}
