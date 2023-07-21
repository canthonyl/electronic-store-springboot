package com.electronicstore.springboot.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
public class ProductController {

    @GetMapping(path = "{id}")
    @ResponseBody
    public String getProduct(@PathVariable String id){
        return "Id = "+id;
    }

}
