package com.embabel.prepper.agent;

import org.springframework.data.repository.ListCrudRepository;

interface ContactRepository extends ListCrudRepository<Domain.Contact, Long> {

}
