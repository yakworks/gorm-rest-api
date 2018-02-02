package gorm.restapi.controller

import gorm.tools.repository.RepoMessage
import gorm.tools.repository.errors.EntityNotFoundException
import gorm.tools.repository.errors.EntityValidationException
import grails.validation.ValidationException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.validation.Errors
import org.springframework.validation.FieldError

import static org.springframework.http.HttpStatus.*

/**
 *  Adds controller error handlers
 *
 *  Created by alexeyzvegintcev.
 */
trait RestControllerErrorHandling {

    void handleException(EntityNotFoundException e) {
        log.info e.message
        render(status: NOT_FOUND, e.message)
    }

    void handleException(EntityValidationException e){
        String m = buildMsg(e.messageMap, e.errors)
        log.info m
        render(status: UNPROCESSABLE_ENTITY, m)
    }

    void handleException(ValidationException e){
        String m = buildMsg([defaultMessage: e.message], e.errors)
        log.info m
        render(status: UNPROCESSABLE_ENTITY, m)
    }

    void handleException(OptimisticLockingFailureException e){
        log.info e.message
        render(status: CONFLICT, e.message)
    }

    void handleException(RuntimeException e){
        log.error e.message
        throw e
    }

    String buildMsg(Map msgMap, Errors errors) {
        StringBuilder result = new StringBuilder(msgMap.defaultMessage)
        errors.getAllErrors().each { FieldError error ->
            result.append("\n" + message(error: error, args: error.arguments, local: RepoMessage.defaultLocale()))
        }
        return result
    }
}