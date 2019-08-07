package fr.epsi.smartmailbox.controller;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import fr.epsi.smartmailbox.component.EmailServiceImpl;
import fr.epsi.smartmailbox.func.Func;
import fr.epsi.smartmailbox.model.GenericObjectWithErrorModel;
import fr.epsi.smartmailbox.model.Received.UtilisateurRegister;
import fr.epsi.smartmailbox.model.Sent.UtilisateurSent;
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

@Api( "API publique, pour se connecter ou s'enregistrer")
@CrossOrigin(origins = "http://localhost", maxAge = 3600)
@RestController
@RequestMapping(Func.routeUserController)
public class UserController {

	@Autowired
	private UtilisateurRepository userService;

	@Autowired
	private VerificationTokenRepository verificationTokenRepository;

	@Autowired
	EmailServiceImpl emailService;

	@ApiOperation(value = "Permet de s'enregistrer")
	@PostMapping
	public Object register(@RequestBody UtilisateurRegister userRegister, HttpServletRequest request) throws NoSuchProviderException, NoSuchAlgorithmException {
		Object returnObj;
		Dictionary<String, List<String>> dictionary = UserValidation(userRegister);
		Utilisateur user = new Utilisateur(userRegister);
		if(dictionary.isEmpty())
		{
			byte[] salt = Func.getSalt();
			user.setSalt(salt);
			user.setPassword(Func.getSecurePassword(user.getPassword(), salt));
			user.setRole(Utilisateur.Role.Client);
			Utilisateur userSaved = userService.save(user);
			returnObj = new UtilisateurSent(userSaved);
			byte[] randomToken = Func.getSalt();
			VerificationToken verificationToken = new VerificationToken();
			verificationToken.setToken(randomToken.toString());
			verificationToken.setUser(userSaved);
			VerificationToken verificationTokenSaved = verificationTokenRepository.save(verificationToken);
			emailService.sendSimpleMessage(userSaved.getEmail(),"Token de vérification", Func.siteAdresse + "/user/verify/"+ verificationTokenSaved.getToken());
		}
		else
		{
			returnObj = dictionary;
		}
		return returnObj;
	}

	public Dictionary<String, List<String>> UserValidation(UtilisateurRegister user)
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
		return dictionary;
	}

	@ApiOperation(value = "Permet de se connecter, l'API répond par un token de type Bearer qu'il faudra passer dans le header par la suite sur toutes les API sécurisées.")
	@PostMapping(value = "/login")
	public String login(@RequestBody Utilisateur login) throws ServletException {

		String jwtToken = "";

		if (login.getEmail() == null || login.getPassword() == null) {
			throw new ServletException("Please fill in username and password");
		}

		String email = login.getEmail();
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

	@ApiOperation(value = "Permet de se connecter, l'API répond par un token de type Bearer qu'il faudra passer dans le header par la suite sur toutes les API sécurisées.")
	@PostMapping(value = "/login-angular")
	public VerificationToken loginForAngular(@RequestBody Utilisateur login) throws ServletException {

		String jwtToken = "";

		if (login.getEmail() == null || login.getPassword() == null) {
			throw new ServletException("Please fill in username and password");
		}

		String email = login.getEmail();
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

		VerificationToken verificationToken = new VerificationToken();
		verificationToken.setToken(jwtToken);
		return verificationToken;
	}

	@PostMapping("/init")
	public String Init() throws NoSuchProviderException, NoSuchAlgorithmException {
		String init = "";
		Utilisateur userInDb = userService.findByEmail(Func.adminAccount);
		if((userInDb!=null && userInDb.getRole()!= Utilisateur.Role.Admin) || (userInDb!=null && !userInDb.isEnabled()))
		{
			userService.delete(userInDb);
		}

		if(userService.findByEmail(Func.adminAccount)==null)
		{
			Utilisateur utilisateur = new Utilisateur();
			utilisateur.setFirstName("Jonathan");
			utilisateur.setLastName("CONTE");
			utilisateur.setEmail(Func.adminAccount);
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

	@ApiOperation(value = "Permet de vérifier l'adresse email lors de la création de compte")
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

	@ApiOperation(value = "Permet d'envoyer un lien par mail pour changer le mot de passe")
	@PostMapping("/forgotPassword")
	public GenericObjectWithErrorModel<String> resetPassword(@RequestBody String email)
	{
		GenericObjectWithErrorModel<String> stringGenericObjectWithErrorModel = new GenericObjectWithErrorModel<>();
		Dictionary<String, List<String>> dictionary = new Hashtable<>();
		try {
			Utilisateur utilisateurInDb = userService.findByEmail(email);
			byte[] randomToken = Func.getSalt();
			VerificationToken verificationToken = new VerificationToken();
			verificationToken.setToken(randomToken.toString());
			verificationToken.setUser(utilisateurInDb);
			VerificationToken verificationTokenSaved = verificationTokenRepository.save(verificationToken);
			emailService.sendSimpleMessage(utilisateurInDb.getEmail(),"Changement de mot de passe", Func.siteAdresse + "/user/changePassword/"+ verificationTokenSaved.getToken());
			stringGenericObjectWithErrorModel.setT("Email envoyé !");
		}
		catch (Exception e)
		{
			List<String> strings = new ArrayList<>();
			strings.add("Email non trouvée");
			dictionary.put("Erreur",strings);
			stringGenericObjectWithErrorModel.setErrors(dictionary);
		}
		return stringGenericObjectWithErrorModel;
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
