package com.ypy.service;

import Spring.SpringApplicationContenxt;

import java.net.URISyntaxException;

public class Test {
    public static void main(String[] args) throws URISyntaxException {
        SpringApplicationContenxt contenxt = new SpringApplicationContenxt(AppConfig.class);

//        UserInterface userService = (UserInterface) contenxt.getBean("userService");
        OrderService orderService = (OrderService) contenxt.getBean("orderService");
        OrderService orderService1 = (OrderService) contenxt.getBean("orderService1");
        OrderService orderService2 = (OrderService) contenxt.getBean("orderService2");
        System.out.println(orderService);
        System.out.println(orderService1);
        System.out.println(orderService2);
//        userService.getAutowired();
//        System.out.println(userService);

    }
}
