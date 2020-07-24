/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.restapi

import groovy.transform.CompileStatic

import org.grails.core.artefact.DomainClassArtefactHandler

import grails.compiler.traits.TraitInjector

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
