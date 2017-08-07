package gorm.restapi

import grails.util.GrailsClassUtils

class UrlMappings {

    static mappings = {

        for(controller in getGrailsApplication().controllerClasses) {
            println "controler $controller.fullName"
            String cName = controller.logicalPropertyName
            String namespace = GrailsClassUtils.getStaticPropertyValue(controller.clazz, 'namespace')
            println "controler $cName with namespace $namespace"

            if(namespace == 'api') {
                group("/api") {
                    "/${cName}/schema" (controller: "schema", action:"index"){
                        id = cName
                    }

                    delete "/${cName}/$id(.$format)?"(controller: cName, action: "delete", namespace:'api')
                    get "/${cName}(.$format)?"(controller: cName, action: "index", namespace:'api')
                    get "/${cName}/$id(.$format)?"(controller: cName, action: "show", namespace:'api')
                    post "/${cName}(.$format)?"(controller: cName, action: "save", namespace:'api')
                    put "/${cName}/$id(.$format)?"(controller: cName,action: "update", namespace:'api')
                    patch "/${cName}/$id(.$format)?"(controller: cName, action: "patch", namespace:'api')
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

        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
