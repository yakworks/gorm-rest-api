package gorm.restapi

import grails.converters.JSON
import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j

@SuppressWarnings(['NoDef'])
@CompileDynamic
@Slf4j
class SchemaController {

    static namespace = 'api'

    JsonSchemaGenerator jsonSchemaGenerator

    def index() {
        log.debug "SchemaController $params"
        //TODO is id is null then what?
        render jsonSchemaGenerator.generate(params.id) as JSON
    }

}
