package test;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class GeneralTest {
	public static void main ( String [] args ) {
		System.out.println ((String.format("%s","sdsdd")));
		NumberFormat format = new DecimalFormat("0.############################################################");
		System.out.println(format.format(Math.ulp(0F)));
		System.out.println(format.format(3.3205E-5F));
	}

}
