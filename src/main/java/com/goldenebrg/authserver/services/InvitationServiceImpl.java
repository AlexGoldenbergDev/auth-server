package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.jpa.dao.InvitationDao;
import com.goldenebrg.authserver.jpa.entities.InvitationToken;
import com.goldenebrg.authserver.mail.MailService;
import com.goldenebrg.authserver.rest.beans.RequestForm;
import org.springframework.beans.factory.annotation.Autowired;
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
    public void create(@Valid RequestForm requestForm) {
        UUID uuid = createUniqueUUID(invitationDao);
        InvitationToken invitationToken = new InvitationToken(uuid, new Date(), requestForm.getEmail());
        mailService.sendSignUpRequest(invitationToken);
        invitationDao.save(invitationToken);
    }

    @Override
    public boolean isExists(UUID uuid) {
        return invitationDao.existsById(uuid);
    }

    @Override
    public List<InvitationToken> getAll() {
        List<InvitationToken> all = invitationDao.findAll();
        TreeSet<InvitationToken> tokens = new TreeSet<>(Comparator.comparing(InvitationToken::getCreationDate));
        tokens.addAll(all);
        return new ArrayList<>(tokens);

    }

    @Override
    public void delete(UUID uuid) {
        invitationDao.deleteById(uuid);
    }

    @Override
    public Optional<InvitationToken> find(UUID uuid) {
        return invitationDao.findById(uuid);
    }

    @Override
    public void delete(InvitationToken invitationToken) {
        invitationDao.delete(invitationToken);
    }
}
