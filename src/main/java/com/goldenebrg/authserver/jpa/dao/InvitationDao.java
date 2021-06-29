package com.goldenebrg.authserver.jpa.dao;

import com.goldenebrg.authserver.jpa.entities.InvitationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InvitationDao extends JpaRepository<InvitationToken, UUID>, TokensDao {

}
