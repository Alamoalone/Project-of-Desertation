package frc.robot.commands.auto;

import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.commands.ShootMech;
import frc.robot.subsystems.Shooter;
import frc.robot.subsystems.Swerve;

public class ShootAndMove extends SequentialCommandGroup {

    private Shooter m_shooter;
    private Swerve m_Swerve;

    public ShootAndMove()
    {
        addCommands(new ShootMech(m_shooter), new DriveToDistance(-3, m_Swerve));
    }
    
}
