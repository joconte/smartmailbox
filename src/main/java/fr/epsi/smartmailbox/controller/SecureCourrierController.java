package fr.epsi.smartmailbox.controller;

import fr.epsi.smartmailbox.model.Courrier;
import fr.epsi.smartmailbox.model.GenericObjectWithErrorModel;
import fr.epsi.smartmailbox.model.Utilisateur;
import fr.epsi.smartmailbox.repository.CourrierRepository;
import fr.epsi.smartmailbox.repository.UtilisateurRepository;
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
@RequestMapping("/secure/courrier")
public class SecureCourrierController {

    @Autowired
    private CourrierRepository courrierRepository;

    @Autowired
    private UtilisateurRepository userService;

    @ApiOperation(value = "Permet de mettre à jour un courrier par id à 'Vu'")
    @PutMapping("/{id}")
    public GenericObjectWithErrorModel<Courrier> updateCourrierVu(@RequestHeader("Authorization") String token, @PathVariable Long id)
    {
        GenericObjectWithErrorModel<Courrier> utilisateurGenericObjectWithErrorModel = new GenericObjectWithErrorModel<>();
        Dictionary<String, List<String>> dictionary = new Hashtable<>();
        String username = Jwts.parser().setSigningKey("secretkey").parseClaimsJws(token.split(" ")[1]).getBody().getSubject();

        Utilisateur userFoundInDb = userService.findByEmail(username);

        if(userFoundInDb==null)
        {
            List<String> strings = new ArrayList<>();
            strings.add("L'utilisateur n'a pas été trouvé en base.");
            dictionary.put("Utilisateur",strings);
            utilisateurGenericObjectWithErrorModel.setErrors(dictionary);
        }
        else
        {
            Courrier courrierFoundInDb = courrierRepository.findOne(id);

            if(courrierFoundInDb==null)
            {
                List<String> strings = new ArrayList<>();
                strings.add("Le courrier n'a pas été trouvé en base.");
                dictionary.put("Courrier",strings);
                utilisateurGenericObjectWithErrorModel.setErrors(dictionary);
            }
            else
            {
                courrierFoundInDb.setVu(true);
                utilisateurGenericObjectWithErrorModel.setT(courrierRepository.save(courrierFoundInDb));
            }
        }
        return utilisateurGenericObjectWithErrorModel;
    }

}
