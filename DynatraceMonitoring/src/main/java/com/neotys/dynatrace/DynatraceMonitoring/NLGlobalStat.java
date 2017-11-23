package com.neotys.dynatrace.DynatraceMonitoring;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import io.swagger.client.model.ElementValues;
import io.swagger.client.model.TestStatistics;

public class NLGlobalStat {

	private float LastRequestCountPerSecond=0;
	private boolean LastRequestCountPerSecond_data=false;

	private float LastTransactionDurationAverage=0;
	private boolean LastTransactionDurationAverage_data=false;
	private int   LastVirtualUserCount=0;
	private boolean LastVirtualUserCount_data=false;
	private long TotalGlobalCountFailure=0;
	private boolean TotalGlobalCountFailure_data=false;
	private float TotalGlobalDownloadedBytes=0;
	private boolean TotalGlobalDownloadedBytes_data=false;
	private float TotalGlobalDownloadedBytesPerSecond=0;
	private boolean TotalGlobalDownloadedBytesPerSecond_data=false;
	private long TotalIterationCountFailure=0;
	private boolean TotalIterationCountFailure_data=false;

	private long TotalIterationCountSuccess=0;
	private boolean TotalIterationCountSuccess_data=false;
	private long TotalRequestCountFailure=0;
	private boolean TotalRequestCountFailure_data=false;
	private float TotalRequestCountPerSecond=0;
	private boolean TotalRequestCountPerSecond_data=false;
	private long TotalRequestCountSuccess=0;
	private boolean TotalRequestCountSuccess_data=false;

	private long TotalTransactionCountFailure=0;
	private boolean TotalTransactionCountFailure_data;

	private long TotalTransactionCountSuccess=0;
	private boolean TotalTransactionCountSuccess_data=false;

	private float TotalTransactionCountPerSecond=0;
	private boolean TotalTransactionCountPerSecond_data=false;

	private float LastRequestDurationAverage=0;
	private boolean LastRequestDurationAverage_data=false;

	private float TotalRequestCountSuccessPerSecond=0;
	private boolean TotalRequestCountSuccessPerSecond_data=false;

	private float TotalRequestCountFailurePerSecond=0;	
	private boolean TotalRequestCountFailurePerSecond_data=false;
	private float TotalTransactionFailurePerSecond=0;
	private boolean TotalTransactionFailurePerSecond_data=false;

	private float TotalTransactionSuccessPerSecond=0;
	private boolean TotalTransactionSuccessPerSecond_data=false;

	private float TotalDownloadedBytesPerSecond=0;
	private boolean TotalDownloadedBytesPerSecond_data=false;
	private float FailureRate=0;
	private boolean FailureRate_data=false;

	private static final String LastRequestDurationAverage_MetricName="Request duration";
	private static final String LastRequestDurationAverage_Component="Request.duration";
	private static final String LastRequestDurationAverage_Unit="Second";
	private boolean LastRequestDurationAverage_status=false;
	
	private static final String LastRequestCountPerSecond_MetricName="Request Count";
	private static final String LastRequestCountPerSecond_Component="Request.Count";
	private static final String LastRequestCountPerSecond_Unit="request/Second";
	private  boolean  LastRequestCountPerSecond_status=false;
	
	private static final String LastTransactionDurationAverage_MetricName="AverageTransactionDuration";
	private static final String LastTransactionDurationAverage_Component="Transaction.Average.Duration";
	private static final String LastTransactionDurationAverage_Unit="Second";
	private  boolean  LastTransactionDurationAverage_status=false;
	
	private static final String LastVirtualUserCount_MetricName="User Load";
	private static final String LastVirtualUserCount_Component="User.Load";
	private static final String LastVirtualUserCount_Unit="Count";
	private  boolean LastVirtualUserCount_status=false;
	
	private static final String TotalGlobalCountFailure_MetricName="Number of Failure";
	private static final String TotalGlobalCountFailure_Component="Count.Average.Failure";
	private static final String TotalGlobalCountFailure_Unit="count";
	private  boolean  TotalGlobalCountFailure_status=false;
	
	private static final String TotalGlobalDownloadedBytes_MetricName="Downloaded Bytes";
	private static final String TotalGlobalDownloadedBytes_Component="DowLoaded.Average.Bytes";
	private static final String TotalGlobalDownloadedBytes_Unit="Bytes";
	private  boolean  TotalGlobalDownloadedBytes_status=false;
	
	private static final String TotalGlobalDownloadedBytesPerSecond_MetricName="Downloaded Bytes";
	private static final String TotalGlobalDownloadedBytesPerSecond_Component="Downloaded.Average.Bytes.PerSecond";
	private static final String TotalGlobalDownloadedBytesPerSecond_Unit="Bytes/Second";
	private  boolean  TotalGlobalDownloadedBytesPerSecond_status=false;
	
	private static final String TotalIterationCountFailure_MetricName="Iteration in Failure";
	private static final String TotalIterationCountFailure_Component="Iteration.Average.Failure";
	private static final String TotalIterationCountFailure_Unit="Count";
	private  boolean  TotalIterationCountFailure_status=false;
	
	private static final String TotalIterationCountSuccess_MetricName="Iteration in Success";
	private static final String TotalIterationCountSuccess_Component="Iteration.Average.Success";
	private static final String TotalIterationCountSuccess_Unit="Count";
	private  boolean  TotalIterationCountSuccess_status=false;
	
	private static final String TotalRequestCountFailure_MetricName="Request in Failure";
	private static final String TotalRequestCountFailure_Component="Request.Average.Failure";
	private static final String TotalRequestCountFailure_Unit="Count";
	private  boolean  TotalRequestCountFailure_status=false;
	
	private static final String TotalRequestCountPerSecond_MetricName="Number of request";
	private static final String TotalRequestCountPerSecond_Component="Request.Average.Count";
	private static final String TotalRequestCountPerSecond_Unit="Request/Second";
	private  boolean  TotalRequestCountPerSecond_status=false;
	
	private static final String TotalRequestCountSuccess_MetricName="Request in Success";
	private static final String TotalRequestCountSuccess_Component="Request.Average.Success";
	private static final String TotalRequestCountSuccess_Unit="Count";
	private  boolean  TotalRequestCountSuccess_status=false;
	
