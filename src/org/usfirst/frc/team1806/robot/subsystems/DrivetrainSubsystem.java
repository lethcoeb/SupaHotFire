package org.usfirst.frc.team1806.robot.subsystems;

import org.usfirst.frc.team1806.robot.Constants;
import org.usfirst.frc.team1806.robot.Robot;
import org.usfirst.frc.team1806.robot.RobotMap;
import org.usfirst.frc.team1806.robot.RobotStates.DrivetrainGear;
import org.usfirst.frc.team1806.robot.commands.DriverControlDrivetrain;

import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.PIDSourceType;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.SerialPort.Port;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Subsystem;

/**
 *
 */
public class DrivetrainSubsystem extends Subsystem {
    
    Talon right1, right2, right3, left1, left2, left3;
    DoubleSolenoid shifter;
    Encoder rightEncoder, leftEncoder;
    AHRS navx;
    
    PIDSource drivePS;
    PIDOutput drivePO;
    PIDController drivePC;
    
    PIDSource turnPS;
    PIDSource turnAbsolutePS;
    PIDOutput turnPO;
    PIDController turnPC;
    PIDController turnAbsolutePC;
    
    final double kJoystickDeadzoneConstant = Constants.joystickDeadzone;
    double lastPower, currPower, lastTurnPower, currTurnPower = 0;
    double PIDTolerance = 30;
    double MaxRotationPID = Constants.drivetrainMaxRotationPIDStage1;
    double maxSpeed = 1;
    
    
    double currentEncoder;
    double lastEncoder;
	Boolean liningUpShot;
	double lastTrackedTime;
	double timeSinceLastAutoShift;
	double lastAutoShift;
	double period;
	Timer periodTimer;
	Timer autoShifting;
    Boolean autoShift = false;

    public DrivetrainSubsystem(){
    	currentEncoder = 0;
    	lastEncoder = 0;
    	lastTrackedTime = 0;
    	timeSinceLastAutoShift = 0;
		periodTimer = new Timer();
		periodTimer.start();
    	right1 = new Talon(RobotMap.rightMotor1);
    	right2 = new Talon(RobotMap.rightMotor2);
    	right3 = new Talon(RobotMap.rightMotor3);
    	left1 = new Talon(RobotMap.leftMotor1);
    	left2 = new Talon(RobotMap.leftMotor2);
    	left3 = new Talon(RobotMap.leftMotor3);
    	
    	
    	//TODO Make sure this is the right port for the shifter on comp bot
    	shifter = new DoubleSolenoid(RobotMap.shiftLow, RobotMap.shiftHigh);
    	//shifter = new DoubleSolenoid(1, RobotMap.shiftLow, RobotMap.shiftHigh);
    	
    	rightEncoder = new Encoder(RobotMap.rightEncoderA, RobotMap.rightEncoderB);
    	leftEncoder = new Encoder(RobotMap.leftEncoderA, RobotMap.leftEncoderB);
    	//leftEncoder.setReverseDirection(true);
    	rightEncoder.setDistancePerPulse(Constants.encoderCountsPerRevolution);
    	leftEncoder.setDistancePerPulse(Constants.encoderCountsPerRevolution);
    	
    	
    	
    	navx = new AHRS(Port.kMXP);
    	
    	drivePS = new PIDSource() {
			
			@Override
			public void setPIDSourceType(PIDSourceType pidSource) {
				setPIDSourceType(PIDSourceType.kDisplacement);
			}
			
			@Override
			public double pidGet() {
				//FIXME use two encoders
				return getRightEncoderDistance();
			}
			
			@Override
			public PIDSourceType getPIDSourceType() {
				return PIDSourceType.kDisplacement;
			}
		};
		
		drivePO = new PIDOutput() {
			
			@Override
			public void pidWrite(double output) {
				
				if(Math.abs(output) > maxSpeed){
					output = maxSpeed * Math.signum(output);
				}
				execute(output, getYaw() * .05);
			}
		};
		
		
		turnPS = new PIDSource() {
			
			@Override
			public void setPIDSourceType(PIDSourceType pidSource) {
				setPIDSourceType(PIDSourceType.kDisplacement);
			}
			
			@Override
			public double pidGet() {
				return navx.getYaw();
			}
			
			@Override
			public PIDSourceType getPIDSourceType() {
				return PIDSourceType.kDisplacement;
			}
		};
		
		turnAbsolutePS = new PIDSource() {
			
			@Override
			public void setPIDSourceType(PIDSourceType pidSource) {
				setPIDSourceType(PIDSourceType.kDisplacement);
			}
			
			@Override
			public double pidGet() {
				return navx.getAngle();
			}
			
			@Override
			public PIDSourceType getPIDSourceType() {
				// TODO Auto-generated method stub
				return PIDSourceType.kDisplacement;
			}
		};
		
		turnPO = new PIDOutput() {
			
			@Override
			public void pidWrite(double output) {
				/*pass the opposite of the value from the PID to the turn value of the drivetrain.
				If the value isn't zero, we need to re-scale it to be between the minimum power to cause movement and max power
				Otherwise, just pass the 0 value to the drivetrain.
				*/
				/*if(output != 0){
					output = output * (1-Constants.drivetrainTurnMinPowerToMove) + Constants.drivetrainTurnMinPowerToMove * Math.signum(output);
				}*/
				execute(0, -output);
				System.out.println(-output);
			}
		};
		
		drivePC = new PIDController(Constants.drivetrainDriveP, Constants.drivetrainDriveI, Constants.drivetrainDriveD, drivePS, drivePO);
		turnPC = new PIDController(Constants.drivetrainTurn3P, Constants.drivetrainTurn3I, Constants.drivetrainTurn3D, turnPS, turnPO);
		turnAbsolutePC = new PIDController(Constants.drivetrainTurn3P, Constants.drivetrainTurn3I,Constants.drivetrainTurn3D, turnAbsolutePS, turnPO);
		
		
		drivePC.setContinuous(false);
		drivePC.setOutputRange(-1, 1);
		drivePC.setAbsoluteTolerance(Constants.drivetrainDrivePIDTolerance);
		
		turnPC.setContinuous(true);
		turnPC.setInputRange(-180, 180);
		turnPC.setOutputRange(-1, 1);
		turnPC.setAbsoluteTolerance(Constants.drivetrainTurnPID1Tolerance);
		
		turnAbsolutePC.setContinuous(true);
		turnAbsolutePC.setInputRange(-180, 180);
		turnAbsolutePC.setOutputRange(-1, 1);
		turnAbsolutePC.setAbsoluteTolerance(Constants.drivetrainTurnPID1Tolerance);
		
    }
    
