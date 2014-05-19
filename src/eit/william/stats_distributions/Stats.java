package eit.william.stats_distributions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class Stats 
{
    public static double getMean(ArrayList<Double> data)
    {
        double sum = 0.0;
        for(double a : data)
            sum += a;
            return sum/data.size();
    }

    public static double getVariance(ArrayList<Double> data)
    {
        double mean = getMean(data);
        double temp = 0;
        for(double a :data)
            temp += (mean-a)*(mean-a);
            return temp/data.size();
    }

    public static double getStdDev(ArrayList<Double> data)
    {
        return Math.sqrt(getVariance(data));
    }

    public static double getMedian(ArrayList<Double> data) 
    {
           double[] b = new double[data.size()];
           System.arraycopy(data, 0, b, 0, b.length);
           Arrays.sort(b);

           if (data.size() % 2 == 0) 
           {
              return (b[(b.length / 2) - 1] + b[b.length / 2]) / 2.0;
           } 
           else 
           {
              return b[b.length / 2];
           }
    }
    
    public static int getUniqueCount(ArrayList<String> data){
    	HashSet<String> noDupSet = new HashSet<String>();
    	
    	for(String target: data)
    		noDupSet.add(target);
    	
    	return noDupSet.size();
    }
}