	private static final String TotalRequestCountSuccessPerSecond_MetricName="Request in Success Per second";
	private static final String TotalRequestCountSuccessPerSecond_Component="Request.Sucess.PerSecond";
	private static final String TotalRequestCountSuccessPerSecond_Unit="request/s";
	private  boolean  TotalRequestCountSuccessPerSecond_status=false;
	
	private static final String TotalRequestCountFailurePerSecond_MetricName="Request in Failure Per Second";
	private static final String TotalRequestCountFailurePerSecond_Component="Request.Failure.PerSeconds";
	private static final String TotalRequestCountFailurePerSecond_Unit="Count";
	private  boolean  TotalRequestCountFailuerPerSecond_status=false;
	
	private static final String TotalTransactionCountFailure_MetricName="Transaction in Failure";
	private static final String TotalTransactionCountFailure_Component="Transaction.Average.Failure";
	private static final String TotalTransactionCountFailure_Unit="Count";
	private  boolean  TotalTransactionCountFailure_status=false;
	
	private static final String TotalTransactionCountSucess_MetricName="Transaction in Success";
	private static final String TotalTransactionCountSucess_Component="Transaction.Average.Success";
	private static final String TotalTransactionCountSucess_Unit="Count";
	private  boolean TotalTransactionCountSucess_status=false;
	
	private static final String TotalTransactionCountFailurePerSecond_MetricName="Transaction in Failure Per Second";
	private static final String TotalTransactionCountFailurePerSecond_Component="Transaction.Failure.PerSecond";
	private static final String TotalTransactionCountFailurePerSecond_Unit="Transaction/s";
	private  boolean  TotalTransactionCountFailurePerSecond_status=false;
	
	private static final String TotalTransactionCountSucessPerSecond_MetricName="Transaction in Success Per Second";
	private static final String TotalTransactionCountSucessPerSecond_Component="Transaction.Sucess.PerSecond";
	private static final String TotalTransactionCountSucessPerSecond_Unit="Transaction/s";
	private  boolean TotalTransactionCountSucessPerSecond_status=false;
	
	private static final String TotalTransactionCountPerSecond_MetricName="Number of Transaction";
	private static final String TotalTransactionCountPerSecond_Component="Transaction.Average.Count";
	private static final String TotalTransactionCountPerSecond_Unit="Transaction/Second";
	private static final String TotalTransactionCountSuccessPerSecond_Component = null;
	private  boolean  TotalTransactionCountPerSecond_status=false;
	

	private static final String TotalDownloadedBytesPerSecond_MetricName="Downloaded Bytes Per Second";
	private static final String TotalDownloadedBytesPerSecond_Component="Downloaded.Bytes.PerSecond";
	private static final String TotalDownloadedBytesPerSecond_Unit="Bytes/Second";
	private  boolean  TotalDownloadedBytesPerSecond_status=false;
	
	
	private static final String FailureRate_MetricName="Failure Rate";
	private static final String FailureRate_Component="Failure.Rate";
	private static final String FailureRate_Unit="percentage";
	private  boolean  FailureRate_status=false;
	
	
	public float getFailureRate() {
		return FailureRate;
	}

	public void setFailureRate(Float failureRate) {
		if(failureRate!=null) {
			FailureRate = failureRate ;
			FailureRate_data=true;
		}
		else
			FailureRate_data=false;
	}

	public boolean isFailureRate_status() {
		return FailureRate_status;
	}

	public void setFailureRate_status(boolean failureRate_status) {
		FailureRate_status = failureRate_status;
	}
	
	public float getTotalDownloadedBytesPerSecond() {
		return TotalDownloadedBytesPerSecond;
	}

	public void setTotalDownloadedBytesPerSecond(Float totalDownloadedBytesPerSecond) {
		if(totalDownloadedBytesPerSecond!=null) {
			TotalDownloadedBytesPerSecond = totalDownloadedBytesPerSecond;
			TotalDownloadedBytesPerSecond_data=true;
		}
		else
			TotalDownloadedBytesPerSecond_data=false;
		}

	public boolean isTotalDownloadedBytesPerSecond_status() {
		return TotalDownloadedBytesPerSecond_status;
	}

	public void setTotalDownloadedBytesPerSecond_status(boolean totalDownloadedBytesPerSecond_status) {
		TotalDownloadedBytesPerSecond_status = totalDownloadedBytesPerSecond_status;
	}

	
	public float getTotalTransactionFailurePerSecond() {
		return TotalTransactionFailurePerSecond;
	}

	public void setTotalTransactionFailurePerSecond(Float totalTransactionFailurePerSecond) {
		if(totalTransactionFailurePerSecond!=null) {
			TotalTransactionFailurePerSecond = totalTransactionFailurePerSecond;
			TotalTransactionFailurePerSecond_data=true;
		}
		else
			TotalTransactionFailurePerSecond_data=false;
	}

	public float getTotalTransactionSuccessPerSecond() {
		return TotalTransactionSuccessPerSecond;
	}

	public void setTotalTransactionSuccessPerSecond(Float totalTransactionSuccessPerSecond) {
		if(totalTransactionSuccessPerSecond!=null) {
			TotalTransactionSuccessPerSecond = totalTransactionSuccessPerSecond;
			TotalTransactionSuccessPerSecond_data=true;
		}
		else
			TotalTransactionSuccessPerSecond_data=false;
	}

	public boolean isTotalTransactionCountFailurePerSecond_status() {
		return TotalTransactionCountFailurePerSecond_status;
	}

	public void setTotalTransactionCountFailurePerSecond_status(boolean totalTransactionCountFailurePerSecond_status) {
		TotalTransactionCountFailurePerSecond_status = totalTransactionCountFailurePerSecond_status;
	}

	public boolean isTotalTransactionCountSucessPerSecond_status() {
		return TotalTransactionCountSucessPerSecond_status;
	}

	public void setTotalTransactionCountSucessPerSecond_status(boolean totalTransactionCountSucessPerSecond_status) {
		TotalTransactionCountSucessPerSecond_status = totalTransactionCountSucessPerSecond_status;
	}

	public boolean isTotalRequestCountSuccessPerSecond_status() {
		return TotalRequestCountSuccessPerSecond_status;
	}

	public void setTotalRequestCountSuccessPerSecond_status(boolean totalRequestCountSuccessPerSecond_status) {
		TotalRequestCountSuccessPerSecond_status = totalRequestCountSuccessPerSecond_status;
	}

	public boolean isTotalRequestCountFailuerPerSecond_status() {
		return TotalRequestCountFailuerPerSecond_status;
	}

