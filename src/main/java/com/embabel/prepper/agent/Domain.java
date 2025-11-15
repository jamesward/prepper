package com.embabel.prepper.agent;

import com.embabel.common.ai.prompt.PromptContributor;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.List;

public abstract class Domain {

	public record Meeting(String context, String objective,
			@JsonPropertyDescription("A list of participant email addresses or however else we identify them,"
					+ " e.g. 'Roger Daltrey The Who' or 'roger@who.com'") List<String> participants)
			implements
				PromptContributor {
		@NonNull
		@Override
		public String contribution() {
			return "Meeting:\nContext: %s\nObjective: %s\n".formatted(context, objective);
		}
	}

	public record NewContact(String name, @Nullable String email, String writeup) {
	}

	@Table("contact")
	public record Contact(@Id Long id, @Column("name") String name, @Column("email") String email,
			@Column("writeup") String writeup, @CreatedDate @Column("created_at") LocalDateTime createdAt,
			@LastModifiedDate @Column("updated_at") LocalDateTime updatedAt) implements PromptContributor {

		public Contact(NewContact newContact) {
			this(null, newContact.name(), newContact.email(), newContact.writeup(), null, null);
		}

		@NotNull
		@Override
		public String contribution() {
			return "- %s: %s".formatted(email, writeup);
		}

		@NotNull
		@Override
		public String toString() {
			return "Contact{id=%d, name='%s', email='%s', writeup:\n%s}".formatted(id, name, email, writeup);
		}
	}

	public record Participants(List<Contact> participants) implements PromptContributor {

		@NotNull
		@Override
		public String contribution() {
			return "Participants:\n" + participants.stream()
				.map(Contact::contribution)
				.collect(java.util.stream.Collectors.joining("\n"));
		}
	}

	public record IndustryAnalysis(String analysis) {
	}

	public record MeetingStrategy(
			@JsonPropertyDescription("Complete report with a list of key talking points and strategic questions"
					+ " to ask, to help achieve the meeting's objective") String strategy) {
	}

	public record Briefing(Meeting meeting, Participants participants, IndustryAnalysis industryAnalysis,
			MeetingStrategy meetingStrategy, String briefing) {
	}

}
