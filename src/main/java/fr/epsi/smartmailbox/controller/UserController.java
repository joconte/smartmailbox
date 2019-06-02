package fr.epsi.smartmailbox.controller;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import fr.epsi.smartmailbox.component.EmailServiceImpl;
import fr.epsi.smartmailbox.func.Func;
import fr.epsi.smartmailbox.model.GenericObjectWithErrorModel;
import fr.epsi.smartmailbox.model.Utilisateur;
import fr.epsi.smartmailbox.model.VerificationToken;
import fr.epsi.smartmailbox.repository.UtilisateurRepository;
import fr.epsi.smartmailbox.repository.VerificationTokenRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Api( description="API publique, pour se connecter ou s'enregistrer")
@CrossOrigin(origins = "http://localhost", maxAge = 3600)
@RestController
@RequestMapping("/user")
public class UserController {

	private final String siteAdresse = "https://smartmailbox-epsi.herokuapp.com";
	//private final String siteAdresse = "http://192.168.1.17:8080";
	@Autowired
	private UtilisateurRepository userService;

	@Autowired
	private VerificationTokenRepository verificationTokenRepository;

	@Autowired
	EmailServiceImpl emailService;

	@ApiOperation(value = "Permet de s'enregistrer")
	@PostMapping
	public GenericObjectWithErrorModel<Utilisateur> register(@RequestBody Utilisateur user, HttpServletRequest request) throws NoSuchProviderException, NoSuchAlgorithmException {
		GenericObjectWithErrorModel<Utilisateur> userGenericObjectWithErrorModel = new GenericObjectWithErrorModel<>();
		Dictionary<String, List<String>> dictionary = new Hashtable<>();
		dictionary = UserValidation(user);
		if(dictionary.isEmpty())
		{
			byte[] salt = Func.getSalt();
			user.setSalt(salt);
			user.setPassword(Func.getSecurePassword(user.getPassword(), salt));
			user.setRole(Utilisateur.Role.Client);
			Utilisateur userSaved = userService.save(user);
			userGenericObjectWithErrorModel.setT(userSaved);
			byte[] randomToken = Func.getSalt();
			VerificationToken verificationToken = new VerificationToken();
			verificationToken.setToken(randomToken.toString());
			verificationToken.setUser(userSaved);
			VerificationToken verificationTokenSaved = verificationTokenRepository.save(verificationToken);
			emailService.sendSimpleMessage(userSaved.getEmail(),"Token de vérification", siteAdresse + request.getRequestURI() + "/verify/"+ verificationTokenSaved.getToken());
		}
		else
		{
			userGenericObjectWithErrorModel.setErrors(dictionary);
		}
		return userGenericObjectWithErrorModel;
	}

	public Dictionary<String, List<String>> UserValidation(Utilisateur user)
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
		if(user.getPassword().isEmpty())
		{
			List<String> strings = new ArrayList<>();
			strings.add("Le mot de passe est obligatoire");
			dictionary.put("password",strings);
		}
		Utilisateur utilisateurInDb = userService.findByEmail(user.getEmail());
		if(utilisateurInDb!=null && utilisateurInDb.isEnabled())
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

	@ApiOperation(value = "Permet de se connecter, l'API répond par un token de type Bearer qu'il faudra passer dans le header par la suite sur toutes les API sécurisées.")
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public String login(@RequestBody Utilisateur login) throws ServletException {

		String jwtToken = "";

		if (login.getEmail() == null || login.getPassword() == null) {
			throw new ServletException("Please fill in username and password");
		}

		String email = login.getEmail();
		//String password = login.getPassword();
		Utilisateur utilisateur = userService.findByEmail(email);

		if (utilisateur == null) {
			throw new ServletException("Utilisateur email not found.");
		}

		String password = Func.getSecurePassword(login.getPassword(),utilisateur.getSalt());
		String pwd = utilisateur.getPassword();

		if (!password.equals(pwd)) {
			throw new ServletException("Invalid login. Please check your name and password.");
		}

		if(!utilisateur.isEnabled())
		{
			throw new ServletException("L'utilisateur n'est pas activé !");
		}

		jwtToken = Jwts.builder().setSubject(email).claim("roles", "utilisateur").setIssuedAt(new Date())
				.signWith(SignatureAlgorithm.HS256, "secretkey").compact();

		return jwtToken;
	}

