package gorm.restapi.controller

import grails.artefact.Artefact

//import grails.transaction.ReadOnly
//import grails.gorm.transactions.Transactional
import grails.transaction.Transactional
import grails.util.GrailsNameUtils
import grails.web.http.HttpHeaders

import static org.springframework.http.HttpStatus.*

/**
 *
 * A simple Controller for a RestApi. Can be extended and gets generated by default using
 * the @RestApi on the domain.
 * This does not use conditional checking
 *
 * @author Joshua Burnett
 *
 * based on Grails' RestFullController
 */
@SuppressWarnings(['FactoryMethodName', 'NoDef'])
@Artefact("Controller")
//@Transactional(readOnly = true)
class SimpleRestApiDomainController<T, ID extends Serializable> implements CoreControllerActions<T> {
    static allowedMethods = [create: "POST", update: ["PUT", "PATCH"], delete: "DELETE"]

    static responseFormats = ['json']
    static namespace = 'api'

    Class<T> resource
    String resourceName
    String resourceClassName
    boolean readOnly

    SimpleRestApiDomainController(Class<T> resource) {
        this(resource, false)
    }

    SimpleRestApiDomainController(Class<T> resource, boolean readOnly) {
        this.resource = resource
        this.readOnly = readOnly
        resourceClassName = resource.simpleName
        resourceName = GrailsNameUtils.getPropertyName(resource)
    }

    /**
     * Lists all resources up to the given maximum
     *
     * @param max The maximum
     * @return A list of resources
     */
    @Transactional(readOnly = true)
    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond listAllResources(params), model: [("${resourceName}Count".toString()): countResources()]
    }

    /**
     * Shows a single resource
     * @param id The id of the resource
     * @return The rendered resource or a 404 if it doesn't exist
     */
    @Transactional(readOnly = true)
    def show() {
        respond queryForResource(params.id)
    }

    /**
     * creates and saves a resource
     */
    @Transactional
    def create() {
        if (handleReadOnly()) {
            return
        }
        def instance = createResource()

        instance.validate()
        if (instance.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond instance.errors, view: 'create' // STATUS CODE 422
            return
        }

        saveResource instance
        addLocationHeader(response, instance.id, 'show')
        respond instance, [status: CREATED, view: 'show']
    }

    /**
     * Updates a resource for the given id
     * @param id
     */
    @Transactional
    def patch() {
        update()
    }

    /**
     * Updates a resource for the given id
     * @param id
     */
    @Transactional
    def update() {
        if (handleReadOnly()) {
            return
        }

        T instance = queryForResource(params.id)
        if (instance == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        instance.properties = getObjectToBind()

        if (instance.hasErrors()) {
            println "ERRORS !!!!"
            transactionStatus.setRollbackOnly()
            respond instance.errors, view: 'edit' // STATUS CODE 422
            return
        }
        println "NO ERRORS !!!!"

        updateResource instance
        addLocationHeader(response, instance.id, 'show')
        respond instance, [status: OK]

    }

    /**
     * Deletes a resource for the given id
     * @param id The id
     */
    @Transactional
    def delete() {
        if (handleReadOnly()) {
            return
        }

        def instance = queryForResource(params.id)
        if (instance == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        deleteResource instance
        render status: NO_CONTENT
    }

}

