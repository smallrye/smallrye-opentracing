package io.smallrye.opentracing.tck;

import java.io.File;

import org.eclipse.microprofile.opentracing.ClientTracingRegistrarProvider;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

/**
 * @author Pavol Loffay
 */
public class DeploymentProcessor implements ApplicationArchiveProcessor {
    @Override
    public void process(Archive<?> archive, TestClass testClass) {
        if (archive instanceof WebArchive) {
            WebArchive war = (WebArchive) archive;
            war.addClass(ServletContextTracingInstaller.class);
            war.addClass(TracerProducer.class);
            war.addAsServiceProvider(ClientTracingRegistrarProvider.class, ResteasyClientTracingRegistrarProvider.class);

            String[] deps = {
                    "org.jboss.resteasy:resteasy-servlet-initializer",
                    "org.jboss.resteasy:resteasy-cdi",
                    "org.jboss.resteasy:resteasy-json-binding-provider",
                    "io.smallrye:smallrye-opentracing"
            };
            File[] dependencies = Maven.configureResolver()
                    .workOffline()
                    .loadPomFromFile(new File("pom.xml"))
                    .resolve(deps)
                    .withoutTransitivity()
                    .asFile();
            war.addAsLibraries(dependencies);
            System.out.println(war.toString(true));
        }
    }
}
