package org.firstinspires.ftc.team4042.drive;

import android.app.Activity;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.team4042.autos.C;

import java.io.PrintWriter;
import java.io.StringWriter;

public class  MecanumDrive extends Drive {

    /**
     * Constructor for Drive, it creates the motors and the gyro objects
     */
    public MecanumDrive() {
        //Initialize motors and gyro
        super();
    }



    @Override
    public void initialize(Telemetry telemetry, HardwareMap hardwareMap) {
        super.initialize(telemetry, hardwareMap);
    }

    public MecanumDrive(boolean verbose) {
        super(verbose);
    }

    public MecanumDrive(boolean verbose, boolean useSensors) {
        //Initialize motors and gyro
        super(verbose);
        super.useSensors = useSensors;
    }

    private void driveTank(double speedFactor, double l, double r) {
        double[] speedWheel = new double[4];

        //Deadzone for joysticks
        l = super.deadZone(l);
        r = super.deadZone(r);

        if (verbose) {
            telemetry.addData("tank left", l);
            telemetry.addData("tank right", r);
        }

        speedWheel[0] = l;
        speedWheel[1] = r;
        speedWheel[2] = r;
        speedWheel[3] = l;

        super.setMotorPower(speedWheel, speedFactor);
    }

    public void driveLRXY(double speedFactor, double x, double y) {
        double[] speedWheel = new double[4];

        y = super.deadZone(y);
        x = super.deadZone(x);

        //Don't move the back motors
        speedWheel[2] = 0;
        speedWheel[3] = 0;

        speedWheel[0] = y - x;
        speedWheel[1] = y + x;

        //0, 1  --> 1, 1
        //0, -1 --> -1, -1
        //1, 0  --> 1, -1
        //-1, 0 --> -1, 1
        //0, 0  --> 0, 0
        //1, .5  --> 1.5, -.5

        super.setMotorPower(speedWheel, speedFactor);
    }

    public void driveLR(double speedFactor, double l, double r) {

        double[] speedWheel = new double[4];

        //Deadzone for joysticks
        l = super.deadZone(l);
        r = super.deadZone(r);

        if (verbose) {
            telemetry.addData("left", l);
            telemetry.addData("right", r);
        }

        if (crawl) {
            //We don't move the front motors
            speedWheel[0] = 0;
            speedWheel[1] = 0;
            //We just want to slightly adjust the back wheels
            speedWheel[2] = l;
            speedWheel[3] = r;
            speedFactor = .75 * speedFactor;
        }
        else {
            speedWheel[0] = l;
            speedWheel[1] = r;
            //We don't move the back motors, for obvious reasons
            speedWheel[2] = 0;
            speedWheel[3] = 0;
        }

        super.setMotorPower(speedWheel, speedFactor);
    }

    /**
     * Uses joystick inputs to set motor speeds for mecanum drive. Speeds partially depend on the drive mode.
     * @param useEncoders determines whether or not the motors use encoders
     */
    public void drive(boolean useEncoders, MyGamepad gamepad1, MyGamepad gamepad2, double speedFactor) {
        super.setEncoders(useEncoders);

        if (crawl || (isExtendo && !ivan)) {
            double left = gamepad1.left_stick_y;
            double right = gamepad1.right_stick_y;

            driveLR(speedFactor, left, right);
        } else if (isExtendo && ivan && !crawl) {
            double x = gamepad1.left_stick_x;
            double y = gamepad1.left_stick_y;

            driveLRXY(speedFactor, x, y);
        } else if (tank) {
            double left = gamepad1.left_stick_y;
            double right = gamepad1.right_stick_y;

            driveTank(speedFactor, left, right);
        } else {
            double x = gamepad1.left_stick_x;
            double y = -gamepad1.left_stick_y; //Y is the opposite direction of what's intuitive: forward is -1, backwards is 1
            double r = gamepad1.right_stick_x;

            if (!gamepad1.dpad_left && gamepad1.right_stick_button) {
                r = useGyro(0);
            }

            driveXYR(speedFactor, x, y, r, useGyro);
        }
    }

