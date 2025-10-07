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
