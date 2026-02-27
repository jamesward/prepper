package com.embabel.prepper.shell;

import com.embabel.agent.core.AgentPlatform;
import com.embabel.prepper.agent.ContactService;
import com.embabel.prepper.agent.Domain;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.Optional;
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
