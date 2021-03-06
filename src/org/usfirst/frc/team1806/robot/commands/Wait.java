package org.usfirst.frc.team1806.robot.commands;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;

/**
 *
 */
public class Wait extends Command {
	
	Timer t;
	double kWaitSeconds;
	
    public Wait(double seconds) {
        t = new Timer();
        kWaitSeconds = seconds;
        t.reset();
        t.start();
    }

    // Called just before this Command runs the first time
    protected void initialize() {
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
        return t.get() >= kWaitSeconds;
    }

    // Called once after isFinished returns true
    protected void end() {
    	System.out.println("Wait() command finished.");
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
    }
}
