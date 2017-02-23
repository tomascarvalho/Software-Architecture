/******************************************************************************************************************
* File:Plumber.java
* Course: 17655
* Project: Assignment 1
* Copyright: Copyright (c) 2003 Carnegie Mellon University
* Versions:
*	1.0 November 2008 - Sample Pipe and Filter code (ajl).
*
* Description:
*
* This class serves as an example to illstrate how to use the PlumberTemplate to create a main thread that
* instantiates and connects a set of filters. This example consists of three filters: a source, a middle filter
* that acts as a pass-through filter (it does nothing to the data), and a sink filter which illustrates all kinds
* of useful things that you can do with the input stream of data.
*
* Parameters: 		None
*
* Internal Methods:	None
*
******************************************************************************************************************/
public class Plumber
{
   public static void main(String argv[])
   {
		/****************************************************************************
		* Here we instantiate three filters.
		****************************************************************************/

		SourceFilter Filter1 = new SourceFilter();
        RemovePressureFilter Filter2 = new RemovePressureFilter();
        RemoveVelocityFilter Filter3 = new RemoveVelocityFilter();
        RemovePitchFilter Filter4 = new RemovePitchFilter();
        AltitudeFilter Filter5 = new AltitudeFilter();
		TemperatureFilter Filter6 = new TemperatureFilter();
		SinkFilter Filter7 = new SinkFilter();


		/****************************************************************************
		* Here we connect the filters starting with the sink filter (Filter 1) which
		* we connect to Filter2 the middle filter. Then we connect Filter2 to the
		* source filter (Filter3).
		****************************************************************************/
        Filter7.Connect(Filter6); // This essentially says, "connect Filter7 input port to Filter6 output port
        Filter6.Connect(Filter5); // This essentially says, "connect Filter6 input port to Filter5 output port
        Filter5.Connect(Filter4); // This essentially says, "connect Filter5 input port to Filter4 output port
        Filter4.Connect(Filter3); // This essentially says, "connect Filter4 input port to Filter3 output port"
		Filter3.Connect(Filter2); // This essentially says, "connect Filter3 input port to Filter2 output port
        Filter2.Connect(Filter1); // This essentially says, "connect Filter2 input port to Filter1 output port

	    //Filter2.Connect(Filter1); // This essentially says, "connect Filter2 intput port to Filter1 output port

		/****************************************************************************
		* Here we start the filters up. All-in-all,... its really kind of boring.
		****************************************************************************/

		Filter1.start();
		Filter2.start();
		Filter3.start();
        Filter4.start();
        Filter5.start();
        Filter6.start();
        Filter7.start();


   } // main

} // Plumber
