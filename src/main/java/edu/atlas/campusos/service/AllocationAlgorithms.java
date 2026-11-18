package edu.atlas.campusos.service;
import java.util.*;

/** Hand-written randomized selection used for median analysis. */
public final class AllocationAlgorithms {
    private AllocationAlgorithms(){}
    public static <T> double median(List<T> values,java.util.function.ToDoubleFunction<T> value){if(values.isEmpty())return 0;double[] left=values.stream().mapToDouble(value).toArray();int n=left.length;if((n&1)==1)return select(left,n/2);double[] right=left.clone();return (select(left,n/2-1)+select(right,n/2))/2.0;}
    /** Randomized Quickselect: expected O(N), worst-case O(N^2). */
    private static double select(double[] a,int k){int lo=0,hi=a.length-1;Random rng=new Random();while(lo<hi){double pivot=a[lo+rng.nextInt(hi-lo+1)];int i=lo,j=hi;while(i<=j){while(a[i]<pivot)i++;while(a[j]>pivot)j--;if(i<=j){double t=a[i];a[i++]=a[j];a[j--]=t;}}if(k<=j)hi=j;else if(k>=i)lo=i;else return a[k];}return a[lo];}
}
