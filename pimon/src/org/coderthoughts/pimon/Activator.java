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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Servlet;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {
    private static Logger LOGGER = Logger.getLogger(Activator.class.getName());

    private final List<String> contexts = new ArrayList<>();

    private ServiceTracker<HttpService, HttpService> httpServiceTracker;
    private BundleContext bundleContext;

    private File hostsFile;
    private Properties hosts;

    @Override
    public void start(BundleContext context) throws Exception {
        bundleContext = context;
        initHosts();

        httpServiceTracker = new ServiceTracker<HttpService, HttpService>(context, HttpService.class, null) {
            @Override
            public HttpService addingService(ServiceReference<HttpService> reference) {
                HttpService httpService = super.addingService(reference);
                registerServlet(httpService, "/pimon", new PimonServlet(Activator.this));
                registerServlet(httpService, "/mon", new MonitorServlet(Activator.this));
                return httpService;
            }
        };
        httpServiceTracker.open();
    }

    private synchronized void initHosts() throws IOException {
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

    @Override
    public void stop(BundleContext context) throws Exception {
        HttpService svc = httpServiceTracker.getService();
        if (svc != null) {
            for (String ctx : contexts) {
                svc.unregister(ctx);
            }
        }
    }

    private void registerServlet(HttpService httpService, String ctx, Servlet servlet) {
        try {
            httpService.registerServlet(ctx, servlet, null, null);
            LOGGER.info("Registered servlet under " + ctx);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Cannot register Pimon Servlet", e);
        }
    }

    public Collection<Host> getHosts() {
        List<Host> result = new ArrayList<>();

        for (String key : hosts.stringPropertyNames()) {
            int idx = key.indexOf(':');
            if (idx < 0)
                throw new IllegalStateException("Host is missing port number: " + key);

            result.add(new Host(key.substring(0, idx), Integer.parseInt(key.substring(idx+1))));
        }

        return result;
    }

    public String getHostUser(String host, int port) {
        String info = hosts.getProperty(host + ":" + port);
        if (info == null)
            return null;

        int idx = info.indexOf(' ');
        if (idx > 0)
            return info.substring(0, idx);
        else
            return null;
    }

    public String getHostPassword(String host, int port) {
        String info = hosts.getProperty(host + ":" + port);
        if (info == null)
            return null;

        int idx = info.indexOf(' ');
        if (idx > 0)
            return info.substring(idx + 1);
        else
            return null;
    }

}
