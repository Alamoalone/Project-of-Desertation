package frc.robot.subsystems;


import org.opencv.dnn.SegmentationModel;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkBase.IdleMode;
import frc.robot.Constants;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;


import frc.robot.subsystems.Apriltags;
import frc.robot.subsystems.PneumaticsSubsystem;
/* Github testing */
public class Shooter extends SubsystemBase{


   CANSparkMax shooterCANSparkMax;
   CANSparkMax shooterCANSparkMaxTwo;
   
   CANSparkMax shooterCANSparkMaxThree;
   CANSparkMax shooterCANSparkMaxFour;
   //TODO: update the launch speed

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
   


   public Shooter(Swerve mSwerve) {
  
   this.mSwerve = mSwerve;

   shooterCANSparkMax.setInverted(true);
   shooterCANSparkMax.setSmartCurrentLimit(40);
   shooterCANSparkMax.setIdleMode(com.revrobotics.CANSparkBase.IdleMode.kBrake);

   shooterCANSparkMaxTwo.setInverted(false);
   shooterCANSparkMaxTwo.setSmartCurrentLimit(40);
   shooterCANSparkMaxTwo.setIdleMode(com.revrobotics.CANSparkBase.IdleMode.kBrake);

   shooterCANSparkMaxThree.setInverted(true);
   shooterCANSparkMaxThree.setSmartCurrentLimit(40);
   shooterCANSparkMaxThree.setIdleMode(com.revrobotics.CANSparkBase.IdleMode.kBrake);

   shooterCANSparkMaxThree.setInverted(false);
   shooterCANSparkMaxThree.setSmartCurrentLimit(40);
   shooterCANSparkMaxThree.setIdleMode(com.revrobotics.CANSparkBase.IdleMode.kBrake);

    //following
   shooterCANSparkMaxTwo.follow(shooterCANSparkMax);
   shooterCANSparkMaxFour.follow(shooterCANSparkMaxThree);

   //tolerance
   m_XPidController.setTolerance(tolerance);
   m_YPidController.setTolerance(tolerance);
   m_AreaPidController.setTolerance(areaTolerance);
   }


   public void cherryBomb() {
     if(m_Apriltags.getV()!=0){
          //TODO: Add the launch speed
          boolean withinXTolerance = (m_Apriltags.getX()<tolerance&&m_Apriltags.getX()>-tolerance);
          boolean withinYTolerance = (m_Apriltags.getY()<tolerance&&m_Apriltags.getY()>-tolerance);
          boolean withinAreaTolerance = (m_Apriltags.getArea()-targetArea<areaTolerance&&m_Apriltags.getArea()-targetArea>-areaTolerance);
          shooterCANSparkMax.set(Constants.Shooter.launchSpeedLimit);
          if(shooterCANSparkMax.getEncoder().getVelocity()<0.6||!onTarget){
               onTarget = withinXTolerance||withinYTolerance||withinAreaTolerance;
          }
          else {
               m_PneumaticsSubsystem.toggle();
          }
          if(!withinXTolerance){
               mSwerve.drive(0, 0, m_XPidController.calculate(m_Apriltags.getX(), 0/*TODO:Might need to change */), false);
          }
          if(!withinYTolerance){
               shooterCANSparkMaxThree.set(m_YPidController.calculate(m_Apriltags.getY(), 0));
          }
          if(!withinAreaTolerance){
               mSwerve.drive(m_AreaPidController.calculate(m_Apriltags.getArea(), /* TODO: Change the setpoint */targetArea), 0, 0, false);
          }
     }
   }
   
  
}





