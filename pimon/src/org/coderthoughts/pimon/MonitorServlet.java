package org.coderthoughts.pimon;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class MonitorServlet extends HttpServlet {
    private final Activator activator;

    public MonitorServlet(Activator a) {
        activator = a;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String host = req.getParameter("host");
        if (host == null)
            throw new IllegalArgumentException("No host specified");

        PrintWriter writer = resp.getWriter();
        writer.print("<H1>");
        writer.print(host);
        writer.print("</H1>");
        writer.print("<UL><LI>User: " + activator.getHostUser(host));
        writer.print("<LI>Pass: " + activator.getHostPassword(host));
        writer.flush();
        writer.close();
    }
}
