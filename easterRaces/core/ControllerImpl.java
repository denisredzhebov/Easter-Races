package easterRaces.core;

import easterRaces.core.interfaces.Controller;
import easterRaces.entities.cars.Car;
import easterRaces.entities.cars.MuscleCar;
import easterRaces.entities.cars.SportsCar;
import easterRaces.entities.drivers.Driver;
import easterRaces.entities.drivers.DriverImpl;
import easterRaces.entities.racers.Race;
import easterRaces.entities.racers.RaceImpl;
import easterRaces.repositories.interfaces.CarRepository;
import easterRaces.repositories.interfaces.DriverRepository;
import easterRaces.repositories.interfaces.RaceRepository;
import easterRaces.repositories.interfaces.Repository;

import java.util.*;
import java.util.stream.Collectors;

import static easterRaces.common.ExceptionMessages.*;
import static easterRaces.common.OutputMessages.*;

public class ControllerImpl implements Controller {
    private CarRepository cars;
    private DriverRepository drivers;
    private RaceRepository races;

    public ControllerImpl(Repository<Car> cars, Repository<Driver> drivers, Repository<Race> races) {
        this.cars = new CarRepository();
        this.drivers = new DriverRepository();
        this.races = new RaceRepository();
    }

    @Override
    public String createDriver(String driver) {
        Driver driver1 = new DriverImpl(driver);
        if (this.drivers.getAll().contains(driver1)) {
            throw new IllegalArgumentException(String.format(DRIVER_EXISTS, driver));
        }
        this.drivers.add(driver1);
        return String.format(DRIVER_CREATED, driver);
    }

    @Override
    public String createCar(String type, String model, int horsePower) {
        Car car;
        if (type.equals("Muscle")) {
            car = new MuscleCar(model, horsePower);
        } else {
            car = new SportsCar(model, horsePower);
        }
        if (this.cars.getAll().contains(car)) {
            throw new IllegalArgumentException(String.format(CAR_EXISTS, model));
        }
        this.cars.add(car);
        return String.format(CAR_CREATED, type + "Car", model);
    }

    @Override
    public String addCarToDriver(String driverName, String carModel) {
        if (this.drivers.getByName(driverName) == null) {
            throw new IllegalArgumentException(String.format(DRIVER_NOT_FOUND, driverName));
        }
        if (this.cars.getByName(carModel) == null) {
            throw new IllegalArgumentException(String.format(CAR_NOT_FOUND, carModel));
        }
        Driver currentDriver = this.drivers.getByName(driverName);
        Car currentCar = this.cars.getByName(carModel);
        currentDriver.addCar(currentCar);

        return String.format(CAR_ADDED, driverName, carModel);
    }

    @Override
    public String addDriverToRace(String raceName, String driverName) {
        Race currentRace = this.races.getAll().stream().filter(a -> a.getName().equals(raceName)).findFirst().orElse(null);
        if (!this.races.getAll().contains(currentRace)) {
            throw new IllegalArgumentException(String.format(RACE_NOT_FOUND, raceName));
        }
        Driver currentDriver = this.drivers.getAll().stream().filter(a -> a.getName().equals(driverName)).findFirst().orElse(null);
        if (!this.drivers.getAll().contains(currentDriver)) {
            throw new IllegalArgumentException(String.format(DRIVER_NOT_FOUND, driverName));
        }

        currentRace.addDriver(currentDriver);

        return String.format(DRIVER_ADDED, driverName, raceName);
    }

    @Override
    public String startRace(String raceName) {
        Race currentRace = this.races.getAll().stream().filter(a -> a.getName().equals(raceName)).findFirst().orElse(null);
        if (!this.races.getAll().contains(currentRace)) {
            throw new IllegalArgumentException(String.format(RACE_NOT_FOUND, raceName));
        }

        if (this.drivers.getAll().size() < 3) {
            throw new IllegalArgumentException(String.format(RACE_INVALID, raceName, 3));
        }

        Collection<Driver> driversAll = this.drivers.getAll();
        Map<Driver, Double> driversPoints = new HashMap<>();
        for (Driver driver : driversAll) {
            driversPoints.put(driver, driver.getCar().calculateRacePoints(this.races.getByName(raceName).getLaps()));
        }

        List<Driver> top3drivers = driversPoints.entrySet()
                .stream().sorted((k, v) -> Double.compare(v.getValue(), k.getValue()))
                .limit(3).map(Map.Entry::getKey).collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();

        sb.append(String.format(DRIVER_FIRST_POSITION, top3drivers.get(0).getName(), raceName))
                .append(System.lineSeparator())
                .append(String.format(DRIVER_SECOND_POSITION, top3drivers.get(1).getName(), raceName))
                .append(System.lineSeparator())
                .append(String.format(DRIVER_THIRD_POSITION, top3drivers.get(2).getName(), raceName));

        return sb.toString().trim();


    }

    @Override
    public String createRace(String name, int laps) {
        Race currentRace = this.races.getAll().stream().filter(a -> a.getName().equals(name)).findFirst().orElse(null);
        if (this.races.getAll().contains(currentRace)) {
            throw new IllegalArgumentException(String.format(RACE_EXISTS, name));
        }
        Race race = new RaceImpl(name, laps);
        this.races.add(race);

        return String.format(RACE_CREATED, name);

    }
}
