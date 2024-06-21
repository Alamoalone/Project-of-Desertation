// Copyright (c) 2023 FRC Team 2881 - The Lady Cans
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.
/* Github testing */
package frc.robot;

import java.io.IOException;

import com.revrobotics.CANSparkBase.IdleMode;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.util.Units;
import frc.robot.subsystems.Apriltags;
import frc.robot.subsystems.PneumaticsSubsystem;


/** Add your docs here. */
public class Constants {

    public static final class Game
    {
        public static final double kBottomRowHeight = Units.inchesToMeters(5);
        public static final double kMiddleRowHeight = Units.inchesToMeters(34);
        public static final double kTopRowHeight = Units.inchesToMeters(46);

    }

    public static final class Controllers {
        public static final int kDriverControllerPort = 0; 
        public static final int kOperatorControllerPort = 1; 
        public static final double kDeadband = 0.1; 
    }
    
    public static final class Arm {
        //TODO: Change the arm length
        public static final double kArmlength = Units.inchesToMeters(28.25);
        public static final double kArmlengthExtended = Units.inchesToMeters(40);
        public static final double kArmReduction = 1;
        public static final double kArmEncoderPositionFactor = (2 * Math.PI);
        public static final double kArmEncoderVelocityFactor = kArmEncoderPositionFactor/60.0;
        //TODO: Tune this! "solve for average radius"
        public static final double kArmLengthConversionFactor = 2*Math.PI/62;
        public static final double kArmLengthVelocityFactor = kArmLengthConversionFactor/60.0;

        public static final double kMaxExtensionEncoderTicks = 35; //2502.921142578125 //2480
        
        //Measured in inches of rope (Need to Tune)
        public static final double kBottomLength = .7;
        public static final double kMiddleLength = 30;
        public static final double kTopLength = 40;

        //Measured in Rotations (Need to Tune)
        public static final double kBottomAngle = .1;
        public static final double kMiddleAngle = .3;
        public static final double kTopAngle = .4;

        //TODO: Update this MotorID to match the arm
        public static final int kRightMotorId = 9;
        public static final int kExtensionMotorId = 11;
        public static final double armEncoderOffset = .225;
    }

    /* Winches should basically be activated when the driver presses one button and the robot automatically pulls itself up */
    public static final class Winches {
    
    //TODO: Update the motorID with the correct ones in the winches
    public static final int kRightWinchMotorID = 4;
    public static final int kLeftWinchMotorID = 5;
    
    //TODO: The inches is just a placeholder, need to measure how much it should realistically extend after robot is built
    public static final double kWinchLength = Units.inchesToMeters(30);

    //TODO: The inches the robot pulls itself up
    public static final double kWinchPull = Units.inchesToMeters(40);
    
    /* Just as a precaution we might need to make sure the robot isn't driving before the button is pressed*/
    }

    public static final class Swerve {
        // Driving Parameters - Note that these are not the maximum capable speeds of
        // the robot, rather the allowed maximum speeds
        // Measure from CANcoder to CANcoder, as they are the center of the wheels
        // Change these!

        // measured 3/21/23
        //TODO: Change the swerve dimentions
        public static final double kTrackWidth = Units.inchesToMeters(21.5);
        // Distance between centers of right and left wheels on robot
        // measured 3/21/23
        public static final double kWheelBase = Units.inchesToMeters(23.5);
        // Distance between front and back wheels on robot
       
        //Calculates to approximately 4.47596143982 meters/sec
        public static final double kMaxSpeedMetersPerSecond = (SwerveModule.kDrivingMotorFreeSpeedRps / 
            SwerveModule.kDrivingMotorReduction) * 
            SwerveModule.kWheelDiameterMeters * 
            Math.PI;
        
        //Calculates to Approximately 18.5429440372 rads/sec (Too much??)
        public static final double kMaxAngularSpeed = kMaxSpeedMetersPerSecond/Math.hypot(kTrackWidth/2.0, kWheelBase/2.0); // radians per second
        
        public static final double kMaxAccel = kMaxSpeedMetersPerSecond * 2;

        public static final double kMaxAngularAccel = kMaxAccel/Math.hypot(kTrackWidth/2.0, kWheelBase/2.0);
        // Chassis configuration
        

