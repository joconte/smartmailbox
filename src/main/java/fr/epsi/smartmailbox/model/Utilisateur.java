package fr.epsi.smartmailbox.model;

import java.util.Date;
import java.util.List;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.CreationTimestamp;

@Entity
public class Utilisateur {

	public enum Role
	{
		Client,
		Admin
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long userId;
	private String firstName;
	private String lastName;
	private String email;
	private String password;

	@CreationTimestamp
	private Date created;

	@JsonIgnore
	private byte[] salt;

	@JsonIgnore
	private Role role;

	@OneToMany
	private List<BoiteAuLettre> boiteAuLettres;

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

	@JsonIgnore
	public String getPassword() {
		return password;
	}

	@JsonProperty
	public void setPassword(String password) {
		this.password = password;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public byte[] getSalt() {
		return salt;
	}

	public void setSalt(byte[] salt) {
		this.salt = salt;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public List<BoiteAuLettre> getBoiteAuLettres() {
		return boiteAuLettres;
	}

	public void setBoiteAuLettres(List<BoiteAuLettre> boiteAuLettres) {
		this.boiteAuLettres = boiteAuLettres;
	}

	public void addBoiteAuLettre(BoiteAuLettre boiteAuLettre)
	{
		this.boiteAuLettres.add(boiteAuLettre);
	}

}
