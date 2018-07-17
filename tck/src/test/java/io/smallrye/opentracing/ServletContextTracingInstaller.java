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
package io.smallrye.opentracing;

import io.opentracing.Tracer;
import io.opentracing.contrib.jaxrs2.server.SpanFinishingFilter;
import java.util.EnumSet;
import javax.enterprise.inject.spi.CDI;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * @author Pavol Loffay
 */
@WebListener
public class ServletContextTracingInstaller implements ServletContextListener {

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    ServletContext servletContext = servletContextEvent.getServletContext();
    servletContext.setInitParameter("resteasy.providers", SmallRyeTracingDynamicFeature.class.getName());

    // Span finishing filter
    Tracer tracer = CDI.current().select(Tracer.class).get();
    Dynamic filterRegistration = servletContext
        .addFilter("tracingFilter", new SpanFinishingFilter(tracer));
    filterRegistration.setAsyncSupported(true);
    filterRegistration.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class),
  true, "*");
  }

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
  }
}
