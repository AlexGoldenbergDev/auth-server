package com.goldenebrg.authserver.jpa.entities;

import lombok.*;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@EqualsAndHashCode
@ToString
@NoArgsConstructor
@RequiredArgsConstructor
@MappedSuperclass
public class EmailLinkToken implements Serializable {

    @NonNull
    @Id
    @Getter
    private UUID id;

    @NonNull
    @Getter
    private Date creationDate;

    @NonNull
    @Getter
    private String email;
}
