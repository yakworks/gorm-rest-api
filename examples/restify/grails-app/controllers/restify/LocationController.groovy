package restify

import gorm.restapi.controller.RestApiRepoController

import static org.springframework.http.HttpStatus.CREATED

class LocationController extends RestApiRepoController<Location> {

    LocationController() {
        super(Location, false)
    }

    def post() {
        Map q = getDataMap()
        q.city = q.city == null ? null : "foo"
        Location instance = getRepo().create(q)
        respond instance, [status: CREATED] //201
    }

}
