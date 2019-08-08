package fr.epsi.smartmailbox.model.Sent;

import fr.epsi.smartmailbox.func.Func;
import fr.epsi.smartmailbox.model.Courrier;

import java.util.Date;

public class CourrierSent {

    private Long Id;

    private boolean vu;

    private Date dateReception;

    private String boiteAuLettre;

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public boolean isVu() {
        return vu;
    }

    public void setVu(boolean vu) {
        this.vu = vu;
    }

    public Date getDateReception() {
        return dateReception;
    }

    public void setDateReception(Date dateReception) {
        this.dateReception = dateReception;
    }

    public String getBoiteAuLettre() {
        return boiteAuLettre;
    }

    public void setBoiteAuLettre(String boiteAuLettre) {
        this.boiteAuLettre = boiteAuLettre;
    }

    public CourrierSent() {

    }

    public CourrierSent(Courrier courrier) {
        this.Id = courrier.getId();
        this.vu = courrier.isVu();
        this.dateReception = courrier.getDateReception();
        this.boiteAuLettre = Func.siteAdresse + Func.routeSecureBoiteAuLettreController + String.format(Func.routeSecureBoiteAuLettreControllerGetMailBoxById.replace("{idMailBox}","{0}"),courrier.getBoiteAuLettre().getId());
    }
}
