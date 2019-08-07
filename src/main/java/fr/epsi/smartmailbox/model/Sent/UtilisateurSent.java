package fr.epsi.smartmailbox.model.Sent;

import fr.epsi.smartmailbox.func.Func;
import fr.epsi.smartmailbox.model.Utilisateur;

import java.util.Date;

public class UtilisateurSent {

    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private Date created;
    private Utilisateur.Role role;
    private String boiteAuLettres;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Utilisateur.Role getRole() {
        return role;
    }

    public void setRole(Utilisateur.Role role) {
        this.role = role;
    }

    public String getBoiteAuLettres() {
        return boiteAuLettres;
    }

    public void setBoiteAuLettres(String boiteAuLettres) {
        this.boiteAuLettres = boiteAuLettres;
    }

    public UtilisateurSent() {

    }

    public UtilisateurSent(Utilisateur user) {
        this.userId = user.getUserId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.created = user.getCreated();
        this.role = user.getRole();
        this.boiteAuLettres = Func.siteAdresse + Func.routeSecureBoiteAuLettreController;
    }
}
