package com.example.demo;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class Controller {

    @GetMapping("/")//Basic route for testing stuff
    public String home(){
        return "Hello World";
    }

    //From here on will be routes that may be able to be used in theory,
    //still trying to figure out where the API calls are in the front end

    @GetMapping("/word_def/?")
    public String word_def(String word) {
        return word + ": def";
    }

    //Honestly, going to have to mess with this one a bunch
}