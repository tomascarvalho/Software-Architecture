/******************************************************************************************************************
* This class was created, extending FilterFramework, to allow a filter to receive input from two different pipes.
This is not the
******************************************************************************************************************/

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

public class DoubleFilterFramework extends FilterFramework
{
  private int MeasurementLength = 8;		// This is the length of all measurements (including time) in bytes
  private int IdLength = 4;				// This is the length of IDs in the byte stream
  private int FrameLenght = 6;

  private long measurement;				// This is the word used to store all measurements - conversions are illustrated.
  private int id;							// This is the measurement id
  private double doubleMeasurement;

  private int i;
  private byte databyte;

  private byte[] byteBuffer;

  int bytesread = 0;					// Number of bytes read from the input file.
  int byteswritten = 0;				// Number of bytes written to the stream.

  private LinkedList<Frame> frameBuffer;
    // Define filter input and output ports

    private PipedInputStream InputReadPort2 = new PipedInputStream();
    private int current = 0;

    // The following reference to a filter is used because java pipes are able to reliably
    // detect broken pipes on the input port of the filter. This variable will point to
    // the previous filter in the network and when it dies, we know that it has closed its
    // output pipe and will send no more data.

    private FilterFramework InputFilter2;

    /***************************************************************************
    * CONCRETE METHOD:: Connect
    * Purpose: This method connects filters to each other. All connections are
    * through the inputport of each filter. That is each filter's inputport is
    * connected to another filter's output port through this method.
    *
    * Arguments:
    *   FilterFramework (2x) - these are the filters that this filter will connect to.
    *
    * Returns: void
    *
    * Exceptions: IOException
    *
    ****************************************************************************/

    void Connect( FilterFramework Filter1, FilterFramework Filter2 )
    {
        try
        {
            // Connect this filter's input to the upstream pipe's output stream

            InputReadPort.connect( Filter1.OutputWritePort );
            InputFilter = Filter1;
            InputReadPort2.connect( Filter2.OutputWritePort );
            InputFilter2 = Filter2;

        } // try

        catch( Exception Error )
        {
            System.out.println(this.getName() + " FilterFramework error connecting::"+ Error );

        } // catch

    } // Connect


    /***************************************************************************
    * CONCRETE METHOD:: ReadFilterInputPort
    * Purpose: This method reads data from the input port one byte at a time.
    * It reads everything there is to read from the first input port, and then
    * everything there is to read from the second input port
    *
    * Arguments: void
    *
    * Returns: byte of data read from each input port of the filter.
    *
    * Exceptions: IOExecption, EndOfStreamException (rethrown)
    *
    ****************************************************************************/

    byte ReadFilterInputPort(int InputReadPortNumber) throws EndOfStreamException
    {
        byte datum = 0;

        /* Check if theres something to read form the input port*/
        if(InputReadPortNumber==1 && EndOfInputStream())
        {
            throw new EndOfStreamException("End of input stream reached");
        }
        if(InputReadPortNumber==2 && EndOfInputStream2())
        {
            throw new EndOfStreamException("End of input stream reached");
        } //if

        /* Wait until there is something to read from the current input port */
        try
        {
            if(InputReadPortNumber==1)
            {
                while (InputReadPort.available()==0)
                {

                    sleep(250);

                } // while
            }
            else if(InputReadPortNumber==2)
            {
                while (InputReadPort2.available()==0)
                {

                    sleep(250);

                } // while
            }


        } // try

        catch( Exception Error )
        {
            System.out.println(this.getName() + " Error in read port wait loop::" + Error );

        } // catch

        /***********************************************************************
        * If at least one byte of data is available on the input
        * pipe we can read it. We read and write one byte to and from ports.
        ***********************************************************************/
        try
        {
            if(InputReadPortNumber == 1)
            {
                datum = (byte)InputReadPort.read();
                return datum;
            }
            else if(InputReadPortNumber == 2)
            {
                datum = (byte)InputReadPort2.read();
                return datum;
            }
            else
            {
                return datum;
            }
        }

        catch( Exception Error )
        {
            System.out.println(this.getName() + " Error in read port wait loop::" + Error );

        } // catch
        return datum;


    } // ReadFilterPort


    /***************************************************************************
    * CONCRETE METHOD:: WriteFilterOutputPort
    * Purpose: This method writes data to the output port one byte at a time.
    *
    * Arguments:
    *   byte datum - This is the byte that will be written on the output port.of
    *   the filter.
    *
    * Returns: void
    *
    * Exceptions: IOException
    *
    ****************************************************************************/

    void WriteFilterOutputPort(byte datum)
    {
        try
        {
            OutputWritePort.write((int) datum );
            OutputWritePort.flush();

        } // try

        catch( Exception Error )
        {
            System.out.println(this.getName() + " Pipe write error::" + Error );

        } // catch

        return;

    } // WriteFilterPort

