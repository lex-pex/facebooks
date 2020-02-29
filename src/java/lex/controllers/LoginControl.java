package lex.controllers;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

@ManagedBean
@RequestScoped
public class LoginControl {

    public LoginControl() {}

    public String login() {
        return "books";
    }
    
    public String logout() {
        FacesContext.getCurrentInstance().
                    getExternalContext().invalidateSession();
        return "/index.xhtml?faces-redirect=true"; // return "exit";
    }
}
