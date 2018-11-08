package ui;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import repast.simphony.userpanel.ui.UserPanelCreator;
import utils.Calls;

public class UserPanel implements UserPanelCreator {

	@Override
	public JPanel createPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		panel.add(new JLabel("Counter-Terrorist Strategy"));
		JComboBox<Calls.Callouts> ctStratSel = new JComboBox<Calls.Callouts>(Calls.Callouts.values());
		panel.add(ctStratSel);
		
		panel.add(new JLabel("Terrorist Strategy"));
		JComboBox<Calls.Callouts> tStratSel = new JComboBox<Calls.Callouts>(Calls.Callouts.values());
		panel.add(tStratSel);
		
		return panel;
	}

}