	public void setTotalRequestCountFailuerPerSecond_status(boolean totalRequestCountFailuerPerSecond_status) {
		TotalRequestCountFailuerPerSecond_status = totalRequestCountFailuerPerSecond_status;
	}


	
	public boolean isLastRequestCountPerSecond_status() {
		return LastRequestCountPerSecond_status;
	}

	public boolean isLastRequestRequestDuration_status() {
		return LastRequestDurationAverage_status;
	}


	public void setLastRequestCountPerSecond_status(boolean lastRequestCountPerSecond_status) {
		LastRequestCountPerSecond_status = lastRequestCountPerSecond_status;
	}



	public boolean isLastTransactionDurationAverage_status() {
		return LastTransactionDurationAverage_status;
	}



	public void setLastTransactionDurationAverage_status(boolean lastTransactionDurationAverage_status) {
		LastTransactionDurationAverage_status = lastTransactionDurationAverage_status;
	}

	public float getTotalRequestCountSuccessPerSecond() {
		return TotalRequestCountSuccessPerSecond;
	}

	public void setTotalRequestCountSuccessPerSecond(Float totalRequestCountSuccessPerSecond) {
		if(totalRequestCountSuccessPerSecond!=null) {
			TotalRequestCountSuccessPerSecond = totalRequestCountSuccessPerSecond;
			TotalRequestCountSuccessPerSecond_data=true;
		}
		else
			TotalRequestCountSuccessPerSecond_data=false;
	}

	public float getTotalRequestCountFailurePerSecond() {
		return TotalRequestCountFailurePerSecond;
	}

	public void setTotalRequestCountFailurePerSecond(Float float1) {
		if(float1!=null) {
			TotalRequestCountFailurePerSecond = float1;
			TotalRequestCountFailurePerSecond_data=true;
		}
		else
			TotalRequestCountFailurePerSecond_data=false;
	}


	public boolean isLastVirtualUserCount_status() {
		return LastVirtualUserCount_status;
	}



	public void setLastVirtualUserCount_status(boolean lastVirtualUserCount_status) {
		LastVirtualUserCount_status = lastVirtualUserCount_status;
	}



	public boolean isTotalGlobalCountFailure_status() {
		return TotalGlobalCountFailure_status;
	}



	public void setTotalGlobalCountFailure_status(boolean totalGlobalCountFailure_status) {
		TotalGlobalCountFailure_status = totalGlobalCountFailure_status;
	}



	public boolean isTotalGlobalDownloadedBytes_status() {
		return TotalGlobalDownloadedBytes_status;
	}



	public void setTotalGlobalDownloadedBytes_status(boolean totalGlobalDownloadedBytes_status) {
		TotalGlobalDownloadedBytes_status = totalGlobalDownloadedBytes_status;
	}



	public boolean isTotalGlobalDownloadedBytesPerSecond_status() {
		return TotalGlobalDownloadedBytesPerSecond_status;
	}



	public void setTotalGlobalDownloadedBytesPerSecond_status(boolean totalGlobalDownloadedBytesPerSecond_status) {
		TotalGlobalDownloadedBytesPerSecond_status = totalGlobalDownloadedBytesPerSecond_status;
	}



	public boolean isTotalIterationCountFailure_status() {
		return TotalIterationCountFailure_status;
	}



	public void setTotalIterationCountFailure_status(boolean totalIterationCountFailure_status) {
		TotalIterationCountFailure_status = totalIterationCountFailure_status;
	}



	public boolean isTotalIterationCountSuccess_status() {
		return TotalIterationCountSuccess_status;
	}



	public void setTotalIterationCountSuccess_status(boolean totalIterationCountSuccess_status) {
		TotalIterationCountSuccess_status = totalIterationCountSuccess_status;
	}



	public boolean isTotalRequestCountFailure_status() {
		return TotalRequestCountFailure_status;
	}



	public void setTotalRequestCountFailure_status(boolean totalRequestCountFailure_status) {
		TotalRequestCountFailure_status = totalRequestCountFailure_status;
	}



	public boolean isTotalRequestCountPerSecond_status() {
		return TotalRequestCountPerSecond_status;
	}



	public void setTotalRequestCountPerSecond_status(boolean totalRequestCountPerSecond_status) {
		TotalRequestCountPerSecond_status = totalRequestCountPerSecond_status;
	}



	public boolean isTotalRequestCountSuccess_status() {
		return TotalRequestCountSuccess_status;
	}



	public void setTotalRequestCountSuccess_status(boolean totalRequestCountSuccess_status) {
		TotalRequestCountSuccess_status = totalRequestCountSuccess_status;
	}



	public boolean isTotalTransactionCountFailure_status() {
		return TotalTransactionCountFailure_status;
	}



	public void setTotalTransactionCountFailure_status(boolean totalTransactionCountFailure_status) {
		TotalTransactionCountFailure_status = totalTransactionCountFailure_status;
	}



	public boolean isTotalTransactionCountSucess_status() {
		return TotalTransactionCountSucess_status;
	}



	public void setTotalTransactionCountSucess_status(boolean totalTransactionCountSucess_status) {
		TotalTransactionCountSucess_status = totalTransactionCountSucess_status;
	}



	public boolean isTotalTransactionCountPerSecond_status() {
		return TotalTransactionCountPerSecond_status;
	}



	public void setTotalTransactionCountPerSecond_status(boolean totalTransactionCountPerSecond_status) {
		TotalTransactionCountPerSecond_status = totalTransactionCountPerSecond_status;
	}
	
	public void setLastRequestDuration_status(boolean totalTransactionCountPerSecond_status) {
		LastRequestDurationAverage_status = totalTransactionCountPerSecond_status;
	}

	public float getLastRequestDurationAverage() {
		return LastRequestDurationAverage;
	}

	public void setLastRequestDurationAverage(Float lastRequestDurationAverage) {
		if(lastRequestDurationAverage!=null) {
			LastRequestDurationAverage = lastRequestDurationAverage / 1000;
			LastRequestDurationAverage_data=true;
		}
		else
			LastRequestDurationAverage_data=false;
	}
	
	private long lasduration=0;

