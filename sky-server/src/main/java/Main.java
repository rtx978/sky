import java.lang.reflect.Array;
import java.util.*;

public class Main {
    static Scanner sc=new Scanner(System.in);
    static class v{
        int l,r;
        v(int a,int b)
        {
            l=a;r=b;
        }
        v(){}
    }
    public static void main(String[] args) {
        int n=sc.nextInt();
        int m=sc.nextInt();
        int[] a=new int[n+10];
        List<v> ans=new ArrayList<>();
//        ans.push(new v(1,n));
        for(int i=1;i<=n;i++)
        {
            a[i]=sc.nextInt();
        }
        int[] d=new int[n+10];
        for(int i=1;i<=n;i++)
        {
            d[i]=a[i]-a[i-1];
        }
        int cnt=0;
        for(int i=1;i<=n;i++)
        {
            cnt+=Math.max(d[i],0);
//            System.out.println("i="+i+" dif= "+d[i]);
        }
        if(m<cnt) {
            System.out.println(-1);
        }
        else{
            Queue<Integer> q=new LinkedList<>();
            int[] b=new int[n+10];
            for(int i=1;i<=n;i++)
            {
                while(d[i]>0){
                    d[i]--;
                    q.offer(i);
                }
                while(d[i]<0)
                {
                    if(q.isEmpty()) {  // 添加检查队列是否为空
                        System.out.println(-1);
                        return;
                    }
                    d[i]++;
                    int s=q.poll();
                    ans.add(new v(s,i-1));

                }
            }
            while(!q.isEmpty()){
                int s=q.poll();
                ans.add(new v(s,n));
            }
            int dif=m-ans.size();

            // 添加新数组来解决迭代器修改问题
            List<v> tempAns = new ArrayList<>();
            for(v x : ans) {
                if(dif == 0) {
                    tempAns.add(x);
                    continue;
                }
                int mx = x.r - x.l;
                if(mx==0)
                {
                    tempAns.add(x);
                    continue;
                }
                if(mx <= dif) {
                    dif -= mx;
                    for(int i = x.l; i <= x.r; i++) {
                        tempAns.add(new v(i, i));
                    }
                } else {
                    for(int i = x.l; i < x.l + dif; i++) {
                        tempAns.add(new v(i, i));
                    }
                    tempAns.add(new v(x.l + dif, x.r));  // 修正区间起始位置
                    dif = 0;
                }
            }

            // 如果拆分后还有剩余的dif，说明无法构造
            if(dif > 0) {
                System.out.println(-1);
                return;
            }

            for(v x : tempAns)
            {
                System.out.println(x.l + " " + x.r);
            }
        }
    }
    class Solution {
        public int minCost(String colors, int[] neededTime) {
            int n=colors.length();
            int ans=0;
            for(int i=0;i<n-1;)
            {
                ArrayList<Integer> a=new ArrayList<>();
                a.clear();
                a.add(neededTime[i]);
                int j=i+1;
                while(j<n&&colors.charAt(i)==(colors.charAt(j))){
                    a.add(neededTime[j]);
                    j++;
                }
                a.sort((Integer aa,Integer b)->{
                    return aa-b;
                });
                for(int k=0;k<a.size()-1;k++)
                {
                    ans+=a.get(k);
                }
                i=j;
            }
            return ans;
        }
    }
}