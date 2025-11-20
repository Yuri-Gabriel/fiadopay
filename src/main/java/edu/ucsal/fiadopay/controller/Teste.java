package edu.ucsal.fiadopay.controller;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.ucsal.fiadopay.annotation.logs.Logger;

@RestController
@RequestMapping("/teste")
public class Teste {
    @Logger
    @GetMapping()
    public String teste() {
        return "LOG PATH => " + System.getProperty("user.dir") + "/logs/" + LocalDateTime.now();
    } 
}
