package com.quinzex.entity;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "lms_login")
@Getter
@Setter
@NoArgsConstructor
public class LmsLogin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    @Version
    private int tokenVersion = 0;

    @ManyToOne(fetch = FetchType.EAGER,optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Roles role;
    @Column(nullable = false,name = "is_authorized")
    private Boolean isAuthorized=false;

    @Column(nullable = false)
    private String status;
}



