package edu.atlas.campusos.service;

import java.util.*;

/** Hand-written analysis algorithms; no external graph/flow library is used. */
public final class AllocationAlgorithms {
    private AllocationAlgorithms() {}
    public static <T> double median(List<T> input, java.util.function.ToDoubleFunction<T> value){if(input.isEmpty())return 0;double[] a=input.stream().mapToDouble(value).toArray();int n=a.length; if(n%2==1)return select(a,n/2);double[] b=a.clone();return (select(a,n/2-1)+select(b,n/2))/2.0;}
    /** Randomized Quickselect. Expected O(N); worst case O(N^2). */
    private static double select(double[] a,int k){int lo=0,hi=a.length-1;Random random=new Random();while(lo<hi){double pivot=a[lo+random.nextInt(hi-lo+1)];int i=lo,j=hi;while(i<=j){while(a[i]<pivot)i++;while(a[j]>pivot)j--;if(i<=j){double t=a[i];a[i++]=a[j];a[j--]=t;}}if(k<=j)hi=j;else if(k>=i)lo=i;else return a[k];}return a[lo];}

    public record FlowEstimate(int possible,int vertices,int edges,String modelNote) {}
    /** Edmonds–Karp over class -> (room, exact interval) nodes -> sink. Shared identical room slots have capacity one. */
    public static FlowEstimate flowEstimate(List<FlowClass> classes,List<FlowRoom> rooms){
        record Slot(String room,String start,String end){}; LinkedHashMap<Slot,Integer> slotIds=new LinkedHashMap<>();
        for(FlowClass c:classes) for(FlowRoom r:rooms) if(r.available&&r.capacity>=c.size)slotIds.computeIfAbsent(new Slot(r.name,c.start,c.end),x->slotIds.size());
        int source=0,classBase=1,slotBase=classBase+classes.size(),sink=slotBase+slotIds.size(),n=sink+1;int[][] cap=new int[n][n];List<List<Integer>> graph=new ArrayList<>();for(int i=0;i<n;i++)graph.add(new ArrayList<>());int edges=0;
        for(int i=0;i<classes.size();i++){addResidualEdge(graph,cap,source,classBase+i);edges++;FlowClass c=classes.get(i);for(FlowRoom r:rooms)if(r.available&&r.capacity>=c.size){int id=slotIds.get(new Slot(r.name,c.start,c.end));addResidualEdge(graph,cap,classBase+i,slotBase+id);edges++;}}
        for(int id=0;id<slotIds.size();id++){addResidualEdge(graph,cap,slotBase+id,sink);edges++;}
        int flow=0;int[] parent=new int[n];while(true){Arrays.fill(parent,-1);parent[source]=source;ArrayDeque<Integer> q=new ArrayDeque<>();q.add(source);while(!q.isEmpty()&&parent[sink]<0){int u=q.remove();for(int v:graph.get(u))if(parent[v]<0&&cap[u][v]>0){parent[v]=u;q.add(v);}}if(parent[sink]<0)break;for(int v=sink;v!=source;v=parent[v]){int u=parent[v];cap[u][v]--;cap[v][u]++;}flow++;}
        return new FlowEstimate(flow,n,edges,"Exact interval slots share one capacity per room. Partial overlaps across different interval boundaries are not encoded; this is an estimate, not a feasible-timetable guarantee. Edmonds–Karp O(VE²).");
    }
    private static void addResidualEdge(List<List<Integer>> graph,int[][] capacity,int from,int to){graph.get(from).add(to);graph.get(to).add(from);capacity[from][to]=1;}
    public record FlowClass(int size,String start,String end){} public record FlowRoom(String name,int capacity,boolean available){}
}