	public void SetStatus(String MetricName)
	{
		switch(MetricName){
			case LastRequestCountPerSecond_Component:
				setLastRequestCountPerSecond_status(true);
				break;
			case LastVirtualUserCount_Component:
				setLastVirtualUserCount_status(true);
				break;
			case LastTransactionDurationAverage_Component:
				setLastTransactionDurationAverage_status(true);
				break;
			case TotalGlobalCountFailure_Component:
				setTotalGlobalCountFailure_status(true);
				break;
			case TotalGlobalDownloadedBytes_Component:
				setTotalGlobalDownloadedBytes_status(true);
				break;
			case TotalGlobalDownloadedBytesPerSecond_Component:
				setTotalGlobalDownloadedBytesPerSecond_status(true);
				break;
			case TotalIterationCountFailure_Component:
				setTotalIterationCountFailure_status(true);
				break;
			case TotalIterationCountSuccess_Component:
				setTotalIterationCountSuccess_status(true);
				break;
			case TotalRequestCountFailure_Component:
				setTotalRequestCountFailure_status(true);
				break;
			case TotalRequestCountPerSecond_Component:
				setTotalRequestCountPerSecond_status(true);
				break;
			case TotalRequestCountSuccess_Component:
				setTotalRequestCountSuccess_status(true);
				break;
			case TotalTransactionCountFailure_Component:
				setTotalTransactionCountFailure_status(true);
				break;
			case TotalTransactionCountPerSecond_Component:
				setTotalTransactionCountPerSecond_status(true);
				break;
			case TotalTransactionCountSucess_Component:
				setTotalTransactionCountSucess_status(true);
				break;
			case LastRequestDurationAverage_Component:
				setLastRequestDuration_status(true);
				break;
			case TotalRequestCountFailurePerSecond_Component:
				setTotalRequestCountFailuerPerSecond_status(true);
				break;
			case TotalRequestCountSuccessPerSecond_Component:
				setTotalRequestCountSuccessPerSecond_status(true);
				break;
			case TotalTransactionCountFailurePerSecond_Component:
				setTotalRequestCountFailuerPerSecond_status(true);
				break;
			case TotalTransactionCountSucessPerSecond_Component:
				setTotalTransactionCountFailure_status(true);
				break;
			case TotalDownloadedBytesPerSecond_Component:
				setTotalDownloadedBytesPerSecond_status(true);
				break;
			case FailureRate_Component:
				setFailureRate_status(true);
				break;
	
		}
	}
	public NLGlobalStat()
	{
		lasduration=0;
	}
	


	public NLGlobalStat(float lastRequestCountPerSecond, float lastTransactionDurationAverage, int lastVirtualUserCount,
			long totalGlobalCountFailure, float totalGlobalDownloadedBytes, float totalGlobalDownloadedBytesPerSecond,
			long totalIterationCountFailure, long totalIterationCountSuccess, long totalRequestCountFailure,
			Float totalRequestCountPerSecond, long totalRequestCountSuccess,
			long totalTransactionCountFailure,long totalTransactionCountSucess, float totalTransactionCountPerSecond,float lastrequestduration
			) {
		super();
		
		LastRequestCountPerSecond = lastRequestCountPerSecond;
		LastTransactionDurationAverage = lastTransactionDurationAverage/1000;
		LastVirtualUserCount = lastVirtualUserCount;
		
		LastRequestDurationAverage=lastrequestduration/1000;
		
		
		TotalTransactionCountPerSecond = totalTransactionCountPerSecond;
		
		if(TotalGlobalCountFailure==0)
			TotalGlobalCountFailure = totalGlobalCountFailure;
		else
			TotalGlobalCountFailure = totalGlobalCountFailure- TotalGlobalCountFailure;
		
		
		if(TotalGlobalDownloadedBytes==0)
			TotalGlobalDownloadedBytes = totalGlobalDownloadedBytes;
		else
			TotalGlobalDownloadedBytes = totalGlobalDownloadedBytes- TotalGlobalDownloadedBytes;
		
		

		if(TotalGlobalDownloadedBytesPerSecond==0)
			TotalGlobalDownloadedBytesPerSecond =totalGlobalDownloadedBytesPerSecond;
		else
			TotalGlobalDownloadedBytesPerSecond = totalGlobalDownloadedBytesPerSecond- TotalGlobalDownloadedBytesPerSecond;
		
		if(TotalIterationCountFailure==0)
			TotalIterationCountFailure = totalIterationCountFailure;
		else
			TotalIterationCountFailure = totalIterationCountFailure- TotalIterationCountFailure;
		
		if(TotalIterationCountSuccess==0)
			TotalIterationCountSuccess = totalIterationCountSuccess;
		else
			TotalIterationCountSuccess = totalIterationCountSuccess- TotalIterationCountSuccess;
	
		if(TotalRequestCountFailure==0)
			TotalRequestCountFailure = totalRequestCountFailure;
		else
			TotalRequestCountFailure = totalRequestCountFailure- TotalRequestCountFailure;
	
		if(TotalRequestCountPerSecond==0)
			TotalRequestCountPerSecond = totalRequestCountPerSecond;
		else
			TotalRequestCountPerSecond = totalRequestCountPerSecond- TotalRequestCountPerSecond;
	
		if(TotalRequestCountSuccess==0)
			TotalRequestCountSuccess = totalRequestCountSuccess;
		else
			TotalRequestCountSuccess = totalRequestCountSuccess- TotalRequestCountSuccess;
	
			
		if(TotalTransactionCountFailure==0)
			TotalTransactionCountFailure = totalTransactionCountFailure;
		else
			TotalTransactionCountFailure = totalTransactionCountFailure- TotalTransactionCountFailure;
	
		if(TotalTransactionCountSuccess==0)
			TotalTransactionCountSuccess = totalTransactionCountSucess;
		else
			TotalTransactionCountSuccess =  totalTransactionCountSucess- TotalTransactionCountSuccess;
		
		if(TotalTransactionCountPerSecond==0)
			TotalTransactionCountPerSecond = totalTransactionCountPerSecond;
		else
			TotalTransactionCountPerSecond = totalTransactionCountPerSecond- TotalTransactionCountPerSecond;
	
		
	}
	
	
	public NLGlobalStat(TestStatistics response)
	{
		LastRequestCountPerSecond = response.getLastRequestCountPerSecond();
		LastTransactionDurationAverage = response.getLastTransactionDurationAverage()/1000;
		LastRequestDurationAverage=response.getTotalRequestDurationAverage()/1000;
		LastVirtualUserCount = response.getLastVirtualUserCount();
		
		if(TotalGlobalCountFailure==0)
			TotalGlobalCountFailure = response.getTotalGlobalCountFailure();
		else
			TotalGlobalCountFailure = response.getTotalGlobalCountFailure()- TotalGlobalCountFailure;
		
		
		if(TotalGlobalDownloadedBytes==0)
			TotalGlobalDownloadedBytes = response.getTotalGlobalDownloadedBytes();
		else
			TotalGlobalDownloadedBytes = response.getTotalGlobalDownloadedBytes()- TotalGlobalDownloadedBytes;
		
		

		if(TotalGlobalDownloadedBytesPerSecond==0)
			TotalGlobalDownloadedBytesPerSecond = response.getTotalGlobalDownloadedBytesPerSecond();
		else
			TotalGlobalDownloadedBytesPerSecond = response.getTotalGlobalDownloadedBytesPerSecond()- TotalGlobalDownloadedBytesPerSecond;
		
		if(TotalIterationCountFailure==0)
			TotalIterationCountFailure = response.getTotalIterationCountFailure();
		else
			TotalIterationCountFailure = response.getTotalIterationCountFailure()- TotalIterationCountFailure;
		
		if(TotalIterationCountSuccess==0)
			TotalIterationCountSuccess = response.getTotalIterationCountSuccess();
		else
			TotalIterationCountSuccess = response.getTotalIterationCountSuccess()- TotalIterationCountSuccess;
	
		if(TotalRequestCountFailure==0)
			TotalRequestCountFailure = response.getTotalRequestCountFailure();
		else
			TotalRequestCountFailure = response.getTotalRequestCountFailure()- TotalRequestCountFailure;
	
		if(TotalRequestCountPerSecond==0)
			TotalRequestCountPerSecond = response.getTotalRequestCountPerSecond();
		else
			TotalRequestCountPerSecond = response.getTotalRequestCountPerSecond()- TotalRequestCountPerSecond;
	
		if(TotalRequestCountSuccess==0)
			TotalRequestCountSuccess = response.getTotalRequestCountSuccess();
		else
			TotalRequestCountSuccess = response.getTotalRequestCountSuccess()- TotalRequestCountSuccess;
	
			
		if(TotalTransactionCountFailure==0)
			TotalTransactionCountFailure = response.getTotalTransactionCountFailure();
		else
			TotalTransactionCountFailure = response.getTotalTransactionCountFailure()- TotalTransactionCountFailure;
	
	
		if(TotalTransactionCountSuccess==0)
			TotalTransactionCountSuccess = response.getTotalTransactionCountSuccess();
		else
			TotalTransactionCountSuccess = response.getTotalTransactionCountSuccess()- TotalTransactionCountSuccess;
	
		if(TotalTransactionCountPerSecond==0)
			TotalTransactionCountPerSecond = response.getTotalTransactionCountFailure();
		else
			TotalTransactionCountPerSecond = response.getTotalTransactionCountPerSecond()- TotalTransactionCountPerSecond;
	

		}
	
