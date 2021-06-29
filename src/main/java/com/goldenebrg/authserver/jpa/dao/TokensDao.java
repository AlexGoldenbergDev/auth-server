package com.goldenebrg.authserver.jpa.dao;

import java.util.Date;

public interface TokensDao {
    void deleteAllByCreationDateBefore(Date date);

}
