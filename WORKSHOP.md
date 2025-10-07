# Embabel Workshop

## Prerequisites

1. Browser
1. Access to your email

## AWS Provisioning

1. Visit the provided workshop URL *in a guest browser*
1. Sign in using the *Email OTP* option
1. Follow the instructions to authenticate to the temporary AWS account
1. Follow the instructions for *Workshop setup*

## Model Access

1. Follow the instructions for *Model Access* except enable these models:
    ```
    Amazon Nova Pro
    Amazon Nova Lite
    ```

1. Now **STOP** following the instructions in the workshop and continue here.

## Get the starter code

1. In the hosted IDE's shell:
    ```
    git clone https://github.com/jamesward/prepper.git
    cd prepper
    ```
1. Pick Java or Kotlin
    ```
    git checkout java
    # or
    git checkout kotlin
    ```
1. Verify the Embabel shell starts:
    ```
    ./mvnw spring-boot:run
    ```
1. Verify the models in the Embabel shell:
    ```
    models
    ```

## Add the domain model

1. Create `src/main/kotlin/com/embabel/prepper/agent/Domain.kt`

```
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
```

1. Create `src/main/kotlin/com/embabel/prepper/agent/ContactRepository.kt`

```
package com.embabel.prepper.agent

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ContactRepository : JpaRepository<Domain.Contact, Long> {
    fun findByEmail(email: String): Optional<Domain.Contact>
    fun existsByEmail(email: String): Boolean
}
```

1. Create `src/main/kotlin/com/embabel/prepper/agent/ContactService.kt`

```
package com.embabel.prepper.agent

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ContactService(private val repository: ContactRepository) {

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
```

1. Replace `src/main/kotlin/com/embabel/prepper/shell/PrepperShell.kt`

```
package com.embabel.prepper.shell

import com.embabel.agent.api.common.autonomy.AgentInvocation
import com.embabel.agent.core.AgentPlatform
import com.embabel.agent.core.ProcessOptions
import com.embabel.prepper.agent.ContactService
import com.embabel.prepper.agent.Domain
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import java.util.Scanner

@ShellComponent
class PrepperShell(
    private val agentPlatform: AgentPlatform,
    private val contactService: ContactService
) {
    @ShellMethod("contacts")
    fun contacts(): String {
        val contacts = contactService.findAll()
        val sb = StringBuilder()

        sb.append("\n=== CONTACTS LIST ===\n")
        contacts.forEach { contact ->
            sb.append("\n")
                .append(contact.name)
                .append("\n")
                .append(contact.email)
                .append("\n")
                .append(contact.writeup)
                .append("\n\n---\n")
        }
        sb.append("=====================\n")

        return sb.toString()
    }
}
```

