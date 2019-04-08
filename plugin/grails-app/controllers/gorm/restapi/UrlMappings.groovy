package gorm.restapi

import grails.util.GrailsClassUtils
import groovy.transform.CompileDynamic

@CompileDynamic
class UrlMappings {

    static mappings = {

        for (controller in getGrailsApplication().controllerClasses) {
            //println "controler $controller.fullName"
            String cName = controller.logicalPropertyName
            String namespace = GrailsClassUtils.getStaticPropertyValue(controller.clazz, 'namespace')
            //println "controller $cName with namespace $namespace"

            if (namespace == 'api') {
                group("/api") {
                    "/${cName}/schema"(controller: "schema", action: "index") {
                        id = cName
                    }
                    //when a post is called allows an action
                    post "/${cName}/$action(.$format)?"(controller: cName, namespace: 'api')
                    //or
                    post "/${cName}/actions/$action(.$format)?"(controller: cName, namespace: 'api')

                    delete "/${cName}/$id(.$format)?"(controller: cName, action: "delete", namespace: 'api')
                    get "/${cName}(.$format)?"(controller: cName, action: "index", namespace: 'api')
                    get "/${cName}/$id(.$format)?"(controller: cName, action: "get", namespace: 'api')
                    get "/${cName}/list(.$format)?"(controller: cName, action: "listGet", namespace: 'api')
                    post "/${cName}/list(.$format)?"(controller: cName, action: "listPost", namespace: 'api')
                    post "/${cName}(.$format)?"(controller: cName, action: "post", namespace: 'api')
                    put "/${cName}/$id(.$format)?"(controller: cName, action: "put", namespace: 'api')
                    patch "/${cName}/$id(.$format)?"(controller: cName, action: "put", namespace: 'api')
                }
            }
        }

        // group("/api") {
        //     delete "/$controller/$id(.$format)?"(action: "delete")
        //     get "/$controller(.$format)?"(action: "index")
        //     get "/$controller/$id(.$format)?"(action: "show")
        //     post "/$controller(.$format)?"(action: "save")
        //     put "/$controller/$id(.$format)?"(action: "update")
        //     patch "/$controller/$id(.$format)?"(action: "patch")
        // }

        "/schema/$id?(.$format)?"(controller: "schema", action: "index")

        "/$controller/$action?/$id?(.$format)?" {
            constraints {
                // apply constraints here
            }
        }

        "/"(view: "/index")
        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
