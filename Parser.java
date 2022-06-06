package cs350s22.component.ui.parser;

//Identifire is the class we use for defining the ids of each component
import cs350s22.support.Identifier;
import java.io.IOException;;

public class Parser 
{
	A_ParserHelper parserHelper;
	String parse;
	
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
	private void actuatorBuilder(String[] values) 
	{
		System.out.print("actuator");
	}
	private void sensorBuilder(String[] values) 
	{
		System.out.print("sensor");
	}
	private void mapperBuilder(String[] values) 
	{
		System.out.print("mapper");
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
}
