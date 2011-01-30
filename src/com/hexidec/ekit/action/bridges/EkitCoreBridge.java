package com.hexidec.ekit.action.bridges;

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.JColorChooser;
import javax.swing.text.StyledEditorKit;

import com.hexidec.ekit.EkitCore;
import com.hexidec.ekit.component.SimpleInfoDialog;
import com.hexidec.ekit.component.UserInputAnchorDialog;

public class EkitCoreBridge implements EkitCoreService {

	private EkitCore inner;

	public EkitCoreBridge(EkitCore ekitCore) {
		this.inner = ekitCore;
	}

	@Override
	public MutatorService getMutator() {
		return inner.getMutator();
	}

	@Override
	public TextPaneService getTextPane() {
		return new TextPaneBridge(inner.getTextPane());
	}

	@Override
	public UserInputService newUserInputAnchorDialog(
			EkitCoreService parentEkit, String title, boolean modal,
			String defaultAnchor) {
		
		return new UserInputBridge(new UserInputAnchorDialog(inner, title, modal, defaultAnchor));
	}

	@Override
	public void repaint() {
		inner.repaint();
	}

	@Override
	public void refreshOnUpdate() {
		inner.refreshOnUpdate();
	}

	@Override
	public void showInfoDialog(String title, boolean modal, String message, int type) {
		new SimpleInfoDialog(inner.getFrame(), title, modal, message, type);
	}

	@Override
	public Color chooseColor(String title, Color c) {
        return JColorChooser.showDialog(inner.getFrame(), title, c);
	}

   @Override
   public void setForegroundColor(Color color, ActionEvent ae) {
      StyledEditorKit.ForegroundAction customColorAction 
         = new StyledEditorKit.ForegroundAction("CustomColor", color);
      customColorAction.actionPerformed(ae);
   }
}
