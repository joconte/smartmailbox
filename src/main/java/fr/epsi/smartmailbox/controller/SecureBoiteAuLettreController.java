package fr.epsi.smartmailbox.controller;


import fr.epsi.smartmailbox.func.Func;
import fr.epsi.smartmailbox.model.BoiteAuLettre;
import fr.epsi.smartmailbox.model.GenericObjectWithErrorModel;
import fr.epsi.smartmailbox.model.Received.BoiteAuLettrePost;
import fr.epsi.smartmailbox.model.Sent.BoiteAuLettreSent;
import fr.epsi.smartmailbox.model.Sent.BoiteAuLettreToken;
import fr.epsi.smartmailbox.model.Utilisateur;
import fr.epsi.smartmailbox.repository.BoiteAuLettreRepository;
import fr.epsi.smartmailbox.repository.UtilisateurRepository;
import fr.epsi.smartmailbox.service.BoiteAuLettreService;
import io.jsonwebtoken.Jwts;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

@Api( description="Secured API to manage mailboxs. Bearer token is needed.")
@RestController
@RequestMapping(Func.routeSecureBoiteAuLettreController)
public class SecureBoiteAuLettreController {

    @Autowired
    private UtilisateurRepository userService;

    @Autowired
    private BoiteAuLettreRepository boiteAuLettreRepository;

    @Autowired
    private BoiteAuLettreService boiteAuLettreService;

    @ApiOperation(value = "Allow to create mailboxs, admin rights needed.")
    @PostMapping
    public Object postBoiteAuLettre(@RequestHeader("Authorization") String token, @RequestBody BoiteAuLettrePost boiteAuLettrePost)
    {
        String username = Func.getUserNameByToken(token);
        Dictionary<String, List<String>> dictionary = new Hashtable<>();
        Object objToReturn;
        if(userService.findByEmail(username).getRole()!= Utilisateur.Role.Admin)
        {
            List<String> strings = new ArrayList<>();
            strings.add("Admin rights needed");
            dictionary.put("Authorization",strings);
            objToReturn = dictionary;
        }
        else if(boiteAuLettreRepository.findByNumeroSerie(boiteAuLettrePost.getNumeroSerie())==null)
        {
            String randomToken = Func.randomAlphaNumeric(50);
            while(boiteAuLettreRepository.findByToken(randomToken).size()>0)
            {
                randomToken = Func.randomAlphaNumeric(50);
            }
            BoiteAuLettre boiteAuLettre = new BoiteAuLettre(boiteAuLettrePost);
            boiteAuLettre.setToken(randomToken);
            boiteAuLettre = boiteAuLettreRepository.save(boiteAuLettre);
            BoiteAuLettreSent boiteAuLettreSent = new BoiteAuLettreSent(boiteAuLettre);
            objToReturn = boiteAuLettreSent;
        }
        else
        {
            List<String> strings = new ArrayList<>();
            strings.add("Mailbox with the same number already exists.");
            dictionary.put("Error",strings);
            objToReturn = dictionary;
        }
        return objToReturn;
    }

    @ApiOperation(value = "Allow to get mailbox token by mailbox serial number. Admin right are needed.")
    @GetMapping(Func.routeSecureBoiteAuLettreControllerGetTokenByNumSerie)
    public Object getBALToken(@RequestHeader("Authorization") String token,@PathVariable String serialNumber)
    {
        Dictionary<String, List<String>> dictionary = new Hashtable<>();
        String username = Func.getUserNameByToken(token);
        Utilisateur userFoundInDb = userService.findByEmail(username);
        BoiteAuLettre boiteAuLettreFoundIndb = boiteAuLettreRepository.findByNumeroSerie(serialNumber);
        Object objToReturn;
        if(userFoundInDb!=null && userFoundInDb.getRole()== Utilisateur.Role.Admin)
        {
            if(boiteAuLettreFoundIndb!=null) {
                objToReturn = new BoiteAuLettreToken(boiteAuLettreFoundIndb.getToken());
            }
            else {
                List<String> strings = new ArrayList<>();
                strings.add("Mailbox not found.");
                dictionary.put("Error",strings);
                objToReturn = dictionary;
            }
        }
        else {
            List<String> strings = new ArrayList<>();
            strings.add("Admin rights are needed.");
            dictionary.put("Authorisation",strings);
            objToReturn = dictionary;
        }
        return objToReturn;
    }

    @ApiOperation(value = "Allow to get all mailboxs, admin rights are needed.")
    @GetMapping(Func.routeSecureBoiteAuLettreControllerGetAll)
    public Object getAllBAL(@RequestHeader("Authorization") String token)
    {
        Dictionary<String, List<String>> dictionary = new Hashtable<>();
        String username = Func.getUserNameByToken(token);
        Utilisateur userFoundInDb = userService.findByEmail(username);
        Object objToReturn;
        if(userFoundInDb!=null && userFoundInDb.getRole()== Utilisateur.Role.Admin)
        {
            List<BoiteAuLettre> boiteAuLettres = boiteAuLettreRepository.findAll();
            List<BoiteAuLettreSent> boiteAuLettreSents = new ArrayList<>();
            for(BoiteAuLettre boiteAuLettre : boiteAuLettres) {
                boiteAuLettreSents.add(new BoiteAuLettreSent(boiteAuLettre));
            }
            objToReturn = boiteAuLettreSents;
        }
        else
        {
            List<String> strings = new ArrayList<>();
            strings.add("Admin rights are needed.");
            dictionary.put("Authorisation",strings);
            objToReturn = dictionary;
        }
        return objToReturn;
    }

    @ApiOperation(value = "Allow to get a mailbox by id.")
    @GetMapping(Func.routeSecureBoiteAuLettreControllerGetMailBoxById)
    public Object getMailboxById(@RequestHeader("Authorization") String token, @PathVariable Long idMailBox){
        return boiteAuLettreService.getMailboxById(token,idMailBox);
    }

    @ApiOperation(value = "Allow to get connected user mailboxs")
    @GetMapping()
    public Object getMailboxs(@RequestHeader("Authorization") String token) {
        Dictionary<String, List<String>> dictionary = new Hashtable<>();
        String username = Func.getUserNameByToken(token);
        Utilisateur userFoundInDb = userService.findByEmail(username);
        Object objToReturn;
        List<BoiteAuLettre> boiteAuLettres = boiteAuLettreRepository.findAll();
        List<BoiteAuLettreSent> boiteAuLettreSents = new ArrayList<>();
        for(BoiteAuLettre boiteAuLettre : boiteAuLettres) {
            if(userFoundInDb.getBoiteAuLettres().contains(boiteAuLettre)) {
                boiteAuLettreSents.add(new BoiteAuLettreSent(boiteAuLettre));
            }
        }
        return boiteAuLettreSents;
    }

}
