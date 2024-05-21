package secureJournal.service;


import org.springframework.security.core.userdetails.UserDetailsService;
import secureJournal.model.JournalUser;

import javax.management.relation.RoleNotFoundException;
import java.util.Optional;

public interface UserService extends UserDetailsService { //implementation for User service

    Optional<JournalUser> findByUsername(String username); //function to find a username

    JournalUser saveNewUser(JournalUser journalUser) throws RoleNotFoundException; //function to save a user into the DB.

}
