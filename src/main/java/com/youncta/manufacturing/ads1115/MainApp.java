package com.youncta.manufacturing.ads1115;



import com.pi4j.gpio.extension.ads.ADS1115GpioProvider;
import com.pi4j.gpio.extension.ads.ADS1115Pin;
import com.pi4j.gpio.extension.ads.ADS1x15GpioProvider.ProgrammableGainAmplifierValue;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinAnalogInput;
import com.pi4j.io.gpio.event.GpioPinAnalogValueChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerAnalog;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.swing.JTextArea;

public class MainApp  {

	public static void sleep(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		try {
			int[] ids = I2CFactory.getBusIds();
			System.out.println("Found follow I2C busses: " + Arrays.toString(ids));



			System.out.println("<--Pi4J--> ADS1115 GPIO Example ... started.");

			// number formatters
			final DecimalFormat df = new DecimalFormat("#.##");
			final DecimalFormat pdf = new DecimalFormat("###.#");

			// create gpio controller
			final GpioController gpio = GpioFactory.getInstance();

			// create custom ADS1115 GPIO provider

			final ADS1115GpioProvider adsManager = new ADS1115GpioProvider(I2CBus.BUS_1, ADS1115GpioProvider.ADS1115_ADDRESS_0x48);

			// provision gpio analog input pins from ADS1115
			GpioPinAnalogInput myInputs[] = {
					gpio.provisionAnalogInputPin(adsManager, ADS1115Pin.INPUT_A0, "MyAnalogInput-A0"),
					gpio.provisionAnalogInputPin(adsManager, ADS1115Pin.INPUT_A1, "MyAnalogInput-A1"),
					gpio.provisionAnalogInputPin(adsManager, ADS1115Pin.INPUT_A2, "MyAnalogInput-A2"),
					gpio.provisionAnalogInputPin(adsManager, ADS1115Pin.INPUT_A3, "MyAnalogInput-A3"),
			};

			// ATTENTION !!
			// It is important to set the PGA (Programmable Gain Amplifier) for all analog input pins.
			// (You can optionally set each input to a different value)
			// You measured input voltage should never exceed this value!
			//
			// In my testing, I am using a Sharp IR Distance Sensor (GP2Y0A21YK0F) whose voltage never exceeds 3.3 VDC
			// (http://www.adafruit.com/products/164)
			//
			// PGA value PGA_4_096V is a 1:1 scaled input,
			// so the output values are in direct proportion to the detected voltage on the input pins
			adsManager.setProgrammableGainAmplifier(ProgrammableGainAmplifierValue.PGA_4_096V, ADS1115Pin.ALL);


			// Define a threshold value for each pin for analog value change events to be raised.
			// It is important to set this threshold high enough so that you don't overwhelm your program with change events for insignificant changes
			//adsManager.setEventThreshold(500, ADS1115Pin.ALL);

			// Define the monitoring thread refresh interval (in milliseconds).
			// This governs the rate at which the monitoring thread will read input values from the ADC chip
			// (a value less than 50 ms is not permitted)
			//adsManager.setMonitorInterval(100);

			int i = 0;
			while (i < 100) {

				sleep(2000);

				// RAW value
				double value = adsManager.getValue(ADS1115Pin.INPUT_A0);

				// percentage
				double percent =  ((value * 100) / ADS1115GpioProvider.ADS1115_RANGE_MAX_VALUE);

				// approximate voltage ( *scaled based on PGA setting )
				double voltage = adsManager.getProgrammableGainAmplifier(ADS1115Pin.INPUT_A0).getVoltage() * (percent/100);

				// display output
				System.out.println(" (" + ADS1115Pin.INPUT_A0.getName() +") : VOLTS=" + df.format(voltage) + "  | PERCENT=" + pdf.format(percent) + "% | RAW=" + value + "       ");

				i++;
			}

			// keep program running for 10 minutes

			// stop all GPIO activity/threads by shutting down the GPIO controller
			// (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
			gpio.shutdown();
		
	    } catch (Exception exception) {
	        System.out.println("I/O error during fetch of I2C busses occurred");
	    }
        System.out.println("Exiting ADS1115GpioExample");
    }
        
	

	/**
	 * Create the application.
	 */
	public MainApp() {

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				System.out.println("Exiting");
			}
		}));

	}
	
}
