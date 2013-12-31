package org.coderthoughts.pimon;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class PimonServlet extends HttpServlet {
    private final Activator activator;

    PimonServlet(Activator a) {
        activator = a;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();
        writer.print("<H1>Hello!!</H1>");
        writer.print("<form method='post'>Add a pi host:<input type='text' name='hostname'/>"
                + " user:<input type='text' name='user'/>"
                + " pwd:<input type='password' name='pwd'/>"
                + "<input name='Add' type='submit'/></form>");


        writer.write("<UL>");
        for (String hostName : activator.getHosts()) {
            StringBuffer monURL = req.getRequestURL();
            int idx = monURL.lastIndexOf("/");
            if (idx > 0)
                monURL.setLength(idx);
            monURL.append("/mon?host=");
            monURL.append(hostName);

            writer.write("<iframe src='" + monURL + "'></iframe>");
        }
        writer.write("</UL>");
        writer.flush();
        writer.close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String hostName = req.getParameter("hostname");
        String userName = req.getParameter("user");
        String passWord = req.getParameter("pwd");
        if (hostName == null || userName == null || passWord == null)
            throw new IllegalArgumentException("Not all required information provided");

        if (userName.contains(" "))
            throw new IllegalArgumentException("User name cannot contain a space.");

        String action;
        if (activator.addHost(hostName, userName, passWord)) {
            action = "Modified";
        } else {
            action = "Added";
        }

        PrintWriter writer = resp.getWriter();
        writer.print("<H1>" + action + " host: </H1>");
        writer.print("<UL><LI>Host name: ");
        writer.print(hostName);
        writer.print("<LI>User: ");
        writer.print(userName);
        writer.print("<LI>Password: ");
        writer.print(passWord);
        writer.print("<p/><a href='pimon'>Back to Main Page</a>");
        writer.flush();
        writer.close();
    }
}
