package com.goldenebrg.authserver.jpa.entities;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@ToString
@NoArgsConstructor
@Entity
@Table(name = "invitation_tokens")
public class InvitationToken extends EmailLinkToken {

    public InvitationToken(UUID uuid, Date date, String email) {
        super(uuid, date, email);
    }
}
