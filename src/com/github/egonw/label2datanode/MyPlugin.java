package com.github.egonw.label2datanode;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JTabbedPane;

import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.plugin.Plugin;

public class MyPlugin implements Plugin {

	private ChangePane changePane;

	@Override
	public void init(PvDesktop desktop) {
		System.out.println("MyPlugin started");
		changePane = new ChangePane(desktop.getSwingEngine());
		desktop.getSwingEngine().getEngine().addApplicationEventListener(changePane);

		JTabbedPane sidebarTabbedPane = desktop.getSideBarTabbedPane();
		sidebarTabbedPane.add("Label2DataNode", changePane);
		JButton redButton = new JButton("Convert to DataNode");
        redButton.setLocation(0, 0);
        redButton.setSize(250, 30);
        changePane.add(redButton);
        redButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.out.println("redButton clicked...");
				changePane.convert();
			}
		});
	}

	@Override
	public void done() {
		changePane = null;
	}

}
