package fr.epsi.smartmailbox.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
public class BoiteAuLettre {

    @Id
    @GeneratedValue
    private Long Id;

    private String numeroSerie;

    private String token;

    private String description;

    private Date lastActivity;

    public Date getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(Date lastActivity) {
        this.lastActivity = lastActivity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @OneToMany
    private List<Courrier> courriers;

    public List<Courrier> getCourriers() {
        return courriers;
    }

    public void setCourriers(List<Courrier> courriers) {
        this.courriers = courriers;
    }

    public void addCourrier(Courrier courrier)
    {
        this.courriers.add(courrier);
    }

    @JsonIgnore
    public String getToken() {
        return token;
    }

    @JsonProperty
    public void setToken(String token) {
        this.token = token;
    }

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
}

