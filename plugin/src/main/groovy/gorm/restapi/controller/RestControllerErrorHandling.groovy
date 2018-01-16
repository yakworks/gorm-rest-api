package gorm.restapi.controller

import gorm.tools.repository.errors.EntityNotFoundException
import gorm.tools.repository.errors.EntityValidationException
import grails.validation.ValidationException
import org.springframework.dao.OptimisticLockingFailureException

import static org.springframework.http.HttpStatus.*

/**
 *  Adds controller error handlers
 *
 *  Created by alexeyzvegintcev.
 */
trait RestControllerErrorHandling extends RestControllerLogging {


    def handleException(EntityNotFoundException e) {
        render(status: NOT_FOUND, e.message)
    }

    def handleException(EntityValidationException e){
        render(status: UNPROCESSABLE_ENTITY, e.message)
    }

    def handleException(ValidationException e){
        render(status: UNPROCESSABLE_ENTITY, e.fullMessage)
    }

    def handleException(OptimisticLockingFailureException e){
        render(status: CONFLICT, e.message)
    }

    def handleException(RuntimeException e){
        handleException(e)
    }
}