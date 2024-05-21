package secureJournal.service;

//import org.example.blogapp.model.Authority;
//import org.example.blogapp.model.BlogUser;
//import org.example.blogapp.repository.AuthorityRepository;
//import org.example.blogapp.repository.BlogUserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import secureJournal.model.Authority;
import secureJournal.model.JournalUser;
import secureJournal.repository.JournalUserRepository;
import secureJournal.repository.AuthorityRepository;

import javax.management.relation.RoleNotFoundException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_ROLE = "ROLE_USER"; //variable to store the default role.
    private final BCryptPasswordEncoder bcryptEncoder; //encoder used for encoding password into DB
    private final JournalUserRepository journalUserRepository; //JPA Repository to store User Models
    private final AuthorityRepository authorityRepository; //JPA Repository to store role Models

    @Autowired
    public UserServiceImpl(BCryptPasswordEncoder bcryptEncoder, JournalUserRepository journalUserRepository, AuthorityRepository authorityRepository) {
        this.bcryptEncoder = bcryptEncoder;
        this.journalUserRepository = journalUserRepository; //constructor from implementation
        this.authorityRepository = authorityRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException { //function to get a username from the Repository
        Optional<JournalUser> journalUser = journalUserRepository.findByUsername(username);  //getting journal object from username
        if (journalUser.isPresent()) { //verifying the user is present
            return journalUser.get(); // pass user object
        } else { //else throw exception
            throw new UsernameNotFoundException("No user found with username " + username); // pass exception
        }
    }

    @Override
    public Optional<JournalUser> findByUsername(String username) { //function to find a username from the Repository
        return journalUserRepository.findByUsername(username);
    }

    @Override
    public JournalUser saveNewUser(JournalUser journalUser) throws RoleNotFoundException { //function to save a user in the repository (DB)
        System.err.println("saveNewBlogUser: " + journalUser);

        journalUser.setPassword(this.bcryptEncoder.encode(journalUser.getPassword())); //encrypting the user password using bcrypt

        journalUser.setEnabled(true); // setting attributes in the user model


        Optional<Authority> addAuthority = this.authorityRepository.findByAuthority(DEFAULT_ROLE);
        if (addAuthority.isPresent()) { //add role to user
            Authority authority = addAuthority.get();
            Collection<Authority> authorities = Collections.singletonList(authority);
            journalUser.setAuthorities(authorities);

            return this.journalUserRepository.saveAndFlush(journalUser); //save user
        } else { //else for if role is not found.
            throw new RoleNotFoundException("Default role not found for blog user with username " + journalUser.getUsername());
        }
    }
}
