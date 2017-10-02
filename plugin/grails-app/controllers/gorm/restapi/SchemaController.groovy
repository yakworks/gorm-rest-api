package gorm.restapi

import grails.converters.JSON

@SuppressWarnings(['NoDef'])
class SchemaController {

	//static namespace = 'api'

    JsonSchemaGenerator jsonSchemaGenerator

    def index() {
    	println "SchemaController $params"
    	//TODO is id is null then what?
        render jsonSchemaGenerator.generate(params.id) as JSON
    }

}
