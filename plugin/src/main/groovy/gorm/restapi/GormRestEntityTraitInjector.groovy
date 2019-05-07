/* Copyright 2018. 9ci Inc. Licensed under the Apache License, Version 2.0 */
package gorm.restapi

import grails.compiler.traits.TraitInjector
import groovy.transform.CompileStatic
import org.grails.core.artefact.DomainClassArtefactHandler

@CompileStatic
class GormRestEntityTraitInjector implements TraitInjector {

    @Override
    Class getTrait() {
        GormRestEntity
    }

    @Override
    String[] getArtefactTypes() {
        [DomainClassArtefactHandler.TYPE] as String[]
    }
}
