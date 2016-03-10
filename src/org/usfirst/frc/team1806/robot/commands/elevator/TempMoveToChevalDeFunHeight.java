package org.usfirst.frc.team1806.robot.commands.elevator;

import org.usfirst.frc.team1806.robot.Constants;
import org.usfirst.frc.team1806.robot.Robot;

import edu.wpi.first.wpilibj.CANTalon.TalonControlMode;
import edu.wpi.first.wpilibj.command.Command;

/**
 *
 */
public class TempMoveToChevalDeFunHeight extends Command {

	//bruh will this work it needs to listen for a button
	
    public TempMoveToChevalDeFunHeight() {
        requires(Robot.elevatorSS);
    }

    // Called just before this Command runs the first time
    protected void initialize() {
    	Robot.elevatorSS.elevatorSetControlMode(TalonControlMode.Position);
    	Robot.elevatorSS.elevatorSetSetpoint(Constants.elevatorChevaldeFunHeight);
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
    	
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
    	return !Robot.oi.oc.getPOVUp();
    }

    // Called once after isFinished returns true
    protected void end() {
    	new MoveToHoldingPID().start();
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
    	System.out.println("Cheval de fun interrupted");
    	
    	
    }
}
