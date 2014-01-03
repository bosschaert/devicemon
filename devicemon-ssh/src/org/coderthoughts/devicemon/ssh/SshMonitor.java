package org.coderthoughts.devicemon.ssh;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.apache.sshd.ClientChannel;
import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.apache.sshd.client.future.ConnectFuture;

public class SshMonitor {
    public static String getInfoViaSSH(String host, int port, String user, String pwd, String command) throws IOException {
        ConnectionData cd = new ConnectionData();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStream pipe = openSshChannel(host, port, user, pwd, out, cd);
        pipe.write((command + "\n").getBytes());
        pipe.flush();
        closeSshChannel(pipe, cd);
        String contents = new String(out.toByteArray());
        int idx1 = contents.lastIndexOf(command) + command.length();
        int idx2 = contents.lastIndexOf("exit");
        String pass1 = contents.substring(idx1, idx2);
        int idx3 = pass1.lastIndexOf("\n");
        return pass1.substring(0, idx3);
    }

    private static OutputStream openSshChannel(String host, int port, String user, String pwd, OutputStream out, ConnectionData cd) throws IOException {
        cd.client = SshClient.setUpDefaultClient();
        cd.client.start();
        ConnectFuture future;
        try {
            future = cd.client.connect(host, port).await();
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

    private static void closeSshChannel(OutputStream pipe, ConnectionData cd) throws IOException {
        pipe.write("exit\n".getBytes());
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
