package com.ypy.service;

import Spring.SpringApplicationContenxt;

import java.net.URISyntaxException;

public class Test {
    public static void main(String[] args) throws URISyntaxException {
        SpringApplicationContenxt contenxt = new SpringApplicationContenxt(AppConfig.class);

        UserService userService = (UserService) contenxt.getBean("userService");
        userService.getAutowired();
//        System.out.println(userService);

    }
}
