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

1. Create `src/main/java/com/embabel/prepper/agent/Domain.java`

```
package com.embabel.prepper.agent;

import com.embabel.common.ai.prompt.PromptContributor;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.persistence.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.List;

public abstract class Domain {

    public record Meeting(
            String context,
            String objective,
            @JsonPropertyDescription("A list of participant email addresses or however else we identify them, e.g. 'Roger Daltrey The Who' or 'roger@who.com'")
            List<String> participants
    ) {

        @NonNull
        public String purpose() {
            return "Meeting:\nContext: %s\nObjective: %s\n".formatted(context, objective);
        }
    }

    public record NewContact(
            String name,
            @Nullable String email,
            String writeup
    ) {
    }

    /**
     * Fleshed out participant
     */
    @Entity
    @EntityListeners(AuditingEntityListener.class)
    public static class Contact implements PromptContributor {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false)
        private String name;

        @Column
        private String email;

        @Column(columnDefinition = "TEXT")
        private String writeup;

        @CreatedDate
        private LocalDateTime createdAt;

        @LastModifiedDate
        private LocalDateTime updatedAt;

        protected Contact() {
        }

        public Contact(NewContact newContact) {
            this.name = newContact.name;
            this.email = newContact.email;
            this.writeup = newContact.writeup;
        }

        @NotNull
        @Override
        public String contribution() {
            return "- %s: %s".formatted(email, writeup);
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getWriteup() {
            return writeup;
        }

        public void setWriteup(String writeup) {
            this.writeup = writeup;
        }

        public String getName() {
            return name;
        }

        public String email() {
            return email;
        }

        public String writeup() {
            return writeup;
        }

        @Override
        public String toString() {
            return "Contact{id=%d, name='%s', email='%s', writeup:\n%s}".formatted(id, name, email, writeup);
        }
    }

    public record Participants(
            List<Contact> participants
    ) implements PromptContributor {

        @NotNull
        @Override
        public String contribution() {
            return participants.stream()
                    .map(Contact::contribution)
                    .collect(java.util.stream.Collectors.joining("\n"));
        }
    }

    public record IndustryAnalysis(
            String analysis
    ) {
    }

    public record MeetingStrategy(
            @JsonPropertyDescription("Complete report with a list of key talking points and strategic questions" +
                    " to ask, to help achieve the meeting's objective")
            String strategy
    ) {
    }

    public record Briefing(
            Meeting meeting,
            Participants participants,
            IndustryAnalysis industryAnalysis,
            MeetingStrategy meetingStrategy,
            String briefing
    ) {
    }

}
```

1. Create `src/main/java/com/embabel/prepper/agent/ResearchedParticipantRepository.java`

```
package com.embabel.prepper.agent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResearchedParticipantRepository extends JpaRepository<Domain.Contact, Long> {

    Optional<Domain.Contact> findByEmail(String email);

    boolean existsByEmail(String email);
}
```

1. Create `src/main/java/com/embabel/prepper/agent/ContactService.java`

```
package com.embabel.prepper.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    @Transactional(readOnly = true)
    public List<Domain.Contact> findAll() {
        return repository.findAll();
    }

    @Transactional
    public Domain.Contact createContact(Domain.NewContact newContact) {
        var saved = repository.save(new Domain.Contact(newContact));
        logger.info("Created new contact: {}", saved);
        return saved;
    }
}
```

1. Replace `src/main/java/com/embabel/prepper/shell/PrepperShell.java`

```
package com.embabel.prepper.shell;

import com.embabel.agent.api.common.autonomy.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.ProcessOptions;
import com.embabel.prepper.agent.ContactService;
import com.embabel.prepper.agent.Domain;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@ShellComponent
record PrepperShell(AgentPlatform agentPlatform, ContactService contactService) {

    @ShellMethod("contacts")
    String contacts() {
        var contacts = contactService.findAll();
        var sb = new StringBuilder();

        sb.append("\n=== CONTACTS LIST ===\n");
        contacts.forEach(contact -> {
            sb.append("\n")
                    .append(contact.getName())
                    .append("\n")
                    .append(contact.getEmail())
                    .append("\n")
                    .append(contact.getWriteup())
                    .append("\n")
                    .append("\n")
                    .append("---")
                    .append("\n");
        });
        sb.append("=====================\n");

        return sb.toString();
    }
}
```

