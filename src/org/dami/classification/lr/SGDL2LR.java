package org.dami.classification.lr;

import java.io.IOException;
import java.util.Properties;

import org.dami.common.Constants;
import org.dami.common.Utilities;
import org.dami.common.Vector;
import org.dami.common.VectorPool;
import org.dami.common.io.VectorStorage;

public class SGDL2LR extends AbstractSDGLogisticRegression{

	private double gradientDescend(Vector sample){
		double weightSum = 0;
		
		for(int i = 0 ; i < sample.featureSize; i++){
			weightSum += featureWeights[sample.features[i]] * sample.weights[i];
		}
		double tmp = Math.pow(Math.E, -weightSum); //e^-sigma(x)
		double error = dataInfo[LABELRANGEBASE + sample.label][0] - (1/ (1+tmp)); 
		double partialDerivation =  tmp  / (tmp * tmp + 2 * tmp + 1) ;

		for(int i = 0 ; i < sample.featureSize; i++){
			// w <- w + alpha * (error * partial_derivation - lambda * w) 
			featureWeights[sample.features[i]] += 
					alpha * (error * sample.weights[i] * partialDerivation - lambda * featureWeights[sample.features[i]]) ;
		}
		return error;
	}
	
	
	
	@Override
	protected void _train(int fold, int remain) throws IOException {
		double avge = 99999.9;
		double lastAVGE = Double.MAX_VALUE;
		
		double corrects  = 0;
		double lastCorrects = -1;
		Vector sample = new Vector();
		
		double multi = (biasWeightRound * minSamples + maxSamples)/(minSamples + maxSamples + 0.0);
		
		for(int l = 0 ; l < Math.min(10, loops) || l < loops && (Math.abs(1- avge/ lastAVGE) > stop || Math.abs(1- corrects/ lastCorrects) > stop * 0.1); l++){
			lastAVGE = avge;
			lastCorrects = corrects;
			if (dataEntry instanceof VectorStorage.FileStorage)
				pool.open();
			else
				dataEntry.reOpenData();

			long timeStart = System.currentTimeMillis();
			
			int c =1; //for n-fold cv
			double error = 0;
			double sume = 0;
			corrects = 0;
			int cc = 0;

			if (dataEntry instanceof VectorStorage.FileStorage){ 
				for(sample = pool.get(); sample != null; sample = pool.get()){
					if (c % fold == remain){ // no train
						;
					}else{ //train
						if ( sample.label == this.biasLabel){ //bias; sequentially compute #(bias - 1) times
							for(int bw = 1 ; bw < this.biasWeightRound; bw++){ //bias
								for(int i = 0 ; i < sample.count; i++)
									this.gradientDescend(sample);
							}
						}
						for(int i = 0 ; i < sample.count; i++){
							error = gradientDescend(sample);
							if (Math.abs(error) < 0.5)//accuracy
								if ( sample.label == this.biasLabel)
									corrects += this.biasWeightRound;
								else
									corrects += 1; 
							cc += 1;
							sume += Math.abs(error);
						}
					}
					c += 1;
					pool.takeBack();
				}
				pool.close();
			}
			else{
				for(dataEntry.next(sample); sample.featureSize >= 0; dataEntry.next(sample)){
					if (c % fold == remain){ // no train
						;
					}else{ //train
						if ( sample.label == this.biasLabel){ //bias; sequentially compute #(bias - 1) times
							for(int bw = 1 ; bw < this.biasWeightRound; bw++){ //bias
								for(int i = 0 ; i < sample.count; i++)
									this.gradientDescend(sample);
							}
						}
						for(int i = 0 ; i < sample.count; i++){
							error = gradientDescend(sample);
							if (Math.abs(error) < 0.5)//accuracy
								if ( sample.label == this.biasLabel)
									corrects += this.biasWeightRound;
								else
									corrects += 1; 
							cc += 1;
							sume += Math.abs(error);
						}
					}
					c += 1;
				}
				
			}
			
			avge = sume / cc;
			
			long timeEnd = System.currentTimeMillis();
			double acc = corrects / (cc * multi) * 100;
			
			if (corrects  < lastCorrects ){ //
				if (!alphaSetted){
					this.alpha *= 0.5;
					if (alpha < minAlpha)
						alpha = minAlpha;
				}
				if (!lambdaSetted){
					this.lambda *= 0.9;
					if (lambda < minLambda)
						lambda = minLambda;
				}
			}
			System.out.println(String.format("#%d loop%d\ttime:%d(ms)\tacc: %.3f(approx)\tavg_error:%.6f", cc, l, (timeEnd - timeStart), acc , avge));
		}
		
	}

	@Override
	public int estimate(Properties dataStatus, Properties parameters) {
		// TODO Auto-generated method stub
		int maxFeatureId = Utilities.getIntFromProperties(dataStatus, Constants.MAXFEATUREID);
		int maxFeatureSize = Utilities.getIntFromProperties(dataStatus, Constants.MAXFEATURESIZE);
		int numberLines = Utilities.getIntFromProperties(dataStatus, Constants.NUMBER_SAMPLES);
		int numberFeatures = Utilities.getIntFromProperties(dataStatus, Constants.TOTAL_FEATURES);
		int vectorStatusPara = Utilities.getIntFromProperties(dataStatus, Constants.VESTOC_STATUS);
		int modelSize = maxFeatureId * 4 / 1024;
		int dataSetKb = 0;
		if (parameters.containsKey(Constants.FILESTREAM_INPUT)){
			// use file data 
			dataSetKb += 512; // VectorStorage.FileStorage approximately cost
			dataSetKb += VectorPool.RAMEstimate( maxFeatureSize);
		}else{
			dataSetKb += VectorStorage.RAMCompactStorage.RAMEstimate(numberLines, numberFeatures, vectorStatusPara);
		}
		return dataSetKb + modelSize;
	}



	@Override
	protected void estimateParameter() throws NullPointerException{
		this.samples = Utilities.getIntFromProperties(dataEntry.getDataSetInfo(), Constants.NUMBER_SAMPLES);
		double rate = Math.log(2 + samples /((1 + biasWeightRound)/(biasWeightRound * 2.0)) /( this.maxFeatureId + 0.0));
		if (rate < 0.5)
			rate = 0.5;

		if (alpha == null){
			alpha = 0.5 / rate;
			minAlpha = alpha  / Math.pow(1 + rate, 1.8);
		}
		if (this.lambda == null){
			lambda = 0.2 / rate;
//			minLambda = lambda  / Math.pow(1 + rate, 1.8);
			minLambda = 0.01;
		}
	}

}
