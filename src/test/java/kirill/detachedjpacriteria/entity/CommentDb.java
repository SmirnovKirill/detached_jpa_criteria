package kirill.detachedjpacriteria.entity;

import java.time.ZonedDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "comment")
public class CommentDb {
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "post_id", nullable = false)
  private PostDb post;

  @Column(name = "text", nullable = false)
  private String text;

  @Column(name = "creation_date", nullable = false)
  private ZonedDateTime created;

  public CommentDb() {
  }

  public CommentDb(PostDb post, String text, ZonedDateTime created) {
    this.post = post;
    this.text = text;
    this.created = created;
  }

  public Integer getId() {
    return id;
  }

  public PostDb getPost() {
    return post;
  }

  public String getText() {
    return text;
  }

  public ZonedDateTime getCreated() {
    return created;
  }
}
