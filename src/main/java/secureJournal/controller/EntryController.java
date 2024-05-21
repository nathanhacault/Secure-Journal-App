package secureJournal.controller;



import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import secureJournal.GCM;
import secureJournal.model.JournalUser;
import secureJournal.model.Entry;
import secureJournal.model.Response;
import secureJournal.repository.EntryRepository;
import secureJournal.service.UserService;

import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletResponse;

import java.security.Principal;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api")
public class EntryController { //controller for journal entries


    private final EntryRepository entryRepository;
    private final UserService userService;

    @Autowired
    public EntryController(EntryRepository entryRepository, UserService userService) {
        this.entryRepository = entryRepository;
        this.userService = userService;
    }

    @GetMapping("/entries")
    public ResponseEntity<Response> getEntries(Principal principal) throws Exception { //function to get all journal entries of a user
        Response response;
        HttpStatus status;
        String authUsername = null;

        if (principal != null) {
             authUsername = principal.getName(); // Retrieves the logged-in username
        }
        Optional<JournalUser> optionalJournalUser = userService.findByUsername(authUsername); //Getting JournalUser Object from username

        List<Entry> entries = entryRepository.findEntriesByUserIDOrderByDateDesc(optionalJournalUser.get().getId()); //finding the entries tied to that user id.

        for(Entry e : entries){ //iterate thourgh the entry list to decrypt entries  using user password.
            String msg = e.getText();
            String img = e.getImg();
            String masterKey = optionalJournalUser.get().getPass(); //decrypting image and journal text
            String decodedMsg = GCM.decrypt(msg, masterKey);
            String decodeImg = GCM.decrypt(img, masterKey);

            e.setText(decodedMsg); //modifying the entry with the decrypted text
            e.setImg(decodeImg);
        }

        if (!entries.isEmpty()){ //if not empty send success response
            response = new Response(entries, "SUCCESS");
            status = HttpStatus.OK;
        } else { //else don't send entries and send no entry response
            response = new Response(null, "NO ENTRIES");
            status = HttpStatus.NO_CONTENT;
        }
        return new ResponseEntity<>(response, status);
    }

    @DeleteMapping("/entry/delete/{id}")
    public ResponseEntity<Response> deleteEntry(@PathVariable("id") long id) { //function to delete a journal entry
        Response response;
        HttpStatus status;
        if (entryRepository.existsById(id)) { //check that journal entry ID exists
            Entry entry = entryRepository.findById(id); //getting a Journal Object from the id.

            entryRepository.delete(entry); //deleting the entry
            response = new Response(null, "DELETED"); //creating success response
            status = HttpStatus.OK;
        } else { //else don't delete
            response = new Response(null, "COULDN'T DELETE"); //creating not found response
            status = HttpStatus.NOT_FOUND;
        }
        return new ResponseEntity<>(response, status);
    }


    @PostMapping("/entry/create")
    public ResponseEntity<Response> addEntry(@RequestBody Entry entry, Principal principal) throws Exception { //function to create a new journal entry
        if (principal != null) { //ensure that user is logged in.
            String authUsername = principal.getName(); // Retrieves the logged-in username
            Optional<JournalUser> optionalJournalUser = userService.findByUsername(authUsername); //Getting JournalUser Object from username
            if (optionalJournalUser.isPresent()) { //Encrypt entries using user password.
                String msg = entry.getText();
                String img = entry.getImg();
                String masterKey = optionalJournalUser.get().getPass(); //encrypting image and journal text
                String encodedMsg = GCM.encrypt(masterKey, msg);
                String encodedImg = GCM.encrypt(masterKey, img);

                entry.setUserID(optionalJournalUser.get().getId()); // Set the user ID to the entry
                entry.setText(encodedMsg);// Save encoded text
                entry.setImg(encodedImg);

                Entry createdEntry = entryRepository.save(entry); //save the entry in the database
                Response response = new Response(createdEntry, "CREATED"); //create response message
                return new ResponseEntity<>(response, HttpStatus.CREATED);
            }
        }


        Response response = new Response(null, "USER NOT FOUND OR UNAUTHORIZED");    // If user not found or principal is null, return error response
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @PatchMapping("/entry/update")
    public ResponseEntity<Response> updateEntry(@RequestBody Entry entry, Principal principal) throws Exception { //function to update an existing entry
        Response response;
        HttpStatus httpStatus;
        String authUsername ="";
        if (principal != null) {
            authUsername = principal.getName(); // Retrieves the logged-in username
        }
        Optional<JournalUser> optionalBlogUser = userService.findByUsername(authUsername); //Getting JournalUser Object from username

        if (entryRepository.existsById(entry.getId())) { //checking if the entry exists in the DB
            String msg = entry.getText();
            String img = entry.getImg();
            String masterKey = optionalBlogUser.get().getPass(); //encrypting the newly modified text
            String decodedMsg = GCM.encrypt(masterKey, msg);
            String decodedImage = GCM.encrypt(masterKey, img);

            entry.setText(decodedMsg);
            entry.setImg(decodedImage);
            entry.setUserID(optionalBlogUser.get().getId());
            Entry modifiedEntry = entryRepository.save(entry); //saved the modified entry in the DB

            response = new Response(modifiedEntry, "MODIFIED"); //create response message
            httpStatus = HttpStatus.OK;
        } else { //if not found send entry not found response
            response = new Response(null, "ENTRY DOESN'T EXIST");
            httpStatus = HttpStatus.NOT_FOUND;
        }
        return new ResponseEntity<>(response, httpStatus);
    }
    /**
     * Download an entry in json form
     * @param long id
     * @return ResponseEntity with entry in JSON form.
     */
    @RequestMapping("/entry/download/{id}")
    public void downloadDocument(@PathVariable long id,HttpServletResponse response, Principal principal) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper(); //create jackson object mapper to convert object to json

        Entry entryD = entryRepository.findById(id); //find entry in the DB

        String authUsername ="";
        if (principal != null) {
            authUsername = principal.getName(); // Retrieves the logged-in username
        }

        Optional<JournalUser> optionalJournalUser = userService.findByUsername(authUsername); //Getting JournalUser Object from username


        String masterKey = optionalJournalUser.get().getPass();
        entryD.setText(GCM.decrypt(entryD.getText(),masterKey)); //decrypt the text before sending to download
        entryD.setImg(GCM.decrypt(entryD.getImg(),masterKey));


        Entry insertedDownload = new Entry();
        insertedDownload.setText(entryD.getText());
        insertedDownload.setDate(entryD.getDate()); //creating new entry to remove user id field to avoid issues.
        insertedDownload.setImg(entryD.getImg());

        String entryInJson = objectMapper.writeValueAsString(insertedDownload); //mapping the object as a json string

        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Transfer-Encoding", "binary");
        response.setContentType("application/json"); //set response attributes
        response.setContentLength( entryInJson.length());
        response.setHeader("Content-Disposition", "attachment; filename=\"JournalEntry" + entryD.getId() + ".json\""); //set file name and type

        IOUtils.copy(IOUtils.toInputStream(entryInJson), response.getOutputStream());    // copies the content of your string to the output stream
        response.flushBuffer();
    }
}