    private void setLeft(double speed){
    	left1.set(speed);
    	left2.set(speed);
    	left3.set(speed);
    }
    
    private void setRight(double speed){
    	right1.set(speed);
    	right2.set(speed);
    	right3.set(speed);
    }
    
    public double zoneInput(double val){
    	
    	if(Math.abs(val) < kJoystickDeadzoneConstant){
    		val = 0;
    	}
    	
    	return val;
    }
    
    public void execute(double power, double turn){
    	lastEncoder = currentEncoder;
		currentEncoder = getRightEncoderDistance();				//This stuff is for all of the period stuff for acceleration
		
		lastPower = currPower;
		currPower = power;
		
		lastTurnPower = currTurnPower;
		currTurnPower = turn;
		
		period = periodTimer.get() - lastTrackedTime;
		lastTrackedTime = periodTimer.get();					//Period timer stuffs
		
		lastPower = currPower;
		currPower = power;
		
		lastTurnPower = currTurnPower;
		currTurnPower = turn;
    	
    	if(Math.abs(currPower - lastPower) > Constants.maxPowerDiffential){
    		if(currPower > lastPower){
        		currPower = lastPower + Constants.maxPowerDiffential;
    		}else{
        		currPower = lastPower - Constants.maxPowerDiffential;
    		}
    	}
    	
    	if(Math.abs(currTurnPower - lastTurnPower) > Constants.maxTurnPowerDifferential){
    		if(currTurnPower > lastTurnPower){
        		currTurnPower = lastTurnPower + Constants.maxTurnPowerDifferential;
    		}else{
        		currTurnPower = lastTurnPower - Constants.maxTurnPowerDifferential;
    		}
    	}
    	if(autoShift && !Robot.oi.dc.getButtonRS()){
    		newShift();
    	} else if(!autoShift && Robot.oi.dc.getButtonRS()){					//Auto shifting TODO please check RS click for manual shifting
    		shiftHigh();
    	} else {
    		shiftLow();
    	}
    	
    	arcadeDrive(currPower, currTurnPower);

    }
    
    public void arcadeDrive(double power, double turn){
    	setLeft(power - turn);
    	setRight(power + turn);
    }
    
