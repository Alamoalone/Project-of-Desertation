// Copyright (c) 2023 FRC Team 2881 - The Lady Cans
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package frc.robot.subsystems;
/* Github testing */
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import frc.robot.Constants;
import frc.robot.lib.NavX;
// import frc.robot.lib.PhotonCameraWrapper;
import frc.robot.lib.SwerveModule;

public class Swerve extends SubsystemBase {
  // Create SwerveModules
  private final SwerveModule m_frontLeft = new SwerveModule(
      Constants.Swerve.kFrontLeftDrivingCanId,
      Constants.Swerve.kFrontLeftTurningCanId,
      Constants.Swerve.kFrontLeftCanCoderId,
      Constants.Swerve.kFrontLeftChassisAngularOffset);

  private final SwerveModule m_frontRight = new SwerveModule(
      Constants.Swerve.kFrontRightDrivingCanId,
      Constants.Swerve.kFrontRightTurningCanId,
      Constants.Swerve.kFrontRightCanCoderId, 
      Constants.Swerve.kFrontRightChassisAngularOffset);

  private final SwerveModule m_rearLeft = new SwerveModule(
      Constants.Swerve.kBackLeftDrivingCanId,
      Constants.Swerve.kBackLeftTurningCanId,
      Constants.Swerve.kBackLeftCanCoderId,
      Constants.Swerve.kBackLeftChassisAngularOffset);

  private final SwerveModule m_rearRight = new SwerveModule(
      Constants.Swerve.kBackRightDrivingCanId,
      Constants.Swerve.kBackRightTurningCanId,
      Constants.Swerve.kBackRightCanCoderId, 
      Constants.Swerve.kBackRightChassisAngularOffset);

  // The gyro sensor
  private final NavX m_gyro = new NavX();

  // public PhotonCameraWrapper m_photonCamera;

  //Functions the same as SwerveDriveOdometry
  private final SwerveDrivePoseEstimator m_poseEstimator = 
    new SwerveDrivePoseEstimator(
      Constants.Swerve.kDriveKinematics, 
      Rotation2d.fromDegrees(m_gyro.getAngle()), 
      new SwerveModulePosition[] {
        m_frontLeft.getPosition(),
        m_frontRight.getPosition(),
        m_rearLeft.getPosition(),
        m_rearRight.getPosition()}, 
      new Pose2d());

  private final Field2d m_fieldSim = new Field2d();
  
  //Default Speed
  public static final double output = 1;

  public static double m_output = output;

  //limits the acceleration of the x, y, and rotational speeds
  public SlewRateLimiter xLimiter = new SlewRateLimiter(Constants.Swerve.kMaxAccel);
  public SlewRateLimiter yLimiter = new SlewRateLimiter(Constants.Swerve.kMaxAccel);
  public SlewRateLimiter rotLimiter = new SlewRateLimiter(Constants.Swerve.kMaxAngularAccel);

  public Swerve() {
    // m_photonCamera = new PhotonCameraWrapper(
    //   Constants.Vision.kCameraName,
    //   Constants.Vision.kRobotToCamera,
    //   PoseStrategy.CLOSEST_TO_REFERENCE_POSE,
    //   Constants.Vision.kAprilTagFieldLayout
    // );
    
    SmartDashboard.putData("Field", m_fieldSim);
    SmartDashboard.setDefaultNumber("P", Constants.SwerveModule.kDrivingP);
    SmartDashboard.setDefaultNumber("D", Constants.SwerveModule.kDrivingD);
  }

  @Override
  public void periodic() {
    updatePose();

    SmartDashboard.putNumber("FrontLeft DrivingRelativePosition", m_frontLeft.getDrivingRelativePosition());
    SmartDashboard.putNumber("FrontLeft SteeringRelativePosition", m_frontLeft.getSteeringRelativePosition());
    SmartDashboard.putNumber("FrontLeft SteeringAbsolutePosition", m_frontLeft.getSteeringAbsolutePosition());
    SmartDashboard.putNumber("FrontLeft Driving Velocity", m_frontLeft.getDrivingVelocity());
    SmartDashboard.putNumber("FrontRight DrivingRelativePosition", m_frontRight.getDrivingRelativePosition());
    SmartDashboard.putNumber("FrontRight SteeringRelativePosition", m_frontRight.getSteeringRelativePosition());
    SmartDashboard.putNumber("FrontRight SteeringAbsolutePosition", m_frontRight.getSteeringAbsolutePosition());
    SmartDashboard.putNumber("FrontRight Driving Velocity", m_frontRight.getDrivingVelocity());
    SmartDashboard.putNumber("RearLeft DrivingRelativePosition", m_rearLeft.getDrivingRelativePosition());
    SmartDashboard.putNumber("RearLeft SteeringRelativePosition", m_rearLeft.getSteeringRelativePosition());
    SmartDashboard.putNumber("RearLeft SteeringAbsolutePosition", m_rearLeft.getSteeringAbsolutePosition());
        SmartDashboard.putNumber("RearLeft Driving Velocity", m_rearLeft.getDrivingVelocity());
    SmartDashboard.putNumber("RearRight DrivingRelativePosition", m_rearRight.getDrivingRelativePosition());
    SmartDashboard.putNumber("RearRight SteeringRelativePosition", m_rearRight.getSteeringRelativePosition());
    SmartDashboard.putNumber("RearRight SteeringAbsolutePosition", m_rearRight.getSteeringAbsolutePosition());
    SmartDashboard.putNumber("RearRight Driving Velocity", m_rearRight.getDrivingVelocity());

    SmartDashboard.putNumber("Drive NavX Angle", m_gyro.getAngle());
    SmartDashboard.putNumber("Drive NavX Yaw", m_gyro.getYaw());
    SmartDashboard.putNumber("Drive NavX Pitch", m_gyro.getPitch());
  
  }
   
