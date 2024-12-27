package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.*;
import com.ecommerce.project.model.*;
import com.ecommerce.project.payload.*;
import com.ecommerce.project.repositories.*;
import com.ecommerce.project.util.*;
import org.modelmapper.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import java.util.*;
import java.util.stream.*;

@Service
public class CartServiceImpl implements CartService{

    @Autowired
    ProductRepository productRepository;
    @Autowired
    CartItemRepository cartItemRepository;
    @Autowired
    CartRepository cartRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    AuthUtil authUtil;


    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {

        Cart cart = createCart();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product","productId",productId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cart.getCartId(), productId);

        if(cartItem!= null)
        {
            throw new APIException("Product " + product.getProductName() + " already exists in the cart");
        }

        if(product.getQuantity()==0){
            throw new APIException(product.getProductName() + " is not available");
        }

        if (product.getQuantity() < quantity) {
            throw new APIException("Please, make an order of the " + product.getProductName()
                    + " less than or equal to the quantity " + product.getQuantity() + ".");
        }

        CartItem newCartItem = new CartItem();
        newCartItem.setCart(cart);
        newCartItem.setProduct(product);
        //newCartItem.setCart(cart);
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setProductPrice(product.getSpecialPrice());
        cartItemRepository.save(newCartItem);

        product.setQuantity(product.getQuantity());
        cart.setTotalPrice(cart.getTotalPrice()+(product.getSpecialPrice()*quantity));
        cart.getCartItems().add(newCartItem);
        cartRepository.save(cart);

        CartDTO cartDTO = modelMapper.map(cart,CartDTO.class);

        List<CartItem> cartItems = cart.getCartItems();
        //System.out.println("CartItems: " + cartItems.size());
        Stream<ProductDTO> productStream=cartItems.stream().map(item->{
            ProductDTO map = modelMapper.map(item.getProduct(),ProductDTO.class);
            map.setQuantity(item.getQuantity());
            return map;
        });

        cartDTO.setProducts(productStream.toList());

        return cartDTO;
    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();

        if (carts.size() == 0) {
            throw new APIException("No cart exists");
        }

        List<CartDTO> cartDTOs = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

            List<ProductDTO> products = cart.getCartItems().stream()
                    .map(p -> {
                        ProductDTO productDTO=
                        modelMapper.map(p.getProduct(), ProductDTO.class);
                        productDTO.setQuantity(p.getQuantity());
                        return productDTO;})
                    .collect(Collectors.toList());

            cartDTO.setProducts(products);

            return cartDTO;

        }).collect(Collectors.toList());

        return cartDTOs;
    }

    @Override
    public CartDTO getCart(String emailId, Long cartId) {
        Cart cart = cartRepository.findCartByEmailAndCartId(emailId, cartId);
        if (cart == null){
            throw new ResourceNotFoundException("Cart", "cartId", cartId);
        }
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        cart.getCartItems().forEach(c ->
                c.getProduct().setQuantity(c.getQuantity()));
        List<ProductDTO> products = cart.getCartItems().stream()
                .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class))
                .toList();
        cartDTO.setProducts(products);
        return cartDTO;
    }

    @Transactional
    @Override
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {
        String email=authUtil.loggedInEmail();
        Cart userCart = cartRepository.findCartByEmail(email);
        Long cartId=userCart.getCartId();

        Cart cart=cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));
        Product product=productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        if (product.getQuantity() == 0) {
            throw new APIException(product.getProductName() + " is not available");
        }

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem == null) {
            throw new APIException("Product " + product.getProductName() + " not available in the cart!!!");
        }
        Integer newQuantity=cartItem.getQuantity()+quantity;
        System.out.println("Old Quantity: " + cartItem.getQuantity());
        System.out.println("newQuantity: " + newQuantity);
        if (product.getQuantity() < newQuantity) {
            throw new APIException("Please, make an order of the " + product.getProductName()
                    + " less than or equal to the quantity " + product.getQuantity() + ".");
        }
        if (newQuantity < 0) {
            throw new APIException("The resulting quantity cannot be negative.");
        }
        if(newQuantity==0){
            deleteProductFromCart(cartId,productId);

        }
        else {
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setDiscount(product.getDiscount());
            cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * quantity));
            cartRepository.save(cart);
            cartItemRepository.save(cartItem);
        }
