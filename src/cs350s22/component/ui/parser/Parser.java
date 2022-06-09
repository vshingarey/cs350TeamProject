package cs350s22.component.ui.parser;

//Identifier is the class we use for defining the ids of each component
import cs350s22.component.sensor.mapper.function.equation.EquationNormalized;
import cs350s22.component.sensor.mapper.function.equation.EquationPassthrough;
import cs350s22.component.sensor.mapper.function.equation.EquationScaled;
import cs350s22.component.sensor.mapper.function.interpolator.InterpolatorLinear;
import cs350s22.component.sensor.mapper.function.interpolator.InterpolatorSpline;
import cs350s22.component.sensor.mapper.function.interpolator.loader.MapLoader;
import cs350s22.support.Filespec;
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
import java.util.ArrayList;
import java.util.List;

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

            values[0] = values[0].toUpperCase();    //converts values[1] string to uppercase

            if (values[0].equals("@CREATE")) { //If second string is CREATE

                values[1] = values[1].toUpperCase(); // Change values[1] to upper case

                if (values[1].equals("ACTUATOR")) {
                    actuatorBuilder(values);
                } else if (values[1].equals("SENSOR")) {
                    sensorBuilder(values);
                } else if (values[1].equals("MAPPER")) {
                    mapperBuilder(values);
                } else if (values[1].equals("NETWORK")) {
                    networkBuilder(values);
                } else if (values[1].equals("REPORTER")) {
                    reporterBuilder(values);
                } else if (values[1].equals("WATCHDOG")) {
                    watchdogBuilder(values);
                } else {
                    throw new IOException("Error: command not found: " + values[1]);
                }
            }
        } // if statement returns nothing if values.length == 1 or less

    } // ends parse method

    private void actuatorBuilder(String[] values) throws IOException{
        System.out.println("actuator");
        values[2] = values[2].toUpperCase(); // changes type of actuator to uppercase

        Identifier actuatorId = Identifier.make(values[3]); //identifier for the actuator
        A_Actuator typeOfActuator; //Actuator type
        ActuatorPrototype ap;   //prototype actuator
        MySensor sensorObj;     //sensor Object
        List<A_Sensor> sensorList;  //sensor list

        if(values[2].equals("LINEAR")){
            typeOfActuator = new ActuatorLinear(actuatorId);
        }else if(values[2].equals("ROTARY")){
            typeOfActuator = new ActuatorRotary(actuatorId);
        }else{
            throw new IOException("Unspecified actuator type");
        }   //if loop to set what type of actuator, linear or rotary

        if(values[4].equals("SENSORS")){     // If sensors are included in the command

            Identifier sensorId = Identifier.make(values[5]);   // makes identifier for sensor
            sensorObj = new MySensor(sensorId);                 // creates a new sensor Object
            symbolTableSensor = parserHelper.getSymbolTableSensor();
            symbolTableSensor.add(actuatorId, sensorObj); //adds sensor to the table
            sensorList = new ArrayList<>();
            sensorList.add(sensorObj);

            ap = new ActuatorPrototype(actuatorId, typeOfActuator.getGroups(), Double.parseDouble(values[8]),
                    Double.parseDouble(values[10]), Double.parseDouble(values[12]), Double.parseDouble(values[15]),
                    Double.parseDouble(values[22]), Double.parseDouble(values[18]), Double.parseDouble(values[20]),
                    Double.parseDouble(values[25]),sensorList); //creates the prototype

        }else if(values[4].equals("ACCELERATION")){  // if sensors are NOT included in the command
            //System.out.println("We are in acceleration if statement");
            ap = new ActuatorPrototype(actuatorId, typeOfActuator.getGroups(), Double.parseDouble(values[6]),
                    Double.parseDouble(values[8]), Double.parseDouble(values[10]),Double.parseDouble(values[13]),
                    Double.parseDouble(values[20]),Double.parseDouble(values[16]), Double.parseDouble(values[18]),
                    Double.parseDouble(values[23]), typeOfActuator.getSensors());

        }else{
            throw new IOException("Something went wrong in " + values[4]);
        }

        symbolTableActuator = parserHelper.getSymbolTableActuator();
        symbolTableActuator.add(actuatorId,ap);

        System.out.println(symbolTableActuator.get(actuatorId));
    }
    private void sensorBuilder(String[] values)
    {
        System.out.println("sensor");




    }


    private void mapperBuilder(String[] values) throws IOException {
        
        System.out.println("mapper");

        Identifier mapperId = Identifier.make(values[2]); //creates the id for the mapper
        A_Mapper map = null;
        
        values[3] = values[3].toUpperCase(); //changes text EQUATION OR INTERPOLATION to uppercase
        values[4] = values[4].toUpperCase(); //changes to uppercase

        if(values[3].equals("EQUATION")){

            if(values[4].equals("PASSTHROUGH")){
                map = new MapperEquation(new EquationPassthrough());
            }
            else if(values[4].equals("SCALE")){
                double value = Double.parseDouble(values[5]);
                map = new MapperEquation(new EquationScaled(value));
            }
            else if(values[4].equals("NORMALIZE")){
                double valueMin = Double.parseDouble(values[5]);
                double valueMax = Double.parseDouble(values[6]);
                map = new MapperEquation(new EquationNormalized(valueMin, valueMax));
            }
        }
        else if(values[3].equals("INTERPOLATION")){

            MapLoader ml = new MapLoader(new Filespec(values[6])); //file name

            if(values[4].equals("LINEAR")){
                InterpolatorLinear il = new InterpolatorLinear(ml.load());
                map = new MapperInterpolation(il);
            }

            else if(values[5].equals("SPLINE")){
                InterpolatorSpline is = new InterpolatorSpline(ml.load());
                map = new MapperInterpolation(is);
            }
        }
        else{
            throw new IOException("Error: command not found: "+ values[3]);
        }
        symbolTableMapper = parserHelper.getSymbolTableMapper();
        symbolTableMapper.add(mapperId,map);
        System.out.println(symbolTableMapper.get(mapperId));

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
