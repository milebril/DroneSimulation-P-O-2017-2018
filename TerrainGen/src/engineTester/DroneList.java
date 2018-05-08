package engineTester;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.border.EmptyBorder;

import autopilot.algorithmHandler.AlgorithmHandler;
import autopilot.algorithmHandler.AutopilotAlain;
import entities.Drone;

import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class DroneList extends JFrame {

	private JPanel contentPane;

	// Labels;
	private JLabel lblPosition;
	private JLabel lblSpeed;
	private JLabel lblAngularSpeed;
	private JLabel lblApState;
	private JLabel lblHoldingPackage;

	/**
	 * Create the frame.
	 */
	public DroneList(List<Drone> drones) {
		setVisible(true);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(0, 0, 295, 473);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JList<String> list = new JList<String>();
		list.setBounds(6, 6, 116, 192);
		DefaultListModel<String> listModel = new DefaultListModel<String>();
		for (Drone d : drones) {
			listModel.addElement(d.getName());
		}
		list.setModel(listModel);
		JScrollPane pane = new JScrollPane(list);
		pane.setBounds(6, 6, 116, 192);
		contentPane.add(pane);

		lblPosition = new JLabel("Position");
		lblPosition.setBounds(6, 210, 283, 16);
		contentPane.add(lblPosition);

		lblSpeed = new JLabel("Speed");
		lblSpeed.setBounds(6, 238, 252, 16);
		contentPane.add(lblSpeed);

		lblAngularSpeed = new JLabel("Angular speed");
		lblAngularSpeed.setBounds(6, 266, 493, 16);
		contentPane.add(lblAngularSpeed);

		JButton btnChoose = new JButton("Choose");
		btnChoose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainGameLoop.setActiveDrone(drones.get(list.getSelectedIndex()));
			}
		});
		btnChoose.setBounds(134, 6, 117, 29);
		contentPane.add(btnChoose);
		
		lblApState = new JLabel("AP State");
		lblApState.setBounds(6, 294, 261, 16);
		contentPane.add(lblApState);
		
		lblHoldingPackage = new JLabel("Holding Package");
		lblHoldingPackage.setBounds(6, 322, 261, 16);
		contentPane.add(lblHoldingPackage);
		
		JButton btnTopDown = new JButton("TOP DOWN");
		btnTopDown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainGameLoop.extraView = ViewEnumExtra.TOP_DOWN;
			}
		});
		btnTopDown.setBounds(134, 47, 117, 29);
		contentPane.add(btnTopDown);
		
		JButton btnLeftSide = new JButton("LEFT SIDE");
		btnLeftSide.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainGameLoop.extraView = ViewEnumExtra.LEFT_SIDE;
			}
		});
		btnLeftSide.setBounds(134, 88, 117, 29);
		contentPane.add(btnLeftSide);
		
		JButton btnRightSide = new JButton("RIGHT SIDE");
		btnRightSide.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainGameLoop.extraView = ViewEnumExtra.RIGHT_SIDE;
			}
		});
		btnRightSide.setBounds(134, 129, 117, 29);
		contentPane.add(btnRightSide);
	}

	public void updateLabels(Drone drone) {
		lblPosition.setText("Position: (" + (float) Math.round(drone.getPosition().x * 100) / 100 + ", "
				+ (float) Math.round(drone.getPosition().y * 100) / 100 + ", "
				+ (float) Math.round(drone.getPosition().z * 100) / 100 + ")");
		lblSpeed.setText("Speed: (" + (float) Math.round(drone.getLinearVelocity().x * 100) / 100 + ", "
				+ (float) Math.round(drone.getLinearVelocity().y * 100) / 100 + ", "
				+ (float) Math.round(drone.getLinearVelocity().z * 100) / 100 + ")");
		lblAngularSpeed.setText("Angular Velocity: (" + (float) Math.round(drone.getAngularVelocity().x * 100) / 100 + ", "
				+ (float) Math.round(drone.getAngularVelocity().y * 100) / 100 + ", "
				+ (float) Math.round(drone.getAngularVelocity().z * 100) / 100 + ")");
		
		//lblApState.setText("AP State : " + ((AlgorithmHandler) drone.getAutopilot()).getAlgorithmName()); 
		lblHoldingPackage.setText("Holding Package: False");
	}
}
