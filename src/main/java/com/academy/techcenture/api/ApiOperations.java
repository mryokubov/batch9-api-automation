package com.academy.techcenture.api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import static io.restassured.RestAssured.*;

public class ApiOperations {

    /**
     * Hide the constructor so it cannot be instantiated
     */
    private ApiOperations(){}

    /* Bearer token so it can be used for order view, place order, update order, delete order*/
    private static final String AUTH_TOKEN = "Bearer 46b2f31d63ce6067f6faa954577b947619e218bdfe9f8d31e44a84f792bfdd05";

    public static Response performGetRequest(String path, boolean requiresAuth){
        RequestSpecification requestSpecification = given();
        if (requiresAuth){
            requestSpecification = requestSpecification.header("Authorization", AUTH_TOKEN);
        }
        return requestSpecification
                .when()
                .get(path);
    }

    public static Response performGetRequestQueryParam(String path, String paramKey, String paramValue, boolean requiresAuth){
        RequestSpecification requestSpecification = given();
        if (requiresAuth){
            requestSpecification = requestSpecification.header("Authorization", AUTH_TOKEN);
        }
        return  requestSpecification
                .queryParam(paramKey, paramValue)
                .when()
                .get(path);
    }

    public static Response performGetRequestPathParam(String path, String paramKey, String paramValue, boolean requiresAuth){
        RequestSpecification requestSpecification = given();
        if (requiresAuth){
            requestSpecification = requestSpecification.header("Authorization", AUTH_TOKEN);
        }
        return   requestSpecification
                .pathParams(paramKey, paramValue)
                .when()
                .get(path);
    }

    public static Response performPostRequest(String path, Object payload){
        return  given()
                .contentType(ContentType.JSON)
                .header("Authorization", AUTH_TOKEN)
                .body(payload)
                .when()
                .post(path);
    }

    public static Response performPatchRequest(String path, String paramKey, String paramValue, Object payload){
        return   given()
                .contentType(ContentType.JSON)
                .header("Authorization", AUTH_TOKEN)
                .body(payload)
                .pathParams(paramKey, paramValue)
                .when()
                .patch(path);
    }

    public static Response performDeleteRequest(String path, String paramKey, String paramValue) {
        return given()
                .header("Authorization", AUTH_TOKEN)
                .pathParams(paramKey, paramValue)
                .when()
                .delete(path);
    }
}
