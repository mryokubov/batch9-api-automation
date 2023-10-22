package com.academy.techcenture.tests;

import com.academy.techcenture.pojos.*;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import static com.academy.techcenture.api.ApiOperations.*;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import java.util.List;
import static com.academy.techcenture.end_points.ApiEndPoints.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class SimpleBooksApiTests {

    private Faker faker = new Faker();

    @BeforeClass
    public static void setUp(){
        RestAssured.baseURI = BASE_URI;
    }

    @Test
    public void getApiStatusTest(){
        performGetRequest(GET_STATUS_ENDPOINT, false)
                .then()
                .statusCode(200)
                .body("status", equalTo("OK"));
    }

    @Test
    public void getBookListApiTest(){
        performGetRequest(GET_ALL_BOOKS_ENDPOINT, false)
                .then()
                .statusCode(200)
                .body("", Matchers.instanceOf(List.class));
    }

    @Test
    public void getAllBooksVerifyEachBookTest(){

        Response response =  performGetRequest(GET_ALL_BOOKS_ENDPOINT, false)
                .then()
                .statusCode(200)
                .extract()
                .response();

        BookLimitedDetailsResponse[] booksResponse = response.as(BookLimitedDetailsResponse[].class);
        for (BookLimitedDetailsResponse bookLimitedDetailsResponse : booksResponse) {
            Assert.assertTrue(bookLimitedDetailsResponse.getId() != null);
        }

    }


    @Test
    public void getAllFictionBookListApiTest(){
        performGetRequestQueryParam(GET_ALL_BOOKS_ENDPOINT, "type", "fiction", false)
                .then()
                .statusCode(200)
                .body("", Matchers.instanceOf(List.class))
                .body("size()", equalTo(4));
    }

    @Test
    public void getAllNonFictionBooksTest(){
        performGetRequestQueryParam(GET_ALL_BOOKS_ENDPOINT, "type", "non-fiction", false)
                .then()
                .statusCode(200)
                .body("", Matchers.instanceOf(List.class))
                .body("size()", equalTo(2));
    }

    @Test
    public void getAllBooksWithLimitTest(){
        performGetRequestQueryParam(GET_ALL_BOOKS_ENDPOINT, "limit", "2", false)
                .then()
                .statusCode(200)
                .body("", Matchers.instanceOf(List.class))
                .body("size()", equalTo(2));
    }

    @Test
    public void getSingleBookApiTest(){
        int randomBookId = (int)(Math.random() * 6) + 1;
        performGetRequestPathParam(GET_ONE_BOOK_ENDPOINT, "bookId", String.valueOf(randomBookId), false)
                .then()
                .statusCode(200)
                .body("id", equalTo(randomBookId));
    }

    @Test
    public void getSingleBookApi2Test(){
        int id = 1;
        Response response = performGetRequestPathParam(GET_ONE_BOOK_ENDPOINT, "bookId", String.valueOf(id), false)
                .then()
                .statusCode(200)
                .extract()
                .response();

        BookFullDetailsResponse bookObject = response.as(BookFullDetailsResponse.class);

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
    public void getAllOrdersTest(){
        performGetRequest(GET_All_ORDERS_ENDPOINT, true)
                .then()
                .statusCode(200)
                .body("", Matchers.instanceOf(List.class));
    }


    @Test
    public void postBookOrderApiBadTest(){

        String customerName = faker.name().fullName();
        int bookId = 1;
        String strPayload = "{\n" +
                "  \"bookId\": "+bookId+",\n" +
                "  \"customerName\": \""+customerName+"\"" +
                "}";

        performPostRequest(POST_ORDERS_ENDPOINT, strPayload)
                .then()
                .statusCode(201)
                .body("created", equalTo(true))
                .body("orderId", notNullValue());
    }

    @Test
    public void postBookOrderApiTest(){

        OrderBookRequest payload = getNewOrderBookRequest();

        performPostRequest(POST_ORDERS_ENDPOINT, payload)
                .then()
                .statusCode(201)
                .body("created", equalTo(true))
                .body("orderId", notNullValue());

    }

    @Test
    public void postBookOrderApiNotAvailableTest(){
        OrderBookRequest.OrderRequestPayload requestPayload = new OrderBookRequest.OrderRequestPayload(2, "Abbos Senior Dev");

        /*
            given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + bearerToken)
                    .body(requestPayload)
                    .when()
                    .post("/orders")
                    .then()
                    .statusCode(404)
                    .body("error", equalTo("This book is not in stock. Try again later."));
        */

        Response response = performPostRequest(POST_ORDERS_ENDPOINT, requestPayload);

        PostOrderErrorResponse postOrderErrorResponse = response.as(PostOrderErrorResponse.class);
        Assert.assertEquals("This book is not in stock. Try again later.", postOrderErrorResponse.getError());
    }


    @Test
    public void getOneOrderTest(){

        OrderBookRequest newOrderBookRequest = getNewOrderBookRequest();
        int bookId = newOrderBookRequest.getBookId();

        Response response = performPostRequest(POST_ORDERS_ENDPOINT, newOrderBookRequest)
                .then()
                .statusCode(201)
                .extract().response();
        String orderId = response.jsonPath().getString("orderId");

        Response getOrderResponse = performGetRequestPathParam(GET_ONE_ORDER_ENDPOINT, "orderId", orderId, true)
                .then()
                .statusCode(200)
                .extract()
                .response();

        OrderResponse orderResponsePayload = getOrderResponse.as(OrderResponse.class);
        Assert.assertEquals(orderId, orderResponsePayload.getId());
        Assert.assertEquals(bookId, orderResponsePayload.getBookId());
        Assert.assertEquals(newOrderBookRequest.getCustomerName(), orderResponsePayload.getCustomerName());
    }

    @Test
    public void patchOrderTest() {
        OrderBookRequest newOrderBookRequest = getNewOrderBookRequest();

        Response response = performPostRequest(POST_ORDERS_ENDPOINT, newOrderBookRequest)
                .then()
                .statusCode(201)
                .extract().response();
        String orderId = response.jsonPath().getString("orderId");

        OrderUpdateRequest updateRequestPayload = new OrderUpdateRequest("John Test");

        performPatchRequest(PATCH_ONE_ORDER_ENDPOINT, "orderId", orderId, updateRequestPayload)
                .then()
                .statusCode(204);
    }

    @Test
    public void deleteOrderTest() {

        OrderBookRequest newOrderBookRequest = getNewOrderBookRequest();

        Response response = performPostRequest(POST_ORDERS_ENDPOINT, newOrderBookRequest)
                .then()
                .statusCode(201)
                .extract()
                .response();

        String orderId = response.jsonPath().getString("orderId");
        performDeleteRequest(DELETE_ONE_ORDER_ENDPOINT, "orderId", orderId)
                .then()
                .statusCode(204);
    }

    public OrderBookRequest getNewOrderBookRequest(){
        String customerName = faker.name().fullName();
        int bookId = 3;
        OrderBookRequest payload = new OrderBookRequest(customerName, bookId);
        return payload;
    }

}

