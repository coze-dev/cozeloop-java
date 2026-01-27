package com.coze.loop.spring.aop;

import com.coze.loop.client.CozeLoopClient;
import com.coze.loop.spring.annotation.CozeTrace;
import com.coze.loop.trace.CozeLoopSpan;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CozeTraceAspect.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CozeTraceAspectTest {

    @Mock
    private CozeLoopClient client;
    
    @Mock
    private CozeLoopSpan span;
    
    @Mock
    private ProceedingJoinPoint joinPoint;
    
    @Mock
    private MethodSignature methodSignature;
    
    private CozeTraceAspect aspect;

    @BeforeEach
    void setUp() throws Exception {
        aspect = new CozeTraceAspect(client);
        
        Method testMethod = TestService.class.getMethod("testMethod", String.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(testMethod);
        when(joinPoint.getArgs()).thenReturn(new Object[]{"test-arg"});
    }

    @Test
    void testTraceMethodWithDefaultAnnotation() throws Throwable {
        CozeTrace annotation = createAnnotation("", "custom", false, false, "", "");
        when(client.startSpan(anyString(), anyString())).thenReturn(span);
        when(joinPoint.proceed()).thenReturn("result");
        
        Object result = aspect.traceMethod(joinPoint, annotation);
        
        assertThat(result).isEqualTo("result");
        verify(client).startSpan("testMethod", "custom");
        verify(span).setStatusCode(0);
        verify(span).close();
    }

    @Test
    void testTraceMethodWithCustomSpanName() throws Throwable {
        CozeTrace annotation = createAnnotation("custom-span", "llm", false, false, "", "");
        when(client.startSpan(anyString(), anyString())).thenReturn(span);
        when(joinPoint.proceed()).thenReturn("result");
        
        Object result = aspect.traceMethod(joinPoint, annotation);
        
        assertThat(result).isEqualTo("result");
        verify(client).startSpan("custom-span", "llm");
        verify(span).setStatusCode(0);
    }

    @Test
    void testTraceMethodWithCaptureArgs() throws Throwable {
        CozeTrace annotation = createAnnotation("", "custom", true, false, "", "");
        when(client.startSpan(anyString(), anyString())).thenReturn(span);
        when(joinPoint.proceed()).thenReturn("result");
        
        aspect.traceMethod(joinPoint, annotation);
        
        verify(span).setInput(any());
        verify(span, never()).setOutput(any());
    }

    @Test
    void testTraceMethodWithCaptureReturn() throws Throwable {
        CozeTrace annotation = createAnnotation("", "custom", false, true, "", "");
        when(client.startSpan(anyString(), anyString())).thenReturn(span);
        when(joinPoint.proceed()).thenReturn("result");
        
        aspect.traceMethod(joinPoint, annotation);
        
        verify(span).setOutput("result");
        verify(span, never()).setInput(any());
    }

    @Test
    void testTraceMethodWithInputExpression() throws Throwable {
        CozeTrace annotation = createAnnotation("", "custom", false, false, "#args[0]", "");
        when(client.startSpan(anyString(), anyString())).thenReturn(span);
        when(joinPoint.proceed()).thenReturn("result");
        
        aspect.traceMethod(joinPoint, annotation);
        
        verify(span).setInput(any());
    }

    @Test
    void testTraceMethodWithOutputExpression() throws Throwable {
        CozeTrace annotation = createAnnotation("", "custom", false, false, "", "#result");
        when(client.startSpan(anyString(), anyString())).thenReturn(span);
        when(joinPoint.proceed()).thenReturn("result");
        
        aspect.traceMethod(joinPoint, annotation);
        
        verify(span).setOutput(any());
    }

    @Test
    void testTraceMethodWithException() throws Throwable {
        CozeTrace annotation = createAnnotation("", "custom", false, false, "", "");
        RuntimeException exception = new RuntimeException("test error");
        when(client.startSpan(anyString(), anyString())).thenReturn(span);
        when(joinPoint.proceed()).thenThrow(exception);
        
        assertThatThrownBy(() -> aspect.traceMethod(joinPoint, annotation))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("test error");
        
        verify(span).setError(exception);
        verify(span).setStatusCode(1);
        verify(span).close();
    }

    @Test
    void testTraceMethodWithSpelSpanName() throws Throwable {
        CozeTrace annotation = createAnnotation("#{'span_' + #args[0]}", "custom", false, false, "", "");
        when(client.startSpan(anyString(), anyString())).thenReturn(span);
        when(joinPoint.proceed()).thenReturn("result");
        
        aspect.traceMethod(joinPoint, annotation);
        
        verify(client).startSpan(anyString(), eq("custom"));
    }

    @Test
    void testTraceMethodWithMultipleArgs() throws Throwable {
        when(joinPoint.getArgs()).thenReturn(new Object[]{"arg1", "arg2", 123});
        CozeTrace annotation = createAnnotation("", "custom", true, false, "", "");
        when(client.startSpan(anyString(), anyString())).thenReturn(span);
        when(joinPoint.proceed()).thenReturn("result");
        
        aspect.traceMethod(joinPoint, annotation);
        
        verify(span).setInput(any(Object[].class));
    }

    @Test
    void testTraceMethodWithNullReturn() throws Throwable {
        CozeTrace annotation = createAnnotation("", "custom", false, true, "", "");
        when(client.startSpan(anyString(), anyString())).thenReturn(span);
        when(joinPoint.proceed()).thenReturn(null);
        
        aspect.traceMethod(joinPoint, annotation);
        
        verify(span, never()).setOutput(any());
        verify(span).setStatusCode(0);
    }

    private CozeTrace createAnnotation(String value, String spanType, 
                                      boolean captureArgs, boolean captureReturn,
                                      String inputExpression, String outputExpression) {
        return new CozeTrace() {
            @Override
            public Class<CozeTrace> annotationType() {
                return CozeTrace.class;
            }

            @Override
            public String value() {
                return value;
            }

            @Override
            public String spanType() {
                return spanType;
            }

            @Override
            public boolean captureArgs() {
                return captureArgs;
            }

            @Override
            public boolean captureReturn() {
                return captureReturn;
            }

            @Override
            public String inputExpression() {
                return inputExpression;
            }

            @Override
            public String outputExpression() {
                return outputExpression;
            }
        };
    }

    // Test service class for method reflection
    static class TestService {
        public String testMethod(String arg) {
            return "result";
        }
    }
}

