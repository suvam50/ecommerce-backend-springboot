package com.ecommerce.project.repositories;

import com.ecommerce.project.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

import java.util.*;
@Repository
public interface AddressRepository extends JpaRepository<Address,Long> {

    @Query("SELECT c FROM Address c WHERE c.user.userId = ?1")
   // @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = ?1 AND ci.product.id = ?2")
    List<Address> findAddressByUserId(Long userId);
}