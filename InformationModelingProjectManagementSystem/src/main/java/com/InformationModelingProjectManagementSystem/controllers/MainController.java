package com.InformationModelingProjectManagementSystem.controllers;

import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
        public String startPage(Model model) {
        return "redirect:/login";
    }

    @GetMapping("/login")
        public String loginPage() {
        return "main/login";
    }
    
}