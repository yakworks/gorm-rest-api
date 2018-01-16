package gorm.restapi.controller

import gorm.tools.repository.errors.EntityNotFoundException
import gorm.tools.repository.errors.EntityValidationException
import grails.validation.ValidationException

/**
 *  Adds controller error handlers
 *
 *  Created by alexeyzvegintcev.
 */
trait RestControllerLogging {


    def logMessageError(String message){
        log.info message
    }

}