package io.smallrye.opentracing.tck;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestApplication extends Arquillian {
    /**
     * The base URL for the container under test
     */
    @ArquillianResource
    private URL baseURL;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap
                .create(WebArchive.class)
                .addClass(TestServlet.class)
                .addClass(RestApplication.class)
                .addClass(TestEndpoint.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));
    }

    @Test
    @RunAsClient
    public void servlet() {
        String uri = baseURL.toExternalForm() + "servlet";
        System.out.println("uri = " + uri);
        WebTarget echoEndpointTarget = ClientBuilder.newClient().target(uri);
        Response response = echoEndpointTarget.request(TEXT_PLAIN).get();
        Assert.assertEquals(response.getStatus(), HttpURLConnection.HTTP_OK);
    }

    @Test
    @RunAsClient
    public void rest() {
        String uri = baseURL.toExternalForm() + "rest";
        System.out.println("uri = " + uri);
        WebTarget echoEndpointTarget = ClientBuilder.newClient().target(uri);
        Response response = echoEndpointTarget.request(TEXT_PLAIN).get();
        Assert.assertEquals(response.getStatus(), HttpURLConnection.HTTP_OK);
    }

    @WebServlet(urlPatterns = "/servlet")
    public static class TestServlet extends HttpServlet {
        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
            resp.getWriter().write(CDI.current().select(HelloBean.class).get().hello());
        }
    }

    @ApplicationPath("/rest")
    public static class RestApplication extends javax.ws.rs.core.Application {

    }

    @RequestScoped
    @Path("/")
    public static class TestEndpoint {
        @Inject
        HelloBean helloBean;

        @GET
        public String hello() {
            return helloBean.hello();
        }
    }

    @ApplicationScoped
    public static class HelloBean {
        public String hello() {
            return "hello";
        }
    }
}
