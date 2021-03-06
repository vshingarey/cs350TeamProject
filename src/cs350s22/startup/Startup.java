package cs350s22.startup;

import cs350s22.component.controller.A_Controller;
import cs350s22.component.logger.LoggerActuator;
import cs350s22.component.sensor.A_Sensor;
import cs350s22.component.ui.parser.A_ParserHelper;
import cs350s22.component.ui.parser.Parser;
import cs350s22.component.ui.parser.ParserHelper;
import cs350s22.component.ui.parser.SymbolTable;
import cs350s22.network.Network;
import cs350s22.support.Filespec;
import cs350s22.support.Identifier;

import java.util.Scanner;

//=================================================================================================================================================================================
public class Startup
{
    private static final A_ParserHelper _parserHelper = new ParserHelper();

    // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    public Startup()
    {
        LoggerActuator.initialize(Filespec.make("blah"));
        System.out.println("Welcome to your Startup class");
    }

    // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    public static void main(final String[] arguments) throws Exception
    {
        Startup startup = new Startup();
        // this command must come first. The filenames do not matter here
        Scanner scanner = new Scanner(System.in);
        Network ourNetwork = _parserHelper.getNetwork();
        SymbolTable<A_Sensor> myTestSensor = _parserHelper.getSymbolTableSensor();

        startup.parse("@CONFIGURE LOG \"a.txt\" DOT SEQUENCE \"b.txt\" NETWORK \"c.txt\" XML \"d.txt\"");
        //startup.parse("@CLOCK PAUSE");
        startup.parse("@CREATE ACTUATOR LINEAR myActuator1 SENSOR mySensor1 ACCELERATION LEADIN 0.1 LEADOUT -0.2 RELAX 0.3 VELOCITY LIMIT 5 VALUE MIN 1 MAX 20 INITIAL 2 JERK LIMIT 3");
        startup.parse("@CREATE ACTUATOR ROTARY myActuator2 SENSOR mySensor1 ACCELERATION LEADIN 0.1 LEADOUT -0.2 RELAX 0.3 VELOCITY LIMIT 5 VALUE MIN 1 MAX 20 INITIAL 2 JERK LIMIT 3");
        startup.parse("@CREATE ACTUATOR LINEAR myActuator3 GROUPS myGroup3 SENSOR mySensor1 ACCELERATION LEADIN 0.1 LEADOUT -0.2 RELAX 0.3 VELOCITY LIMIT 5 VALUE MIN 1 MAX 20 INITIAL 2 JERK LIMIT 3");

        startup.parse("@CREATE SENSOR POSITION mySensor1 GROUPS myGroup1 REPORTERS myReporter1 WATCHDOGS myWatchdog1 MAPPER myMapper1");
        //startup.parse("@CREATE MAPPER myMapper1 EQUATION PASSTHROUGH");
        //startup.parse("@CREATE WATCHDOG ACCELERATION myWatchdog1 MODE INSTANTANEOUS THRESHOLD LOW 1 HIGH 3 GRACE 4");
        startup.parse("@CREATE REPORTER CHANGE myReporter1 NOTIFY IDS myActuator1 GROUPS myGroup1 FREQUENCY 1");
        //startup.parse("@CREATE SENSOR POSITION mySensor1 GROUPS myGroup1 REPORTERS myReporter1 WATCHDOGS myWatchdog1 MAPPER myMapper1");

        //startup.parse("@CREATE SENSOR POSITION mySensor8 REPORTER myReporter1");
        startup.parse("@BUILD NETWORK WITH COMPONENT myControllerMaster myActuator1 myActuator2 myGroup3 mySensor1");
        startup.parse("@CLOCK ");


        //startup.parse("@SET SENSOR mySensor1 VALUE 35");
        //startup.parse("@SET SENSOR mySensor1 VALUE 45");



        startup.parse("@EXIT");



//Network Tests
        //startup.parse("@CREATE SENSOR POSITION mySensor1 GROUPS myGroup1 REPORTERS myReporter1 WATCHDOGS myWatchdog1 MAPPER myMapper1");
        //startup.parse("@CREATE ACTUATOR LINEAR myActuator1 GROUPS myGroup1 myGroup2 SENSOR mySensor1 ACCELERATION LEADIN 0.1 LEADOUT -0.2 RELAX 0.3 VELOCITY LIMIT 5 VALUE MIN 1 MAX 10 INITIAL 2 JERK LIMIT 3");
        //startup.parse("@BUILD NETWORK WITH COMPONENT myActuator1 mySensor1");
        //startup.parse("@BUILD NETWORK WITH COMPONENTS myController myActuator");




//Mapper Tests
        //startup.parse("@CREATE MAPPER myMapper1 EQUATION PASSTHROUGH"); //PASS
        //startup.parse("@CREATE MAPPER myMapper1 EQUATION SCALE 10");    //PASS
        //startup.parse("@CREATE MAPPER myMapper1 EQUATION NORMALIZE 10 20"); //PASS
        //startup.parse("CREATE MAPPER myMapper1 INTERPOLATION SPLINE DEFINITION \"C:/temp/definition.map\"");

//Reporter Tests

        //startup.parse("@CREATE REPORTER CHANGE myReporter1 NOTIFY IDS myActuator1 GROUPS myGroup1 FREQUENCY 3");
        //startup.parse("@CREATE REPORTER FREQUENCY myReporter1 NOTIFY IDS myActuator1 myActuator2 GROUPS myGroup3 FREQUENCY 3");

//Sensor Tests
        //startup.parse("@CREATE MAPPER myMapper1 EQUATION PASSTHROUGH");
        //startup.parse("@CREATE WATCHDOG ACCELERATION myWatchdog1 MODE INSTANTANEOUS THRESHOLD LOW 1 HIGH 3 GRACE 4");
        //startup.parse("@CREATE REPORTER CHANGE myReporter1 NOTIFY IDS myActuator1 GROUPS myGroup1 FREQUENCY 3");
        //startup.parse("@CREATE SENSOR POSITION mySensor1 GROUPS myGroup1 REPORTERS myReporter1 WATCHDOGS myWatchdog1 MAPPER myMapper1");

        //startup.parse("@CREATE MAPPER myMapper1 EQUATION PASSTHROUGH");
        //startup.parse("@CREATE SENSOR POSITION mySensor8 GROUPS myGroup1 REPORTERS myReporter1");
        //startup.parse("@CREATE SENSOR POSITION mySensor1 GROUPS myGroup1 MAPPER myMapper1");
        //startup.parse("@CREATE SENSOR POSITION mySensor1 GROUPS myGroup1 REPORTERS myReporter1 WATCHDOGS myWatchdog1 MAPPER myMapper1");

//WatchDog Tests

        //startup.parse("@CREATE WATCHDOG ACCELERATION myWatchdog1 MODE INSTANTANEOUS THRESHOLD LOW 1 HIGH 3 GRACE 4"); //PASS
        //startup.parse("@CREATE WATCHDOG ACCELERATION myWatchdog2 MODE AVERAGE 6 THRESHOLD LOW 1 HIGH 3 GRACE 4"); //PASS
        //startup.parse("@CREATE WATCHDOG ACCELERATION myWatchdog2 MODE STANDARD DEVIATION 10 THRESHOLD LOW 1 HIGH 3 GRACE 4");

        //startup.parse("@CREATE WATCHDOG BAND myWatchdog1 MODE INSTANTANEOUS THRESHOLD LOW 1 HIGH 3 GRACE 4"); //PASS
        //startup.parse("@CREATE WATCHDOG BAND myWatchdog1 MODE AVERAGE 6 THRESHOLD LOW 1 HIGH 3 GRACE 4");     //PASS
        //startup.parse("@CREATE WATCHDOG BAND myWatchdog1 MODE STANDARD DEVIATION 10 THRESHOLD LOW 1 HIGH 3 GRACE 4");

        //startup.parse("@CREATE WATCHDOG NOTCH myWatchdog2 MODE INSTANTANEOUS THRESHOLD LOW 1 HIGH 3 GRACE 4");
        //startup.parse("@CREATE WATCHDOG NOTCH myWatchdog2 MODE AVERAGE 6 THRESHOLD LOW 1 HIGH 3 GRACE 4");
        //startup.parse("@CREATE WATCHDOG NOTCH myWatchdog2 MODE STANDARD DEVIATION 10 THRESHOLD LOW 1 HIGH 3 GRACE 4");

        //startup.parse("@CREATE WATCHDOG LOW myWatchdog1 MODE INSTANTANEOUS THRESHOLD 3 GRACE 4");
        //startup.parse("@CREATE WATCHDOG LOW myWatchdog1 MODE AVERAGE 6 THRESHOLD 3 GRACE 4");
        //startup.parse("@CREATE WATCHDOG LOW myWatchdog1 MODE STANDARD DEVIATION 10 THRESHOLD 3 GRACE 4");

        //startup.parse("@CREATE WATCHDOG HIGH myWatchdog2 MODE INSTANTANEOUS THRESHOLD 3 GRACE 4");
        //startup.parse("@CREATE WATCHDOG HIGH myWatchdog2 MODE AVERAGE 6 THRESHOLD 3 GRACE 4");
        //startup.parse("@CREATE WATCHDOG HIGH myWatchdog2 MODE STANDARD DEVIATION 10 THRESHOLD 3 GRACE 4");


//Actuator Tests

        //startup.parse("@CREATE ACTUATOR LINEAR myActuator0 GROUPS myGroup1 myGroup2 SENSOR mySensor1 ACCELERATION LEADIN 0.1 LEADOUT -0.2 RELAX 0.3 VELOCITY LIMIT 5 VALUE MIN 1 MAX 10 INITIAL 2 JERK LIMIT 3");



        //startup.parse("@EXIT");
    }

    // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    private void parse(final String parse) throws Exception
    {
        System.out.println("PARSE> "+ parse);

        Parser parser = new Parser(_parserHelper, parse);

        parser.parse();
    }
}
