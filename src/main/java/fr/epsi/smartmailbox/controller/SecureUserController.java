package fr.epsi.smartmailbox.controller;

import io.jsonwebtoken.Jwts;
import fr.epsi.smartmailbox.model.BoiteAuLettre;
import fr.epsi.smartmailbox.model.GenericObjectWithErrorModel;
import fr.epsi.smartmailbox.model.Utilisateur;
import fr.epsi.smartmailbox.repository.BoiteAuLettreRepository;
import fr.epsi.smartmailbox.repository.UtilisateurRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;


@Api( description="API sécurisée pour éffectuer des actions sur l'utilisateur, il faut un token d'authentification.")
@RestController
@RequestMapping("/secure/user")
public class SecureUserController {

	@Autowired
	private UtilisateurRepository userService;

	@Autowired
	private BoiteAuLettreRepository boiteAuLettreRepository;

	@ApiOperation(value = "Permet de vérifier que le token de connexion est bien valide, facultatif.")
	@GetMapping("/token")
	public String loginSuccess() {
		return "Login Successful!";
	}

	@ApiOperation(value = "Permet de récupérer la liste de tous les utilisateurs de l'application, uniquement possible en possédant un token d'un compte Admin.")
	@GetMapping
	public GenericObjectWithErrorModel<List<Utilisateur>> getAllUser(@RequestHeader("Authorization") String token)
	{
		String username = Jwts.parser().setSigningKey("secretkey").parseClaimsJws(token.split(" ")[1]).getBody().getSubject();
		GenericObjectWithErrorModel<List<Utilisateur>> listGenericObjectWithErrorModel = new GenericObjectWithErrorModel<>();
		Dictionary<String, List<String>> dictionary = new Hashtable<>();
		Utilisateur userFoundInDb = userService.findByEmail(username);
		if(userFoundInDb.getRole()== Utilisateur.Role.Admin)
		{
			listGenericObjectWithErrorModel.setT(userService.findAll());
		}
		else
		{
			List<String> strings = new ArrayList<>();
			strings.add("Vous n'etes pas autorisé à utiliser cette fonctionnalité");
			dictionary.put("Autorisation",strings);
			listGenericObjectWithErrorModel.setErrors(dictionary);
		}
		return listGenericObjectWithErrorModel;
	}

	@ApiOperation(value = "Permet de supprimer le compte de l'utilisateur connecter.")
	@DeleteMapping
	public String deleteUser(@RequestHeader("Authorization") String token)
	{
		String username = Jwts.parser().setSigningKey("secretkey").parseClaimsJws(token.split(" ")[1]).getBody().getSubject();
		Utilisateur userFoundInDb = userService.findByEmail(username);
		if(userFoundInDb==null)
		{
			return "L'utilisateur n'a pas été trouvé en base.";
		}
		else
		{
			userService.delete(userFoundInDb);
			return "L'utilisateur a été supprimé";
		}
	}

	@ApiOperation(value = "Permet d'ajouter une boite au lettre à l'utilisateur connecté.")
	@PostMapping("/addBAL")
	public GenericObjectWithErrorModel<Utilisateur> enregistreBoiteAuLettre(@RequestHeader("Authorization") String token, @RequestBody BoiteAuLettre boiteAuLettre)
	{
		String username = Jwts.parser().setSigningKey("secretkey").parseClaimsJws(token.split(" ")[1]).getBody().getSubject();
		GenericObjectWithErrorModel<Utilisateur> utilisateurGenericObjectWithErrorModel = new GenericObjectWithErrorModel<>();
		Dictionary<String, List<String>> dictionary = new Hashtable<>();
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
			if(boiteAuLettreRepository.findByNumeroSerie(boiteAuLettre.getNumeroSerie())==null)
			{
				List<String> strings = new ArrayList<>();
				strings.add("La boite au lettre n'existe pas !");
				dictionary.put("BoiteAuLettre",strings);
				utilisateurGenericObjectWithErrorModel.setErrors(dictionary);
			}
			else if(userService.findBoiteAuLettreIfTaken(boiteAuLettre.getNumeroSerie())!=null)
			{
				List<String> strings = new ArrayList<>();
				strings.add("La boite au lettre est déja prise !");
				dictionary.put("BoiteAuLettre",strings);
				utilisateurGenericObjectWithErrorModel.setErrors(dictionary);
			}
			else
			{
				BoiteAuLettre boiteAuLettrefoundInDb = boiteAuLettreRepository.findByNumeroSerie(boiteAuLettre.getNumeroSerie());
				boiteAuLettrefoundInDb.setDescription(boiteAuLettre.getDescription());
				BoiteAuLettre boiteAuLettresavedInDb = boiteAuLettreRepository.save(boiteAuLettrefoundInDb);
				userFoundInDb.addBoiteAuLettre(boiteAuLettresavedInDb);
				utilisateurGenericObjectWithErrorModel.setT(userService.save(userFoundInDb));
			}
		}
		return utilisateurGenericObjectWithErrorModel;
	}

	@ApiOperation(value = "Permet de récupérer les informations de l'utilisateur connecté.")
	@RequestMapping("/me")
	@GetMapping
	public GenericObjectWithErrorModel<Utilisateur> getConnectedUser(@RequestHeader("Authorization") String token)
	{
		GenericObjectWithErrorModel<Utilisateur> utilisateurGenericObjectWithErrorModel = new GenericObjectWithErrorModel<>();
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
			utilisateurGenericObjectWithErrorModel.setT(userFoundInDb);
		}
		return utilisateurGenericObjectWithErrorModel;
	}

}
