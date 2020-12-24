package com.glenfordham.webserver;

import com.glenfordham.webserver.config.Arguments;
import com.glenfordham.webserver.config.ConfigProperties;
import com.glenfordham.webserver.logging.Log;
import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.JarResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import java.io.File;
import java.net.BindException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TomcatServer {

    private static boolean started = false;

    /**
     * Starts the Tomcat server. Only one running Tomcat instance is supported
     */
    static synchronized void start(ConfigProperties configProperties) {
        try {
            if (!started) {
                File root = getRootFolder();
                Path tempPath = Files.createTempDirectory(configProperties.getPropertyValue(Arguments.TEMP_DIR_PREFIX));
                System.setProperty("org.apache.catalina.startup.EXIT_ON_INIT_FAILURE", "true");

                Tomcat tomcat = new Tomcat();
                tomcat.setBaseDir(tempPath.toString());
                tomcat.setPort(configProperties.getPropertyValueAsInt(Arguments.PORT));
                tomcat.getConnector();
                // Prevent multiple factory exception
                TomcatURLStreamHandlerFactory.disable();

                // Initialise Lucee
                final String webAppRootPath = new File(configProperties.getPropertyValue(Arguments.WEB_APP_ROOT)).getAbsolutePath();
                Context ctx = tomcat.addContext("", webAppRootPath + "/webroot");
                Class<?> cfmlServletClass = lucee.loader.servlet.CFMLServlet.class;
                final String cfmlServletName = cfmlServletClass.getSimpleName();
                Tomcat.addServlet(ctx, cfmlServletName, cfmlServletClass.getName());
                ctx.addServletMappingDecoded("*.cfm", cfmlServletName);
                ctx.addServletMappingDecoded("*.cfc", cfmlServletName);
                ctx.addServletMappingDecoded("*.cfml", cfmlServletName);
                ctx.addServletMappingDecoded("/index.cfc/*", cfmlServletName);
                ctx.addServletMappingDecoded("/index.cfm/*", cfmlServletName);
                ctx.addServletMappingDecoded("/index/cfml/*", cfmlServletName);

                // Set WEB-INF and lucee-server directory locations
                ctx.getServletContext().getServletRegistrations().get(cfmlServletName).setInitParameter("lucee-web-directory", webAppRootPath + "/WEB-INF");
                ctx.getServletContext().getServletRegistrations().get(cfmlServletName).setInitParameter("lucee-server-root", webAppRootPath + "/lucee-server");

                // Prevent access to Lucee admin outside of localhost
                Class<?> filterClass = org.apache.catalina.filters.RemoteAddrFilter.class;
                final String filterName = filterClass.getSimpleName();
                FilterDef filterDef = new FilterDef();
                filterDef.setFilterName(filterName);
                filterDef.setFilterClass(filterClass.getName());
                filterDef.addInitParameter("allow", "127\\.\\d+\\.\\d+\\.\\d+|::1|0:0:0:0:0:0:0:1");
                ctx.addFilterDef(filterDef);
                FilterMap filterMap = new FilterMap();
                filterMap.setFilterName(filterName);
                filterMap.addURLPattern("/lucee/admin/*");
                ctx.addFilterMap(filterMap);

                // Initialise Lucee REST servlet
                Class<?> restServletClass = lucee.loader.servlet.RestServlet.class;
                Tomcat.addServlet(ctx, restServletClass.getSimpleName(), restServletClass.getName());
                ctx.addServletMappingDecoded("/rest/*", restServletClass.getSimpleName());

                // Check if running within a jar and use appropriate resource set object
                String runningUriPath = Application.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
                WebResourceRoot resources = new StandardRoot(ctx);
                resources.addPreResources(runningUriPath.toUpperCase().endsWith(".JAR")
                        ? new JarResourceSet(resources, "/WEB-INF/classes", new File(runningUriPath).getAbsolutePath(), "/")
                        : new DirResourceSet(resources, "/WEB-INF/classes", new File(runningUriPath).getAbsolutePath(), "/"));
                ctx.setResources(resources);

                Log.infoFormat("Application root: %s", root.getAbsolutePath());
                Log.infoFormat("Listening port: %s", configProperties.getPropertyValue(Arguments.PORT));
                tomcat.start();
                started = true;
                tomcat.getServer().await();
            } else {
                Log.error("Unable to start Tomcat. Tomcat is already started.");
            }
        } catch (Exception e) {
            // Annoyingly, BindExceptions are nested inside LifeCycle exceptions
            if (e.getCause() instanceof BindException) {
                Log.error("Unable to start. A process is already bound to port.");
            } else {
                Log.error("Unexpected error occurred when starting Tomcat.", e);
            }
        }
    }

    /**
     * Gets the root folder of the Tomcat directory
     *
     * @return a File containing the absolute root path
     * @throws URISyntaxException if unable to convert location to URI
     */
    private static File getRootFolder() throws URISyntaxException {
        File root;
        String runningJarPath = Application.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().replaceAll("\\\\", "/");
        int lastIndexOf = runningJarPath.lastIndexOf("/target/");
        if (lastIndexOf < 0) {
            root = new File("");
        } else {
            root = new File(runningJarPath.substring(0, lastIndexOf));
        }
        return root;
    }

    // Ensure only one TomcatServer is created using static start() method
    private TomcatServer() {
    }
}
