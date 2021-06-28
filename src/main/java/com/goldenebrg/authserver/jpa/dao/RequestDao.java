package com.goldenebrg.authserver.jpa.dao;

import com.goldenebrg.authserver.jpa.entities.Request;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface RequestDao extends JpaRepository<Request, UUID> {

    List<Request> findAllByCreationDateBefore(Date date);

    void deleteAllByCreationDateIsBetween(Date date0, Date date1);



}
