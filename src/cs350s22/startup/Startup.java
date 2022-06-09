package cs350s22.startup;

import cs350s22.component.actuator.A_Actuator;
import cs350s22.component.actuator.ActuatorLinear;
import cs350s22.component.actuator.ActuatorRotary;
import cs350s22.component.sensor.A_Sensor;
import cs350s22.component.ui.parser.A_ParserHelper;
import cs350s22.component.ui.parser.Parser;
import cs350s22.component.ui.parser.ParserHelper;
import cs350s22.component.ui.parser.SymbolTable;
import cs350s22.support.Identifier;
import cs350s22.test.ActuatorPrototype;
import cs350s22.test.MyActuator;

import java.util.ArrayList;
import java.util.List;

//=================================================================================================================================================================================
public class Startup
{
    private final A_ParserHelper _parserHelper = new ParserHelper();

    // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    public Startup()
    {
        System.out.println("Welcome to your Startup class");
    }

    // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    public static void main(final String[] arguments) throws Exception
    {
        Startup startup = new Startup();
        // this command must come first. The filenames do not matter here
        startup.parse("@CONFIGURE LOG \"a.txt\" DOT SEQUENCE \"b.txt\" NETWORK \"c.txt\" XML \"d.txt\"");

        //startup.parse("@CREATE MAPPER myMapper EQUATION PASSTHROUGH");
        //startup.parse("@CREATE MAPPER myMapper EQUATION SCALE 10");
        //startup.parse("@CREATE MAPPER myMapper EQUATION NORMALIZE 10 20");
        //startup.parse("CREATE MAPPER myMapper INTERPOLATION SPLINE DEFINITION \"C:/temp/definition.map\"");



     //   startup.parse("@CREATE ACTUATOR LINEAR myActuator0 ACCELERATION LEADIN 0.1 LEADOUT -0.2 RELAX 0.3 " +
     //           "VELOCITY LIMIT 5 VALUE MIN 1 MAX 10 INITIAL 2 JERK LIMIT 3");


    //    startup.parse("@CREATE ACTUATOR ROTARY myActuator8 SENSORS mySensor3 ACCELERATION LEADIN 0.1 LEADOUT -0.2 RELAX 0.3\n" +
    //            " VELOCITY LIMIT 5 VALUE MIN 1 MAX 10 INITIAL 2 JERK LIMIT 3");

        startup.parse("@exit");
    }

    // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    private void parse(final String parse) throws Exception
    {
        System.out.println("PARSE> "+ parse);

        Parser parser = new Parser(_parserHelper, parse);

        parser.parse();
    }
}
