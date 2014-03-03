package dk.mercantec.smartgrid;

import java.util.Hashtable;
import java.util.Vector;

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;

public class requestId extends Vector<String> implements KvmSerializable
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1477625528945740369L;
	public String deviceId;
	public String meterId;
	
	
	public requestId(String deviceId, String meterId)
	{
		this.deviceId = deviceId;
		this.meterId = meterId;
	}
	
	@Override
	public int getPropertyCount() {
	    // TODO Auto-generated method stub
	    return this.size();
	}

	@Override
	public void getPropertyInfo(int arg0, Hashtable arg1, PropertyInfo arg2) {
	    // TODO Auto-generated method stub
	    arg2.name = "string";
	    arg2.type = PropertyInfo.STRING_CLASS;
	}

	@Override
	public void setProperty(int arg0, Object arg1) {
	    // TODO Auto-generated method stub
	    this.add(arg1.toString());
	}

	@Override
	public Object getProperty(int arg0) {
		// TODO Auto-generated method stub
		return this.get(arg0);
	}
		
}