    public void driveXYR(double speedFactor, double x, double y, double r, boolean useGyro, double pConstant) {
        double[] targSpeedWheel = new double[4];

        //Deadzone for joysticks
        x = super.deadZone(x);
        y = super.deadZone(y);
        r = super.deadZone(r);

        /*if (verbose) {
            telemetry.addData("x", x);
            telemetry.addData("y", y);
            telemetry.addData("r", r);
        }*/

        double velFeedForwardConstant = 0.01;

        double heading = OFFSET;
        if (useGyro) {
            heading = super.gyro.updateHeading();
            telemetry.addData("heading", heading);
        }

        /*
        Adjust x, y for gyro values
         */
        double gyroRadians = Math.toRadians(heading);
        double xPrime = x * Math.cos(gyroRadians) + y * Math.sin(gyroRadians);
        double yPrime = -x * Math.sin(gyroRadians) + y * Math.cos(gyroRadians);

        //Sets relative target wheel speeds for mecanum drive based on controller inputs
        targSpeedWheel[0] = 27*Range.clip((-xPrime - yPrime - r), -1, 1);
        targSpeedWheel[1] = 27*Range.clip((xPrime - yPrime + r), -1, 1);
        targSpeedWheel[2] = 43*Range.clip((-xPrime - yPrime + r), -1, 1);
        targSpeedWheel[3] = 43*Range.clip((xPrime - yPrime - r), -1, 1);

        //Sets control factors based on target speeds and actual speeds
        double[] speedWheel = new double[4];
        for(int i = 0; i < speedWheel.length; i++) {
            speedWheel[i] = (targSpeedWheel[i] - encoderRates[i]) * pConstant + targSpeedWheel[i] * velFeedForwardConstant;
            //telemetry.addData("" + i, targSpeedWheel[i] + " " + encoderRates[i]);
        }

        //sets the wheel powers to the appropriate ratios
        super.setMotorPower(speedWheel, speedFactor);
    }

    /**
     * Drives an inputted amount in regular drive mode
     * @param speedFactor the amount to scale the drive by
     * @param x x component
     * @param y y component
     * @param r rotate component
     */
    public void driveXYR(double speedFactor, double x, double y, double r, boolean useGyro) {

        //Deadzone for joysticks
        x = super.deadZone(x);
        y = super.deadZone(y);
        r = super.deadZone(r);

        //Use the gyro, or ignore it.
        double heading = OFFSET;
        //Note that OFFSET = 0, but we could make it 180 if we wanted to drive backwards, or 45 if we were using omni drive
        if (useGyro) {
            heading = super.gyro.updateHeading();
            telemetry.addData("heading", heading);
        }

        /*
        Adjust x, y for gyro values
         */
        double gyroRadians = Math.toRadians(heading);
        double xPrime = x * Math.cos(gyroRadians) + y * Math.sin(gyroRadians);
        double yPrime = -x * Math.sin(gyroRadians) + y * Math.cos(gyroRadians);

        setMotorNormal(xPrime, yPrime, r, speedFactor);
    }

    public void driveXYRWimpo(double speedFactor, double x, double y, double r, boolean useGyro) {
        //Deadzone for joysticks
        x = super.deadZone(x);
        y = super.deadZone(y);
        r = super.deadZone(r);

        //Use the gyro, or ignore it.
        double heading = OFFSET;
        //Note that OFFSET = 0, but we could make it 180 if we wanted to drive backwards, or 45 if we were using omni drive
        if (useGyro) {
            heading = super.gyro.updateHeading();
            telemetry.addData("heading", heading);
        }

        /*
        Adjust x, y for gyro values
         */
        double gyroRadians = Math.toRadians(heading);
        double xPrime = x * Math.cos(gyroRadians) + y * Math.sin(gyroRadians);
        double yPrime = -x * Math.sin(gyroRadians) + y * Math.cos(gyroRadians);

        setMotorWimpo(xPrime, yPrime, r, speedFactor);
    }

