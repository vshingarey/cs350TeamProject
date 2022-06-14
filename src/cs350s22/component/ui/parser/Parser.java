package cs350s22.component.ui.parser;


import cs350s22.component.A_Component;
import cs350s22.component.logger.LoggerMessage;
import cs350s22.component.logger.LoggerMessageSequencing;
import cs350s22.component.sensor.mapper.function.equation.EquationNormalized;
import cs350s22.component.sensor.mapper.function.equation.EquationPassthrough;
import cs350s22.component.sensor.mapper.function.equation.EquationScaled;
import cs350s22.component.sensor.mapper.function.interpolator.InterpolatorLinear;
import cs350s22.component.sensor.mapper.function.interpolator.InterpolatorSpline;
import cs350s22.component.sensor.mapper.function.interpolator.loader.MapLoader;
import cs350s22.component.sensor.watchdog.mode.A_WatchdogMode;
import cs350s22.component.sensor.watchdog.mode.WatchdogModeAverage;
import cs350s22.component.sensor.watchdog.mode.WatchdogModeInstantaneous;
import cs350s22.component.sensor.watchdog.mode.WatchdogModeStandardDeviation;
import cs350s22.component.ui.A_Interface;
import cs350s22.component.ui.CommandLineInterface;
import cs350s22.message.actuator.MessageActuatorReportPosition;
import cs350s22.message.actuator.MessageActuatorRequestPosition;
import cs350s22.message.ping.MessagePing;
import cs350s22.network.Network;
import cs350s22.support.Clock;
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


