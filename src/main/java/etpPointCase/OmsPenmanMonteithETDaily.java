package etpPointCase;
        import static java.lang.Math.exp;
        import static java.lang.Math.pow;
        import java.util.HashMap;
        import java.util.Map.Entry;
        import java.util.Set;

        import oms3.annotations.Author;
        import oms3.annotations.Description;
        import oms3.annotations.Execute;
        import oms3.annotations.In;
        import oms3.annotations.Keywords;
        import oms3.annotations.Label;
        import oms3.annotations.License;
        import oms3.annotations.Name;
        import oms3.annotations.Out;
        import oms3.annotations.Status;
        import oms3.annotations.Unit;

        import org.jgrasstools.gears.libs.modules.JGTModel;

/*
 * This file is part of JGrasstools (http://www.jgrasstools.org)
 * (C) HydroloGIS - www.hydrologis.com
 *
 * JGrasstools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
@Description("Calculates evapotranspiration at daily timestep using Penman-Monteith equation")
@Author(name = "Michele Bottazzi", contact = "michele.bottazzi@gmail.com")
@Keywords("Evapotranspiration, Hydrology")
@Label("")
@Name("")
@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")
public class OmsPenmanMonteithETDaily extends JGTModel {
    ///////////
	@Description("The maximum daily air temperature.")
    @In
    @Unit("C")
    public HashMap<Integer, double[]> inMaxTemp;
    public double defaultMaxTemp = 15.0;
    ///////////
    @Description("The minimum daily air temperature.")
    @In
    @Unit("C")
    public HashMap<Integer, double[]> inMinTemp;
    @Description("The min temperature default value in case of missing data.")
    @In
    @Unit("C")
    public double defaultMinTemp = 15.0;
    ///////////
    @Description("The wind at the surface in km/hr for the current day.")
    @In
    @Unit("m s-1")
    public HashMap<Integer, double[]> inWind;
    @Description("The wind default value in case of missing data.")
    @In
    @Unit("m s-1")
    public double defaultWind = 2.0;
    ///////////
    @Description("The average air daily relative humidity.")
    @In
    @Unit("%")
    public HashMap<Integer, double[]> inRelativeHumidity;
    @Description("The humidity default value in case of missing data.")
    @In
    @Unit("%")
    public double defaultRelativeHumidity = 90.0;
    ////////////
    @Description("The net Radiation at the grass surface in W/m2 for the current day.")
    @In
    @Unit("J m-2 s-1")
    public HashMap<Integer, double[]> inNetradiation;
    @Description("The net Radiation default value in case of missing data.")
    @In
    @Unit("J m-2 s-1")
    public double defaultNetradiation = 30.0;
    ///////////
    @Description("The average atmospheric daily air pressure in hPa.")
    @In
    @Unit("Pa")
    public HashMap<Integer, double[]> inPressure;
    @Description("The default average atmospheric daily air pressure in hPa.")
    @In
    @Unit("Pa")
    public double defaultPressure = 100000.0;
    ///////////
    @Description("The average soilflux.")
    @In
    @Unit("J m-2 s-1")
    public HashMap<Integer, double[]> inSoilFlux;
    @Description("The default average atmospheric daily air pressure in hPa.")
    @In
    @Unit("J m-2 s-1")
    public double defaultSoilFlux = 0.0;
    ///////////
    @Description("The elevation of basin centroid.")
    @In
    @Unit("m")
    
    double nullValue = -9999.0;
    ///////////
    // TODO Add the elevation value in case of missing P data
    @Description("The reference evapotranspiration.")
    @Unit("mm day-1")
    @Out
    public HashMap<Integer, double[]> outPMEtp;
    @Execute
    public void process() throws Exception {
    	outPMEtp = new HashMap<Integer, double[]>();
        Set<Entry<Integer, double[]>> entrySet = inMaxTemp.entrySet();
        for( Entry<Integer, double[]> entry : entrySet ) {
            Integer basinId = entry.getKey();
            double maxTemperature = inMaxTemp.get(basinId)[0];
            if (maxTemperature == nullValue) {maxTemperature = defaultMaxTemp;}
            
            double minTemperature = inMinTemp.get(basinId)[0];
            if (minTemperature  == nullValue) {minTemperature = defaultMinTemp;}
            
            double relativeHumidity = inRelativeHumidity.get(basinId)[0];
            if (relativeHumidity  == nullValue) {relativeHumidity = defaultRelativeHumidity;}
            
            double netRadiation = inNetradiation.get(basinId)[0]*0.8;
            if (netRadiation  == nullValue) {netRadiation = defaultNetradiation;}
            
            double soilFlux = inSoilFlux.get(basinId)[0];
            if (soilFlux  == nullValue) {soilFlux = defaultSoilFlux;}
            
            double wind = inWind.get(basinId)[0];
            if (wind == nullValue) {wind = defaultWind;}
            
            double pressure = inPressure.get(basinId)[0];
            if (pressure == nullValue) {pressure = defaultPressure;}
            
            
            double etp = compute(maxTemperature, minTemperature, relativeHumidity, netRadiation, soilFlux, wind, pressure);
            outPMEtp.put(basinId, new double[]{etp});
        }
    }
    private double compute(double maxTemperature,double minTemperature,double relativeHumidity,double netRadiation,double soilFlux,double wind,double pressure) {
    	// Computation of Delta [KPa °C-1]
        double defaultGasConstant = 287.058;
        double defaultSpecificHeat = 1003.5;
        double defaultAeroResistance = 208;
        double defaultSurfResistance = 70;
        double meanTemperature = (maxTemperature + minTemperature) / 2.0;
        double denDelta = pow(meanTemperature + 237.3, 2);
        double expDelta = (17.27 * meanTemperature) / (meanTemperature + 237.3);
        double expDeltaMax = (17.27 * maxTemperature) / (maxTemperature + 237.3);
        double expDeltaMin = (17.27 * minTemperature) / (minTemperature + 237.3);
        double numDelta = 4098 * (0.6108 * exp(expDelta));
        double delta = numDelta*1000 / denDelta;
        // End Computation of Delta

        // Computation of Psicrometric constant gamma[kPa °C-1]
        double gamma = 0.000800 * pressure;
        // End Computation of Psicrometric constant gamma

        // Computation of mean saturation vapour pressure e0_AirTem [kPa]
        double e0AirTemMax = 0.6108 * exp(expDeltaMax);
        double e0AirTemMin = 0.6108 * exp(expDeltaMin);
        double es = (e0AirTemMax + e0AirTemMin) / 2.0;
        // End of computation of mean saturation vapour pressure e0_AirTem

        // Computation of average hourly actual vapour pressure ea [kPa]
        double ea = es*relativeHumidity/100;//Math.log((zCentroid - dHeight)/zMomentum)*Math.log((zCentroid - dHeight)/zHeat) / Math.pow(karmanConstant,2)*inWind;
        // End of computation average hourly actual vapour pressure ea

        // Computation of Aerodynamic resistance [s m^-1]
        //double aerodynamicResistance = relativeHumidity / 100.0 * ((e0_AirTemMax + e0_AirTemMin) / 2.0);
        //double bulkResistance = relativeHumidity / 100.0 * ((e0_AirTemMax + e0_AirTemMin) / 2.0);
        // End of computation average hourly actual vapour pressure ea

        // compute the daily evapotranspiration in mm/day
        double firstTerm = delta * (netRadiation - soilFlux);
        double density = pressure/ ((meanTemperature+273) * defaultGasConstant);
        double secondTerm = (density * defaultSpecificHeat) * ((es - ea) / defaultAeroResistance);
        double den = delta + gamma * (1 + defaultSurfResistance/defaultAeroResistance);
        double result = (firstTerm + secondTerm) / den;
        return result;
    }
}
