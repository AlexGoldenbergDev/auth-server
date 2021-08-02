package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.jpa.entities.InvitationToken;
import com.goldenebrg.authserver.rest.beans.RequestForm;
import com.sun.istack.NotNull;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvitationService {

    /**
     * Creates a new Invitation {@link InvitationToken}
     */
    void create(@NotNull @Valid RequestForm requestForm);

    /**
     * Validates presence of {@link InvitationToken} with following UUID
     *
     * @param uuid - {@link InvitationToken#getId()}
     */
    boolean isExists(UUID uuid);

    /**
     * Returns full {@link InvitationToken} list
     *
     * @return List of all persisted requests
     */

    List<InvitationToken> getAll();

    /**
     * Deletes specific {@link InvitationToken}
     *
     * @param uuid - {@link InvitationToken#getId()}
     */
    void delete(UUID uuid);

    Optional<InvitationToken> find(UUID uuid);

    void delete(InvitationToken invitationToken);
}
