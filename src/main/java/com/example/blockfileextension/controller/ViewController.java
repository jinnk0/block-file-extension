package com.example.blockfileextension.controller;

import com.example.blockfileextension.service.FileExtensionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    private final FileExtensionService fileExtensionService;

    public ViewController(FileExtensionService fileExtensionService) {
        this.fileExtensionService = fileExtensionService;
    }

    @GetMapping("/home")
    public String showHome(Model model) {
        model.addAttribute("extensions", fileExtensionService.getExtensions());
        return "home";
    }
}

