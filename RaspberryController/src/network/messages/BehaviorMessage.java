package network.messages;

import behaviors.Behavior;

public class BehaviorMessage extends Message{
	
	protected Class<Behavior> selectedBehavior;
	
	protected boolean changeStatus = false;
	protected boolean selectedStatus = false;
	
	protected boolean setArgument = false;
	protected int argumentIndex = 0;
	protected double argumentValue = 0;
	
	public BehaviorMessage(Class<Behavior> selectedBehavior, int argumentIndex, double argumentValue) {
		this.selectedBehavior = selectedBehavior;
		this.setArgument = true;
		this.argumentIndex = argumentIndex;
		this.argumentValue = argumentValue;
	}
	
	public BehaviorMessage(Class<Behavior> selectedBehavior, boolean selectedStatus) {
		this.selectedBehavior = selectedBehavior;
		this.changeStatus = true;
		this.selectedStatus = selectedStatus;
	}
	
	public boolean changeStatusOrder() {
		return changeStatus;
	}
	
	public boolean changeArgumentOrder() {
		return setArgument;
	}
	
	public boolean getSelectedStatus() {
		return selectedStatus;
	}
	
	public int getArgumentIndex() {
		return argumentIndex;
	}
	
	public double getArgumentValue() {
		return argumentValue;
	}
	
	public Class<Behavior> getSelectedBehavior() {
		return selectedBehavior;
	}
}