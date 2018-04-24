package engineTester;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.border.EmptyBorder;

import entities.Drone;

import javax.swing.JList;
import javax.swing.JLabel;

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
		
		JList list = new JList();
		list.setBounds(6, 6, 116, 192);
		
		ListModel listModel = new DefaultListModel<String>();
		for (Drone d : drones) {
			listModel.addElement(d.getName());
		}
		list.setModel(listModel);
		
		contentPane.add(list);
		
		JLabel lblPosition = new JLabel("Position");
		lblPosition.setBounds(6, 210, 283, 16);
		contentPane.add(lblPosition);
		
		JLabel lblSpeed = new JLabel("Speed");
		lblSpeed.setBounds(6, 238, 61, 16);
		contentPane.add(lblSpeed);
		
		JLabel lblAngularSpeed = new JLabel("Angular speed");
		lblAngularSpeed.setBounds(6, 266, 116, 16);
		contentPane.add(lblAngularSpeed);
	}
}
