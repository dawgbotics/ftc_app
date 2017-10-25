package org.firstinspires.ftc.team4042;

import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DigitalChannelController;
import com.qualcomm.robotcore.hardware.DigitalChannelImpl;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

/**
 * Created by Ryan Whiting on 10/17/2017.
 */

public class GlyphPlacementSystem
{
    private int targetX;
    private int targetY;
    private int currentX;
    private int currentY;
    private int currentPositon;
    private int targetPosition;
    private String baseOutput;
    private DigitalChannel homeLimit;
    private DcMotor verticalDrive;

    public GlyphPlacementSystem(HardwareMap map)
    {
        this(0, 0, map);
    }

    public GlyphPlacementSystem(int currentX, int currentY, HardwareMap map)
    {
        this.homeLimit = map.digitalChannel.get("Limit");
        this.verticalDrive = map.dcMotor.get("Vertical Drive");
        this.baseOutput = "[_______]\n[_______]\n[_______]\n[_______]";
        this.targetX = currentX;
        this.targetY = currentY;
        this.currentX = currentX;
        this.currentY = currentY;

        verticalDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    //returns the current position of the glyph placement system
    public String getPositionAsString()
    {
        char[] output = baseOutput.toCharArray();

        int position = targetX + 3 * targetY;
        switch(position)
        {
            case(0): output[2] = 'X'; break;

            case(1): output[4] = 'X'; break;

            case(2): output[6] = 'X'; break;

            case(3): output[13] = 'X'; break;

            case(4): output[15] = 'X'; break;

            case(5): output[17] = 'X'; break;

            case(6): output[24] = 'X'; break;

            case(7): output[26] = 'X'; break;

            case(8): output[28] = 'X'; break;

            case(9): output[35] = 'X'; break;

            case(10): output[37] = 'X'; break;

            case(11): output[39] = 'X'; break;
        }

        return new String(output);
    }

    public int getPosition() {
        return targetX + 3 * targetY;
    }

    public void up() {
        if (targetY != 0) {
            targetY -= 1;
        }
    }

    public void down() {
        if (targetY != 3) {
            targetY += 1;
        }
    }

    public void left() {
        if (targetX != 0) {
            targetX -= 1;
        }
    }

    public void right() {
        if (targetX != 2) {
            targetX += 1;
        }
    }

    public void place() {
        //TODO: motor code here
        /*
        Assuming motor forward power moves it right and up
        Move sideways motor (block distance * (targetX - currentX))
        Move vertical motor (block distance * (targetY - currentY))
         */
    }

    public void runToPosition()
    {

    }
}