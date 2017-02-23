/******************************************************************************************************************
* File:MiddleFilter.java
* Course: 17655
* Project: Assignment 1
* Copyright: Copyright (c) 2003 Carnegie Mellon University
* Versions:
*	1.0 November 2008 - Sample Pipe and Filter code (ajl).
*
* Description:
*
* This class serves as an example for how to use the FilterRemplate to create a standard filter. This particular
* example is a simple "pass-through" filter that reads data from the filter's input port and writes data out the
* filter's output port.
*
* Parameters: 		None
*
* Internal Methods: None
*
******************************************************************************************************************/

import java.util.*;						// This class is used to interpret time words
import java.text.SimpleDateFormat;		// This class is used to format and write time in a string format.

public class MergeFilter extends FilterFramework
{

  private int FrameLenght = 6;
  private boolean isFirstFrameA = true;
  private boolean isFirstFrameB = true;
  private boolean lastFrameReachedA = false;
  private boolean lastFrameReachedB = false;

  class Frame {
    long[] frame = new long[FrameLenght];

    public void saveMeasurement(long measurementToSave, int idToSave) {
      System.out.println("Id to Save: " + idToSave);
      frame[idToSave] = measurementToSave;
    }

    public void send() throws EndOfStreamException {
      for(i = 0; i < FrameLenght; i++) {
        sendIdByteBuffer(i);
        sendMeasurementByteBuffer(frame[i]);
      }
    }
  }

	public void run()
    {


		int bytesread = 0;					// Number of bytes read from the input file.
		int byteswritten = 0;				// Number of bytes written to the stream.
		byte databyte = 0;					// The byte of data read from the file


    Calendar TimeStampA = Calendar.getInstance();
    Calendar TimeStampB = Calendar.getInstance();
    Frame frameA = null;
    Frame frameB = null;

		// Next we write a message to the terminal to let the world know we are alive...

		System.out.println( "\n" + this.getName() + "::Middle Reading ");

		while (true)
		{
			/*************************************************************
			*	Here we read a byte and write a byte
			*************************************************************/

			try
			{

        // If one of them has reached the last frame, the other one should be sent without any other comparison
        if(lastFrameReachedA && !lastFrameReachedB) {
          frameB.send();
          frameB = null;
        } else if(!lastFrameReachedA && lastFrameReachedB) {
          frameA.send();
          frameA = null;
        } else if(lastFrameReachedA && lastFrameReachedB) {
          break;
        }

        // If frame A is null must be the first iteration or was the last sent
        if(frameA == null && !lastFrameReachedA) {
          frameA = getFrameA(isFirstFrameA);
          isFirstFrameA = false;
          TimeStampA.setTimeInMillis(frameA.frame[0]);
        }

        // If frame B is null must be the first iteration or was the last sent
        if(frameB == null && !lastFrameReachedB) {
          frameB = getFrameB(isFirstFrameB);
          isFirstFrameB = false;
          TimeStampB.setTimeInMillis(frameB.frame[0]);
        }

        if(frameA != null && frameB != null) {
          // Here we should have two values to compare
          if(TimeStampA.after(TimeStampB)) {
            frameA.send();
            frameA = null;
          } else {
            frameB.send();
            frameB = null;
          }
        }

			} // try

			catch (EndOfStreamException e)
			{
				ClosePorts();
				System.out.print( "\n" + this.getName() + "::Middle Exiting; bytes read: " + bytesread + " bytes written: " + byteswritten );
				break;

			} // catch

		} // while

   } // run

   private Frame getFrameA(boolean isFirstFrame) throws EndOfStreamException {
     Frame frame = new Frame();

     if(isFirstFrame) { //Read id 0
       readId();
       readMeasurement();
     }


     frame.saveMeasurement(measurementA, idA);

     while(true) {
       try {
         readId();
         readMeasurement();



         if(idA != 0) {
           frame.saveMeasurement(measurementA, idA);
         } else {
           break;
         }
       } catch(EndOfStreamException e) {
         lastFrameReachedA = true;
         break;
       }
     }

     return frame;
   }

   private Frame getFrameB(boolean isFirstFrame) throws EndOfStreamException {

     Frame frame = new Frame();

     if(isFirstFrame) { //Read id 0
       readIdB();
       readMeasurementB();
     }



     frame.saveMeasurement(measurementB, idB);

     while(true) {
       try {
         readIdB();
         readMeasurementB();



         if(idB != 0) {
           frame.saveMeasurement(measurementB, idB);
         } else {
           break;
         }
       } catch(EndOfStreamException e) {
         lastFrameReachedB = true;
         break;
       }
     }

     return frame;
   }

} // MiddleFilter
