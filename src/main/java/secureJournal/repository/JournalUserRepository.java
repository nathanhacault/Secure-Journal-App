package secureJournal.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import secureJournal.model.JournalUser;

import java.util.Optional;

@Repository
public interface JournalUserRepository extends JpaRepository<JournalUser, Long> { // JPA repo to store users

    Optional<JournalUser> findByUsername(String username); //function to find a username returns model class

}
