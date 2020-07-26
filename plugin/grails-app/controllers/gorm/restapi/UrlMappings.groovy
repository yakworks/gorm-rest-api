/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.restapi

import groovy.transform.CompileDynamic

import grails.util.GrailsClassUtils

@CompileDynamic
class UrlMappings {

    static mappings = {

        for (controller in getGrailsApplication().controllerClasses) {
            //println "controler $cName.fullName"
            String cName = controller.logicalPropertyName
            String namespace = GrailsClassUtils.getStaticPropertyValue(controller.clazz, 'namespace')
            //println "controller $cName with namespace $namespace"

            if (namespace) {
                group("/$namespace") {
                    "/${cName}/schema"(controller: "schema", action: "index") {
                        id = cName
                    }
                    delete "/$cName/$id(.$format)?" (controller: cName, action: "delete", namespace: 'api')
                    get "/$cName(.$format)?"(controller: cName, action: "index", namespace: 'api')
                    get "/$cName/$id(.$format)?"(controller: cName, action: "get", namespace: 'api') {
                        constraints {
                            id(validator: {
                                return it.isLong()
                            })
                        }
                    }
                    get "/${cName}/list(.$format)?"(controller: cName, action: "listGet", namespace: 'api')
                    post "/${cName}/list(.$format)?"(controller: cName, action: "listPost", namespace: 'api')
                    post "/$cName(.$format)?" (controller: cName, action: "post", namespace: 'api')
                    post "/$cName/$action?"(controller: cName, namespace: 'api')
                    put "/$cName/$id" (controller: cName, action: "put", namespace: 'api')
                    patch "/$cName/$id"(controller: cName, action: "put", namespace: 'api')
                    get "/$cName/$action/"(controller: cName, namespace: 'api')
                    get "/$cName/$action/$id"(controller: cName, namespace: 'api')
                    //when a post is called allows an action
                   /* post "/${cName}/$action(.$format)?"(controller: cName, namespace: 'api')
                    //or
                    post "/${cName}/actions/$action(.$format)?"(controller: cName, namespace: 'api')

                    delete "/${cName}/$id(.$format)?"
                    get "/${cName}(.$format)?"(controller: cName, action: "index", namespace: 'api')
                    get "/${cName}/$id(.$format)?"(controller: cName, action: "get", namespace: 'api')
                    get "/${cName}/list(.$format)?"(controller: cName, action: "listGet", namespace: 'api')
                    post "/${cName}/list(.$format)?"(controller: cName, action: "listPost", namespace: 'api')
                    post "/${cName}(.$format)?"(controller: cName, action: "post", namespace: 'api')
                    put "/${cName}/$id(.$format)?"(controller: cName, action: "put", namespace: 'api')
                    patch "/${cName}/$id(.$format)?"(controller: cName, action: "put", namespace: 'api')*/
                }
            }
        }

        // group("/api") {
        //     delete "/$cName/$id(.$format)?"(action: "delete")
        //     get "/$cName(.$format)?"(action: "index")
        //     get "/$cName/$id(.$format)?"(action: "show")
        //     post "/$cName(.$format)?"(action: "save")
        //     put "/$cName/$id(.$format)?"(action: "update")
        //     patch "/$cName/$id(.$format)?"(action: "patch")
        // }

        "/schema/$id?(.$format)?"(controller: "schema", action: "index")

      /*  "/$cName/$action?/$id?(.$format)?" {
            constraints {
                // apply constraints here
            }
        }*/

        "/"(view: "/index")
        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
