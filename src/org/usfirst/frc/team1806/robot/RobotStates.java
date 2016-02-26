package org.usfirst.frc.team1806.robot;

public class RobotStates {
	
	public RobotStates(){
		reset();
	}
	
	public enum IntakePosition{
		DEPLOYED, RETRACTED, MOVING
	}
	
	public enum ShooterArmPosition{
		DOWN, UP, HOLDING, OTHER
	}
	
	public enum DriveControlMode{
		DRIVER, AUTO
	}
	
	public enum DrivetrainGear{
		HIGH, LOW
	}
	
	public enum ShooterCocked{
		COCKED, NOTCOCKED
	}
	
	public enum IntakeRollerState{
		INTAKING, OUTTAKING, STOPPED
	}
	
	public enum ElevatorOperatorControlMode{
		AUTO, MANUAL
	}
	
	public enum VisionTrackingState{
		JETSON, ROBORIO, NONE
	}
	
	public enum IntakeControlMode{
		DRIVER, AUTOMATIC
	}
	
	public boolean hasBall = true;
	
	public IntakePosition intakePositionTracker;
	public ShooterArmPosition shooterArmPositionTracker;
	public DriveControlMode driveControlModeTracker;
	public DrivetrainGear drivetrainGearTracker;
	public ShooterCocked shooterCockedTracker;
	public IntakeRollerState intakeRollerStateTracker;
	public ElevatorOperatorControlMode elevatorOperatorControlModeTracker;
	public VisionTrackingState visionTrackingStateTracker;
	public IntakeControlMode intakeControlModeTracker;
	
	
	public void reset(){
		intakePositionTracker = IntakePosition.RETRACTED;
		shooterArmPositionTracker = ShooterArmPosition.UP;
		driveControlModeTracker = DriveControlMode.DRIVER;
		drivetrainGearTracker = DrivetrainGear.LOW;
		shooterCockedTracker = ShooterCocked.COCKED;
		intakeRollerStateTracker = IntakeRollerState.STOPPED;
		elevatorOperatorControlModeTracker = ElevatorOperatorControlMode.AUTO;
		visionTrackingStateTracker = VisionTrackingState.JETSON;
		intakeControlModeTracker = IntakeControlMode.DRIVER;
		hasBall = true;
	}
	
}