    private void setMotorNormal(double xPrime, double yPrime, double r, double speedFactor) {
        //Sets relative wheel speeds for mecanum drive based on controller inputs
        double[] speedWheel = new double[4];
        speedWheel[0] = (-xPrime - yPrime - r);
        speedWheel[1] = (xPrime - yPrime + r);
        speedWheel[2] = -xPrime - yPrime + r;
        speedWheel[3] = xPrime - yPrime - r;

        /*for (int i = 0; i < speedWheel.length; i++) {
            telemetry.addData("" + i, speedWheel[i]);
        }
        //telemetry.addData("xPrime", xPrime + " yPrime: " + yPrime + " r: " + r);
        //telemetry.addData("speedFactor", speedFactor);*/

        //sets the wheel powers to the appropriate ratios

        super.setMotorPower(speedWheel, speedFactor);

        /*motorLeftFront.setPower(deadZone(speedWheel[0]));
        telemetry.addData("left front", deadZone(speedWheel[0]));
        motorLeftFront.getPower();

        motorRightFront.setPower(deadZone(-speedWheel[1]));
        telemetry.addData("right front", deadZone(-speedWheel[1]));
        motorRightFront.getPower();

        motorRightBack.setPower(deadZone(-speedWheel[2]));

        motorLeftBack.setPower(deadZone(speedWheel[3]));*/


    }

    private void setMotorWimpo(double xPrime, double yPrime, double r, double speedFactor) {
        //Sets relative wheel speeds for mecanum drive based on controller inputs
        double[] speedWheel = new double[4];
        speedWheel[0] = MAGIC_NUMBER * (-xPrime - yPrime - r);
        speedWheel[1] = MAGIC_NUMBER * (xPrime - yPrime + r);
        speedWheel[2] = -xPrime - yPrime + r;
        speedWheel[3] = xPrime - yPrime - r;

        //sets the wheel powers to the appropriate ratios
        super.setMotorPower(speedWheel, speedFactor);
    }

    /**
     * Rotates the robot to the target location, returning true while it has not
     * reached the target then false once it has. Also speeds up and slows down
     *
     * @param targetTicks the tick count you want to reach with at least one of your motors
     * @param speed speed at which to travel
     * @param rotation which way to rotate
     * @return returns if it is completed (true if has reached target, false if it hasn't)
     */
    public boolean rotateWithEncoders(Direction.Rotation rotation, double speed, double targetTicks) throws IllegalArgumentException {
        //telemetry data
        /*telemetry.addData("Left Back", motorLeftBack.getCurrentPosition());
        telemetry.addData("Left Front", motorLeftFront.getCurrentPosition());
        telemetry.addData("Right Back", motorRightBack.getCurrentPosition());
        telemetry.addData("Right Front", motorRightFront.getCurrentPosition());*/

        double scaledSpeed = setUpSpeed(speed, targetTicks, false);
        if (scaledSpeed == Math.PI) { //The target's been reached
            return true;
        }
        //if it hasn't reached the target (it won't have returned yet),
        // drive at the given speed (possibly scaled b/c of first and last fourth), and return false
        scaledSpeed = Range.clip(scaledSpeed, 0, FULL_SPEED);

        if (rotation.equals(Direction.Rotation.Clockwise) || rotation.equals(Direction.Rotation.Counterclockwise)) { //Rotating
            //Don't use the gyro because the robot is MEANT to be turning
            driveXYR(FULL_SPEED, 0, 0, -scaledSpeed, false);
        }
        else { //Null or other problematic directions
            throw new IllegalArgumentException("Illegal direction inputted! Direction was: " + rotation);
        }
        return false;
    }

