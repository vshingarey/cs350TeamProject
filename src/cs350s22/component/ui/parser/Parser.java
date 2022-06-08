package cs350s22.component.ui.parser;

//Identifier is the class we use for defining the ids of each component
import cs350s22.support.Identifier;
import cs350s22.test.ActuatorPrototype;
import cs350s22.component.actuator.*;
import cs350s22.component.sensor.*;
import cs350s22.component.sensor.mapper.*;
import cs350s22.component.sensor.reporter.*;
import cs350s22.component.sensor.watchdog.*;
import cs350s22.component.controller.*;
import cs350s22.test.MySensor;


import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class Parser
{
    A_ParserHelper parserHelper;
    String parse;
    SymbolTable<A_Sensor> symbolTableSensor;
    SymbolTable<A_Actuator> symbolTableActuator;
    SymbolTable<A_Mapper> symbolTableMapper;
    SymbolTable<A_Reporter> symbolTableMessage;
    SymbolTable<A_Watchdog> symbolTableWatchdog;
    SymbolTable<A_Controller> symbolTableController;

    public Parser (A_ParserHelper parserHelper, String parse)
    {
        this.parserHelper = parserHelper;
        this.parse = parse;

    }

    public void parse () throws IOException {

        String[] values = parse.split(" "); //splits the string into single strings and stores in values

        if(values.length > 1) {     //deals with startup.parse("@exit") in main
            //values[0] == @CONFIGURE
            values[1] = values[1].toUpperCase();    //converts values[1] string to uppercase

            if (values[1].equals("CREATE")) { //If second string is CREATE

                values[2] = values[2].toUpperCase(); // Change values[1] to upper case

                if (values[2].equals("ACTUATOR")) {
                    actuatorBuilder(values);
                } else if (values[2].equals("SENSOR")) {
                    sensorBuilder(values);
                } else if (values[2].equals("MAPPER")) {
                    mapperBuilder(values);
                } else if (values[2].equals("NETWORK")) {
                    networkBuilder(values);
                } else if (values[2].equals("REPORTER")) {
                    reporterBuilder(values);
                } else if (values[2].equals("WATCHDOG")) {
                    watchdogBuilder(values);
                } else {
                    throw new IOException("Error: command not found: " + values[2]);
                }
            }
        } // if statement returns nothing if values.length == 1 or less

    } // ends parse method

    private void actuatorBuilder(String[] values) throws IOException{
        System.out.println("actuator");
/*
        System.out.println(values[0]); //@CONFIGURE
        System.out.println(values[1]); //CREATE
        System.out.println(values[2]); //ACTUATOR
        System.out.println(values[3]); //LINEAR OR ROTARY
        System.out.println(values[4]); //myActuator0
        System.out.println(values[5]); // ACCELERATION
        System.out.println(values[6]); //LEADIN
        System.out.println(values[7]); // 0.1
        System.out.println(values[8]); //LEADOUT
        System.out.println(values[9]); //-0.2
        System.out.println(values[10]); //RELAX
        System.out.println(values[11]); //0.3
        System.out.println(values[12]); // VELOCITY
        System.out.println(values[13]); // LIMIT
        System.out.println(values[14]); //5
        System.out.println(values[15]); //VALUE
        System.out.println(values[16]); // MIN
        System.out.println(values[17]); //1
        System.out.println(values[18]); // MAX
        System.out.println(values[19]); // 10
        System.out.println(values[20]); // INITIAL
        System.out.println(values[21]); // 2
        System.out.println(values[22]); // JERK
        System.out.println(values[23]); // LIMIT
        System.out.println(values[24]); // 3
*/
        values[3] = values[3].toUpperCase(); // changes type of actuator to uppercase

        Identifier actuatorId = Identifier.make(values[4]); //identifier for the actuator
        A_Actuator typeOfActuator; //Actuator type
        ActuatorPrototype ap;   //prototype actuator

        if(values[3].equals("LINEAR")){
            typeOfActuator = new ActuatorLinear(actuatorId);
        }else if(values[3].equals("ROTARY")){
            typeOfActuator = new ActuatorRotary(actuatorId);
        }else{
            throw new IOException("Unspecified actuator type");
        }   //if loop to set what type of actuator, linear or rotary

        if(values[5].equals("SENSORS")){     // If sensors are included in the command
            //System.out.println("We are in sensors if statement");
            ap = new ActuatorPrototype(actuatorId, typeOfActuator.getGroups(), Double.parseDouble(values[9]),
                    Double.parseDouble(values[11]), Double.parseDouble(values[13]), Double.parseDouble(values[16]),
                    Double.parseDouble(values[23]), Double.parseDouble(values[19]), Double.parseDouble(values[21]),
                    Double.parseDouble(values[26]),typeOfActuator.getSensors()); //creates the prototype

        }else if(values[5].equals("ACCELERATION")){  // if sensors are NOT included in the command
            //System.out.println("We are in acceleration if statement");
            ap = new ActuatorPrototype(actuatorId, typeOfActuator.getGroups(), Double.parseDouble(values[7]),
                    Double.parseDouble(values[9]), Double.parseDouble(values[11]),Double.parseDouble(values[14]),
                    Double.parseDouble(values[21]),Double.parseDouble(values[17]), Double.parseDouble(values[19]),
                    Double.parseDouble(values[24]), typeOfActuator.getSensors());

        }else{
            throw new IOException("Something went wrong");
        }

        symbolTableActuator = parserHelper.getSymbolTableActuator();
        symbolTableActuator.add(actuatorId,ap);

        System.out.println(symbolTableActuator.get(actuatorId));
    }
    private void sensorBuilder(String[] values)
    {
        System.out.println("sensor");
    }
    private void mapperBuilder(String[] values)
    {
        System.out.println("mapper");
    }
    private void networkBuilder(String[] values)
    {
        System.out.println("network");
    }
    private void reporterBuilder(String[] values)
    {
        System.out.println("reporter");
    }
    private void watchdogBuilder(String[] values)
    {
        System.out.println("watchdog");
    }



}
