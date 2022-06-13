package io.cloudbeat.common.restassured;

import io.cloudbeat.common.CbTestContext;
import io.restassured.filter.FilterContext;
import io.restassured.filter.OrderedFilter;
import io.restassured.internal.NameAndValue;
import io.restassured.internal.support.Prettifier;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CbRestAssuredFilter implements OrderedFilter {
    final CbTestContext ctx;
    public CbRestAssuredFilter(CbTestContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public Response filter(final FilterableRequestSpecification requestSpec,
                           final FilterableResponseSpecification responseSpec,
                           final FilterContext filterContext) {
    //public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
        final Prettifier prettifier = new Prettifier();
        final String url = requestSpec.getURI();
        final String method = requestSpec.getMethod();
        final Map<String, String> requestHeaders = toMap(requestSpec.getHeaders());
        String requestBody = null;
        if (Objects.nonNull(requestSpec.getBody())) {
            requestBody = prettifier.getPrettifiedBodyIfPossible(requestSpec);
        }
        // start step
        final String stepId = ctx.getReporter().startStep(stepName);
        // get a response
        final Response response = filterContext.next(requestSpec, responseSpec);
        // extract response properties
        final String statusText = response.getStatusLine();
        final int statusCode = response.getStatusCode();
        final String responseBody = prettifier.getPrettifiedBodyIfPossible(response, response.getBody());
        final Map<String, String> responseHeaders = toMap(response.getHeaders());
        final String stepName = String.format("%s %s", method, url);
        // end step
        ctx.getReporter().endStep(stepId);
        return response;
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }

    private static Map<String, String> toMap(final Iterable<? extends NameAndValue> items) {
        final Map<String, String> result = new HashMap<>();
        items.forEach(h -> result.put(h.getName(), h.getValue()));
        return result;
    }

}
