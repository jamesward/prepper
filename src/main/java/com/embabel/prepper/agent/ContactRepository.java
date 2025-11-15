package com.embabel.prepper.agent;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

interface ContactRepository extends ListCrudRepository<Domain.Contact, Long> {

}
