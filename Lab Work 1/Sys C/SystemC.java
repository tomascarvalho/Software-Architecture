public class SystemC
{
   public static void main( String argv[])
   {
		/****************************************************************************
		* Here we instantiate three filters.
		****************************************************************************/

		SourceFilter Filter1 = new SourceFilter("SubSetA.dat");	//Reads from first file
		SourceFilter Filter2 = new SourceFilter("SubSetB.dat"); //Reads from second file
		MergeFilter Filter3 = new MergeFilter(); //Merges And sort the content from both files
		SinkFilter Filter4 = new SinkFilter(); //Writes to stdout

		/****************************************************************************
		* Here we connect the filters starting with the sink filter (Filter 1) which
		* we connect to Filter2 the middle filter. Then we connect Filter2 to the
		* source filter (Filter3).
		****************************************************************************/
		Filter4.Connect(Filter3); // This esstially says, "connect Filter3 input port to Filter2 output port
		Filter3.Connect(Filter1, Filter2); // This esstially says, "connect Filter2 intput port to Filter1 output port

		/****************************************************************************
		* Here we start the filters up. All-in-all,... its really kind of boring.
		****************************************************************************/

		Filter1.start();
		Filter2.start();
		Filter3.start();
		Filter4.start();


   } // main

} // Plumber
