package etp;
import static java.lang.Math.exp;
import static java.lang.Math.pow;
//import static org.jgrasstools.gears.libs.modules.JGTConstants.isNovalue;
//import static org.jgrasstools.gears.libs.modules.JGTConstants.isNovalue;
import static java.lang.Math.abs;
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
@Description("Calculates evapotranspiration at hourly/daily timestep using Schimanski & Or formula")
@Author(name = "Michele Bottazzi", contact = "michele.bottazzi@gmail.com")
@Keywords("Evapotranspiration, Hydrology")
@Label("")
@Name("")
@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")
public class OmsSchymanskiOrET extends JGTModel {
		
	@Description("Air temperature.")
	@In
	@Unit("K")
	public HashMap<Integer, double[]> inAirTemperature;
	@Description("The air temperature default value in case of missing data.")
	@In
	@Unit("K")
	public double defaultAirTemperature = 15.0+273.0;
	  
	@Description("Leaves length")
	@In
	@Unit("m")
	public double leafLength;
	
	@Description("Leaves side")
	@In
	@Unit("")
	public int leafSide;
	
	@Description("Leaves emissivity")
	@In
	@Unit(" ")
	public double leafEmissivity;
	
	@Description("Leaves temperature")
	@In
	@Unit("K")
	public double leafTemperature;
	
	@Description("The wind speed.")
	@In
	@Unit("m s-1")
	public HashMap<Integer, double[]> inWindVelocity;
	@Description("The wind default value in case of missing data.")
	@In
	@Unit("m s-1")
	public double defaultWindVelocity = 2.0;
	
	@Description("The air relative humidity.")
	@In
	@Unit("%")
	public HashMap<Integer, double[]> inRelativeHumidity;
	@Description("The humidity default value in case of missing data.")
	@In
	@Unit("%")
	public double defaultRelativeHumidity = 70.0;
	
	@Description("The short wave radiation at the surface.")
	@In
	@Unit("W m-2")
	public HashMap<Integer, double[]> inShortWaveRadiation;
	@Description("The short wave radiation default value in case of missing data.")
	@In
	@Unit("W m-2")
	public double defaultShortWaveRadiation = 0.0;
	
	@Description("The long wave radiation at the surface.")
	@In
	@Unit("W m-2")
	public HashMap<Integer, double[]> inLongWaveRadiation;
//	@Description("The long wave radiation default value in case of missing data.")
//	@In
//	@Unit("W m-2")
	//public double defaultLongWaveRadiation = 6.0;
	
	@Description("The atmospheric pressure.")
	@In
	@Unit("Pa")
	public HashMap<Integer, double[]> inAtmosphericPressure;
	@Description("The atmospheric pressure default value in case of missing data.")
	@In
	@Unit("Pa")
	public double defaultAtmosphericPressure = 101325.0;
	
	@Description("The soilflux.")
	@In
	@Unit("W m-2")
	public HashMap<Integer, double[]> inSoilFlux;
	@Description("The soilflux default value in case of missing data.")
	@In
	@Unit("W m-2")
	public double defaultSoilFlux = 0.0;
	
	@Description("Leaf area index.")
	@In
	@Unit("m2 m-2")
	public HashMap<Integer, double[]> inLeafAreaIndex;
	@Description("The leaf area index default value in case of missing data.")
	@In
	@Unit("m2 m-2")
	public double defaultLeafAreaIndex = 1.0;
	
	@Description("area.")
	@In
	@Unit("m2")
	public double area;
	
	@Description("Switch that defines if it is hourly.")
	@In
	public boolean doHourly = true;
	
	double waterMolarMass = 0.018;
	double latentHeatEvaporation = 2.45 * pow(10,6);
	double molarGasConstant = 8.314472;
	double nullValue = -9999.0;
	double stefanBoltzmannConstant = 5.670373 * pow(10,-8);
	public int time;
	
	@Description("The reference evapotranspiration.")
	@Unit("mm day-1")
	@Out
	public HashMap<Integer, double[]> outSOEt;
	@Description("The reference evapotranspiration.")
	@Unit("mm day-1")
	@Out
	public HashMap<Integer, double[]> outSOLT;
	@Execute
	
