package secureJournal.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import secureJournal.model.Authority;

import java.util.Optional;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Long> { // JPA repo to store user roles

    Optional<Authority> findByAuthority(String authority); //function to find a role in DB

}

