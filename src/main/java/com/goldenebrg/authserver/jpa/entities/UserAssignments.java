package com.goldenebrg.authserver.jpa.entities;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Entity
@EqualsAndHashCode(exclude = "user")
@ToString
@NoArgsConstructor
@Table(name = "assignments")
public class UserAssignments {


    @Id
    @Getter
    @Setter
    @GeneratedValue
    @NonNull
    UUID id;

    @Getter
    @Setter
    @ManyToOne
    User user;

    @Getter
    @Setter
    @NonNull
    String name;

    @Getter
    @Setter
    boolean isEnabled;

    @Getter
    @ElementCollection
    @CollectionTable(name = "assignments_fields")
    @Column(name = "value")
    Map<String, Serializable> fields = new TreeMap<>(Comparator.naturalOrder());



    public void addField(String key, Serializable serializable) {
        fields.put(key, serializable);
    }
}
