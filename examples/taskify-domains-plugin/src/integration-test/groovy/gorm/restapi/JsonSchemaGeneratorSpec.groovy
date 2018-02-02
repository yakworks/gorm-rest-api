package gorm.restapi

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import taskify.Task

@Integration
@Rollback
class JsonSchemaGeneratorSpec extends Specification {

    @Autowired
    JsonSchemaGenerator jsonSchemaGenerator

    def "test fail"() {
        given:
        Map schema = jsonSchemaGenerator.generate(Task)

        expect:
        schema != null
        schema['$schema'] == "http://json-schema.org/schema#"
        schema['$id'] == "http://localhost:8080/schema/Task.json"
        //schema.description == "This is a task"
        schema.type == "Object"
        //schema.required.size() == 10
        //schema.required.containsAll(["name", "project", "note", "dueDate", "reminderEmail", "estimatedHours", "estimatedCost", "progressPct", "roleVisibility", "flex"])

        //verify properties
        schema.properties != null
        schema.properties.size() == 16 //12 props, + id/version/dateCreated/lastUpdated

        schema.properties.id != null
        schema.properties.id.type == "integer"
        schema.properties.id.readOnly == true

        schema.properties.version != null
        schema.properties.version.type == "integer"
        schema.properties.version.readOnly == true

        schema.properties.name != null
        schema.properties.name.type == "string"
        schema.properties.name.description == "The task summary/description"
        schema.properties.name.example == "Design App"
        schema.properties.name.maxLength == 100

        //verify enum property
        schema.properties.type != null
        schema.properties.type.type == "string"
        schema.properties.type.enum.size() == 5
        (schema.properties.type.enum as List).containsAll(["Todo", "Call", "Meeting", "Review", "Development"])
        //schema.properties.type.required == null
        //schema.properties.type.default == "Todo"

        //associations
        schema.properties.project != null
        //schema.properties.project['$ref'] == "Project.json"

        schema.properties.flex != null
        //schema.properties.flex['$ref'] == "#/definitions/TaskFlex"

        //verify definitions
        schema.definitions != null
        schema.definitions.size() == 1
        schema.definitions.TaskFlex != null
        schema.definitions.TaskFlex.type == "Object"

    }
}
