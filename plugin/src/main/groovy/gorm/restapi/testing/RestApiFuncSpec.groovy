package gorm.restapi.testing

import grails.test.mixin.integration.Integration
import grails.transaction.*
import static grails.web.http.HttpHeaders.*
import grails.web.http.HttpHeaders
import static org.springframework.http.HttpStatus.*
import spock.lang.*
import geb.spock.*
import grails.plugins.rest.client.RestBuilder

// @Integration
// @Rollback
abstract class RestApiFuncSpec extends GebSpec implements RestApiTestTrait{

    // RestBuilder getRestBuilder() {
    //     new RestBuilder()
    // }

    // String getResourcePath() {
    //     "${baseUrl}/api/project"
    // }

    // Map getValidJson() {[ name: "project", code: "x123"]}

    // Map getUpdateJson() { [name: "project Update", code: "x123u"]}

    // String getInvalidJson() {'''{
    //     "name": null
    // }'''}

    void test_get_index() {
        when:"The index action is requested"
        def response = restBuilder.get(resourcePath)

        then:"The response is correct"
        response.status == OK.value()
        response.json.size() > 0 // == []
    }

    void test_save_post() {
        when:"The save action is executed with no content"
        def response = restBuilder.post(resourcePath)

        then:"The response is UNPROCESSABLE_ENTITY"
        verify_UNPROCESSABLE_ENTITY(response)
        //subsetEquals(validJson, response.json)

        when:"The save action is executed with invalid data"
        response = restBuilder.post(resourcePath) {
            json invalidJson
        }
        then:"The response is UNPROCESSABLE_ENTITY"
        verify_UNPROCESSABLE_ENTITY(response)

        when:"The save action is executed with valid data"
        response = restBuilder.post(resourcePath) {
            json validJson
        }

        then:"The response is correct"
        response.status == CREATED.value()
        verifyHeaders(response)
        //response.json.id
        subsetEquals(validJson, response.json)
        //Project.count() > 1// == 1
        def rget = restBuilder.get("$resourcePath/${response.json.id}")
        subsetEquals(validJson, rget.json)
    }


    void test_update_put() {
        given:
        def response = post_a_valid_resource()

        when: "The update action is called with invalid data"
        def goodId = response.json.id
        response = restBuilder.put("$resourcePath/$goodId") {
            json invalidJson
        }

        then:"The response is invalid"
        verify_UNPROCESSABLE_ENTITY(response)

        when:"The update action is called with valid data"
        response = restBuilder.put("$resourcePath/$goodId") {
            json updateJson
        }

        then:"The response is correct"
        response.status == OK.value()
        //response.json
        subsetEquals(updateJson, response.json)
        //get it and make sure
        def rget = restBuilder.get("$resourcePath/$goodId")
        subsetEquals(updateJson, rget.json)

    }

    void test_show_get() {
        given :"The save action is executed with valid data"
        def response = post_a_valid_resource()

        when:"When the show action is called to retrieve a resource"
        def id = response.json.id
        response = restBuilder.get("$resourcePath/$id")

        then:"The response is correct"
        response.status == OK.value()
        response.json.id == id
    }

    void test_delete() {
        given :"The save action is executed with valid data"
        def response = post_a_valid_resource()
        def id = response.json.id

        when:"When the delete action is executed on an unknown instance"
        response = restBuilder.delete("$resourcePath/99999")

        then:"The response is bad"
        response.status == NOT_FOUND.value()

        when:"When the delete action is executed on an existing instance"
        response = restBuilder.delete("$resourcePath/$id")

        then:"The response is correct"
        response.status == NO_CONTENT.value()
        //!Project.get(id)
    }

    def post_a_valid_resource(){
        def response = restBuilder.post(resourcePath) {
            json validJson
        }
        verifyHeaders(response)
        assert response.status == CREATED.value()
        assert response.json.id
        return response
    }

    def verifyHeaders(response){
        assert response.headers.getFirst(CONTENT_TYPE) == 'application/json;charset=UTF-8'
        assert response.headers.getFirst(HttpHeaders.LOCATION) == "$resourcePath/${response.json.id}"
        true
    }

    def verify_UNPROCESSABLE_ENTITY(response){
        assert response.status == UNPROCESSABLE_ENTITY.value()
        assert response.headers.getFirst(CONTENT_TYPE) == 'application/vnd.error;charset=UTF-8'
        true
    }


}