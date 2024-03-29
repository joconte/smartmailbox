package fr.epsi.smartmailbox.func;


import fr.epsi.smartmailbox.model.Utilisateur;
import fr.epsi.smartmailbox.repository.UtilisateurRepository;
import io.jsonwebtoken.Jwts;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

public class Func {

    public static String getSecurePassword(String passwordToHash, byte[] salt)
    {
        String generatedPassword = null;
        try {
            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            //Add password bytes to digest
            md.update(salt);
            //Get the hash's bytes
            byte[] bytes = md.digest(passwordToHash.getBytes());
            //This bytes[] has bytes in decimal format;
            //Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++)
            {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            //Get complete hashed password in hex format
            generatedPassword = sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }

    //Add salt
    public static byte[] getSalt() throws NoSuchAlgorithmException, NoSuchProviderException
    {
        //Always use a SecureRandom generator
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "SUN");
        //Create array for salt
        byte[] salt = new byte[16];
        //Get a random salt
        sr.nextBytes(salt);
        //return salt
        return salt;
    }

    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    public static String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    public static boolean isValidEmail(String email) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }

    public static String getUserNameByToken(String token) {
        return Jwts.parser().setSigningKey(Func.secretKey).parseClaimsJws(token.split(" ")[1]).getBody().getSubject();
    }

    //public static final String siteAdresse = "http://192.168.1.17:8080";

    public static final String siteAdresse =  "https://smartmailbox-epsi.herokuapp.com";

    public static final String secretKey = "secretkey";

    public static final String userNotFoundInDb = "L'utilisateur n'a pas été trouvé en base.";

    public static final String adminAccount = "admin@contejonathan.net";

    public static final String routeUserController = "/user";
    public static final String routeUserControllerLogin = "/login";
    public static final String routeUserControllerForgotPassword = "/forgotPassword";
    public static final String routeUserControllerVerifyEmail = "/verify/{token}";
    public static final String routeUserControllerChangePassword = "/changePassword/{token}";
    public static final String routeSecureUserController =  "/secure/user";
    public static final String routeSecureUserControllerGetUserConnected = "/me";
    public static final String routeSecureCourrierController =  "/secure/courrier";
    public static final String routeSecureBoiteAuLettreController =  "/secure/BAL";
    public static final String routeSecureBoiteAuLettreControllerGetTokenByNumSerie = "/serialNumber/{serialNumber}";
    public static final String routeSecureBoiteAuLettreControllerGetAll = "/all";
    public static final String routeSecureBoiteAuLettreControllerGetMailBoxById = "/{idMailBox}";
    public static final String routeCourrierController = "/courrier";
    public static final String routeSecureCourrierControllerGetMailByMailBoxId = "/idMailBox/{idMailBox}";


}
