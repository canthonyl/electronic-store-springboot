package com.electronicstore.springboot.controller.handler;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ControllerResponse {

    private Map<String, List<String>> errors = new LinkedHashMap<>();

    public Map<String, List<String>> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, List<String>> errors) {
        this.errors = errors;
    }
}