    /**
     * Runs the robot to the target location, returning true while it has not
     * reached the target then false once it has. Also speeds up and slows down
     *
     * @param targetTicks the tick count you want to reach with at least one of your motors
     * @param speed speed at which to travel
     * @param direction which direction to go
     * @return returns if it is completed (true if has reached target, false if it hasn't)
     */
    public boolean driveWithEncoders(Direction direction, double speed, double targetTicks, boolean useGyro,
                                     double targetGyro, boolean ignoreBack, double mulch, double rotConstant) throws IllegalArgumentException{
        //telemetry data
        /*if (verbose) {
            log.add("x " + direction.getX());
            log.add("y " + direction.getY());
        }*/

        double scaledSpeed = setUpSpeed(speed, targetTicks, ignoreBack);

        if (scaledSpeed == Math.PI) { //The target's been reached
            return true;
        }

        scaledSpeed *= mulch;

        //if it hasn't reached the target (it won't have returned yet),
        // drive at the given speed (possibly scaled b/c of first and last fourth), and return false
        scaledSpeed = Range.clip(scaledSpeed, 0, FULL_SPEED);

        double r = 0;
        if (useGyro) {
            r = useGyro(targetGyro);
        }
        /*if (verbose) {
            log.add("useGyro " + useGyro + " r " + r);
        }*/

        //Drives at x
        driveXYR(FULL_SPEED, direction.getX() * scaledSpeed, direction.getY() * scaledSpeed, r * rotConstant, false);
        return false;
    }

    public boolean driveWithEncoders(Direction direction, double speed, double targetTicks, boolean useGyro, double targetGyro, boolean ignoreBack, double mulch) throws IllegalArgumentException {
        return driveWithEncoders(direction, speed, targetTicks, useGyro, targetGyro, ignoreBack, mulch, 1);
    }

    public double getEncoderTravel() {
        return super.max(motorLeftBack.getCurrentPosition(),
                motorLeftFront.getCurrentPosition(),
                motorRightBack.getCurrentPosition(), motorRightFront.getCurrentPosition());
    }

    /**
     * A helper function that scales speed if you're in the first or last fourth of the target encoder values
     * @param speed The inputted speed
     * @param targetTicks The final ticks for the encoders
     * @return Returns the speed, scaled, or Math.PI if you've already reached the value
     */
    private double setUpSpeed(double speed, double targetTicks, boolean ignoreBack) {
        //finds the maximum of all encoder counts
        double currentTicks = super.max(motorLeftBack.getCurrentPosition(),
                motorLeftFront.getCurrentPosition(),
                motorRightBack.getCurrentPosition(), motorRightFront.getCurrentPosition());

        currentTicks = ignoreBack ? Math.max(Math.abs(motorLeftFront.getCurrentPosition()),
                Math.abs(motorRightFront.getCurrentPosition())) : currentTicks;
        //if it has not reached the target, it tests if it is in the
        // last or first fourth of the way there, and
        // scales the speed such that it speeds up and slows down
        // to BASE_SPEED as it reaches the target
        if (currentTicks > targetTicks) {
            //if it has reached target, stop moving, reset encoders, and return PI
            stopMotors(); //stops the motors
            this.resetEncoders();
            this.runWithEncoders();
            return Math.PI;
        }
        return speed;
    }

    /**
     * Stops all motors
     */
    public void stopMotors() {
        driveXYR(STOP_SPEED, 0, 0, 0, false);
    }

    /**
     * CODE FROM HERE DOWN IS AN ATTEMPT TO IMPLEMENT DYLAN'S DRIVE ALGORITHM
     */
    double lastX;
    double lastY;
    double lastR;
    double lastTime = System.currentTimeMillis();