    public void arcadeRight(double power, double turn){
    	setLeft(0);
    	setRight(power + turn);
    }
    
    public void shiftHigh(){
    	shifter.set(Value.kForward);
    	Robot.states.drivetrainGearTracker = DrivetrainGear.HIGH;
    }
    
    public void shiftLow(){
    	shifter.set(Value.kReverse);
    	Robot.states.drivetrainGearTracker = DrivetrainGear.LOW;
    }
    
    public double getRightEncoderDistance(){
    	return -rightEncoder.getDistance();
    }
    
    public double getLeftEncoderDistance(){
    	return leftEncoder.getDistance();
    }
    
    public double getAverageEncoderDistance(){
    	return (rightEncoder.getDistance() + leftEncoder.getDistance())/2;
    }
    
    public void drivetrainDrivePIDSetMaxSpeed(double speed){
    	maxSpeed = speed;
    }
    
    public void drivetrainDrivePIDResetMaxSpeed(){
    	maxSpeed = 1;
    }
    
    public void resetEncoders(){
    	leftEncoder.reset();
    	rightEncoder.reset();
    }
    //NAVX
    
    public void resetYaw(){
    	navx.zeroYaw();
    }
    
    public void resetNavx(){
    	navx.reset();
    }
    
    public boolean isNavxConnected(){
    	return navx.isConnected();
    }
    
    public boolean isNavxFlat(){
    	return Math.abs(navx.getRoll()) < Constants.navxMinPitchToBeFlat;
    }
    
    public double getTrueAngle(){
    	return navx.getAngle();
    }
    
    
    public double getYaw(){
    	return navx.getYaw();
    }
    
    public double getPitch(){
    	return navx.getPitch();
    }
    
    public double getRoll(){
    	return navx.getRoll();
    }
    
    public double getRotationalSpeed(){
    	return navx.getRate();
    }
    
    public double getQuaternion(){
    	return navx.getQuaternionZ() * 180;
    }
    
    public double getTilt(){
    	return Math.sqrt(Math.pow(navx.getPitch(), 2) + Math.pow(navx.getRoll(), 2));
    }
    
    public void drivetrainControlLoopsDisable(){
    	drivePC.disable();
    	turnPC.disable();
    	turnAbsolutePC.disable();
    	drivePC.reset();
    	turnPC.reset();
    	turnAbsolutePC.reset();
    }
    
    public void drivetrainDrivePIDEnable(){
    	if(turnPC.isEnabled()){
    		turnPC.disable();
    	}
    	
    	if(turnAbsolutePC.isEnabled()){
    		turnAbsolutePC.disable();
    	}
    	
    	drivePC.enable();
    }
    
    public void drivetrainDrivePIDDisable(){
    	drivePC.disable();
    	drivePC.reset();
    }
    
    public void drivetrainDrivePIDSetSetpoint(double setpoint){
    	drivePC.setSetpoint(setpoint);
    }
    
    public boolean drivetrainDrivePIDisOnTarget(){
    	return Math.abs(drivePC.getError()) < Constants.drivetrainDrivePIDTolerance;
    }
    
    public void drivetrainTurnPIDEnable(){
    	if(drivePC.isEnabled()){
    		drivePC.disable();
    	}
    	
    	if(turnAbsolutePC.isEnabled()){
    		turnAbsolutePC.disable();
    	}
    	turnPC.enable();
    }
    
    public void drivetrainTurnPIDDisable(){
    	turnPC.disable();
    }
    
    public void drivetrainTurnPIDSetSetpoint(double setpoint){
    	turnPC.setSetpoint(setpoint);
    }
    
    public boolean drivetrainTurnPIDisOnTarget(){
    	return Math.abs(turnPC.getError()) < PIDTolerance && Math.abs(getRotationalSpeed()) < MaxRotationPID;
    }
    
    public double getTurnPCError(){
    	return turnPC.getError();
    }
    
    public void drivetrainTurnPIDchangePID(double p, double i, double d){
    	turnPC.setPID(p, i, d);
    }
    
    public void drivetrainTurnPIDchangeMaxRotation(double maxRot){
    	MaxRotationPID = maxRot;
    }
    
    public void drivetrainTurnPIDReset(){
    	turnPC.reset();
    }
    
    public void drivetrainTurnPIDSetTolerance(double tolerance){
    	PIDTolerance = tolerance;
    }
    
