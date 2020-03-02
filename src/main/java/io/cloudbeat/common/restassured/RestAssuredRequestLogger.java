package io.cloudbeat.common.restassured;

import io.cloudbeat.common.CloudBeatTest;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

public class RestAssuredRequestLogger implements Filter {
    private final CloudBeatTest currentTest;

    public RestAssuredRequestLogger(CloudBeatTest test) {
        this.currentTest = test;
    }

    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
        String uri = requestSpec.getURI();
        /*if (!showUrlEncodedUri) {
            uri = UrlDecoder.urlDecode(uri, Charset.forName(requestSpec.getConfig().getEncoderConfig().defaultQueryParameterCharset()), true);
        }

        RequestPrinter.print(requestSpec, requestSpec.getMethod(), uri, logDetail, blacklistedHeaders, stream, shouldPrettyPrint);

         */
        return ctx.next(requestSpec, responseSpec);
    }

}
