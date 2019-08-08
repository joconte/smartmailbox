package fr.epsi.smartmailbox.service;

import fr.epsi.smartmailbox.model.BoiteAuLettre;
import fr.epsi.smartmailbox.model.Sent.BoiteAuLettreSent;
import fr.epsi.smartmailbox.model.Utilisateur;
import fr.epsi.smartmailbox.repository.BoiteAuLettreRepository;
import fr.epsi.smartmailbox.repository.UtilisateurRepository;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

@Service
public class BoiteAuLettreService {

    @Autowired
    private UtilisateurRepository userService;

    @Autowired
    private BoiteAuLettreRepository boiteAuLettreRepository;

    public Object getMailboxById(String token, Long idMailBox){
        Dictionary<String, List<String>> dictionary = new Hashtable<>();
        String username = Jwts.parser().setSigningKey("secretkey").parseClaimsJws(token.split(" ")[1]).getBody().getSubject();
        Utilisateur userFoundInDb = userService.findByEmail(username);
        Object objToReturn;
        // If Admin, we allow to get any mailbox
        if(userFoundInDb!=null && userFoundInDb.getRole()== Utilisateur.Role.Admin) {
            BoiteAuLettre boiteAuLettre = boiteAuLettreRepository.findOne(idMailBox);
            if (boiteAuLettre!= null) {
                objToReturn = new BoiteAuLettreSent(boiteAuLettre);
            }
            else {
                List<String> strings = new ArrayList<>();
                strings.add("No mailbox found with id : " + idMailBox);
                dictionary.put("error",strings);
                objToReturn = dictionary;
            }
        }
        // if not admin, we need to check if the mailbox belong to the user
        else {
            List<BoiteAuLettre> userBAL = userFoundInDb.getBoiteAuLettres();
            BoiteAuLettre boiteAuLettre = boiteAuLettreRepository.findOne(idMailBox);
            if(userBAL.contains(boiteAuLettre)) {
                objToReturn = new BoiteAuLettreSent(boiteAuLettre);
            }
            else {
                List<String> strings = new ArrayList<>();
                strings.add("No mailbox found with id : " + idMailBox);
                dictionary.put("error",strings);
                objToReturn = dictionary;
            }
        }
        return objToReturn;
    }
}
