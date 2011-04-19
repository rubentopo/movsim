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
package org.movsim.simulator.vehicles.longmodel.accelerationmodels.impl;

import org.movsim.input.model.vehicle.longModel.ModelInputDataKCA;
import org.movsim.simulator.impl.MyRandom;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModelCategory;

// TODO: Auto-generated Javadoc
// paper reference / Kerner book 
/**
 * The Class KCA.
 */
public class KCA extends LongitudinalModelImpl implements AccelerationModel {

    private static final double dtCA = 1; // update timestep for CA !!

    private final double v0;
    private final double k; // Multiplikator fuer sync-Abstand D=lveh+k*v*tau
    private final double pb0; // "Troedelwahrsch." for standing vehicles
    private final double pb1; // "Troedelwahrsch." for moving vehicles
    private final double pa1; // "Beschl.=Anti-Troedelwahrsch." falls v<vp
    private final double pa2; // "Beschl.=Anti-Troedelwahrsch." falls v>=vp
    private final double vp; // Geschw., ab der weniger "anti-getroedelt" wird

    private double length;

    /**
     * Instantiates a new kCA.
     * 
     * @param modelName
     *            the model name
     * @param parameters
     *            the parameters
     * @param length
     *            the length
     */
    public KCA(String modelName, ModelInputDataKCA parameters, double length) {
        super(modelName, AccelerationModelCategory.CELLULAR_AUTOMATON);

        this.v0 = parameters.getV0();
        this.k = parameters.getK();
        this.pb0 = parameters.getPb0();
        this.pb1 = parameters.getPb1();
        this.pa1 = parameters.getPa1();
        this.pa2 = parameters.getPa2();
        this.vp = parameters.getVp();

        this.length = length; // model parameter!
    }

    // copy constructor
    /**
     * Instantiates a new kCA.
     * 
     * @param kcaToCopy
     *            the kca to copy
     */
    public KCA(KCA kcaToCopy) {
        super(kcaToCopy.modelName(), kcaToCopy.getModelCategory());
        this.v0 = kcaToCopy.getV0();
        this.k = kcaToCopy.getK();
        this.pb0 = kcaToCopy.getPb0();
        this.pb1 = kcaToCopy.getPb1();
        this.pa1 = kcaToCopy.getPa1();
        this.pa2 = kcaToCopy.getPa2();
        this.vp = kcaToCopy.getVp();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel
     * #acc(org.movsim.simulator.vehicles.Vehicle,
     * org.movsim.simulator.vehicles.VehicleContainer, double, double, double)
     */
    @Override
    public double acc(Vehicle me, VehicleContainer vehContainer, double alphaT, double alphaV0, double alphaA) {
        // Local dynamical variables
        final Vehicle vehFront = vehContainer.getLeader(me);
        final double s = me.netDistance(vehFront);
        final double v = me.speed();
        final double dv = me.relSpeed(vehFront);
        return accSimple(s, v, dv, alphaT, alphaV0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel
     * #accSimple(double, double, double)
     */
    @Override
    public double accSimple(double s, double v, double dv) {
        return accSimple(s, v, dv, 1, 1);
    }

    /**
     * Acc simple.
     * 
     * @param s
     *            the s
     * @param v
     *            the v
     * @param dv
     *            the dv
     * @param alphaT
     *            the alpha t
     * @param alphaV0
     *            the alpha v0
     * @return the double
     */
    private double accSimple(double s, double v, double dv, double alphaT, double alphaV0) {

        final int v0Loc = (int) (alphaV0 * v0 + 0.5); // adapt v0 spatially
        final int vLoc = (int) (v + 0.5);

        final double kLoc = alphaT * k;
        final int a = 1; // cell length/dt^2 with dt=1 s and length 0.5 m => 0.5
                         // m/s^2

        final double pa = (vLoc < vp) ? pa1 : pa2;
        final double pb = (vLoc < 1) ? pb0 : pb1;
        final double D = length + kLoc * vLoc * dtCA; // double bei Kerner, da k
                                                      // reelle Zahl

        // dynamic part
        final int vSafe = (int) s; // (Delta x-d)/tau mit s=Delta x-d und tau=1
                                   // (s)
        final int dvSign = (dv < -0.5) ? 1 : (dv > 0.5) ? -1 : 0;
        final int vC = (s > D - length) ? vLoc + a * (int) dtCA : vLoc + a * (int) dtCA * dvSign;
        int vtilde = Math.min(Math.min(v0Loc, vSafe), vC);
        vtilde = Math.max(0, vtilde);

        // stochastic part
        final double r1 = MyRandom.nextDouble(); // noise terms ~ G(0,1)
        final int xi = (r1 < pb) ? -1 : (r1 < pb + pa) ? 1 : 0;

        int vNew = 0;
        vNew = Math.min(vtilde + a * (int) dtCA * xi, vLoc + a * (int) dtCA);
        vNew = Math.min(Math.min(v0Loc, vSafe), vNew);
        vNew = Math.max(0, vNew);

        return ((vNew - vLoc) / dtCA);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.longmodel.accelerationmodels.impl.
     * LongitudinalModelImpl#parameterV0()
     */
    @Override
    public double parameterV0() {
        return v0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.longmodel.accelerationmodels.impl.
     * LongitudinalModelImpl#getRequiredUpdateTime()
     */
    @Override
    public double getRequiredUpdateTime() {
        return dtCA; // cellular automaton requires specific dt
    }

    /**
     * Gets the v0.
     * 
     * @return the v0
     */
    public double getV0() {
        return v0;
    }

    /**
     * Gets the k.
     * 
     * @return the k
     */
    public double getK() {
        return k;
    }

    /**
     * Gets the pb0.
     * 
     * @return the pb0
     */
    public double getPb0() {
        return pb0;
    }

    /**
     * Gets the pb1.
     * 
     * @return the pb1
     */
    public double getPb1() {
        return pb1;
    }

    /**
     * Gets the pa1.
     * 
     * @return the pa1
     */
    public double getPa1() {
        return pa1;
    }

    /**
     * Gets the pa2.
     * 
     * @return the pa2
     */
    public double getPa2() {
        return pa2;
    }

    /**
     * Gets the vp.
     * 
     * @return the vp
     */
    public double getVp() {
        return vp;
    }

}