        ////Translation2d's are the x and y coordinates relative to your gyro. In this instance, 
        ////the four Translation2d objects are the coordinates of your four swerve modules
        //// x is front and back and y is left and right
        public static final SwerveDriveKinematics kDriveKinematics = new SwerveDriveKinematics(
            new Translation2d(kWheelBase / 2, kTrackWidth / 2),
            new Translation2d(kWheelBase / 2, -kTrackWidth / 2),
            new Translation2d(-kWheelBase / 2, kTrackWidth / 2),
            new Translation2d(-kWheelBase / 2, -kTrackWidth / 2));
        // Swerve Module IDs

        //TODO: Front Left Module
        public static final int kFrontLeftDrivingCanId = 1;
        public static final int kFrontLeftTurningCanId = 2;
        public static final int kFrontLeftCanCoderId = 61;
        public static final double kFrontLeftChassisAngularOffset = Math.toRadians(0);   //   epic
        
        //Front Right Module
        public static final int kFrontRightDrivingCanId = 3; 
        public static final int kFrontRightTurningCanId = 4;
        public static final int kFrontRightCanCoderId = 62;
        public static final double kFrontRightChassisAngularOffset = Math.toRadians(0);
        
         //Back Left Module
        public static final int kBackLeftDrivingCanId = 7;
        public static final int kBackLeftTurningCanId = 8;
        public static final int kBackLeftCanCoderId = 60;
        public static final double kBackLeftChassisAngularOffset = Math.toRadians(0); // manually zeroed
        
        //Back Right Module
        public static final int kBackRightDrivingCanId = 5;
        public static final int kBackRightTurningCanId = 6;
        public static final int kBackRightCanCoderId = 59;
        public static final double kBackRightChassisAngularOffset = Math.toRadians(0);

        public static final boolean kGyroReversed = false;
      }
    
      public static final class SwerveModule {
        // The MAXSwerve module can be configured with one of three pinion gears: 12T, 13T, or 14T.
        // This changes the drive speed of the module (a pinion gear with more teeth will result in a
        // robot that drives faster).
        public static final int kDrivingMotorPinionTeeth = 14;
    
        // Invert the turning encoder, since the output shaft rotates in the opposite direction of
        // the steering motor in the MAXSwerve Module.
        public static final boolean kTurningEncoderInverted = true;
    
        // Calculations required for driving motor conversion factors and feed forward
        public static final double kNeoMotorFreeSpeedRpm = 5676;

        public static final double kDrivingMotorFreeSpeedRps = kNeoMotorFreeSpeedRpm / 60.0;
        public static final double kWheelDiameterMeters = Units.inchesToMeters(4); // 3 : 0.0762; // 3.75 : 0.09525; //3.8 : 0.09652; // 4 : 0.1016; //Units.inchesToMeters(4.0);
        public static final double kWheelCircumferenceMeters = kWheelDiameterMeters * Math.PI;
        // 45 teeth on the wheel's bevel gear, 22 teeth on the first-stage spur gear, 15 teeth on the bevel pinion
        public static final double kDrivingMotorReduction = (50.0 * 17.0 * 45.0) / (kDrivingMotorPinionTeeth * 27.0 * 15.0);//
        public static final double kDriveWheelFreeSpeedRps = (kDrivingMotorFreeSpeedRps * kWheelCircumferenceMeters)
            /kDrivingMotorReduction;
    
        public static final double kSteeringMotorReduction = 12.8;
    
        public static final double kDrivingEncoderPositionFactor = (kWheelDiameterMeters * Math.PI)
            / kDrivingMotorReduction; // meters
        public static final double kDrivingEncoderVelocityFactor = ((kWheelDiameterMeters * Math.PI)
            / kDrivingMotorReduction) / 60.0; // meters per second
    
        public static final double kTurningEncoderPositionFactor = (2 * Math.PI) / kSteeringMotorReduction; // radians
        public static final double kTurningEncoderVelocityFactor = ((2 * Math.PI) / kSteeringMotorReduction) / 60.0; // radians per second
    
        public static final double kTurningEncoderPositionPIDMinInput = -Math.PI; // radians
        public static final double kTurningEncoderPositionPIDMaxInput = Math.PI; // radians
    
        public static final double kTurningAnalogPositionFactor = (Math.PI * 2 / 3.3);
        public static final double kTurningAnalogVelocityFactor = (kTurningAnalogPositionFactor/60);
        
