package gorm.restapi

import grails.converters.JSON
import gorm.restapi.appinfo.AppInfoBuilder

//see http://plugins.grails.org/plugin/grails/spring-security-appinfo
// for app that shows app-info
@SuppressWarnings(['NoDef'])
class AppInfoController {

      //injected
      AppInfoBuilder appInfoBuilder

      def meta() {
          render grailsApplication.metadata as JSON
      }

      def urlMappings() {
          render appInfoBuilder.urlMappings() as JSON
      }

      def memoryInfo() {
          render appInfoBuilder.memoryInfo() as JSON
      }

      def beanInfo() {
          render appInfoBuilder.beanInfo() as JSON
      }

  }