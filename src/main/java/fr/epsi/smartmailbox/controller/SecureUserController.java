package fr.epsi.smartmailbox.controller;

import fr.epsi.smartmailbox.component.EmailServiceImpl;
import fr.epsi.smartmailbox.func.Func;
import fr.epsi.smartmailbox.model.VerificationToken;
import fr.epsi.smartmailbox.repository.VerificationTokenRepository;
import io.jsonwebtoken.Jwts;
import fr.epsi.smartmailbox.model.BoiteAuLettre;
import fr.epsi.smartmailbox.model.GenericObjectWithErrorModel;
import fr.epsi.smartmailbox.model.Utilisateur;
import fr.epsi.smartmailbox.repository.BoiteAuLettreRepository;
import fr.epsi.smartmailbox.repository.UtilisateurRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
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

	@Autowired
	private VerificationTokenRepository verificationTokenRepository;

	@Autowired
	EmailServiceImpl emailService;

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

	@ApiOperation(value = "Permet de modifier les informations de l'utilisateur connecté.")
	@PutMapping
	public GenericObjectWithErrorModel<Utilisateur> updateUtilisateur(@RequestHeader("Authorization") String token, @RequestBody Utilisateur user) throws NoSuchProviderException, NoSuchAlgorithmException {
		String username = Jwts.parser().setSigningKey("secretkey").parseClaimsJws(token.split(" ")[1]).getBody().getSubject();
		GenericObjectWithErrorModel<Utilisateur> utilisateurGenericObjectWithErrorModel = new GenericObjectWithErrorModel<>();
		Dictionary<String, List<String>> dictionary = new Hashtable<>();
		Utilisateur userFoundInDb = userService.findByEmail(username);

		if(userFoundInDb!=null)
		{
			dictionary = UserValidationForUpdate(user,userFoundInDb.getEmail());
			if(dictionary.isEmpty())
			{
				userFoundInDb.setFirstName(user.getFirstName());
				userFoundInDb.setLastName(user.getLastName());
				boolean needValidationByEmail = false;
				if(!userFoundInDb.getEmail().equals(user.getEmail()))
				{
					userFoundInDb.setEmail(user.getEmail());
					needValidationByEmail=true;
				}

				if(user.getPassword()!=null && user.getPassword()!="")
				{
					byte[] salt = Func.getSalt();
					userFoundInDb.setSalt(salt);
					userFoundInDb.setPassword(Func.getSecurePassword(user.getPassword(), salt));
				}
				Utilisateur userSaved = userService.save(userFoundInDb);
				utilisateurGenericObjectWithErrorModel.setT(userSaved);
				if(needValidationByEmail)
				{
					byte[] randomToken = Func.getSalt();
					VerificationToken verificationToken = new VerificationToken();
					verificationToken.setToken(randomToken.toString());
					verificationToken.setUser(userSaved);
					VerificationToken verificationTokenSaved = verificationTokenRepository.save(verificationToken);
					emailService.sendSimpleMessage(userSaved.getEmail(),"Token de vérification", Func.siteAdresse + "/user/verify/"+ verificationTokenSaved.getToken());

				}
			}
		}
		else
		{
			List<String> strings = new ArrayList<>();
			strings.add("Utilisateur non trouvé");
			dictionary.put("Utilisateur",strings);
		}
		utilisateurGenericObjectWithErrorModel.setErrors(dictionary);
		return utilisateurGenericObjectWithErrorModel;
	}

	public Dictionary<String, List<String>> UserValidationForUpdate(Utilisateur user, String email)
	{
		Dictionary<String, List<String>> dictionary = new Hashtable<>();
		if(user.getFirstName().isEmpty())
		{
			List<String> strings = new ArrayList<>();
			strings.add("Le prénom est obligatoire");
			dictionary.put("firstName",strings);
		}
		if(user.getLastName().isEmpty())
		{
			List<String> strings = new ArrayList<>();
			strings.add("Le nom est obligatoire");
			dictionary.put("lastName",strings);
		}
		if(!Func.isValidEmail(user.getEmail()))
		{
			List<String> strings = new ArrayList<>();
			strings.add("L'adresse email n'est pas valide");
			dictionary.put("email",strings);
		}
		Utilisateur utilisateurInDb = userService.findByEmail(user.getEmail());
		if(utilisateurInDb!=null && utilisateurInDb.isEnabled() && utilisateurInDb.getEmail()!=email)
		{
			List<String> strings = new ArrayList<>();
			strings.add("L'adresse email est déjà utilisée");
			dictionary.put("email",strings);
		}
		else if(utilisateurInDb!=null && !utilisateurInDb.isEnabled())
		{
			userService.delete(utilisateurInDb);
		}
		return dictionary;
	}

}