  //updates the pose periodically
  public void updatePose() {
    m_poseEstimator.update(
      m_gyro.getRotation2d(), 
      new SwerveModulePosition[] {
        m_frontLeft.getPosition(),
        m_frontRight.getPosition(),
        m_rearLeft.getPosition(),
        m_rearRight.getPosition()
    });

    // Optional<EstimatedRobotPose> cameraResult = m_photonCamera.getEstimatedGlobalPose(m_poseEstimator.getEstimatedPosition());
    // if (leftCameraResult.isPresent()) {
    //   EstimatedRobotPose camPose = leftCameraResult.get();
    //   m_poseEstimator.addVisionMeasurement(camPose.estimatedPose.toPose2d(), camPose.timestampSeconds);
    //}

  //   m_fieldSim.setRobotPose(m_poseEstimator.getEstimatedPosition());
    }

   /**
    * Returns the currently-estimated pose of the robot.
    *
    * @return The pose.
    */
   public Pose2d getPose() {
     return m_poseEstimator.getEstimatedPosition();
   }

  public NavX getGyro()
  {
    return m_gyro;
  }

  public void slowMode()
  {
    m_frontLeft.evilMode();
    m_frontRight.evilMode();
    m_rearLeft.evilMode();
    m_rearRight.evilMode();

		m_output = 0.2;
  }

  public void mediumMode()
  {
    m_frontLeft.evilMode();
    m_frontRight.evilMode();
    m_rearLeft.evilMode();
    m_rearRight.evilMode();

		m_output = 0.5;
  }

  public void fastMode()
  {
    m_frontLeft.goodMode();
    m_frontRight.goodMode();
    m_rearLeft.goodMode();
    m_rearRight.goodMode();

		m_output = output;
  }

  /**
   * Resets the odometry to the specified pose.
   *
   * @param pose The pose to which to set the odometry.
   */
  public void resetPose(Pose2d pose) {
    m_poseEstimator.resetPosition(
        Rotation2d.fromDegrees(m_gyro.getAngle()),
        new SwerveModulePosition[] {
            m_frontLeft.getPosition(),
            m_frontRight.getPosition(),
            m_rearLeft.getPosition(),
            m_rearRight.getPosition()
        },
        pose);

      m_fieldSim.setRobotPose(m_poseEstimator.getEstimatedPosition());
  }

  /**
   * Method to drive the robot using joystick info.
   *
   * @param xSpeed        Speed of the robot in the x direction (forward).
   * @param ySpeed        Speed of the robot in the y direction (sideways).
   * @param rot           Angular rate of the robot.
   * @param fieldRelative Whether the provided x and y speeds are relative to the
   *                      field.
   */
  public void drive(double xSpeed, double ySpeed, double rot, boolean fieldRelative) {
    // Adjust input based on   speed
    // xSpeed *= Constants.Swerve.kMaxSpeedMetersPerSecond;
    // ySpeed *= Constants.Swerve.kMaxSpeedMetersPerSecond;
    // rot *= Constants.Swerve.kMaxAngularSpeed;

    //multiply by output to change the speed, useful for slow mode or medium slow mode
    xSpeed *= m_output;
    ySpeed *= m_output;
    rot *= m_output;


    // double deadband = Constants.Swerve.kMaxSpeedMetersPerSecond / 10;
    // xSpeed = deadBand(xSpeed, deadband);
    // ySpeed = deadBand(ySpeed, deadband);
    
    // double deadbandRot = 2 * Math.PI / 9.45;
    // rot = deadBand(rot, deadbandRot);

    //returns the modified speed if rate of change is above maximum
    xSpeed = xLimiter.calculate(xSpeed);
    ySpeed = yLimiter.calculate(ySpeed);
    rot = rotLimiter.calculate(rot);

    //converts your desired chassis speeds into the appropriate speed and angles 
    //for each swerve module with the given kinematics
    var swerveModuleStates = Constants.Swerve.kDriveKinematics.toSwerveModuleStates(
        fieldRelative
            ? ChassisSpeeds.fromFieldRelativeSpeeds(xSpeed, ySpeed, rot, Rotation2d.fromDegrees(-m_gyro.getAngle()))
            : new ChassisSpeeds(xSpeed, ySpeed, rot));

    //limits the wheel speeds
    SwerveDriveKinematics.desaturateWheelSpeeds(
        swerveModuleStates, Constants.Swerve.kMaxSpeedMetersPerSecond);
    
    //sets these wheels to the desired speed and angle
    m_frontLeft.setDesiredState(swerveModuleStates[2]);
    m_frontRight.setDesiredState(swerveModuleStates[3]);
    m_rearLeft.setDesiredState(swerveModuleStates[0]);
    m_rearRight.setDesiredState(swerveModuleStates[1]);
  }