1. Restart the Embabel shell
1. Verify the new `contacts` command works (you shouldn't see any contacts yet)

## Create the Agent

1. Create `src/main/java/com/embabel/prepper/agent/PrepperConfig.java`

```
package com.embabel.prepper.agent;

import com.embabel.agent.prompt.persona.Actor;
import com.embabel.agent.prompt.persona.RoleGoalBackstory;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;


@ConfigurationProperties(prefix = "prepper")
@Validated
public record PrepperConfig(
        @NotNull Actor<RoleGoalBackstory> researcher,
        @NotNull Actor<RoleGoalBackstory> industryAnalyzer,
        @NotNull Actor<RoleGoalBackstory> meetingStrategist,
        @NotNull Actor<RoleGoalBackstory> briefingWriter,
        @DefaultValue("8") int maxConcurrency
) {
}
```

1. Update `src/main/java/com/embabel/prepper/PrepperApplication.java` adding the following before `@SpringBootApplication`:

```
import com.embabel.prepper.agent.PrepperConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(PrepperConfig.class)
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

1. Create `src/main/java/com/embabel/prepper/agent/PrepperAgent.java`

```
package com.embabel.prepper.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.annotation.Export;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.OperationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Agent(description = "A meeting prepper agent that helps users prepare for meetings ")
public record PrepperAgent(
        PrepperConfig config,
        ContactService contactService
) {

    private final static Logger logger = LoggerFactory.getLogger(PrepperAgent.class);

    public PrepperAgent {
        logger.info("Initialized PrepperAgent with config: {}", config);
    }

    @Action
    public Domain.Participants researchParticipants(Domain.Meeting meeting, OperationContext embabel) {
        var researcher = config.researcher()
                .promptRunner(embabel)
                .creating(Domain.NewContact.class);
        var contacts = embabel.parallelMap(
                meeting.participants(),
                config.maxConcurrency(),
                participant ->
                        contactService.resolveContact(participant)
                                .orElseGet(() -> {
                                    var newContact = researcher.fromPrompt("""
                                            Conduct comprehensive research on this individual and company
                                            involved in the upcoming meeting. Gather information on recent
                                            news, achievements, professional background, and any relevant
                                            business activities.
                                            
                                            Do your best to populate email address.
                                            
                                            Participant: %s
                                            %s
                                            """.formatted(participant, meeting.purpose()));
                                    return contactService.createContact(newContact);
                                })
        );
        return new Domain.Participants(contacts);
    }

    @Action
    public Domain.IndustryAnalysis analyzeIndustry(Domain.Meeting meeting, Domain.Participants participants, Ai ai) {
        return config.industryAnalyzer()
                .promptRunner(ai)
                .createObject("""
                                Analyze the current industry trends, challenges, and opportunities
                                relevant to the meeting's context. Consider market reports, recent
                                developments, and expert opinions to provide a comprehensive
                                overview of the industry landscape.
                                
                                Identify major trends, potential
                                challenges, and strategic opportunities.
                                
                                Participants: %s
                                %s
                                """.formatted(participants.contribution(), meeting.purpose()),
                        Domain.IndustryAnalysis.class);
    }

    @Action
    public Domain.MeetingStrategy formulateMeetingStrategy(
            Domain.Meeting meeting,
            Domain.Participants participants,
            Domain.IndustryAnalysis industryAnalysis,
            Ai ai) {
        return config.meetingStrategist()
                .promptRunner(ai)
                .createObject("""
                                Develop strategic talking points, questions, and discussion angles
                                for the meeting based on the research and industry analysis conducted
                                
                                Participants: %s
                                
                                %s),
                                """.formatted(participants.contribution(), meeting.purpose()),
                        Domain.MeetingStrategy.class);
    }

    @Action
    @AchievesGoal(description = "Produce a briefing for the meeting",
            export = @Export(remote = true, startingInputTypes = {Domain.Meeting.class}))
    public Domain.Briefing produceBriefing(
            Domain.Meeting meeting,
            Domain.Participants participants,
            Domain.IndustryAnalysis industryAnalysis,
            Domain.MeetingStrategy meetingStrategy,
            Ai ai) {
        var briefing = config.briefingWriter()
                .promptRunner(ai)
                .generateText("""
                        Compile all the information given into a briefing for the meeting
                        Consolidate research, analysis, and strategic insights.
                        
                        %s
                        Participants: %s
                        """.formatted(meeting.purpose(), participants.contribution()
                ));
        return new Domain.Briefing(
                meeting,
                participants,
                industryAnalysis,
                meetingStrategy,
                briefing
        );
    }
}
```

1. Update `src/main/java/com/embabel/prepper/shell/PrepperShell.java` adding the following inside the record:

```
    @ShellMethod("prep")
    String prep() {
        var scanner = new Scanner(System.in);

        System.out.println("Embabel Meeting Preparation Assistant");
        System.out.println("============================");

        System.out.print("1. Enter meeting context/title: ");
        var context = scanner.nextLine();

        System.out.print("2. Enter meeting objective: ");
        var objective = scanner.nextLine();

        System.out.println("3. Enter participants (one per line, type 'done' to finish):");
        System.out.println("   Examples: Fred Flintstone, bill@bigcompany.com, John Smith CEO Acme Corp");

        var participants = new ArrayList<String>();
        String participant;
        while (!(participant = scanner.nextLine()).equalsIgnoreCase("done")) {
            if (!participant.trim().isEmpty()) {
                participants.add(participant.trim());
                System.out.printf("   Added: %s%n", participant.trim());
            }
        }

        // Print summary before processing
        System.out.println("\n=== MEETING SUMMARY ===");
        System.out.printf("Context: %s%n", context);
        System.out.printf("Objective: %s%n", objective);
        System.out.println("Participants:");
        for (int i = 0; i < participants.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, participants.get(i));
        }
        System.out.println("=======================");

        System.out.println("\nGenerating briefing...");

        var meeting = new Domain.Meeting(context, objective, participants);
        var briefing = AgentInvocation.builder(agentPlatform)
                .options(ProcessOptions.builder().verbosity(v -> v.showPrompts(true)).build())
                .build(Domain.Briefing.class)
                .invoke(meeting);
        return briefing.briefing();
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
