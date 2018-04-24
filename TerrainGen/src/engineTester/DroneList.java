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

import entities.Drone;

import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class DroneList extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
//	public static void main(String[] args) {
//		EventQueue.invokeLater(new Runnable() {
//			public void run() {
//				try {
//					DroneList frame = new DroneList();
//					frame.setVisible(true);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//	}

	//Labels;
	JLabel lblPosition;
	JLabel lblSpeed;
	JLabel lblAngularSpeed;
	
	/**
	 * Create the frame.
	 */
	public DroneList(List<Drone> drones) {
		setVisible(true);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 505, 473);
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
	}
	
	public void updateLabels(Drone drone) {
		lblPosition.setText("Position: (" + drone.getPosition().x + ", " + drone.getPosition().y + ", " + drone.getPosition().z + ")");
		lblSpeed.setText("Speed: (" + drone.getLinearVelocity().x + ", " + drone.getLinearVelocity().y + ", " + drone.getLinearVelocity().z + ")");
		lblAngularSpeed.setText("Speed: (" + drone.getAngularVelocity().x + ", " + drone.getAngularVelocity().y + ", " + drone.getAngularVelocity().z + ")");
	}
}