	public long getLasduration() {
		return lasduration;
	}


	public void setLasduration(long lasduration) {
		this.lasduration = lasduration;
	}
	
	public void UpdateStat(TestStatistics response)
	{

		setLastRequestCountPerSecond(response.getLastRequestCountPerSecond());
		setLastTransactionDurationAverage(response.getLastTransactionDurationAverage());
		setLastVirtualUserCount(response.getLastVirtualUserCount());
		setTotalGlobalCountFailure(response.getTotalGlobalCountFailure());
		setTotalGlobalDownloadedBytes(response.getTotalGlobalDownloadedBytes());
		setTotalGlobalDownloadedBytesPerSecond(response.getTotalGlobalDownloadedBytesPerSecond());
		setTotalIterationCountFailure(response.getTotalIterationCountFailure());
		setTotalIterationCountSuccess(response.getTotalIterationCountSuccess());
		setTotalRequestCountFailure(response.getTotalRequestCountFailure());
		setTotalRequestCountPerSecond(response.getTotalRequestCountPerSecond());
		setTotalRequestCountSuccess(response.getTotalRequestCountSuccess());
		setTotalTransactionCountFailure(response.getTotalTransactionCountFailure());
		setTotalTransactionCountPerSecond(response.getTotalTransactionCountPerSecond());
		setTotalTransactionCountSucess(response.getTotalTransactionCountSuccess());
		setLastRequestDurationAverage(response.getTotalRequestDurationAverage());
	
	}
	public float getLastRequestCountPerSecond() {
		return LastRequestCountPerSecond;
	}

	public void UpdateRequestStat(ElementValues value,String Type)
	{
		switch(Type)
		{
		case "TRANSACTION" :
			setTotalTransactionFailurePerSecond(value.getFailurePerSecond());
			setTotalTransactionSuccessPerSecond(value.getSuccessPerSecond());
					
			break;
		case "REQUEST":
				setTotalRequestCountFailurePerSecond(value.getFailurePerSecond());
				setTotalRequestCountFailurePerSecond(value.getSuccessPerSecond());
				setTotalDownloadedBytesPerSecond(value.getDownloadedBytesPerSecond());
				setFailureRate(value.getFailureRate());
			break;
		}
	}
	
	public String[] GetFailureRateData()
	{
		DecimalFormat df = new DecimalFormat("#.##########");

		String[] result=new String[5];
		result[0]=FailureRate_MetricName;
		result[1]=FailureRate_Component;
		result[2]=FailureRate_Unit;
		result[3]=df.format(getFailureRate());
		result[4]=String.valueOf(FailureRate_status);

		return result;
		
	}
	
	public String[] GetTotalDownloadedBytesPerSecondData()
	{
		DecimalFormat df = new DecimalFormat("#.##########");

		String[] result=new String[5];
		result[0]=TotalDownloadedBytesPerSecond_MetricName;
		result[1]=TotalDownloadedBytesPerSecond_Component;
		result[2]=TotalDownloadedBytesPerSecond_Unit;
		result[3]=df.format(getTotalDownloadedBytesPerSecond());
		result[4]=String.valueOf(TotalDownloadedBytesPerSecond_status);

		return result;
		
	}
	
