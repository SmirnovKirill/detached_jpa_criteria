package kirill.detachedjpacriteria.entity;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "`user`")
public class UserDb {
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  private Integer id;

  @Column(name = "login", nullable = false)
  private String login;

  @Column(name = "email", nullable = false)
  private String email;

  @Enumerated(EnumType.STRING)
  @Column(name = "user_type_str", nullable = false)
  private UserType userTypeStr;

  @Enumerated(EnumType.ORDINAL)
  @Column(name = "user_type_ord", nullable = false)
  private UserType userTypeOrd;

  @Column(name = "active", nullable = false)
  private boolean active;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "last_name")
  private String lastName;

  @Column(name = "creation_date", nullable = false)
  private ZonedDateTime created;

  @Column(name = "last_login")
  private ZonedDateTime lastLogin;

  @OneToMany(mappedBy = "user")
  private Set<PostDb> posts;

  public UserDb() {
  }

  public UserDb(
      String login,
      String email,
      UserType userTypeStr,
      UserType userTypeOrd,
      boolean active,
      String name,
      String lastName,
      ZonedDateTime created,
      ZonedDateTime lastLogin
  ) {
    this.login = login;
    this.email = email;
    this.userTypeStr = userTypeStr;
    this.userTypeOrd = userTypeOrd;
    this.active = active;
    this.name = name;
    this.lastName = lastName;
    this.created = created;
    this.lastLogin = lastLogin;
  }

  public Integer getId() {
    return id;
  }

  public String getLogin() {
    return login;
  }

  public String getEmail() {
    return email;
  }

  public UserType getUserTypeStr() {
    return userTypeStr;
  }

  public UserType getUserTypeOrd() {
    return userTypeOrd;
  }

  public boolean isActive() {
    return active;
  }

  public String getName() {
    return name;
  }

  public String getLastName() {
    return lastName;
  }

  public ZonedDateTime getCreated() {
    return created;
  }

  public ZonedDateTime getLastLogin() {
    return lastLogin;
  }

  public Set<PostDb> getPosts() {
    return posts;
  }

  public void addPost(PostDb post) {
    if (posts == null) {
      posts = new HashSet<>();
    }

    posts.add(post);
  }
}
