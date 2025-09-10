package com.boot.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppController {

    @GetMapping("/demo/users/export")
    public String exportUsers(HttpServletResponse response) {
        return "ok";
    }

}
