package io.smallrye.opentracing.tck;

import java.util.EnumSet;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterRegistration.Dynamic;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import io.smallrye.opentracing.SmallRyeTracingDynamicFeature;
import io.smallrye.opentracing.contrib.jaxrs2.server.SpanFinishingFilter;

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
