package skhu.jijijig.domain.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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

    @OneToMany(mappedBy = "reporter", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Report> reports; // 회원이 신고한 내역

    @OneToMany(mappedBy = "member", orphanRemoval = true)
    @JsonManagedReference
    private List<Heart> hearts; // 유저가 누른 좋아요

    public boolean isAuthorizedToDelete(Member member) {
        return this.equals(member) || member.getRole().equals(Role.ADMIN);
    }

    public static Member fromDTO(MemberDTO memberDTO) {
        return Member.builder()
                .name(memberDTO.getName())
                .email(memberDTO.getEmail())
                .picture(memberDTO.getPicture())
                .role(Role.USER)
                .firebaseAuth(true)
                .build();
    }
}