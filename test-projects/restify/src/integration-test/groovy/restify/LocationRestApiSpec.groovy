package restify

import geb.spock.GebSpec
import gorm.restapi.testing.RestApiTestTrait
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback
import static grails.web.http.HttpHeaders.CONTENT_TYPE
import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY

@Integration
@Rollback
//Copied all stuff here, because we cant override test cases, but need to test controller method Overriding
class LocationRestApiSpec extends GebSpec implements RestApiTestTrait{

    Class<Book> domainClass = Location
    boolean vndHeaderOnError = false

    String getResourcePath() {
        "${baseUrl}api/location"
    }

    //data to force a post or patch failure
    Map getInvalidData() { [city: null] }

    //Override if you don't want to use the autogenerated Example data.
    Map getInsertData() { [city: "city"] }

    Map getUpdateData() { [city: "city"] }


    void test_get_index() {
        given:
        def response = post_a_valid_resource()

        when: "The index action is requested"
        response = restBuilder.get(resourcePath)

        then: "The response is correct"
        response.status == OK.value()
        response.json.size() >= 0 // == []
    }

    void test_save_post() {
        given:
        def response
        when: "The save action is executed with no content"
        response = restBuilder.post(resourcePath)

        then: "The response is UNPROCESSABLE_ENTITY"
        verify_UNPROCESSABLE_ENTITY(response)

        when: "The save action is executed with invalid data"
        response = restBuilder.post(resourcePath) {
            json invalidData
        }
        then: "The response is UNPROCESSABLE_ENTITY"
        verify_UNPROCESSABLE_ENTITY(response)

        when: "The save action is executed with valid data"
        response = restBuilder.post(resourcePath) {
            json insertData
        }

        then: "The response is correct"
        response.status == CREATED.value()
        verifyHeaders(response)
        //response.json.id
        subsetEquals([city: "foo"], response.json)
        //Project.count() > 1// == 1
        def rget = restBuilder.get("$resourcePath/${response.json.id}")
        subsetEquals([city: "foo"], rget.json)
    }

    void test_update_put() {
        given:
        def response = post_a_valid_resource()

        when: "The update action is called with invalid data"
        def goodId = response.json.id
        def response2 = restBuilder.put("$resourcePath/$goodId") {
            json invalidData
        }

        then: "The response is invalid"
        verify_UNPROCESSABLE_ENTITY(response2)

        when: "The update action is called with valid data"
        goodId = response.json.id
        response = restBuilder.put("$resourcePath/$goodId") {
            json updateData
        }

        then: "The response is correct"
        response.status == OK.value()
        //response.json
        subsetEquals(updateData, response.json)
        //get it and make sure
        def rget = restBuilder.get("$resourcePath/$goodId")
        subsetEquals(updateData, rget.json)

    }

    void test_show_get() {
        given: "The save action is executed with valid data"
        def response = post_a_valid_resource()

        when: "When the show action is called to retrieve a resource"
        def id = response.json.id
        response = restBuilder.get("$resourcePath/$id")

        then: "The response is correct"
        response.status == OK.value()
        response.json.id == id
    }

    void test_delete() {
        given: "The save action is executed with valid data"
        def response = post_a_valid_resource()
        def id = response.json.id

        when: "When the delete action is executed on an unknown instance"
        response = restBuilder.delete("$resourcePath/99999")

        then: "The response is bad"
        response.status == NOT_FOUND.value()

        when: "When the delete action is executed on an existing instance"
        response = restBuilder.delete("$resourcePath/$id")

        then: "The response is correct"
        response.status == NO_CONTENT.value()
    }

    def post_a_valid_resource() {
        def response = restBuilder.post(resourcePath) {
            json insertData
        }
        verifyHeaders(response)
        assert response.status == CREATED.value()
        assert response.json.id
        return response
    }

    def verifyHeaders(response) {
        //assert response.headers.getFirst(CONTENT_TYPE) == 'application/json;charset=UTF-8'
        //assert response.headers.getFirst(HttpHeaders.LOCATION) == "$resourcePath/${response.json.id}"
        true
    }

    def verify_UNPROCESSABLE_ENTITY(response) {
        assert response.status == UNPROCESSABLE_ENTITY.value()
        if (vndHeaderOnError) {
            assert response.headers.getFirst(CONTENT_TYPE) == 'application/vnd.error;charset=UTF-8'
        }
        true
    }


}