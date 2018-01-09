package restify

class ShipAddress {
    String city
    Long testId
    static constraints = {
        testId nullable: true
    }
}

class Location {
    String city
    Long testId
    static constraints = {
        testId nullable: true
    }
}
