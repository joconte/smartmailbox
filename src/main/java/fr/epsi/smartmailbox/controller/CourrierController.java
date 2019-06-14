package fr.epsi.smartmailbox.controller;


import fr.epsi.smartmailbox.model.BoiteAuLettre;
import fr.epsi.smartmailbox.model.Courrier;
import fr.epsi.smartmailbox.model.GenericObjectWithErrorModel;
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
@RequestMapping("/courrier")
public class CourrierController {

    @Autowired
    private BoiteAuLettreRepository boiteAuLettreRepository;

    @Autowired
    private CourrierRepository courrierRepository;

    @ApiOperation(value = "Permet de créer un courrier")
    @PostMapping
    public GenericObjectWithErrorModel<BoiteAuLettre> postCourrier(@RequestBody Courrier courrier)
    {
        GenericObjectWithErrorModel<BoiteAuLettre> courrierGenericObjectWithErrorModel = new GenericObjectWithErrorModel<>();
        Dictionary<String, List<String>> dictionary = new Hashtable<>();
        if(boiteAuLettreRepository.findByToken(courrier.getBoiteAuLettre().getToken()).isEmpty())
        {
            courrier.setDateReception(Calendar.getInstance().getTime());
            courrier.setVu(false);
            BoiteAuLettre boiteAuLettreFoundInDb = boiteAuLettreRepository.findByToken(courrier.getBoiteAuLettre().getToken()).get(0);
            courrier.setBoiteAuLettre(boiteAuLettreFoundInDb);
            Courrier courrierSaved = courrierRepository.save(courrier);
            boiteAuLettreFoundInDb.addCourrier(courrierSaved);
            boiteAuLettreFoundInDb.setLastActivity(courrierSaved.getDateReception());
            courrierGenericObjectWithErrorModel.setT(boiteAuLettreRepository.save(boiteAuLettreFoundInDb));
        }
        else
        {
            List<String> strings = new ArrayList<>();
            strings.add("La boite au lettre n'est pas reconnue");
            dictionary.put("BoiteAuLettre",strings);
            courrierGenericObjectWithErrorModel.setErrors(dictionary);
        }
        return courrierGenericObjectWithErrorModel;
    }
}