    /***************************************************************************
    * CONCRETE METHOD:: EndOfInputStream/EndOfInputStream2
    * Purpose: These methods are used within this framework which is why they are private
    * They return a true when there is no more data to read on the input port of
    * the instance filter. What they really do is to check if the upstream filter
    * is still alive. This is done because Java does not reliably handle broken
    * input pipes and will often continue to read (junk) from a broken input pipe.
    *
    * Arguments: void
    *
    * Returns: A value of true if the previous filter has stopped sending data,
    *          false if it is still alive and sending data.
    *
    * Exceptions: none
    *
    ****************************************************************************/

    private boolean EndOfInputStream()
    {
        if (InputFilter.isAlive())
        {
            return false;

        } else {

            return true;

        } // if

    } // EndOfInputStream

    private boolean EndOfInputStream2()
    {
        if (InputFilter2.isAlive())
        {
            return false;

        } else {

            return true;

        } // if

    } // EndOfInputStream

    /***************************************************************************
    * CONCRETE METHOD:: ClosePorts
    * Purpose: This method is used to close the input and output ports of the
    * filter. It is important that filters close their ports before the filter
    * thread exits.
    *
    * Arguments: void
    *
    * Returns: void
    *
    * Exceptions: IOExecption
    *
    ****************************************************************************/

    void ClosePorts()
    {
        try
        {
            InputReadPort.close();
            InputReadPort2.close();
            OutputWritePort.close();

        }
        catch( Exception Error )
        {
            System.out.println(this.getName() + " ClosePorts error::" + Error );

        } // catch

    } // ClosePorts

    /***************************************************************************
    * CONCRETE METHOD:: run
    * Purpose: This is actually an abstract method defined by Thread. It is called
    * when the thread is started by calling the Thread.start() method. In this
    * case, the run() method should be overridden by the filter programmer using
    * this framework superclass
    *
    * Arguments: void
    *
    * Returns: void
    *
    * Exceptions: IOExecption
    *
    ****************************************************************************/

    class Frame {
      long[] frame = new long[FrameLenght];
      long time_measurement;

      public void saveMeasurement(long measurementToSave, int idToSave) {
        frame[idToSave] = measurementToSave;
      }

      public void send() throws EndOfStreamException {
        //System.out.println("Sending frame...");
        for( i = 0; i < FrameLenght; i++) {
          if(i == 0) {
            sendIdByteBuffer(i);
            sendMeasurementByteBuffer(frame[i]);
          } else {
            sendIdByteBuffer(i);
            sendMeasurementByteBuffer(frame[i]);
          }
        }
        //System.out.println("Frame sent...");
      }
    }

    public Frame getFrame(int InputReadPortNumber) throws EndOfStreamException {
      //ystem.out.println("Getting frame...");
      Frame frame = new Frame();


      while(true) {
        readId(InputReadPortNumber);
        readMeasurement(InputReadPortNumber);

        if(id != 0) {
          frame.saveMeasurement(measurement, id);
        } else {
          if(id == 0)
          {
            frame.time_measurement = measurement;
            frame.saveMeasurement(measurement, id);
          }
          break;
        }
      }

      return frame;
    }


    private void sendIdByteBuffer(int idToSend) throws EndOfStreamException {
      //System.out.println("Sending id: " + idToSend);
      byteBuffer = ByteBuffer.allocate(IdLength).putInt(idToSend).array();
      for (byte b : byteBuffer) {
        WriteFilterOutputPort(b);
        byteswritten++;
      }
    }

    private void sendMeasurementByteBuffer(long measurementToSend) throws EndOfStreamException {
      //System.out.println("Sending long: " + measurementToSend);
      byteBuffer = ByteBuffer.allocate(MeasurementLength).putLong(measurementToSend).array();
      for (byte b : byteBuffer) {
        WriteFilterOutputPort(b);
        byteswritten++;
      }
    }

    private void readId(int InputReadPortNumber) throws EndOfStreamException {
      id = 0;
      for (i=0; i<IdLength; i++ ) {
        databyte = ReadFilterInputPort(InputReadPortNumber);
        bytesread++;
        id = id | (databyte & 0xFF);
        if (i != IdLength-1) {
          id = id << 8;
        }
      }
    }

    private void readMeasurement(int InputReadPortNumber) throws EndOfStreamException {
      measurement = 0;
      for (i = 0; i < MeasurementLength; i++) {
        databyte = ReadFilterInputPort(InputReadPortNumber);
        measurement = measurement | (databyte & 0xFF);
        bytesread++;
        if (i != MeasurementLength-1) {
          measurement = measurement << 8;
        }
      }
    }

    public void run()
    {
        // The run method should be overridden by the subordinate class. Please
        // see the example applications provided for more details.

    } // run

} // FilterFramework class
