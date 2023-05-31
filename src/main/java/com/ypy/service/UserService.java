package com.ypy.service;


import Spring.Autowired;
import Spring.Component;
import Spring.Scope;

@Component
@Scope("prototype")//多例bean
public class UserService {
    @Autowired
    private OrderService orderService;

    public void getAutowired(){
        System.out.println(orderService);
        System.out.println(orderService.test());
    }
}
