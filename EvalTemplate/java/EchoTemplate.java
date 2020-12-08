import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

public class Test {

    private ServletRequest Request;
    private ServletResponse Response;
    private HttpSession Session;

    @Override
    public boolean equals(Object obj){

        PageContext page = (PageContext) obj;
        this.Session=page.getSession();
        this.Response=page.getResponse();
        this.Request=page.getRequest();

        try {
            ServletOutputStream so=Response.getOutputStream();
            so.write("hello world".getBytes("UTF-8"));
            so.flush();
            so.close();
            page.getOut().clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}