package com.ypy.service;


import Spring.*;

@Component
@Scope("prototype")//多例bean
public class UserService implements UserInterface{
    @Autowired
    private OrderService orderService;  //先byType在ByNAME

    public void getAutowired(){
//        System.out.println(orderService.test());
    }


}
