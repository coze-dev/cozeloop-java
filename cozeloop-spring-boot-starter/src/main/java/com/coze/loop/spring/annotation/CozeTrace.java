package com.coze.loop.spring.annotation;

import java.lang.annotation.*;

/**
 * Annotation to automatically create a trace span for a method.
 * Supports SpEL expressions for dynamic span names and input/output capture.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CozeTrace {
    
    /**
     * Span name. Supports SpEL expressions.
     * Example: "llm_call_#{#args[0]}" or "#{T(java.util.UUID).randomUUID().toString()}"
     *
     * @return span name or SpEL expression
     */
    String value() default "";
    
    /**
     * Span type (e.g., "llm", "tool", "custom").
     *
     * @return span type
     */
    String spanType() default "custom";
    
    /**
     * Whether to capture method arguments as span input.
     * If true, all arguments will be serialized to JSON.
     *
     * @return true to capture arguments
     */
    boolean captureArgs() default false;
    
    /**
     * Whether to capture method return value as span output.
     * If true, the return value will be serialized to JSON.
     *
     * @return true to capture return value
     */
    boolean captureReturn() default false;
    
    /**
     * SpEL expression to extract input from method context.
     * Available variables: #args (argument array), #arg0, #arg1, ... (individual arguments)
     * Example: "#args[0].query" or "#arg0"
     *
     * @return SpEL expression for input extraction
     */
    String inputExpression() default "";
    
    /**
     * SpEL expression to extract output from method context.
     * Available variables: #result (return value)
     * Example: "#result.data" or "#result.toString()"
     *
     * @return SpEL expression for output extraction
     */
    String outputExpression() default "";
}

