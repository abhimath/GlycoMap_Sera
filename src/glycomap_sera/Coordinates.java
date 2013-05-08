package glycomap_sera;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.net.URLConnection;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Coordinates
{
    public Coordinates()
    {
        try
        {
            con = new URL("http://ggdb.informatics.indiana.edu:8080/webdav/data").openConnection();
            fetchInfo();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public Double[] convDouble(JSONArray arr)
    {
        Double[] d = new Double[arr.size()];
        for(int i = 0; i < arr.size(); i++)
        {
            d[i] = arr.getDouble(i);
        }
        
        return d;
    }
    
    public int[] convInt(JSONArray arr)
    {
        int[] in = new int[arr.size()];
        for(int i = 0; i < arr.size(); i++)
        {
            in[i] = arr.getInt(i);
        }
        
        return in;
    }
    
    public String[] convString(JSONArray arr)
    {
        String[] s = new String[arr.size()];
        for(int i = 0; i < arr.size(); i++)
        {
            s[i] = arr.getString(i);
        }
        
        return s;
    }
    
    private void fetchInfo()
    {
        try
        {
            con.setRequestProperty("Content-Type", "application/x-java-serialized-object");
            InputStream instr = con.getInputStream();
            ObjectInputStream inputFromServlet = new ObjectInputStream(instr);
            data = (JSONObject) inputFromServlet.readObject();
	    inputFromServlet.close();
	    instr.close();
            
            mass = convDouble((JSONArray) data.get("mass"));
            net = convDouble((JSONArray) data.get("net"));
            peptide = convString((JSONArray) data.get("peptide"));
            glycan = convString((JSONArray) data.get("glycan"));
            proteinid = convString((JSONArray) data.get("proteinid"));
            site = convString((JSONArray) data.get("site"));
            cidlen = convInt((JSONArray) data.get("cidlen"));
            cidspec = convString((JSONArray) data.get("cidspec"));
            hcdlen = convInt((JSONArray) data.get("hcdlen"));
            hcdspec = convString((JSONArray) data.get("hcdspec"));
            intca = convDouble((JSONArray) data.get("intca"));
            intco = convDouble((JSONArray) data.get("intco"));
            nzca = convInt((JSONArray) data.get("nzca"));
            nzco = convInt((JSONArray) data.get("nzco"));
            charge = convInt((JSONArray) data.get("charge"));
        }
        catch(IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }
    
    public Double[] getMass()
    {
        return mass;
    }
    
    public Double[] getNET()
    {
        return net;
    }
    
    public String[] getPeptide()
    {
        return peptide;
    }
    
    public String[] getGlycan()
    {
        return glycan;
    }
    
    public String[] getProteinID()
    {
        return proteinid;
    }
    
    public String[] getSite()
    {
        return site;
    }
    
    public int[] getCIDLen()
    {
        return cidlen;
    }
    
    public String[] getCIDSpec()
    {
        return cidspec;
    }
    
    public int[] getHCDLen()
    {
        return hcdlen;
    }
    
    public String[] getHCDSpec()
    {
        return hcdspec;
    }
    
    public Double[] getIntCa()
    {
        return intca;
    }
    
    public Double[] getIntCo()
    {
        return intco;
    }
    
    public int[] getNonZeroCa()
    {
        return nzca;
    }
    
    public int[] getNonZeroCo()
    {
        return nzco;
    }
    
    public int[] getCharge()
    {
        return charge;
    }
    
    private URLConnection con = null;
    private JSONObject data;
    private int cidlen[], hcdlen[], nzca[], nzco[], charge[];
    private Double mass[], net[], intca[], intco[];
    private String peptide[], glycan[], proteinid[], site[], cidspec[], hcdspec[];
}