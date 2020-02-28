package lex.valid;

import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator("lex.valid.LogValid")
public class LoginValid implements Validator {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value)
            throws ValidatorException {

        String rqst = value.toString();
        ResourceBundle bundle = ResourceBundle.getBundle("lex.nls.msg",
                    FacesContext.getCurrentInstance().getViewRoot().getLocale());
        if (rqst.length() < 3) {
            FacesMessage msg = new FacesMessage(bundle.getString("login_length"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(msg);
        }
        
        if (notValidName(rqst)) {
            FacesMessage msg = new FacesMessage(bundle.getString("bad_name"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(msg);
        }
    }

    private boolean notValidName(String rqst) {
        String[] reserve = new String[]{ "login", "username", "user", "admin" };
        for (String s : reserve)
            if (rqst.equalsIgnoreCase(s))
                return true;
        return false;
    }
    
}