    public void drivetrainTurnAbsolutePIDEnable(){
    	if(drivePC.isEnabled()){
    		drivePC.disable();
    	}
    	
    	if(turnPC.isEnabled()){
    		turnPC.disable();
    	}
    	turnAbsolutePC.reset();
    	turnAbsolutePC.enable();
    }
    
    public void drivetrainTurnAbsolutePIDDisable(){
    	turnAbsolutePC.disable();
    	turnAbsolutePC.reset();
    }
    
    public void drivetrainTurnAbsolutePIDSetSetpoint(double setpoint){
    	turnAbsolutePC.setSetpoint(setpoint);
    }
    
    public boolean drivetrainTurnAbsolutePIDisOnTarget(){
    	return Math.abs(turnAbsolutePC.getError()) < PIDTolerance && Math.abs(getRotationalSpeed()) < MaxRotationPID;
    }
    
    public void drivetrainTurnAbsolutePIDchangePID(double p, double i, double d){
    	turnAbsolutePC.setPID(p, i, d);
    }
    
    public void drivetrainTurnAbsolutePIDchangeMaxRotation(double maxRot){
    	MaxRotationPID = maxRot;
    }
    
    public void drivetrainTurnAbsolutePIDReset(){
    	turnAbsolutePC.reset();
    }
    
    public void drivetrainTurnAbsolutePIDSetTolerance(double tolerance){
    	PIDTolerance = tolerance;
    }
    
    //////////////////////////////////////////////
	public boolean isInLowGear() {
		return Robot.states.drivetrainGearTracker == DrivetrainGear.LOW;
	}
	public boolean isInHighGear() {
		return Robot.states.drivetrainGearTracker == DrivetrainGear.HIGH;
	}
	public double getDriveSpeedFPS() {
		return Math.abs(getDriveVelocityFPS());
	}
	public double getDriveVelocityFPS() {
		// TODO: This should convert to fps but fix it if it doesn't
		
		//FOR NAVX WHICH SUCKS
		//return getDriveVelocity() * 3.28083989501;
		
		 return getDriveVelocity() / 12;
	}
	public double getDriveVelocity() {
		// returns the average speed of the left and right side in inches per
		// second
		
		
		// getVelocity is in m/s
		
		//return SWATLib.convertTo2DVector(navX.getVelocityX(), navX.getVelocityY());
		//return navx.getVelocityX();
		
		// Old version, using encoders:
		return ((currentEncoder - lastEncoder) / period);
	}
	public boolean isSpeedingUp() {
		return getDriveAccelFPSPS() > Constants.drivetrainAccelerationThreshold;
	}
	public double getDriveAccelFPSPS() {		
		//return navx.getRawAccelX();
		// THIS WILL NOT WORK!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		return (currPower - lastPower);
	}
	public boolean isSlowingDown() {
		return getDriveAccelFPSPS() < -Constants.drivetrainAccelerationThreshold;
	}
	public boolean isAutoShifting(){
		return autoShift;
	}
	public void disableAutoShift() {
		autoShift = false;
	}
	public void enableAutoShift() {
		autoShift = true;
	}

	private void newShift(){
		if(autoShifting.get() > .5 || autoShifting.get() == 0){
			if(getDriveSpeedFPS() > Constants.drivetrainUpshiftSpeedThreshold && Math.abs(currPower) > (Constants.drivetrainUpshiftPowerThreshold + .25) 
				&& isInLowGear()){
				
				/*
			 	* If it pushes the current value to over the power (The joystick) threshold 
			 	* and its in low gear, then it'll shift to high hear.
			 	* Also accounts for acceleration so its lit
			 	* 
			 	* I just gotta make sure not to hit the robot on the door :(
			 	*/
				shiftHigh();
				autoShifting.reset();
				autoShifting.start();
			}

			if(getDriveSpeedFPS() < Constants.drivetrainDownshiftSpeedThreshold 
					&& Math.abs(currPower) < (Constants.drivetrainPowerDownshiftPowerThreshold + .1) && isInHighGear()){
				/*
				 * If it goes under the PowerDownShiftPowerThreshhold 
				 * and its in high gear, then we shift to low gear
				 * Also accounts for acceleration
				 */
				shiftLow();
				autoShifting.reset();
				autoShifting.start();
			}
		}
	}
    
    public void initDefaultCommand() {
    	
    	setDefaultCommand(new DriverControlDrivetrain());
        // Set the default command for a subsystem here.
        //setDefaultCommand(new MySpecialCommand());
    }
}

