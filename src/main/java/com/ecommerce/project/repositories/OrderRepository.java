package com.ecommerce.project.repositories;

import com.ecommerce.project.model.*;
import org.springframework.data.jpa.repository.*;

public interface OrderRepository extends JpaRepository<Order,Long> {
}
