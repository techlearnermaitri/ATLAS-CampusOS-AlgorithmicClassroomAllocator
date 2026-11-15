package edu.atlas.campusos.service;

/** Minute-granularity occupancy map for one room and one calendar day. Intervals are [start,end). */
public final class SegmentTree {
    private static final int SIZE=1440;
    private final int[] max=new int[SIZE*4];
    public boolean isFree(int start,int end){if(start<0||end>SIZE||start>=end)return false;return query(1,0,SIZE,start,end)==0;}
    public void occupy(int start,int end){if(start<0||end>SIZE||start>=end)throw new IllegalArgumentException("Invalid time range");update(1,0,SIZE,start,end);}
    private int query(int node,int lo,int hi,int ql,int qr){if(qr<=lo||hi<=ql)return 0;if(ql<=lo&&hi<=qr)return max[node];int mid=(lo+hi)/2;return Math.max(query(node*2,lo,mid,ql,qr),query(node*2+1,mid,hi,ql,qr));}
    private void update(int node,int lo,int hi,int ql,int qr){if(qr<=lo||hi<=ql)return;if(hi-lo==1){max[node]=1;return;}int mid=(lo+hi)/2;update(node*2,lo,mid,ql,qr);update(node*2+1,mid,hi,ql,qr);max[node]=Math.max(max[node*2],max[node*2+1]);}
}
