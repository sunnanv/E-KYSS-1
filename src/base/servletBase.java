package base;

import java.io.IOException;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 *  This class is the superclass for all servlets in the application. 
 *  It includes basic functionality required by many servlets, like for example a page head 
 *  written by all servlets, and the connection to the database. 
 *  
 *  This application requires a database.
 *  For username and password, see the constructor in this class.
 *  
 *  <p>The database can be created with the following SQL command: 
 *  mysql> create database base;
 *  <p>The required table can be created with created with:
 *  mysql> create table users(name varchar(10), password varchar(10), primary key (name));
 *  <p>The administrator can be added with:
 *  mysql> insert into users (name, password) values('admin', 'adminp'); 
 *  
 *  @author Martin Host
 *  @version 1.0
 *  
 */
public class servletBase extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	// Define states
	protected static final int LOGIN_FALSE = 0;
	protected static final int LOGIN_TRUE = 1;
    protected static final int LOGIN_LOGOUT_TOO_LONG = 2;
    protected static final long MAXINTERVAL = 15; // maxinterval mellan aktivitet på sidan.

    /**
     * Checks if a user is logged in or not.
     * @param request The HTTP Servlet request (so that the session can be found)
     * @return true if the user is logged in, otherwise false.
     */
    protected boolean loggedIn(HttpServletRequest request) {
    	HttpSession session = request.getSession(true);
        int state = LOGIN_FALSE;
    	Object objectState = session.getAttribute("state");
		if (objectState != null) 
			state = (Integer) objectState; 
		return (state == LOGIN_TRUE);
    }

    /**
     * Utility-funktion för att beräkna tidsdifferens mellan första entry till senaste
     * aktivitet/session aktivitet.
     * @param date1
     * @param date2
     * @param timeUnit
     * @return
     */
    public static long calcActivityTime(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.SECONDS.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    /**
     * Validerar aktiviteten och returnerar sant eller falskt om senaste aktivitet skedde
     * för maxInterval minuter sen.
     * @param session
     * @param maxInterval
     * @return
     */
    protected boolean validateActivity(HttpSession session, long maxInterval) {
        Date createTime = new Date(session.getCreationTime());
        if(session.isNew()) {
            // Get last access time of this web page.
            Date lastAccessTime =
                    new Date(session.getLastAccessedTime());
            System.out.println("Visitor at: " + lastAccessTime.toString());
            return true;
        }
        else {
            long difftime = 0;
            Date lastAccessTime = new Date(session.getLastAccessedTime());
            System.out.println("Diff: " + (difftime = calcActivityTime(createTime, lastAccessTime, TimeUnit.MILLISECONDS)));
            return(TimeUnit.MINUTES.convert(difftime, TimeUnit.SECONDS) < maxInterval);
        }
    }

    protected void forwardToView(HttpServletRequest request, HttpServletResponse response, String patternToJSP, Object bean) throws ServletException, IOException {
        if(!validateActivity(request.getSession(), MAXINTERVAL)) {
            System.out.print("INVALIDATE SESSION");
            response.sendRedirect("/logout");
            request.getSession().invalidate(); // invalidera sessionen.
            return;
        }
        request.setAttribute("bean", bean);
        forwardToView(request, response, patternToJSP);
    }

    protected void forwardToView(HttpServletRequest request, HttpServletResponse response, String patternToJSP) throws ServletException, IOException {
        ServletContext sc = getServletContext();
        RequestDispatcher rd = sc.getRequestDispatcher(patternToJSP);
        rd.forward(request, response);


    }

}
