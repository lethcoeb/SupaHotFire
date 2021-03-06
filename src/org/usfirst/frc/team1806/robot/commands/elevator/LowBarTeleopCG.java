package org.usfirst.frc.team1806.robot.commands.elevator;

import org.usfirst.frc.team1806.robot.commands.intake.IntakeRunAtSpeed;

import edu.wpi.first.wpilibj.command.CommandGroup;

/**
 *
 */
public class LowBarTeleopCG extends CommandGroup {
    
    public  LowBarTeleopCG() {
        
    	addSequential(new SetStateLowBarring(true));
    	addSequential(new IntakeRunAtSpeed(-.3));
    	addSequential(new MoveToLocationPID(0));
    	addParallel(new IntakeRunAtSpeed(0));
    	addSequential(new SetStateGrab());
    	
    }
}
