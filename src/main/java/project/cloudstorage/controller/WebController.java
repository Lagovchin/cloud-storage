package project.cloudstorage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping(value = {"/files/", "/login", "/registration"})
    String getHomePage() {
        return "forward:/index.html";
    }
}
