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

import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.HandlesTypes;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.core.AsynchronousDispatcher;
import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyContextParameters;

/**
 * This is a fixed replacement for <a href=
 * "https://github.com/resteasy/Resteasy/blob/master/resteasy-servlet-initializer/src/main/java/org/jboss/resteasy/plugins/servlet/ResteasyServletInitializer.java">ResteasyServletInitializer.java</a>.
 * See also <a href="https://issues.jboss.org/browse/RESTEASY-1922">RESTEASY-1922</a>.
 */
@HandlesTypes({ Application.class, Path.class, Provider.class })
public class FixedResteasyServletInitializer implements ServletContainerInitializer {

  final static Set<String> IGNORED_PACKAGES = new HashSet<String>();

  static {
    IGNORED_PACKAGES.add(AsynchronousDispatcher.class.getPackage().getName());
  }

  @Override
  public void onStartup(Set<Class<?>> classes, ServletContext servletContext) throws ServletException {
    if (classes == null || classes.size() == 0)
      return;
    for (ServletRegistration reg : servletContext.getServletRegistrations().values()) {
      if (reg.getInitParameter("javax.ws.rs.Application") != null) {
        return; // there's already a servlet mapping, do nothing
      }
    }

    Set<Class<?>> appClasses = new HashSet<Class<?>>();
    Set<Class<?>> providers = new HashSet<Class<?>>();
    Set<Class<?>> resources = new HashSet<Class<?>>();

    for (Class<?> clazz : classes) {
      // FIXED: Ignore client proxy interfaces
      if (clazz.isInterface() || IGNORED_PACKAGES.contains(clazz.getPackage().getName()))
        continue;
      if (clazz.isAnnotationPresent(Path.class)) {
        resources.add(clazz);
      } else if (clazz.isAnnotationPresent(Provider.class)) {
        providers.add(clazz);
      } else {
        appClasses.add(clazz);
      }
    }
    if (appClasses.size() == 0 && resources.size() == 0)
      return;

    if (appClasses.size() == 0) {
      // todo make sure we can do this on all servlet containers
//       handleNoApplicationClass(providers, resources, servletContext);
      return;
    }

    for (Class<?> app : appClasses) {
      register(app, providers, resources, servletContext);
    }
  }

  protected void handleNoApplicationClass(Set<Class<?>> providers, Set<Class<?>> resources, ServletContext servletContext) {
    ServletRegistration defaultApp = null;
    for (ServletRegistration reg : servletContext.getServletRegistrations().values()) {
      if (reg.getName().equals(Application.class.getName())) {
        defaultApp = reg;
      }
    }
    if (defaultApp == null)
      return;
    throw new IllegalStateException("Default application not implemented yet");

  }

  protected void register(Class<?> applicationClass, Set<Class<?>> providers, Set<Class<?>> resources, ServletContext servletContext) {
    ApplicationPath path = applicationClass.getAnnotation(ApplicationPath.class);
    if (path == null) {
      // todo we don't support this yet, i'm not sure if partial deployments are supported in all servlet containers
      return;
    }
    ServletRegistration.Dynamic reg = servletContext.addServlet(applicationClass.getName(), HttpServlet30Dispatcher.class);
    reg.setLoadOnStartup(1);
    reg.setAsyncSupported(true);
    reg.setInitParameter("javax.ws.rs.Application", applicationClass.getName());

    if (path != null) {
      String mapping = path.value();
      if (!mapping.startsWith("/"))
        mapping = "/" + mapping;
      String prefix = mapping;
      if (!prefix.equals("/") && prefix.endsWith("/"))
        prefix = prefix.substring(0, prefix.length() - 1);
      if (!mapping.endsWith("/*")) {
        if (mapping.endsWith("/"))
          mapping += "*";
        else
          mapping += "/*";
      }
      // resteasy.servlet.mapping.prefix
      reg.setInitParameter("resteasy.servlet.mapping.prefix", prefix);
      reg.addMapping(mapping);
    }

    if (resources.size() > 0) {
      StringBuilder builder = new StringBuilder();
      boolean first = true;
      for (Class resource : resources) {
        if (first) {
          first = false;
        } else {
          builder.append(",");
        }

        builder.append(resource.getName());
      }
      reg.setInitParameter(ResteasyContextParameters.RESTEASY_SCANNED_RESOURCES, builder.toString());
    }
    if (providers.size() > 0) {
      StringBuilder builder = new StringBuilder();
      boolean first = true;
      for (Class provider : providers) {
        if (first) {
          first = false;
        } else {
          builder.append(",");
        }
        builder.append(provider.getName());
      }
      reg.setInitParameter(ResteasyContextParameters.RESTEASY_SCANNED_PROVIDERS, builder.toString());
    }

  }
}
