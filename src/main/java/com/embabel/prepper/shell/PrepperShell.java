package com.embabel.prepper.shell;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.ProcessOptions;
import com.embabel.agent.core.Verbosity;
import com.embabel.prepper.agent.ContactService;
import com.embabel.prepper.agent.Domain;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;

@ShellComponent
record PrepperShell(AgentPlatform agentPlatform, ContactService contactService) {

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
                .options(ProcessOptions.DEFAULT.withVerbosity(Verbosity.DEFAULT.showPrompts()))
                .build(Domain.Briefing.class)
                .invoke(meeting);
        return briefing.briefing();
    }

    @ShellMethod("contacts")
    String contacts() {
        var contacts = contactService.findAll();
        var sb = new StringBuilder();

        sb.append("\n=== CONTACTS LIST ===\n");
        contacts.forEach(contact -> {
            sb.append("\n")
                    .append(contact.name())
                    .append("\n")
                    .append(contact.email())
                    .append("\n")
                    .append(contact.writeup())
                    .append("\n")
                    .append("\n")
                    .append("---")
                    .append("\n");
        });
        sb.append("=====================\n");

        return sb.toString();
    }

    @ShellMethod("findcontact")
    Optional<Domain.Contact> findContact() {
        var scanner = new Scanner(System.in);
        System.out.println("Contacts query: ");
        var identification = scanner.nextLine();
        return contactService.resolveContact(identification);
    }

}
