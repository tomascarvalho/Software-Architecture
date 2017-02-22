/******************************************************************************************************************
* File:AltitudeFilter.java
* Course: 17655
* Project: Assignment 1
* Copyright: Copyright (c) 2003 Carnegie Mellon University
* Versions:
*	1.0 November 2008 - Initial rewrite of original assignment 1 (ajl).
*
* Description:
*
* This class serves as a template for creating filters. The details of threading, filter connections, input, and output
* are contained in the FilterFramework super class. In order to use this template the program should rename the class.
* The template includes the run() method which is executed when the filter is started.
* The run() method is the guts of the filter and is where the programmer should put their filter specific code.
* In the template there is a main read-write loop for reading from the input port of the filter and writing to the
* output port of the filter. This template assumes that the filter is a "normal" that it both reads and writes data.
* That is both the input and output ports are used - its input port is connected to a pipe from an up-stream filter and
* its output port is connected to a pipe to a down-stream filter. In cases where the filter is a source or sink, you
* should use the SourceFilterTemplate.java or SinkFilterTemplate.java as a starting point for creating source or sink
* filters.
*
* Parameters: 		None
*
* Internal Methods:
*
*	public void run() - this method must be overridden by this class.
*
******************************************************************************************************************/
import java.nio.ByteBuffer;

public class AltitudeFilter extends FilterFramework
{
	public void run()
    {
		byte databyte;					// This is the data byte read from the stream
		byte[] bytes; 					// Bytes array to store bytes
		double altitude_feet = 0.0;		// This is the double to store the altitude in feet
		double altitude_meters = 0.0; 	// This is the double to store the altitude in meteres
		int MeasurementLength = 8;		// This is the length of all measurements (including time) in bytes
		int IdLength = 4;				// This is the length of IDs in the byte stream

		int bytesread = 0;				// This is the number of bytes read from the stream
		int byteswritten = 0;			// This is the number of bytes written to the output stream

		long measurement;				// This is the word used to store all measurements - conversions are illustrated.
		int id;							// This is the measurement id
		int i;							// This is a loop counter


		while (true)
		{

/***************************************************************
*	The program can insert code for the filter operations
* 	here. Note that data must be received and sent one
* 	byte at a time. This has been done to adhere to the
* 	pipe and filter paradigm and provide a high degree of
* 	portabilty between filters. However, you must reconstruct
* 	data on your own. First we read a byte from the input
* 	stream...
***************************************************************/

			try
			{

				/***************************************************************************
				 // We know that the first data coming to this filter is going to be an ID and
				 // that it is IdLength long. So we first decommutate the ID bytes.
				 ****************************************************************************/

				id = 0;

				for (i=0; i<IdLength; i++ )
				{
					databyte = ReadFilterInputPort();	// This is where we read the byte from the stream...

					id = id | (databyte & 0xFF);		// We append the byte on to ID...

					if (i != IdLength-1)				// If this is not the last byte, then slide the
					{									// previously appended byte to the left by one byte
						id = id << 8;					// to make room for the next byte we append to the ID

					} // if

					bytesread++;						// Increment the byte count

				} // for


				measurement = 0;

				/****************************************************************************
				 // Here we read measurements. All measurement data is read as a stream of bytes
				 // and stored as a long value. This permits us to do bitwise manipulation that
				 // is neccesary to convert the byte stream into data words. Note that bitwise
				 // manipulation is not permitted on any kind of floating point types in Java.
				 // If the id = 0 then this is a time value and is therefore a long value - no
				 // problem. However, if the id is something other than 0, then the bits in the
				 // long value is really of type double and we need to convert the value using
				 // Double.longBitsToDouble(long val) to do the conversion which is illustrated.
				 // below.
				 *****************************************************************************/

				for (i=0; i<MeasurementLength; i++ )
				{
					databyte = ReadFilterInputPort();
					measurement = measurement | (databyte & 0xFF);	// We append the byte on to measurement...
					if (i != MeasurementLength-1)					// If this is not the last byte, then slide the
					{												// previously appended byte to the left by one byte
						measurement = measurement << 8;				// to make room for the next byte we append to the
						// measurement
					} // if

					bytesread++;									// Increment the byte count

				} // for


				/****************************************************************************
				 * Here we pick up the altitude measurement (ID = 2).
				 *****************************************************************************/
				if ( id == 2 )
				{
					// We convert the bytes to a double and store them in the altitude_feet variable
					altitude_feet = Double.longBitsToDouble(measurement);
					// We then convert the altitude from feet to meters and store it in the altitude_meters variable
					altitude_meters = (altitude_feet * 0.3048);
				} // if

				// We put the ID in the bytes array
				bytes = ByteBuffer.allocate(IdLength).putInt(id).array();

				// For each byte in the bytes array
				for (byte b : bytes)
				{
					// We send the bytes to the next filter
					WriteFilterOutputPort(b);
					byteswritten++;
				} // for

				// If it's the altitude ID, the measurement is a double
				if(id == 2)
				{
					bytes = ByteBuffer.allocate(MeasurementLength).putDouble(altitude_meters).array();
				} // if

				// Else, it's a long
				else
				{
					bytes = ByteBuffer.allocate(MeasurementLength).putLong(measurement).array();
				} // else

				// We put the measurement in the bytes array
				for (byte b : bytes)
				{
					// We send the bytes to the next filter
					WriteFilterOutputPort(b);
					byteswritten++;
				} // for

			} // try


/***************************************************************
*	When we reach the end of the input stream, an exception is
* 	thrown which is shown below. At this point, you should
* 	finish up any processing, close your ports and exit.
***************************************************************/

			catch (EndOfStreamException e)
			{
				ClosePorts();
				break;

			} // catch

		} // while

   } // run

} // AltitudeFilter
