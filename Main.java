import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

// Interface for a user (common functionality for both drivers and riders)
interface UserInterface {
    String getUsername();
    String getPassword();
}

// Interface for a driver
interface DriverInterface extends UserInterface {
    void offerRide(String destination);
    boolean acceptRideRequest(Rider rider);
    boolean rejectRideRequest(Rider rider);
}

// Interface for a rider
interface RiderInterface extends UserInterface {
    void requestRide(String destination);
    boolean receiveRideRequest(Driver driver);
    void acceptRideRequest(Driver driver);
    void rejectRideRequest(Driver driver);
}

class User implements UserInterface {
    private String username;
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }
}

class Driver extends User implements DriverInterface {
    private List<Ride> offeredRides;
    private List<Rider> rideRequests;

    public Driver(String username, String password) {
        super(username, password);
        offeredRides = new ArrayList<>();
        rideRequests = new ArrayList<>();
    }

    @Override
    public void offerRide(String destination) {
        offeredRides.add(new Ride(getUsername(), destination));
    }

    public List<Ride> getOfferedRides() {
        return offeredRides;
    }

    @Override
    public boolean acceptRideRequest(Rider rider) {
        if (rideRequests.contains(rider)) {
            // Accept the ride request
            rideRequests.remove(rider);
            return true;
        }
        return false;
    }

    @Override
    public boolean rejectRideRequest(Rider rider) {
        if (rideRequests.contains(rider)) {
            // Reject the ride request
            rideRequests.remove(rider);
            return true;
        }
        return false;
    }

    public List<Rider> getRideRequests() {
        return rideRequests;
    }
}

class Rider extends User implements RiderInterface {
    private Ride confirmedRide;
    private String requestedDestination;

    public Rider(String username, String password) {
        super(username, password);
        requestedDestination = "";
    }

    @Override
    public void requestRide(String destination) {
        requestedDestination = destination;
    }

    @Override
    public boolean receiveRideRequest(Driver driver) {
        // Check if the driver is offering a ride to the requested destination
        if (driver.getOfferedRides().stream().anyMatch(ride -> ride.getDestination().equals(requestedDestination))) {
            return true;
        }
        return false;
    }

    @Override
    public void acceptRideRequest(Driver driver) {
        // Notify the driver that the ride request is accepted
        driver.acceptRideRequest(this);
        confirmedRide = new Ride(driver.getUsername(), requestedDestination);
    }

    @Override
    public void rejectRideRequest(Driver driver) {
        // Notify the driver that the ride request is rejected
        driver.rejectRideRequest(this);
    }

    public Ride getConfirmedRide() {
        return confirmedRide;
    }
}

class Ride {
    private String driver;
    private String destination;

    public Ride(String driver, String destination) {
        this.driver = driver;
        this.destination = destination;
    }

    public String getDriver() {
        return driver;
    }

    public String getDestination() {
        return destination;
    }
}

class RideSharingPlatform {
    private Map<String, Driver> drivers;
    private Map<String, Rider> riders;

    public RideSharingPlatform() {
        drivers = new HashMap<>();
        riders = new HashMap<>();
    }

    public void registerDriver(String username, String password) {
        drivers.put(username, new Driver(username, password));
    }

    public void registerRider(String username, String password) {
        riders.put(username, new Rider(username, password));
    }

    public boolean isDriverRegistered(String username) {
        return drivers.containsKey(username);
    }

    public boolean isRiderRegistered(String username) {
        return riders.containsKey(username);
    }

    public boolean login(String username, String password, Class<? extends UserInterface> userType) {
        if (DriverInterface.class.isAssignableFrom(userType)) {
            return drivers.containsKey(username) && drivers.get(username).getPassword().equals(password);
        } else if (RiderInterface.class.isAssignableFrom(userType)) {
            return riders.containsKey(username) && riders.get(username).getPassword().equals(password);
        }
        return false;
    }

    public List<Driver> getAvailableDriversForDestination(String destination) {
        List<Driver> availableDrivers = new ArrayList<>();
        for (Driver driver : drivers.values()) {
            if (driver.getOfferedRides().stream().anyMatch(ride -> ride.getDestination().equals(destination))) {
                availableDrivers.add(driver);
            }
        }
        return availableDrivers;
    }

    public Map<String, Driver> getDrivers() {
        return drivers;
    }

    public Map<String, Rider> getRiders() {
        return riders;
    }
}



