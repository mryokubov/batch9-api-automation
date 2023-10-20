package com.academy.techcenture.tests;

import com.academy.techcenture.pojo.*;
import io.restassured.RestAssured;
import static io.restassured.RestAssured.*;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class SimpleBooksApiTests {

    private static final String BASE_URI = "https://simple-books-api.glitch.me";
    String bearerToken = "823747f33c65b10e3953bc71404ad8ec3c29532a0f4c581ee2a8d0d98bfc32f3";

    @BeforeClass
    public static void setUp(){
        RestAssured.baseURI = BASE_URI;
    }

    @Test
    public void getApiStatusTest(){
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/status")
                .then()
                .statusCode(200)
                .body("status", equalTo("OK"));
    }

    @Test
    public void getBookListApiTest(){
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/books")
                .then()
                .statusCode(200)
                .body("", Matchers.instanceOf(List.class));
    }

    @Test
    public void getAllFictionBookListApiTest(){
        given()
                .contentType(ContentType.JSON)
                .queryParam("type", "fiction")
                .when()
                .get("/books")
                .then()
                .body("", Matchers.instanceOf(List.class))
                .body("size()", equalTo(4));
    }

    @Test
    public void getAllNonFictionBooksTest(){
        given()
                .contentType(ContentType.JSON)
                .queryParam("type", "non-fiction")
                .when()
                .get("/books")
                .then()
                .statusCode(200)
                .body("", Matchers.instanceOf(List.class))
                .body("size()", equalTo(2));
    }

    @Test
    public void getAllBooksWithLimitTest(){
        given()
                .contentType(ContentType.JSON)
                .queryParam("limit", "2")
                .when()
                .get("/books")
                .then()
                .statusCode(200)
                .body("size", equalTo(2));
    }

    @Test
    public void getSingleBookApiTest(){
        int randomBookId = (int)(Math.random() * 6) + 1;
        given()
                .pathParams("bookId", randomBookId)
                .contentType(ContentType.JSON)
                .when()
                .get("/books/{bookId}")
                .then()
                .statusCode(200)
                .body("id", equalTo(randomBookId))
                .body("name", notNullValue());
    }

    @Test
    public void getSingleBookApi2Test(){
        int id = 1;
        Response bookResponse = given()
                .pathParams("bookId", id)
                .contentType(ContentType.JSON)
                .when()
                .get("/books/{bookId}")
                .then()
                .statusCode(200)
                .extract()
                .response();

        BookResponse bookObject = bookResponse.as(BookResponse.class);

        Assert.assertEquals(1, bookObject.getId());
        Assert.assertEquals("The Russian", bookObject.getName());
        Assert.assertEquals("James Patterson and James O. Born", bookObject.getAuthor());
        Assert.assertEquals("1780899475", bookObject.getIsbn());
        Assert.assertEquals("fiction", bookObject.getType());
        Assert.assertEquals(12.98, bookObject.getPrice(), 0.01);
        Assert.assertEquals(12, bookObject.getCurrentStock());
        Assert.assertEquals(true, bookObject.isAvailable());

    }


    @Test
    public void postBookOrderApiBadTest(){

        String requestBody = "{\n" +
                "  \"bookId\": 1,\n" +
                "  \"customerName\": \"Ahmed Senior QA\"\n" +
                "}";

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + bearerToken)
                .body(requestBody)
                .when()
                .post("/orders")
                .then()
                .statusCode(201)
                .body("created", equalTo(true))
                .body("orderId", notNullValue());
    }

    @Test
    public void postBookOrderApiTest(){

        OrderRequestPayload requestPayload = new OrderRequestPayload(3, "Abbos Senior Dev");

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + bearerToken)
                .body(requestPayload)
                .when()
                .post("/orders")
                .then()
                .statusCode(201)
                .body("created", equalTo(true))
                .body("orderId", notNullValue());
    }

    @Test
    public void postBookOrderApiNotAvailableTest(){

        OrderRequestPayload requestPayload = new OrderRequestPayload(2, "Abbos Senior Dev");

//        given()
//                .contentType(ContentType.JSON)
//                .header("Authorization", "Bearer " + bearerToken)
//                .body(requestPayload)
//                .when()
//                .post("/orders")
//                .then()
//                .statusCode(404)
//                .body("error", equalTo("This book is not in stock. Try again later."));


        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + bearerToken)
                .body(requestPayload)
                .when()
                .post("/orders")
                .then()
                .statusCode(404)
                .extract()
                .response();

        PostOrderErrorResponse postOrderErrorResponse = response.as(PostOrderErrorResponse.class);
        Assert.assertEquals("This book is not in stock. Try again later.", postOrderErrorResponse.getError());
    }

    @Test
    public void getAllOrdersTest(){
        given()
                .header("Authorization", "Bearer " + bearerToken)
                .when()
                .get("/orders")
                .then()
                .statusCode(200)
                .body("", Matchers.instanceOf(List.class));
    }

    @Test
    public void getOneOrderTest(){

        String orderId = "0Sz-Ba6tWXko4TMqEraZb";
        int bookId = 1;

        Response response = given()
                .header("Authorization", "Bearer " + bearerToken)
                .pathParams("orderId", orderId)
                .when()
                .get("/orders/{orderId}")
                .then()
                .statusCode(200)
                .extract()
                .response();

        OrderResponse orderResponsePayload = response.as(OrderResponse.class);
        Assert.assertEquals(orderId, orderResponsePayload.getId());
        Assert.assertEquals(bookId, orderResponsePayload.getBookId());
        Assert.assertEquals("Ahmed Senior QA", orderResponsePayload.getCustomerName());
    }

    @Test
    public void patchOrderTest() {
        String orderId = "07pFdpOWE1SIouzvydAz7";
        OrderUpdateRequestPayload updateRequestPayload = new OrderUpdateRequestPayload("John Test");

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + bearerToken)
                .body(updateRequestPayload)
                .pathParam("orderId", orderId)
                .when()
                .patch("/orders/{orderId}")
                .then()
                .statusCode(204);
    }

    @Test
    public void deleteOrderTest() throws InterruptedException {

        String customerName = "Techcenture LLC";
        int bookId = 3;
        OrderRequestPayload payload = new OrderRequestPayload(bookId, customerName);

        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + bearerToken)
                .body(payload)
                .when()
                .post("/orders")
                .then()
                .statusCode(201)
                .extract()
                .response();

        String orderId = response.jsonPath().get("orderId");

        given()
                .header("Authorization", "Bearer " + bearerToken)
                .pathParam("orderId", orderId)
                .when()
                .delete("/orders/{orderId}")
                .then()
                .statusCode(204);
    }




}

