package com.project.first;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/test-db")
    public String test(){
        return "MySQL OK!";
    }
}
