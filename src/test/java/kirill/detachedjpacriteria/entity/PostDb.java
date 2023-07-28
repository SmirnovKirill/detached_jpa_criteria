package kirill.detachedjpacriteria.entity;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "post")
public class PostDb {
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private UserDb user;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "creation_date", nullable = false)
  private ZonedDateTime created;

  @OneToMany(mappedBy = "post")
  private Set<CommentDb> comments;

  public PostDb() {
  }

  public PostDb(UserDb user, String title, ZonedDateTime created) {
    this.user = user;
    this.title = title;
    this.created = created;
  }

  public Integer getId() {
    return id;
  }

  public UserDb getUser() {
    return user;
  }

  public String getTitle() {
    return title;
  }

  public ZonedDateTime getCreated() {
    return created;
  }

  public Set<CommentDb> getComments() {
    return comments;
  }

  public void addComment(CommentDb comment) {
    if (comments == null) {
      comments = new HashSet<>();
    }

    comments.add(comment);
  }
}
