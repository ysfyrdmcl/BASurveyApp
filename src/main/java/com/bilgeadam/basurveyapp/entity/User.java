package com.bilgeadam.basurveyapp.entity;

import com.bilgeadam.basurveyapp.entity.base.BaseEntity;
import com.bilgeadam.basurveyapp.entity.enums.Role;
import com.bilgeadam.basurveyapp.entity.enums.State;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name = "users")
public class User extends BaseEntity implements UserDetails {
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(name = "first_name", nullable = false)
    private String firstName;
    @Column(name = "last_name", nullable = false)
    private String lastName;
    @Enumerated(EnumType.STRING)
    private Role role;
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "user_classroom",
            joinColumns = @JoinColumn(name = "user_oid"),
            inverseJoinColumns = @JoinColumn(name = "classroom_oid"))
    private List<Classroom> classrooms;
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "user_survey",
            joinColumns = @JoinColumn(name = "user_oid"),
            inverseJoinColumns = @JoinColumn(name = "survey_oid"))
    private List<Survey> surveys;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.toString()));
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return getState() == State.ACTIVE;
    }
}
