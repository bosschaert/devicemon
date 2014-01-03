package org.coderthoughts.devicemon;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.coderthoughts.devicemon.ssh.SshMonitor;

@SuppressWarnings("serial")
class DeviceServlet extends HttpServlet {
    private final Hosts hosts;

    public DeviceServlet(Hosts hosts) {
        this.hosts = hosts;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String host = req.getParameter("host");
        if (host == null)
            throw new IllegalArgumentException("No host specified");
        String portString = req.getParameter("port");
        int port;
        if (portString == null)
            port = 22;
        else
            port = Integer.parseInt(portString);

        PrintWriter writer = resp.getWriter();
        writer.print("<H1>");
        writer.print(host);
        writer.print("</H1>");
        writer.print("<pre>");
        String command = "uptime";
        writer.print("Command: ");
        writer.print(command);
        writer.print("<p/>");
        try {
            writer.write("<span style='color:green'>");
            writer.print(SshMonitor.getInfoViaSSH(host, port, hosts.getHostUser(new Host(host, port)),
                    hosts.getHostPassword(new Host(host, port)), command));
            writer.write("</span>");
        } catch (Exception e) {
            writer.write("<span style='color:red'>Unable to contact host. Error message: ");
            writer.write(e.getMessage());
            writer.write("</span>");
        }
        writer.print("</pre>");
        writer.flush();
        writer.close();
    }
}
