package gorm.restapi.controller

import gorm.tools.repository.GormRepo
import gorm.tools.repository.api.RepositoryApi
import grails.gorm.DetachedCriteria

/**
 *  Adds controller methods for list
 *
 *  Created by alexeyzvegintcev.
 */
trait MangoControllerApi {

    abstract RepositoryApi getRepo()

    DetachedCriteria buildCriteria(Map criteriaParams=[:], Map params = [:], Closure closure = null) {
        getRepo().buildCriteria( criteriaParams + params, closure)
    }

    List query(Map criteriaParams=[:], Map params = [:], Closure closure = null) {
        getRepo().query(criteriaParams + params, closure)
    }

}
