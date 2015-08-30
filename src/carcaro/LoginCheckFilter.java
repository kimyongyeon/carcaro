package carcaro;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginCheckFilter implements Filter
{
    
    @Override
    public void init(FilterConfig arg0) throws ServletException
    {
        
    }
    
    @Override
    public void destroy()
    {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException
    {
        
        //현재 HttpRequest를 HttpServletRequest로 캐스팅
        HttpServletRequest httpRequest = (HttpServletRequest) request;
       
        //현재 HttpSession을 구한다 새션이 존재하면 session반환 존재하지않으면 null반환
        HttpSession session = httpRequest.getSession(false);
       
        //현재 HttpResponse를 HttpServletResponse로 캐스팅
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        
        boolean login = false;
        if (session != null && session.getAttribute("id") != null) {
            login = true;
        }

        if(login || "loginConfirm".equals(request.getParameter("cmd")) )
        {
            //세션에 member가 있다
            chain.doFilter(request, response);
        }
        else
        {
            //세선에 member가 없으면 로그인 폼으로 다시 돌아가라
            httpResponse.sendRedirect(httpRequest.getContextPath()+"/index.jsp");
        }
       
    }

}