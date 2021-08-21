package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.jpa.dao.InvitationDao;
import com.goldenebrg.authserver.jpa.entities.InvitationToken;
import com.goldenebrg.authserver.mail.MailService;
import com.goldenebrg.authserver.rest.beans.RequestForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.*;

import static com.goldenebrg.authserver.services.CrossServiceUtils.createUniqueUUID;

@Service

public class InvitationServiceImpl implements InvitationService {

    private final InvitationDao invitationDao;
    private final MailService mailService;

    @Autowired
    public InvitationServiceImpl(InvitationDao invitationDao, MailService mailService) {
        this.invitationDao = invitationDao;
        this.mailService = mailService;
    }


    @Override
    @Caching(
            put = @CachePut(value = {"invitations"}, key = "#result.id"),
            evict = @CacheEvict(value = "allInvitations", allEntries = true)
    )
    public InvitationToken create(@Valid RequestForm requestForm) {
        UUID uuid = createUniqueUUID(invitationDao);
        InvitationToken invitationToken = new InvitationToken(uuid, new Date(), requestForm.getEmail());
        mailService.sendSignUpRequest(invitationToken);
        return invitationDao.save(invitationToken);
    }

    @Override
    @Cacheable(value = "invitationsExistence", key = "#uuid")
    public boolean isExists(UUID uuid) {
        return invitationDao.existsById(uuid);
    }

    @Override
    @Cacheable(value = "allInvitations")
    public List<InvitationToken> getAll() {
        List<InvitationToken> all = invitationDao.findAll();
        TreeSet<InvitationToken> tokens = new TreeSet<>(Comparator.comparing(InvitationToken::getCreationDate));
        tokens.addAll(all);
        return new ArrayList<>(tokens);

    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = {"allInvitations"}, allEntries = true),
            @CacheEvict(value = {"invitations"}, key = "#uuid"),
            @CacheEvict(value = {"invitationsExistence"}, key = "#uuid")
    })
    public void delete(UUID uuid) {
        invitationDao.deleteById(uuid);
    }

    @Override
    @Cacheable(value = {"invitations"}, key = "#uuid")
    public Optional<InvitationToken> find(UUID uuid) {
        return invitationDao.findById(uuid);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = {"allInvitations"}, allEntries = true),
            @CacheEvict(value = {"invitations"}, key = "#invitationToken.id"),
            @CacheEvict(value = {"invitationsExistence"}, key = "#invitationToken.id")
    })
    public void delete(InvitationToken invitationToken) {
        invitationDao.delete(invitationToken);
    }
}
