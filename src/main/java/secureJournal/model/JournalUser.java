package secureJournal.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.Collection;

@Data
@Entity
@Table(name = "users")
@SequenceGenerator(name = "user_seq_gen", sequenceName = "user_seq", initialValue = 10, allocationSize = 1)
public class JournalUser implements UserDetails { //model class for users

    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MIN_PASSWORD_LENGTH = 8;


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq_gen")
    @Column(name = "id")
    private Long id; //variable for user ID

    @Length(min = MIN_USERNAME_LENGTH, message = "Username must be at least " + MIN_USERNAME_LENGTH + " characters long")
    @NotEmpty(message = "Please enter username") //making sure it's not null
    @Column(name = "username", nullable = false, unique = true)
    private String username; //variable for user name

    @JsonIgnore
    @Size(min = MIN_PASSWORD_LENGTH, message = "Password must be at least " + MIN_PASSWORD_LENGTH + " characters long")
    @NotEmpty(message = "Please enter the password") //making sure it's not null
    @Column(name = "password", nullable = false)
    private String password; //variable for password

    @Column(name = "enabled", nullable = false) //variable for user security
    private Boolean enabled;


    @ManyToMany(cascade = CascadeType.REMOVE)
    @JoinTable(
            name = "users_authorities",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "authority_id")
    )

    private Collection<Authority> authorities; //collection of roles

    @Override
    public boolean isAccountNonExpired() {
        return true;
    } //function for security checks


    @Override
    public boolean isAccountNonLocked() {
        return true;
    } //function for security checks

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    } //function for security checks

    @Override
    public boolean isEnabled() {
        return this.enabled;
    } //function for security checks


    public String getPass(){
        return password;
    } //getter function


    @Override
    public String toString() { //to string method for model class
        return "BlogUser{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", authorities=" + authorities +
                '}';
    }
}
