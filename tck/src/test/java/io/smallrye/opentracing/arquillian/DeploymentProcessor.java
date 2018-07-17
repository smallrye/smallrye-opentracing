/**
 * Copyright 2018 The SmallRye Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.smallrye.opentracing.arquillian;

import io.smallrye.opentracing.ExceptionMapper;
import io.smallrye.opentracing.OpenTracingCDIExtension;
import io.smallrye.opentracing.ResteasyClientTracingRegistrarProvider;
import io.smallrye.opentracing.TracerProducer;
import io.smallrye.opentracing.ServletContextTracingInstaller;
import java.io.File;
import javax.enterprise.inject.spi.Extension;
import javax.servlet.ServletContainerInitializer;
import javax.ws.rs.ext.Providers;
import org.eclipse.microprofile.opentracing.ClientTracingRegistrarProvider;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.weld.environment.deployment.discovery.BeanArchiveHandler;

/**
 * @author Pavol Loffay
 */
public class DeploymentProcessor implements ApplicationArchiveProcessor {

  @Override
  public void process(Archive<?> archive, TestClass testClass) {
    if (archive instanceof WebArchive) {
      JavaArchive extensionsJar = ShrinkWrap.create(JavaArchive.class,"extension.jar");
      extensionsJar.addAsServiceProvider(Providers.class, ExceptionMapper.class);
      extensionsJar.addClass(ExceptionMapper.class);
      extensionsJar.addAsServiceProvider(ClientTracingRegistrarProvider.class, ResteasyClientTracingRegistrarProvider.class);
      extensionsJar.addClasses(ResteasyClientTracingRegistrarProvider.class);
      extensionsJar.addPackages(true, "io.opentracing");

      // install CDI extensions
      extensionsJar.addAsServiceProvider(Extension.class, OpenTracingCDIExtension.class);
      extensionsJar.addClasses(TracerProducer.class);
      extensionsJar.addClasses(OpenTracingCDIExtension.class);

      WebArchive war = WebArchive.class.cast(archive);
      war.addAsLibraries(extensionsJar);

      // install tracing filters
      war.addClass(ServletContextTracingInstaller.class);

      // Workaround for RESTEASY-1922
      war.addClass(FixedResteasyServletInitializer.class);
      war.addAsServiceProvider(ServletContainerInitializer.class, FixedResteasyServletInitializer.class);
      war.addClass(SmallRyeBeanArchiveHandler.class);
      war.addAsServiceProvider(BeanArchiveHandler.class, SmallRyeBeanArchiveHandler.class);

      String[] deps = {
          "io.smallrye:smallrye-opentracing",
          "org.jboss.resteasy:resteasy-cdi",
          "org.jboss.resteasy:resteasy-jaxb-provider",
          "org.eclipse.microprofile.opentracing:microprofile-opentracing-tck",
          "org.jboss.weld.servlet:weld-servlet-core"
      };
      File[] dependencies = Maven.resolver()
          .loadPomFromFile(new File("pom.xml"))
          .resolve(deps)
          .withTransitivity()
          .asFile();
      war.addAsLibraries(dependencies);
      System.out.println(war.toString(true));
    }
  }
}
