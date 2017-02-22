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

  int MeasurementLength = 8;		// This is the length of all measurements (including time) in bytes
  int IdLength = 4;				// This is the length of IDs in the byte stream

  byte databyte = 0;				// This is the data byte read from the stream
  int bytesread = 0;				// This is the number of bytes read from the stream
  int byteswritten = 0;

  long measurement;				// This is the word used to store all measurements - conversions are illustrated.
  double temperature, level, pressure;
  int id;							// This is the measurement id
  int i;							// This is a loop counter

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

   private void readId() throws EndOfStreamException {
     id = 0;
     for (i=0; i<IdLength; i++ ) {
       databyte = ReadFilterInputPort();

       id = id | (databyte & 0xFF);

       if (i != IdLength-1) {
         id = id << 8;
       }

       bytesread++;
     }
   }

   private void readMeasurement() throws EndOfStreamException {
     measurement = 0;

     for (i = 0; i < MeasurementLength; i++) {
       databyte = ReadFilterInputPort();
       measurement = measurement | (databyte & 0xFF);

       if (i != MeasurementLength-1) {
         measurement = measurement << 8;
       }

       bytesread++;
     }
   }

} // SingFilter
