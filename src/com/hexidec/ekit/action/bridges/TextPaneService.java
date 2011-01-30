package com.hexidec.ekit.action.bridges;

import javax.swing.text.SimpleAttributeSet;

public interface TextPaneService {

	String getSelectedText();

	int getSelectionStart();

	void select(int i, int j);

	void setCharacterAttributes(SimpleAttributeSet sasTag, boolean b);

	void requestFocus();

	SimpleAttributeSet newSimpleAttributeSet();

}
