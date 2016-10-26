package sg.edu.nus.mda.simquery.pivotselection.kmean;
/**
 * 简单数组计算
 *
 * <p>time:2011-5-27</p>
 * @author T. QIN
 */
public class ArrayCompute
{
    /**
     * 数组相加
     *
     * @param x1
     * @param x2
     * @return
     * @see: 
     */
    public static double[] add(final double[] x1, final double[] x2)
    {
        if (x1.length != x2.length)
        {
            System.err.print("向量长度不等不能相加！");
            System.exit(0);
        }
        double[] result = new double[x1.length];
        for (int i = 0; i < result.length; i++)
        {
            result[i] = x1[i] + x2[i];
        }
        return result;
    }

    /**
     * 数组相减
     *
     * @param x1
     * @param x2
     * @return
     * @see: 
     */
    public static double[] minus(final double[] x1, final double[] x2)
    {
        if (x1.length != x2.length)
        {
            System.err.print("向量长度不等不能相减！");
            System.exit(0);
        }
        double[] result = new double[x1.length];
        for (int i = 0; i < result.length; i++)
        {
            result[i] = x1[i] - x2[i];
        }
        return result;
    }

    /**
     * 数组乘以一个常数
     *
     * @param x1
     * @param c
     * @return
     * @see: 
     */
    public static double[] multiplyC(final double[] x1, final double c)
    {
        double[] ret = new double[x1.length];
        for (int i = 0; i < x1.length; i++)
        {
            ret[i] = x1[i] * c;
        }
        return ret;
    }

    /**
     * 数组除以一个常数
     *
     * @param x1
     * @param c
     * @return
     * @see: 
     */
    public static double[] devideC(final double[] x1, final double c)
    {
        double[] ret = new double[x1.length];
        for (int i = 0; i < x1.length; i++)
        {
            ret[i] = x1[i] / c;
        }
        return ret;
    }
}