	//add the methods for request success, falure per second
	public String[] GetTotalRequestCountFailuerPerSecond()
	{
		DecimalFormat df = new DecimalFormat("#.##########");

		String[] result=new String[5];
		result[0]=TotalRequestCountFailurePerSecond_MetricName;
		result[1]=TotalRequestCountFailurePerSecond_Component;
		result[2]=TotalRequestCountFailurePerSecond_Unit;
		result[3]=df.format(getTotalRequestCountFailurePerSecond());
		result[4]=String.valueOf(TotalRequestCountFailuerPerSecond_status);
		
		return result;
		
	}
	
	///add the mtedods for transation success, failuer per second
	public String[] GetTotalRequestCountSucessPerSecond()
	{
		DecimalFormat df = new DecimalFormat("#.##########");

		String[] result=new String[5];
		result[0]=TotalRequestCountSuccessPerSecond_MetricName;
		result[1]=TotalRequestCountSuccessPerSecond_Component;
		result[2]=TotalRequestCountSuccessPerSecond_Unit;
		result[3]=df.format(getTotalRequestCountSuccessPerSecond());
		result[4]=String.valueOf(TotalRequestCountSuccessPerSecond_status);
		
		return result;
		
	}
	
	public String[] GetTotalTransactionFailurePerSecond()
	{
		DecimalFormat df = new DecimalFormat("#.##########");

		String[] result=new String[5];
		result[0]=TotalTransactionCountFailure_MetricName;
		result[1]=TotalTransactionCountFailure_Component;
		result[2]=TotalTransactionCountFailure_Unit;
		result[3]=df.format(getTotalTransactionFailurePerSecond());
		result[4]=String.valueOf(TotalTransactionCountFailurePerSecond_status);
		
		return result;
		
	}
	public String[] GetTotalTransactionSucessPerSecond()
	{
		DecimalFormat df = new DecimalFormat("#.##########");

		String[] result=new String[5];
		result[0]=TotalTransactionCountSucessPerSecond_MetricName;
		result[1]=TotalTransactionCountSucessPerSecond_Component;
		result[2]=TotalTransactionCountSucessPerSecond_Unit;
		result[3]=df.format(getTotalTransactionSuccessPerSecond());
		result[4]=String.valueOf(TotalTransactionCountSucessPerSecond_status);
		
		return result;
		
	}
	
	public String[] GetLastRequestDuration()
	{
		DecimalFormat df = new DecimalFormat("#.##########");

		String[] result=new String[5];
		result[0]=LastRequestDurationAverage_MetricName;
		result[1]=LastRequestDurationAverage_Component;
		result[2]=LastRequestDurationAverage_Unit;
		result[3]=df.format(getLastRequestDurationAverage());
		result[4]=String.valueOf(LastRequestDurationAverage_status);
		
		return result;
		
	}
	
	public String[] GetRequestCountData()
	{
		DecimalFormat df = new DecimalFormat("#.##########");

		String[] result=new String[5];
		result[0]=LastRequestCountPerSecond_MetricName;
		result[1]=LastRequestCountPerSecond_Component;
		result[2]=LastRequestCountPerSecond_Unit;
		result[3]=df.format(getLastRequestCountPerSecond());
		result[4]=String.valueOf(LastRequestCountPerSecond_status);
		
		return result;
		
	}
	public void setLastRequestCountPerSecond(Float lastRequestCountPerSecond) {
		if(lastRequestCountPerSecond!=null) {
			LastRequestCountPerSecond = lastRequestCountPerSecond;
			LastRequestCountPerSecond_data=true;
		}
		else
			LastRequestCountPerSecond_data=false;
	}


	public float getLastTransactionDurationAverage() {
		return LastTransactionDurationAverage;
	}
	public String[] GetTransactionDuractionData()
	{
		DecimalFormat df = new DecimalFormat("#.##########");

		String[] result=new String[5];
		result[0]=LastTransactionDurationAverage_MetricName;
		result[1]=LastTransactionDurationAverage_Component;
		result[2]=LastTransactionDurationAverage_Unit;
		result[3]=df.format(getLastTransactionDurationAverage());
		result[4]=String.valueOf(LastTransactionDurationAverage_status);
		
		return result;
		
	}


	public void setLastTransactionDurationAverage(Float lastTransactionDurationAverage) {
		if(lastTransactionDurationAverage!=null) {
			LastTransactionDurationAverage = lastTransactionDurationAverage / 1000;
			LastTransactionDurationAverage_data=true;
		}
		else
			LastTransactionDurationAverage_data=false;

	}


	public int getLastVirtualUserCount() {
		return LastVirtualUserCount;
	}


	public String[] GetVirtualUserCountData()
	{
		String[] result=new String[5];
		result[0]=LastVirtualUserCount_MetricName;
		result[1]=LastVirtualUserCount_Component;
		result[2]=LastVirtualUserCount_Unit;
		result[3]=String.valueOf(getLastVirtualUserCount());
		result[4]=String.valueOf(LastVirtualUserCount_status);
		return result;
		
	}
	
	public void setLastVirtualUserCount(Integer lastVirtualUserCount) {
		if(lastVirtualUserCount!=null) {
			LastVirtualUserCount = lastVirtualUserCount;
			LastVirtualUserCount_data=true;
		}
		else
			LastVirtualUserCount_data=false;
	}


	public long getTotalGlobalCountFailure() {
		return TotalGlobalCountFailure;
	}

	public String[] GetTotalGlobalCountFailureData()
	{
		String[] result=new String[5];
		result[0]=TotalGlobalCountFailure_MetricName;
		result[1]=TotalGlobalCountFailure_Component;
		result[2]=TotalGlobalCountFailure_Unit;
		result[3]=String.valueOf(getTotalGlobalCountFailure());
		result[4]=String.valueOf(TotalGlobalCountFailure_status);
		return result;
		
	}

	public void setTotalGlobalCountFailure(Long totalGlobalCountFailure) {
		if(totalGlobalCountFailure!=null) {
			if (TotalGlobalCountFailure == 0)
				TotalGlobalCountFailure = totalGlobalCountFailure;
			else
				TotalGlobalCountFailure = totalGlobalCountFailure - TotalGlobalCountFailure;

			TotalGlobalCountFailure_data=true;
		}
		else
			TotalGlobalCountFailure_data=false;
	}

