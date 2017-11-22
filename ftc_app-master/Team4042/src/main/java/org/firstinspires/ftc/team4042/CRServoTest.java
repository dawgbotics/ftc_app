package org.firstinspires.ftc.team4042;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.CRServoImpl;

/**
 * Created by Hazel on 10/30/2017.
 */
@TeleOp(name="CR Servo Test", group="Iterative Opmode")
public class CRServoTest extends OpMode {

    private CRServo crServo;
    private double power = 0;
    private boolean y = false;
    private boolean a = false;

    private static final double STEP = .1;

    @Override
    public void init() {
        crServo = hardwareMap.crservo.get("horizontal");
        crServo.setPower(power);
    }

    @Override
    public void loop() {
        if (gamepad1.y && !y && power < 1) {
            power += STEP;
            crServo.setPower(power);
        }
        y = gamepad1.y;

        if (gamepad1.a && !a && power > -1) {
            power -= STEP;
            crServo.setPower(power);
        }
        a = gamepad1.a;

        telemetry.addData("power", power);
        telemetry.update();
    }
}
