package edu.atlas.campusos.service;

import java.util.*;

/** Maximum-cardinality bipartite matching using DFS augmenting paths (Kuhn algorithm). */
public final class MaximumBipartiteMatcher {
    private MaximumBipartiteMatcher(){}
    public record Result(int[] rightByLeft,int cardinality){public int[] rightByLeft(){return rightByLeft.clone();}}
    public static Result match(boolean[][] edges){
        int left=edges.length,right=left==0?0:edges[0].length;for(boolean[] row:edges)if(row.length!=right)throw new IllegalArgumentException("Adjacency matrix must be rectangular.");
        int[] leftByRight=new int[right];Arrays.fill(leftByRight,-1);int count=0;
        for(int l=0;l<left;l++)if(augment(l,edges,leftByRight,new boolean[right]))count++;
        int[] rightByLeft=new int[left];Arrays.fill(rightByLeft,-1);for(int r=0;r<right;r++)if(leftByRight[r]>=0)rightByLeft[leftByRight[r]]=r;
        return new Result(rightByLeft,count);
    }
    private static boolean augment(int left,boolean[][] edges,int[] leftByRight,boolean[] seen){
        for(int right=0;right<leftByRight.length;right++)if(edges[left][right]&&!seen[right]){seen[right]=true;if(leftByRight[right]<0||augment(leftByRight[right],edges,leftByRight,seen)){leftByRight[right]=left;return true;}}
        return false;
    }
}
