package fr.epsi.smartmailbox;

import fr.epsi.smartmailbox.config.JwtFilter;
import fr.epsi.smartmailbox.model.Utilisateur;
import fr.epsi.smartmailbox.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Repository;

@SpringBootApplication
public class SpringBootJwtApplication {

	@Bean
	public FilterRegistrationBean jwtFilter() {
		final FilterRegistrationBean registrationBean = new FilterRegistrationBean();
		registrationBean.setFilter(new JwtFilter());
		registrationBean.addUrlPatterns("/secure/*");

		return registrationBean;
	}




	public static void main(String[] args) {
		SpringApplication.run(SpringBootJwtApplication.class, args);

	}
}
