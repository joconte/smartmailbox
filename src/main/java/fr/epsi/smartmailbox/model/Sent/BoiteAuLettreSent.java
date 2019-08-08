package fr.epsi.smartmailbox.model.Sent;

import fr.epsi.smartmailbox.func.Func;
import fr.epsi.smartmailbox.model.BoiteAuLettre;

import java.util.Date;

public class BoiteAuLettreSent {

    private Long Id;

    private String numeroSerie;

    private String description;

    private Date lastActivity;

    private String courriers;

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(Date lastActivity) {
        this.lastActivity = lastActivity;
    }

    public String getCourriers() {
        return courriers;
    }

    public void setCourriers(String courriers) {
        this.courriers = courriers;
    }

    public BoiteAuLettreSent() {

    }

    public BoiteAuLettreSent(BoiteAuLettre boiteAuLettre) {
        this.Id = boiteAuLettre.getId();
        this.numeroSerie = boiteAuLettre.getNumeroSerie();
        this.description = boiteAuLettre.getDescription();
        this.lastActivity = boiteAuLettre.getLastActivity();
        this.courriers = Func.siteAdresse + Func.routeSecureCourrierController + String.format(Func.routeSecureCourrierControllerGetMailByMailBoxId.replace("{idMailBox}","{0}"),boiteAuLettre.getId());
    }
}
