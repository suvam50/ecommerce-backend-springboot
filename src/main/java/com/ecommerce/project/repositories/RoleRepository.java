package com.ecommerce.project.repositories;

import com.ecommerce.project.model.*;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(AppRole appRole);
}
