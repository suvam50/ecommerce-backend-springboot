package com.ecommerce.project.controller;

import com.ecommerce.project.payload.*;
import com.ecommerce.project.service.*;
import com.ecommerce.project.util.*;
import lombok.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
//@CrossOrigin(origins = "*", allowedHeaders = "*")
//@Produces(MediaType.APPLICATION_JSON_VALUE)
//@Consumes(MediaType.APPLICATION_JSON_VALUE)
//@Api(value = "Order Management API", tags = {"Order Management"})
public class OrderController {

    @Autowired
    AuthUtil authUtil;
    @Autowired
    OrderService orderService;

    @PostMapping("/order/users/payments/{paymentMethod}")
    public ResponseEntity<?>orderProducts(@PathVariable String paymentMethod, @RequestBody OrderRequestDTO orderRequestDTO){
        String userEmailId=authUtil.loggedInEmail();
        //System.out.println(orderRequestDTO.getPgResponseMessage());
        OrderDTO order = orderService.placeOrder(
                userEmailId,
                orderRequestDTO.getAddressId(),
                paymentMethod,
                orderRequestDTO.getPgName(),
                orderRequestDTO.getPgPaymentId(),
                orderRequestDTO.getPgStatus(),
                orderRequestDTO.getPgResponseMessage()
        );

        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }
}
