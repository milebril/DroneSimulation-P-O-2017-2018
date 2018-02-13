package toolbox;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import autopilot.AutopilotConfigReader;
import autopilot.AutopilotConfigValues;
import autopilot.AutopilotConfigWriter;
import interfaces.AutopilotConfig;

public class generateConfigFile extends JFrame{

	private static JPanel panel = new JPanel();

	private static JLabel gravity = new JLabel("Gravity: ");
	private static JTextField gravityText = new JTextField();
	
	private static JLabel wingX = new JLabel("wingX: ");
	private static JTextField wingXText = new JTextField();
	
	private static JLabel tailSize = new JLabel("tailSize: ");
	private static JTextField tailSizeText = new JTextField();
	
	private static JLabel engineMass = new JLabel("engineMass: ");
	private static JTextField engineMassText= new JTextField();
	
	private static JLabel wingMass = new JLabel("wingMass: ");
	private static JTextField wingMassText= new JTextField();
	
	private static JLabel tailMass = new JLabel("tailMass: ");
	private static JTextField tailMassText= new JTextField();
	
	private static JLabel maxThrust = new JLabel("maxThrust: ");
	private static JTextField maxThrustText= new JTextField();
	
	private static JLabel maxAOA = new JLabel("maxAOA: ");
	private static JTextField maxAOAText= new JTextField();
	
	private static JLabel wingLiftSlope = new JLabel("wingLiftSlope: ");
	private static JTextField wingLiftSlopeText = new JTextField();
	
	private static JLabel horStabLiftSlope = new JLabel("horStabLiftSlope: ");
	private static JTextField horStabLiftSlopeText = new JTextField();
	
	private static JLabel verStabLiftSlope = new JLabel("verStabLiftSlope: ");
	private static JTextField verStabLiftSlopeText= new JTextField();
	
	private static JLabel horizontalAngleOfView = new JLabel("horizontalAngleOfView: ");
	private static JTextField horizontalAngleOfViewText= new JTextField();
	
	private static JLabel verticalAngleOfView = new JLabel("verticalAngleOfView: ");
	private static JTextField verticalAngleOfViewText= new JTextField();
	
	private static JLabel nbColumns = new JLabel("nbColumns: ");
	private static JTextField nbColumnsText= new JTextField();
	
	private static JLabel nbRows = new JLabel("nbRows: ");
	private static JTextField nbRowsText= new JTextField();
	
	private static JButton btnSave = new JButton("Save");
	
