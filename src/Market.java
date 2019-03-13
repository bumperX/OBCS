/**
 * The class consists of multiple properties of the spot market including flows
 * and respective market prices.
 *
 * @author: D.X.
 * @version: 1.0
 * @since Jan 15th, 2019
 */

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


public class Market {
    private HashMap<String, List<Double>> od_prices;
    private List<String> ods;
    private List<List<Double>> price_ls;
    private HashMap<String, Double> recent_od_price;

    public Market() {
        this.price_ls = new ArrayList<List<Double>>();
        this.ods = new ArrayList<String>();
        this.od_prices = new HashMap<String, List<Double>>();
        this.recent_od_price = new HashMap<String, Double>();
    }

//    /**
//     * The method is used to initialize the market prices randomly with the Normal distribution
//     * @param mean mean for Normal distribution
//     * @param std  standard deviation for Normal distribution
//     * @param sample_size sample size of market price for one flow
//     * @return list of prices for one flow
//     */
//    private List<Double> setPrice(Double mean, Double std, Integer sample_size, Integer lb, Integer ub) {
//        List<Double> price = new ArrayList<Double>();
//        NormalDistribution generator = new NormalDistribution(mean, std);
//        for (int i = 0; i < sample_size; i++) {
//            Double sample = generator.sample();
//            while (sample < lb) sample = generator.sample();
//            while (sample > ub) sample = generator.sample();
//            price.add(sample);
////            System.out.println(price.sample());
//        }
//        return price;
//    }


//    /**
//     * The method is used to initialize the market prices randomly with the Gamma(Erlang) distribution
//     * @param shape shape of Erlang distribution - k
//     * @param scale scale of Erlang distribution - lambda
//     * @param sample_size size of sample price for each flow
//     * @param lb lower bound of sample price
//     * @param ub upper bound of sample price
//     * @return list of prices for one flow
//     */
//    private List<Double> setPrice(double shape, double scale, int sample_size, int lb, int ub) {
//        List<Double> price = new ArrayList<Double>();
//        GammaDistribution generator = new GammaDistribution(shape, scale);
//        for (int i = 0; i < sample_size; i++) {
//            Double sample = generator.sample();
//            while (sample < lb) sample = generator.sample();
//            while (sample > ub) sample = generator.sample();
//            price.add(sample);
//        }
//        return price;
//    }


    /**
     * The method is used to initialize the market prices randomly with the Uniform distribution
     * @param lower Lower bound of this distribution (inclusive)
     * @param upper Upper bound of this distribution (exclusive)
     * @param sample_size size of sample price for each flow
     * @param lb lower bound of sample price
     * @param ub upper bound of sample price
     * @return list of prices for one flow
     */
    private List<Double> setPrice(double lower, double upper, int sample_size, int lb, int ub) {
        List<Double> price = new ArrayList<Double>();
        UniformRealDistribution generator = new UniformRealDistribution(lower, upper);
        for (int i = 0; i < sample_size; i++) {
            Double sample = generator.sample();
            while (sample < lb) sample = generator.sample();
            while (sample > ub) sample = generator.sample();
            price.add(sample);
        }
        return price;
    }


//    /**
//     * The method randomly generates the pair of mean and std value for each flow.
//     * @param n the number of flows
//     * @param mean_ub high bound of mean value
//     * @param mean_lb low bound of mean value
//     * @param std_ub high bound of standard deviation
//     * @param std_lb low bound of standard deviation
//     * @return list of list, each containing the pair of randomly generated mean and std
//     */
//    private List<List<Double>> setAttrs(int n, double mean_ub, double mean_lb, double std_ub, double std_lb) {
//        List<List<Double>> attrs = new ArrayList<List<Double>>(n);
//        for (int i = 0; i < n; i++) {
//            Double mean = ThreadLocalRandom.current().nextDouble(mean_lb, mean_ub);
//            Double std = ThreadLocalRandom.current().nextDouble(std_lb, std_ub);
//            List<Double> pair = new ArrayList<Double>();
//            pair.add(mean);
//            pair.add(std);
//            attrs.add(pair);
//        }
//        return attrs;
//    }


//    /**
//     * The method randomly generates the pair of shape and scale for each flow
//     * @param n the number of flows
//     * @param shape_ub upper bound of shape - k
//     * @param shape_lb lower bound of shape - k
//     * @param scale_ub upper bound of scale - lambda
//     * @param scale_lb lower bound of scale - lambda
//     * @return list of list, each containing the pair of randomly generated shape and scale
//     */
//    private List<List<Double>> setAttrs(int n, double shape_ub, double shape_lb, double scale_ub, double scale_lb) {
//        List<List<Double>> attrs = new ArrayList<List<Double>>(n);
//        for (int i = 0; i < n; i++) {
//            double shape = ThreadLocalRandom.current().nextDouble(shape_lb, shape_ub);
//            double scale = ThreadLocalRandom.current().nextDouble(scale_lb, scale_ub);
//            List<Double> pair = new ArrayList<Double>();
//            pair.add(shape);
//            pair.add(scale);
//            attrs.add(pair);
//        }
//        return attrs;
//    }

