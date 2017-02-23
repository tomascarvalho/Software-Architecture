import java.util.*;
import java.text.SimpleDateFormat;

public class MergeFilter extends DoubleFilterFramework
{
	public void run()
    {

			Calendar TimeStamp1 = Calendar.getInstance();
			Calendar TimeStamp2 = Calendar.getInstance();
			SimpleDateFormat TimeStampFormat = new SimpleDateFormat("yyyy MM dd::hh:mm:ss:SSS");


		byte databyte = 0;					// The byte of data read from the file

		// Next we write a message to the terminal to let the world know we are alive...

		System.out.println(this.getName() + "::Merge Reading ");


			/*************************************************************
			*	Here we read a frame from each stream and write the one is ealier first so we can merge the streams and sort them
			*************************************************************/

			try
			{
					int SubsetA = 1;
					int SubsetB = 2;
					int firstIteration = 3;

					/*here we fetch one frame from SubsetA or SubsetB*/
					Frame frame1 = super.getFrame(SubsetA);
					Frame frame2 = super.getFrame(SubsetB);

					//variable to check if the streams are still readable
					int send=firstIteration;

					while(true){


					if(send<3)
					{
						//if the frame sent was from subsetB try to fetch another one
						if(send == SubsetB)
						{
							try
							{

								frame2 = super.getFrame(SubsetB);
							}
							catch(EndOfStreamException e)
							{
								//if subsetB stream already over try to fetch anoter one from subsetA
								try
								{

											frame2 = super.getFrame(SubsetA);
								}
								catch(EndOfStreamException e1)
								{
									//if cant be fetched it means both streans are over
									throw new EndOfStreamException ();
								}
							}
					}
					//if the frame sent was from subsetA try to fetch another one
					else if(send == SubsetA)
					{
						try
						{

							frame1 = super.getFrame(SubsetA);
						}
						catch(EndOfStreamException e)
						{
							//if subsetB stream already over try to fetch anoter one from subsetA
							try
							{

									frame1 = super.getFrame(SubsetB);
							}
							catch(EndOfStreamException e1)
							{
								//if cant be fetched it means both streans are over
									throw new EndOfStreamException ();

							}
						}
					}
				}

				//Convert time to be analized
					TimeStamp1.setTimeInMillis(frame1.time_measurement);
					TimeStamp2.setTimeInMillis(frame2.time_measurement);

					//if frame1 is after frame2 send frame2
				if(TimeStamp1.after(TimeStamp2))
				{
					frame2.send();
					send = SubsetB;
				//else send frame 1
				}
				else
				{
					frame1.send();
					send = SubsetA;
				}
			}
		} // try

			catch (EndOfStreamException e)
			{
				ClosePorts();
				System.out.println(this.getName() + "::Merge Exiting; bytes read: " + bytesread + " bytes written: " + byteswritten );

			} // catch



   } // run

} // MergeFilter
