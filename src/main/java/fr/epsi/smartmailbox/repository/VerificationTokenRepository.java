package fr.epsi.smartmailbox.repository;

import fr.epsi.smartmailbox.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken,Long> {

    public VerificationToken findByToken(String Token);
}
