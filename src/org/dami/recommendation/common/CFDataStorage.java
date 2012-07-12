package org.dami.recommendation.common;


import java.io.IOException;
import java.util.Properties;


import org.dami.common.io.RandomAccessVectorStorage;
import org.dami.common.io.VectorStorage;
import org.dami.recommendation.common.UserRatings;


/**
 * CFDataStorage has a few differences with VectorStorage
 * More specific :	1. return UserRatings type instead of Vector
 * 					2. support UserRatings fetch by userid
 * 
 * @author lgn
 *
 */
public class CFDataStorage {
	VectorStorage storage;
	RandomAccessVectorStorage RAstorage;
	boolean randomAccess = true;
	
	public boolean supportRandomAccess(){
		return randomAccess;
	}
	
	public CFDataStorage(VectorStorage storage){
		this.storage = storage;
		if (storage instanceof RandomAccessVectorStorage){
			RAstorage = (RandomAccessVectorStorage)storage;
		}else{
			randomAccess = false;
		}
	}
	
	public void reOpenDataSet() throws IOException{
		storage.reOpenData();
	}

	public void close() throws IOException{
		storage.close();
	}
	
	public Properties getDataSetInfo(){
		return storage.getDataSetInfo();
	}
	
	public void nextUser(UserRatings ur) throws IOException{
		storage.next(ur.vector);
		ur.refresh();
	}
	
	/**
	 * if storage is not supported for random access, return null
	 * @param uid
	 * @return
	 * @throws Exception
	 */
	public UserRatings getUserById(int uid) throws Exception{
		if (randomAccess)
			return new UserRatings(RAstorage.getVectorById( uid));
		return null;
	}
}
