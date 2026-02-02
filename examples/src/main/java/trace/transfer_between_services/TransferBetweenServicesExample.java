package trace.transfer_between_services;

import com.coze.loop.client.CozeLoopClient;
import com.coze.loop.client.CozeLoopClientBuilder;
import com.coze.loop.spec.tracespec.SpanKeys;
import com.coze.loop.trace.CozeLoopSpan;
import io.opentelemetry.context.Context;

import java.util.Map;

/**
 * Example demonstrating trace propagation between services.
 * In a real scenario, Service A and Service B would be separate processes.
 * They are combined here for demonstration purposes.
 */
public class TransferBetweenServicesExample {

    public static void main(String[] args) {
        // Initialize client (shared for this demo, usually separate in real services)
        CozeLoopClient client = new CozeLoopClientBuilder()
                .workspaceId(System.getenv("COZELOOP_WORKSPACE_ID"))
                .tokenAuth(System.getenv("COZELOOP_API_TOKEN"))
                .serviceName("service-A")
                .build();

        try {
            System.out.println("--- Starting Service A Operation ---");
            Map<String, String> carrier = serviceA(client);

            System.out.println("\n--- Service A passed context to Service B ---");
            
            // Simulating Service B receiving the request
            serviceB(client, carrier);

            System.out.println("\n--- Distributed Trace Completed ---");
        } finally {
            // force flush trace to backend
            // 警告：一般情况下不需要调用此方法，因为 spans 会自动批量上报。
            // 注意：flush 会阻塞并等待上报完成，可能导致频繁上报，影响性能。
            client.flush();
        }
    }

    /**
     * Simulated Service A
     * @return headers containing trace context to be passed to Service B
     */
    private static Map<String, String> serviceA(CozeLoopClient client) {
        // 1. Start root span in Service A
        try (CozeLoopSpan span = client.startSpan("service_a_operation", "main")) {
            span.setTag(SpanKeys.SERVICE_NAME, "ServiceA");
            
            // 2. Set baggage that will be propagated to Service B
            span.setBaggage("user_id", "user_12345");
            span.setBaggage("request_id", "req_abc_789");
            
            System.out.println("Service A: Performing some work...");
            try { Thread.sleep(100); } catch (InterruptedException e) {}

            // 3. Get headers for context propagation
            Map<String, String> carrier = span.toHeader();
            System.out.println("Service A: Generated headers: " + carrier);

            return carrier;
        }
    }

    /**
     * Simulated Service B
     * @param headers headers received from Service A
     */
    private static void serviceB(CozeLoopClient client, Map<String, String> headers) {
        // 1. Extract context from incoming headers
        Context parentContext = client.extractContext(headers);
        System.out.println("Service B: Extracted parent context from headers");
        
        // 2. Start a new span as a child of the extracted context
        try (CozeLoopSpan span = client.startSpan("service_b_operation", "main", parentContext)) {
            span.setTag(SpanKeys.SERVICE_NAME, "ServiceB");
            
            // Print inherited baggage
            System.out.println("Service B: Inherited Baggage: " + span.getBaggage());
            System.out.println("Service B: Trace ID: " + span.getTraceID());

            System.out.println("Service B: Received request, processing...");
            try { Thread.sleep(200); } catch (InterruptedException e) {}

            // Start a child span within Service B
            // This child span will automatically inherit Baggage from Service A
            // because it is started within the scope of service_b_operation.
            try (CozeLoopSpan child = client.startSpan("service_b_internal_logic", "logic")) {
                child.setUserID("user_12345");
                child.setMessageID("msg_999");
                System.out.println("Service B: Executing internal logic (Baggage inherited: " + child.getBaggage() + ")...");
                try { Thread.sleep(50); } catch (InterruptedException e) {}
            }
        }
    }
}