    public void drive(boolean useEncoders, Gamepad gamepad1, double speedFactor) {
        super.setEncoders(useEncoders);

        double x = gamepad1.left_stick_x;
        double y = -gamepad1.left_stick_y;
        double r = gamepad1.right_stick_x;

        x = super.deadZone(x);
        y = super.deadZone(y);
        r = super.deadZone(r);

        double time = System.currentTimeMillis();
        double dX = (lastX - x) / (lastTime - time);
        double dY = (lastY - y) / (lastTime - time);
        double dR = (lastR - r) / (lastTime - time);

        double distanceFromCenter = 3;
        double rollerAngle = 0; //Math.PI / 4; //45 degrees, in radians

        double[] speedWheel = new double[4];

        try {
            for (int i = 0; i < speedWheel.length; i++) {
                double angleShaft = Math.PI / 4 + (Math.PI / 2) * i;

                double[][] xy = new double[1][2];
                xy[0][0] = dX;
                xy[0][1] = dY;

                double[][] sincos1 = new double[1][2];
                sincos1[0][0] = Math.sin(r + angleShaft + Math.PI / 2);
                sincos1[0][1] = Math.cos(r + angleShaft + Math.PI / 2);

                double[][] sincos2 = new double[2][1];
                sincos2[0][0] = Math.sin(r + angleShaft + rollerAngle);
                sincos2[1][0] = Math.cos(r + angleShaft + rollerAngle);

                speedWheel[i] =
                        multiplyMatrices(
                                addMatrices(
                                        xy,
                                        multiplyConstant(
                                                distanceFromCenter * dR,
                                                sincos1)
                                ),
                                sincos2
                        )[0][0] / Math.sin(rollerAngle);
            }

            super.setMotorPower(speedWheel, speedFactor);

            lastX = x;
            lastY = y;
            lastR = r;
            lastTime = time;
        } catch (ArrayIndexOutOfBoundsException ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            telemetry.addData("ex", exceptionAsString);
        }
    }

    /**
     * Adds arrays matrix1 and matrix2 by standard rules of matrix addition
     * @param matrix1 The first matrix
     * @param matrix2 The second matrix
     * @return The result matrix
     */
    public double[][] addMatrices(double[][] matrix1, double[][] matrix2) {
        if ((matrix1.length != matrix2.length) || (matrix1[0].length != matrix2[0].length)) {
            throw(new IllegalArgumentException("Illegal matrix dimensions for addition."));
        }
        else {
            telemetry.addData("matrix1", matrix1.length + "x" + matrix1[0].length);
            telemetry.addData("matrix2", matrix2.length + "x" + matrix2[0].length);

            double[][] result = new double[matrix1.length][matrix1[0].length];
            for (int i = 0; i < matrix1.length; i++) {
                for (int j = 0; j < matrix1[0].length; i++) {
                    telemetry.addData("i", i);
                    telemetry.addData("j", j);

                    double one = matrix1[i][j];
                    double two = matrix2[i][j];
                    result[i][j] = one + two;
                }
            }
            return result;
        }
    }

    /**
     * Multiplies arrays matrix1 and matrix2 by standard rules of matrix multiplication
     * @param matrix1 The first matrix
     * @param matrix2 The second matrix
     * @return The result matrix
     */
    public double[][] multiplyMatrices(double[][] matrix1, double[][] matrix2) {
        if (matrix1.length != matrix2[0].length) {
            throw(new IllegalArgumentException("Illegal matrix dimensions for multiplication."));
        }
        else {
            double[][] result = new double[matrix1[0].length][matrix2.length];
            for (int i = 0; i < matrix1.length; i++) {
                for (int j = 0; j < matrix2[0].length; j++) {
                    for (int k = 0; k < matrix1[0].length; k++) {
                        result[i][j] += matrix1[i][k] * matrix2[k][j];
                    }
                }
            }
            return result;
        }
    }

    /**
     * Multiplies a matrix by a constant
     * @param constant The constant to multiply by
     * @param matrix The matrix to multiply
     * @return The result matrix
     */
    public double[][] multiplyConstant(double constant, double[][] matrix) {
        double[][] result = new double[matrix.length][matrix[0].length];

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                result[i][j] = matrix[i][j] * constant;
            }
        }

        return result;
    }
}
