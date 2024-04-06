package skhu.jijijig.domain.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.google.firebase.auth.FirebaseToken;
import jakarta.persistence.*;
import lombok.*;
import skhu.jijijig.domain.dto.MemberDTO;

import java.util.List;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String picture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean firebaseAuth;

    @OneToMany(mappedBy = "member", orphanRemoval = true)
    @JsonManagedReference
    private List<Board> boards; // 작성글

    @OneToMany(mappedBy = "member", orphanRemoval = true)
    @JsonManagedReference
    private List<Comment> comments; // 댓글

    @OneToMany(mappedBy = "member", orphanRemoval = true)
    @JsonManagedReference
    private List<Heart> hearts; // 유저가 누른 좋아요

    public static Member fromDTO(MemberDTO memberDTO) {
        return Member.builder()
                .name(memberDTO.getName())
                .email(memberDTO.getEmail())
                .picture(memberDTO.getPicture())
                .role(Role.USER)
                .firebaseAuth(true)
                .build();
    }

    public static Member fromToken(FirebaseToken firebaseToken) {
        return Member.builder()
                .name(firebaseToken.getName())
                .email(firebaseToken.getEmail())
                .picture(firebaseToken.getPicture())
                .role(Role.USER)
                .firebaseAuth(true)
                .build();
    }

    public boolean isAuthorizedToDelete(Member member) {
        return this.equals(member) || member.getRole().equals(Role.ADMIN);
    }
}