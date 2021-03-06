package org.usfirst.frc.team1806.robot.commands.shooter;

import org.usfirst.frc.team1806.robot.Constants;
import org.usfirst.frc.team1806.robot.Robot;
import org.usfirst.frc.team1806.robot.RobotMap;
import org.usfirst.frc.team1806.robot.RobotStates.ShooterCocked;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;

/**
 *
 */
public class ManualCock extends Command {

	Timer t;
	boolean cocking = false;
	
	boolean finished = false;
	
    public ManualCock() {
        requires(Robot.shooterSS);
    }

    // Called just before this Command runs the first time
    protected void initialize() {
    	
    	t = new Timer();
    	t.reset();
    	t.start();
    	
    	Robot.shooterSS.cockingDogGearEngage();
    	
    	//engage gear, start pulling
    	if(Robot.shooterSS.isShooterCocked()){
    		
    		Robot.shooterSS.cockShooterEngageGear();
    	}
    	
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
    	
    	if(Robot.shooterSS.isShooterCocked()){
    		finished = true;
    	}
    	
    	if(!cocking && t.get() > Constants.timeToEngageDogGear){
    		//you've waited for long enough, the gear is engaged fam, COCK THAT SHIT BRUH
    		cocking = true;
    		Robot.shooterSS.cockShooterFullSpeed();
    	}
    	
    	if(Robot.pdp.getCurrent(RobotMap.PDPcockingWinchSlot) > 100){
    		//kill pls
    		Robot.states.overCocked = true;
    		finished = true;
    	}
    	
    	
    	
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
        return finished;
    }

    // Called once after isFinished returns true
    protected void end() {
    	Robot.shooterSS.stopCocking();
    	Robot.states.autoalignmentShooting = false;
    	Robot.states.shooterCockedTracker = ShooterCocked.COCKED;
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
    	Robot.shooterSS.stopCocking();
    }
}
