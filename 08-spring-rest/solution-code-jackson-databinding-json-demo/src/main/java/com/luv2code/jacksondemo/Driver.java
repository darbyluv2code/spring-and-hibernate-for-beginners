package com.luv2code.jacksondemo;

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Driver {

	public static void main(String[] args) {

		try {
			
			// create object mapper
			ObjectMapper mapper = new ObjectMapper();
			
			// read JSON from file and map/convert to Java POJO
			Customer myCustomer = mapper.readValue(new File("data/sample.json"), Customer.class);
			
			// also print individual items
			System.out.println("First name = " + myCustomer.getFirstName());
			System.out.println("Last name = " + myCustomer.getLastName());		

			// get nested object: array
			Address tempAddress = myCustomer.getAddress();
			System.out.println("Street = " + tempAddress.getStreet());		
			System.out.println("City = " + tempAddress.getCity());		
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
