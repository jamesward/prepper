package com.embabel.prepper.repository;

import com.embabel.prepper.agent.Domain;
import com.embabel.prepper.agent.ResearchedParticipantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ContactRepositoryTest {

    @Autowired
    private ResearchedParticipantRepository repository;

    @Test
    void shouldSaveAndFindResearchedParticipant() {
        Domain.Contact participant = new Domain.Contact(
                new Domain.NewContact("Test", "Participant", "Tester")
        );
        participant.setEmail("test@example.com");
        participant.setWriteup("Test participant writeup");

        Domain.Contact saved = repository.save(participant);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("test@example.com");
        assertThat(saved.getWriteup()).isEqualTo("Test participant writeup");
    }

    @Test
    void shouldFindByEmail() {
        Domain.Contact participant = new Domain.Contact(new Domain.NewContact("x", "y", ""));
        participant.setEmail("find@example.com");
        participant.setWriteup("Findable participant");
        repository.save(participant);

        assertThat(repository.findByEmail("find@example.com")).isPresent();
        assertThat(repository.findByEmail("notfound@example.com")).isEmpty();
    }

    @Test
    void shouldCheckExistsByEmail() {
        Domain.Contact participant = new Domain.Contact(
                new Domain.NewContact("Exists", "Participant", "Tester")
        );
        participant.setEmail("exists@example.com");
        participant.setWriteup("Existing participant");
        repository.save(participant);

        assertThat(repository.existsByEmail("exists@example.com")).isTrue();
        assertThat(repository.existsByEmail("notexists@example.com")).isFalse();
    }
}