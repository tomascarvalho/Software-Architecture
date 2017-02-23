public class SystemC
{
   public static void main( String argv[])
   {
		/****************************************************************************
		* Here we instantiate three filters.
		****************************************************************************/

		SourceFilter sourceA = new SourceFilter("SubSetA.dat");	//Reads from first file
		SourceFilter sourceB = new SourceFilter("SubSetB.dat"); //Reads from second file
		MiddleFilter middleFilter = new MiddleFilter(); //Merges And sort the content from both files
		SinkFilter sink = new SinkFilter(); //Writes to stdout

		/****************************************************************************
		* Here we connect the filters starting with the sink filter (Filter 1) which
		* we connect to Filter2 the middle filter. Then we connect Filter2 to the
		* source filter (Filter3).
		****************************************************************************/
		sink.ConnectA(middleFilter); // This esstially says, "connect Filter3 input port to Filter2 output port
		middleFilter.ConnectA(sourceA); // This esstially says, "connect Filter2 intput port to Filter1 output port
    middleFilter.ConnectB(sourceB);

		/****************************************************************************
		* Here we start the filters up. All-in-all,... its really kind of boring.
		****************************************************************************/

		sourceA.start();
		sourceB.start();

    middleFilter.start();

		sink.start();

   } // main

} // Plumber
