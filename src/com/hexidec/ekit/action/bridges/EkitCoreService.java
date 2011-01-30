package com.hexidec.ekit.action.bridges;

import java.awt.Color;
import java.awt.event.ActionEvent;


public interface EkitCoreService {

	MutatorService getMutator();

	TextPaneService getTextPane();

	UserInputService newUserInputAnchorDialog(EkitCoreService owner, String title, boolean modal,
		String defaultAnchor);

	void repaint();

	void refreshOnUpdate();

	void showInfoDialog(String title, boolean modal, String message, int type);

	Color chooseColor(String title, Color c);

   void setForegroundColor(Color color, ActionEvent ae);

}
