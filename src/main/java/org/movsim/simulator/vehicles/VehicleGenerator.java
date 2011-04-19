/**
 * Copyright (C) 2010, 2011 by Arne Kesting <movsim@akesting.de>, 
 *                             Martin Treiber <treibi@mtreiber.de>,
 *                             Ralph Germ <germ@ralphgerm.de>,
 *                             Martin Budden <mjbudden@gmail.com>
 *
 * ----------------------------------------------------------------------
 * 
 *  This file is part of 
 *  
 *  MovSim - the multi-model open-source vehicular-traffic simulator 
 *
 *  MovSim is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MovSim is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MovSim.  If not, see <http://www.gnu.org/licenses/> or
 *  <http://www.movsim.org>.
 *  
 * ----------------------------------------------------------------------
 */
package org.movsim.simulator.vehicles;

// TODO: Auto-generated Javadoc
/**
 * The Interface VehicleGenerator.
 */
public interface VehicleGenerator {

    /**
     * Gets the vehicle prototype.
     * 
     * @return the vehicle prototype
     */
    VehiclePrototype getVehiclePrototype();

    /**
     * Creates the vehicle.
     * 
     * @param prototype
     *            the prototype
     * @return the vehicle
     */
    Vehicle createVehicle(VehiclePrototype prototype);

    /**
     * Creates the vehicle.
     * 
     * @return the vehicle
     */
    Vehicle createVehicle();

    /**
     * Creates the vehicle.
     * 
     * @param type
     *            the type
     * @return the vehicle
     */
    Vehicle createVehicle(String type);

    /**
     * Required timestep.
     * 
     * @return the double
     */
    double requiredTimestep();

}
