package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.*;
import com.ecommerce.project.model.*;
import com.ecommerce.project.payload.*;
import com.ecommerce.project.repositories.*;
import org.modelmapper.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import java.time.*;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService{

    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    CartService cartService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    CartItemRepository cartItemRepository;

    @Override
    @Transactional
    public OrderDTO placeOrder(String userEmailId, Long addressId,
                               String paymentMethod, String pgName,
                               String pgPaymentId, String pgStatus,
                               String pgResponseMessage) {
        Cart cart = cartRepository.findCartByEmail(userEmailId);
        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "email", userEmailId);
        }
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        List<CartItem>cartItems=cart.getCartItems();
        if(cartItems.isEmpty()){
            throw new APIException("Cart is empty");
        }
        Order order=new Order();
        order.setEmail(userEmailId);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(cart.getTotalPrice());
        order.setOrderStatus("Order Placed Successfully");
        order.setAddress(address);

        Payment payment = new Payment(paymentMethod,pgPaymentId,pgStatus,pgResponseMessage,pgName);
        Payment savedPayment=paymentRepository.save(payment);
        order.setPayment(savedPayment);
        Order savedOrder=orderRepository.save(order);

        List<OrderItem>orderItems=new ArrayList<>();
        for(CartItem cartItem : cartItems){
            OrderItem orderItem=new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setOrder(savedOrder);
            orderItem.setDiscount(cartItem.getDiscount());
            orderItem.setOrderedProductPrice(cartItem.getProductPrice());
            orderItems.add(orderItem);
        }
        orderItems=orderItemRepository.saveAll(orderItems);

        for(CartItem cartItem:cartItems){
            int quantity=cartItem.getQuantity();
            Product product=cartItem.getProduct();
            product.setQuantity(product.getQuantity()-quantity);
            productRepository.save(product);
            //cartItemRepository.deleteById(cartItem.getCartItemId());
            cartService.deleteProductFromCart(cart.getCartId(),product.getProductId());
        }

        OrderDTO orderDTO=modelMapper.map(savedOrder, OrderDTO.class);
        orderItems.forEach(
                item->orderDTO.getOrderItems()
                        .add(modelMapper.map(item,OrderItemDTO.class)));
        orderDTO.setAddressId(addressId);
        return orderDTO;
    }
}