public class Main {
    public static void main(String[] args) {
        RideSharingPlatform platform = new RideSharingPlatform();
        Scanner scanner = new Scanner(System.in);

        String exitResponse;
        do {
            System.out.print("Are you a Rider or a Driver? Enter 'r' for Rider, 'd' for Driver: ");
            String userTypeInput = scanner.nextLine().trim().toLowerCase();

            if (userTypeInput.equals("r")) {
                // User is a Rider
                System.out.print("-------RIDER LOGIN/REGISTRATION-------\n");
                System.out.print("Are you a new user? Enter 'yes' for new registration, 'no' for login: ");
                String isNewUser = scanner.nextLine().trim().toLowerCase();

                if (isNewUser.equals("yes")) {
                    System.out.print("Enter your username: ");
                    String riderUsername = scanner.nextLine();
                    // Check if the username is already taken
                    while (platform.isRiderRegistered(riderUsername)) {
                        System.out.println("Username already taken. Choose a different username.");
                        System.out.print("Enter your username: ");
                        riderUsername = scanner.nextLine();
                    }

                    System.out.print("Enter password: ");
                    String riderPassword = scanner.nextLine();
                    platform.registerRider(riderUsername, riderPassword);
                    System.out.println("Rider registration successful!\n");
                }

                // Rider login
                System.out.print("Enter username for login: ");
                String riderLoginUsername = scanner.nextLine();
                System.out.print("Enter password for login: ");
                String riderLoginPassword = scanner.nextLine();
                if (platform.login(riderLoginUsername, riderLoginPassword, RiderInterface.class)) {
                    System.out.println("Rider login successful!\n");

                    // Request a ride
                    System.out.print("Enter your desired destination: ");
                    String requestedDestination = scanner.nextLine();
                    Rider rider = platform.getRiders().get(riderLoginUsername);
                    rider.requestRide(requestedDestination);

                    // View available drivers for the destination
                    List<Driver> availableDrivers = platform.getAvailableDriversForDestination(requestedDestination);
                    if (availableDrivers.isEmpty()) {
                        System.out.println("Sorry, no available drivers for " + requestedDestination + ". Try again later.");
                    } else {
                        System.out.println("Available Drivers for " + requestedDestination + ":");
                        for (int i = 0; i < availableDrivers.size(); i++) {
                            System.out.println(i + ": " + availableDrivers.get(i).getUsername());
                        }

                        // Make a request to a valid driver
                        if (!availableDrivers.isEmpty()) {
                            System.out.print("Enter the index of the driver you want to request: ");
                            int driverIndex = scanner.nextInt();
                            if (driverIndex >= 0 && driverIndex < availableDrivers.size()) {
                                Driver selectedDriver = availableDrivers.get(driverIndex);

                                // Receive response from the driver
                                if (rider.receiveRideRequest(selectedDriver)) {
                                    System.out.println("Ride request sent to " + selectedDriver.getUsername());
                                    System.out.print("Do you want to accept the ride request? (yes/no): ");
                                    String response = scanner.next().trim().toLowerCase();
                                    if (response.equals("yes")) {
                                        rider.acceptRideRequest(selectedDriver);
                                        System.out.println("Ride request accepted. Enjoy your ride!");
                                    } else {
                                        rider.rejectRideRequest(selectedDriver);
                                        System.out.println("Ride request rejected.");
                                    }
                                } else {
                                    System.out.println("Invalid driver selection. Exiting.");
                                }
                            } else {
                                System.out.println("Invalid driver index. Exiting.");
                            }
                        }
                    }

                } else {
                    System.out.println("Invalid rider credentials. Exiting.");
                }

            } else if (userTypeInput.equals("d")) {
                // User is a Driver
                System.out.print("--------DRIVER LOGIN/REGISTRATION-------\n");
                System.out.print("Are you a new user? Enter 'yes' for new registration, 'no' for login: ");
                String isNewUser = scanner.nextLine().trim().toLowerCase();

                if (isNewUser.equals("yes")) {
                    System.out.print("Enter your username: ");
                    String driverUsername = scanner.nextLine();
                    // Check if the username is already taken
                    while (platform.isDriverRegistered(driverUsername)) {
                        System.out.println("Username already taken. Choose a different username.");
                        System.out.print("Enter your username: ");
                        driverUsername = scanner.nextLine();
                    }

                    System.out.print("Enter password: ");
                    String driverPassword = scanner.nextLine();
                    platform.registerDriver(driverUsername, driverPassword);
                    System.out.println("Driver registration successful!\n");
                }

                // Driver login
                System.out.print("Enter username for login: ");
                String driverLoginUsername = scanner.nextLine();
                System.out.print("Enter password for login: ");
                String driverLoginPassword = scanner.nextLine();
                if (platform.login(driverLoginUsername, driverLoginPassword, DriverInterface.class)) {
                    System.out.println("Driver login successful!\n");

                    // Offer a ride
                    System.out.print("Enter your offered destination: ");
                    String offeredDestination = scanner.nextLine();
                    Driver driver = platform.getDrivers().get(driverLoginUsername);
                    driver.offerRide(offeredDestination);

                    // View ride requests
                    List<Rider> rideRequests = driver.getRideRequests();
                    if (rideRequests.isEmpty()) {
                        System.out.println("No ride requests at the moment.");
                    } else {
                        System.out.println("Ride Requests:");
                        for (int i = 0; i < rideRequests.size(); i++) {
                            System.out.println(i + ": " + rideRequests.get(i).getUsername());
                        }

                        // Accept or reject ride requests
                        if (!rideRequests.isEmpty()) {
                            System.out.print("Enter the index of the rider request you want to respond to: ");
                            int requestIndex = scanner.nextInt();
                            if (requestIndex >= 0 && requestIndex < rideRequests.size()) {
                                Rider requestingRider = rideRequests.get(requestIndex);
                                System.out.print("Do you want to accept the ride request? (yes/no): ");
                                String response = scanner.next().trim().toLowerCase();
                                if (response.equals("yes")) {
                                    if (driver.acceptRideRequest(requestingRider)) {
                                        System.out.println("Ride request accepted. Enjoy the ride!");
                                    } else {
                                        System.out.println("Error accepting ride request. Exiting.");
                                    }
                                } else {
                                    driver.rejectRideRequest(requestingRider);
                                    System.out.println("Ride request rejected.");
                                }
                            } else {
                                System.out.println("Invalid ride request index. Exiting.");
                            }
                        }
                    }

                } else {
                    System.out.println("Invalid driver credentials. Exiting.");
                }

            } else {
                System.out.println("Invalid input. Exiting.");
            }

            // Ask the user if they want to exit
            System.out.print("Do you want to exit? Enter 'yes' to exit, 'no' to continue: ");
            exitResponse = scanner.next().trim().toLowerCase();
            scanner.nextLine(); // Consume the newline character
        } while (!exitResponse.equals("yes"));

        System.out.println("Exiting the program. Thank you!");
    }
}
