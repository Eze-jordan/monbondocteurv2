package com.esiitech.monbondocteurv2.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/V2/test")
public class TestController {

    @GetMapping
    public String hello() {
        return "Hello Swagger!";
    }
}