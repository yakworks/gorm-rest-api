package gorm.restapi.testing

import grails.plugins.rest.client.RestBuilder


//@CompileStatic
trait RestApiTestTrait {

    //private static GrailsApplication _grailsApplication
    //private static Object _servletContext

    RestBuilder getRestBuilder() {
        new RestBuilder()
    }

    // String getResourcePath() {
    //     "${baseUrl}/api/project"
    // }

    /**
     * Loosely test 2 maps for equality
     * asserts more or less that main:[a: 1, b: 2, c: 3] == subset:[a: 1, b: 2]
     *
     * @param subset the full map
     * @param full the full map
     * http://csierra.github.io/posts/2013/02/12/loosely-test-for-map-equality-using-groovy/
     */
    boolean subsetEquals(Map subset, Map full) {
        if (!full.keySet().containsAll(subset.keySet())) return false
        subset.every { it.value == full[it.key] }
    }

}