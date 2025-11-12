package com.embabel.prepper.agent;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContactRepository extends ListCrudRepository<Domain.Contact, Long> {

    Optional<Domain.Contact> findByEmail(String email);

    boolean existsByEmail(String email);
}
