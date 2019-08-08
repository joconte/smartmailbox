package fr.epsi.smartmailbox.controller;

import fr.epsi.smartmailbox.func.Func;
import fr.epsi.smartmailbox.model.Courrier;
import fr.epsi.smartmailbox.model.GenericObjectWithErrorModel;
import fr.epsi.smartmailbox.model.Sent.CourrierSent;
import fr.epsi.smartmailbox.model.Utilisateur;
import fr.epsi.smartmailbox.repository.CourrierRepository;
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

@Api( description="API sécurisée pour éffectuer des actions sur les courriers, il faut un token d'authentification.")
@RestController
@RequestMapping(Func.routeSecureCourrierController)
public class SecureCourrierController {

    @Autowired
    private CourrierRepository courrierRepository;

    @Autowired
    private UtilisateurRepository userService;

    @Autowired
    private BoiteAuLettreService boiteAuLettreService;

    @ApiOperation(value = "Allow to indicate a mail is 'seen'.")
    @PutMapping("/{id}")
    public Object updateCourrierVu(@RequestHeader("Authorization") String token, @PathVariable Long id)
    {
        Dictionary<String, List<String>> dictionary = new Hashtable<>();
        String username = Func.getUserNameByToken(token);
        Utilisateur userFoundInDb = userService.findByEmail(username);
        Object objToReturn;
        if(userFoundInDb==null)
        {
            List<String> strings = new ArrayList<>();
            strings.add("User not found.");
            dictionary.put("User",strings);
            objToReturn = dictionary;
        }
        else
        {
            Courrier courrierFoundInDb = courrierRepository.findOne(id);
            if(courrierFoundInDb==null)
            {
                List<String> strings = new ArrayList<>();
                strings.add("Le courrier n'a pas été trouvé en base.");
                dictionary.put("Courrier",strings);
                objToReturn = dictionary;
            }
            else
            {
                courrierFoundInDb.setVu(true);
                objToReturn = new CourrierSent(courrierFoundInDb);
            }
        }
        return objToReturn;
    }

    @ApiOperation(value = "Allow to get mails by mailbox id")
    @GetMapping(Func.routeSecureCourrierControllerGetMailByMailBoxId)
    public Object getMailByMailBoxId(@RequestHeader("Authorization") String token, @PathVariable Long idMailBox) {
        Object obj = boiteAuLettreService.getMailboxById(token,idMailBox);
        if(obj instanceof Dictionary) {
            return obj;
        }
        List<Courrier> courriers = courrierRepository.findAll();
        List<CourrierSent> courrierSents = new ArrayList<>();
        for(Courrier courrier : courriers) {
            if(courrier.getBoiteAuLettre().getId()==idMailBox){
                courrierSents.add(new CourrierSent(courrier));
            }
        }
        return courrierSents;
    }
}
