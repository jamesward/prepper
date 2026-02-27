package com.embabel.prepper.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ContactService {

	private final Logger logger = LoggerFactory.getLogger(ContactService.class);

	private final ContactRepository repository;

	private final VectorStore vectorStore;

	public ContactService(ContactRepository repository, VectorStore vectorStore) {
		this.repository = repository;
		this.vectorStore = vectorStore;
	}

	@Transactional(readOnly = true)
	public Optional<Domain.Contact> resolveContact(String identification) {
		var found = vectorStore.similaritySearch(identification)
			.stream()
			.findFirst()
			.flatMap(document -> repository.findById((Long) document.getMetadata().get("id")));
		logger.info("Resolved contact for {}: {}", identification, found);
		return found;
	}

	@Transactional(readOnly = true)
	public List<Domain.Contact> findAll() {
		return repository.findAll();
	}

	@Transactional
	public Domain.Contact createContact(Domain.NewContact newContact) {
		var saved = repository.save(new Domain.Contact(newContact));
		logger.info("Created new contact: {}", saved);
		var contactDocument = new Document("%s <%s>".formatted(saved.name(), saved.email()), Map.of("id", saved.id()));
		vectorStore.add(List.of(contactDocument));
		return saved;
	}

}
