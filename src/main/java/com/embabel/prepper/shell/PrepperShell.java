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
