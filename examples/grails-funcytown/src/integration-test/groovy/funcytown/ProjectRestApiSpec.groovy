package funcytown

import gorm.restapi.testing.RestApiFuncSpec
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration

@Integration(applicationClass = Application)
@Rollback
class ProjectRestApiSpec extends RestApiFuncSpec {

    String getResourcePath() {
        "${baseUrl}api/project"
    }

    //FIXME the following should not be needed as we know everythign we need to generate this from constraints
    Map getInsertData() { [name: "project", num: "x123"] }

    Map getUpdateData() { [name: "project Update", num: "x123u"] }

    Map getInvalidData() { ["name": null] }

    @Override
    def cleanup() {}
}
