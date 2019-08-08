package fr.epsi.smartmailbox.repository;


import fr.epsi.smartmailbox.model.Courrier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourrierRepository extends JpaRepository<Courrier,Long> {


}
