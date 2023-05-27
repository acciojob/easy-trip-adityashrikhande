package com.driver.controllers;


import com.driver.model.Airport;
import com.driver.model.City;
import com.driver.model.Flight;
import com.driver.model.Passenger;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
public class AirportController {
    ArrayList<Airport> airports = new ArrayList<>();
    ArrayList<Flight> flights = new ArrayList<>();
    ArrayList<Passenger> passengers = new ArrayList<>();
    HashMap<City, String> mp = new HashMap<>();
    HashMap<Passenger, ArrayList<Integer>> passengerBookings = new HashMap<>();
    HashMap<Flight, Integer> noOfBookings = new HashMap<>();
    HashMap<Integer, Integer> revenue = new HashMap<>();
    @PostMapping("/add_airport")
    public String addAirport(@RequestBody Airport airport){

        //Simply add airport details to your database
        //Return a String message "SUCCESS"

        airports.add(airport);

        mp.put(airport.getCity(), airport.getAirportName());

        return "SUCCESS";
    }

    @GetMapping("/get-largest-aiport")
    public String getLargestAirportName(){

        //Largest airport is in terms of terminals. 3 terminal airport is larger than 2 terminal airport
        //Incase of a tie return the Lexicographically smallest airportName
        int n = airports.size();
        int maxTerminals = 0;
        String LargestAirport = "";
        for(int i=0; i<n; i++){
            if(maxTerminals < airports.get(i).getNoOfTerminals()){
                maxTerminals = airports.get(i).getNoOfTerminals();
                LargestAirport = airports.get(i).getAirportName();
            }else if(maxTerminals == airports.get(i).getNoOfTerminals()){
                if(LargestAirport.compareTo(airports.get(i).getAirportName()) < 0){
                    LargestAirport = airports.get(i).getAirportName();
                }
            }
        }
        return LargestAirport;

       //return null;
    }

    @GetMapping("/get-shortest-time-travel-between-cities")
    public double getShortestDurationOfPossibleBetweenTwoCities(@RequestParam("fromCity") City fromCity, @RequestParam("toCity")City toCity){

        //Find the duration by finding the shortest flight that connects these 2 cities directly
        //If there is no direct flight between 2 cities return -1.
        int n = flights.size();
        Boolean flag = false;
        double flightTime = 1000000000.0;
        for(int i=0; i<n; i++){
            if(flights.get(i).getFromCity() == fromCity && flights.get(i).getToCity() == toCity){
                flag = true;
                if(flights.get(i).getDuration() < flightTime){
                    flightTime = flights.get(i).getDuration();
                }
            }
        }
        if(flag == true)return flightTime;

       return -1;
    }

    @GetMapping("/get-number-of-people-on-airport-on/{date}")
    public int getNumberOfPeopleOn(@PathVariable("date") Date date,@RequestParam("airportName")String airportName){

        //Calculate the total number of people who have flights on that day on a particular airport
        //This includes both the people who have come for a flight and who have landed on an airport after their flight
        int people = 0;
        int n = flights.size();
        for(int i=0; i<n; i++){
            if(flights.get(i).getFlightDate().equals(date) && mp.get(flights.get(i).getFromCity()).equals(airportName)){
                people++;
            }
            if(flights.get(i).getFlightDate().equals(date) && mp.get(flights.get(i).getToCity()).equals(airportName)){
                people++;
            }
        }

        return people;
    }

    @GetMapping("/calculate-fare")
    public int calculateFlightFare(@RequestParam("flightId")Integer flightId){

        //Calculation of flight prices is a function of number of people who have booked the flight already.
        //Price for any flight will be : 3000 + noOfPeopleWhoHaveAlreadyBooked*50
        //Suppose if 2 people have booked the flight already : the price of flight for the third person will be 3000 + 2*50 = 3100
        //This will not include the current person who is trying to book, he might also be just checking price

        int n = flights.size();

        for(int i=0; i<n; i++){
            if(flightId == flights.get(i).getFlightId()){
                return 3000 + noOfBookings.get(flights.get(i))*50;
            }
        }

       return 0;

    }


