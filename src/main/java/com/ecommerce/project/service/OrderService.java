package com.ecommerce.project.service;

import com.ecommerce.project.payload.*;

public interface OrderService {
    OrderDTO placeOrder(String userEmailId, Long addressId, String paymentMethod, String pgName, String pgPaymentId, String pgStatus, String pgResponseMessage);
}
