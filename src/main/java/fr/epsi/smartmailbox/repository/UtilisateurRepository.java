package fr.epsi.smartmailbox.repository;

import fr.epsi.smartmailbox.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur,Long> {

    Utilisateur findByEmail(String email);

    @Query(
            value = "select b.numero_serie from utilisateur u inner join utilisateur_boite_au_lettres ub on u.user_id = ub.utilisateur_user_id inner join boite_au_lettre b on b.id = ub.boite_au_lettres_id where b.numero_serie = ?1",
            nativeQuery = true)
    public String findBoiteAuLettreIfTaken(String numeroSerie);
}
