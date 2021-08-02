package com.goldenebrg.authserver.services;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

class CrossServiceUtils {

    /**
     * Creating unique {@link UUID}
     *
     * @return unique {@link UUID}
     */
    static UUID createUniqueUUID(JpaRepository<?, UUID> dao) {
        UUID uuid = null;
        boolean isExists = true;
        while (isExists) {
            uuid = UUID.randomUUID();
            isExists = isDaoIdExists(dao, uuid);
        }
        return uuid;
    }

    private static boolean isDaoIdExists(JpaRepository<?, UUID> dao, UUID uuid) {
        return dao.existsById(uuid);
    }
}
