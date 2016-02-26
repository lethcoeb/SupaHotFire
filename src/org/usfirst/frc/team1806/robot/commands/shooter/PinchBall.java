package org.usfirst.frc.team1806.robot.commands.shooter;

import org.usfirst.frc.team1806.robot.Constants;
import org.usfirst.frc.team1806.robot.Robot;
import org.usfirst.frc.team1806.robot.RobotStates.IntakeControlMode;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;

/**
 *
 */
public class PinchBall extends Command {
	
	Timer t;
	double kTimeToPinch = Constants.timeToPinch;
	
    public PinchBall() {
        requires(Robot.shooterSS);
        t = new Timer();
    }

    // Called just before this Command runs the first time
    protected void initialize() {
    	t.reset();
    	t.start();
    	Robot.shooterSS.pinchBall();
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
        return t.get() > kTimeToPinch;
    }

    // Called once after isFinished returns true
    protected void end() {
    	Robot.states.hasBall = true;
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
    	Robot.states.hasBall = true;
    }
}
