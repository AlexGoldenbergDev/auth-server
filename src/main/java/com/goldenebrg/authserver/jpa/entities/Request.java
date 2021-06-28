package com.goldenebrg.authserver.jpa.entities;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@RequiredArgsConstructor
@Table(name = "request")
public class Request implements Serializable {

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
