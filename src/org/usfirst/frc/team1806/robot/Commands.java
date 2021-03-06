package org.usfirst.frc.team1806.robot;

public class Commands {
	
	public Commands(){
		reset();
	}
	
	public enum RunIntakeCommand{
		INTAKE, OUTTAKE, STOP
	}
	
	public enum DeployIntakeCommand{
		DEPLOY, RETRACT, STAY, SWITCH
	}
	
	public enum ShiftRequest{
		LOW, HIGH, NONE
	}
	
	public enum ElevatorPositionRequest{
		NONE, CHEVAL, OUTERWORKS, BATTER, HOLDING
	}
	
	public enum ShootRequest{
		SHOOT, NONE
	}
	
	public enum ElevatorControlMode{
		AUTO, MANUAL, NONE
	}
	
	public enum ManualCockCommand{
		COCK, NONE
	}
	
	public enum ArmDefenseCommand{
		CHEVALDEFUN, LOWBAR, NONE;
	}
	
	public boolean autoLineUp;
	
	public RunIntakeCommand intakeCommandTracker;
	public DeployIntakeCommand deployIntakeCommandTracker;
	public ShiftRequest shiftRequestCommandTracker;
	public ElevatorPositionRequest elevatorPositionRequestTracker;
	public ShootRequest shootRequestTracker;
	public ElevatorControlMode elevatorControlModeTracker;
	public ManualCockCommand manualCockCommandTracker;
	public ArmDefenseCommand armDefenseCommandTracker;
	
	public void reset(){
		intakeCommandTracker = RunIntakeCommand.STOP;
		deployIntakeCommandTracker = DeployIntakeCommand.STAY;
		shiftRequestCommandTracker = ShiftRequest.NONE;
		elevatorPositionRequestTracker = ElevatorPositionRequest.NONE;
		shootRequestTracker = ShootRequest.NONE;
		elevatorControlModeTracker = ElevatorControlMode.AUTO;
		manualCockCommandTracker = ManualCockCommand.NONE;
		armDefenseCommandTracker = ArmDefenseCommand.NONE;
		autoLineUp = false;
	}
	
}
