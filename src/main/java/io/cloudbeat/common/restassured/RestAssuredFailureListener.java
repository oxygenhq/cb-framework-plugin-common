package io.cloudbeat.common.restassured;

import io.cloudbeat.common.CloudBeatTest;
import io.restassured.listener.ResponseValidationFailureListener;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public class RestAssuredFailureListener implements ResponseValidationFailureListener {
    private final CloudBeatTest currentTest;

    public RestAssuredFailureListener(CloudBeatTest test) {
        this.currentTest = test;
    }

    @Override
    public void onFailure(RequestSpecification requestSpecification, ResponseSpecification responseSpecification, Response response) {

    }
}