	public void process() throws Exception {
		//DateTime startDateTime = formatter.parseDateTime(tStartDate);
		//DateTime date=(doHourly==false)?startDateTime.plusDays(step):startDateTime.plusHours(step).plusMinutes(30); 
		
		outSOEt = new HashMap<Integer, double[]>();
		outSOLT = new HashMap<Integer, double[]>();
		Set<Entry<Integer, double[]>> entrySet = inAirTemperature.entrySet();
		for( Entry<Integer, double[]> entry : entrySet ) {
			Integer basinId = entry.getKey();    
			double relativeHumidity = inRelativeHumidity.get(basinId)[0];
			if (relativeHumidity == nullValue) {relativeHumidity = defaultRelativeHumidity;}
			
			double airTemperature = inAirTemperature.get(basinId)[0]+273.0;
			if (airTemperature == (nullValue+273.0)) {airTemperature = defaultAirTemperature;}		
			
			if (doHourly == true) {
				time =3600;
				} else {
				time = 86400;
				}
			leafTemperature = airTemperature + 2.0;   
			double leafAbsorption = 0.8;	//double leafTransmittance = 0.1;
			//int numberOfLayers = 3; 
			//double area = 1.0;
			//double absorbedRadiation = new double[numberOfLayers]; //double transmittedRadiation[] = new double[numberOfLayers];
							
			 
			double shortWaveRadiation = inShortWaveRadiation.get(basinId)[0];
			if (shortWaveRadiation == nullValue) {shortWaveRadiation = defaultShortWaveRadiation;}   
			
			double absorbedRadiation = shortWaveRadiation * leafAbsorption; //double transmittedRadiation[] = new double[numberOfLayers];

			
			double longWaveRadiation = inLongWaveRadiation.get(basinId)[0];
			if (longWaveRadiation == nullValue) {longWaveRadiation = 1 * stefanBoltzmannConstant * pow (airTemperature, 4);}//defaultLongWaveRadiation;}
			
			double windVelocity = inWindVelocity.get(basinId)[0];
			if (windVelocity == nullValue) {windVelocity = defaultWindVelocity;}   
			
			double atmosphericPressure = inAtmosphericPressure.get(basinId)[0];
			if (atmosphericPressure == nullValue) {atmosphericPressure = defaultAtmosphericPressure;}	

			double leafAreaIndex = inLeafAreaIndex.get(basinId)[0];
			if (leafAreaIndex == nullValue) {leafAreaIndex = defaultLeafAreaIndex;}	
			
			double saturationVaporPressure = computeSaturationVaporPressure(airTemperature);
			double vaporPressure = relativeHumidity * saturationVaporPressure/100.0;
			
			double delta = computeDelta (airTemperature);
			SensibleHeatTransferCoefficient cH = new SensibleHeatTransferCoefficient();
			LatentHeatTransferCoefficient cE = new LatentHeatTransferCoefficient();
			
			double convectiveTransferCoefficient = cH.computeConvectiveTransferCoefficient(airTemperature, windVelocity, leafLength);
			double sensibleHeatTransferCoefficient = cH.computeSensibleHeatTransferCoefficient(convectiveTransferCoefficient, leafSide);
			double latentHeatTransferCoefficient = cE.computeLatentHeatTransferCoefficient(airTemperature, atmosphericPressure, leafSide, convectiveTransferCoefficient);
			
			shortWaveRadiation = absorbedRadiation;
			double residual = 1.0;
			double latentHeatFlux = 0;
			double sensibleHeatFlux = 0;
			double netLongWaveRadiation = 0;
			double leafTemperatureSun = leafTemperature;
			double ETsun = 0;
			double ETshadow = 0;
			while(abs(residual) > pow(10,-1)) 
				{
				//deltaLeaf = computeDeltaLeaf(leafTemperatureSun, airTemperature);
				sensibleHeatFlux = computeSensibleHeatFlux(sensibleHeatTransferCoefficient, leafTemperatureSun, airTemperature);
				latentHeatFlux = computeLatentHeatFlux(delta, leafTemperatureSun, airTemperature, latentHeatTransferCoefficient, sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure);
				netLongWaveRadiation = computeNetLongWaveRadiation(leafSide,leafEmissivity, airTemperature, leafTemperatureSun);
				residual = (shortWaveRadiation - netLongWaveRadiation) - sensibleHeatFlux - latentHeatFlux;
				leafTemperatureSun = computeLeafTemperature(leafSide, leafEmissivity,sensibleHeatTransferCoefficient,latentHeatTransferCoefficient,airTemperature,shortWaveRadiation,longWaveRadiation,vaporPressure, saturationVaporPressure,delta);
				}
			ETsun = computeLatentHeatFlux(delta, leafTemperatureSun, airTemperature, latentHeatTransferCoefficient, sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure);
			outSOLT.put(basinId, new double[]{leafTemperatureSun});

			if (leafAreaIndex >1.0) {
				
				
			shortWaveRadiation = absorbedRadiation*0.2;
			double residualSh = 1.0;
			double latentHeatFluxSh = 0;
			double sensibleHeatFluxSh = 0;
			double netLongWaveRadiationSh = 0;
			double leafTemperatureSh = leafTemperature;
			
			while(abs(residualSh) > pow(10,-1)) 
				{
				sensibleHeatFluxSh = computeSensibleHeatFlux(sensibleHeatTransferCoefficient, leafTemperatureSh, airTemperature);
				latentHeatFluxSh = computeLatentHeatFlux(delta, leafTemperatureSh, airTemperature, latentHeatTransferCoefficient, sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure);
				netLongWaveRadiationSh = computeNetLongWaveRadiation(leafSide,leafEmissivity, airTemperature, leafTemperatureSh);
				residualSh = (shortWaveRadiation- netLongWaveRadiationSh) - sensibleHeatFluxSh - latentHeatFluxSh;
				leafTemperatureSh = computeLeafTemperature(leafSide, leafEmissivity,sensibleHeatTransferCoefficient,latentHeatTransferCoefficient,airTemperature,shortWaveRadiation,longWaveRadiation,vaporPressure, saturationVaporPressure,delta);
				}
			ETshadow = computeLatentHeatFlux(delta, leafTemperatureSh, airTemperature, latentHeatTransferCoefficient, sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure);
			}
			//double ETout = (ETsun*area + ETshadow*(leafAreaIndex-area))*time/latentHeatEvaporation;
			//System.out.print(ETout);
			outSOEt.put(basinId, new double[]{(((2.0*ETsun) + (ETshadow*(leafAreaIndex-2.0*area)))*time/latentHeatEvaporation)});
			}
			}
		//}
	private double computeSaturationVaporPressure(double airTemperature) {
		 // Computation of the saturation vapor pressure at air temperature [Pa]
		double saturationVaporPressure = 611.0 * exp((waterMolarMass*latentHeatEvaporation/molarGasConstant)*((1.0/273.0)-(1.0/airTemperature)));
		return saturationVaporPressure;
	}
	private double computeDelta (double airTemperature) {
		// Computation of delta [Pa K-1]
		// Slope of saturation vapor pressure at air temperature
		double numerator = 611 * waterMolarMass * latentHeatEvaporation;
		double exponential = exp((waterMolarMass * latentHeatEvaporation / molarGasConstant)*((1/273.0)-(1/airTemperature)));
		double denominator = (molarGasConstant * pow(airTemperature,2));
		double delta = numerator * exponential / denominator;
		return delta;
	}
	//private double computeDeltaLeaf (double airTemperature,double leafTemperature) {
		// Computation of delta [Pa K-1]
		// Slope of saturation vapor pressure at air temperature
	//	double first = 611 * exp((waterMolarMass * latentHeatEvaporation / molarGasConstant)*((1/273.0)-(1/leafTemperature)));
	//	double second = 611 * exp((waterMolarMass * latentHeatEvaporation / molarGasConstant)*((1/273.0)-(1/airTemperature)));
	//	double deltaLeaf = (first - second)/(leafTemperature - airTemperature);
	//	return deltaLeaf;
	//}
//	private double computeLongWaveRadiation(double side, double emissivity, double Temperature) {
//		double longWaveRadiation = 4 * side * emissivity * stefanBoltzmannConstant * (pow (Temperature, 4));
//		return longWaveRadiation;	
//	}
	private double computeNetLongWaveRadiation(double leafSide, double leafEmissivity, double airTemperature, double leafTemperature) {
		 // Compute the net long wave radiation i.e. the incoming minus outgoing [J m-2 s-1]
		double longWaveRadiation = 4 * leafSide * leafEmissivity * stefanBoltzmannConstant * (((pow (airTemperature, 3))*leafTemperature - (pow (airTemperature, 4))));
		return longWaveRadiation;	
	}
	private double computeLatentHeatFlux(double delta, double leafTemperature, double airTemperature, double latentHeatTransferCoefficient,double sensibleHeatTransferCoefficient, double vaporPressure, double saturationVaporPressure) {
		 // Computation of the latent heat flux from leaf [J m-2 s-1]
		double latentHeatFlux = (sensibleHeatTransferCoefficient* (delta * (leafTemperature - airTemperature) + saturationVaporPressure - vaporPressure))/(sensibleHeatTransferCoefficient/latentHeatTransferCoefficient);
		return latentHeatFlux;	
	}
	private double computeSensibleHeatFlux(double sensibleHeatTransferCoefficient, double leafTemperature, double airTemperature) {
		 // Computation of the sensible heat flux from leaf [J m-2 s-1]
		double sensibleHeatFlux = sensibleHeatTransferCoefficient * (leafTemperature - airTemperature);
		return sensibleHeatFlux;	
	}
	private double computeLeafTemperature(
			double side,
			double emissivity,
			double sensibleHeatTransferCoefficient,
			double latentHeatTransferCoefficient, 
			double airTemperature, 
			double shortWaveRadiation,
			double longWaveRadiation,
			double vaporPressure,
			double saturationVaporPressure,
			double delta) {
		double leafTemperature = (shortWaveRadiation + sensibleHeatTransferCoefficient*airTemperature +
				latentHeatTransferCoefficient*(delta*airTemperature + vaporPressure - saturationVaporPressure) + 
				side * emissivity * stefanBoltzmannConstant * 4 * pow(airTemperature,4))*
				(1/(sensibleHeatTransferCoefficient + latentHeatTransferCoefficient * delta +	
				side * emissivity * stefanBoltzmannConstant * 4 * pow(airTemperature,3)));
		return leafTemperature;	
	}
	}