    /**
     * The method randomly generates the pair of shape and scale for each flow
     * @param n the number of flows
     * @param upper_ub upper bound of upper
     * @param upper_lb lower bound of upper
     * @param lower_ub upper bound of lower
     * @param lower_lb lower bound of lower
     * @return list of list, each containing the pair of randomly generated upper and lower
     */
    private List<List<Double>> setAttrs(int n, double upper_ub, double upper_lb, double lower_ub, double lower_lb) {
        List<List<Double>> attrs = new ArrayList<List<Double>>(n);
        for (int i = 0; i < n; i++) {
            double upper = ThreadLocalRandom.current().nextDouble(upper_lb, upper_ub);
            double lower = ThreadLocalRandom.current().nextDouble(lower_lb, lower_ub);
            List<Double> pair = new ArrayList<Double>();
            pair.add(lower);
            pair.add(upper);
            attrs.add(pair);
        }
        return attrs;
    }

//    /**
//     * The method generates random price samples for each flow
//     * @param sample_size sample size of price for each flow
//     * @param mean_ub high bound of mean value
//     * @param mean_lb low bound of mean value
//     * @param std_ub high bound of standard deviation
//     * @param std_lb low bound of standard deviation
//     * @return list of list, each containing several sample prices
//     */
//    private List<List<Double>> setPrices(
//            int sample_size, double mean_ub, double mean_lb, double std_ub, double std_lb, Integer lb, Integer ub){
//        List<List<Double>> prices = new ArrayList<List<Double>>();
//        List<List<Double>> ods_attrs = setAttrs(ods.size(), mean_ub, mean_lb, std_ub, std_lb);
//        for (List<Double> attr_pair : ods_attrs) {
//            List<Double> prices_sample = setPrice(attr_pair.get(0), attr_pair.get(1), sample_size, lb, ub);
//            prices.add(prices_sample);
//        }
//        return prices;
//    }

//    /**
//     * The method generates random price samples for each flow
//     * @param sample_size size of sample price for each flow
//     * @param shape_ub upper bound of shape - k
//     * @param shape_lb lower bound of shape - k
//     * @param scale_ub upper bound of scale - lambda
//     * @param scale_lb lower bound of scale - lambda
//     * @param lb lower bound of sample price
//     * @param ub upper bound of sample price
//     * @return list of list, each containing sample_size sample prices
//     */
//    private List<List<Double>> setPrices(
//            int sample_size, double shape_ub, double shape_lb, double scale_ub, double scale_lb, int lb, int ub){
//        List<List<Double>> prices = new ArrayList<List<Double>>();
//        List<List<Double>> ods_attrs = setAttrs(ods.size(), shape_ub, shape_lb, scale_ub, scale_lb);
//        for (List<Double> attr_pair : ods_attrs) {
//            List<Double> prices_sample = setPrice(attr_pair.get(0), attr_pair.get(1), sample_size, lb, ub);
//            prices.add(prices_sample);
//        }
//        return prices;
//    }