    @PostMapping("/book-a-ticket")
    public String bookATicket(@RequestParam("flightId")Integer flightId,@RequestParam("passengerId")Integer passengerId){

        //If the numberOfPassengers who have booked the flight is greater than : maxCapacity, in that case :
        //return a String "FAILURE"
        //Also if the passenger has already booked a flight then also return "FAILURE".
        //else if you are able to book a ticket then return "SUCCESS"

        int a = flights.size();
        Flight fl = new Flight();

        for(int i=0; i<a; i++){
            if(flightId == flights.get(i).getFlightId()){
                fl = flights.get(i);
                if(noOfBookings.get(flights.get(i)) >= flights.get(i).getMaxCapacity()){
                    return "FAILURE";
                }
            }
        }
        int n = passengers.size();

        for(int i=0; i<n; i++){
            if(passengerBookings.get(passengers.get(i)).contains(flightId)){
                return "FAILURE";
            }
            if(passengerId == passengers.get(i).getPassengerId()){
                passengerBookings.get(passengers.get(i)).add(flightId);
                noOfBookings.put(fl, noOfBookings.get(fl) + 1);
                revenue.put(flightId, revenue.get(fl) + calculateFlightFare(flightId));
                return "SUCCESS";
            }
        }


        return null;
    }

    @PutMapping("/cancel-a-ticket")
    public String cancelATicket(@RequestParam("flightId")Integer flightId,@RequestParam("passengerId")Integer passengerId){

        //If the passenger has not booked a ticket for that flight or the flightId is invalid or in any other failure case
        // then return a "FAILURE" message
        // Otherwise return a "SUCCESS" message
        // and also cancel the ticket that passenger had booked earlier on the given flightId
        int a = flights.size();
        boolean flag = false;
        Flight fl = new Flight();

        for(int i=0; i<a; i++){
            if(flightId == flights.get(i).getFlightId()){
                flag = true;
                break;
            }
        }
        if(!flag)return "FAILURE";

        int n = passengers.size();

        for(int i=0; i<n; i++){
            if(!passengerBookings.get(passengers.get(i)).contains(flightId)){
                return "FAILURE";
            }
            if(passengerId == passengers.get(i).getPassengerId()){
                passengerBookings.get(passengers.get(i)).remove(flightId);
                noOfBookings.put(fl, noOfBookings.get(fl) - 1);
                revenue.put(flightId, revenue.get(fl) - calculateFlightFare(flightId));
                return "SUCCESS";
            }
        }


       return null;
    }


    @GetMapping("/get-count-of-bookings-done-by-a-passenger/{passengerId}")
    public int countOfBookingsDoneByPassengerAllCombined(@PathVariable("passengerId")Integer passengerId){

        //Tell the count of flight bookings done by a passenger: This will tell the total count of flight bookings done by a passenger :
       return 0;
    }

    @PostMapping("/add-flight")
    public String addFlight(@RequestBody Flight flight){

        flights.add(flight);

        //Return a "SUCCESS" message string after adding a flight.
       return "SUCCESS";
    }


    @GetMapping("/get-aiportName-from-flight-takeoff/{flightId}")
    public String getAirportNameFromFlightId(@PathVariable("flightId")Integer flightId){

        int n = flights.size();
        for(int i=0; i<n; i++){
            if(flights.get(i).getFlightId() == flightId){
                return mp.get(flights.get(i).getFromCity());
            }
        }
        //We need to get the starting airportName from where the flight will be taking off (Hint think of City variable if that can be of some use)
        //return null incase the flightId is invalid or you are not able to find the airportName

        return null;
    }


    @GetMapping("/calculate-revenue-collected/{flightId}")
    public int calculateRevenueOfAFlight(@PathVariable("flightId")Integer flightId){

        //Calculate the total revenue that a flight could have
        //That is of all the passengers that have booked a flight till now and then calculate the revenue
        //Revenue will also decrease if some passenger cancels the flight

        return revenue.get(flightId);

    }


    @PostMapping("/add-passenger")
    public String addPassenger(@RequestBody Passenger passenger){

        //Add a passenger to the database
        //And return a "SUCCESS" message if the passenger has been added successfully.
        passengers.add(passenger);

       return "SUCCESS";
    }


}
