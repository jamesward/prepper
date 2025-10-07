package com.embabel.prepper.agent

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ResearchedParticipantRepository : JpaRepository<Domain.Contact, Long> {
    fun findByEmail(email: String): Optional<Domain.Contact>
    fun existsByEmail(email: String): Boolean
}
