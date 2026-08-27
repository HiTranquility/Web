import java.io.File;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

/**
 * Starts an embedded Tomcat 9 so the web doc truyen can be run with a
 * single command, without installing a server first. It serves the files in
 * src/main/webapp directly, so edits to index.html, thanks.jsp and main.css
 * show up on refresh.
 */
public class DevServer {

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getProperty("port", "8080"));
        String contextPath = System.getProperty("contextPath", "/webdoctruyen");

        File webapp = new File("src/main/webapp").getAbsoluteFile();
        File classes = new File("build/classes").getAbsoluteFile();
        // src/main/resources (db.properties) duoc copy vao build/classes boi run.ps1
        File work = new File("build/tomcat").getAbsoluteFile();
        work.mkdirs();

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.setBaseDir(work.getAbsolutePath());
        tomcat.getConnector();

        Context context = tomcat.addWebapp(contextPath, webapp.getAbsolutePath());

        // Put the compiled servlet/business/data classes on the webapp classpath.
        WebResourceRoot resources = new StandardRoot(context);
        resources.addPreResources(new DirResourceSet(
                resources, "/WEB-INF/classes", classes.getAbsolutePath(), "/"));
        context.setResources(resources);

        tomcat.start();
        System.out.println();
        System.out.println("  Web Doc Truyen running at http://localhost:"
                + port + contextPath + "/");
        System.out.println("  Nguon: " + webapp.getPath());
        System.out.println("  Press Ctrl+C to stop.");
        System.out.println();
        tomcat.getServer().await();
    }
}
