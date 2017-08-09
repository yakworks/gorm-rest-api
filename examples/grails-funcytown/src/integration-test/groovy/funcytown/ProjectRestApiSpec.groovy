package funcytown

import grails.test.mixin.integration.Integration
import grails.transaction.*
import static grails.web.http.HttpHeaders.*
import static org.springframework.http.HttpStatus.*
import spock.lang.*
import geb.spock.*
import grails.plugins.rest.client.RestBuilder
import gorm.restapi.testing.RestApiFuncSpec

@Integration
@Rollback
class ProjectRestApiSpec extends RestApiFuncSpec {

    String getResourcePath() {
        "${baseUrl}/api/project"
    }

    Map getValidJson() {[ name: "project", code: "x123"]}

    Map getUpdateJson() { [name: "project Update", code: "x123u"]}

    Map getInvalidJson() { ["name": null] }

}