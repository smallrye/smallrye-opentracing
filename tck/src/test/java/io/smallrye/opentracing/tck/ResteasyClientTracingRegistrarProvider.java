package io.smallrye.opentracing.tck;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.ws.rs.client.ClientBuilder;

import org.eclipse.microprofile.opentracing.ClientTracingRegistrarProvider;

import io.opentracing.Tracer;
import io.opentracing.contrib.concurrent.TracedExecutorService;
import io.smallrye.opentracing.SmallRyeClientTracingFeature;

/**
 * @author Pavol Loffay
 */
public class ResteasyClientTracingRegistrarProvider implements ClientTracingRegistrarProvider {

    public ClientBuilder configure(ClientBuilder clientBuilder) {
        return configure(clientBuilder, Executors.newFixedThreadPool(10));
    }

    public ClientBuilder configure(ClientBuilder clientBuilder, ExecutorService executorService) {
        Tracer tracer = CDI.current().select(Tracer.class).get();
        return clientBuilder.executorService(new TracedExecutorService(executorService, tracer))
                .register(new SmallRyeClientTracingFeature(tracer));
    }
}
