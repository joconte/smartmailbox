package fr.epsi.smartmailbox.repository;


import fr.epsi.smartmailbox.model.BoiteAuLettre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoiteAuLettreRepository extends JpaRepository<BoiteAuLettre, Long> {

    BoiteAuLettre findByNumeroSerie(String numeroSerie);

    List<BoiteAuLettre> findByToken(String token);

}
