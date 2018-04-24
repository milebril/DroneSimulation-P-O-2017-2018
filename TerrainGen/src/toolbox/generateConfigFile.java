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
import java.io.UTFDataFormatException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import autopilot.interfaces.AutopilotConfig;
import autopilot.interfaces.config.AutopilotConfigReader;
import autopilot.interfaces.config.AutopilotConfigValues;
import autopilot.interfaces.config.AutopilotConfigWriter;

public class generateConfigFile extends JFrame{

	private static JPanel panel = new JPanel();

	private static JLabel droneID = new JLabel("droneID: "); //1
	private static JTextField droneIDText = new JTextField();
	
	private static JLabel gravity = new JLabel("Gravity: "); //2
	private static JTextField gravityText = new JTextField();
	
	private static JLabel wingX = new JLabel("wingX: "); //3
	private static JTextField wingXText = new JTextField();
	
	private static JLabel tailSize = new JLabel("tailSize: "); //4
	private static JTextField tailSizeText = new JTextField();
	
	private static JLabel wheelY = new JLabel("wheelY: "); //5
	private static JTextField wheelYText = new JTextField();
	
	private static JLabel frontWheelZ = new JLabel("frontWheelZ: "); //6
	private static JTextField frontWheelZText = new JTextField();
	
	private static JLabel rearWheelZ = new JLabel("rearWheelZ: "); //7
	private static JTextField rearWheelZText = new JTextField();
	
	private static JLabel rearWheelX = new JLabel("rearWheelX: "); //8
	private static JTextField rearWheelXText = new JTextField();
	
	private static JLabel tyreSlope = new JLabel("tyreSlope: "); //9
	private static JTextField tyreSlopeText = new JTextField();
	
	private static JLabel dampSlope = new JLabel("dampSlope: "); //10
	private static JTextField dampSlopeText = new JTextField();
	
	private static JLabel tyreRadius = new JLabel("tyreRadius: "); //11
	private static JTextField tyreRadiusText = new JTextField();
	
	private static JLabel rMax = new JLabel("rMax: "); //12
	private static JTextField rMaxText = new JTextField();
	
	private static JLabel fcMax = new JLabel("fcMAx: "); //13
	private static JTextField fcMaxText = new JTextField();
	
	private static JLabel engineMass = new JLabel("engineMass: "); //14
	private static JTextField engineMassText= new JTextField();
	
	private static JLabel wingMass = new JLabel("wingMass: "); //15
	private static JTextField wingMassText= new JTextField();
	
	private static JLabel tailMass = new JLabel("tailMass: "); //16
	private static JTextField tailMassText= new JTextField();
	
	private static JLabel maxThrust = new JLabel("maxThrust: "); //17
	private static JTextField maxThrustText= new JTextField();
	
	private static JLabel maxAOA = new JLabel("maxAOA: "); //18
	private static JTextField maxAOAText= new JTextField();
	
	private static JLabel wingLiftSlope = new JLabel("wingLiftSlope: "); //19
	private static JTextField wingLiftSlopeText = new JTextField();
	
	private static JLabel horStabLiftSlope = new JLabel("horStabLiftSlope: "); //20
	private static JTextField horStabLiftSlopeText = new JTextField();
	
	private static JLabel verStabLiftSlope = new JLabel("verStabLiftSlope: "); //21
	private static JTextField verStabLiftSlopeText= new JTextField();
	
	private static JLabel horizontalAngleOfView = new JLabel("horizontalAngleOfView: "); //22
	private static JTextField horizontalAngleOfViewText= new JTextField();
	
	private static JLabel verticalAngleOfView = new JLabel("verticalAngleOfView: "); //23
	private static JTextField verticalAngleOfViewText= new JTextField();
	
	private static JLabel nbColumns = new JLabel("nbColumns: "); //24
	private static JTextField nbColumnsText= new JTextField();
	
	private static JLabel nbRows = new JLabel("nbRows: "); //25
	private static JTextField nbRowsText= new JTextField(); 
	
	private static JButton btnSave = new JButton("Save");
	
	public static void main(String[] arg) {
		
		AutopilotConfig config = readFile("AutopilotConfig");
		if (config != null) {
			writeDataInTextFields(config);
		}
		
		JFrame frame = new JFrame("Config Writer");
		frame.setSize(300, 667); //400 voor 15 -> 26,66666 per regel
		frame.setLocation(200, 200);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		GridLayout g = new GridLayout(25, 2);
		g.setHgap(10);
		
		panel.setLayout(g);
		
		panel.add(droneID);
		panel.add(droneIDText);
		panel.add(gravity);
		panel.add(gravityText);
		panel.add(wingX);
		panel.add(wingXText);
		panel.add(tailSize);
		panel.add(tailSizeText);
		panel.add(wheelY);
		panel.add(wheelYText);
		panel.add(frontWheelZ);
		panel.add(frontWheelZText);
		panel.add(rearWheelZ);
		panel.add(rearWheelZText);
		panel.add(rearWheelX);
		panel.add(rearWheelXText);
		panel.add(tyreSlope);
		panel.add(tyreSlopeText);
		panel.add(dampSlope);
		panel.add(dampSlopeText);
		panel.add(tyreRadius);
		panel.add(tyreRadiusText);
		panel.add(rMax);
		panel.add(rMaxText);
		panel.add(fcMax);
		panel.add(fcMaxText);
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
		droneIDText.setText(cfg.getDroneID());
		gravityText.setText(Float.toString(cfg.getGravity()));
		wingXText.setText(Float.toString(cfg.getWingX()));
		tailSizeText.setText(Float.toString(cfg.getTailSize()));
		wheelYText.setText(Float.toString(cfg.getWheelY()));
		frontWheelZText.setText(Float.toString(cfg.getFrontWheelZ()));
		rearWheelZText.setText(Float.toString(cfg.getRearWheelZ()));
		rearWheelXText.setText(Float.toString(cfg.getRearWheelX()));
		tyreSlopeText.setText(Float.toString(cfg.getTyreSlope()));
		dampSlopeText.setText(Float.toString(cfg.getDampSlope()));
		tyreRadiusText.setText(Float.toString(cfg.getTyreRadius()));
		rMaxText.setText(Float.toString(cfg.getRMax()));
		fcMaxText.setText(Float.toString(cfg.getFcMax()));
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

				public String getDroneID() {
					return droneIDText.getText();
				}

				public float getWheelY() {
					return Float.parseFloat(wheelYText.getText());
				}

				public float getFrontWheelZ() {
					return Float.parseFloat(frontWheelZText.getText());
				}

				public float getRearWheelZ() {
					return Float.parseFloat(rearWheelZText.getText());
				}

				public float getRearWheelX() {
					return Float.parseFloat(rearWheelXText.getText());
				}

				public float getTyreSlope() {
					return Float.parseFloat(tyreSlopeText.getText());
				}

				public float getDampSlope() {
					return Float.parseFloat(dampSlopeText.getText());
				}

				public float getTyreRadius() {
					return Float.parseFloat(tyreRadiusText.getText());
				}

				public float getRMax() {
					return Float.parseFloat(rMaxText.getText());
				}

				public float getFcMax() {
					return Float.parseFloat(fcMaxText.getText());
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
