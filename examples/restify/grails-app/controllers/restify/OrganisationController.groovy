package restify

import gorm.restapi.controller.RestApiRepoController
import grails.core.GrailsApplication

import static org.springframework.http.HttpStatus.CREATED

//TODO: Added for manual tests - should be delted
class OrganisationController extends RestApiRepoController<Organisation> {

    OrganisationController() {
        super(Organisation, false)
    }


    def listGet() {
        respond query(params), [includes: listFields]
    }
}