import java.io.File;
import java.io.FileNotFoundException;
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
    SymbolTable<A_Reporter> symbolTableReporter;
    SymbolTable<A_Watchdog> symbolTableWatchdog;
    SymbolTable<A_Controller> symbolTableController;

    public Parser (A_ParserHelper parserHelper, String parse)
    {
        this.parserHelper = parserHelper;
        this.parse = parse;

    }

    public void parse () throws IOException, ParseException {

        String[] values = parse.split(" "); //splits the string into single strings and stores in values


        if(values.length > 1) {     //deals with startup.parse("@exit") in main

            values[0] = values[0].toUpperCase();    //converts values[1] string to uppercase

            if (values[0].equals("@CREATE") || values[0].equals("@BUILD")) { //If second string is CREATE

                values[1] = values[1].toUpperCase(); // Change values[1] to upper case

                switch (values[1]) {
                    case "ACTUATOR":
                        actuatorBuilder(values);
                        break;
                    case "SENSOR":
                        sensorBuilder(values);
                        break;
                    case "MAPPER":
                        mapperBuilder(values);
                        break;
                    case "NETWORK":
                        networkBuilder(values);
                        break;
                    case "REPORTER":
                        reporterBuilder(values);
                        break;
                    case "WATCHDOG":
                        watchdogBuilder(values);
                        break;
                    default:
                        throw new IOException("Error: command not found: " + values[1]);
                }
            }else if(values[0].equals("@CONFIGURE")){
                configure(values);
            }else if(values[0].equals("@CLOCK")){
                clock(values);
            }else if(values[0].equals("@RUN")){
                run(values);
            }else if(values[0].equals("SEND")){
                messageBuilder(values);
            }
        }
        if(values[0].equals("@CLOCK") && values.length == 1){
            Clock myClock = Clock.getInstance();
            System.out.println("Current clock: " + myClock.getTime());
        }
        if(values[0].equals("@EXIT")){

            parserHelper.exit();
        }


    } // ends parse method

    private void actuatorBuilder(String[] values){
        System.out.println("actuator");
        values[2] = values[2].toUpperCase(); // changes type of actuator to uppercase


        Identifier actuatorId = Identifier.make(values[3]); //identifier for the actuator
        ActuatorPrototype ap;   //prototype actuator
        symbolTableActuator = parserHelper.getSymbolTableActuator();
        symbolTableSensor = parserHelper.getSymbolTableSensor();
        StringBuilder currentSb = new StringBuilder();

        String groupString = "";
        String sensorString = "";
        List<Identifier> groupList = new ArrayList<>();
        List<A_Sensor> sensorList = new ArrayList<>();
        double leadInVal = 0.0;
        double leadOutVal = 0.0;
        double relaxVal = 0.0;
        double velocityLimitVal = 0.0;
        double minVal = 0.0;
        double maxVal = 0.0;
        double initialVal = 0.0;
        double jerkLimitVal = 0.0;

        for(int i = 0; i < values.length; i++) {
            switch (values[i]) {
                case "GROUPS":
                case "GROUP":

                    for (int j = i + 1; j < values.length; j++) {
                        if (values[j].equals("SENSOR") || values[j].equals("SENSORS") || values[j].equals("ACCELERATION")) {
                            groupString = currentSb.toString();
                            currentSb.setLength(0);
                            j = values.length;
                        } else {
                            currentSb.append(values[j]);
                            currentSb.append(" ");
                        }
                    }// end of for loop in j
                    break;
                case "SENSOR":
                case "SENSORS":
                    for (int j = i + 1; j < values.length; j++) {
                        if (values[j].equals("ACCELERATION")) {
                            sensorString = currentSb.toString();
                            currentSb.setLength(0);
                            j = values.length;
                        } else {
                            currentSb.append(values[j]);
                            currentSb.append(" ");
                        }
                    }
                    break;
                case "ACCELERATION":
                    leadInVal = Double.parseDouble(values[i + 2]);
                    leadOutVal = Double.parseDouble(values[i + 4]);
                    relaxVal = Double.parseDouble(values[i + 6]);
                    velocityLimitVal = Double.parseDouble(values[i + 9]);
                    minVal = Double.parseDouble(values[i + 12]);
                    maxVal = Double.parseDouble(values[i + 14]);
                    initialVal = Double.parseDouble(values[i + 16]);
                    jerkLimitVal = Double.parseDouble(values[i + 19]);
                    i = values.length;
                    break;
            }
        } //Deals with the string entirely
        if(!groupString.isEmpty()){
            String[] groupStringArr = groupString.split(" ");

            for(String temp: groupStringArr){
                Identifier tempId = Identifier.make(temp);
                groupList.add(tempId);
            }
        }
        if(!sensorString.isEmpty()){
            String[] sensorStringArr = sensorString.split(" ");

            for(String temp: sensorStringArr){
                Identifier tempId = Identifier.make(temp);
                if(symbolTableSensor.contains(tempId)) {
                    sensorList.add(symbolTableSensor.get(tempId)); // sensor has to be created
                }
            }
        }
        ap = new ActuatorPrototype(actuatorId,groupList,leadInVal,leadOutVal,relaxVal,velocityLimitVal,initialVal,minVal,
                maxVal,jerkLimitVal,sensorList);
        symbolTableActuator.add(actuatorId,ap);

        // System.out.println(symbolTableActuator.get(actuatorId));
    }
    private void sensorBuilder(String[] values)
    {
        System.out.println("sensor");

        StringBuilder currentStringBuilder = new StringBuilder();

        String groupsString = "";
        String reportersString = "";
        String watchdogsString = "";
        String mapperString = "";
        boolean isWatchDogThere = false;
        boolean isReporterThere = false;
        boolean isReporterSet = false;
        boolean isMapperThere = false;

        for(int x = 0; x < values.length; x++){

            if(values[x].equals("GROUP") || values[x].equals("GROUPS")){ //x = 4
                x++; // x = 5
                currentStringBuilder.append(values[x]);
                currentStringBuilder.append(" ");
            }
            if(x > 5 && (!values[x].equals("REPORTER") && !values[x].equals("REPORTERS") &&
                    !values[x].equals("WATCHDOG") && !values[x].equals("WATCHDOGS") &&
                    !values[x].equals("MAPPER"))){

                currentStringBuilder.append(values[x]);
                currentStringBuilder.append(" ");
            }
            if(x > 5 && (values[x].equals("REPORTER") || values[x].equals("REPORTERS"))){ // x = 7
                isReporterThere = true;
                if(groupsString.isEmpty()){
                    groupsString = currentStringBuilder.toString();
                    currentStringBuilder.setLength(0);
                }
            }
            if(x > 5 && (values[x].equals("WATCHDOG") || values[x].equals("WATCHDOGS"))){
                isWatchDogThere = true;
                if(groupsString.isEmpty()){ // if this is empty, that means we set groupString
                    groupsString = currentStringBuilder.toString();
                    currentStringBuilder.setLength(0);
                }else{
                    reportersString = currentStringBuilder.toString();  //sets reporters here
                    currentStringBuilder.setLength(0);
                    isReporterSet = true;
                }
            }
            if(x > 5 && (values[x].equals("MAPPER"))){
                isMapperThere = true;
                if(groupsString.isEmpty()){
                    groupsString = currentStringBuilder.toString();
                    currentStringBuilder.setLength(0);
                }else if(reportersString.isEmpty() && isWatchDogThere) {
                    watchdogsString = currentStringBuilder.toString();
                    currentStringBuilder.setLength(0);
                }else if(isReporterSet){
                    watchdogsString = currentStringBuilder.toString();
                    currentStringBuilder.setLength(0);
                }else if(isReporterThere){
                    reportersString = currentStringBuilder.toString();
                    currentStringBuilder.setLength(0);
                }
                mapperString = values[x+1];
            }
        }
        if(groupsString.isEmpty()){ //deals with if we go into none of those if statements
            groupsString = currentStringBuilder.toString();
            currentStringBuilder.setLength(0);
        }else{
            if(isReporterThere && isReporterSet && !isMapperThere){
                watchdogsString = currentStringBuilder.toString();
                currentStringBuilder.setLength(0);
            }else if(isReporterThere && !isWatchDogThere && !isMapperThere){
                reportersString = currentStringBuilder.toString();
                currentStringBuilder.setLength(0);
            }else if(isWatchDogThere && !isReporterThere && !isMapperThere){
                watchdogsString = currentStringBuilder.toString();
                currentStringBuilder.setLength(0);
            }
        }
        System.out.println("Testing group string:   " + groupsString);
        System.out.println("Testing reporters string:   " + reportersString);
        System.out.println("Testing watchdogs string:   " + watchdogsString);
        System.out.println("Testing mapper string:   " + mapperString);

        // Above deals with the String

        List<Identifier> groupList = new ArrayList<>();     // A list that will contain groups
        List<A_Reporter> reporterList = new ArrayList<>();  // A list that will contain the reporters
        List<A_Watchdog> watchdogList = new ArrayList<>();  // A list that will contain the watchdogs
        A_Mapper mapper = null;                             // A mapper if needed

        MySensor myNewSensor;
        symbolTableWatchdog = parserHelper.getSymbolTableWatchdog();
        symbolTableReporter = parserHelper.getSymbolTableReporter();
        symbolTableSensor = parserHelper.getSymbolTableSensor();
        symbolTableMapper = parserHelper.getSymbolTableMapper();
        Identifier sensorId = Identifier.make(values[3]); //sensor id to add the sensor to table

        if(!groupsString.isEmpty()){
            String[] groupsStringArr = groupsString.split(" ");
            for(String temp: groupsStringArr){
                Identifier tempId = Identifier.make(temp);
                groupList.add(tempId);
            }
        }
        if(!watchdogsString.isEmpty()){
            String[] watchdogStringArr = watchdogsString.split(" ");

            for(String temp: watchdogStringArr){
                Identifier tempId = Identifier.make(temp);
                if(symbolTableWatchdog.contains(tempId)) {
                    watchdogList.add(symbolTableWatchdog.get(tempId));
                }
            }
        } // DONE PASS

        if(!reportersString.isEmpty()){
            String[] reporterStringArr = reportersString.split(" ");
            for(String temp : reporterStringArr){
                Identifier tempId = Identifier.make(temp);
                if(symbolTableReporter.contains(tempId)) {
                    reporterList.add(symbolTableReporter.get(tempId));
                }
            }
        }
        if(!mapperString.isEmpty()){
            Identifier tempId = Identifier.make(mapperString);
            if(symbolTableMapper.contains(tempId)) {
                mapper = symbolTableMapper.get(tempId);
            }
        }
        if(!reporterList.isEmpty() && !watchdogList.isEmpty() && mapper != null){
            myNewSensor = new MySensor(sensorId,groupList,reporterList,watchdogList,mapper);
            symbolTableSensor.add(sensorId,myNewSensor);
        }else{
            myNewSensor = new MySensor(sensorId);
            symbolTableSensor.add(sensorId,myNewSensor);
        }
        System.out.println(symbolTableSensor.toString());
    }
    private void mapperBuilder(String[] values) throws IOException {

        System.out.println("mapper");
        Identifier id = Identifier.make(values[2]);
        values[3] = values[3].toUpperCase();
        values[4] = values[4].toUpperCase();
        symbolTableMapper = parserHelper.getSymbolTableMapper();

        if(values[3].equals("EQUATION")){

            switch (values[4]) {
                case "PASSTHROUGH": {
                    MapperEquation map = new MapperEquation(new EquationPassthrough());
                    symbolTableMapper.add(id, map);
                    System.out.println(symbolTableMapper.get(id));
                    break;
                }
                case "SCALE": {
                    double value = Double.parseDouble(values[5]);
                    MapperEquation map = new MapperEquation(new EquationScaled(value));
                    symbolTableMapper.add(id, map);
                    System.out.println(symbolTableMapper.get(id));
                    break;
                }
                case "NORMALIZE": {
                    double valueMin = Double.parseDouble(values[5]);
                    double valueMax = Double.parseDouble(values[6]);
                    MapperEquation map = new MapperEquation(new EquationNormalized(valueMin, valueMax));
                    symbolTableMapper.add(id, map);
                    System.out.println(symbolTableMapper.get(id));
                    break;
                }
            }
        }
        else if(values[3].equals("INTERPOLATION")){ // Will leave for reference
            MapLoader ml = new MapLoader(new Filespec(values[6]));

            if(values[4].equals("LINEAR")){
                InterpolatorLinear il = new InterpolatorLinear(ml.load());
                MapperInterpolation map = new MapperInterpolation(il);
                symbolTableMapper.add(id, map);
            }

            else if(values[4].equals("SPLINE")){
                InterpolatorSpline is = new InterpolatorSpline(ml.load());
                MapperInterpolation map = new MapperInterpolation(is);
                symbolTableMapper.add(id, map);
            }
        }
        else{
            throw new IOException("Error: command not found: "+ values[3]);
        }


    }
    private void networkBuilder(String[] values)
    {
        System.out.println("network");
        symbolTableSensor = parserHelper.getSymbolTableSensor();
        symbolTableController = parserHelper.getSymbolTableController();
        symbolTableActuator = parserHelper.getSymbolTableActuator();
        A_Controller myController = parserHelper.getControllerMaster(); // master controller to add to


        if(values[3].equals("COMPONENT") || values[3].equals("COMPONENTS")) {
            for (int i = 4; i < values.length; i++) {
                Identifier tempId = Identifier.make(values[i]);
                A_Component tempComponent;

                // Controller already exists so we dont have to check for controller parameter
                if(symbolTableActuator.contains(tempId)){
                    System.out.println("checkmark actuator");
                    tempComponent = symbolTableActuator.get(tempId);
                    myController.addComponent(tempComponent);
                }else if(symbolTableSensor.contains(tempId)){
                    System.out.println("checkmark sensor");
                    tempComponent = symbolTableSensor.get(tempId);
                    myController.addComponent(tempComponent);
                }
            }
        }

        System.out.println("TESSTT");
        System.out.println(parserHelper.getNetwork());


    }
    private void reporterBuilder(String[] values) throws IOException {

        System.out.println("reporter");
        Identifier reporterId = Identifier.make(values[3]);
        StringBuilder currentSB = new StringBuilder();
        ReporterChange reporterChangeObj;
        ReporterFrequency reporterFrequencyObj;
        symbolTableReporter = parserHelper.getSymbolTableReporter();

        String idString = "";
        String groupString = "";
        int deltaFrequencyInt = 1;

        List<Identifier> idList = new ArrayList<>();
        List<Identifier> groupList = new ArrayList<>();

        if(values[5].equals("IDS")) {
            for(int i = 6; i < values.length; i++){
                if(values[i].equals("DELTA") || values[i].equals("FREQUENCY")){ //means there is no groups provided
                    idString = currentSB.toString();
                    currentSB.setLength(0);
                    deltaFrequencyInt = Integer.parseInt(values[i+1]);
                    i = values.length;
                }else if(values[i].equals("GROUP") || values[i].equals("GROUPS")) { //if there is groups
                    idString = currentSB.toString(); //sets currentSB to idString
                    currentSB.setLength(0);          //resets the currentSB
                    for(int j = i + 1; j < values.length; j++){
                        if(values[j].equals("DELTA") || values[j].equals("FREQUENCY")){
                            groupString = currentSB.toString();
                            currentSB.setLength(0);
                            deltaFrequencyInt = Integer.parseInt(values[j+1]);
                            j = values.length;
                            i = values.length;
                        }else{
                            currentSB.append(values[j]);
                            currentSB.append(" "); //ends for loop if we get to GROUP or DELTA
                        }
                    }
                }else{
                    currentSB.append(values[i]);
                    currentSB.append(" "); //ends for loop if we get to GROUP or DELTA
                }
            }
        }
        if(values[5].equals("GROUP") || values[5].equals("GROUPS")){ //deal with only groups and no IDS
            for(int i = 6; i < values.length; i++){
                if(values[i].equals("DELTA") || values[i].equals("FREQUENCY")){
                    groupString = currentSB.toString();
                    currentSB.setLength(0);
                    deltaFrequencyInt = Integer.parseInt(values[i+1]);
                    i = values.length;
                }else{
                    currentSB.append(values[i]);
                    currentSB.append(" ");
                }
            }
        }
        if(!groupString.isEmpty()) {
            String[] groupStringArr = groupString.split(" ");
            for (String temp : groupStringArr) {
                Identifier tempId = Identifier.make(temp);
                groupList.add(tempId);
            }
        } //fills groupList with groups
        if(!idString.isEmpty()){
            String[] idStringArr = idString.split(" ");
            for(String temp: idStringArr){
                Identifier tempId = Identifier.make(temp);
                idList.add(tempId);
            }
        } //fills idList with ids
        if(values[2].equals("CHANGE")){
            if(!idList.isEmpty() && !groupList.isEmpty()) {
                reporterChangeObj = new ReporterChange(idList, groupList, deltaFrequencyInt);
                symbolTableReporter.add(reporterId, reporterChangeObj);
            }else{
                throw new IOException("idList or groupList in reporter creation was null in reporter method");
            }
        }else if(values[2].equals("FREQUENCY")){
            if(!idList.isEmpty() && !groupList.isEmpty()) {
                reporterFrequencyObj = new ReporterFrequency(idList, groupList, deltaFrequencyInt);
                symbolTableReporter.add(reporterId,reporterFrequencyObj);
            }else{
                throw new IOException("idList or groupList in reporter creation was null in reporter method");
            }
        }else{
            throw new IOException("Error in Command: " + values[2]);
        }

        System.out.println(symbolTableReporter.toString());

    }
    private void watchdogBuilder(String[] values) throws IOException {
        System.out.println("watchdog");
        values[2] = values[2].toUpperCase();
        Identifier watchdogId = Identifier.make(values[3]);
        symbolTableWatchdog = parserHelper.getSymbolTableWatchdog();

        switch (values[2]) {
            case "ACCELERATION":
                switch (values[5]) {
                    case "INSTANTANEOUS":
                        WatchdogAcceleration watchdogAccelerationInstant; //creates acceleration object

                        if (values.length < 12) {
                            watchdogAccelerationInstant = new WatchdogAcceleration(Double.parseDouble(values[8]),
                                    Double.parseDouble(values[10]), new WatchdogModeInstantaneous()); //If no Grace
                        } else {
                            watchdogAccelerationInstant = new WatchdogAcceleration(Double.parseDouble(values[8]),
                                    Double.parseDouble(values[10]), new WatchdogModeInstantaneous(), Integer.parseInt(values[12]));
                            // If there is Grace
                        }
                        symbolTableWatchdog.add(watchdogId, watchdogAccelerationInstant);
                        System.out.println(symbolTableWatchdog.get(watchdogId));

                        break;
                    case "AVERAGE":
                        WatchdogAcceleration watchdogAccelerationAverage;
                        if (values.length == 14) { // IF there is a grace value AND an Average value
                            watchdogAccelerationAverage = new WatchdogAcceleration(Double.parseDouble(values[9]),
                                    Double.parseDouble(values[11]), new WatchdogModeAverage(Integer.parseInt(values[6])),
                                    Integer.parseInt(values[13]));

                        } else if (values.length == 12) { //if there is an average value but no Grace Val
                            watchdogAccelerationAverage = new WatchdogAcceleration(Double.parseDouble(values[9]),
                                    Double.parseDouble(values[11]), new WatchdogModeAverage(Integer.parseInt(values[6])));

                        } else if (values.length == 13) { // if there is a grace value but no average val
                            watchdogAccelerationAverage = new WatchdogAcceleration(Double.parseDouble(values[8]),
                                    Double.parseDouble(values[10]), new WatchdogModeAverage(), Integer.parseInt(values[12]));

                        } else { //there is no grace values or no average value
                            watchdogAccelerationAverage = new WatchdogAcceleration(Double.parseDouble(values[8]),
                                    Double.parseDouble(values[10]), new WatchdogModeAverage());
                        }
                        symbolTableWatchdog.add(watchdogId, watchdogAccelerationAverage);
                        System.out.println(symbolTableWatchdog.get(watchdogId));

                        break;
                    case "STANDARD":
                        WatchdogAcceleration watchdogAccelerationStandardDev;
                        if (values.length == 15) { //If there is a grace AND there is a standard dev
                            watchdogAccelerationStandardDev = new WatchdogAcceleration(Double.parseDouble(values[10]),
                                    Double.parseDouble(values[12]), new WatchdogModeStandardDeviation(Integer.parseInt(values[7])),
                                    Integer.parseInt(values[14]));
                        } else if (values.length == 14) { // if there is a grace and NO standard dev
                            watchdogAccelerationStandardDev = new WatchdogAcceleration(Double.parseDouble(values[9]),
                                    Double.parseDouble(values[11]), new WatchdogModeStandardDeviation(), Integer.parseInt(values[13]));
                        } else if (values.length == 13) { //if there is a standard dev but no grace
                            watchdogAccelerationStandardDev = new WatchdogAcceleration(Double.parseDouble(values[10]),
                                    Double.parseDouble(values[12]), new WatchdogModeStandardDeviation(Integer.parseInt(values[7])));
                        } else { // there is no grace and there is no standard dev
                            watchdogAccelerationStandardDev = new WatchdogAcceleration(Double.parseDouble(values[9]),
                                    Double.parseDouble(values[11]), new WatchdogModeStandardDeviation());
                        }
                        symbolTableWatchdog.add(watchdogId, watchdogAccelerationStandardDev);
                        System.out.println(symbolTableWatchdog.get(watchdogId));

                        break;
                    default:
                        throw new IOException("Something went wrong in Acceleration Watchdog");
                }


                break;
            case "BAND":
                switch (values[5]) {
                    case "INSTANTANEOUS":
                        WatchdogBand watchdogBandInstant; //creates acceleration object

                        if (values.length < 12) {
                            watchdogBandInstant = new WatchdogBand(Double.parseDouble(values[8]),
                                    Double.parseDouble(values[10]), new WatchdogModeInstantaneous()); //If no Grace
                        } else {
                            watchdogBandInstant = new WatchdogBand(Double.parseDouble(values[8]),
                                    Double.parseDouble(values[10]), new WatchdogModeInstantaneous(), Integer.parseInt(values[12]));
                            // If there is Grace
                        }
                        symbolTableWatchdog.add(watchdogId, watchdogBandInstant);
                        System.out.println(symbolTableWatchdog.get(watchdogId));
                        break;
                    case "AVERAGE":
                        WatchdogBand watchdogBandAverage;
                        if (values.length == 14) { // IF there is a grace value AND an Average value
                            watchdogBandAverage = new WatchdogBand(Double.parseDouble(values[9]),
                                    Double.parseDouble(values[11]), new WatchdogModeAverage(Integer.parseInt(values[6])),
                                    Integer.parseInt(values[13]));

                        } else if (values.length == 12) { //if there is an average value but no Grace Val
                            watchdogBandAverage = new WatchdogBand(Double.parseDouble(values[9]),
                                    Double.parseDouble(values[11]), new WatchdogModeAverage(Integer.parseInt(values[6])));

                        } else if (values.length == 13) { // if there is a grace value but no average val
                            watchdogBandAverage = new WatchdogBand(Double.parseDouble(values[8]),
                                    Double.parseDouble(values[10]), new WatchdogModeAverage(), Integer.parseInt(values[12]));

                        } else { //there is no grace values or no average value
                            watchdogBandAverage = new WatchdogBand(Double.parseDouble(values[8]),
                                    Double.parseDouble(values[10]), new WatchdogModeAverage());
                        }
                        symbolTableWatchdog.add(watchdogId, watchdogBandAverage);
                        System.out.println(symbolTableWatchdog.get(watchdogId));
                        break;
                    case "STANDARD":
                        WatchdogBand watchdogBandStandardDev;
                        if (values.length == 15) { //If there is a grace AND there is a standard dev
                            watchdogBandStandardDev = new WatchdogBand(Double.parseDouble(values[10]),
                                    Double.parseDouble(values[12]), new WatchdogModeStandardDeviation(Integer.parseInt(values[7])),
                                    Integer.parseInt(values[14]));
                        } else if (values.length == 14) { // if there is a grace and NO standard dev
                            watchdogBandStandardDev = new WatchdogBand(Double.parseDouble(values[9]),
                                    Double.parseDouble(values[11]), new WatchdogModeStandardDeviation(), Integer.parseInt(values[13]));
                        } else if (values.length == 13) { //if there is a standard dev but no grace
                            watchdogBandStandardDev = new WatchdogBand(Double.parseDouble(values[10]),
                                    Double.parseDouble(values[12]), new WatchdogModeStandardDeviation(Integer.parseInt(values[7])));
                        } else { // there is no grace and there is no standard dev
                            watchdogBandStandardDev = new WatchdogBand(Double.parseDouble(values[9]),
                                    Double.parseDouble(values[11]), new WatchdogModeStandardDeviation());
                        }
                        symbolTableWatchdog.add(watchdogId, watchdogBandStandardDev);
                        System.out.println(symbolTableWatchdog.get(watchdogId));

                        break;
                    default:
                        throw new IOException("Something went wrong in BAND Watchdog");
                }

                break;
            case "NOTCH":
                switch (values[5]) {
                    case "INSTANTANEOUS":
                        WatchdogNotch watchdogNotchInstant; //creates acceleration object

                        if (values.length < 12) {
                            watchdogNotchInstant = new WatchdogNotch(Double.parseDouble(values[8]),
                                    Double.parseDouble(values[10]), new WatchdogModeInstantaneous()); //If no Grace
                        } else {
                            watchdogNotchInstant = new WatchdogNotch(Double.parseDouble(values[8]),
                                    Double.parseDouble(values[10]), new WatchdogModeInstantaneous(), Integer.parseInt(values[12]));
                            // If there is Grace
                        }
                        symbolTableWatchdog.add(watchdogId, watchdogNotchInstant);
                        System.out.println(symbolTableWatchdog.get(watchdogId));

                        break;
                    case "AVERAGE":
                        WatchdogNotch watchdogNotchAverage;
                        if (values.length == 14) { // IF there is a grace value AND an Average value
                            watchdogNotchAverage = new WatchdogNotch(Double.parseDouble(values[9]),
                                    Double.parseDouble(values[11]), new WatchdogModeAverage(Integer.parseInt(values[6])),
                                    Integer.parseInt(values[13]));

                        } else if (values.length == 12) { //if there is an average value but no Grace Val
                            watchdogNotchAverage = new WatchdogNotch(Double.parseDouble(values[9]),
                                    Double.parseDouble(values[11]), new WatchdogModeAverage(Integer.parseInt(values[6])));

                        } else if (values.length == 13) { // if there is a grace value but no average val
                            watchdogNotchAverage = new WatchdogNotch(Double.parseDouble(values[8]),
                                    Double.parseDouble(values[10]), new WatchdogModeAverage(), Integer.parseInt(values[12]));

                        } else { //there is no grace values or no average value
                            watchdogNotchAverage = new WatchdogNotch(Double.parseDouble(values[8]),
                                    Double.parseDouble(values[10]), new WatchdogModeAverage());
                        }
                        symbolTableWatchdog.add(watchdogId, watchdogNotchAverage);
                        System.out.println(symbolTableWatchdog.get(watchdogId));

                        break;
                    case "STANDARD":
                        WatchdogNotch watchdogNotchStandardDev;
                        if (values.length == 15) { //If there is a grace AND there is a standard dev
                            watchdogNotchStandardDev = new WatchdogNotch(Double.parseDouble(values[10]),
                                    Double.parseDouble(values[12]), new WatchdogModeStandardDeviation(Integer.parseInt(values[7])),
                                    Integer.parseInt(values[14]));
                        } else if (values.length == 14) { // if there is a grace and NO standard dev
                            watchdogNotchStandardDev = new WatchdogNotch(Double.parseDouble(values[9]),
                                    Double.parseDouble(values[11]), new WatchdogModeStandardDeviation(), Integer.parseInt(values[13]));
                        } else if (values.length == 13) { //if there is a standard dev but no grace
                            watchdogNotchStandardDev = new WatchdogNotch(Double.parseDouble(values[10]),
                                    Double.parseDouble(values[12]), new WatchdogModeStandardDeviation(Integer.parseInt(values[7])));
                        } else { // there is no grace and there is no standard dev
                            watchdogNotchStandardDev = new WatchdogNotch(Double.parseDouble(values[9]),
                                    Double.parseDouble(values[11]), new WatchdogModeStandardDeviation());
                        }
                        symbolTableWatchdog.add(watchdogId, watchdogNotchStandardDev);
                        System.out.println(symbolTableWatchdog.get(watchdogId));

                        break;
                    default:
                        throw new IOException("Something went wrong in Notch Watchdog");
                }

                break;
            case "LOW":
                switch (values[5]) {
                    case "INSTANTANEOUS":
                        WatchdogLow watchdogLowInstant; //creates acceleration object

                        if (values.length < 9) {
                            watchdogLowInstant = new WatchdogLow(Double.parseDouble(values[7]), new WatchdogModeInstantaneous()); //If no Grace
                        } else {
                            watchdogLowInstant = new WatchdogLow(Double.parseDouble(values[7]),
                                    new WatchdogModeInstantaneous(), Integer.parseInt(values[9]));
                            // If there is Grace
                        }
                        symbolTableWatchdog.add(watchdogId, watchdogLowInstant);
                        System.out.println(symbolTableWatchdog.get(watchdogId));

                        break;
                    case "AVERAGE":
                        WatchdogLow watchdogLowAverage;
                        if (values.length == 11) { // IF there is a grace value AND an Average value
                            watchdogLowAverage = new WatchdogLow(Double.parseDouble(values[8]),
                                    new WatchdogModeAverage(Integer.parseInt(values[6])),
                                    Integer.parseInt(values[10]));

                        } else if (values.length == 9) { //if there is an average value but no Grace Val
                            watchdogLowAverage = new WatchdogLow(Double.parseDouble(values[8]),
                                    new WatchdogModeAverage(Integer.parseInt(values[6])));

                        } else if (values.length == 10) { // if there is a grace value but no average val
                            watchdogLowAverage = new WatchdogLow(Double.parseDouble(values[7]),
                                    new WatchdogModeAverage(), Integer.parseInt(values[9]));

                        } else { //there is no grace values or no average value
                            watchdogLowAverage = new WatchdogLow(Double.parseDouble(values[7]), new WatchdogModeAverage());
                        }
                        symbolTableWatchdog.add(watchdogId, watchdogLowAverage);
                        System.out.println(symbolTableWatchdog.get(watchdogId));

                        break;
                    case "STANDARD":
                        WatchdogLow watchdogLowStandardDev;
                        if (values.length == 12) { //If there is a grace AND there is a standard dev
                            watchdogLowStandardDev = new WatchdogLow(Double.parseDouble(values[9]),
                                    new WatchdogModeStandardDeviation(Integer.parseInt(values[7])),
                                    Integer.parseInt(values[11]));
                        } else if (values.length == 11) { // if there is a grace and NO standard dev
                            watchdogLowStandardDev = new WatchdogLow(Double.parseDouble(values[8]),
                                    new WatchdogModeStandardDeviation(), Integer.parseInt(values[10]));
                        } else if (values.length == 10) { //if there is a standard dev but no grace
                            watchdogLowStandardDev = new WatchdogLow(Double.parseDouble(values[9]),
                                    new WatchdogModeStandardDeviation(Integer.parseInt(values[7])));
                        } else { // there is no grace and there is no standard dev
                            watchdogLowStandardDev = new WatchdogLow(Double.parseDouble(values[8]),
                                    new WatchdogModeStandardDeviation());
                        }
                        symbolTableWatchdog.add(watchdogId, watchdogLowStandardDev);
                        System.out.println(symbolTableWatchdog.get(watchdogId));

                        break;
                    default:
                        throw new IOException("Something went wrong in Low Watchdog");
                }

                break;
            case "HIGH":
                switch (values[5]) {
                    case "INSTANTANEOUS":
                        WatchdogHigh watchdogHighInstant; //creates acceleration object

                        if (values.length < 9) {
                            watchdogHighInstant = new WatchdogHigh(Double.parseDouble(values[7]), new WatchdogModeInstantaneous()); //If no Grace
                        } else {
                            watchdogHighInstant = new WatchdogHigh(Double.parseDouble(values[7]),
                                    new WatchdogModeInstantaneous(), Integer.parseInt(values[9]));
                            // If there is Grace
                        }
                        symbolTableWatchdog.add(watchdogId, watchdogHighInstant);
                        System.out.println(symbolTableWatchdog.get(watchdogId));

                        break;
                    case "AVERAGE":
                        WatchdogHigh watchdogHighAverage;
                        if (values.length == 11) { // IF there is a grace value AND an Average value
                            watchdogHighAverage = new WatchdogHigh(Double.parseDouble(values[8]),
                                    new WatchdogModeAverage(Integer.parseInt(values[6])),
                                    Integer.parseInt(values[10]));

                        } else if (values.length == 9) { //if there is an average value but no Grace Val
                            watchdogHighAverage = new WatchdogHigh(Double.parseDouble(values[8]),
                                    new WatchdogModeAverage(Integer.parseInt(values[6])));

                        } else if (values.length == 10) { // if there is a grace value but no average val
                            watchdogHighAverage = new WatchdogHigh(Double.parseDouble(values[7]),
                                    new WatchdogModeAverage(), Integer.parseInt(values[9]));

                        } else { //there is no grace values or no average value
                            watchdogHighAverage = new WatchdogHigh(Double.parseDouble(values[7]), new WatchdogModeAverage());
                        }
                        symbolTableWatchdog.add(watchdogId, watchdogHighAverage);
                        System.out.println(symbolTableWatchdog.get(watchdogId));

                        break;
                    case "STANDARD":
                        WatchdogHigh watchdogHighStandardDev;
                        if (values.length == 12) { //If there is a grace AND there is a standard dev
                            watchdogHighStandardDev = new WatchdogHigh(Double.parseDouble(values[9]),
                                    new WatchdogModeStandardDeviation(Integer.parseInt(values[7])),
                                    Integer.parseInt(values[11]));
                        } else if (values.length == 11) { // if there is a grace and NO standard dev
                            watchdogHighStandardDev = new WatchdogHigh(Double.parseDouble(values[8]),
                                    new WatchdogModeStandardDeviation(), Integer.parseInt(values[10]));
                        } else if (values.length == 10) { //if there is a standard dev but no grace
                            watchdogHighStandardDev = new WatchdogHigh(Double.parseDouble(values[9]),
                                    new WatchdogModeStandardDeviation(Integer.parseInt(values[7])));
                        } else { // there is no grace and there is no standard dev
                            watchdogHighStandardDev = new WatchdogHigh(Double.parseDouble(values[8]),
                                    new WatchdogModeStandardDeviation());
                        }
                        symbolTableWatchdog.add(watchdogId, watchdogHighStandardDev);
                        System.out.println(symbolTableWatchdog.get(watchdogId));

                        break;
                    default:
                        throw new IOException("Something went wrong in Low Watchdog");
                }
                break;
            default:
                throw new IOException("Watchdog command error" + values[2]);
        }

    }

    public void configure(String[] values){
        try {
            Filespec fs1 = new Filespec(values[2]);
            LoggerMessage.initialize(fs1);

            Filespec fs2 = new Filespec((values[5]));
            Filespec fs3 = new Filespec(values[7]);
            LoggerMessageSequencing.initialize(fs2, fs3);
        }catch (IOException e){
            System.out.println("Something went wrong in Configure");
        }
    }

    public void clock(String[] values){
        Clock myClock = Clock.getInstance();

        if(values[1].equals("PAUSE")){
            myClock.isActive(false);
        }else if(values[1].equals("RESUME")){
            myClock.isActive(true);
        } //Option1
        if(values[1].equals("ONESTEP")){
            if(!myClock.isActive()){ //If not active
                if(values.length == 3){ // means there is count included
                    myClock.onestep(Integer.parseInt(values[2]));
                }else{
                    myClock.onestep();
                }
            }
        } //Option2

        if(values[1].equals("SET")){
            myClock.setRate(Integer.parseInt(values[3]));
        } //Option3

        if(values[1].equals("WAIT")){
            if(values[2].equals("FOR")){
                myClock.waitFor(Double.parseDouble(values[3]));
            }else if(values[2].equals("UNTIL")){
                myClock.waitUntil(Double.parseDouble(values[3]));
            }
        } //Option 8 and 9, option 7 at the top = Done

    }
    public void run(String[] values) throws ParseException {
        System.out.println("Run");
        try {
            parserHelper.run(values[1]);

        }catch (IOException e){
            e.printStackTrace();
        }
    }
    private void messageBuilder(String[] values) throws IOException
    {
        System.out.print("message");
        int hasPING = -1;
        int hasID = -1;
        int hasGROUPS = -1;
        int hasREQUEST = -1;
        int hasREPORT = -1;
        for(int i = 2; i < values.length; i++) {
            if(values[i].toUpperCase().equals("PING")){
                hasPING = i;
            }
            else if(values[i].toUpperCase().equals("REQUEST")){
                hasREQUEST = i;
            }
            else if(values[i].toUpperCase().equals("REPORT")){
                hasREPORT = i;
            }
            if(values[i].toUpperCase().equals("ID")){
                hasID = i;
            }
            if(values[i].toUpperCase().equals("GROUPS")){
                hasGROUPS = i;
            }
        }
        List<Identifier> idList = new ArrayList<>();
        List<Identifier> groupList = new ArrayList<>();

        CommandLineInterface cli = parserHelper.getCommandLineInterface();

        if(hasPING > 0) {
			/* Sends a ping message to the master controller, which propagates it recursively
			throughout the network regardless of whether controllers are forwarding or nonforwarding. */
            cli.issueMessage(new MessagePing());
			/* Get the command line interface from ParserHelper. Create a MessagePing and send it through
			the command line interface with issueMessage(). */
        }
        else if(hasREQUEST > 0) { //SEND MESSAGE ... POSITION REQUEST value
			/* Sends a request message to the recipients such that their position is expected to go to value, if possible.
			Inappropriate requests (for example, to a sensor) are ignored. */
            if(hasID > 0 && hasGROUPS < 0){ //ID passed, but no GROUPS passed
                //values[2] == ID
                for(int i = 3; i < hasREQUEST - 1; i++){
                    idList.add(Identifier.make(values[i]));
                }
                for (Identifier id : idList) { //remove inappropriate ids
                    if(symbolTableController.contains(id) || symbolTableSensor.contains(id) || symbolTableWatchdog.contains(id) || symbolTableMapper.contains(id) || symbolTableReporter.contains(id)){
                        idList.remove(id);
                    }
                }
                cli.issueMessage(new MessageActuatorRequestPosition(idList, Double.parseDouble(values[values.length - 1])));
            }
            else if(hasGROUPS > 0 && hasID < 0){ //GROUPS passed, but no ID passed
                //values[2] == GROUPS
                for(int i = 3; i < hasREQUEST - 1; i++){
                    groupList.add(Identifier.make(values[i]));
                }
                for (Identifier group : groupList) { //remove inappropriate ids
                    if(symbolTableController.contains(group) || symbolTableSensor.contains(group) || symbolTableWatchdog.contains(group) || symbolTableMapper.contains(group) || symbolTableReporter.contains(group)){
                        idList.remove(group);
                    }
                }
                cli.issueMessage(new MessageActuatorRequestPosition(groupList, Double.parseDouble(values[values.length - 1])));
            }
            else if(hasID > 0 && hasGROUPS > 0){ //ID passed and GROUPS passed
                if(hasID < hasGROUPS){ //ID comes before GROUPS
                    for(int i = 3; i < hasGROUPS; i++){
                        idList.add(Identifier.make(values[i]));
                    }
                    for(int i = hasGROUPS + 1; i < hasREQUEST - 1; i ++){
                        groupList.add(Identifier.make(values[i]));
                    }
                }
                else{ //GROUPS comes before ID
                    for(int i = 3; i < hasID; i++){
                        groupList.add(Identifier.make(values[i]));
                    }
                    for(int i = hasID + 1; i < hasREQUEST - 1; i++){
                        idList.add(Identifier.make(values[i]));
                    }
                }
                for (Identifier id : idList) { //remove inappropriate ids
                    if(symbolTableController.contains(id) || symbolTableSensor.contains(id) || symbolTableWatchdog.contains(id) || symbolTableMapper.contains(id) || symbolTableReporter.contains(id)){
                        idList.remove(id);
                    }
                }
                for (Identifier group : groupList) { //remove inappropriate ids
                    if(symbolTableController.contains(group) || symbolTableSensor.contains(group) || symbolTableWatchdog.contains(group) || symbolTableMapper.contains(group) || symbolTableReporter.contains(group)){
                        idList.remove(group);
                    }
                }
                cli.issueMessage(new MessageActuatorRequestPosition(idList, Double.parseDouble(values[values.length - 1])));
                cli.issueMessage(new MessageActuatorRequestPosition(groupList, Double.parseDouble(values[values.length - 1])));
            }
            else {
                throw new IOException("Invalid SEND MESSAGE POSITION REQUEST");
            }
			/* Get the command line interface from ParserHelper.
			A_Message message = new MessageActuatorRequestPosition(as_appropriate);
			cli.issueMessage(message); */
            //A_Message message = new Message...
        }
        else if(hasREPORT > 0) { //SEND MESSAGE ... POSITION REPORT
			/* Sends a request message to the recipients such that they report their current value, if possible.
			Inappropriate requests (for example, to a controller) are ignored. */
            if(hasID > 0 && hasGROUPS < 0){ //ID passed, but no GROUPS passed
                //values[2] == ID
                for(int i = 3; i < hasREPORT - 1; i++){
                    idList.add(Identifier.make(values[i]));
                }
                for (Identifier id : idList) { //remove inappropriate ids
                    if(symbolTableController.contains(id) || symbolTableSensor.contains(id) || symbolTableWatchdog.contains(id) || symbolTableMapper.contains(id) || symbolTableReporter.contains(id)){
                        idList.remove(id);
                    }
                }
                cli.issueMessage(new MessageActuatorReportPosition(idList));
            }
            else if(hasGROUPS > 0 && hasID < 0){ //GROUPS passed, but no ID passed
                //values[2] == GROUPS
                for(int i = 3; i < hasREPORT - 1; i++){
                    groupList.add(Identifier.make(values[i]));
                }
                for (Identifier group : groupList) { //remove inappropriate ids
                    if(symbolTableController.contains(group) || symbolTableSensor.contains(group) || symbolTableWatchdog.contains(group) || symbolTableMapper.contains(group) || symbolTableReporter.contains(group)){
                        idList.remove(group);
                    }
                }
                cli.issueMessage(new MessageActuatorReportPosition(groupList));
            }
            else if(hasID > 0 && hasGROUPS > 0){ //ID passed and GROUPS passed
                if(hasID < hasGROUPS){ //ID comes before GROUPS
                    for(int i = 3; i < hasGROUPS; i++){
                        idList.add(Identifier.make(values[i]));
                    }
                    for(int i = hasGROUPS + 1; i < hasREPORT - 1; i ++){
                        groupList.add(Identifier.make(values[i]));
                    }
                }
                else{ //GROUPS comes before ID
                    for(int i = 3; i < hasID; i++){
                        groupList.add(Identifier.make(values[i]));
                    }
                    for(int i = hasID + 1; i < hasREPORT - 1; i++){
                        idList.add(Identifier.make(values[i]));
                    }
                }
                for (Identifier id : idList) { //remove inappropriate ids
                    if(symbolTableController.contains(id) || symbolTableSensor.contains(id) || symbolTableWatchdog.contains(id) || symbolTableMapper.contains(id) || symbolTableReporter.contains(id)){
                        idList.remove(id);
                    }
                }
                for (Identifier group : groupList) { //remove inappropriate ids
                    if(symbolTableController.contains(group) || symbolTableSensor.contains(group) || symbolTableWatchdog.contains(group) || symbolTableMapper.contains(group) || symbolTableReporter.contains(group)){
                        idList.remove(group);
                    }
                }
                cli.issueMessage(new MessageActuatorReportPosition(idList));
                cli.issueMessage(new MessageActuatorReportPosition(groupList));
            }
            else {
                throw new IOException("Invalid SEND MESSAGE POSITION REPORT");
            }

        }
        else{
            throw new IOException("Error: command not found: "+ values[2]);
        }


    }


}