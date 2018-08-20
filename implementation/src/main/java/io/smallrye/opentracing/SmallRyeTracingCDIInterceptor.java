package io.smallrye.opentracing;

import io.opentracing.Scope;
import io.opentracing.Tracer;
import org.eclipse.microprofile.opentracing.Traced;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.ws.rs.Path;
import java.lang.reflect.Method;

/**
 * @author Pavol Loffay
 */
@Traced
@Interceptor
@Priority(value = Interceptor.Priority.LIBRARY_BEFORE + 1)
@Dependent
public class SmallRyeTracingCDIInterceptor {

  @Inject
  private Tracer tracer;

  @AroundInvoke
  public Object interceptTraced(InvocationContext ctx) throws Exception {
    Scope activeScope = null;
    try {
      if (!isJaxRs(ctx.getMethod()) && isTraced(ctx.getMethod())) {
        activeScope = tracer.buildSpan(getOperationName(ctx.getMethod()))
            .startActive(true);
      }
      return ctx.proceed();
    } finally {
      if (activeScope != null) {
        activeScope.close();
      }
    }
  }

  /**
   * Determines whether invoked method is jax-rs endpoint
   * @param method invoked method
   * @return true if invoked method is jax-rs endpoint
   */
  protected boolean isJaxRs(Method method) {
    if (method.getAnnotation(Path.class) != null ||
        method.getDeclaringClass().getAnnotation(Path.class) != null) {
      return true;
    }
    return false;
  }

  /**
   * Determines whether invoked method should be traced or not
   * @param method invoked method
   * @return true if {@link Traced} defined on method or class has value true
   */
  protected boolean isTraced(Method method) {
    Traced classTraced = method.getDeclaringClass().getAnnotation(Traced.class);
    Traced methodTraced = method.getAnnotation(Traced.class);
    if (methodTraced != null) {
      return methodTraced.value();
    }
    return classTraced != null ? classTraced.value() : false;
  }

  /**
   * Returns operation name for given method
   *
   * @param method invoked method
   * @return operation name
   */
  protected String getOperationName(Method method) {
    Traced classTraced = method.getDeclaringClass().getAnnotation(Traced.class);
    Traced methodTraced = method.getAnnotation(Traced.class);
    if (methodTraced != null && methodTraced.operationName().length() > 0) {
      return methodTraced.operationName();
    } else if (classTraced != null && classTraced.operationName().length() > 0) {
      return classTraced.operationName();
    }
    return String.format("%s.%s", method.getDeclaringClass().getName(), method.getName());
  }
}
