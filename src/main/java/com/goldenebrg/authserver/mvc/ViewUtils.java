package com.goldenebrg.authserver.mvc;

import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

public class ViewUtils {

    static ModelAndView getErrorPage(HttpStatus status, String message) {
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("status", status.value());
        modelAndView.addObject("error", status.getReasonPhrase());
        Optional.ofNullable(message).ifPresent(m -> modelAndView.addObject("message", message));
        return modelAndView;
    }
}
