package model.aprioritid;

import java.util.ArrayList;
import java.util.List;

public class ItemSetBar {
	private Integer tid;
	private List<Integer> candidateItemsetIds;
	
	public ItemSetBar()
	{
		candidateItemsetIds = new ArrayList<Integer>();
	}
	
	public Integer getTid() {
		return tid;
	}
	
	public void setTid(Integer tid) {
		this.tid = tid;
	}
	
	public List<Integer> getCandidateItemsetId() {
		return candidateItemsetIds;
	}
	
	public void setCandidateItemsetId(List<Integer> candidateItemsetId) {
		this.candidateItemsetIds = candidateItemsetId;
	}
}
