package fr.epsi.smartmailbox.model;


import java.util.Dictionary;
import java.util.List;

public class GenericObjectWithErrorModel<T> {

    private T t;

    private Dictionary<String, List<String>> Errors;

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }

    public Dictionary<String, List<String>> getErrors() {
        return Errors;
    }

    public void setErrors(Dictionary<String, List<String>> errors) {
        Errors = errors;
    }
}