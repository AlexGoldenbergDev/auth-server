package com.goldenebrg.authserver.services.mvc;

import com.goldenebrg.authserver.services.FacadeService;
import com.goldenebrg.authserver.services.config.AbstractServiceField;
import com.goldenebrg.authserver.services.config.ServiceInputField;
import com.goldenebrg.authserver.services.config.ServiceSelectionListField;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.stream.Collectors;

final class MvcControllerUtils {

    private static final String SPRING_SECURITY_CONTEXT = "SSC";


    static void doAutoLogin(AuthenticationManager authenticationManager, String username, String password, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(username, password);
        Authentication auth = authenticationManager.authenticate(authReq);
        SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(auth);
        HttpSession session = request.getSession(true);
        session.setAttribute(SPRING_SECURITY_CONTEXT, sc);
    }


    public static <T extends AbstractServiceField> Map<String, T> filterFieldsByRole(Map<String, T> map, String role) {
        return map.entrySet().stream()
                .filter(entry -> entry.getValue().getChangers().contains(role))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }


    static void addInputFields(FacadeService facadeService, ModelAndView modelAndView, String service, String role) {

        Map<String, ServiceSelectionListField> selectionFields = facadeService.getServicesSelectionListFieldsMap(service);
        Map<String, ServiceInputField> inputFields = facadeService.getServicesInputFieldsMap(service);

        Map<String, ServiceSelectionListField> selectionFieldsFlt = filterFieldsByRole(selectionFields, role);
        Map<String, ServiceInputField> inputFieldsFlt = filterFieldsByRole(inputFields, role);

        modelAndView.addObject("selectionListFieldsMap", selectionFieldsFlt);
        modelAndView.addObject("inputFieldsMap", inputFieldsFlt);


    }
}