1. Restart the Embabel shell
1. Verify the new `contacts` command works (you shouldn't see any contacts yet)

## Create the Agent

1. Create `src/main/kotlin/com/embabel/prepper/agent/PrepperConfig.kt`

```
package com.embabel.prepper.agent

import com.embabel.agent.prompt.persona.Actor
import com.embabel.agent.prompt.persona.RoleGoalBackstory
import jakarta.validation.constraints.NotNull
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "prepper")
@Validated
data class PrepperConfig(
    @field:NotNull val researcher: Actor<RoleGoalBackstory>,
    @field:NotNull val industryAnalyzer: Actor<RoleGoalBackstory>,
    @field:NotNull val meetingStrategist: Actor<RoleGoalBackstory>,
    @field:NotNull val briefingWriter: Actor<RoleGoalBackstory>,
    @DefaultValue("8") val maxConcurrency: Int = 8
)
```

1. Verify or create `src/main/kotlin/com/embabel/prepper/PrepperApplication.kt`:

```
package com.embabel.prepper

import com.embabel.agent.config.annotation.EnableAgents
import com.embabel.agent.config.annotation.LoggingThemes
import com.embabel.prepper.agent.PrepperConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableConfigurationProperties(PrepperConfig::class)
@EnableJpaRepositories(basePackages = ["com.embabel.prepper"])
@EnableJpaAuditing
@EnableAgents(loggingTheme = LoggingThemes.SEVERANCE)
class PrepperApplication

fun main(args: Array<String>) {
    runApplication<PrepperApplication>(*args)
}
```

1. Append the following to `src/main/resources/application.yml`

```
prepper:
  researcher:
    llm:
      role: best
    persona:
      role: Research Specialist
      goal: >
        Conduct thorough research on people and companies involved in the meeting.
      backstory: >
        As a Research Specialist, your mission is to uncover detailed information
        about the individuals and entities participating in the meeting. Your insights
        will lay the groundwork for strategic meeting preparation.
  industry-analyzer:
    llm:
      role: balanced
    persona:
      role: Industry Analyst
      goal: Analyze the current industry trends, challenges, and opportunities
      backstory: >
        As an Industry Analyst, your analysis will identify key trends,
        challenges facing the industry, and potential opportunities that
        could be leveraged during the meeting for strategic advantage
  meeting-strategist:
    llm:
      role: best
    persona:
      role: Meeting Strategy Advisor
      goal: Develop talking points, questions, and strategic angles for the meeting
      backstory: >
        As a Strategy Advisor, your expertise will guide the development of
        talking points, insightful questions, and strategic angles
        to ensure the meeting's objectives are achieved
  briefing-writer:
    llm:
      role: best
    persona:
      role: Briefing Coordinator
      goal: Compile all gathered information into a concise, informative briefing document
      backstory: >
        As the Briefing Coordinator, your role is to consolidate the research,
        analysis, and strategic insights
```

1. Create `src/main/kotlin/com/embabel/prepper/agent/PrepperAgent.kt`

```
package com.embabel.prepper.agent

import com.embabel.agent.api.annotation.AchievesGoal
import com.embabel.agent.api.annotation.Action
import com.embabel.agent.api.annotation.Agent
import com.embabel.agent.api.annotation.Export
import com.embabel.agent.api.common.Ai
import com.embabel.agent.api.common.OperationContext
import org.slf4j.LoggerFactory

@Agent(description = "A meeting prepper agent that helps users prepare for meetings ")
class PrepperAgent(
    private val config: PrepperConfig,
    private val contactService: ContactService
) {

    private val logger = LoggerFactory.getLogger(PrepperAgent::class.java)

    init {
        logger.info("Initialized PrepperAgent with config: {}", config)
    }

    @Action
    fun researchParticipants(meeting: Domain.Meeting, embabel: OperationContext): Domain.Participants {
        val researcher = config.researcher
            .promptRunner(embabel)
            .creating(Domain.NewContact::class.java)

        val contacts = embabel.parallelMap(
            meeting.participants,
            config.maxConcurrency
        ) { participant ->
            contactService.resolveContact(participant)
                .orElseGet {
                    val newContact = researcher.fromPrompt(
                        """
                            Conduct comprehensive research on this individual and company
                            involved in the upcoming meeting. Gather information on recent
                            news, achievements, professional background, and any relevant
                            business activities.
                            
                            Do your best to populate email address.
                            
                            Participant: $participant
                            ${meeting.purpose()}
                        """.trimIndent()
                    )
                    contactService.createContact(newContact)
                }
        }
        return Domain.Participants(contacts)
    }

    @Action
    fun analyzeIndustry(meeting: Domain.Meeting, participants: Domain.Participants, ai: Ai): Domain.IndustryAnalysis {
        return config.industryAnalyzer
            .promptRunner(ai)
            .createObject(
                """
                    Analyze the current industry trends, challenges, and opportunities
                    relevant to the meeting's context. Consider market reports, recent
                    developments, and expert opinions to provide a comprehensive
                    overview of the industry landscape.
                    
                    Identify major trends, potential
                    challenges, and strategic opportunities.
                    
                    Participants: ${participants.contribution()}
                    ${meeting.purpose()}
                """.trimIndent(),
                Domain.IndustryAnalysis::class.java
            )
    }

    @Action
    fun formulateMeetingStrategy(
        meeting: Domain.Meeting,
        participants: Domain.Participants,
        industryAnalysis: Domain.IndustryAnalysis,
        ai: Ai
    ): Domain.MeetingStrategy {
        return config.meetingStrategist
            .promptRunner(ai)
            .createObject(
                """
                    Develop strategic talking points, questions, and discussion angles
                    for the meeting based on the research and industry analysis conducted
                    
                    Participants: ${participants.contribution()}
                    
                    ${meeting.purpose()},
                """.trimIndent(),
                Domain.MeetingStrategy::class.java
            )
    }

    @Action
    @AchievesGoal(
        description = "Produce a briefing for the meeting",
        export = Export(remote = true, startingInputTypes = [Domain.Meeting::class])
    )
    fun produceBriefing(
        meeting: Domain.Meeting,
        participants: Domain.Participants,
        industryAnalysis: Domain.IndustryAnalysis,
        meetingStrategy: Domain.MeetingStrategy,
        ai: Ai
    ): Domain.Briefing {
        val briefing = config.briefingWriter
            .promptRunner(ai)
            .generateText(
                """
                    Compile all the information given into a briefing for the meeting
                    Consolidate research, analysis, and strategic insights.
                    
                    ${meeting.purpose()}
                    Participants: ${participants.contribution()}
                """.trimIndent()
            )
        return Domain.Briefing(
            meeting,
            participants,
            industryAnalysis,
            meetingStrategy,
            briefing
        )
    }
}
```

1. Update `src/main/kotlin/com/embabel/prepper/shell/PrepperShell.kit` adding the following inside the record:

```
    @ShellMethod("prep")
    fun prep(): String {
        val scanner = Scanner(System.`in`)

        println("Embabel Meeting Preparation Assistant")
        println("============================")

        print("1. Enter meeting context/title: ")
        val context = scanner.nextLine()

        print("2. Enter meeting objective: ")
        val objective = scanner.nextLine()

        println("3. Enter participants (one per line, type 'done' to finish):")
        println("   Examples: Fred Flintstone, bill@bigcompany.com, John Smith CEO Acme Corp")

        val participants = mutableListOf<String>()
        var participant: String
        while (true) {
            participant = scanner.nextLine()
            if (participant.equals("done", ignoreCase = true)) break
            if (participant.trim().isNotEmpty()) {
                participants.add(participant.trim())
                println("   Added: ${participant.trim()}")
            }
        }

        // Print summary before processing
        println("\n=== MEETING SUMMARY ===")
        println("Context: $context")
        println("Objective: $objective")
        println("Participants:")
        for (i in participants.indices) {
            println("  ${i + 1}. ${participants[i]}")
        }
        println("=======================")

        println("\nGenerating briefing...")

        val meeting = Domain.Meeting(context, objective, participants)
        val briefing = AgentInvocation.builder(agentPlatform)
            .options(ProcessOptions.builder().verbosity { v -> v.showPrompts(true) }.build())
            .build(Domain.Briefing::class.java)
            .invoke(meeting)
        return briefing.briefing
    }
```

1. Restart the Embabel shell
1. Verify the new `agents` command lists your agent
1. Verify the new `prep` command runs the agent (likely with bad results)
1. Verify a Contact was created with the `contacts` command

## Add the Brave MCP Tool

1. Update the `src/main/resources/application.yml` file:

```
spring:
  datasource:
    url: jdbc:h2:file:./data/prepper;AUTO_SERVER=true
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: true
  ai:
    mcp:
      client:
        sse:
          connections:
            brave-search-mcp:
              url:
                https://bravemcp-fb63dd930387.herokuapp.com/sse

embabel:
  models:
    default-llm: us.amazon.nova-pro-v1:0
    llms:
      best: us.amazon.nova-pro-v1:0
      balanced: us.amazon.nova-lite-v1:0

prepper:
  researcher:
    llm:
      role: best
    tool-groups:
      - web
    persona:
      role: Research Specialist
      goal: >
        Conduct thorough research on people and companies involved in the meeting.
        Use web tools to perform your research
      backstory: >
        As a Research Specialist, your mission is to uncover detailed information
        about the individuals and entities participating in the meeting. Your insights
        will lay the groundwork for strategic meeting preparation.

  industry-analyzer:
    llm:
      role: balanced
    tool-groups:
      - web
    persona:
      role: Industry Analyst
      goal: Analyze the current industry trends, challenges, and opportunities
      backstory: >
        As an Industry Analyst, your analysis will identify key trends,
        challenges facing the industry, and potential opportunities that
        could be leveraged during the meeting for strategic advantage

  meeting-strategist:
    llm:
      role: best
    tool-groups:
      - web
    persona:
      role: Meeting Strategy Advisor
      goal: Develop talking points, questions, and strategic angles for the meeting
      backstory: >
        As a Strategy Advisor, your expertise will guide the development of
        talking points, insightful questions, and strategic angles
        to ensure the meeting's objectives are achieved

  briefing-writer:
    llm:
      role: best
    tool-groups:
      - web
    persona:
      role: Briefing Coordinator
      goal: Compile all gathered information into a concise, informative briefing document
      backstory: >
        As the Briefing Coordinator, your role is to consolidate the research,
        analysis, and strategic insights
```

1. Restart the Embabel shell
1. Run the `tools` command to verify the Brave MCP tool is installed
1. Run the `prep` command to run the agent again (hopefully with better results)

## Celebrate!
