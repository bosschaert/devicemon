package org.coderthoughts.devicemon.ds;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Servlet;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;

@Component
public class DeviceMonitor {
    private static Logger LOGGER = Logger.getLogger(DeviceMonitor.class.getName());

    private HttpService httpService;

    @Reference
    public void setHttpService(HttpService svc) {
        httpService = svc;
    }

    @Activate
    public void activate(BundleContext bc) throws Exception {
        System.out.println("*** DeviceMonitor activated: " + httpService);
        System.out.println("    bc: " + bc);

        Hosts hosts = new Hosts(bc);

        registerServlet("/dmon", new MonitorServlet(hosts));
        // registerServlet("/device", new DeviceServlet(hosts));
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
