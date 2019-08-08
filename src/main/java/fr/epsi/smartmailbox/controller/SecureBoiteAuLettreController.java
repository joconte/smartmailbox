package fr.epsi.smartmailbox.controller;


import fr.epsi.smartmailbox.func.Func;
import fr.epsi.smartmailbox.model.BoiteAuLettre;
import fr.epsi.smartmailbox.model.GenericObjectWithErrorModel;
import fr.epsi.smartmailbox.model.Sent.BoiteAuLettreSent;
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

@Api( description="API sécurisée pour effectuer des actions sur les boites au lettre, il faut un token d'authentification.")
@RestController
@RequestMapping(Func.routeSecureBoiteAuLettreController)
public class SecureBoiteAuLettreController {

    @Autowired
    private UtilisateurRepository userService;

    @Autowired
    private BoiteAuLettreRepository boiteAuLettreRepository;

    @Autowired
    private BoiteAuLettreService boiteAuLettreService;

    @ApiOperation(value = "Permet de créer une boite au lettre, il faut etre connecté en administrateur.")
    @PostMapping
    public GenericObjectWithErrorModel<BoiteAuLettre> postBoiteAuLettre(@RequestHeader("Authorization") String token, @RequestBody BoiteAuLettre boiteAuLettre)
    {
        String username = Jwts.parser().setSigningKey("secretkey").parseClaimsJws(token.split(" ")[1]).getBody().getSubject();
        GenericObjectWithErrorModel<BoiteAuLettre> boiteAuLettreGenericObjectWithErrorModel = new GenericObjectWithErrorModel<>();
        Dictionary<String, List<String>> dictionary = new Hashtable<>();
        if(userService.findByEmail(username).getRole()!= Utilisateur.Role.Admin)
        {
            List<String> strings = new ArrayList<>();
            strings.add("Vous n'etes pas autorisé à créer des boites au lettre");
            dictionary.put("Authorization",strings);
            boiteAuLettreGenericObjectWithErrorModel.setErrors(dictionary);
        }
        else if(boiteAuLettreRepository.findByNumeroSerie(boiteAuLettre.getNumeroSerie())==null)
        {
            String randomToken = Func.randomAlphaNumeric(50);
            while(boiteAuLettreRepository.findByToken(randomToken).size()>0)
            {
                randomToken = Func.randomAlphaNumeric(50);
            }
            boiteAuLettre.setToken(randomToken);
            boiteAuLettreGenericObjectWithErrorModel.setT(boiteAuLettreRepository.save(boiteAuLettre));
        }
        else
        {
            List<String> strings = new ArrayList<>();
            strings.add("La boite au lettre existe déjà");
            dictionary.put("Erreur",strings);
            boiteAuLettreGenericObjectWithErrorModel.setErrors(dictionary);
        }
        return boiteAuLettreGenericObjectWithErrorModel;
    }

    @ApiOperation(value = "Permet de récupérer le token d'une boite au lettre, il faut etre connecté en administrateur.")
    @GetMapping(Func.routeSecureBoiteAuLettreControllerGetTokenByNumSerie)
    public String getBALToken(@RequestHeader("Authorization") String token,@PathVariable String numeroSerie)
    {
        String username = Jwts.parser().setSigningKey("secretkey").parseClaimsJws(token.split(" ")[1]).getBody().getSubject();
        Utilisateur userFoundInDb = userService.findByEmail(username);
        BoiteAuLettre boiteAuLettreFoundIndb = boiteAuLettreRepository.findByNumeroSerie(numeroSerie);
        String baltoken="";
        if(userFoundInDb!=null && userFoundInDb.getRole()== Utilisateur.Role.Admin && boiteAuLettreFoundIndb!=null)
        {
            baltoken = boiteAuLettreFoundIndb.getToken();
        }
        return baltoken;
    }

    @ApiOperation(value = "Allow to get all mailboxs, admin rights are needed.")
    @GetMapping(Func.routeSecureBoiteAuLettreControllerGetAll)
    public Object getAllBAL(@RequestHeader("Authorization") String token)
    {
        Dictionary<String, List<String>> dictionary = new Hashtable<>();
        String username = Jwts.parser().setSigningKey("secretkey").parseClaimsJws(token.split(" ")[1]).getBody().getSubject();
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

}
