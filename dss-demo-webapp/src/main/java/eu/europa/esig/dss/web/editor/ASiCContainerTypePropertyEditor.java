package eu.europa.esig.dss.web.editor;

import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.utils.Utils;

public class ASiCContainerTypePropertyEditor extends EnumPropertyEditor {

	public ASiCContainerTypePropertyEditor() {
		super(ASiCContainerType.class);
	}
	
	@Override
	public String getAsText() {
		return getValue() == null ? Utils.EMPTY_STRING : ((ASiCContainerType) getValue()).toString();
	}
	
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		try {
			setValue(ASiCContainerType.valueByName(text));
		} catch (Exception e) {
			setValue(null);
		}
	}

}
