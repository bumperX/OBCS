/**
 * The class consists of Buyer's properties and its optimal decision
 * corresponding to the spot market status and the Seller's activity.
 *
 * @author: D.X.
 * @version: 1.0
 * @since Jan 18th, 2019
 */

import java.util.*;
import java.lang.Math;
import java.util.concurrent.ThreadLocalRandom;


public class Buyer {
    private HashMap<String, Double> contractLevel;
    private HashMap<String, Double> demandFromSeller;
    private HashMap<String, Double> demandFromMkt;


    public Buyer() {
        this.contractLevel = new HashMap<String, Double>();
        this.demandFromSeller = new HashMap<String, Double>();
        this.demandFromMkt = new HashMap<String, Double>();
    }


    /**
     * The method initializes the contract level(Q) of all flows
     * @param ods the list of flow records
     */
    public void initializeContractLevel(List<String> ods) {
        for (int i = 0; i < ods.size(); i++) {
            Double temp_Q = ThreadLocalRandom.current().nextDouble(50, 500);
            this.contractLevel.put(ods.get(i), temp_Q);
        }
    }

    /**
     * The method calculates the quantity purchased from the seller - q
     * @param contractLevel Q - the contract level
     * @param execuFee g - the execution fee per unit
     * @param mktPrice Ps - the spot market price
     */
    public void setDemandFromSeller(
            HashMap<String, Double> contractLevel,
            HashMap<String, Double> execuFee,
            HashMap<String, Double> mktPrice
    ){
        for (Map.Entry<String, Double> entry: mktPrice.entrySet()) {
            Double demandContr = 0.0;
            String od = entry.getKey();
            Double price = entry.getValue();
            if (execuFee.containsKey(od)) {
                Double execCost = execuFee.get(od);
                Double contrAmt = contractLevel.get(od);
                if (price > execCost) {
                    demandContr = contrAmt;
                }
            }
            demandFromSeller.put(od, demandContr);
        }
    }


    /** Calculate derivative of willingness-to-pay value given demand */
    private Double getWTPPrime(Double demand) {
//        return 2000.0/3 *Math.pow( demand, -2.0/3);
        return 100.0*Math.exp(-0.01*demand);
    }

    /** Calculate demand value given price */
    private Double getDemand(Double price) {
//        return Math.pow(3.0*price/2000, -3.0/2);
        return (-100)*Math.log(price/100.0);
    }

    /**
     * The method calculates the quantity purchased from the Market - x
     * @param contractLevel Q - the contract level
     * @param execuFee g - the execution fee per unit
     * @param mktPrice Ps - the spot market price
     */
    public void setDemandFromMkt(
            HashMap<String, Double> contractLevel,
            HashMap<String, Double> execuFee,
            HashMap<String, Double> mktPrice
    ) {
        for (Map.Entry<String, Double> entry: mktPrice.entrySet()) {
            double demandMkt = 0.0;
            var od = entry.getKey();
            double price = entry.getValue();
            if (execuFee.containsKey(od)) {
                double execCost = execuFee.get(od);
                double contrAmt = contractLevel.get(od);
                double wtfPrime = getWTPPrime(contrAmt);

                if (price < execCost) {
                    demandMkt = getDemand(price);
                } else if (execCost <= price && price < wtfPrime) {
                    demandMkt = getDemand(price) - contrAmt;
                } else if (price >= wtfPrime) {
                    demandMkt = 0.0;
                }
            }
            demandFromMkt.put(od, demandMkt);
        }
    }

    /** Calculate the expected value given price distribution - G(a) */
    private Double getExpectedValue(List<Double> prices, Double a) {
        double res = 0.0;
        double prob = 1.0/prices.size();
        for (double price : prices) {
            res += Math.min(price, a) * prob;
        }
        return res;
    }

    /** Return result of the right part */
    private Double getRightResult(List<Double> prices, Double s, Double g) {
        return s + getExpectedValue(prices, g);
    }


