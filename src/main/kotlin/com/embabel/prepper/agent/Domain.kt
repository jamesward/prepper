package com.embabel.prepper.agent

import com.embabel.common.ai.prompt.PromptContributor
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.lang.NonNull
import org.springframework.lang.Nullable
import java.time.LocalDateTime

abstract class Domain {

    data class Meeting(
        val context: String,
        val objective: String,
        @JsonPropertyDescription("A list of participant email addresses or however else we identify them, e.g. 'Roger Daltrey The Who' or 'roger@who.com'")
        val participants: List<String>
    ) {
        @NonNull
        fun purpose(): String = "Meeting:\nContext: $context\nObjective: $objective\n"
    }

    data class NewContact(
        val name: String,
        @field:Nullable val email: String?,
        val writeup: String
    )

    @Entity
    @EntityListeners(AuditingEntityListener::class)
    open class Contact() : PromptContributor {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        open var id: Long? = null

        @Column(nullable = false)
        open lateinit var name: String

        @Column
        open var email: String? = null

        @Column(columnDefinition = "TEXT")
        open var writeup: String? = null

        @CreatedDate
        open var createdAt: LocalDateTime? = null

        @LastModifiedDate
        open var updatedAt: LocalDateTime? = null

        constructor(newContact: NewContact) : this() {
            this.name = newContact.name
            this.email = newContact.email
            this.writeup = newContact.writeup
        }

        override fun contribution(): String = "- $email: $writeup"

        override fun toString(): String =
            "Contact{id=$id, name='$name', email='$email', writeup:\n$writeup}"
    }

    data class Participants(
        val participants: List<Contact>
    ) : PromptContributor {
        override fun contribution(): String = participants.joinToString("\n") { it.contribution() }
    }

    data class IndustryAnalysis(
        val analysis: String
    )

    data class MeetingStrategy(
        @JsonPropertyDescription("Complete report with a list of key talking points and strategic questions to ask, to help achieve the meeting's objective")
        val strategy: String
    )

    data class Briefing(
        val meeting: Meeting,
        val participants: Participants,
        val industryAnalysis: IndustryAnalysis,
        val meetingStrategy: MeetingStrategy,
        val briefing: String
    )
}
