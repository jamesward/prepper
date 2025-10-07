package com.embabel.prepper.agent

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ContactService(private val repository: ResearchedParticipantRepository) {

    private val logger = LoggerFactory.getLogger(ContactService::class.java)

    @Transactional(readOnly = true)
    fun resolveContact(identification: String) = repository.findByEmail(identification).also {
        logger.info("Resolved contact for {}: {}", identification, it)
    }

    @Transactional(readOnly = true)
    fun findAll(): List<Domain.Contact> = repository.findAll()

    @Transactional
    fun createContact(newContact: Domain.NewContact): Domain.Contact {
        val saved = repository.save(Domain.Contact(newContact))
        logger.info("Created new contact: {}", saved)
        return saved
    }
}
