package org.coderthoughts.pimon;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.Servlet;

import org.osgi.framework.BundleContext;
import org.osgi.service.cdi.Component;
import org.osgi.service.cdi.Service;
import org.osgi.service.http.HttpService;

@Component
public class DeviceMonitor {
    private static Logger LOGGER = Logger.getLogger(DeviceMonitor.class.getName());

    @Inject
    BundleContext bundleContext;

    @Inject @Service
    HttpService httpService;

    @PostConstruct
    public void postConstruct() throws Exception {
        System.out.println("%%%%: " + httpService + "#" + bundleContext);

        Hosts hosts = new Hosts(bundleContext);
        registerServlet("/dmon", new DeviceMonServlet(hosts));
        registerServlet("/device", new DeviceServlet(hosts));
    }

    private void registerServlet(String ctx, Servlet servlet) {
        try {
            httpService.registerServlet(ctx, servlet, null, null);
            LOGGER.info("Registered servlet under " + ctx);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Cannot register Pimon Servlet", e);
        }
    }
}
