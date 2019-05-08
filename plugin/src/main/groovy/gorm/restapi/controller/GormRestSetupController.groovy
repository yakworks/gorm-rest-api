package gorm.restapi.controller

import gorm.tools.repository.api.RepositoryApi
import grails.core.GrailsApplication
import groovy.transform.CompileDynamic

/**
 *  Adds controller methods to use appsetup configs
 *
 *  Created by alexeyzvegintcev.
 */
@CompileDynamic
trait GormRestSetupController<D> {

    abstract RepositoryApi getRepo()

    abstract Class<D> getEntityClass()

    abstract GrailsApplication getGrailsApplication()

    /**
     *
     * @return ConfigObject for current controller, that is under `screens."$controllerName"` in appSetup config
     */
    @CompileDynamic
    Map getExternalConfig() {
        ConfigObject controllerConfig = getGrailsApplication().getSetupConfig(true)?.screens."$controllerName"
        return controllerConfig
    }

    /**
     * Default field list that should be rendered on GET/POST/PUT requests for entity
     * Checks if Domain has field list(`showFields`), then if repo does, if not returns ["*"]
     *
     * @return list of fields that should be rendered
     */
    List<String> getDefaultShowFields() { (entityClass.getShowFields() ?: getRepo().showFields) ?: ["*"] }

    /**
     * Field list that should be rendered on GET/POST/PUT requests for entity
     * Checks if appSetup Config has configuration, if not use defaultShowFields
     *
     * @return list of fields that should be rendered
     */
    @CompileDynamic
    List<String> getShowFields() {
        return externalConfig?.show?.fields ?: defaultShowFields
    }

    /**
     * Default field list that should be rendered on list requests for entity
     * Checks if Domain has field list(`listFields`), then if repo does, if not returns ["*"]
     *
     * @return list of fields that should be rendered for each entity in list
     */
    List<String> getDefaultListFields() { (entityClass.getListFields() ?: getRepo().listFields) ?: ["*"] }

    /**
     * Field list that should be rendered on list request
     * Checks if appSetup Config has configuration, if not use defaultListFields
     *
     * @return list of fields that should be rendered for each entity in list
     */
    @CompileDynamic
    List<String> getListFields() {
        return externalConfig?.list?.fields ?: defaultListFields
    }

}
