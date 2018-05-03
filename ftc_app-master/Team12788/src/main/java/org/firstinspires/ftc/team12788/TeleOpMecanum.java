package org.firstinspires.ftc.team12788;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.opencv.core.Range;


@TeleOp(name = "Mecanum", group = "Iterative Opmode")
public class TeleOpMecanum extends OpMode {

    //Declare OpMode members.
    private MecanumDrive drive = new MecanumDrive();

    //private UltrasonicI2cRangeSensor sensor;

    private double adjustedSpeed;

    //private DcMotor lift;
    //private DcMotor intakeRight;
    //private DcMotor intakeLeft;
    private DcMotor arm;

    private boolean aToggle;

    private int liftPos;

    //public Servo grabLeft;
    //public Servo grabRight;
    public Servo pinch;
    public Servo jewel;

    private boolean invert;
    private boolean pinchBool;
    private boolean a;
    private boolean x;

    private double jewelPos;

    private double num;

    @Override
    public void init() {
        drive.initialize(telemetry, hardwareMap);
        telemetry.update();

        adjustedSpeed = MecanumDrive.FULL_SPEED;

        num = 0;
        jewelPos = .1;

        invert = false;
        aToggle = false;

        pinchBool = false;
        a = false;
        x = false;

        //lift = hardwareMap.dcMotor.get("lift");
        arm = hardwareMap.dcMotor.get("relic");
        //intakeLeft = hardwareMap.dcMotor.get("intakeLeft");
        //intakeRight = hardwareMap.dcMotor.get("intakeRight");
        //grabLeft = hardwareMap.servo.get("grabLeft");
        //grabRight = hardwareMap.servo.get("grabRight");
        pinch = hardwareMap.servo.get("pinch");
        jewel = hardwareMap.servo.get("jewel");
        //lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        //lift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        //intakeLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        //intakeRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }

    @Override
    public void start() {
        telemetry.log().add("started tele");
    }

    @Override
    public void loop() {
        if (gamepad1.dpad_up){
            jewelPos -= .005;
        }
        if (gamepad1.dpad_down){
            jewelPos += .005;
        }
        jewelPos = com.qualcomm.robotcore.util.Range.clip(jewelPos, 0, 1);
        jewel.setPosition(jewelPos);
        /*if (gamepad2.dpad_up) {
            liftPos = 12000;
        } else if (gamepad2.dpad_down) {
            liftPos = 0;
        } else if (gamepad2.dpad_left) {
            liftPos = 6000;
        } else if(gamepad2.dpad_right) {
            liftPos = 10600;
        }
        if (gamepad2.back){
            x = true;
        }
        if(x && gamepad2.dpad_up){
            lift.setPower(1);
        }
        else if(x && gamepad2.dpad_down){
            lift.setPower(-.5);
        }
        else if (x){
            lift.setPower(0);
        }*/


        /*if (0 < Math.abs(lift.getCurrentPosition() - liftPos) - 100) {
            if (liftPos < lift.getCurrentPosition()) {
                lift.setPower(-.5);
            } else if (liftPos > lift.getCurrentPosition()) {
                lift.setPower(1);
            }
        } else {
            lift.setPower(0);
        }*/

        if (gamepad1.left_bumper) {
            arm.setPower(-1);
        }
        else if (gamepad1.right_bumper) {
            arm.setPower(1);
        }
        else {
            arm.setPower(0);
        }
        if (gamepad1.a && !a) {
            a = true;
            pinchBool = !pinchBool;
        } else if (!gamepad1.a & a){
            a = false;
        }
        if (pinchBool){
            pinch.setPosition(1);
        }
        else {
            pinch.setPosition(.4);
        }
        /*if (drive.deadZone(gamepad2.right_trigger) > 0) {
            intakeLeft.setPower(1);
            intakeRight.setPower(-1);
        } else if (drive.deadZone(gamepad2.left_trigger) > 0) {
            intakeLeft.setPower(-1);
            intakeRight.setPower(1);
        } else {
            intakeLeft.setPower(0);
            intakeRight.setPower(0);
        }
        if (gamepad2.b) {
            grabRight.setPosition(.9);
            grabLeft.setPosition(0);
        }
        if (gamepad2.x) {
            grabRight.setPosition(.1);
            grabLeft.setPosition(.6);
        }
        if (gamepad2.y) {
            grabRight.setPosition(.4);
            grabLeft.setPosition(.4);
        }*/
        if (gamepad1.y) {
            adjustedSpeed = .5;
        }
        if (gamepad1.b) {
            adjustedSpeed = 1;
        }
        if (gamepad1.x) {
            adjustedSpeed = .25;
        }
            /*num ++;
            if(num % 2 == 1) {
                relic.setPosition(1);
            }
            else{
                relic.setPosition(-1);
            }

        if(aToggle){
            relic.setPosition(1);
        } else {
            relic.setPosition(-1);
        }

        if (gamepad2.a || overide){
            overide = true;
            if (!drive.driveWithEncoders(Direction.Forward, .5, .2*Autonomous.tile)) {

            } else {
                grabLeft.setPosition(-1);
                grabRight.setPosition(1);
                overide = false;
            }
        } else {
            drive.drive(false, gamepad1,adjustedSpeed * MecanumDrive.FULL_SPEED, invert);
        }*/
        drive.drive(false, gamepad1, adjustedSpeed, false);
    }

    private void telemetryUpdate() {
        telemetry.addData("Speed mode", adjustedSpeed);
        telemetry.update();
    }
}