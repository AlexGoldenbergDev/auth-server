package com.goldenebrg.authserver.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FragmentsController {

    @GetMapping("/auth-fragments")
    public String getAuthFragments() {
        return "auth-fragments";
    }

    @GetMapping("/assignments-fragments")
    public String getAssignmentsFragments() {
        return "assignments-fragments";
    }
}
