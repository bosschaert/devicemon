package org.coderthoughts.pimon;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.sshd.ClientChannel;
import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.apache.sshd.client.future.ConnectFuture;

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
        writer.print("<pre>");
        String command = "uptime";
        writer.print("Command: ");
        writer.print(command);
        writer.print("<p/>");
        writer.print(getInfoViaSSH(host, activator.getHostUser(host), activator.getHostPassword(host), command));
        writer.print("</pre>");
        writer.flush();
        writer.close();
    }

    private String getInfoViaSSH(String host, String user, String pwd, String command) throws IOException {
        ConnectionData cd = new ConnectionData();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStream pipe = openSshChannel(host, user, pwd, out, cd);
        pipe.write((command + "\n").getBytes());
        pipe.flush();
        closeSshChannel(pipe, cd);
        String contents = new String(out.toByteArray());
        int idx1 = contents.lastIndexOf(command) + command.length();
        int idx2 = contents.lastIndexOf("logout");
        String pass1 = contents.substring(idx1, idx2);
        int idx3 = pass1.lastIndexOf("\n");
        return pass1.substring(0, idx3);
    }

    private OutputStream openSshChannel(String host, String user, String pwd, OutputStream out, ConnectionData cd) throws IOException {
        cd.client = SshClient.setUpDefaultClient();
        cd.client.start();
        ConnectFuture future;
        try {
            future = cd.client.connect(host, 22).await();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        cd.session = future.getSession();

        int ret = ClientSession.WAIT_AUTH;
        while ((ret & ClientSession.WAIT_AUTH) != 0) {
            cd.session.authPassword(user, pwd);
            ret = cd.session.waitFor(ClientSession.WAIT_AUTH | ClientSession.CLOSED | ClientSession.AUTHED, 0);
        }
        if ((ret & ClientSession.CLOSED) != 0) {
            throw new IOException("Could not open SSH channel.");
        }
        cd.channel = cd.session.createChannel("shell");
        PipedOutputStream pipe = new PipedOutputStream();
        cd.channel.setIn(new PipedInputStream(pipe));
        cd.channel.setOut(out);
        cd.channel.setErr(out);
        cd.channel.open();

        return pipe;
    }

    private void closeSshChannel(OutputStream pipe, ConnectionData cd) throws IOException {
        pipe.write("logout\n".getBytes());
        pipe.flush();
        cd.channel.waitFor(ClientChannel.CLOSED, 0);
        cd.session.close(true);
        cd.client.stop();
    }

    static class ConnectionData {
        ClientChannel channel;
        SshClient client;
        ClientSession session;
    }
}
