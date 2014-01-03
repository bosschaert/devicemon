package org.coderthoughts.devicemon;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class MonitorServlet extends HttpServlet {
    private final Hosts hosts;

    public MonitorServlet(Hosts hosts) {
        this.hosts = hosts;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();
        writer.print("<H1>Device Monitor</H1>");
        writer.print("<form method='post'>Add a device:<input type='text' name='hostname' size='15' />"
                + " port:<input type='text' name='port' value='22' size='5' />"
                + " user:<input type='text' name='user' size='10' />"
                + " pwd:<input type='password' name='pwd' size='10' />"
                + "<input name='Add' value='Add' type='submit'/></form>");


        writer.write("<UL>");
        for (Host host : hosts.getHosts()) {
            StringBuffer monURL = req.getRequestURL();
            int idx = monURL.lastIndexOf("/");
            if (idx > 0)
                monURL.setLength(idx);
            monURL.append("/device?host=");
            monURL.append(host.getHostName());
            monURL.append("&port=");
            monURL.append(host.getPort());

            writer.write("<iframe width='800' src='" + monURL + "'></iframe><p/>");
        }
        writer.write("</UL>");
        writer.flush();
        writer.close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String hostName = req.getParameter("hostname");
        String port = req.getParameter("port");
        String userName = req.getParameter("user");
        String passWord = req.getParameter("pwd");
        if (hostName == null || userName == null || passWord == null)
            throw new IllegalArgumentException("Not all required information provided");

        if (userName.contains(" "))
            throw new IllegalArgumentException("User name cannot contain a space.");

        String action;
        if (hosts.addHost(hostName, port, userName, passWord)) {
            action = "Modified";
        } else {
            action = "Added";
        }

        PrintWriter writer = resp.getWriter();
        writer.print("<H1>" + action + " host: </H1>");
        writer.print("<UL><LI>Host name: ");
        writer.print(hostName);
        writer.print("<LI>Port: ");
        writer.print(port);
        writer.print("<LI>User: ");
        writer.print(userName);
        writer.print("<LI>Password: ");
        writer.print(passWord.replaceAll(".", "*"));
        writer.print("<p/><a href=''>Back to Main Page</a>");
        writer.flush();
        writer.close();
    }
}
