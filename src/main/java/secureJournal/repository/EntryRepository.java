package secureJournal.repository;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import secureJournal.model.Entry;

import java.util.List;


@Repository
public interface EntryRepository extends CrudRepository<Entry, Long> { // crud repo to store journal entries



    /**
     * Find a journal entry by id
     * @param id long
     * @return entry
     */
    Entry findById(long id);


    /**
     * Creates or replaces a journal entry
     */
    @Override
    <S extends Entry> S save(S s);


    /**
     * Finds all the entries by user id and sorts them by date
     * @return list of journal entries
     */
    List<Entry> findEntriesByUserIDOrderByDateDesc(Long id);


}

