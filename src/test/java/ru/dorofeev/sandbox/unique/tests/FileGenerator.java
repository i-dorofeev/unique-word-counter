package ru.dorofeev.sandbox.unique.tests;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;

public class FileGenerator {

	@Test
	public void generate() {

		String delimiters = " ;,\n\t";

		try (PrintWriter pw = new PrintWriter("temp/data.txt")) {
			Random rnd = new Random();

			for (int i = 0; i < 1000000; i++) {
				pw.print("word" + rnd.nextInt(9));
				pw.print(delimiters.charAt(rnd.nextInt(delimiters.length())));
			}

			pw.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
