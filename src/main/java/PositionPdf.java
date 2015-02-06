import junit.framework.Assert;
import org.apache.xmlbeans.impl.tool.XSTCTester;
import org.junit.Test;

/**
 * Created by rest on 1/27/15.
 */
public class PositionPdf extends XSTCTester.TestCase{

    public static void main(String[] args) {


    }

    @Test
    public void someTest(){
        float var = 60.000004f;
        Assert.assertEquals(var,60,001);
        Assert.assertEquals(var,60);
    }

}
