package io.smallrye.opentracing.tck;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.Provider;

/**
 * Temporal fix to catch exceptions thrown in JAX-RS endpoints
 * See https://issues.jboss.org/browse/RESTEASY-1758
 *
 * @author Pavol Loffay
 */
@Provider
public class ExceptionMapper implements jakarta.ws.rs.ext.ExceptionMapper<RuntimeException> {

    @Override
    public Response toResponse(RuntimeException exception) {
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.getMessage()).build();
    }
}
