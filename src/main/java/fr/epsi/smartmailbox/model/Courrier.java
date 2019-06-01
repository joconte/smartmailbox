package fr.epsi.smartmailbox.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Courrier {

    @Id
    @GeneratedValue
    private Long Id;

    private boolean vu;

    private Date dateReception;

    @ManyToOne
    private BoiteAuLettre boiteAuLettre;

    @JsonIgnore
    public BoiteAuLettre getBoiteAuLettre() {
        return boiteAuLettre;
    }

    @JsonProperty
    public void setBoiteAuLettre(BoiteAuLettre boiteAuLettre) {
        this.boiteAuLettre = boiteAuLettre;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    @JsonProperty
    public boolean isVu() {
        return vu;
    }

    @JsonIgnore
    public void setVu(boolean vu) {
        this.vu = vu;
    }

    public Date getDateReception() {
        return dateReception;
    }

    public void setDateReception(Date dateReception) {
        this.dateReception = dateReception;
    }

}
