package cs350s22.component.ui.parser;

import cs350s22.support.Filespec;
//Identifire is the class we use for defining the ids of each component
import cs350s22.support.Identifier;
import cs350s22.test.ActuatorPrototype;
import cs350s22.component.actuator.*;
import cs350s22.component.sensor.*;
import cs350s22.component.sensor.mapper.*;
import cs350s22.component.sensor.mapper.function.equation.A_Equation;
import cs350s22.component.sensor.mapper.function.equation.EquationNormalized;
import cs350s22.component.sensor.mapper.function.equation.EquationPassthrough;
import cs350s22.component.sensor.mapper.function.equation.EquationScaled;
import cs350s22.component.sensor.reporter.*;
import cs350s22.component.sensor.watchdog.*;
import cs350s22.component.controller.*;
import cs350s22.component.sensor.mapper.function.equation.EquationPassthrough.*;
import cs350s22.component.sensor.mapper.function.interpolator.loader.MapLoader;
import cs350s22.component.sensor.mapper.function.interpolator.loader.MapLoader.*;
import cs350s22.component.sensor.mapper.function.interpolator.InterpolatorLinear;
import cs350s22.component.sensor.mapper.function.interpolator.InterpolatorSpline;
import cs350s22.component.sensor.mapper.function.interpolator.InterpolatorLinear.*;
import cs350s22.support.Filespec.*;
import java.io.IOException;;

public class Parser 
{
	A_ParserHelper parserHelper;
	String parse;
	
	SymbolTable<A_Actuator> symbolTableActuator = parserHelper.getSymbolTableActuator();
	SymbolTable<A_Sensor> symbolTableSensor = parserHelper.getSymbolTableSensor();
	SymbolTable<A_Mapper> symbolTableMapper = parserHelper.getSymbolTableMapper();
	SymbolTable<A_Reporter> symbolTableMessage = parserHelper.getSymbolTableReporter();
	SymbolTable<A_Watchdog> symbolTableWatchdog = parserHelper.getSymbolTableWatchdog();
	SymbolTable<A_Controller> symbolTableController = parserHelper.getSymbolTableController();
	public Parser (A_ParserHelper parserHelper, String parse) 
	{
		this.parserHelper = parserHelper;
		this.parse = parse;
	} 
	
	public void parse () throws IOException
	{
		// -------Loop through the string using a scanner 
		// Figure out which values are ID
		//Identifier id = new Identifier();
		// Figure out which values are just string commands
		//Figure out which values
		// 
		
		String[] values = parse.split(" ");
		
		//values[0] holds whichever top level command we will be using
		values[0] = values[0].toUpperCase();
		if(values[0] == "CREATE") 
		{
			//values 1 holds whichever command is to be executed
			values[1] = values[1].toUpperCase();
			if(values[1] == "ACTUATOR")
				actuatorBuilder(values);
			else if(values[1] == "SENSOR") 
			{
				sensorBuilder(values);
			}
			else if(values[1] == "MAPPER") 
			{
				mapperBuilder(values);
			}
			else if(values[1] == "NETWORK") 
			{
				networkBuilder(values);
			}
			else if(values[1] == "REPORTER") 
			{
				reporterBuilder(values);
			}
			else if(values[1] == "WATCHDOG") 
			{
				watchdogBuilder(values);
			}
			else 
			{
				throw new IOException("Error: command not found: "+ values[1]);
			}
			
			
		}//meta commands / send commands
		//else if() 
		//{
			
		//}
		else 
		{
			throw new IOException("Error: command not found: "+ values[0]);
		}
	}
	
	//What each one of us will implement
	private void actuatorBuilder(String[] values) throws IOException
	{
		System.out.print("actuator");
		
		String type = values[2].toUpperCase();
		Identifier id = Identifier.make("");
		if(type == "ROTARY" || type == "LINEAR") //for the project, we ignore the type of actuator
		{
			id = Identifier.make(values[3]); 
		}
		else 
		{
			throw new IOException("Error: command not found: "+ values[2]);
		}
		
		
		ActuatorPrototype ap = new ActuatorPrototype();		
		symbolTableActuator.add(id, ap);
	}
	private void sensorBuilder(String[] values) 
	{
		System.out.print("sensor");
	}
	private void mapperBuilder(String[] values) throws IOException
	{
		System.out.print("mapper");
		Identifier id = Identifier.make(values[2]);
		
		if(values[3].toUpperCase() == "EQUATION"){
			
			if(values[4].toUpperCase() == "PASSTHROUGH"){
				MapperEquation map = new MapperEquation(new EquationPassthrough());
				symbolTableMapper.add(id, map);
			}
			else if(values[4].toUpperCase() == "SCALE"){
				double value = Double.parseDouble(values[5]);
				MapperEquation map = new MapperEquation(new EquationScaled(value));
				symbolTableMapper.add(id, map);
			}
			else if(values[4].toUpperCase() == "NORMALIZE"){
				double valueMin = Double.parseDouble(values[5]);
				double valueMax = Double.parseDouble(values[6]);
				MapperEquation map = new MapperEquation(new EquationNormalized(valueMin, valueMax));
				symbolTableMapper.add(id, map);
			}
		}
		else if(values[3].toUpperCase() == "INTERPOLATION"){
			MapLoader ml = new MapLoader(new Filespec(values[6]));

			if(values[4].toUpperCase() == "LINEAR"){
				InterpolatorLinear il = new InterpolatorLinear(ml.load());
				MapperInterpolation map = new MapperInterpolation(il);
				symbolTableMapper.add(id, map);
			}

			else if(values[4].toUpperCase() == "SPLINE"){
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
		System.out.print("network");
	}
	private void reporterBuilder(String[] values) 
	{
		System.out.print("reporter");
	}
	private void watchdogBuilder(String[] values) 
	{
		System.out.print("watchdog");
	}
} //end Parser class