  public double getDistanceMeters()
	{
		// return 0.0;
		return Math.max(
      Math.max(Math.abs(m_frontLeft.getDrivingRelativePosition()), Math.abs(m_frontRight.getDrivingRelativePosition())),
      Math.max(Math.abs(m_rearLeft.getDrivingRelativePosition()), Math.abs(m_rearRight.getDrivingRelativePosition())));
	}

  private static double deadBand(double value, double deadband)
  {
    if (Math.abs(value) > deadband)
    {
      if (value > 0.0)
        return (value - deadband)/(1.0 - deadband);
      else
        return (value + deadband)/(1.0 - deadband);
    }
    else
      return 0.0;
  }

  /**
   * Sets the wheels into an X formation to prevent movement.
   */
  public void setX() {
    m_frontLeft.setDesiredState(new SwerveModuleState(0, Rotation2d.fromDegrees(45)));
    m_frontRight.setDesiredState(new SwerveModuleState(0, Rotation2d.fromDegrees(-45)));
    m_rearLeft.setDesiredState(new SwerveModuleState(0, Rotation2d.fromDegrees(-45)));
    m_rearRight.setDesiredState(new SwerveModuleState(0, Rotation2d.fromDegrees(45)));
  }

  /**
   * Sets the swerve ModuleStates.
   *
   * @param desiredStates The desired SwerveModule states.
   */
  public void setModuleStates(SwerveModuleState[] desiredStates) {
    SwerveDriveKinematics.desaturateWheelSpeeds(
        desiredStates, Constants.Swerve.kMaxSpeedMetersPerSecond);
    m_frontLeft.setDesiredState(desiredStates[2]);
    m_frontRight.setDesiredState(desiredStates[3]);
    m_rearLeft.setDesiredState(desiredStates[0]);
    m_rearRight.setDesiredState(desiredStates[1]);
  }

  /** Resets the drive encoders to currently read a position of 0. */
  public void resetEncoders() {
    m_frontLeft.resetEncoders();
    m_rearLeft.resetEncoders();
    m_frontRight.resetEncoders();
    m_rearRight.resetEncoders();
  }

  /** Zeroes the heading of the robot. */
  public void zeroHeading() {
    m_gyro.reset();
  }
  

  /**
   * Returns the heading of the robot.
   *
   * @return the robot's heading in degrees, from -180 to 180
   */
  public double getHeading() {
    return Rotation2d.fromDegrees(m_gyro.getAngle()).getDegrees();
  }

  /**
   * Returns the turn rate of the robot.
   *
   * @return The turn rate of the robot, in degrees per second
   */
  public double getTurnRate() {
    return m_gyro.getRate() * (Constants.Swerve.kGyroReversed ? -1.0 : 1.0);
  }

  @Override
  public void initSendable(SendableBuilder builder) {
    super.initSendable(builder);
    m_gyro.initSendable(builder);
  }

  public void stopModules() {
    m_frontLeft.setDesiredState(new SwerveModuleState(0, m_frontLeft.getState().angle));
    m_frontRight.setDesiredState(new SwerveModuleState(0, m_frontRight.getState().angle));
    m_rearLeft.setDesiredState(new SwerveModuleState(0, m_rearLeft.getState().angle));
    m_rearRight.setDesiredState(new SwerveModuleState(0, m_rearRight.getState().angle));
  }

  public void setToCurrentPosition()
  {
    m_frontLeft.getTurningEncoder().setPosition(Math.toRadians(m_frontLeft.getSteeringAbsolutePosition()));
    m_frontRight.getTurningEncoder().setPosition(Math.toRadians(m_frontRight.getSteeringAbsolutePosition()));
    m_rearLeft.getTurningEncoder().setPosition(Math.toRadians(m_rearLeft.getSteeringAbsolutePosition()));
    m_rearRight.getTurningEncoder().setPosition(Math.toRadians(m_rearRight.getSteeringAbsolutePosition()));
  }
  // private static ChassisSpeeds fieldRelativeSpeeds(double vxMetersPerSecond,
  // double vyMetersPerSecond,
  // double omegaRadiansPerSecond,
  // Rotation2d robotAngle) //Math for ChassisSpeeds.fromFieldRelativeSpeeds() could be incorrect.
  // {
  //   return new ChassisSpeeds(
  //     vxMetersPerSecond * robotAngle.getCos() + vyMetersPerSecond * robotAngle.getSin(),
  //     -vxMetersPerSecond * robotAngle.getSin() + vyMetersPerSecond * robotAngle.getCos(),
  //     omegaRadiansPerSecond);
  // }

}