	public List<String[]> GetNLData()
	{
		List<String[]> result = new ArrayList<String[]>();
		if(LastRequestCountPerSecond_data)
			result.add(GetRequestCountData());
		if(TotalGlobalCountFailure_data)
			result.add(GetTotalGlobalCountFailureData());
		if(TotalGlobalDownloadedBytes_data)
			result.add(GetTotalGlobalDownloadedBytesData());
		if(TotalGlobalDownloadedBytesPerSecond_data)
			result.add(GetTotalGlobalDownloadedBytesPerSecondData());
		if(TotalIterationCountFailure_data)
			result.add(GetTotalIterationCountFailureData());
		if(TotalIterationCountSuccess_data)
			result.add(GetTotalIterationCountSuccessData());

		if(TotalRequestCountFailure_data)
			result.add(GetTotalRequestCountFailureData());

		if(TotalRequestCountPerSecond_data)
			result.add(GetTotalRequestCountPerSecondData());

		if(TotalRequestCountSuccess_data)
			result.add(GetTotalRequestCountSuccessData());

		if(TotalTransactionCountFailure_data)
			result.add(GetTransactionCountFailureData());

		if(TotalTransactionCountPerSecond_data)
			result.add(GetTransactionCountPefSecondData());
		if(TotalTransactionCountSuccess_data)
			result.add(GetTransactionCountSucessData());

		if(LastTransactionDurationAverage_data)
			result.add(GetTransactionDuractionData());

		if(LastVirtualUserCount_data)
			result.add(GetVirtualUserCountData());

		if(LastRequestDurationAverage_data)
			result.add(GetLastRequestDuration());
		if(TotalDownloadedBytesPerSecond_data)
			result.add(GetTotalDownloadedBytesPerSecondData());
		if(TotalRequestCountFailurePerSecond_data)
			result.add(GetTotalRequestCountFailuerPerSecond());
		if(TotalRequestCountSuccessPerSecond_data)
			result.add(GetTotalRequestCountSucessPerSecond());
		if(TotalTransactionCountFailure_data)
			result.add(GetTotalTransactionFailurePerSecond());

		if(FailureRate_data)
			result.add(GetFailureRateData());
		
		return result;
	}

	public float getTotalGlobalDownloadedBytes() {
		return TotalGlobalDownloadedBytes;
	}


	public String[] GetTotalGlobalDownloadedBytesData()
	{
		DecimalFormat df = new DecimalFormat("#.##########");

		String[] result=new String[5];
		result[0]=TotalGlobalDownloadedBytes_MetricName;
		result[1]=TotalGlobalDownloadedBytes_Component;
		result[2]=TotalGlobalDownloadedBytes_Unit;
		result[3]=df.format(getTotalGlobalDownloadedBytes());
		result[4]=String.valueOf(TotalGlobalDownloadedBytes_status);
		return result;
		
	}
	
	public void setTotalGlobalDownloadedBytes(Long totalGlobalDownloadedBytes) {
		if(totalGlobalDownloadedBytes!=null) {
			if (TotalGlobalDownloadedBytes == 0)
				TotalGlobalDownloadedBytes = totalGlobalDownloadedBytes;
			else
				TotalGlobalDownloadedBytes = totalGlobalDownloadedBytes - TotalGlobalDownloadedBytes;

			TotalGlobalDownloadedBytes_data=true;
		}
		else
			TotalGlobalDownloadedBytes_data=false;
	}


	public float getTotalGlobalDownloadedBytesPerSecond() {
		return TotalGlobalDownloadedBytesPerSecond;
	}


	public String[] GetTotalGlobalDownloadedBytesPerSecondData()
	{
		DecimalFormat df = new DecimalFormat("#.##########");

		String[] result=new String[5];
		result[0]=TotalGlobalDownloadedBytesPerSecond_MetricName;
		result[1]=TotalGlobalDownloadedBytesPerSecond_Component;
		result[2]=TotalGlobalDownloadedBytesPerSecond_Unit;
		result[3]=df.format(getTotalGlobalDownloadedBytesPerSecond());
		result[4]=String.valueOf(TotalGlobalDownloadedBytesPerSecond_status);

		return result;
		
	}
	
	public void setTotalGlobalDownloadedBytesPerSecond(Float totalGlobalDownloadedBytesPerSecond) {
		if(totalGlobalDownloadedBytesPerSecond!=null) {
			if (TotalGlobalDownloadedBytesPerSecond == 0)
				TotalGlobalDownloadedBytesPerSecond = totalGlobalDownloadedBytesPerSecond;
			else
				TotalGlobalDownloadedBytesPerSecond = totalGlobalDownloadedBytesPerSecond - TotalGlobalDownloadedBytesPerSecond;

			TotalGlobalDownloadedBytesPerSecond_data=true;
		}
		else
			TotalGlobalDownloadedBytesPerSecond_data=false;
	}


	public long getTotalIterationCountFailure() {
		return TotalIterationCountFailure;
	}


	public String[] GetTotalIterationCountFailureData()
	{
		String[] result=new String[5];
		result[0]=TotalIterationCountFailure_MetricName;
		result[1]=TotalIterationCountFailure_Component;
		result[2]=TotalIterationCountFailure_Unit;
		result[3]=String.valueOf(getTotalIterationCountFailure());
		result[4]=String.valueOf(TotalIterationCountFailure_status);

		return result;
		
	}
	
	public void setTotalIterationCountFailure(Long totalIterationCountFailure) {
		if(totalIterationCountFailure!=null)
		{
			if (TotalIterationCountFailure == 0)
				TotalIterationCountFailure = totalIterationCountFailure;
			else
				TotalIterationCountFailure = totalIterationCountFailure - TotalIterationCountFailure;

			TotalIterationCountFailure_data=true;
		}
		else
			TotalIterationCountFailure_data=false;
	}


	public long getTotalIterationCountSuccess() {
		return TotalIterationCountSuccess;
	}


	public String[] GetTotalIterationCountSuccessData()
	{
		String[] result=new String[5];
		result[0]=TotalIterationCountSuccess_MetricName;
		result[1]=TotalIterationCountSuccess_Component;
		result[2]=TotalIterationCountSuccess_Unit;
		result[3]=String.valueOf(getTotalIterationCountSuccess());
		result[4]=String.valueOf(TotalIterationCountSuccess_status);
		return result;
		
	}

	public void setTotalIterationCountSuccess(Long totalIterationCountSuccess) {
		if(totalIterationCountSuccess !=null) {
			if (TotalIterationCountSuccess == 0)
				TotalIterationCountSuccess = totalIterationCountSuccess;
			else
				TotalIterationCountSuccess = totalIterationCountSuccess - TotalIterationCountSuccess;

			TotalIterationCountSuccess_data=true;
		}
		else
			TotalIterationCountSuccess_data=false;
	}