    /**
     * The helper method helps calculate the optimal derivative of WTP - U'
     * @param prices sample prices of one od flow
     * @param right the right result of the equation to get optimal Q
     * @param lb lower bound of U_prime
     * @param ub upper bound of U_prime
     * @param threshold the stop condition
     * @return the optimal U_prime which is the output of optimal Q
     */
    private Double calculateUPrimeHelper(
            List<Double> prices, Double right, Double lb, Double ub, Double threshold){
//        double right = getRightResult(prices, s, g);
        double left_lb = getExpectedValue(prices, lb);
        double left_ub = getExpectedValue(prices, ub);

        if (right < left_lb || right > left_ub) return -1.0;
        double temp = getExpectedValue(prices, (lb + ub) / 2.0);

        if (Math.abs(left_ub - left_lb) < threshold) {
          return left_ub;
        } else {
            if (right >= left_lb && right < temp) {
                left_ub = temp;
//                calculateUPrimeHelper(prices, s, g, lb, (lb + ub) / 2.0, threshold);
                calculateUPrimeHelper(prices, right, lb, (lb + ub) / 2.0, threshold);
            } else if (right >= temp && right <= left_ub) {
                left_lb = temp;
//                calculateUPrimeHelper(prices, s, g, (lb + ub) / 2.0, ub, threshold);
                calculateUPrimeHelper(prices, right, (lb + ub) / 2.0, ub, threshold);
            }
        }
        return -1.0;
    }


    /**
     * The method calculates the optimal WTP_prime U' which results to the optimal contract level Q
     * @param prices sample prices of one od flow
     * @param s the reservation fee per unit
     * @param g the execution fee per unit
     * @param lb the lower bound of U'
     * @param ub the upper bound of U'
     * @param threshold the stop condition
     * @return the optimal WTP_prime U'
     */
    private Double calculateUPrime(
            List<Double> prices, Double s, Double g, Double lb, Double ub, Double threshold) {
        double right = getRightResult(prices, s, g);
        return calculateUPrimeHelper(prices, right, lb, ub, threshold);
    }


    /**
     * The method calculate the optimal contract level Q for the Buyer
     * @param mktPrices key value pairs of flows and market price samples - Ps
     * @param reservPrices key value pairs of flows and reservation price - s
     * @param execuPrices key value pairs of flows and execution price - g
     * @param capacity key value pairs of flows and capacity - K
     * @param threshold the stop condition
     */
    public void setOptimalContractLevel(
            HashMap<String, List<Double>> mktPrices,
            HashMap<String, Double> reservPrices,
            HashMap<String, Double> execuPrices,
            HashMap<String, Double> capacity,
            Double threshold
    ) {
        contractLevel = new HashMap<String, Double>();
        for (Map.Entry<String, List<Double>> entry : mktPrices.entrySet()) {
            String od = entry.getKey();
            List<Double> prices = entry.getValue();

            double optimalQ;
            double s = reservPrices.get(od);
            double g = execuPrices.get(od);
            double K = capacity.get(od);

            double P_u = Collections.max(prices);
            double P_d = Collections.min(prices);
            double mu = 1.0*prices.stream().mapToDouble(Double::doubleValue).sum()/prices.size();
            if ((g >= Math.min(100, P_u-Math.sqrt(2*s*(P_u-P_d)))) || (s >= 100)) {
                this.contractLevel.put(od, 0.0);
            } else {
                double lb = getWTPPrime(K);
//            double ub = getWTPPrime(0.1);
                double ub = getWTPPrime(0.0);

                if (s + getExpectedValue(prices, g) > getExpectedValue(prices, getWTPPrime(0.0))) {
                    optimalQ = 0.0;
                } else {
//                double optimalUPrime = calculateUPrimeHelper(prices, s, g, lb, ub, threshold);
                    double optimalUPrime = calculateUPrime(prices, s, g, lb, ub, threshold);
                    if (optimalUPrime == -1.0) {
                        optimalQ = 0.0;
                    } else {
                        optimalQ = getDemand(optimalUPrime);
                    }
                }
                this.contractLevel.put(od, optimalQ);
            }
        }
    }


    /** Get contract level Q */
    public HashMap<String, Double> getContractLevel() {
        return this.contractLevel;
    }

    /** Get quantity purchased from the Seller q */
    public HashMap<String, Double> getDemandFromSeller() {
        return this.demandFromSeller;
    }

    /** Get quantity purchased from the market x */
    public HashMap<String, Double> getDemandFromMarket() {
        return this.demandFromMkt;
    }

}
