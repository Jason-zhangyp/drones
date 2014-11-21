package utils;

import io.input.ControllerInput;
import io.input.GPSModuleInput;
import io.input.I2CCompassModuleInput;
import io.output.ControllerOutput;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import main.Controller;
import dataObjects.GPSData;

public class Logger extends Thread {
	
	private final static long SLEEP_TIME = 100;
	
	private String fileName = "";
	private Controller controller;
	
	public Logger(Controller controller) {
		this.controller = controller;
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
		fileName = dateFormat.format(new Date());
	}
	
	@Override
	public void run() {
		
		BufferedWriter bw = null;
		
		try {
		
			FileWriter fw = new FileWriter(new File("logs/values_"+fileName+".log"));
			bw = new BufferedWriter(fw);
			
			while(true) {
				bw.write(getLogString());
				Thread.sleep(SLEEP_TIME);
			}
			
		} catch(InterruptedException e) {
			//this will happen when the program exits
		} catch(Exception e) {
			e.printStackTrace();
		} finally { 
			try {
			if(bw != null)
				bw.close();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private String getLogString() {
		List<ControllerInput> inputs = controller.getIOManager().getInputs();
		List<ControllerOutput> outputs = controller.getIOManager().getOutputs();
		
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
		
		String result = dateFormat.format(new Date())+"\t";
		
		for(ControllerInput i : inputs) {
			if(i instanceof I2CCompassModuleInput) {
				I2CCompassModuleInput ic = (I2CCompassModuleInput)i;
				result+=ic.getHeading()+"\t";
			} else if(i instanceof GPSModuleInput) {
				GPSModuleInput ig = (GPSModuleInput)i;
				GPSData data = ig.getReadings();
				result+=data.getLatitude()+"\t"+data.getLongitude()+"\t"+data.getOrientation()+"\t"+data.getGroundSpeedKmh()+"\t";
			}
		}
		
		for(ControllerOutput o : outputs) {
			for(int i = 0 ; i < o.getNumberOfOutputs() ; i++)
				result+=o.getValue(i)+"\t";
		}
		return result+"\n";
		
	}
}
