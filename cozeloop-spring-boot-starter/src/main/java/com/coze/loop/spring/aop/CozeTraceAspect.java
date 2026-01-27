package com.coze.loop.spring.aop;

import com.coze.loop.client.CozeLoopClient;
import com.coze.loop.spring.annotation.CozeTrace;
import com.coze.loop.trace.CozeLoopSpan;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

/**
 * AOP aspect to handle @CozeTrace annotation.
 */
@Aspect
@Component
public class CozeTraceAspect {
    private static final Logger logger = LoggerFactory.getLogger(CozeTraceAspect.class);
    private final ExpressionParser parser = new SpelExpressionParser();
    
    private final CozeLoopClient client;
    
    public CozeTraceAspect(CozeLoopClient client) {
        this.client = client;
    }
    
    @Around("@annotation(cozeTrace)")
    public Object traceMethod(ProceedingJoinPoint pjp, CozeTrace cozeTrace) throws Throwable {
        String spanName = resolveSpanName(cozeTrace, pjp);
        String spanType = cozeTrace.spanType();
        
        try (CozeLoopSpan span = client.startSpan(spanName, spanType)) {
            // Capture input
            captureInput(span, cozeTrace, pjp);
            
            try {
                // Execute method
                Object result = pjp.proceed();
                
                // Capture output
                captureOutput(span, cozeTrace, result);
                
                // Mark as successful
                span.setStatusCode(0);
                
                return result;
            } catch (Throwable throwable) {
                // Capture error
                span.setError(throwable);
                span.setStatusCode(1);
                throw throwable;
            }
        }
    }
    
    /**
     * Resolve span name from annotation.
     * Supports SpEL expressions.
     */
    private String resolveSpanName(CozeTrace annotation, ProceedingJoinPoint pjp) {
        String name = annotation.value();
        
        // If no name specified, use method name
        if (name.isEmpty()) {
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            return signature.getMethod().getName();
        }
        
        // Check if it's a SpEL expression
        if (name.contains("#{")) {
            try {
                StandardEvaluationContext context = new StandardEvaluationContext();
                Object[] args = pjp.getArgs();
                context.setVariable("args", args);
                
                // Set individual arguments
                for (int i = 0; i < args.length; i++) {
                    context.setVariable("arg" + i, args[i]);
                }
                
                Expression expression = parser.parseExpression(name);
                Object value = expression.getValue(context);
                return value != null ? value.toString() : name;
            } catch (Exception e) {
                logger.warn("Failed to evaluate SpEL expression for span name: {}", name, e);
                return name;
            }
        }
        
        return name;
    }
    
    /**
     * Capture input based on annotation configuration.
     */
    private void captureInput(CozeLoopSpan span, CozeTrace annotation, ProceedingJoinPoint pjp) {
        try {
            // Use input expression if provided
            if (!annotation.inputExpression().isEmpty()) {
                Object input = evaluateExpression(annotation.inputExpression(), pjp.getArgs(), null);
                if (input != null) {
                    span.setInput(input);
                }
            } else if (annotation.captureArgs()) {
                // Capture all arguments
                Object[] args = pjp.getArgs();
                if (args != null && args.length > 0) {
                    if (args.length == 1) {
                        span.setInput(args[0]);
                    } else {
                        span.setInput(args);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to capture input", e);
        }
    }
    
    /**
     * Capture output based on annotation configuration.
     */
    private void captureOutput(CozeLoopSpan span, CozeTrace annotation, Object result) {
        try {
            // Use output expression if provided
            if (!annotation.outputExpression().isEmpty()) {
                Object output = evaluateExpression(annotation.outputExpression(), null, result);
                if (output != null) {
                    span.setOutput(output);
                }
            } else if (annotation.captureReturn() && result != null) {
                // Capture return value
                span.setOutput(result);
            }
        } catch (Exception e) {
            logger.warn("Failed to capture output", e);
        }
    }
    
    /**
     * Evaluate SpEL expression.
     */
    private Object evaluateExpression(String expressionString, Object[] args, Object result) {
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            
            if (args != null) {
                context.setVariable("args", args);
                for (int i = 0; i < args.length; i++) {
                    context.setVariable("arg" + i, args[i]);
                }
            }
            
            if (result != null) {
                context.setVariable("result", result);
            }
            
            Expression expression = parser.parseExpression(expressionString);
            return expression.getValue(context);
        } catch (Exception e) {
            logger.warn("Failed to evaluate expression: {}", expressionString, e);
            return null;
        }
    }
}

