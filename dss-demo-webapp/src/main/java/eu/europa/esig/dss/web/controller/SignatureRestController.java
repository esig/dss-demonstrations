package eu.europa.esig.dss.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class SignatureRestController {

    private static final String[] ALLOWED_FIELDS = { }; // nothing

    @InitBinder
    public void setAllowedFields(WebDataBinder webDataBinder) {
        webDataBinder.setAllowedFields(ALLOWED_FIELDS);
    }

    @RequestMapping(value = "/signature-rest", method = RequestMethod.GET)
    public String getRestInfo() {
        return "signature-rest";
    }

}
