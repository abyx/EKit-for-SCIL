/*
GNU Lesser General Public License

CustomAction
Copyright (C) 2000 Howard Kistler

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.hexidec.ekit.action;

import java.awt.Color;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTML;

import com.hexidec.ekit.action.bridges.EkitCoreService;
import com.hexidec.ekit.action.bridges.MutatorService;
import com.hexidec.ekit.action.bridges.TextPaneService;
import com.hexidec.ekit.action.bridges.UserInputService;
import com.hexidec.ekit.component.SimpleInfoDialog;
import com.hexidec.util.Translatrix;

/** Class for implementing custom HTML insertion actions */
public class CustomAction extends StyledEditorKit.StyledTextAction {
	private static final long serialVersionUID = 7752928122828347312L;
	protected EkitCoreService parentEkit;
	private HTML.Tag htmlTag;
	private Hashtable<?, ?> htmlAttribs;
	private MutatorService mutator;

	public CustomAction(EkitCoreService ekit, String actionName,
			HTML.Tag inTag, Hashtable<?, ?> attribs) {
		super(actionName);
		parentEkit = ekit;
		htmlTag = inTag;
		htmlAttribs = attribs;
	}

	public CustomAction(EkitCoreService ekit, String actionName, HTML.Tag inTag) {
		this(ekit, actionName, inTag, new Hashtable<Object, Object>());
	}

	public void actionPerformed(ActionEvent ae) {
		mutator = parentEkit.getMutator();
		if (!this.isEnabled())
			return;
		
		TextPaneService parentTextPane = parentEkit.getTextPane();
		String selText = parentTextPane.getSelectedText();
		if (selText == null || selText.length() < 1) {
			parentEkit.showInfoDialog(
					Translatrix.getTranslationString("Error"), true,
					Translatrix.getTranslationString("ErrorNoTextSelected"),
					SimpleInfoDialog.ERROR);
			return;
		} 
		addHyperlink(ae, parentTextPane, selText);
	}

	public void addHyperlink(ActionEvent ae, TextPaneService parentTextPane, String selText) {
		if (htmlTag.toString().equals(HTML.Tag.FONT.toString())) {
			handleFontTag(ae);
		}
		if (!htmlAttribs.containsKey("href")) {
			SimpleAttributeSet sasAttr = new SimpleAttributeSet();
			if (htmlTag.toString().equals(HTML.Tag.A.toString())) {
				String anchor = getAnchor(findCurrentAnchor(parentTextPane, selText));
				if (anchor == null) return;
				insertAttribute(sasAttr, "href", anchor);
			}
			mergeAttributesFromTextPane(sasAttr);
			SimpleAttributeSet sasTag = new SimpleAttributeSet();
			sasTag.addAttribute(htmlTag, sasAttr);
			parentTextPane.setCharacterAttributes(sasTag, false);
			parentEkit.refreshOnUpdate();
		}
		parentTextPane.requestFocus();
	}

	public void mergeAttributesFromTextPane(SimpleAttributeSet sasAttr) {
		SimpleAttributeSet baseAttrs = parentEkit.getTextPane().newSimpleAttributeSet();
		mutator.mutate(baseAttrs);

		Enumeration<?> attribEntriesOriginal = baseAttrs.getAttributeNames();
		while (attribEntriesOriginal.hasMoreElements()) {
			Object entryKey = attribEntriesOriginal.nextElement();
			Object entryValue = baseAttrs.getAttribute(entryKey);
			insertAttribute(sasAttr, entryKey, entryValue);
		}
	}

	public void handleFontTag(ActionEvent ae) {
		if (htmlAttribs.containsKey("color")) {
			Color color = parentEkit.chooseColor(Translatrix
					.getTranslationString("CustomColorDialog"),
					Color.black);
			if (color != null) {
				parentEkit.setForegroundColor(color, ae);
			}
		}
	}
	
	public String getAnchor(String currentAnchor) {
		String newAnchor = getAnchorFromUser(currentAnchor);
		if (newAnchor == null) {
			parentEkit.repaint();
		}
		return newAnchor;
	}

	public String getAnchorFromUser(String currentAnchor) {
		UserInputService uidInput = parentEkit
				.newUserInputAnchorDialog(parentEkit, Translatrix
						.getTranslationString("AnchorDialogTitle"),
						true, currentAnchor);
		String newAnchor = uidInput.getInputText();
		uidInput.dispose();
		return newAnchor;
	}

	public String findCurrentAnchor(TextPaneService parentTextPane, String selectedText) {
		SimpleAttributeSet sasText = null;
		for (int i = 0; i < selectedText.length(); i++) {
			sasText = parentTextPane.newSimpleAttributeSet();
			
			Object entryValue = getValueOf(HTML.Tag.A.toString(), sasText);
			if (entryValue instanceof SimpleAttributeSet) {
				String currentAnchor = (String) getValueOf("href", (SimpleAttributeSet) entryValue);
				if (currentAnchor != null) {
					return currentAnchor;
				}
			}
		}
		return "";
	}

	public boolean isATag(Object entryKey) {
		return entryKey.toString().equals(HTML.Tag.A.toString());
	}
	
	private Object getValueOf(String key, SimpleAttributeSet set) {
		Enumeration<?> attributes = set.getAttributeNames();
		while (attributes.hasMoreElements()) {
			Object entryKey = attributes.nextElement();
			if (key.equalsIgnoreCase(entryKey.toString())) {
				return set.getAttribute(entryKey);
			}
		}
		return null;
	}

	private void insertAttribute(SimpleAttributeSet attrs, Object key,
			Object value) {
		if (value instanceof AttributeSet) {
			AttributeSet subSet = (AttributeSet) value;
			Enumeration<?> attribEntriesSub = subSet.getAttributeNames();
			while (attribEntriesSub.hasMoreElements()) {
				Object subKey = attribEntriesSub.nextElement();
				Object subValue = subSet.getAttribute(subKey);
				insertAttr(attrs, subKey, subValue);
			}
		} else {
			insertAttr(attrs, key, value);
		}
		if (key.toString().toLowerCase().equals("font-family")) {
			if (attrs.isDefined("face")) {
				insertAttr(attrs, "face", attrs.getAttribute("face"));
				insertAttr(attrs, "font-family", attrs.getAttribute("face"));
			} else {
				insertAttr(attrs, "face", value);
			}
		}
	}

	private void insertAttr(SimpleAttributeSet attrs, Object key, Object value) {
		while (attrs.isDefined(key)) {
			attrs.removeAttribute(key);
			break;
		}
		attrs.addAttribute(key, value);
	}
}
