/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.restapi

import groovy.transform.CompileDynamic

@CompileDynamic
class BootStrap {

    Closure init = { servletContext ->
    }
    Closure destroy = {
    }
}
