package fr.epsi.smartmailbox.controller;


import fr.epsi.smartmailbox.func.Func;
import fr.epsi.smartmailbox.model.BoiteAuLettre;
import fr.epsi.smartmailbox.model.Courrier;
import fr.epsi.smartmailbox.model.GenericObjectWithErrorModel;
import fr.epsi.smartmailbox.model.Received.CourrierPost;
import fr.epsi.smartmailbox.model.Sent.CourrierSent;
import fr.epsi.smartmailbox.repository.BoiteAuLettreRepository;
import fr.epsi.smartmailbox.repository.CourrierRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Api( description="API publique pour effectuer des actions sur les courriers.")
@CrossOrigin
@RestController
@RequestMapping(Func.routeCourrierController)
public class CourrierController {

    @Autowired
    private BoiteAuLettreRepository boiteAuLettreRepository;

    @Autowired
    private CourrierRepository courrierRepository;

    @ApiOperation(value = "Permet de créer un courrier")
    @PostMapping
    public Object postCourrier(@RequestBody CourrierPost courrierPost)
    {
        Dictionary<String, List<String>> dictionary = new Hashtable<>();
        Object objToReturn;
        if(boiteAuLettreRepository.findByToken(courrierPost.getToken())!=null)
        {
            Courrier courrier = new Courrier();
            courrier.setDateReception(Calendar.getInstance().getTime());
            courrier.setVu(false);
            BoiteAuLettre boiteAuLettreFoundInDb = boiteAuLettreRepository.findByToken(courrierPost.getToken()).get(0);
            courrier.setBoiteAuLettre(boiteAuLettreFoundInDb);
            Courrier courrierSaved = courrierRepository.save(courrier);
            boiteAuLettreFoundInDb.addCourrier(courrierSaved);
            boiteAuLettreFoundInDb.setLastActivity(courrierSaved.getDateReception());
            boiteAuLettreRepository.save(boiteAuLettreFoundInDb);
            Courrier courrierWithBAL = courrierRepository.findOne(courrierSaved.getId());
            objToReturn = new CourrierSent(courrierWithBAL);
        }
        else
        {
            List<String> strings = new ArrayList<>();
            strings.add("La boite au lettre n'est pas reconnue");
            dictionary.put("BoiteAuLettre",strings);
            objToReturn = dictionary;
        }
        return objToReturn;
    }
}