    /**
     * The method generates random price samples for each flow
     * @param sample_size size of sample price for each flow
     * @param upper_ub upper bound of upper
     * @param upper_lb lower bound of upper
     * @param lower_ub upper bound of lower
     * @param lower_lb lower bound of lower
     * @param lb lower bound of sample price
     * @param ub upper bound of sample price
     * @return list of list, each containing sample_size sample prices
     */
    private List<List<Double>> setPrices(
            int sample_size, double upper_ub, double upper_lb, double lower_ub, double lower_lb, int lb, int ub){
        List<List<Double>> prices = new ArrayList<List<Double>>();
        List<List<Double>> ods_attrs = setAttrs(ods.size(), upper_ub, upper_lb, lower_ub, lower_lb);
        for (List<Double> attr_pair : ods_attrs) {
            List<Double> prices_sample = setPrice(attr_pair.get(0), attr_pair.get(1), sample_size, lb, ub);
            prices.add(prices_sample);
        }
        return prices;
    }


    /**
     * The method retrieves flow records from csv file
     * @param csvFile csv file path
     */
    private void setOds(String csvFile) {
        String line;
        try {
            // Create an instance of BufferReader
            BufferedReader  br = new BufferedReader(new FileReader(csvFile));
            // loop until all lines are read
            while ((line = br.readLine()) != null) {
                if (!line.equals("od")) {
                    this.ods.add(line);
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

//    /**
//     * The method generate a key value pair of flows and market price samples
//     * @param sample_size sample numbers of each flow
//     * @param csvFile csv file path containing flow records
//     * @param mean_ub high bound of mean value
//     * @param mean_lb low bound of mean value
//     * @param std_ub high bound of standard deviation
//     * @param std_lb low bound of standard deviation
//     */
//    private void setOdPrice(
//            Integer sample_size, String csvFile, double mean_ub, double mean_lb,
//            double std_ub, double std_lb, Integer lb, Integer ub){
//        setOds(csvFile);
//        this.price_ls = setPrices(sample_size, mean_ub, mean_lb, std_ub, std_lb, lb, ub);
//
//        Iterator<String> od_iter = ods.iterator();
//        Iterator<List<Double>> price_iter = this.price_ls.iterator();
//
//        while (od_iter.hasNext() && price_iter.hasNext()) {
//            List<Double> prices = price_iter.next();
//            String od = od_iter.next();
//            od_prices.put(od, prices);
//            recent_od_price.put(od, prices.get(prices.size()-1));
//        }
//    }


//    /**
//     * The method generate a key value pair of flows and market price samples
//     * @param sample_size size of sample price for each flow
//     * @param csvFile csv file path containing flow records
//     * @param shape_ub upper bound of shape - k
//     * @param shape_lb lower bound of shape - k
//     * @param scale_ub upper bound of scale - lambda
//     * @param scale_lb lower bound of scale - lambda
//     * @param lb lower bound of sample price
//     * @param ub upper bound of sample price
//     */
//    private void setOdPrice(
//            int sample_size, String csvFile, double shape_ub, double shape_lb,
//            double scale_ub, double scale_lb, Integer lb, Integer ub){
//        setOds(csvFile);
//        this.price_ls = setPrices(sample_size, shape_ub, shape_lb, scale_ub, scale_lb, lb, ub);
//
//        Iterator<String> od_iter = ods.iterator();
//        Iterator<List<Double>> price_iter = this.price_ls.iterator();
//
//        while (od_iter.hasNext() && price_iter.hasNext()) {
//            List<Double> prices = price_iter.next();
//            String od = od_iter.next();
//            od_prices.put(od, prices);
//            recent_od_price.put(od, prices.get(prices.size()-1));
//        }
//    }


    /**
     * The method generate a key value pair of flows and market price samples
     * @param sample_size size of sample price for each flow
     * @param csvFile file path containing flow records
     * @param upper_ub upper bound of upper
     * @param upper_lb lower bound of upper
     * @param lower_ub upper bound of lower
     * @param lower_lb lower bound of lower
     * @param lb lower bound of sample price
     * @param ub upper bound of sample price
     */
    private void setOdPrice(
            int sample_size, String csvFile, double upper_ub, double upper_lb,
            double lower_ub, double lower_lb, Integer lb, Integer ub){
        setOds(csvFile);
        this.price_ls = setPrices(sample_size, upper_ub, upper_lb, lower_ub, lower_lb, lb, ub);

        Iterator<String> od_iter = ods.iterator();
        Iterator<List<Double>> price_iter = this.price_ls.iterator();

        while (od_iter.hasNext() && price_iter.hasNext()) {
            List<Double> prices = price_iter.next();
            String od = od_iter.next();
            od_prices.put(od, prices);
            recent_od_price.put(od, prices.get(prices.size()-1));
        }
    }


    /** Get sample prices of a specific flow */
    public List<Double> getPrices(String od) {
        return this.od_prices.get(od);
    }

    /** Get most recent price of a specific flow */
    public HashMap<String, Double> getRecentPrice() {
        return recent_od_price;
    }

    /** Get flow records */
    public List<String> getOds() {
        return this.ods;
    }

    /** Get flows and their respective sample prices */
    public HashMap<String, List<Double>>getOdPrice() {
        return this.od_prices;
    }



    public static void main(String[] args) {
        String filePath = "G:\\Python\\SF\\special_sales\\large_vol_list.csv";

        /** Initialize Market price and flows */
        Market mkt = new Market();
//        mkt.setOdPrice(30, filePath, 30, 15,3, 1, 10, 40);
//        mkt.setOdPrice(30, filePath, 20, 9,1.5, 1, 10, 40);
        mkt.setOdPrice(30, filePath, 40, 30,20, 10, 10, 40);
        List<String> ods = mkt.getOds();
        HashMap<String, List<Double>> odsPrice = mkt.getOdPrice();
        HashMap<String, Double> recent_od_price = mkt.getRecentPrice();


        /** Initialize the Buyer and get the contract level (Q) of the Buyer */
        Buyer buyer = new Buyer();
        buyer.initializeContractLevel(ods);
        HashMap<String, Double> contractLevel = buyer.getContractLevel();

        /** Initialize the Seller and get the execution and reservation fee of the Seller */
        Seller seller = new Seller();
        seller.setExecuFee(ods, 25, 10, 3, 1, 5, 30);
        HashMap<String, Double> execuFee = seller.getExecuFee();
        seller.setReservFee(ods, 3, 2, 2, 1, 1, 5);
        HashMap<String, Double> reservFee = seller.getReservFee();
//        seller.setCapacity(ods, 800, 400, 10, 5, 200, 1000);
        seller.setCapacity(ods, 300, 150, 10, 5, 100, 500);
        HashMap<String, Double> capacity = seller.getCapacity();

        /**  Calculate demand from the Seller and market with the most recent price */
        buyer.setDemandFromSeller(contractLevel, execuFee, recent_od_price);
        HashMap<String, Double> demandFromSeller = buyer.getDemandFromSeller();
//        System.out.println(demandFromSeller);
        buyer.setDemandFromMkt(contractLevel, execuFee, recent_od_price);
        HashMap<String, Double> demandFromMkt = buyer.getDemandFromMarket();
//        System.out.println(demandFromMkt);


        /** Optimize Buyer's contract level */
        System.out.println("Initial Q: " + buyer.getContractLevel());
        buyer.setOptimalContractLevel(odsPrice, reservFee, execuFee, capacity, 4.0);
        System.out.println("Updated Q: " + buyer.getContractLevel());

        /** Optimize Seller's reservation fee */
        System.out.println("Initial s: " + seller.getReservFee());
        contractLevel = buyer.getContractLevel();
        HashMap<String, Double> marginalCost = seller.getMarginalCost();
        seller.setOptimalReserFee(
                odsPrice, reservFee, execuFee, capacity, contractLevel, marginalCost, 0.25, 0.65
        );
        reservFee = seller.getReservFee();
        System.out.println("Updated s: " + reservFee);

//        /** Print out the optimal non-zero contract level and reservation fee */
//        for (Map.Entry<String, Double> entry: buyer.getContractLevel().entrySet()){
//            if (entry.getValue() > 0) {
//                String od = entry.getKey();
//                System.out.println(entry);
//                System.out.println(seller.getReservFee().get(od));
//            }
//        }

        System.out.println("Initial K: " + capacity);
        seller.setOptimalCapacity(odsPrice, reservFee, execuFee);
        capacity = seller.getCapacity();
        System.out.println("Optimal K: " + capacity);
    }

}