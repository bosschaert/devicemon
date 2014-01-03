package org.coderthoughts.pimon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.osgi.framework.BundleContext;

class Hosts {
    private final File hostsFile;
    private final Properties hosts;

    Hosts(BundleContext bundleContext) throws IOException {
        hostsFile = bundleContext.getDataFile("hosts.properties");

        if (!hostsFile.exists()) {
            Properties p = new Properties();
            try(FileOutputStream fos = new FileOutputStream(hostsFile)) {
                p.store(fos, "");
            }
            hosts = p;
        } else {
            Properties p = new Properties();
            try (InputStream fis = new FileInputStream(hostsFile)) {
                p.load(fis);
            }
            hosts = p;
        }
    }

    synchronized boolean addHost(String host, String port, String user, String pwd) throws IOException {
        if (user.contains(" "))
            throw new IllegalArgumentException("User cannot contain a space");

        Object oldVal = hosts.put(host + ":" + port, user + " " + pwd);

        try (OutputStream os = new FileOutputStream(hostsFile)) {
            hosts.store(os, "");
        }
        return oldVal != null;
    }

    Collection<Host> getHosts() {
        List<Host> result = new ArrayList<>();

        for (String key : hosts.stringPropertyNames()) {
            int idx = key.indexOf(':');
            if (idx < 0)
                throw new IllegalStateException("Host is missing port number: " + key);

            result.add(new Host(key.substring(0, idx), Integer.parseInt(key.substring(idx+1))));
        }

        return result;
    }

    String getHostUser(Host host) {
        String info = hosts.getProperty(host.getHostName() + ":" + host.getPort());
        if (info == null)
            return null;

        int idx = info.indexOf(' ');
        if (idx > 0)
            return info.substring(0, idx);
        else
            return null;
    }

    String getHostPassword(Host host) {
        String info = hosts.getProperty(host.getHostName() + ":" + host.getPort());
        if (info == null)
            return null;

        int idx = info.indexOf(' ');
        if (idx > 0)
            return info.substring(idx + 1);
        else
            return null;
    }
}
