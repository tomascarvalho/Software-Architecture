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
import java.util.*;

public class PressureFilter extends FilterFramework {

  private int MeasurementLength = 8;		// This is the length of all measurements (including time) in bytes
  private int IdLength = 4;				// This is the length of IDs in the byte stream
  private int FrameLenght = 6;

  private long measurement;				// This is the word used to store all measurements - conversions are illustrated.
  private int id;							// This is the measurement id
  private double doubleMeasurement;

  private int i;
  private byte databyte;

  private byte[] byteBuffer;

  private LinkedList<Frame> frameBuffer;
  private double lastValidPressure;
  private boolean lastFrameReached = false;

  class Frame {
    long[] frame = new long[FrameLenght];

    public void saveMeasurement(long measurementToSave, int idToSave) {
      frame[idToSave] = measurementToSave;
    }

    public void send() throws EndOfStreamException {
      for(i = 0; i < FrameLenght; i++) {
        // if(i == 0) {
          sendIdByteBuffer(i);
          sendMeasurementByteBuffer(frame[i]);
        // } else {
        //   sendIdByteBuffer(i);
        //   sendMeasurementByteBuffer(frame[i]);
        // }
      }
    }
  }

	public void run() {
    frameBuffer = new LinkedList<Frame>();

    boolean isFirstFrame = true;

		while (true) {
			try {
        Frame frame = getFrame(isFirstFrame);

        filter(frame, isFirstFrame);

        isFirstFrame = false;
			}	catch (EndOfStreamException e) {
        try {
          System.out.println("Sending last buffer");
          sendBuffer(-lastValidPressure);
        } catch(EndOfStreamException ex) {
          System.out.println("\n" + this.getName() + "::Problem sending last buffer::" + ex);
        }
				ClosePorts();
				break;
			}
		}
  }

   private void filter(Frame frame, boolean isFirstFrame) throws EndOfStreamException {
      frameBuffer.add(frame);
      double pressure = Double.longBitsToDouble(frame.frame[3]);
      System.out.println("Read: " + pressure);
      if(pressure > 50 && pressure < 80) {
        lastValidPressure = pressure;
        System.out.println("New last valid pressure " + lastValidPressure);
        sendBuffer(lastValidPressure);
      } else {
        while(true) {
          Frame newFrame = getFrame(false);

          double newPressure = Double.longBitsToDouble(newFrame.frame[3]);
          System.out.println("Read while buffering: " + newPressure);
          if(newPressure > 50 && newPressure < 80) {
            if(isFirstFrame) {
              pressure = -newPressure;
              System.out.println("New transformed value " + pressure);
              //frame.frame[3] = Double.doubleToLongBits(pressure);
            } else {
              pressure = -interpolateValues(lastValidPressure, newPressure);
              System.out.println("New interpolated value " + pressure);
              //frame.frame[3] = Double.doubleToLongBits(pressure);
            }
            lastValidPressure = newPressure;
            System.out.println("New last valid pressure " + lastValidPressure);
            frameBuffer.add(newFrame);
            sendBuffer(pressure);
            break;
          } else {
            frameBuffer.add(newFrame);
          }

          if(lastFrameReached) {
            throw new EndOfStreamException();
          }
        }
      }
   }

   private double interpolateValues(double val1, double val2) {
     System.out.println("Interpolation: " + val1 + " " + val2);
     return (val1 + val2) / 2;
   }

   private Frame getFrame(boolean isFirstFrame) throws EndOfStreamException {
     Frame frame = new Frame();

     if(isFirstFrame) { //Read id 0
       readId();
       readMeasurement();
     }

     frame.saveMeasurement(measurement, id);

     while(true) {
       try {
         readId();
         readMeasurement();

         if(id != 0) {
           frame.saveMeasurement(measurement, id);
         } else {
           break;
         }
       } catch(EndOfStreamException e) {
         lastFrameReached = true;
         System.out.println("Last frame reached");
         break;
       }
     }

     return frame;
   }

   private void sendBuffer(double interpolatedValue) throws EndOfStreamException {

     while(frameBuffer.size() > 0) {
       Frame head = frameBuffer.removeFirst();
       double framePressure = Double.longBitsToDouble(head.frame[3]);
       if(framePressure > 50  && framePressure < 80) {
         System.out.println("Sending as last valid pressure: " + lastValidPressure);
         head.saveMeasurement(Double.doubleToLongBits(lastValidPressure), 3);
       } else {
         System.out.println("Sending as interpolated value: " + interpolatedValue);
         head.saveMeasurement(Double.doubleToLongBits(interpolatedValue), 3);
       }
       head.send();
       System.out.println("\n");
     }
   }

   private void sendIdByteBuffer(int idToSend) throws EndOfStreamException {
     byteBuffer = ByteBuffer.allocate(IdLength).putInt(idToSend).array();
     for (byte b : byteBuffer) {
       WriteFilterOutputPort(b);
     }
   }

   private void sendMeasurementByteBuffer(long measurementToSend) throws EndOfStreamException {
     byteBuffer = ByteBuffer.allocate(MeasurementLength).putLong(measurementToSend).array();
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

  //  private long readMeasurement(byte[] input) throws EndOfStreamException {
  //    long newMeasurement = 0;
  //    for (i = 0; i < MeasurementLength; i++) {
  //      databyte = input[i];
  //      newMeasurement = newMeasurement | (databyte & 0xFF);
   //
  //      if (i != MeasurementLength-1) {
  //        newMeasurement = newMeasurement << 8;
  //      }
  //    }
  //    return newMeasurement;
  //  }
}
