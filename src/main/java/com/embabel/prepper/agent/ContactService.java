package com.embabel.prepper.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ContactService {

    private final Logger logger = LoggerFactory.getLogger(ContactService.class);
    private final ResearchedParticipantRepository repository;

    public ContactService(ResearchedParticipantRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<Domain.Contact> resolveContact(String identification) {
        // TODO real implementation might use others means of identification,
        // including full text search, phone number, etc,
        // but we rely on email
        var found = repository.findByEmail(identification);
        logger.info("Resolved contact for {}: {}", identification, found);
        return found;
    }

    @Transactional
    public Domain.Contact createContact(Domain.NewContact newContact) {
        var saved = repository.save(new Domain.Contact(newContact));
        logger.info("Created new contact: {}", saved);
        return saved;
    }
}
