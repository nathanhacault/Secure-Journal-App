package secureJournal.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import secureJournal.GCM;
import secureJournal.model.JournalUser;
import secureJournal.model.Entry;
import secureJournal.repository.EntryRepository;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import secureJournal.service.UserService;


@Controller
public class WebController { //controller for the login and index(home) page

    private final EntryRepository entryRepository;
    private final UserService userService; //defining service and repo for use in class

    @Autowired
    public WebController(EntryRepository entryRepository,UserService userService) { //constructor
        this.entryRepository = entryRepository;
        this.userService = userService;
    }

    @GetMapping("/index")
    public String index(Model model,Principal principal) throws Exception { //index map used to show the main page
        String authUsername = null;
        if (principal != null) { //Checking if the user is logged in

            authUsername = principal.getName(); // Retrieves the logged-in username

            Optional<JournalUser> optionalJournalUser = userService.findByUsername(authUsername); //Getting JournalUser Object from username

            if(optionalJournalUser.isPresent()) { //ensuring the user exists

                Long userId = optionalJournalUser.get().getId(); //getting the user ID of the logged in user
                List<Entry> entries = entryRepository.findEntriesByUserIDOrderByDateDesc(userId); //finding the entries tied to that user id.

                for(Entry e : entries){ //iterate thourgh the entry list to decrypt entries  using user password.
                    String msg = e.getText();
                    String img = e.getImg();
                    String masterKey = optionalJournalUser.get().getPass(); //decrypting image and journal text
                    String decodedMsg = GCM.decrypt(msg, masterKey);
                    String decodeImg = GCM.decrypt(img, masterKey);

                    e.setText(decodedMsg); //modifying the entry with the decrypted text
                    e.setImg(decodeImg);
                }

                model.addAttribute("entries", entries); //creating a model to pass to the index page to use to load entries on start-up
            }
        }
        return "index";
    }

    @GetMapping("/") // Handles GET requests to "/", redirects to login.
    public String redirect() {
            return "login"; // Renders the login view if the user is not logged in
    }
    @GetMapping("/login") // Handles GET requests to "/login"
    public String login(Principal principal) {
        if (principal != null) {
            return "redirect:/index"; // Redirects to the root context if the user is already logged in
        } else {
            return "login"; // Renders the login view if the user is not logged in
        }
    }

}