	public static void main(String[] arg) {
		
		AutopilotConfig config = readFile("AutopilotConfig");
		if (config != null) {
			writeDataInTextFields(config);
		}
		
		JFrame frame = new JFrame("Config Writer");
		frame.setSize(300, 400);
		frame.setLocation(200, 200);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		GridLayout g = new GridLayout(15, 2);
		g.setHgap(10);
		
		panel.setLayout(g);
		
		panel.add(gravity);
		panel.add(gravityText);
		panel.add(wingX);
		panel.add(wingXText);
		panel.add(tailSize);
		panel.add(tailSizeText);
		panel.add(engineMass);
		panel.add(engineMassText);
		panel.add(wingMass);
		panel.add(wingMassText);
		panel.add(tailMass);
		panel.add(tailMassText);
		panel.add(maxThrust);
		panel.add(maxThrustText);
		panel.add(maxAOA);
		panel.add(maxAOAText);
		panel.add(wingLiftSlope);
		panel.add(wingLiftSlopeText);
		panel.add(horStabLiftSlope);
		panel.add(horStabLiftSlopeText);
		panel.add(verStabLiftSlope);
		panel.add(verStabLiftSlopeText);
		panel.add(horizontalAngleOfView);
		panel.add(horizontalAngleOfViewText);
		panel.add(verticalAngleOfView);
		panel.add(verticalAngleOfViewText);
		panel.add(nbColumns);
		panel.add(nbColumnsText);
		panel.add(nbRows);
		panel.add(nbRowsText);
		
		GridBagLayout gFrame = new GridBagLayout();
		frame.setLayout(gFrame);
		
		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.anchor = GridBagConstraints.PAGE_START;
		gbc1.weighty = 0.9;
		gbc1.gridy = 0; 
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = 1; 
		gbc.weightx = 1;
				
		btnSave.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				saveDataInConfig("AutopilotConfig");
			}
		});
		
		frame.add(panel, gbc1);
		frame.add(btnSave, gbc);
		frame.pack();
	}

	private static void writeDataInTextFields(AutopilotConfig cfg) {
		gravityText.setText(Float.toString(cfg.getGravity()));
		wingXText.setText(Float.toString(cfg.getWingX()));
		tailSizeText.setText(Float.toString(cfg.getTailSize()));
		engineMassText.setText(Float.toString(cfg.getEngineMass()));
		wingMassText.setText(Float.toString(cfg.getWingMass()));
		tailMassText.setText(Float.toString(cfg.getTailMass()));
		maxThrustText.setText(Float.toString(cfg.getMaxThrust()));
		maxAOAText.setText(Float.toString(cfg.getMaxAOA()));
		wingLiftSlopeText.setText(Float.toString(cfg.getWingLiftSlope()));
		horStabLiftSlopeText.setText(Float.toString(cfg.getHorStabLiftSlope()));
		verStabLiftSlopeText.setText(Float.toString(cfg.getVerStabLiftSlope()));
		horizontalAngleOfViewText.setText(Float.toString(cfg.getHorizontalAngleOfView()));
		verticalAngleOfViewText.setText(Float.toString(cfg.getVerticalAngleOfView()));
		nbColumnsText.setText(Integer.toString(cfg.getNbColumns()));
		nbRowsText.setText(Integer.toString(cfg.getNbRows()));
	}
	
	public static void saveDataInConfig(String fileName) {
		File config = new File("res/" + fileName + ".cfg");
		
		try {
			DataOutputStream s = new DataOutputStream(new FileOutputStream(config));
			AutopilotConfigWriter.write(s, new AutopilotConfig() {
				public float getWingX() {
					return Float.parseFloat(wingXText.getText());
				}
				
				public float getWingMass() {
					return Float.parseFloat(wingMassText.getText());
				}
				
				public float getWingLiftSlope() {
					return Float.parseFloat(wingLiftSlopeText.getText());
				}
				
				public float getVerticalAngleOfView() {
					return Float.parseFloat(verticalAngleOfViewText.getText());
				}
				
				public float getVerStabLiftSlope() {
					return Float.parseFloat(verStabLiftSlopeText.getText());
				}
				
				public float getTailSize() {
					return Float.parseFloat(tailSizeText.getText());
				}
				
				public float getTailMass() {
					return Float.parseFloat(tailMassText.getText());
				}
				
				public int getNbRows() {
					return Integer.parseInt(nbRowsText.getText());
				}
				
				public int getNbColumns() {
					return Integer.parseInt(nbColumnsText.getText());
				}
				
				public float getMaxThrust() {
					return Float.parseFloat(maxThrustText.getText());
				}
				
				public float getMaxAOA() {
					return Float.parseFloat(maxAOAText.getText());
				}
				
				public float getHorizontalAngleOfView() {
					return Float.parseFloat(horizontalAngleOfViewText.getText());
				}
				
				public float getHorStabLiftSlope() {
					return Float.parseFloat(horStabLiftSlopeText.getText());
				}
				
				public float getGravity() {
					return Float.parseFloat(gravityText.getText());
				}
				
				public float getEngineMass() {
					return Float.parseFloat(engineMassText.getText());
				}
			});
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("hier");
		System.exit(1);
	}

	private static AutopilotConfig readFile(String fileName) {
		File config = new File("res/" + fileName + ".cfg");
		
		if (!config.exists()) {
			return null;
		}
		System.out.println("hier");
		try {
			//Read the config file
			DataInputStream inputStream = new DataInputStream(new FileInputStream(config));
			return AutopilotConfigReader.read(inputStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("nooit");
		//Komen Hier nooit
		return null;
	}
	
}
