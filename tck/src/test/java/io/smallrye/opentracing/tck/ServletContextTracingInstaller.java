package io.smallrye.opentracing.tck;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import io.opentracing.contrib.jaxrs2.server.SpanFinishingFilter;
import io.smallrye.opentracing.SmallRyeTracingDynamicFeature;

/**
 * @author Pavol Loffay
 */
@WebListener
public class ServletContextTracingInstaller implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();
        servletContext.setInitParameter("resteasy.providers",
                SmallRyeTracingDynamicFeature.class.getName() + "," + ExceptionMapper.class.getName());

        // Span finishing filter
        Dynamic filterRegistration = servletContext.addFilter("tracingFilter", new SpanFinishingFilter());
        filterRegistration.setAsyncSupported(true);
        filterRegistration.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "*");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }
}
