package etpClasses;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.lang.Math.PI;

public class LatentHeat {
	public double computeLatentHeatTransferCoefficient (double airTemperature, double atmosphericPressure, int leafSide,double convectiveTransferCoefficient,
		double airSpecificHeat, double airDensity, double molarGasConstant, double molarVolume, double waterMolarMass, double latentHeatEvaporation, 
		double poreDensity,	double poreArea, double poreDepth, double poreRadius) {
		
		double thermalDiffusivity = 1.32 * pow(10,-7) * airTemperature - 1.73 * pow(10,-5);
		double binaryDiffusionCoefficient = 1.49 * pow(10,-7) * airTemperature - 1.96 * pow(10,-5);
		double ratio = binaryDiffusionCoefficient/molarVolume;
		double lewisNumber = thermalDiffusivity/binaryDiffusionCoefficient;
		double throatResistance = poreDepth/(poreArea*ratio*poreDensity);
		double constantTerm= 1/(4*poreRadius) - 1/(PI * (1/sqrt(poreDensity)));
		double vapourResistance = constantTerm * 1/(ratio * poreDensity);
		double molarStomatalConductance = 1/(throatResistance + vapourResistance);
		double stomatalConductance = molarStomatalConductance * (molarGasConstant * airTemperature)/atmosphericPressure  ;
		double boundaryLayerConductance = leafSide*convectiveTransferCoefficient/(airSpecificHeat*airDensity*pow(lewisNumber,0.66));
		double totalConductance = 1/ ((1/stomatalConductance) + (1/boundaryLayerConductance));
		double molarTotalConductance = totalConductance*40;
		double latentHeatTransferCoefficient = waterMolarMass * latentHeatEvaporation * molarTotalConductance / atmosphericPressure;
		return latentHeatTransferCoefficient;	
		}
	public double computeLatentHeatFlux(double delta, double leafTemperature, double airTemperature, double latentHeatTransferCoefficient,double sensibleHeatTransferCoefficient, double vaporPressure, double saturationVaporPressure) {
		 // Computation of the latent heat flux from leaf [J m-2 s-1]
		double latentHeatFlux = (sensibleHeatTransferCoefficient* (delta * (leafTemperature - airTemperature) + saturationVaporPressure - vaporPressure))/(sensibleHeatTransferCoefficient/latentHeatTransferCoefficient);
		return latentHeatFlux;	
	}
	}