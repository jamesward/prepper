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
