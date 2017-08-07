package gorm.restapi

import grails.artefact.Artefact
import grails.converters.JSON
import grails.rest.RestfulController
import grails.web.http.HttpHeaders

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.OK

@Artefact("Controller")
abstract class RestApiController<T> extends RestfulController<T> {
    //Responce formats, json - by default
    static responseFormats = ['json']
    static namespace = 'api'

    //ErrorMessageService errorMessageService

    RestApiController(Class<T> domainClass) {
        this(domainClass, false)
    }

    RestApiController(Class<T> domainClass, boolean readOnly) {
        super(domainClass, readOnly)
    }

    Class getDomainClass() {
        resource
    }

}