//
//        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
//
//        List<CartItem> cartItems = cart.getCartItems();
//
//        Stream<ProductDTO> productStream = cartItems.stream().map(item -> {
//            ProductDTO prd = modelMapper.map(item.getProduct(), ProductDTO.class);
//            prd.setQuantity(item.getQuantity());
//            return prd;
//        });
//
//
//        cartDTO.setProducts(productStream.toList());
//
//        return cartDTO;
//        String emailId = authUtil.loggedInEmail();
//        Cart userCart = cartRepository.findCartByEmail(emailId);
//        Long cartId  = userCart.getCartId();
//
//        Cart cart = cartRepository.findById(cartId)
//                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));
//
//        Product product = productRepository.findById(productId)
//                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
//
//        if (product.getQuantity() == 0) {
//            throw new APIException(product.getProductName() + " is not available");
//        }
//
//        if (product.getQuantity() < quantity) {
//            throw new APIException("Please, make an order of the " + product.getProductName()
//                    + " less than or equal to the quantity " + product.getQuantity() + ".");
//        }
//
//        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);
//
//        if (cartItem == null) {
//            throw new APIException("Product " + product.getProductName() + " not available in the cart!!!");
//        }
//
//        // Calculate new quantity
//        int newQuantity = cartItem.getQuantity() + quantity;
//
//        // Validation to prevent negative quantities
//        if (newQuantity < 0) {
//            throw new APIException("The resulting quantity cannot be negative.");
//        }
//
//        if (newQuantity == 0){
//            deleteProductFromCart(cartId, productId);
//        } else {
//            cartItem.setProductPrice(product.getSpecialPrice());
//            cartItem.setQuantity(cartItem.getQuantity() + quantity);
//            cartItem.setDiscount(product.getDiscount());
//            cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * quantity));
//            cartRepository.save(cart);
//        }

//        CartItem updatedItem = cartItemRepository.save(cartItem);
//        if(updatedItem.getQuantity() == 0){
//            cartItemRepository.deleteById(updatedItem.getCartItemId());
//        }


        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<CartItem> cartItems = cart.getCartItems();

        Stream<ProductDTO> productStream = cartItems.stream().map(item -> {
            ProductDTO prd = modelMapper.map(item.getProduct(), ProductDTO.class);
            prd.setQuantity(item.getQuantity());
            return prd;
        });


        cartDTO.setProducts(productStream.toList());

        return cartDTO;
    }


    private Cart createCart() {
        Cart userCart  = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if(userCart != null){
            return userCart;
        }

        Cart cart = new Cart();
        cart.setTotalPrice(0.00);
        cart.setUser(authUtil.loggedInUser());
        Cart newCart =  cartRepository.save(cart);

        return newCart;
    }


    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem == null) {
            throw new ResourceNotFoundException("Product", "productId", productId);
        }

        cart.setTotalPrice(cart.getTotalPrice() -
                (cartItem.getProductPrice() * cartItem.getQuantity()));

        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId, productId);
        return "Product " + cartItem.getProduct().getProductName() + " removed from the cart !!!";
    }

    @Override
    public void updateProductInCarts(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        System.out.println("ProductPrice: " + product.getSpecialPrice());
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem == null) {
            throw new APIException("Product " + product.getProductName() + " not available in the cart!!!");
        }

//        System.out.println("ProductPrice: " + cartItem.getProductPrice());
//        System.out.println("CartQuantity: " + cartItem.getQuantity());
//        System.out.println("CartPreviousPrice: " + cart.getTotalPrice());
        double cartPrice = cart.getTotalPrice()
                - (cartItem.getProductPrice() * cartItem.getQuantity());
//        System.out.println("CartPrice: " + cartPrice);
        cartItem.setProductPrice(product.getSpecialPrice());
//        System.out.println("ProductPrice1: " + cartItem.getProductPrice());
//        System.out.println("CartQuantity1: " + cartItem.getQuantity());
//        System.out.println("CartPreviousPrice1: " + cart.getTotalPrice());
        cart.setTotalPrice(cartPrice
                + (cartItem.getProductPrice() * cartItem.getQuantity()));
//        System.out.println("CartPrice1: " + cartPrice);
        cartRepository.save(cart);
        cartItemRepository.save(cartItem);
    }
}
