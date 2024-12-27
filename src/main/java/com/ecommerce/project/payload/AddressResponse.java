package com.ecommerce.project.payload;

import lombok.*;

import java.util.*;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressResponse {
    private List<AddressDTO>response;
}
