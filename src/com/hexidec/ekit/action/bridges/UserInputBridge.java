package com.hexidec.ekit.action.bridges;

import com.hexidec.ekit.component.UserInputAnchorDialog;

public class UserInputBridge implements UserInputService {

	private UserInputAnchorDialog inner;

	public UserInputBridge(
			UserInputAnchorDialog userInputAnchorDialog) {
		this.inner = userInputAnchorDialog;
	}

	@Override
	public void dispose() {
		inner.dispose();
	}

	@Override
	public String getInputText() {
		return inner.getInputText();
	}

}
