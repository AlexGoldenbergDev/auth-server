package com.goldenebrg.authserver.jpa.entities;


import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Entity
@EqualsAndHashCode(exclude = "userServices")
@RequiredArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User implements Serializable {

    @Id
    @Getter
    @NonNull
    private UUID id;

    @Getter
    @NonNull
    private String email;
    @Getter
    @NonNull
    private String username;

    @Getter
    @Setter
    @NonNull
    private String password;

    @Getter
    @Setter
    @NonNull
    private String role;

    @Getter
    @Setter
    @NonNull
    private Boolean enabled;

    @Getter
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @MapKey(name = "name")
    @NonNull
    private final Map<String, UserServices> userServices = new TreeMap<>(Comparator.naturalOrder());


    public User(UUID id, String username, String email, String role, String password) {
        this(id, email, username, password, role, true);
    }

    public void addUserService(UserServices userServices) {
        this.userServices.put(userServices.getName(), userServices);
    }
}
