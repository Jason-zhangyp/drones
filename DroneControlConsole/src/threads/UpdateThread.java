package threads;

import gui.UpdatePanel;
import main.DroneControlConsole;
import network.messages.InformationRequest;
import network.messages.InformationRequest.MessageType;

public class UpdateThread extends Thread {
	
	protected UpdatePanel panel;
	protected DroneControlConsole console;
	protected boolean keepGoing = true;
	protected MessageType type;
	
	/**
	 * The UpdateThread is responsible for sending data requests, such as GPS, Compass or
	 * status messages, to the drone. It then waits for a response from the drone before another
	 * message is sent. This prevents the drone from being overrun with messages when connectivity  
	 * issues appear.
	 */
	public UpdateThread(DroneControlConsole console, UpdatePanel panel, MessageType type) {
		this.console = console;
		this.panel = panel;
		this.type = type;
		panel.registerThread(this);
	}
	
	@Override
	public void run() {

		while (keepGoing) {
			console.sendData(new InformationRequest(type));
			long timeAfterSending = System.currentTimeMillis();
			panel.threadWait();
			calculateSleep(timeAfterSending);
		}
	}
	
	/**
	 * Try to keep the desired refresh rate by taking into account
	 * the time lost waiting for an answer 
	 */
	private void calculateSleep(long timeAfterSending) {
		long timeElapsed = System.currentTimeMillis()-timeAfterSending;
		long sleepTime = panel.getSleepTime();
		
		long necessarySleep = sleepTime - timeElapsed;
		try {
			if(necessarySleep > 0)
				Thread.sleep(necessarySleep);
		} catch(InterruptedException e){}
	}
	
	public void stopExecuting() {
		keepGoing = false;
	}
}