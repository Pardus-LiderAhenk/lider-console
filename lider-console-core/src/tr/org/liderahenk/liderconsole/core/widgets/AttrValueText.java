/*
*
*    Copyright © 2015-2016 Tübitak ULAKBIM
*
*    This file is part of Lider Ahenk.
*
*    Lider Ahenk is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 3 of the License, or
*    (at your option) any later version.
*
*    Lider Ahenk is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with Lider Ahenk.  If not, see <http://www.gnu.org/licenses/>.
*/
package tr.org.liderahenk.liderconsole.core.widgets;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * @author Emre Akkaya <emre.akkaya@agem.com.tr>
 *
 */
public class AttrValueText extends Text {

	private static String KEY_PRESS = "Ctrl+Space";
	private String[] proposals = new String[] {};

	/**
	 * @param parent
	 * @param style
	 */
	public AttrValueText(Composite parent, int style) {
		super(parent, style);
		setAutoCompletion(this, null);
		this.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent ke) {
				// Method for autocompletion
				setAutoCompletion(getSelf(), getSelf().getText());
			}
		});
	}

	public AttrNameCombo getRelatedAttrCombo() {
		Control[] children = this.getParent().getChildren();
		if (children != null) {
			for (Control child : children) {
				if (child instanceof AttrNameCombo) {
					return (AttrNameCombo) child;
				}
			}
		}
		return null;
	}

	private void setAutoCompletion(Text text, String value) {
		try {
			ContentProposalAdapter adapter = null;
			SimpleContentProposalProvider scp = new SimpleContentProposalProvider(proposals);
			scp.setProposals(proposals);
			KeyStroke ks = KeyStroke.getInstance(KEY_PRESS);
			adapter = new ContentProposalAdapter(text, new TextContentAdapter(), scp, ks, null);
			adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public AttrOperator getRelatedAttrOperator() {
		Control[] children = this.getParent().getChildren();
		if (children != null) {
			for (Control child : children) {
				if (child instanceof AttrOperator) {
					return (AttrOperator) child;
				}
			}
		}
		return null;
	}

	public void setAutoCompleteProposals(String[] proposals) {
		this.proposals = proposals;
	}

	protected AttrValueText getSelf() {
		return this;
	}

	@Override
	protected void checkSubclass() {
		// By default, subclassing is not allowed for many of the SWT Controls
		// This empty method disables the check that prevents subclassing of
		// this class
	}

}
