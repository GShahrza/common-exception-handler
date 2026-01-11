package az.abb.loan.common.exception.handler.model;

public final class TraceHeaders {

    private TraceHeaders() {}

    public static final String TRACEPARENT = "traceparent";
    public static final String X_B3_TRACE_ID = "X-B3-TraceId";
    public static final String X_B3_SPAN_ID = "X-B3-SpanId";
    public static final String X_TRACE_ID = "X-Trace-Id";
}