	public long getTotalRequestCountFailure() {
		return TotalRequestCountFailure;
	}


	public String[] GetTotalRequestCountFailureData()
	{
		String[] result=new String[5];
		result[0]=TotalRequestCountFailure_MetricName;
		result[1]=TotalRequestCountFailure_Component;
		result[2]=TotalRequestCountFailure_Unit;
		result[3]=String.valueOf(getTotalRequestCountFailure());
		result[4]=String.valueOf(TotalRequestCountFailure_status);
		
		return result;
		
	}

	public void setTotalRequestCountFailure(Long totalRequestCountFailure) {
		if(totalRequestCountFailure!=null) {
			if (TotalRequestCountFailure == 0)
				TotalRequestCountFailure = totalRequestCountFailure;
			else
				TotalRequestCountFailure = totalRequestCountFailure - TotalRequestCountFailure;

			TotalRequestCountFailure_data=true;
		}
		else
			TotalRequestCountFailure_data=false;
	}


	public float getTotalRequestCountPerSecond() {
		return TotalRequestCountPerSecond;
	}

	public String[] GetTotalRequestCountPerSecondData()
	{
		DecimalFormat df = new DecimalFormat("#.##########");

		String[] result=new String[5];
		result[0]=TotalRequestCountPerSecond_MetricName;
		result[1]=TotalRequestCountPerSecond_Component;
		result[2]=TotalRequestCountPerSecond_Unit;
		result[3]=df.format(getTotalRequestCountPerSecond());
		result[4]=String.valueOf(TotalRequestCountPerSecond_status);
		
		return result;
		
	}
	public void setTotalRequestCountPerSecond(Float totalRequestCountPerSecond) {
		if(totalRequestCountPerSecond != null) {
			if (TotalRequestCountPerSecond == 0)
				TotalRequestCountPerSecond = totalRequestCountPerSecond;
			else
				TotalRequestCountPerSecond = totalRequestCountPerSecond - TotalRequestCountPerSecond;

			TotalRequestCountPerSecond_data=true;
		}
		else
			TotalRequestCountPerSecond_data=false;
	}


	public long getTotalRequestCountSuccess() {
		return TotalRequestCountSuccess;
	}

	public String[] GetTotalRequestCountSuccessData()
	{
		String[] result=new String[5];
		result[0]=TotalRequestCountSuccess_MetricName;
		result[1]=TotalRequestCountSuccess_Component;
		result[2]=TotalRequestCountSuccess_Unit;
		result[3]=String.valueOf(getTotalRequestCountSuccess());
		result[4]=String.valueOf(TotalRequestCountSuccess_status);
		
		return result;
		
	}
	
	public void setTotalRequestCountSuccess(Long totalRequestCountSuccess) {
		if(totalRequestCountSuccess!=null) {
			if (TotalRequestCountSuccess == 0)
				TotalRequestCountSuccess = totalRequestCountSuccess;
			else
				TotalRequestCountSuccess = totalRequestCountSuccess - TotalRequestCountSuccess;

			TotalRequestCountSuccess_data=true;
		}
		else
			TotalRequestCountSuccess_data=false;
	}

	

	public long getTotalTransactionCountFailure() {
		return TotalTransactionCountFailure;
	}


	public String[] GetTransactionCountFailureData()
	{
		String[] result=new String[5];
		result[0]=TotalTransactionCountFailure_MetricName;
		result[1]=TotalTransactionCountFailure_Component;
		result[2]=TotalTransactionCountFailure_Unit;
		result[3]=String.valueOf(getTotalTransactionCountFailure());
		result[4]=String.valueOf(TotalTransactionCountFailure_status);
		
		return result;
		
	}
	
	public void setTotalTransactionCountFailure(Long totalTransactionCountFailure) {
		if(totalTransactionCountFailure!=null) {
			if (TotalTransactionCountFailure == 0)
				TotalTransactionCountFailure = totalTransactionCountFailure;
			else
				TotalTransactionCountFailure = totalTransactionCountFailure - TotalTransactionCountFailure;

			TotalTransactionCountFailure_data=true;
		}
		else
			TotalTransactionCountFailure_data=false;
	}
	
	public long getTotalTransactionCountSucess() {
		return TotalTransactionCountSuccess;
	}


	public String[] GetTransactionCountSucessData()
	{
		String[] result=new String[5];
		result[0]=TotalTransactionCountSucess_MetricName;
		result[1]=TotalTransactionCountSucess_Component;
		result[2]=TotalTransactionCountSucess_Unit;
		result[3]=String.valueOf(getTotalTransactionCountSucess());
		result[4]=String.valueOf(TotalTransactionCountSucess_status);
		
		return result;
		
	}
	
	public void setTotalTransactionCountSucess(Long totalTransactionCountFailure) {
		if(totalTransactionCountFailure!=null) {
			if (TotalTransactionCountSuccess == 0)
				TotalTransactionCountSuccess = totalTransactionCountFailure;
			else
				TotalTransactionCountSuccess = totalTransactionCountFailure - TotalTransactionCountSuccess;

			TotalTransactionCountSuccess_data=true;
		}
		else
			TotalTransactionCountSuccess_data=false;
	}

	public float getTotalTransactionCountPerSecond() {
		return TotalTransactionCountPerSecond;
	}

	public String[] GetTransactionCountPefSecondData()
	{
		DecimalFormat df = new DecimalFormat("#.##########");

		String[] result=new String[5];
		result[0]=TotalTransactionCountPerSecond_MetricName;
		result[1]=TotalTransactionCountPerSecond_Component;
		result[2]=TotalTransactionCountPerSecond_Unit;
		result[3]=df.format(getTotalTransactionCountPerSecond());
		result[4]=String.valueOf(TotalTransactionCountPerSecond_status);
		
		return result;
		
	}
	
	public void setTotalTransactionCountPerSecond(Float totalTransactionCountPerSecond) {
		if(totalTransactionCountPerSecond!=null) {
			if (TotalTransactionCountPerSecond == 0)
				TotalTransactionCountPerSecond = totalTransactionCountPerSecond;
			else
				TotalTransactionCountPerSecond = totalTransactionCountPerSecond - TotalTransactionCountPerSecond;

			TotalTransactionCountPerSecond_data=true;
		}
		else
			TotalTransactionCountPerSecond_data=false;
	}


	
	
}
