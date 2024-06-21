package frc.robot.commands.auto;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.Constants;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.Swerve;

public class MoveandAmp extends SequentialCommandGroup {
    
    private Intake m_intake;
    private Swerve m_Swerve;

    public MoveandAmp(){
        //TODO: Test TurnToAngle
        addCommands(new DriveToDistance(3, m_Swerve), new TurnToAngle(90, m_Swerve), new InstantCommand(() -> m_intake.on(Constants.Intake.kIntakeSpeed)));
    }
}