        public static final double kDrivingP = 0.15;
        public static final double kDrivingI = 0;
        public static final double kDrivingD = 0.015;
        public static final double kDrivingFF = 1 / kDriveWheelFreeSpeedRps;
        public static final double kDrivingMinOutput = -1;
        public static final double kDrivingMaxOutput = 1;
    
        public static final double kTurningP = 2.295;
        public static final double kTurningI = 0;
        public static final double kTurningD = 0.2295;
        public static final double kTurningFF = 0;
        public static final double kTurningMinOutput = -1;
        public static final double kTurningMaxOutput = 1;
    
        public static final IdleMode kDrivingMotorIdleMode = IdleMode.kBrake;
        public static final IdleMode kTurningMotorIdleMode = IdleMode.kBrake;
    
        public static final int kDrivingMotorCurrentLimit = 40; // amps
        public static final int kTurningMotorCurrentLimit = 30; // amps
      }


      public static final class Intake {

        //TODO: Match the correct Motor ID and intakeSpeed
        public static final int kIntakeMotorId = 12;

        public static final double kIntakeSpeed = .6;
      }

      public static final class Shooter {

        //TODO: Change the motor IDs and the shooter speed
        public static final int kRightShooterMotorID = 4;
        public static final int kLeftShooterMotorID = 2;

        public static final int launchSpeedLimit = 60;
        public static final int feedSpeedLimit = 60;

        private Apriltags m_Apriltags = new Apriltags();
        private PneumaticsSubsystem m_PneumaticsSubsystem = new PneumaticsSubsystem();
        private PIDController m_XPidController = new PIDController(.6, 0, 0);
        private PIDController m_YPidController = new PIDController(.6, 0, 0);
        private PIDController m_AreaPidController = new PIDController(.6, 0, 0);
        private Swerve mSwerve;
        private double tolerance = 0.1;
        private boolean onTarget = false;
        public boolean shoot = false;
        private double areaTolerance = .01;
        private double targetArea = 0.5;//calibrate this



      }


    //TODO: Finish the vision class for apriltags
      public static final class Vision {
       /* public static AprilTagFieldLayout kAprilTagFieldLayout = null;
        static {
            try {
                //TODO: Change the AprilTagFields to updated version bc the k2023ChargedUp does not work with 2023.1.1 for gradle
                kAprilTagFieldLayout = AprilTagFieldLayout.loadFromResource(AprilTagFields.k2023ChargedUp.m_resourceFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //TODO: Change the Camera Name
        public static final String kCameraName = "gloworm";
        public static final Transform3d kRobotToCamera =
            new Transform3d(
                new Translation3d(0.5, 0.0, 0.5),
                new Rotation3d(0, 0,0));  */

        // public static final String kRightCameraName = "RIGHT";
        // public static final Transform3d kRightRobotToCamera =
        //     new Transform3d(
        //         new Translation3d(0.5, 0.0, 0.5),
        //         new Rotation3d(0, 0,0)); 

      }
         // public static final class Suction {
    //     /**
    //      * The CAN ID of the first suction motor.
    //      */
    //     public static final int kMotor1Id = 40;

    //     /**
    //      * The CAN ID of the second suction motor.
    //      */
    //     public static final int kMotor2Id = 41; //TODO check the actual ID number

    //     /**
    //      * The pneumatic hub channel ID of the first suction solenoid.
    //      */
    //     public static final int kSolenoid1Id = 0;

    //     /**
    //      * The pneumatic hub channel ID of the second suction solenoid.
    //      */
    //     public static final int kSolenoid2Id = 1;

    //     /**
    //      * The maximum current to send to the suction motor.
    //      */
    //     public static final int kCurrentLimit = 30;

    //     /**
    //      * The maximum speed that the suction motor runs.
    //      */
    //     public static final double kMaxSpeed = 0.33;

    //     /**
    //      * The pneumatic hub channel ID of the first pressure sensor.
    //      */
    //     public static final int kPressureSensor1Id = 0;

    //     /**
    //      * The pneumatic hub channel ID of the first pressure sensor.
    //      */
    //     public static final int kPressureSensor2Id = 1;

    //     /*
    //      * The target pressure for vacuum state
    //      */
    //     public static final double kTargetPressure = 17;

    //     /*
    //      * The minimum pressure for vacuum state
    //      */
    //     public static final double kMinimumPressure = 20;

    //   }

}
