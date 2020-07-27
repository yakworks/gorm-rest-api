package restify

class ShipAddress {
    String city
    Long testId
    static constraints = {
        city nullable: false
    }
}

class Location {
    String city
    Long testId
    static constraints = {
        city nullable: false
    }
}
