/**
 * The class consists of Seller's properties and its optimal decision
 * corresponding to the spot market status and the Buyer's activity.
 *
 * @author: D.X.
 * @version: 1.0
 * @since Jan 18th, 2019
 */

import org.apache.commons.math3.distribution.NormalDistribution;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Seller {
    private HashMap<String, Double> marginCost;
    private HashMap<String, Double> reservFee;
    private HashMap<String, Double> execuFee;
    private HashMap<String, Double> capacity;


    public Seller() {
        this.marginCost = new HashMap<String, Double>();
        this.reservFee = new HashMap<String, Double>();
        this.execuFee = new HashMap<String, Double>();
        this.capacity = new HashMap<String, Double>();
    }

    /**
     * The method randomly generates marginal cost per unit for each flow - b
     * @param ods flow records
     * @param mean_hb high bound of mean value
     * @param mean_lb low bound of mean value
     * @param std_hb high bound of standard deviation
     * @param std_lb low bound of standard deviation
     * @param lb low bound of the reservation fee
     * @param hb high bound of the reservation fee
     * @return HashMap of marginal cost w.r.t flows
     */
    private HashMap<String, Double> getMarginCost(
            List<String> ods, double mean_hb, double mean_lb, double std_hb, double std_lb, double lb, double hb) {
        for (int i = 0; i < ods.size(); i++) {
            Double mean = ThreadLocalRandom.current().nextDouble(mean_lb, mean_hb);
            Double std = ThreadLocalRandom.current().nextDouble(std_lb, std_hb);
            NormalDistribution generator = new NormalDistribution(mean, std);
            double temp = generator.sample();
            while (temp < lb) temp = generator.sample();
            while (temp > hb) temp = generator.sample();
            marginCost.put(ods.get(i), temp);
        }
        return marginCost;
    }

    /**
     * The method sets the optimal execution Fee which equals the marginal cost - g
     * @params the same as the params of method 'getMarginCost'
     */
    public void setExecuFee(
            List<String> ods, double mean_hb, double mean_lb, double std_hb, double std_lb, double lb, double hb) {
        execuFee = getMarginCost(ods, mean_hb, mean_lb, std_hb, std_lb, lb, hb);
    }


    /**
     * Initialize the reservation fee purchased by the Buyer
     * @param ods the list of flow records
     * @param mean_hb high bound of mean value
     * @param mean_lb low bound of mean value
     * @param std_hb high bound of standard deviation
     * @param std_lb low bound of standard deviation
     * @param lb low bound of the reservation fee
     * @param hb high bound of the reservation fee
     */
    public void setReservFee(
            List<String> ods, double mean_hb, double mean_lb, double std_hb, double std_lb, double lb, double hb) {
        for (int i = 0; i < ods.size(); i++) {
            Double mean = ThreadLocalRandom.current().nextDouble(mean_lb, mean_hb);
            Double std = ThreadLocalRandom.current().nextDouble(std_lb, std_hb);
            NormalDistribution generator = new NormalDistribution(mean, std);
            double temp = generator.sample();
            while (temp < lb) temp = generator.sample();
            while (temp > hb) temp = generator.sample();
            reservFee.put(ods.get(i), temp);
        }
    }

    /**
     * Initialize the capacity of the Seller
     * @param ods the list of flow records
     * @param mean_hb high bound of mean value
     * @param mean_lb low bound of mean value
     * @param std_hb high bound of standard deviation
     * @param std_lb low bound of standard deviation
     * @param lb low bound of the reservation fee
     * @param hb high bound of the reservation fee
     */
    public void setCapacity(
            List<String> ods, double mean_hb, double mean_lb, double std_hb, double std_lb, double lb, double hb) {
        for (int i = 0; i < ods.size(); i++) {
            Double mean = ThreadLocalRandom.current().nextDouble(mean_lb, mean_hb);
            Double std = ThreadLocalRandom.current().nextDouble(std_lb, std_hb);
            NormalDistribution generator = new NormalDistribution(mean, std);
            double temp = generator.sample();
            while (temp < lb) temp = generator.sample();
            while (temp > hb) temp = generator.sample();
            capacity.put(ods.get(i), temp);
        }
    }

    /** Calculate the expected value given price distribution - G(a) */
    private Double calculateG(List<Double> prices, Double a) {
        double res = 0.0;
        double prob = 1.0/prices.size();
        for (double price : prices) {
            res += Math.min(price, a) * prob;
        }
        return res;
    }

    /** Calculate derivative of willingness-to-pay value given demand */
    private Double getUPrime(Double demand) {
//        return 2000.0/3 *Math.pow( demand, -2.0/3);
        return 100.0*Math.exp(-0.01*demand);
    }

    /** Calculate sigma used to calculate Q'(s) */
    private double calculateSigma(double P_u, double P_d) {
        return (P_u - P_d)/(2*Math.sqrt(3));
    }

    /** Calculate the derivative of Demand function */
    private double calculateDPrime(double x) {
        return -100.0/x;
    }

    /** Calculate the second derivative of Demand function */
    private double calculateDDoublePrime(double x) {
        return 100.0/Math.pow(x, 2);
    }

    /** Calculate the partial derivative of Q w.r.t. s - gamma=0.01 */
    private double calculateQPrime(double gamma, double P_u, double P_d, double g, double s) {
        double sigma = calculateSigma(P_u, P_d);
        return -2.0*Math.sqrt(3)*sigma/(gamma*Math.pow(Math.pow(P_u-g, 2)-4*Math.sqrt(3)*s*sigma, -3.0/2));
    }

    /** Calculate the buyer's elasticity */
    private double calculateElasticity(double gamma, double s, double g, double Q, double P_u, double P_d) {
        return s*calculateQPrime(gamma, P_u, P_d, g, s)/Q;
    }

    /**
     * This method optimizes the reservation fee for the seller
     * @param mktPrices key value pairs of flows and market price samples - Ps
     * @param reservPrices key value pairs of flows and reservation price - s
     * @param execuPrices key value pairs of flows and execution price - g
     * @param capacity key value pairs of flows and capacity - K
     * @param contractLevel key value pairs of flows and contract level - Q
     * @param marginCost key value pairs of flows and marginal cost of Seller - b
     * @param m_lb key value pairs of flows and the lower bound of probability to enter market
     * @param m_ub key value pairs of flows and the upper bound of probability to enter market
     */
    public void setOptimalReserFee(
            HashMap<String, List<Double>> mktPrices,
            HashMap<String, Double> reservPrices,
            HashMap<String, Double> execuPrices,
            HashMap<String, Double> capacity,
            HashMap<String, Double> contractLevel,
            HashMap<String, Double> marginCost,
            double m_lb, double m_ub) {
        HashMap<String, Double> reserv_opt = new HashMap<String, Double>();
        for (Map.Entry<String, List<Double>> entry : mktPrices.entrySet()) {
            double s_opt;
            String od = entry.getKey();
            List<Double> prices = entry.getValue();
            double P_u = Collections.max(prices);
            double P_d = Collections.min(prices);
            double mu = 1.0*prices.stream().mapToDouble(Double::doubleValue).sum()/prices.size();
            double m = ThreadLocalRandom.current().nextDouble(m_lb, m_ub);
            double s = reservPrices.get(od);
            double g = execuPrices.get(od);
            double Q = contractLevel.get(od);
            double K = capacity.get(od);
            double b = marginCost.get(od);

            if (Q*calculateDDoublePrime(Q) + 2*calculateDPrime(Q) <= 0) {
                if (Q < K) {
                    s_opt = m*(mu - calculateG(prices, b))/(1-1.0/calculateElasticity(0.01, s, g, Q, P_u, P_d));
                } else {
                    s_opt = calculateG(prices, getUPrime(K)) - calculateG(prices, b);
                }
            } else {
                s_opt = 0.0;
            }
            reserv_opt.put(od, s_opt);
        }
        this.reservFee = reserv_opt;
    }

    /** Get Q with inputs s and g */
    private double calculateQ(List<Double>prices, double s, double g){
        double P_u = Collections.max(prices);
        double P_d = Collections.min(prices);
        double sigma = (P_u - P_d)/(2*Math.sqrt(3));
        return 100 * Math.log(100.0/(P_u - Math.sqrt(Math.pow(P_u-g, 2)-4*Math.sqrt(3)*s*sigma)));
    }

    /**
     * This method optimizes the capacity of the Seller with optimal s and g
     * @param mktPrices key value pairs of flows and market price samples - Ps
     * @param reservFee key value pairs of flows and reservation price - s
     * @param execuFee key value pairs of flows and execution price - g
     */
    public void setOptimalCapacity(
            HashMap<String, List<Double>> mktPrices,
            HashMap<String, Double> reservFee,
            HashMap<String, Double> execuFee) {
        for (Map.Entry<String, List<Double>> entry : mktPrices.entrySet()) {
            String od = entry.getKey();
            List<Double> prices = entry.getValue();
            double s = reservFee.get(od);
            double g  = execuFee.get(od);
            double K = calculateQ(prices, s, g);
            this.capacity.put(od, K);
        }
    }

    /** Return the reservation fee s */
    public HashMap<String, Double> getReservFee() {
        return this.reservFee;
    }

    /* Return the execution fee g */
    public HashMap<String, Double> getExecuFee() {
        return this.execuFee;
    }

    /* Return the capacity K */
    public HashMap<String, Double> getCapacity() {
        return this.capacity;
    }

    /* Return the marginal cost b */
    public HashMap<String, Double> getMarginalCost() {
        return this.marginCost;
    }
}
