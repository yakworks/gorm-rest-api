package restify

import gorm.restapi.controller.RestApiRepoController
import taskify.Project

import static org.springframework.http.HttpStatus.CREATED

class ProjectController extends RestApiRepoController<Project> {

    ProjectController() {
        super(Project, false)
    }

    def post() {
        Map q = getDataMap()
        println "q $q"
        String comments = q.comments ?: ""
        q.comments = "$comments - post was here"
        //q.num = q.num == null ? null : "foo"
        Project instance = getRepo().create(q)
        respond instance, [status: CREATED] //201
    }

}