	@PostMapping("/init")
	public String Init() throws NoSuchProviderException, NoSuchAlgorithmException {
		String init = "";
		Utilisateur userInDb = userService.findByEmail("admin@contejonathan.net");
		if((userInDb!=null && userInDb.getRole()!= Utilisateur.Role.Admin) || (userInDb!=null && !userInDb.isEnabled()))
		{
			userService.delete(userInDb);
		}

		if(userService.findByEmail("admin@contejonathan.net")==null)
		{
			Utilisateur utilisateur = new Utilisateur();
			utilisateur.setFirstName("Jonathan");
			utilisateur.setLastName("CONTE");
			utilisateur.setEmail("admin@contejonathan.net");
			byte[] salt = Func.getSalt();
			utilisateur.setSalt(salt);
			utilisateur.setPassword(Func.getSecurePassword("mydil34000", salt));
			utilisateur.setRole(Utilisateur.Role.Admin);
			utilisateur.setEnabled(true);
			userService.save(utilisateur);
			init = "user created";
		}
		else
		{
			init = "user already created";
		}
		return init;
	}

	@GetMapping("/verify/{token}")
	public String verifyUser(@PathVariable String token)
	{
		String toreturn = "";
		VerificationToken verificationToken = verificationTokenRepository.findByToken(token);
		if(verificationToken==null)
		{
			toreturn="Erreur le token n'est pas bon";
		}
		else
		{
			Utilisateur utilisateurInDb = userService.findOne(verificationToken.getUser().getUserId());
			utilisateurInDb.setEnabled(true);
			userService.save(utilisateurInDb);
			toreturn="Utilisateur activé !";
			verificationTokenRepository.delete(verificationToken);
		}
		return toreturn;
	}

	@PostMapping("/forgotPassword")
	public String resetPassword(@RequestBody String email,HttpServletRequest request)
	{
		String toReturn="";
		try {
			Utilisateur utilisateurInDb = userService.findByEmail(email);
			byte[] randomToken = Func.getSalt();
			VerificationToken verificationToken = new VerificationToken();
			verificationToken.setToken(randomToken.toString());
			verificationToken.setUser(utilisateurInDb);
			VerificationToken verificationTokenSaved = verificationTokenRepository.save(verificationToken);
			emailService.sendSimpleMessage(utilisateurInDb.getEmail(),"Changement de mot de passe", siteAdresse + request.getRequestURI() + "/changePassword/"+ verificationTokenSaved.getToken());
			toReturn="Email envoyé !";
		}
		catch (Exception e)
		{
			toReturn= "Erreur";
		}
		return toReturn;
	}

	@GetMapping("/changePassword/{token}")
	public String changePassword(@PathVariable String token) throws NoSuchProviderException, NoSuchAlgorithmException {
		Utilisateur utilisateurFoundInDB = verificationTokenRepository.findByToken(token).getUser();
		byte[] salt = Func.getSalt();
		utilisateurFoundInDB.setSalt(salt);
		byte[] temporarypass = Func.getSalt();
		utilisateurFoundInDB.setPassword(Func.getSecurePassword(temporarypass.toString(), salt));
		utilisateurFoundInDB.setRole(Utilisateur.Role.Client);
		userService.save(utilisateurFoundInDB);
		VerificationToken verificationToken = verificationTokenRepository.findByToken(token);
		verificationTokenRepository.delete(verificationToken);
		return "Mot de passe temporaire : " + temporarypass.toString();
	}
}
