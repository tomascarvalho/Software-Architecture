/******************************************************************************************************************
* File:SinkFilter.java
* Course: 17655
* Project: Assignment 1
* Copyright: Copyright (c) 2003 Carnegie Mellon University
* Versions:
*	1.0 November 2008 - Sample Pipe and Filter code (ajl).
*
* Description:
*
* This class serves as an example for using the SinkFilterTemplate for creating a sink filter. This particular
* filter reads some input from the filter's input port and does the following:
*
*	1) It parses the input stream and "decommutates" the measurement ID
*	2) It parses the input steam for measurments and "decommutates" measurements, storing the bits in a long word.
*
* This filter illustrates how to convert the byte stream data from the upstream filterinto useable data found in
* the stream: namely time (long type) and measurements (double type).
*
*
* Parameters: 	None
*
* Internal Methods: None
*
******************************************************************************************************************/
import java.util.*;						// This class is used to interpret time words
import java.io.*; // note we must add this here since we use BufferedReader class to read from the keyboard
import java.text.SimpleDateFormat;		// This class is used to format and write time in a string format.

public class SinkFilter extends FilterFramework {

  int bytesread = 0;				// This is the number of bytes read from the stream
  int byteswritten = 0;

  double temperature, level, pressure;

	public void run()
    {
		/************************************************************************************
		*	TimeStamp is used to compute time using java.util's Calendar class.
		* 	TimeStampFormat is used to format the time value so that it can be easily printed
		*	to the terminal.
		*************************************************************************************/

		Calendar TimeStamp = Calendar.getInstance();
		SimpleDateFormat TimeStampFormat = new SimpleDateFormat("yyyy:dd:HH:mm:ss");

    String fileName = "OutputB.dat";	// Input data file.
    PrintStream out = null;			// File stream reference.

    try {
      out = new PrintStream(new FileOutputStream(fileName));
      System.out.println("\n" + this.getName() + "::Sink Writing to " + fileName );
    } catch (FileNotFoundException e) {
      ClosePorts();
      System.out.println("\n" + this.getName() + "::Problem opening output data file::" + e);
    }

		/*************************************************************
		*	First we announce to the world that we are alive...
		**************************************************************/

		System.out.print( "\n" + this.getName() + "::Sink Reading ");

		while (true)
		{
			try
			{

				readId();

				readMeasurement();

				if (id == 0) {
					TimeStamp.setTimeInMillis(measurement);
				}

        if (id == 2) {
          level = Double.longBitsToDouble(measurement);
        }

        if (id == 3) {
          pressure = Double.longBitsToDouble(measurement);
          System.out.println("Receiving pressure " + pressure);
				}

        if (id == 4) {
          temperature = Double.longBitsToDouble(measurement);
          out.format("%s\t%010.5f\t%011.5f\t", TimeStampFormat.format(TimeStamp.getTime()), temperature, level, pressure);
          if (pressure < 0) {
            pressure = -pressure;
            out.format("%3.5f*\n", pressure);
          } else {
            out.format("%3.5f\n", pressure);
          }
        }
			} // try

			/*******************************************************************************
			*	The EndOfStreamExeception below is thrown when you reach end of the input
			*	stream (duh). At this point, the filter ports are closed and a message is
			*	written letting the user know what is going on.
			********************************************************************************/

			catch (EndOfStreamException e) {
        try {
  				out.close();
  				ClosePorts();
  				System.out.println( "\n" + this.getName() + "::Write file complete, bytes read::" + bytesread + " bytes written: " + bytesread );
  			} catch (Exception closeerr) {
  				System.out.println("\n" + this.getName() + "::Problem closing input data file::" + closeerr);
  			} // catch
        break;
			}
		} // while

   } // run
} // SingFilter
