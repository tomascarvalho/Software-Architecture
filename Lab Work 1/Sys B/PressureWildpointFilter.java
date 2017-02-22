/******************************************************************************************************************
* File:FilterTemplate.java
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

public class PressureWildpointFilter extends FilterFramework {

  private int MeasurementLength = 8;		// This is the length of all measurements (including time) in bytes
  private int IdLength = 4;				// This is the length of IDs in the byte stream

  private long measurement;				// This is the word used to store all measurements - conversions are illustrated.
  private int id;							// This is the measurement id
  private double doubleMeasurement;

  private int i;
  private byte databyte;

  private byte[] byteBuffer;

	public void run() {
		while (true) {
			try {
        filter();
			}	catch (EndOfStreamException e) {
				ClosePorts();
				break;
			}
		}
   }

   private void filter() throws EndOfStreamException {
     readId();
     readMeasurement();

     if(id == 0) {
       sendIdByteBuffer();
       sendMeasurementByteBuffer();
     }

     // If is pressure id
     if(id == 3) {
       doubleMeasurement = Double.longBitsToDouble(measurement);
       if(doubleMeasurement > 50 && doubleMeasurement < 80) {
         sendIdByteBuffer();
         sendMeasurementByteBuffer();
       }
     }
   }

   private void sendIdByteBuffer() throws EndOfStreamException {
     byteBuffer = ByteBuffer.allocate(IdLength).putInt(id).array();
     for (byte b : byteBuffer) {
       WriteFilterOutputPort(b);
     }
   }

   private void sendMeasurementByteBuffer() throws EndOfStreamException {
     byteBuffer = ByteBuffer.allocate(MeasurementLength).putLong(measurement).array();
     for (byte b : byteBuffer) {
       WriteFilterOutputPort(b);
     }
   }

   private void readId() throws EndOfStreamException {
     id = 0;
     for (i=0; i<IdLength; i++ ) {
       databyte = ReadFilterInputPort();

       id = id | (databyte & 0xFF);

       if (i != IdLength-1) {
         id = id << 8;
       }
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
     }
   }
}
