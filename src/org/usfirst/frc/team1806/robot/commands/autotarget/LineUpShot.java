package org.usfirst.frc.team1806.robot.commands.autotarget;

import org.usfirst.frc.team1806.robot.Constants;
import org.usfirst.frc.team1806.robot.OperatorInterface;
import org.usfirst.frc.team1806.robot.Robot;
import org.usfirst.frc.team1806.robot.RobotStates.DriveControlMode;
import org.usfirst.frc.team1806.robot.commands.RumbleController;

import edu.wpi.first.wpilibj.command.Command;

/**
 *
 */
public class LineUpShot extends Command {

	boolean goalFound, withinRange, hasRumbled;
	
	boolean finished;
	double targetAngle;
	int stage;
	double loops = 0;
	double kLoopsUntilCheck = Constants.loopsToCheckSensorDisconnect;
	
    public LineUpShot() {
        requires(Robot.drivetrainSS);
    	Robot.states.autoLiningUp = true;

    }

    // Called just before this Command runs the first time
    protected void initialize() {
    	
    	//Robot.drivetrainSS.resetYaw();
    	goalFound = false;
    	withinRange = false;
    	hasRumbled = false;
    	
    	Robot.states.driveControlModeTracker = DriveControlMode.AUTO;
    	Robot.drivetrainSS.drivetrainTurnPIDReset();
    	Robot.drivetrainSS.resetYaw();
    	
    	if(Math.abs(targetAngle) > 15){
    		//use big ol' pid
    		Robot.drivetrainSS.drivetrainTurnPIDchangePID(Constants.drivetrainTurn1P, Constants.drivetrainTurn1I, Constants.drivetrainTurn1D);
    		Robot.drivetrainSS.drivetrainTurnPIDSetTolerance(Constants.drivetrainTurnPID3Tolerance);
    		Robot.drivetrainSS.drivetrainTurnPIDchangeMaxRotation(Constants.drivetrainMaxRotationPIDStage1);
    		stage = 3;
    	}else if(Math.abs(targetAngle) > 5){
    		Robot.drivetrainSS.drivetrainTurnPIDchangePID(Constants.drivetrainTurn2P, Constants.drivetrainTurn2I, Constants.drivetrainTurn2D);
    		Robot.drivetrainSS.drivetrainTurnPIDSetTolerance(Constants.drivetrainTurnPID2Tolerance);
    		Robot.drivetrainSS.drivetrainTurnPIDchangeMaxRotation(Constants.drivetrainMaxRotationPIDStage2);
    		stage = 2;
    	}else{
    		//you hella close bruh use dat small loop
    		Robot.drivetrainSS.drivetrainTurnPIDchangePID(Constants.drivetrainTurn3P, Constants.drivetrainTurn3I, Constants.drivetrainTurn3D);
    		Robot.drivetrainSS.drivetrainTurnPIDSetTolerance(Constants.drivetrainTurnPID1Tolerance);
    		Robot.drivetrainSS.drivetrainTurnPIDchangeMaxRotation(Constants.drivetrainMaxRotationPIDStage3);
    		stage = 1;
    	}
    	
    	Robot.drivetrainSS.drivetrainTurnPIDSetSetpoint(targetAngle);
    	Robot.drivetrainSS.drivetrainTurnPIDEnable();
    	System.out.println("auto line up started");
    	
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
    	
    	//System.out.println(Robot.jr.getAngleToGoal());
    	
    	if(!goalFound){
    		//allow driver to move the drivetrain to where the bot sees the goal
    		if(Robot.jr.isGoalFound()){
    			System.out.println("goal found!");
    			goalFound = true;
    			Robot.states.driveControlModeTracker = DriveControlMode.AUTO;
    			System.out.println(Robot.jr.getAngleToGoal());
    			Robot.drivetrainSS.drivetrainTurnPIDSetSetpoint(Robot.jr.getAngleToGoal());
        		Robot.drivetrainSS.drivetrainTurnPIDEnable();
        		
    		}
    	}else if(goalFound && !withinRange){
    		//found goal now line up
    		
    		loops++;
        	if(loops >= kLoopsUntilCheck){
        		loops = 0;
        		if(!Robot.drivetrainSS.isNavxConnected()){
        			//navx is dead bruh kill it
        			Robot.drivetrainSS.drivetrainTurnPIDDisable();
        			finished = true;
        		}
        	}
        	if(Robot.drivetrainSS.drivetrainTurnPIDisOnTarget()){
        		System.out.println("PID on target");
        		Robot.drivetrainSS.drivetrainTurnPIDDisable();
        		Robot.drivetrainSS.drivetrainTurnPIDReset();
        		if(stage == 3){
        			//step down to stage 2
        			stage = 2;
        			Robot.drivetrainSS.drivetrainTurnPIDchangePID(Constants.drivetrainTurn2P, Constants.drivetrainTurn2I, Constants.drivetrainTurn2D);
        			Robot.drivetrainSS.drivetrainTurnPIDSetTolerance(Constants.drivetrainTurnPID2Tolerance);
        			Robot.drivetrainSS.drivetrainTurnPIDchangeMaxRotation(Constants.drivetrainMaxRotationPIDStage2);
        			Robot.drivetrainSS.drivetrainTurnPIDEnable();
        			
        			System.out.println("moving to stage 2");
        		}else if(stage == 2){
        			//step down to stage 1
        			stage = 1;
        			Robot.drivetrainSS.drivetrainTurnPIDchangePID(Constants.drivetrainTurn3P, Constants.drivetrainTurn3I, Constants.drivetrainTurn3D);
        			Robot.drivetrainSS.drivetrainTurnPIDSetTolerance(Constants.drivetrainTurnPID1Tolerance);
        			Robot.drivetrainSS.drivetrainTurnPIDchangeMaxRotation(Constants.drivetrainMaxRotationPIDStage1);
        			Robot.drivetrainSS.drivetrainTurnPIDEnable();
        			
        			System.out.println("moving to stage 1");
        		}else if(stage == 1 && Robot.jr.isAngleAcceptable()){
        			//should be done.
        			withinRange = true;
        			System.out.println("stage one done, ON TARGET");
        		}
        	}
    		//else if(Robot.drivetrainSS.drivetrainTurnAbsolutePIDisOnTarget()){
    			/* If for some reason our original angle was wrong, being that we aren't in an
    			 * acceptable range according to the Jetson, yet we got to the angle it originally
    			 * said the goal was at, try to line up again to the new angle.
    			 * 
    			 * the PID is disabled and re-enabled in order to reset the navX and clear
    			 * accumulated error.
    			 */
    			/*Robot.drivetrainSS.drivetrainTurnPIDDisable();
    			Robot.drivetrainSS.drivetrainTurnPIDSetSetpoint(Robot.jr.getAngleToGoal());
    			Robot.drivetrainSS.drivetrainTurnPIDEnable();
    		}*/
    	}else if(goalFound && withinRange && !hasRumbled){
    		System.out.println("rumbled");
    		new RumbleController(Robot.oi.dc).start();
    		hasRumbled = true;
    	}
    	
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
        return (Robot.oi.dc.getRightTrigger() < .4);
    }

    // Called once after isFinished returns true
    protected void end() {
    	
    	System.out.println("lineupshot finished");
    	
    	Robot.drivetrainSS.drivetrainTurnPIDDisable();
    	Robot.states.autoLiningUp = false;
    	Robot.states.driveControlModeTracker = DriveControlMode.DRIVER;
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
    	Robot.drivetrainSS.drivetrainTurnPIDDisable();
    	Robot.states.autoLiningUp = false;
    	Robot.states.driveControlModeTracker = DriveControlMode.DRIVER;
    }
}
