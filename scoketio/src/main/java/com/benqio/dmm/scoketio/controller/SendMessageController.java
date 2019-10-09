package com.benqio.dmm.scoketio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;



@Controller
public class SendMessageController {
    @RequestMapping("/test")
    public String test(){
        return "UserClient01";
    }

    @RequestMapping("/test2")
    public String test2(){
        return "UserClient02";
    }

    @RequestMapping("/test3")
    public String test3(){
        return "UserClient03";
    }

}
