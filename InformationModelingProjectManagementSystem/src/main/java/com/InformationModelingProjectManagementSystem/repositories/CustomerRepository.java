package com.InformationModelingProjectManagementSystem.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.InformationModelingProjectManagementSystem.models.Customer;

@Repository
public interface CustomerRepository extends CrudRepository<Customer, Integer> {
    
}
