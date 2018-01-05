package gorm.restapi.controller

import gorm.tools.repository.GormRepoEntity
import gorm.tools.repository.api.RepositoryApi
import grails.artefact.controller.support.ResponseRenderer
import grails.databinding.SimpleMapDataBindingSource
import grails.web.Action
import grails.web.api.ServletAttributes
import grails.web.databinding.DataBindingUtils
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.springframework.core.GenericTypeResolver

import static org.springframework.http.HttpStatus.*

@CompileStatic
trait RestRepositoryApi<D extends GormRepoEntity> implements RestResponder, ServletAttributes{

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
    @CompileDynamic
    RepositoryApi<D> getRepo() {
        getEntityClass().getRepo()
    }

    /**
     * POST /api/entity
     * Create with data
     */
    @Action
    def post() {
        D instance = getRepo().create(getDataMap())
        respond instance, [status: CREATED] //201
    }

    /**
     * PUT /api/entity/${id}
     * Update with data
     */
    @Action
    def put() {
        D instance = getRepo().update(getDataMap())
        respond instance, [status: OK] //200
    }

    /**
     * DELETE /api/entity/${id}
     * update with params
     */
    @Action
    def delete() {
        getRepo().removeById((Serializable)params.id)
        callRender(status: NO_CONTENT) //204
    }

    /**
     * GET /api/entity/${id}
     * update with params
     */
    @Action
    def get() {
        respond getRepo().get(params)
    }

    /**
     * The Map object that can be bound to create or update domain entity.  Defaults whats in the request based on mime-type.
     * Subclasses may override this
     */
    @CompileDynamic
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