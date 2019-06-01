package fr.epsi.smartmailbox.controller;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.*;

import javax.servlet.ServletException;

import fr.epsi.smartmailbox.func.Func;
import fr.epsi.smartmailbox.model.GenericObjectWithErrorModel;
import fr.epsi.smartmailbox.model.Utilisateur;
import fr.epsi.smartmailbox.repository.UtilisateurRepository;
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

	@Autowired
	private UtilisateurRepository userService;

	@ApiOperation(value = "Permet de s'enregistrer")
	@PostMapping
	public GenericObjectWithErrorModel<Utilisateur> register(@RequestBody Utilisateur user) throws NoSuchProviderException, NoSuchAlgorithmException {
		GenericObjectWithErrorModel<Utilisateur> userGenericObjectWithErrorModel = new GenericObjectWithErrorModel<>();
		Dictionary<String, List<String>> dictionary = new Hashtable<>();

		byte[] salt = Func.getSalt();
		user.setSalt(salt);

		user.setPassword(Func.getSecurePassword(user.getPassword(), salt));
		user.setRole(Utilisateur.Role.Client);

		if(userService.findByEmail(user.getEmail())==null)
		{
			userGenericObjectWithErrorModel.setT(userService.save(user));
		}
		else
		{
			List<String> strings = new ArrayList<>();
			strings.add("L'adresse email est déja utilisée.");
			dictionary.put("email",strings);
			userGenericObjectWithErrorModel.setErrors(dictionary);
		}
		return userGenericObjectWithErrorModel;
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

		jwtToken = Jwts.builder().setSubject(email).claim("roles", "utilisateur").setIssuedAt(new Date())
				.signWith(SignatureAlgorithm.HS256, "secretkey").compact();

		return jwtToken;
	}

	@PostMapping("/init")
	public String Init() throws NoSuchProviderException, NoSuchAlgorithmException {
		String init = "";
		Utilisateur userInDb = userService.findByEmail("admin@contejonathan.net");
		if(userInDb!=null && userInDb.getRole()!= Utilisateur.Role.Admin)
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
			userService.save(utilisateur);
			init = "user created";
		}
		else
		{
			init = "user already created";
		}
		return init;
	}
}
