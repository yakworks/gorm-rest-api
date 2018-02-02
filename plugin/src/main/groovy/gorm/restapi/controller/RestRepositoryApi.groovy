package gorm.restapi.controller

import gorm.tools.repository.EntityFieldsHandler
import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepoEntity
import gorm.tools.repository.api.RepositoryApi
import grails.artefact.controller.RestResponder
import grails.artefact.controller.support.ResponseRenderer
import grails.converters.JSON
import grails.databinding.SimpleMapDataBindingSource
import grails.util.GrailsClassUtils
import grails.web.Action
import grails.web.api.ServletAttributes
import grails.web.databinding.DataBindingUtils
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.codehaus.groovy.runtime.InvokerHelper
import org.springframework.core.GenericTypeResolver

import static org.springframework.http.HttpStatus.*

@CompileStatic
@SuppressWarnings(['CatchRuntimeException', 'NoDef'])
trait RestRepositoryApi<D extends GormRepoEntity> implements RestResponder, ServletAttributes, MangoControllerApi, RestControllerErrorHandling {

    /**
     * The java class for the Gorm domain (persistence entity). will generally get set in constructor or using the generic as
     * done in {@link gorm.tools.repository.GormRepo#getEntityClass}
     * using the {@link org.springframework.core.GenericTypeResolver}
     * @see org.grails.datastore.mapping.model.PersistentEntity#getJavaClass().
     */
    Class<D> entityClass // the domain class this is for

    /**
     * The gorm domain class. uses the {@link org.springframework.core.GenericTypeResolver} is not set during contruction
     */
    Class<D> getEntityClass() {
        if (!entityClass) this.entityClass = (Class<D>) GenericTypeResolver.resolveTypeArgument(getClass(), RestRepositoryApi.class)
        return entityClass
    }

    /**
     * Gets the repository for the entityClass
     * @return The repository
     */
    RepositoryApi<D> getRepo() {
        //GrailsClassUtils.getStaticPropertyValue(getEntityClass(),'repo')
        (RepositoryApi<D>) InvokerHelper.invokeStaticMethod(getEntityClass(), 'getRepo', null)
    }

    /**
     * POST /api/entity
     * Create with data
     */
    @Action
    def post() {
        try {
            D instance = getRepo().create(getDataMap())
            response.status = CREATED.value() //201
            callRespond instance
        } catch (RuntimeException e){
            handleException(e)
        }
    }

    /**
     * PUT /api/entity/${id}
     * Update with data
     */
    @Action
    def put() {
        Map data = [id: params.id]
        data.putAll(getDataMap()) // getDataMap doesnt contains id because it passed in params
        try {
            D instance = getRepo().update(data)
            respond instance, [status: OK] //200
        } catch (RuntimeException e){
            handleException(e)
        }

    }

    /**
     * DELETE /api/entity/${id}
     * update with params
     */
    @Action
    def delete() {
        try {
            getRepo().removeById((Serializable) params.id)
            callRender(status: NO_CONTENT) //204
        } catch (RuntimeException e){
            handleException(e)
        }

    }

    /**
     * GET /api/entity/${id}
     * update with params
     */
    @Action
    def get() {
        try {
            D entity = getRepo().get(params)
            if (((GormRepo) getRepo()).getShowFieldsConfig()){
                    respond(((EntityFieldsHandler) getRepo()).getFields(entity))}
            else {
                callRespond(entity)
            }
        } catch (RuntimeException e){
            handleException(e)
        }
    }

    @Action
    def index(){
        listGet()
    }

    /**
     * request type is handled in urlMapping
     *
     * returns the list of domain objects
     */
    @Action
    def listPost() {
        List list =  query((request.JSON?:[:]) as Map, params)
        if (((EntityFieldsHandler) getRepo()).getListFieldsConfig()){
            list = ((EntityFieldsHandler) getRepo()).getListFields(list)
        }
        respond(list)

    }

    /**
     * request type is handled in urlMapping
     *
     * returns the list of domain objects
     */
    @Action
    def listGet() {
        List list = query(params)
        if (((EntityFieldsHandler) getRepo()).getListFieldsConfig()){
            list = ((EntityFieldsHandler) getRepo()).getListFields(list)
        }
        respond(list)
    }

    /**
     * The Map object that can be bound to create or update domain entity.  Defaults whats in the request based on mime-type.
     * Subclasses may override this
     */
    @CompileDynamic //so it can access the SimpleMapDataBindingSource.map
    Map getDataMap() {
        SimpleMapDataBindingSource bsrc =
            (SimpleMapDataBindingSource) DataBindingUtils.createDataBindingSource(grailsApplication, getEntityClass(), getRequest())
        return bsrc.map
    }

    /**
     * Cast this to ResponseRenderer and call render
     * @param args
     */
    void callRender(Map args) {
        ((ResponseRenderer) this).render args
    }

    /**
     * CAst this to RestResponder and call respond
     * @param value
     * @param args
     */
    def callRespond(value, Map args = [:]) {
        ((RestResponder) this).respond value, args
    }


}