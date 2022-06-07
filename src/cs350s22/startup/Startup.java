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
        //startup.parse("@CONFIGURE LOG \"a.txt\" DOT SEQUENCE \"b.txt\" NETWORK \"c.txt\" XML \"d.txt\"");

        // run your tests like this
        //startup.parse("@exit");


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
