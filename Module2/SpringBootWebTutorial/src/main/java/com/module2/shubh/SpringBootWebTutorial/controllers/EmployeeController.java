package com.module2.shubh.SpringBootWebTutorial.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// The annotation below makes sure that mappings defined are actually REST in nature
// Rest Controller uses @Controller & @ResponseBody annotations behind the scenes
// making sure all methods defined within the controller automatically return JSON/XML responses
// directly to the response body.
@RestController
public class EmployeeController {
//    Component Scan ensures that whenever this resource endpoint is called with a get request
//    then the appropriate controller method is run to respond back to the client
    @GetMapping(path = "/getSecretMessage")
    public String getMySuperSecretMessage(){
        return "Secret Message:fshdjghs@sejnh";
    }
}

