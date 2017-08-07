package gorm.restapi.appinfo

//import grails.converters.JSON
import grails.core.DefaultGrailsApplication
import grails.core.GrailsDomainClass
import grails.core.GrailsDomainClassProperty
import grails.transaction.Transactional
import grails.util.GrailsNameUtils
import grails.validation.ConstrainedProperty
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.core.DefaultGrailsDomainClass
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.orm.hibernate.cfg.HibernateMappingContext
import org.grails.orm.hibernate.cfg.Mapping

import java.lang.management.ManagementFactory
import java.lang.management.MemoryPoolMXBean
import java.lang.management.MemoryType
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

import grails.util.GrailsClassUtils

/**
 * Misc Application Info. TODO probably move to its own plugin
 */
//@CompileStatic
class AppInfoBuilder {
    //HibernateMappingContext grailsDomainClassMappingContext
    DefaultGrailsApplication grailsApplication
    def grailsUrlMappingsHolder

    List urlMappings() {

        def urlMappings = grailsUrlMappingsHolder.urlMappings.collect { [
              name: it.mappingName?:'',
              url: it.urlData.logicalUrls.first(),
              methods: it.parameterValues,
              parameters: it.constraints.propertyName

        ] }
        return urlMappings
    }

    Map beanInfo() {

        def springInfo = new SpringInfoHelper()
        springInfo.grailsApplication = grailsApplication

        return springInfo.splitBeans()
    }

    /**
     * Collect information about memory usage. The returned map has the following info:
     * heapPoolNames: a list of the names of the heap memory pools
     * heapSectionNames: a list of the names of the heap section names
     * heapNumbers: a Map with section names as keys, and values are a list of memory values (in MB) for each heap pool section
     * nonheapPoolNames: a list of the names of the non-heap memory pools
     * nonheapSectionNames: a list of the names of the non-heap section names
     * nonheapNumbers: a Map with section names as keys, and values are a list of memory values (in MB) for each non-heap pool section
     * memoryNames: hard-coded to ['Heap'] for consistency
     * memorySectionNames: hard-coded to ['Free', 'Used'] for consistency
     * memoryNumbers: a Map with section names as keys, and values are a list of memory values (in MB) for each memory section
     * @return the info
     */
    Map<String, Object> memoryInfo() {

        def heapPoolNames = []
        def heapNumbers = [:]
        generatePoolGraphData MemoryType.HEAP, heapPoolNames, heapNumbers

        def nonheapPoolNames = []
        def nonheapNumbers = [:]
        generatePoolGraphData MemoryType.NON_HEAP, nonheapPoolNames, nonheapNumbers

        long memoryTotal = Runtime.runtime.totalMemory()
        long memoryFree = Runtime.runtime.freeMemory()
        long memoryUsed = memoryTotal - memoryFree

        def memoryNames = ['Heap']
        def memoryNumbers = ['Free': [formatMB(memoryFree)],
                                    'Used': [formatMB(memoryUsed)]]

        [heapPoolNames: heapPoolNames,
         heapSectionNames: heapNumbers.keySet(),
         heapNumbers: heapNumbers,
         nonheapPoolNames: nonheapPoolNames,
         nonheapSectionNames: nonheapNumbers.keySet(),
         nonheapNumbers: nonheapNumbers,
         memoryNames: memoryNames,
         memorySectionNames: memoryNumbers.keySet(),
         memoryNumbers: memoryNumbers]
    }

    void generatePoolGraphData(MemoryType type, poolNames, numbers) {

        numbers.Init = []
        numbers.Used = []
        numbers.Committed = []
        numbers.Max = []

        for (MemoryPoolMXBean bean : ManagementFactory.memoryPoolMXBeans) {
            if (bean.type == type) {
                numbers.Init << formatMB(bean.usage.init)
                numbers.Used << formatMB(bean.usage.used)
                numbers.Committed << formatMB(bean.usage.committed)
                numbers.Max << formatMB(bean.usage.max)
                poolNames << bean.name
            }
        }
    }

    float formatMB(long value) {
        String formatted = new DecimalFormat('.000', new DecimalFormatSymbols(Locale.ENGLISH)).format(
            value / 1024.0 / 1024.0)
        formatted.toFloat()
    }

}

