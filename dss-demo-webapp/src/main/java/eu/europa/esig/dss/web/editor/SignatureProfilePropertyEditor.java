package eu.europa.esig.dss.web.editor;

import eu.europa.esig.dss.enumerations.SignatureProfile;
import eu.europa.esig.dss.utils.Utils;

public class SignatureProfilePropertyEditor extends EnumPropertyEditor {

    public SignatureProfilePropertyEditor() {
        super(SignatureProfile.class);
    }

    @Override
    public String getAsText() {
        return getValue() == null ? Utils.EMPTY_STRING : ((SignatureProfile) getValue()).toString();
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        try {
            setValue(SignatureProfile.valueByName(text));
        } catch (Exception e) {
            setValue(null);
        }
    }

}
