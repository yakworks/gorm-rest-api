/* Copyright 2018. 9ci Inc. Licensed under the Apache License, Version 2.0 */
package gorm.restapi

import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEntity

@CompileStatic
trait GormRestEntity<D extends GormEntity<D>> {


    static List<String> getShowFields() {
        ["*"]
    }

    static List<String> getListFields() {
        ["*"]
    }

}
